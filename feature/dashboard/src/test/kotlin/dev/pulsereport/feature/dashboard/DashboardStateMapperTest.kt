package dev.pulsereport.feature.dashboard

import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepSummary
import java.time.Duration
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStateMapperTest {

    private val today = LocalDate.of(2026, 7, 18)

    private fun rawData(
        weekSteps: List<DailySteps> = emptyList(),
        sleep: SleepSummary? = null,
        heart: HeartSummary? = null,
        activeCaloriesKcal: Double = 0.0,
        hydrationLitres: Double = 0.0,
        weekWeight: List<DailyValue> = emptyList(),
    ) = DashboardRawData(
        selectedDate = today,
        weekSteps = weekSteps,
        sleep = sleep,
        heart = heart,
        activeCaloriesKcal = activeCaloriesKcal,
        hydrationLitres = hydrationLitres,
        weekWeight = weekWeight,
    )

    @Test
    fun `derives walking distance from today's steps`() {
        val state = mapDashboardState(rawData(weekSteps = listOf(DailySteps(today, 8_432))))

        assertEquals(6.1, state.steps.distanceKm, 0.01)
    }

    @Test
    fun `a full night of restful sleep scores well`() {
        val sleep = SleepSummary(
            total = Duration.ofHours(8),
            deep = Duration.ofMinutes(120),
            light = Duration.ofMinutes(180),
            rem = Duration.ofMinutes(180),
        )

        val state = mapDashboardState(rawData(sleep = sleep))

        assertEquals(Duration.ofHours(8), state.sleep.duration)
        assert(state.sleep.score >= 70) { "expected a good score, was ${state.sleep.score}" }
        assertEquals(listOf(120f, 180f, 180f), state.sleep.stageWeights)
    }

    @Test
    fun `missing sleep data falls back without crashing the segmented bar`() {
        val state = mapDashboardState(rawData(sleep = null))

        assertEquals(0, state.sleep.score)
        assertEquals("No data", state.sleep.scoreLabel)
        assertEquals(listOf(1f, 1f, 1f), state.sleep.stageWeights)
        assert(state.sleep.stageWeights.all { it > 0f })
    }

    @Test
    fun `missing heart data yields an empty series instead of a crash`() {
        val state = mapDashboardState(rawData(heart = null))

        assertEquals(0, state.heart.restingBpm)
        assertEquals(emptyList<Float>(), state.heart.series)
    }

    @Test
    fun `week-old weight against today's weight gives the weekly delta`() {
        val weekWeight = listOf(
            DailyValue(today.minusDays(6), 79.0),
            DailyValue(today.minusDays(5), 78.9),
            DailyValue(today.minusDays(4), 78.8),
            DailyValue(today.minusDays(3), 78.7),
            DailyValue(today.minusDays(2), 78.6),
            DailyValue(today.minusDays(1), 78.5),
            DailyValue(today, 78.4),
        )

        val state = mapDashboardState(rawData(weekWeight = weekWeight))

        assertEquals(78.4, state.weight.currentKg, 0.001)
        assertEquals(-0.6, state.weight.weekDeltaKg, 0.001)
    }

    @Test
    fun `synced source count reflects only the metrics that returned data`() {
        val state = mapDashboardState(
            rawData(
                weekSteps = listOf(DailySteps(today, 8_432)),
                sleep = SleepSummary(Duration.ofHours(7), Duration.ZERO, Duration.ofHours(7), Duration.ZERO),
                heart = null,
                activeCaloriesKcal = 0.0,
                hydrationLitres = 1.6,
                weekWeight = listOf(DailyValue(today, 78.4)),
            ),
        )

        assertEquals(4, state.syncedSourceCount)
    }

    @Test
    fun `no data anywhere yields a zero synced source count`() {
        val state = mapDashboardState(rawData())

        assertEquals(0, state.syncedSourceCount)
    }
}
