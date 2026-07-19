package dev.pulsereport.core.database

import dev.pulsereport.core.model.HealthMetric
import kotlinx.coroutines.flow.Flow

/** User-configured per-metric ordering of which app/device wins when several report data. */
interface SourcePriorityRepository {

    /** Saved priority order per metric, package names highest-priority first. Metrics with no saved order are absent. */
    fun observePriorities(): Flow<Map<HealthMetric, List<String>>>

    /** Replaces the saved priority order for [metric] with [orderedPackageNames]. */
    suspend fun setPriority(metric: HealthMetric, orderedPackageNames: List<String>)
}
