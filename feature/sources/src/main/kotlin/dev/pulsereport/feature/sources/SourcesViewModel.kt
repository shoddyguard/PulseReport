package dev.pulsereport.feature.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pulsereport.core.database.SourcePriorityRepository
import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.HealthMetric
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
class SourcesViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val sourcePriorityRepository: SourcePriorityRepository,
    private val labelResolver: AppLabelResolver,
) : ViewModel() {

    private val discoveredPackages = MutableStateFlow<Map<HealthMetric, Set<String>>>(emptyMap())

    private val _uiState = MutableStateFlow(SourcesUiState())
    val uiState: StateFlow<SourcesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            discoveredPackages.value = discoverAllMetricPackages()
        }
        viewModelScope.launch {
            combine(discoveredPackages, sourcePriorityRepository.observePriorities(), ::buildUiState)
                .collectLatest { state -> _uiState.value = state }
        }
    }

    /** [orderedPackageNames] is the full new priority order for [metric], as computed by the drag gesture. */
    fun onReorder(metric: HealthMetric, orderedPackageNames: List<String>) {
        viewModelScope.launch {
            sourcePriorityRepository.setPriority(metric, orderedPackageNames)
        }
    }

    private fun buildUiState(
        discovered: Map<HealthMetric, Set<String>>,
        saved: Map<HealthMetric, List<String>>,
    ): SourcesUiState = SourcesUiState(
        metrics = HealthMetric.entries.map { metric ->
            val order = effectiveOrder(discovered[metric].orEmpty(), saved[metric].orEmpty())
            MetricSourcesUiState(
                metric = metric,
                sources = order.map { packageName ->
                    SourceUiState(packageName = packageName, label = labelResolver.labelFor(packageName))
                },
            )
        },
    )

    private suspend fun discoverAllMetricPackages(): Map<HealthMetric, Set<String>> = coroutineScope {
        HealthMetric.entries
            .map { metric -> metric to async { healthRepository.getContributingPackages(metric) } }
            .associate { (metric, deferred) -> metric to deferred.await() }
    }
}

/** The saved priority order, highest first, followed by any other discovered packages (alphabetical). */
private fun effectiveOrder(discovered: Set<String>, saved: List<String>): List<String> {
    val savedPresent = saved.filter { packageName -> packageName in discovered }
    val unlisted = (discovered - savedPresent.toSet()).sorted()
    return savedPresent + unlisted
}
