package dev.pulsereport.feature.health

import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.GOAL_ACTIVE_KCAL
import dev.pulsereport.core.model.GOAL_HYDRATION_LITRES
import dev.pulsereport.core.model.GOAL_STEPS
import dev.pulsereport.core.model.HYDRATION_SLOTS
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.STRIDE_METERS
import kotlin.math.roundToInt

fun mapStepsDetailState(weekSteps: List<DailySteps>): SimpleMetricDetailUiState.Steps {
    val todayCount = weekSteps.lastOrNull()?.count?.toInt() ?: 0
    return SimpleMetricDetailUiState.Steps(
        todayCount = todayCount,
        goalCount = GOAL_STEPS,
        distanceKm = (todayCount * STRIDE_METERS / 1000).roundTo(1),
        weekCounts = weekSteps.map { it.count.toInt() },
    )
}

fun mapWeightDetailState(weekWeight: List<DailyValue>): SimpleMetricDetailUiState.Weight {
    val currentKg = weekWeight.lastOrNull()?.value ?: 0.0
    val weekAgoKg = weekWeight.firstOrNull()?.value ?: currentKg
    return SimpleMetricDetailUiState.Weight(
        currentKg = currentKg,
        weekDeltaKg = (currentKg - weekAgoKg).roundTo(1),
        series = weekWeight.map { it.value.toFloat() },
    )
}

fun mapHeartDetailState(heart: HeartSummary?): SimpleMetricDetailUiState.Heart =
    SimpleMetricDetailUiState.Heart(
        hasData = heart != null,
        restingBpm = heart?.restingBpm ?: 0,
        series = heart?.series.orEmpty(),
    )

fun mapCaloriesDetailState(burnedKcal: Double): SimpleMetricDetailUiState.Calories =
    SimpleMetricDetailUiState.Calories(
        burnedKcal = burnedKcal.roundToInt(),
        goalKcal = GOAL_ACTIVE_KCAL,
    )

fun mapHydrationDetailState(consumedLitres: Double): SimpleMetricDetailUiState.Hydration =
    SimpleMetricDetailUiState.Hydration(
        consumedLitres = consumedLitres.roundTo(1),
        goalLitres = GOAL_HYDRATION_LITRES,
        totalSlots = HYDRATION_SLOTS,
    )

private fun Double.roundTo(decimals: Int): Double {
    val factor = Math.pow(10.0, decimals.toDouble())
    return Math.round(this * factor) / factor
}
