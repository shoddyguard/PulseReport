package dev.pulsereport.feature.sources

import dev.pulsereport.core.database.SourcePriorityRepository
import dev.pulsereport.core.model.HealthMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSourcePriorityRepository(
    initial: Map<HealthMetric, List<String>> = emptyMap(),
) : SourcePriorityRepository {

    private val priorities = MutableStateFlow(initial)

    val setPriorityCalls: MutableList<Pair<HealthMetric, List<String>>> = mutableListOf()

    override fun observePriorities(): Flow<Map<HealthMetric, List<String>>> = priorities

    override suspend fun setPriority(metric: HealthMetric, orderedPackageNames: List<String>) {
        setPriorityCalls += metric to orderedPackageNames
        priorities.value = priorities.value + (metric to orderedPackageNames)
    }
}
