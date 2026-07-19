package dev.pulsereport.core.healthconnect

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceSelectionMapperTest {

    private val day: LocalDate = LocalDate.of(2026, 7, 17)
    private val otherDay: LocalDate = LocalDate.of(2026, 7, 18)

    @Test
    fun `empty priority picks the alphabetically-first origin with data, never combines`() {
        val items = mapOf(
            day to mapOf(
                "com.fitbit" to listOf("fitbit-value"),
                "com.samsung" to listOf("samsung-value"),
            ),
        )

        val result = selectByPriority(items, priority = emptyList())

        assertEquals(listOf("fitbit-value"), result.getValue(day))
    }

    @Test
    fun `empty priority falls through to the next origin alphabetically when the first has no data that day`() {
        val items = mapOf(
            day to mapOf(
                "com.samsung" to listOf("samsung-value"),
            ),
        )

        val result = selectByPriority(items, priority = emptyList())

        assertEquals(listOf("samsung-value"), result.getValue(day))
    }

    @Test
    fun `first prioritized origin with data wins`() {
        val items = mapOf(
            day to mapOf(
                "com.fitbit" to listOf("fitbit-value"),
                "com.samsung" to listOf("samsung-value"),
            ),
        )

        val result = selectByPriority(items, priority = listOf("com.samsung", "com.fitbit"))

        assertEquals(listOf("samsung-value"), result.getValue(day))
    }

    @Test
    fun `falls through to the next prioritized origin when the top choice has no data that day`() {
        val items = mapOf(
            day to mapOf(
                "com.fitbit" to listOf("fitbit-value"),
            ),
        )

        val result = selectByPriority(items, priority = listOf("com.samsung", "com.fitbit"))

        assertEquals(listOf("fitbit-value"), result.getValue(day))
    }

    @Test
    fun `unlisted origins rank after listed ones, alphabetically`() {
        val items = mapOf(
            day to mapOf(
                "com.zzz" to listOf("zzz-value"),
                "com.aaa" to listOf("aaa-value"),
            ),
        )

        val result = selectByPriority(items, priority = listOf("com.unrelated"))

        assertEquals(listOf("aaa-value"), result.getValue(day))
    }

    @Test
    fun `selection is independent per day`() {
        val items = mapOf(
            day to mapOf("com.fitbit" to listOf("fitbit-day1")),
            otherDay to mapOf("com.samsung" to listOf("samsung-day2")),
        )

        val result = selectByPriority(items, priority = listOf("com.samsung", "com.fitbit"))

        assertEquals(listOf("fitbit-day1"), result.getValue(day))
        assertEquals(listOf("samsung-day2"), result.getValue(otherDay))
    }

    @Test
    fun `a day where every origin's list is empty yields an empty list`() {
        val items = mapOf(day to mapOf("com.other" to emptyList<String>()))

        val result = selectByPriority(items, priority = listOf("com.other"))

        assertEquals(emptyList<String>(), result.getValue(day))
    }
}
