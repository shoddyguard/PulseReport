package dev.pulsereport.core.healthconnect

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyStepsMapperTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 17)

    @Test
    fun `fills full window oldest first ending today`() {
        val result = zeroFilledDailySteps(emptyMap(), endDate = today, days = 7)

        assertEquals(7, result.size)
        assertEquals(today.minusDays(6), result.first().date)
        assertEquals(today, result.last().date)
        assertEquals(List(7) { 0L }, result.map { it.count })
    }

    @Test
    fun `keeps counts for days that have data`() {
        val totals = mapOf(
            today to 5000L,
            today.minusDays(2) to 1234L,
        )

        val result = zeroFilledDailySteps(totals, endDate = today, days = 3)

        assertEquals(listOf(1234L, 0L, 5000L), result.map { it.count })
    }

    @Test
    fun `single day window contains only today`() {
        val result = zeroFilledDailySteps(mapOf(today to 42L), endDate = today, days = 1)

        assertEquals(listOf(today), result.map { it.date })
        assertEquals(listOf(42L), result.map { it.count })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive day count`() {
        zeroFilledDailySteps(emptyMap(), endDate = today, days = 0)
    }
}
