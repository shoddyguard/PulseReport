package dev.pulsereport.feature.dashboard

import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.GOAL_ACTIVE_KCAL
import dev.pulsereport.core.model.GOAL_HYDRATION_LITRES
import dev.pulsereport.core.model.GOAL_STEPS
import dev.pulsereport.core.model.HYDRATION_SLOTS
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.STRIDE_METERS
import dev.pulsereport.core.model.SleepSummary
import java.time.Duration
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Everything [mapDashboardState] needs for one day, already fetched from
 * [dev.pulsereport.core.healthconnect.HealthRepository]. [weekSteps] and [weekWeight] cover
 * the trailing week ending on [selectedDate], oldest first.
 */
data class DashboardRawData(
    val selectedDate: LocalDate,
    val weekSteps: List<DailySteps>,
    val sleep: SleepSummary?,
    val heart: HeartSummary?,
    val activeCaloriesKcal: Double,
    val hydrationLitres: Double,
    val weekWeight: List<DailyValue>,
)

/** Maps raw Health Connect reads into the shape the dashboard tiles render. */
fun mapDashboardState(raw: DashboardRawData): DashboardUiState {
    val todaySteps = raw.weekSteps.lastOrNull()?.count?.toInt() ?: 0
    val currentKg = raw.weekWeight.lastOrNull()?.value ?: 0.0
    val weekAgoKg = raw.weekWeight.firstOrNull()?.value ?: currentKg
    val hasWeightData = raw.weekWeight.any { it.value != 0.0 }

    return DashboardUiState(
        selectedDate = raw.selectedDate,
        syncedSourceCount = countSyncedSources(raw, todaySteps, hasWeightData),
        steps = StepsData(
            todaySteps = todaySteps,
            goalSteps = GOAL_STEPS,
            distanceKm = (todaySteps * STRIDE_METERS / 1000).roundTo(1),
            weekSteps = raw.weekSteps.map { it.count.toInt() },
        ),
        sleep = raw.sleep.toSleepData(),
        heart = HeartData(
            restingBpm = raw.heart?.restingBpm ?: 0,
            series = raw.heart?.series.orEmpty(),
        ),
        activeCalories = ActiveCaloriesData(
            burnedKcal = raw.activeCaloriesKcal.roundToInt(),
            goalKcal = GOAL_ACTIVE_KCAL,
        ),
        water = WaterData(
            consumedLitres = raw.hydrationLitres.roundTo(1),
            goalLitres = GOAL_HYDRATION_LITRES,
            totalSlots = HYDRATION_SLOTS,
        ),
        weight = WeightData(
            currentKg = currentKg,
            weekDeltaKg = (currentKg - weekAgoKg).roundTo(1),
            series = raw.weekWeight.map { it.value.toFloat() },
        ),
    )
}

private fun countSyncedSources(raw: DashboardRawData, todaySteps: Int, hasWeightData: Boolean): Int {
    var count = 0
    if (todaySteps > 0) count++
    if (raw.sleep != null) count++
    if (raw.heart != null) count++
    if (raw.activeCaloriesKcal > 0) count++
    if (raw.hydrationLitres > 0) count++
    if (hasWeightData) count++
    return count
}

private fun SleepSummary?.toSleepData(): SleepData {
    if (this == null) {
        return SleepData(duration = Duration.ZERO, score = 0, scoreLabel = "No data", stageWeights = listOf(1f, 1f, 1f))
    }

    val durationMinutes = total.toMinutes()
    val durationScore = when {
        durationMinutes >= 420 && durationMinutes <= 540 -> 60 // 7h-9h
        durationMinutes >= 360 -> 45 // 6h-7h
        durationMinutes >= 300 -> 30 // 5h-6h
        else -> 15
    }
    val restfulRatio = if (durationMinutes > 0) {
        (deep.toMinutes() + rem.toMinutes()).toFloat() / durationMinutes
    } else {
        0f
    }
    val score = (durationScore + (restfulRatio * 40).roundToInt()).coerceIn(0, 100)
    val label = when {
        score >= 85 -> "Excellent"
        score >= 70 -> "Good"
        score >= 50 -> "Fair"
        else -> "Poor"
    }

    return SleepData(
        duration = total,
        score = score,
        scoreLabel = label,
        stageWeights = listOf(
            deep.toMinutes().toFloat().coerceAtLeast(0.01f),
            light.toMinutes().toFloat().coerceAtLeast(0.01f),
            rem.toMinutes().toFloat().coerceAtLeast(0.01f),
        ),
    )
}

private fun Double.roundTo(decimals: Int): Double {
    val factor = Math.pow(10.0, decimals.toDouble())
    return Math.round(this * factor) / factor
}
