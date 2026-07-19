package dev.pulsereport.core.model

import java.time.LocalDate

/** A single scalar reading for a calendar day, e.g. active calories, hydration, or weight. */
data class DailyValue(
    val date: LocalDate,
    val value: Double,
)
