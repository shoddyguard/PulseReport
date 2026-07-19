package dev.pulsereport.core.model

import java.time.LocalDate

/** A free-form diary entry for a given day. */
data class DiaryEntry(
    val id: Long,
    val date: LocalDate,
    val text: String,
)
