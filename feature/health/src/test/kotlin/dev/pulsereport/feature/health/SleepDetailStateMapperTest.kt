package dev.pulsereport.feature.health

import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepStage
import dev.pulsereport.core.model.SleepStageSegment
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepDetailStateMapperTest {

    private val zone = ZoneId.systemDefault()

    /** Builds an [java.time.Instant] on a fixed reference night, in the JVM's own zone so it round-trips through [mapSleepDetailState]'s `ZoneId.systemDefault()` formatting. */
    private fun at(hour: Int, minute: Int, dayOffset: Long = 0) =
        LocalDateTime.of(2026, 7, 18, hour, minute).plusDays(dayOffset).atZone(zone).toInstant()

    @Test
    fun `formats duration, bedtime and waketime`() {
        val start = at(22, 0)
        val end = at(6, 3, dayOffset = 1)
        val detail = SleepDetail(
            start = start,
            end = end,
            total = Duration.between(start, end),
            stages = listOf(SleepStageSegment(SleepStage.LIGHT, start, end)),
        )

        val state = mapSleepDetailState(detail)

        assertTrue(state.hasData)
        assertEquals("8h 03m", state.durationLabel)
        assertEquals("22:00", state.bedTimeLabel)
        assertEquals("06:03", state.wakeTimeLabel)
    }

    @Test
    fun `positions segments as fractions of the night and sums stage totals in lane order`() {
        val start = at(23, 0)
        val split = at(0, 0, dayOffset = 1)
        val end = at(1, 0, dayOffset = 1)
        val detail = SleepDetail(
            start = start,
            end = end,
            total = Duration.between(start, end),
            stages = listOf(
                SleepStageSegment(SleepStage.DEEP, start, split),
                SleepStageSegment(SleepStage.REM, split, end),
            ),
        )

        val state = mapSleepDetailState(detail)

        assertEquals(0f, state.segments[0].startFraction, 0.001f)
        assertEquals(0.5f, state.segments[0].endFraction, 0.001f)
        assertEquals(0.5f, state.segments[1].startFraction, 0.001f)
        assertEquals(1f, state.segments[1].endFraction, 0.001f)
        assertEquals(
            listOf(SleepStage.DEEP to "1h 00m", SleepStage.REM to "1h 00m"),
            state.stageTotals.map { it.stage to it.durationLabel },
        )
    }

    @Test
    fun `a stage with no time in the session is left out of the legend`() {
        val start = at(23, 0)
        val end = at(0, 0, dayOffset = 1)
        val detail = SleepDetail(
            start = start,
            end = end,
            total = Duration.between(start, end),
            stages = listOf(SleepStageSegment(SleepStage.LIGHT, start, end)),
        )

        val state = mapSleepDetailState(detail)

        assertEquals(listOf(SleepStage.LIGHT), state.stageTotals.map { it.stage })
    }

    @Test
    fun `null detail produces the no-data default state`() {
        val state = mapSleepDetailState(null)

        assertFalse(state.hasData)
        assertTrue(state.segments.isEmpty())
    }
}
