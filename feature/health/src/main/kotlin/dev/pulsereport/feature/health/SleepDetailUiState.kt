package dev.pulsereport.feature.health

import dev.pulsereport.core.model.SleepStage

/** Sleep detail screen state. [hasData] is false until a session has loaded (or none exists for the night). */
data class SleepDetailUiState(
    val hasData: Boolean = false,
    val durationLabel: String = "–",
    val bedTimeLabel: String = "–",
    val wakeTimeLabel: String = "–",
    val segments: List<SleepChartSegment> = emptyList(),
    val stageTotals: List<SleepStageTotal> = emptyList(),
)

/** One [stage] block in the hypnogram, positioned as a 0f..1f fraction of the night. */
data class SleepChartSegment(
    val stage: SleepStage,
    val startFraction: Float,
    val endFraction: Float,
)

/** A stage's total time asleep, formatted for the legend (e.g. "1h 12m"). */
data class SleepStageTotal(
    val stage: SleepStage,
    val durationLabel: String,
)
