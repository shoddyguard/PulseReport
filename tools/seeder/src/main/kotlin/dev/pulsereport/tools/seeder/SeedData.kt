package dev.pulsereport.tools.seeder

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random

private const val SEED_DAYS = 30
private val ZONE: ZoneId = ZoneId.systemDefault()

/** All the records [SeedData.generate] produces, grouped by Health Connect record type. */
data class SeedRecords(
    val steps: List<StepsRecord>,
    val heartRate: List<HeartRateRecord>,
    val sleep: List<SleepSessionRecord>,
    val activeCalories: List<ActiveCaloriesBurnedRecord>,
    val hydration: List<HydrationRecord>,
    val weight: List<WeightRecord>,
) {
    val totalCount: Int
        get() = steps.size + heartRate.size + sleep.size + activeCalories.size + hydration.size + weight.size
}

/**
 * Health data anchored to [today], with value ranges matching `DashboardMockData.kt`.
 *
 * 2026-07-18: some seeded records ended up with a start time in the future - Health Connect
 * rejects those outright - because we were reusing the zone offset computed for a record's
 * originally scheduled local time even after truncating that record's actual end to `now`.
 * Every generator below now takes `now`, drops any record that hasn't started yet, and
 * truncates in-progress records to `now` while recomputing their offset via
 * `ZONE.rules.getOffset(end)` for the truncated instant, not the original schedule.
 *
 * [scale] multiplies every generated quantity (steps, bpm, kcal, litres, kg, sleep
 * duration). The seeder's "alt" build flavor seeds a scaled-down copy of the same days
 * under a second package name, so we've got a real multi-source clash to test Sources-screen
 * priority selection against.
 */
object SeedData {

    fun generate(today: LocalDate = LocalDate.now(), scale: Double = 1.0): SeedRecords {
        val random = Random(42)
        val now = Instant.now()
        val days = (SEED_DAYS - 1 downTo 0).map { today.minusDays(it.toLong()) }

        return SeedRecords(
            steps = days.mapNotNull { day -> stepsFor(day, random, now, scale) },
            heartRate = days.flatMap { day -> heartRateSessionsFor(day, random, now, scale) },
            sleep = days.mapNotNull { day -> sleepFor(day, random, now, scale) },
            activeCalories = days.mapNotNull { day -> activeCaloriesFor(day, random, now, scale) },
            hydration = days.flatMap { day -> hydrationFor(day, random, now, scale) },
            weight = days.mapIndexedNotNull { daysElapsed, day -> weightFor(day, daysElapsed, random, now, scale) },
        )
    }

    private val wristWorn = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
    private fun manual(): Metadata = Metadata.manualEntry()

    private fun stepsFor(day: LocalDate, random: Random, now: Instant, scale: Double): StepsRecord? {
        val (start, startOffset) = instantAt(day, LocalTime.of(8, 0))
        val (originalEnd, _) = instantAt(day, LocalTime.of(20, 0))
        val count = (random.nextLong(4_000, 11_001) * scale).toLong()
        if (!start.isBefore(now)) return null
        val end = minOf(originalEnd, now)
        return StepsRecord(
            startTime = start,
            startZoneOffset = startOffset,
            endTime = end,
            endZoneOffset = ZONE.rules.getOffset(end),
            count = count,
            metadata = wristWorn,
        )
    }

    private fun heartRateSessionsFor(day: LocalDate, random: Random, now: Instant, scale: Double): List<HeartRateRecord> =
        listOf(
            HeartRateSession(hour = 7, bpmRange = 55..65),
            HeartRateSession(hour = 13, bpmRange = 70..115),
            HeartRateSession(hour = 21, bpmRange = 60..80),
        ).mapNotNull { session -> session.toRecord(day, random, now, scale) }

    private data class HeartRateSession(val hour: Int, val bpmRange: IntRange) {
        fun toRecord(day: LocalDate, random: Random, now: Instant, scale: Double): HeartRateRecord? {
            val (start, startOffset) = instantAt(day, LocalTime.of(hour, 0))
            val allSamples = (0 until 10).map { minute ->
                HeartRateRecord.Sample(
                    time = start.plusSeconds(minute * 60L),
                    beatsPerMinute = (random.nextInt(bpmRange.first, bpmRange.last + 1) * scale).toLong(),
                )
            }
            val samples = allSamples.filter { it.time.isBefore(now) }
            if (samples.isEmpty()) return null

            val (originalEnd, _) = instantAt(day, LocalTime.of(hour, 10))
            val end = minOf(originalEnd, now)
            return HeartRateRecord(
                startTime = start,
                startZoneOffset = startOffset,
                endTime = end,
                endZoneOffset = ZONE.rules.getOffset(end),
                samples = samples,
                metadata = wristWorn,
            )
        }
    }

    private fun sleepFor(day: LocalDate, random: Random, now: Instant, scale: Double): SleepSessionRecord? {
        val (start, startOffset) = instantAt(day.minusDays(1), LocalTime.of(23, 0))
        val durationMinutes = (random.nextInt(390, 511) * scale).toInt() // 6.5h - 8.5h
        val originalEnd = start.plusSeconds(durationMinutes * 60L)
        if (!start.isBefore(now)) return null

        val stageWeights = listOf(0.35, 0.25, 0.20, 0.20)
        val stageTypes = listOf(
            SleepSessionRecord.STAGE_TYPE_LIGHT,
            SleepSessionRecord.STAGE_TYPE_DEEP,
            SleepSessionRecord.STAGE_TYPE_LIGHT,
            SleepSessionRecord.STAGE_TYPE_REM,
        )
        var cursor = start
        val allStages = stageWeights.zip(stageTypes).mapIndexed { index, (weight, stageType) ->
            val stageEnd = if (index == stageWeights.lastIndex) {
                originalEnd
            } else {
                cursor.plusSeconds((durationMinutes * 60L * weight).toLong())
            }
            SleepSessionRecord.Stage(startTime = cursor, endTime = stageEnd, stage = stageType).also {
                cursor = stageEnd
            }
        }

        val end = minOf(originalEnd, now)
        val stages = truncateStages(allStages, end)
        if (stages.isEmpty()) return null

        return SleepSessionRecord(
            startTime = start,
            startZoneOffset = startOffset,
            endTime = end,
            endZoneOffset = ZONE.rules.getOffset(end),
            metadata = wristWorn,
            title = null,
            notes = null,
            stages = stages,
        )
    }

    private fun truncateStages(
        stages: List<SleepSessionRecord.Stage>,
        cutoff: Instant,
    ): List<SleepSessionRecord.Stage> =
        stages.mapNotNull { stage ->
            when {
                !stage.startTime.isBefore(cutoff) -> null
                stage.endTime.isAfter(cutoff) -> SleepSessionRecord.Stage(stage.startTime, cutoff, stage.stage)
                else -> stage
            }
        }

    private fun activeCaloriesFor(day: LocalDate, random: Random, now: Instant, scale: Double): ActiveCaloriesBurnedRecord? {
        val (start, startOffset) = instantAt(day, LocalTime.of(17, 0))
        val (originalEnd, _) = instantAt(day, LocalTime.of(18, 0))
        val energy = Energy.kilocalories(random.nextDouble(400.0, 800.0) * scale)
        if (!start.isBefore(now)) return null
        val end = minOf(originalEnd, now)
        return ActiveCaloriesBurnedRecord(
            startTime = start,
            startZoneOffset = startOffset,
            endTime = end,
            endZoneOffset = ZONE.rules.getOffset(end),
            energy = energy,
            metadata = wristWorn,
        )
    }

    private fun hydrationFor(day: LocalDate, random: Random, now: Instant, scale: Double): List<HydrationRecord> {
        val drinkCount = random.nextInt(5, 8)
        return (0 until drinkCount).mapNotNull {
            val minuteOfWindow = random.nextInt(0, (21 - 8) * 60)
            val time = LocalTime.of(8, 0).plusMinutes(minuteOfWindow.toLong())
            val (start, startOffset) = instantAt(day, time)
            if (!start.isBefore(now)) return@mapNotNull null
            val end = minOf(start.plusSeconds(60), now)
            HydrationRecord(
                startTime = start,
                startZoneOffset = startOffset,
                endTime = end,
                endZoneOffset = ZONE.rules.getOffset(end),
                volume = Volume.liters(random.nextDouble(0.20, 0.35) * scale),
                metadata = manual(),
            )
        }
    }

    private fun weightFor(day: LocalDate, daysElapsed: Int, random: Random, now: Instant, scale: Double): WeightRecord? {
        val (time, offset) = instantAt(day, LocalTime.of(7, 30))
        val trend = 79.0 - 0.02 * daysElapsed
        val noise = random.nextDouble(-0.3, 0.3)
        if (!time.isBefore(now)) return null
        return WeightRecord(
            time = time,
            zoneOffset = offset,
            weight = Mass.kilograms((trend + noise) * scale),
            metadata = manual(),
        )
    }

    private fun instantAt(date: LocalDate, time: LocalTime): Pair<Instant, ZoneOffset> {
        val dateTime = LocalDateTime.of(date, time)
        val offset = ZONE.rules.getOffset(dateTime)
        return dateTime.toInstant(offset) to offset
    }
}
