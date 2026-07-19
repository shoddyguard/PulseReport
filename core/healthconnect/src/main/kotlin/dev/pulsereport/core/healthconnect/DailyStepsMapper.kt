package dev.pulsereport.core.healthconnect

import dev.pulsereport.core.model.DailySteps
import java.time.LocalDate

fun zeroFilledDailySteps(
    totalsByDate: Map<LocalDate, Long>,
    endDate: LocalDate,
    days: Int,
): List<DailySteps> {
    require(days > 0) { "days must be positive, was $days" }
    return (days - 1 downTo 0).map { offset ->
        val date = endDate.minusDays(offset.toLong())
        DailySteps(date = date, count = totalsByDate[date] ?: 0L)
    }
}
