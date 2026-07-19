package dev.pulsereport.feature.health

import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HealthConnectAvailability
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepSummary
import java.time.LocalDate

/**
 * A [HealthRepository] fake for the feature:health ViewModel tests. Every read is configurable
 * by date (steps/weight) or as a single canned value (sleep detail/heart/calories/hydration);
 * unconfigured reads default to zero/empty/null, matching what an unseeded day looks like.
 * [receivedPriorities] records the last priority list each metric's read was called with.
 */
class FakeHealthRepository(
    private val sleepDetail: SleepDetail? = null,
    private val stepsByDate: Map<LocalDate, Long> = emptyMap(),
    private val weightByDate: Map<LocalDate, Double> = emptyMap(),
    private val heart: HeartSummary? = null,
    private val activeCaloriesKcal: Double = 0.0,
    private val hydrationLitres: Double = 0.0,
) : HealthRepository {

    val receivedPriorities: MutableMap<HealthMetric, List<String>> = mutableMapOf()

    override val requiredPermissions: Set<String> = emptySet()

    override fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun hasAllPermissions(): Boolean = true

    override suspend fun getContributingPackages(metric: HealthMetric, days: Int): Set<String> = emptySet()

    override suspend fun getDailySteps(anchor: LocalDate, days: Int, priority: List<String>): List<DailySteps> {
        receivedPriorities[HealthMetric.STEPS] = priority
        return (days - 1 downTo 0).map { offset ->
            val date = anchor.minusDays(offset.toLong())
            DailySteps(date, stepsByDate[date] ?: 0L)
        }
    }

    override suspend fun getSleep(anchor: LocalDate, priority: List<String>): SleepSummary? = null

    override suspend fun getSleepDetail(anchor: LocalDate, priority: List<String>): SleepDetail? {
        receivedPriorities[HealthMetric.SLEEP] = priority
        return sleepDetail
    }

    override suspend fun getHeart(anchor: LocalDate, priority: List<String>): HeartSummary? {
        receivedPriorities[HealthMetric.HEART_RATE] = priority
        return heart
    }

    override suspend fun getActiveCalories(anchor: LocalDate, priority: List<String>): Double {
        receivedPriorities[HealthMetric.CALORIES] = priority
        return activeCaloriesKcal
    }

    override suspend fun getHydration(anchor: LocalDate, priority: List<String>): Double {
        receivedPriorities[HealthMetric.HYDRATION] = priority
        return hydrationLitres
    }

    override suspend fun getDailyWeight(anchor: LocalDate, days: Int, priority: List<String>): List<DailyValue> {
        receivedPriorities[HealthMetric.WEIGHT] = priority
        return (days - 1 downTo 0).map { offset ->
            val date = anchor.minusDays(offset.toLong())
            DailyValue(date, weightByDate[date] ?: 0.0)
        }
    }
}
