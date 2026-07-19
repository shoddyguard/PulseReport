package dev.pulsereport.feature.health

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pulsereport.core.database.SourcePriorityRepository
import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.TRAILING_WEEK_DAYS
import dev.pulsereport.feature.health.navigation.HEALTH_DETAIL_EPOCH_DAY_KEY
import dev.pulsereport.feature.health.navigation.HEALTH_DETAIL_METRIC_KEY
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Backs every metric detail screen except Sleep (see [SleepDetailViewModel]), one repo call per [metric]. */
@HiltViewModel
class SimpleMetricDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val healthRepository: HealthRepository,
    private val sourcePriorityRepository: SourcePriorityRepository,
) : ViewModel() {

    private val metric: HealthMetric =
        HealthMetric.valueOf(checkNotNull(savedStateHandle.get<String>(HEALTH_DETAIL_METRIC_KEY)))
    private val date: LocalDate =
        LocalDate.ofEpochDay(checkNotNull(savedStateHandle.get<Long>(HEALTH_DETAIL_EPOCH_DAY_KEY)))

    private val _uiState = MutableStateFlow(emptyState(metric))
    val uiState: StateFlow<SimpleMetricDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sourcePriorityRepository.observePriorities().collectLatest { priorities ->
                _uiState.value = loadState(metric, priorities[metric].orEmpty())
            }
        }
    }

    private suspend fun loadState(metric: HealthMetric, priority: List<String>): SimpleMetricDetailUiState =
        when (metric) {
            HealthMetric.STEPS ->
                mapStepsDetailState(healthRepository.getDailySteps(date, TRAILING_WEEK_DAYS, priority))
            HealthMetric.WEIGHT ->
                mapWeightDetailState(healthRepository.getDailyWeight(date, TRAILING_WEEK_DAYS, priority))
            HealthMetric.HEART_RATE -> mapHeartDetailState(healthRepository.getHeart(date, priority))
            HealthMetric.CALORIES -> mapCaloriesDetailState(healthRepository.getActiveCalories(date, priority))
            HealthMetric.HYDRATION -> mapHydrationDetailState(healthRepository.getHydration(date, priority))
            HealthMetric.SLEEP -> unsupportedSleepMetric()
        }
}

/** Zeroed state shown for the instant before the first Health Connect read completes, matching [DashboardViewModel]'s equivalent. */
private fun emptyState(metric: HealthMetric): SimpleMetricDetailUiState = when (metric) {
    HealthMetric.STEPS -> mapStepsDetailState(emptyList())
    HealthMetric.WEIGHT -> mapWeightDetailState(emptyList())
    HealthMetric.HEART_RATE -> mapHeartDetailState(null)
    HealthMetric.CALORIES -> mapCaloriesDetailState(0.0)
    HealthMetric.HYDRATION -> mapHydrationDetailState(0.0)
    HealthMetric.SLEEP -> unsupportedSleepMetric()
}

private fun unsupportedSleepMetric(): Nothing =
    error("Sleep is handled by SleepDetailViewModel, not SimpleMetricDetailViewModel")
