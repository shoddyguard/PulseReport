package dev.pulsereport.core.healthconnect

import org.junit.Assert.assertEquals
import org.junit.Test

class HeartSeriesMapperTest {

    @Test
    fun `leaves a series at or under the target size untouched`() {
        val values = listOf(1f, 2f, 3f)

        assertEquals(values, downsampleSeries(values, targetSize = 8))
    }

    @Test
    fun `downsamples evenly across a longer series`() {
        val values = (0 until 100).map { it.toFloat() }

        val result = downsampleSeries(values, targetSize = 10)

        assertEquals(10, result.size)
        // Evenly spaced indices, oldest first, without clustering near the end.
        assertEquals(listOf(0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f), result)
    }

    @Test
    fun `handles an empty series`() {
        assertEquals(emptyList<Float>(), downsampleSeries(emptyList(), targetSize = 8))
    }
}
