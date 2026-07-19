package dev.pulsereport.core.healthconnect

import dev.pulsereport.core.model.DailyValue
import java.time.LocalDate

fun carriedForwardDailyValues(
    averagesByDate: Map<LocalDate, Double>,
    endDate: LocalDate,
    days: Int,
): List<DailyValue> {
    require(days > 0) { "days must be positive, was $days" }

    var lastKnown: Double? = null
    val forwardFilled = (days - 1 downTo 0).map { offset ->
        val date = endDate.minusDays(offset.toLong())
        averagesByDate[date]?.let { lastKnown = it }
        date to lastKnown
    }

    val firstKnown = forwardFilled.firstNotNullOfOrNull { it.second }
    return forwardFilled.map { (date, value) -> DailyValue(date = date, value = value ?: firstKnown ?: 0.0) }
}
