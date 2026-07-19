package dev.pulsereport.core.healthconnect

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyValueMapperTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 17)

    @Test
    fun `carries the last known value forward through gaps`() {
        val averages = mapOf(today.minusDays(4) to 80.0, today.minusDays(1) to 79.0)

        val result = carriedForwardDailyValues(averages, endDate = today, days = 5)

        assertEquals(listOf(80.0, 80.0, 80.0, 79.0, 79.0), result.map { it.value })
    }

    @Test
    fun `backward-fills a leading gap from the first known value`() {
        val averages = mapOf(today to 78.0)

        val result = carriedForwardDailyValues(averages, endDate = today, days = 3)

        assertEquals(listOf(78.0, 78.0, 78.0), result.map { it.value })
    }

    @Test
    fun `falls back to zero when there is no data at all`() {
        val result = carriedForwardDailyValues(emptyMap(), endDate = today, days = 3)

        assertEquals(listOf(0.0, 0.0, 0.0), result.map { it.value })
    }

    @Test
    fun `dates run oldest first ending at the anchor`() {
        val result = carriedForwardDailyValues(emptyMap(), endDate = today, days = 3)

        assertEquals(listOf(today.minusDays(2), today.minusDays(1), today), result.map { it.date })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive day count`() {
        carriedForwardDailyValues(emptyMap(), endDate = today, days = 0)
    }
}
