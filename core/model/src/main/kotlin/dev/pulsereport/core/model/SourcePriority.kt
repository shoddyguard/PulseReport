package dev.pulsereport.core.model

/**
 * Ordered preference of data origins for one metric. When multiple apps or
 * devices write the same metric, the first origin in [packageNames] that has
 * data wins.
 */
data class SourcePriority(
    val metric: HealthMetric,
    val packageNames: List<String>,
)
