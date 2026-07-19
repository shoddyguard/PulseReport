package dev.pulsereport.core.model

import java.time.Instant

/** A logged feeling or mood, to correlate with health data. */
data class MoodEntry(
    val id: Long,
    val recordedAt: Instant,
    /** 1 (very low) to 5 (very high). */
    val rating: Int,
    val note: String?,
)
