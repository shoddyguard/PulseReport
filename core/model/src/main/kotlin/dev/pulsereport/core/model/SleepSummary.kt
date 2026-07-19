package dev.pulsereport.core.model

import java.time.Duration

/** A night's sleep, broken down by stage. [deep], [light], and [rem] sum to [total]. */
data class SleepSummary(
    val total: Duration,
    val deep: Duration,
    val light: Duration,
    val rem: Duration,
)
