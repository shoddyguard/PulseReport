package dev.pulsereport.feature.dashboard

import java.time.Duration
import java.time.LocalDate

/** Dashboard tile data for [selectedDate], read from Health Connect. */
data class DashboardUiState(
    val selectedDate: LocalDate,
    val syncedSourceCount: Int,
    val isRefreshing: Boolean = false,
    val steps: StepsData,
    val sleep: SleepData,
    val heart: HeartData,
    val activeCalories: ActiveCaloriesData,
    val water: WaterData,
    val weight: WeightData,
)

data class StepsData(
    val todaySteps: Int,
    val goalSteps: Int,
    val distanceKm: Double,
    /** Step totals for the last seven days, oldest first. */
    val weekSteps: List<Int>,
) {
    val goalFraction: Float
        get() = (todaySteps.toFloat() / goalSteps).coerceIn(0f, 1f)
}

data class SleepData(
    val duration: Duration,
    val score: Int,
    val scoreLabel: String,
    /** Relative weights of the deep, core, and rem stages. */
    val stageWeights: List<Float>,
)

data class HeartData(
    val restingBpm: Int,
    val series: List<Float>,
)

data class ActiveCaloriesData(
    val burnedKcal: Int,
    val goalKcal: Int,
) {
    val goalFraction: Float
        get() = (burnedKcal.toFloat() / goalKcal).coerceIn(0f, 1f)
}

data class WaterData(
    val consumedLitres: Double,
    val goalLitres: Double,
    val totalSlots: Int,
) {
    val filledSlots: Int
        get() = ((consumedLitres / goalLitres) * totalSlots).toInt().coerceIn(0, totalSlots)
}

data class WeightData(
    val currentKg: Double,
    val weekDeltaKg: Double,
    val series: List<Float>,
)
