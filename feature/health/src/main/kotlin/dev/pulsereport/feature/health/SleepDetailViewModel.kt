package dev.pulsereport.feature.health

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pulsereport.core.database.SourcePriorityRepository
import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.feature.health.navigation.HEALTH_DETAIL_EPOCH_DAY_KEY
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SleepDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val healthRepository: HealthRepository,
    private val sourcePriorityRepository: SourcePriorityRepository,
) : ViewModel() {

    private val date: LocalDate =
        LocalDate.ofEpochDay(checkNotNull(savedStateHandle.get<Long>(HEALTH_DETAIL_EPOCH_DAY_KEY)))

    private val _uiState = MutableStateFlow(SleepDetailUiState())
    val uiState: StateFlow<SleepDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sourcePriorityRepository.observePriorities().collectLatest { priorities ->
                val detail = healthRepository.getSleepDetail(date, priorities[HealthMetric.SLEEP].orEmpty())
                _uiState.value = mapSleepDetailState(detail)
            }
        }
    }
}
