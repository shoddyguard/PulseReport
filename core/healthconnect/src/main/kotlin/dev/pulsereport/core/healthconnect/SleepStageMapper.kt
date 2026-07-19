package dev.pulsereport.core.healthconnect

import androidx.health.connect.client.records.SleepSessionRecord
import dev.pulsereport.core.model.SleepStage
import java.time.Duration

/** Awake, out-of-bed, and unknown stages fold into light rather than getting dropped, so the bar always accounts for the full session. */
fun sleepStageBreakdown(stages: List<Pair<Int, Duration>>): Triple<Duration, Duration, Duration> {
    var deep = Duration.ZERO
    var light = Duration.ZERO
    var rem = Duration.ZERO
    stages.forEach { (stageType, duration) ->
        when (stageType) {
            SleepSessionRecord.STAGE_TYPE_DEEP -> deep += duration
            SleepSessionRecord.STAGE_TYPE_REM -> rem += duration
            else -> light += duration
        }
    }
    return Triple(deep, light, rem)
}

/** Maps a raw Health Connect stage type to our [SleepStage], keeping awake distinct for the detail screen's hypnogram. */
fun sleepStageOf(stageType: Int): SleepStage = when (stageType) {
    SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStage.DEEP
    SleepSessionRecord.STAGE_TYPE_REM -> SleepStage.REM
    SleepSessionRecord.STAGE_TYPE_AWAKE,
    SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED,
    SleepSessionRecord.STAGE_TYPE_OUT_OF_BED,
    -> SleepStage.AWAKE
    else -> SleepStage.LIGHT
}
