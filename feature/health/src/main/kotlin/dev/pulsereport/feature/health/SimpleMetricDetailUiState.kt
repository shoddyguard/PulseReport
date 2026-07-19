package dev.pulsereport.feature.health

/** State for every metric detail screen except Sleep, which has its own richer [SleepDetailUiState]. */
sealed interface SimpleMetricDetailUiState {

    data class Steps(
        val todayCount: Int,
        val goalCount: Int,
        val distanceKm: Double,
        /** Step totals for the trailing week ending on the screen's date, oldest first. */
        val weekCounts: List<Int>,
    ) : SimpleMetricDetailUiState {
        val goalFraction: Float get() = (todayCount.toFloat() / goalCount).coerceIn(0f, 1f)
    }

    data class Weight(
        val currentKg: Double,
        val weekDeltaKg: Double,
        /** Weight readings for the trailing week ending on the screen's date, oldest first. */
        val series: List<Float>,
    ) : SimpleMetricDetailUiState

    /** [hasData] is false when Health Connect has no heart rate readings for the day; [restingBpm]/[series] are meaningless in that case. */
    data class Heart(
        val hasData: Boolean,
        val restingBpm: Int,
        val series: List<Float>,
    ) : SimpleMetricDetailUiState

    data class Calories(
        val burnedKcal: Int,
        val goalKcal: Int,
    ) : SimpleMetricDetailUiState {
        val goalFraction: Float get() = (burnedKcal.toFloat() / goalKcal).coerceIn(0f, 1f)
    }

    data class Hydration(
        val consumedLitres: Double,
        val goalLitres: Double,
        val totalSlots: Int,
    ) : SimpleMetricDetailUiState {
        val filledSlots: Int get() = ((consumedLitres / goalLitres) * totalSlots).toInt().coerceIn(0, totalSlots)
    }
}
