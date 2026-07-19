package dev.pulsereport.core.database

import dev.pulsereport.core.model.HealthMetric
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomSourcePriorityRepository @Inject constructor(
    private val dao: SourcePriorityDao,
) : SourcePriorityRepository {

    override fun observePriorities(): Flow<Map<HealthMetric, List<String>>> =
        dao.observeAll().map { entities ->
            entities
                .groupBy { entity -> entity.metric }
                .mapNotNull { (metricName, rows) ->
                    val metric = runCatching { HealthMetric.valueOf(metricName) }.getOrNull() ?: return@mapNotNull null
                    metric to rows.sortedBy { row -> row.position }.map { row -> row.packageName }
                }
                .toMap()
        }

    override suspend fun setPriority(metric: HealthMetric, orderedPackageNames: List<String>) {
        dao.replaceForMetric(metric.name, orderedPackageNames)
    }
}
