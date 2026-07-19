package dev.pulsereport.core.healthconnect

import androidx.health.connect.client.records.SleepSessionRecord
import dev.pulsereport.core.model.SleepStage
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepStageMapperTest {

    @Test
    fun `sums each band separately`() {
        val stages = listOf(
            SleepSessionRecord.STAGE_TYPE_LIGHT to Duration.ofMinutes(120),
            SleepSessionRecord.STAGE_TYPE_DEEP to Duration.ofMinutes(90),
            SleepSessionRecord.STAGE_TYPE_LIGHT to Duration.ofMinutes(60),
            SleepSessionRecord.STAGE_TYPE_REM to Duration.ofMinutes(70),
        )

        val (deep, light, rem) = sleepStageBreakdown(stages)

        assertEquals(Duration.ofMinutes(90), deep)
        assertEquals(Duration.ofMinutes(180), light)
        assertEquals(Duration.ofMinutes(70), rem)
    }

    @Test
    fun `folds awake and unknown stages into light`() {
        val stages = listOf(
            SleepSessionRecord.STAGE_TYPE_AWAKE to Duration.ofMinutes(5),
            SleepSessionRecord.STAGE_TYPE_UNKNOWN to Duration.ofMinutes(3),
            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED to Duration.ofMinutes(2),
        )

        val (deep, light, rem) = sleepStageBreakdown(stages)

        assertEquals(Duration.ZERO, deep)
        assertEquals(Duration.ofMinutes(10), light)
        assertEquals(Duration.ZERO, rem)
    }

    @Test
    fun `an empty stage list sums to zero everywhere`() {
        val (deep, light, rem) = sleepStageBreakdown(emptyList())

        assertEquals(Duration.ZERO, deep)
        assertEquals(Duration.ZERO, light)
        assertEquals(Duration.ZERO, rem)
    }

    @Test
    fun `sleepStageOf keeps awake distinct from light, unlike the breakdown`() {
        assertEquals(SleepStage.DEEP, sleepStageOf(SleepSessionRecord.STAGE_TYPE_DEEP))
        assertEquals(SleepStage.REM, sleepStageOf(SleepSessionRecord.STAGE_TYPE_REM))
        assertEquals(SleepStage.LIGHT, sleepStageOf(SleepSessionRecord.STAGE_TYPE_LIGHT))
        assertEquals(SleepStage.AWAKE, sleepStageOf(SleepSessionRecord.STAGE_TYPE_AWAKE))
        assertEquals(SleepStage.AWAKE, sleepStageOf(SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED))
        assertEquals(SleepStage.AWAKE, sleepStageOf(SleepSessionRecord.STAGE_TYPE_OUT_OF_BED))
    }

    @Test
    fun `sleepStageOf folds unknown stages into light`() {
        assertEquals(SleepStage.LIGHT, sleepStageOf(SleepSessionRecord.STAGE_TYPE_UNKNOWN))
    }
}
