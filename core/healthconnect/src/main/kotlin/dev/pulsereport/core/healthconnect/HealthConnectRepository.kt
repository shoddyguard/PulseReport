package dev.pulsereport.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HealthConnectAvailability
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepStageSegment
import dev.pulsereport.core.model.SleepSummary
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val HEART_SERIES_POINTS = 8

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthRepository {

    override val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
    )

    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    private val zone: ZoneId = ZoneId.systemDefault()

    override fun availability(): HealthConnectAvailability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.NOT_AVAILABLE
        }

    override suspend fun hasAllPermissions(): Boolean =
        client.permissionController.getGrantedPermissions().containsAll(requiredPermissions)

    override suspend fun getContributingPackages(metric: HealthMetric, days: Int): Set<String> = safeRead(
        default = emptySet(),
    ) {
        val end = LocalDate.now(zone).plusDays(1).atStartOfDay()
        val start = LocalDate.now(zone).minusDays((days - 1).toLong()).atStartOfDay()
        val timeRangeFilter = TimeRangeFilter.between(start, end)

        val records: List<Record> = when (metric) {
            HealthMetric.STEPS -> readAllRecords(StepsRecord::class, timeRangeFilter)
            HealthMetric.HEART_RATE -> readAllRecords(HeartRateRecord::class, timeRangeFilter)
            HealthMetric.SLEEP -> readAllRecords(SleepSessionRecord::class, timeRangeFilter)
            HealthMetric.CALORIES -> readAllRecords(ActiveCaloriesBurnedRecord::class, timeRangeFilter)
            HealthMetric.HYDRATION -> readAllRecords(HydrationRecord::class, timeRangeFilter)
            HealthMetric.WEIGHT -> readAllRecords(WeightRecord::class, timeRangeFilter)
        }

        records.map { record -> record.metadata.dataOrigin.packageName }.toSet()
    }

    /**
     * Reads raw records instead of using [HealthConnectClient.aggregateGroupByPeriod]. Steps
     * is one of the "Activity" category types Health Connect aggregates via a cross-app
     * priority list, and that list can end up stale (e.g. after a contributing app's write
     * permission is revoked): the platform aggregate then silently returns null totals for
     * every bucket even though the underlying records are still readable. Reading raw records
     * sidesteps that entirely, and also lets us apply our own [priority] instead of Health
     * Connect's (which is user-configured only via its own Settings app, not by us).
     */
    override suspend fun getDailySteps(
        anchor: LocalDate,
        days: Int,
        priority: List<String>,
    ): List<DailySteps> = safeRead(
        default = zeroFilledDailySteps(emptyMap(), endDate = anchor, days = days),
    ) {
        val start = anchor.minusDays((days - 1).toLong()).atStartOfDay()
        val end = anchor.plusDays(1).atStartOfDay()

        val records = readAllRecords(StepsRecord::class, TimeRangeFilter.between(start, end))
        val byDayThenOrigin = records
            .groupBy { record -> record.startTime.atZone(zone).toLocalDate() }
            .mapValues { (_, dayRecords) -> dayRecords.groupBy { record -> record.metadata.dataOrigin.packageName } }

        val totalsByDate = selectByPriority(byDayThenOrigin, priority)
            .mapValues { (_, winning) -> winning.sumOf { record -> record.count } }

        zeroFilledDailySteps(totalsByDate, endDate = anchor, days = days)
    }

    override suspend fun getSleep(anchor: LocalDate, priority: List<String>): SleepSummary? = safeRead(default = null) {
        val session = selectSleepSession(anchor, priority) ?: return@safeRead null

        val stages = session.stages.map { stage -> stage.stage to Duration.between(stage.startTime, stage.endTime) }
        val (deep, light, rem) = sleepStageBreakdown(stages)

        SleepSummary(
            total = Duration.between(session.startTime, session.endTime),
            deep = deep,
            light = light,
            rem = rem,
        )
    }

    override suspend fun getSleepDetail(anchor: LocalDate, priority: List<String>): SleepDetail? = safeRead(default = null) {
        val session = selectSleepSession(anchor, priority) ?: return@safeRead null

        SleepDetail(
            start = session.startTime,
            end = session.endTime,
            total = Duration.between(session.startTime, session.endTime),
            stages = session.stages.map { stage ->
                SleepStageSegment(stage = sleepStageOf(stage.stage), start = stage.startTime, end = stage.endTime)
            },
        )
    }

    /** The sleep session covering the night ending on [anchor], resolved by [priority] when more than one app wrote one. */
    private suspend fun selectSleepSession(anchor: LocalDate, priority: List<String>): SleepSessionRecord? {
        val night = anchor.minusDays(1)
        val start = night.atStartOfDay()
        val end = anchor.plusDays(1).atStartOfDay()

        val records = readAllRecords(SleepSessionRecord::class, TimeRangeFilter.between(start, end))
        val nightRecords = records.filter { record -> record.startTime.atZone(zone).toLocalDate() == night }
        val byOrigin = nightRecords.groupBy { record -> record.metadata.dataOrigin.packageName }
        return selectByPriority(mapOf(night to byOrigin), priority)[night]?.firstOrNull()
    }

    override suspend fun getHeart(anchor: LocalDate, priority: List<String>): HeartSummary? = safeRead(default = null) {
        val start = anchor.atStartOfDay()
        val end = anchor.plusDays(1).atStartOfDay()

        val records = readAllRecords(HeartRateRecord::class, TimeRangeFilter.between(start, end))
        val byOrigin = records.groupBy { record -> record.metadata.dataOrigin.packageName }
        val winningRecords = selectByPriority(mapOf(anchor to byOrigin), priority)[anchor] ?: emptyList()

        val samples = winningRecords
            .flatMap { record -> record.samples }
            .sortedBy { sample -> sample.time }
        if (samples.isEmpty()) return@safeRead null

        HeartSummary(
            restingBpm = samples.minOf { sample -> sample.beatsPerMinute }.toInt(),
            series = downsampleSeries(samples.map { sample -> sample.beatsPerMinute.toFloat() }, HEART_SERIES_POINTS),
        )
    }

    /** Reads raw records rather than the platform aggregate; see [getDailySteps]. */
    override suspend fun getActiveCalories(anchor: LocalDate, priority: List<String>): Double = safeRead(default = 0.0) {
        val records = readAllRecords(ActiveCaloriesBurnedRecord::class, dayRangeFilter(anchor))
        val byOrigin = records.groupBy { record -> record.metadata.dataOrigin.packageName }
        val winning = selectByPriority(mapOf(anchor to byOrigin), priority)[anchor] ?: emptyList()
        winning.sumOf { record -> record.energy.inKilocalories }
    }

    /** Reads raw records rather than the platform aggregate; see [getDailySteps]. */
    override suspend fun getHydration(anchor: LocalDate, priority: List<String>): Double = safeRead(default = 0.0) {
        val records = readAllRecords(HydrationRecord::class, dayRangeFilter(anchor))
        val byOrigin = records.groupBy { record -> record.metadata.dataOrigin.packageName }
        val winning = selectByPriority(mapOf(anchor to byOrigin), priority)[anchor] ?: emptyList()
        winning.sumOf { record -> record.volume.inLiters }
    }

    /** Reads raw records rather than the platform aggregate; see [getDailySteps]. */
    override suspend fun getDailyWeight(
        anchor: LocalDate,
        days: Int,
        priority: List<String>,
    ): List<DailyValue> = safeRead(
        default = carriedForwardDailyValues(emptyMap(), endDate = anchor, days = days),
    ) {
        val start = anchor.minusDays((days - 1).toLong()).atStartOfDay()
        val end = anchor.plusDays(1).atStartOfDay()

        val records = readAllRecords(WeightRecord::class, TimeRangeFilter.between(start, end))
        val byDayThenOrigin = records
            .groupBy { record -> record.time.atZone(zone).toLocalDate() }
            .mapValues { (_, dayRecords) -> dayRecords.groupBy { record -> record.metadata.dataOrigin.packageName } }

        val averagesByDate = selectByPriority(byDayThenOrigin, priority)
            .filterValues { winning -> winning.isNotEmpty() }
            .mapValues { (_, winning) -> winning.map { record -> record.weight.inKilograms }.average() }

        carriedForwardDailyValues(averagesByDate, endDate = anchor, days = days)
    }

    private fun dayRangeFilter(day: LocalDate): TimeRangeFilter =
        TimeRangeFilter.between(day.atStartOfDay(), day.plusDays(1).atStartOfDay())

    /** Reads every page of [recordType] records in [timeRangeFilter], newest paging aside. */
    private suspend fun <T : Record> readAllRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter,
    ): List<T> {
        val records = mutableListOf<T>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(recordType = recordType, timeRangeFilter = timeRangeFilter, pageToken = pageToken),
            )
            records += response.records
            pageToken = response.pageToken
        } while (!pageToken.isNullOrEmpty())
        return records
    }

    private suspend fun <T> safeRead(default: T, block: suspend () -> T): T =
        try {
            block()
        } catch (e: SecurityException) {
            default
        }
}
