package dev.pulsereport.core.model

import java.time.Duration
import java.time.Instant

/** A single night's sleep stages, distinct from [SleepSummary]: keeps [AWAKE] separate and preserves stage timing for a hypnogram-style chart. */
enum class SleepStage {
    AWAKE,
    REM,
    LIGHT,
    DEEP,
}

/** One contiguous block of a single [stage], from [start] to [end]. */
data class SleepStageSegment(
    val stage: SleepStage,
    val start: Instant,
    val end: Instant,
)

/** A night's sleep session with per-stage timing, for the sleep detail screen. */
data class SleepDetail(
    val start: Instant,
    val end: Instant,
    val total: Duration,
    val stages: List<SleepStageSegment>,
) {
    fun durationsByStage(): Map<SleepStage, Duration> =
        stages
            .groupingBy { segment -> segment.stage }
            .fold(Duration.ZERO) { acc, segment -> acc + Duration.between(segment.start, segment.end) }
}
