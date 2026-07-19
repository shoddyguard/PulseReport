package dev.pulsereport.core.ui.metric

import androidx.compose.ui.graphics.Color
import dev.pulsereport.core.model.SleepStage
import dev.pulsereport.core.ui.theme.PulseColors

/** Chart color for a [SleepStage], shared by the sleep tile's [dev.pulsereport.core.ui.component.SegmentedBar] and the sleep detail hypnogram. */
fun SleepStage.color(colors: PulseColors): Color = when (this) {
    SleepStage.DEEP -> colors.sleepStageDeep
    SleepStage.LIGHT -> colors.sleepStageCore
    SleepStage.REM -> colors.sleepStageRem
    SleepStage.AWAKE -> colors.sleepStageAwake
}

fun SleepStage.displayLabel(): String = when (this) {
    SleepStage.DEEP -> "Deep"
    SleepStage.LIGHT -> "Light"
    SleepStage.REM -> "REM"
    SleepStage.AWAKE -> "Awake"
}
