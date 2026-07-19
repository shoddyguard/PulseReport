package dev.pulsereport.feature.sources

import dev.pulsereport.core.model.HealthMetric

data class SourcesUiState(
    val metrics: List<MetricSourcesUiState> = emptyList(),
)

/** [sources] is the effective priority order for [metric], highest priority first. */
data class MetricSourcesUiState(
    val metric: HealthMetric,
    val sources: List<SourceUiState>,
)

data class SourceUiState(
    val packageName: String,
    val label: String,
)
