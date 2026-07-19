package dev.pulsereport.feature.health

import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HeartSummary
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleMetricDetailStateMapperTest {

    private val today = LocalDate.of(2026, 7, 18)

    @Test
    fun `steps state computes goal fraction and distance from the last (today) entry`() {
        val week = (0..6).map { offset -> DailySteps(today.minusDays((6 - offset).toLong()), 1_000L * (offset + 1)) }

        val state = mapStepsDetailState(week)

        assertEquals(7_000, state.todayCount)
        assertEquals(10_000, state.goalCount)
        assertEquals(0.7f, state.goalFraction, 0.001f)
        assertEquals(5.0, state.distanceKm, 0.001)
        assertEquals(listOf(1_000, 2_000, 3_000, 4_000, 5_000, 6_000, 7_000), state.weekCounts)
    }

    @Test
    fun `steps state with no data defaults to zero`() {
        val state = mapStepsDetailState(emptyList())

        assertEquals(0, state.todayCount)
        assertEquals(0f, state.goalFraction, 0f)
    }

    @Test
    fun `weight state computes the delta between the first and last reading in the window`() {
        val week = listOf(DailyValue(today.minusDays(6), 80.0), DailyValue(today, 78.4))

        val state = mapWeightDetailState(week)

        assertEquals(78.4, state.currentKg, 0.001)
        assertEquals(-1.6, state.weekDeltaKg, 0.001)
    }

    @Test
    fun `heart state with a summary has data`() {
        val state = mapHeartDetailState(HeartSummary(restingBpm = 58, series = listOf(1f, 2f)))

        assertTrue(state.hasData)
        assertEquals(58, state.restingBpm)
    }

    @Test
    fun `heart state with no summary has no data`() {
        val state = mapHeartDetailState(null)

        assertFalse(state.hasData)
    }

    @Test
    fun `calories state computes goal fraction`() {
        val state = mapCaloriesDetailState(350.0)

        assertEquals(350, state.burnedKcal)
        assertEquals(700, state.goalKcal)
        assertEquals(0.5f, state.goalFraction, 0.001f)
    }

    @Test
    fun `hydration state computes filled slots from the goal fraction`() {
        val state = mapHydrationDetailState(1.2)

        assertEquals(1.2, state.consumedLitres, 0.001)
        assertEquals(2.5, state.goalLitres, 0.001)
        assertEquals(5, state.totalSlots)
        assertEquals(2, state.filledSlots)
    }
}
