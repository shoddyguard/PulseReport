package dev.pulsereport.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pulsereport.core.database.SourcePriorityRepository
import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.TRAILING_WEEK_DAYS
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val sourcePriorityRepository: SourcePriorityRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow(emptyDashboardState(selectedDate.value))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(selectedDate, sourcePriorityRepository.observePriorities()) { date, priorities -> date to priorities }
                .collectLatest { (date, priorities) ->
                    _uiState.value = loadDashboardState(date, priorities)
                }
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    private suspend fun loadDashboardState(
        date: LocalDate,
        priorities: Map<HealthMetric, List<String>>,
    ): DashboardUiState = coroutineScope {
        val weekSteps = async {
            healthRepository.getDailySteps(date, TRAILING_WEEK_DAYS, priorities[HealthMetric.STEPS].orEmpty())
        }
        val sleep = async { healthRepository.getSleep(date, priorities[HealthMetric.SLEEP].orEmpty()) }
        val heart = async { healthRepository.getHeart(date, priorities[HealthMetric.HEART_RATE].orEmpty()) }
        val activeCalories = async {
            healthRepository.getActiveCalories(date, priorities[HealthMetric.CALORIES].orEmpty())
        }
        val hydration = async { healthRepository.getHydration(date, priorities[HealthMetric.HYDRATION].orEmpty()) }
        val weekWeight = async {
            healthRepository.getDailyWeight(date, TRAILING_WEEK_DAYS, priorities[HealthMetric.WEIGHT].orEmpty())
        }

        mapDashboardState(
            DashboardRawData(
                selectedDate = date,
                weekSteps = weekSteps.await(),
                sleep = sleep.await(),
                heart = heart.await(),
                activeCaloriesKcal = activeCalories.await(),
                hydrationLitres = hydration.await(),
                weekWeight = weekWeight.await(),
            ),
        )
    }
}

/** Zeroed state shown for the instant before the first Health Connect read completes. */
private fun emptyDashboardState(date: LocalDate): DashboardUiState = mapDashboardState(
    DashboardRawData(
        selectedDate = date,
        weekSteps = emptyList(),
        sleep = null,
        heart = null,
        activeCaloriesKcal = 0.0,
        hydrationLitres = 0.0,
        weekWeight = emptyList(),
    ),
)
