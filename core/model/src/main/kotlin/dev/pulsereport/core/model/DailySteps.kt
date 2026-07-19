package dev.pulsereport.core.model

import java.time.LocalDate

/** Total step count for a single calendar day. */
data class DailySteps(
    val date: LocalDate,
    val count: Long,
)
