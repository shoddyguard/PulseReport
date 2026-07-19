package dev.pulsereport.feature.sources

import dev.pulsereport.core.model.HealthMetric
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        packagesByMetric: Map<HealthMetric, Set<String>> = emptyMap(),
        savedPriorities: Map<HealthMetric, List<String>> = emptyMap(),
        labels: Map<String, String> = emptyMap(),
    ): Triple<SourcesViewModel, FakeHealthRepository, FakeSourcePriorityRepository> {
        val healthRepository = FakeHealthRepository(packagesByMetric)
        val priorityRepository = FakeSourcePriorityRepository(savedPriorities)
        val viewModel = SourcesViewModel(healthRepository, priorityRepository, FakeAppLabelResolver(labels))
        return Triple(viewModel, healthRepository, priorityRepository)
    }

    @Test
    fun `discovered packages populate every metric's source list`() = runTest {
        val (viewModel, _, _) = viewModel(
            packagesByMetric = mapOf(HealthMetric.STEPS to setOf("com.fitbit", "com.samsung")),
            labels = mapOf("com.fitbit" to "Fitbit", "com.samsung" to "Samsung Health"),
        )

        val stepsState = viewModel.uiState.value.metrics.first { it.metric == HealthMetric.STEPS }

        assertEquals(listOf("Fitbit", "Samsung Health"), stepsState.sources.map { it.label })
    }

    @Test
    fun `a metric with no discovered packages has an empty source list`() = runTest {
        val (viewModel, _, _) = viewModel()

        val stepsState = viewModel.uiState.value.metrics.first { it.metric == HealthMetric.STEPS }

        assertEquals(emptyList<SourceUiState>(), stepsState.sources)
    }

    @Test
    fun `saved priority orders the sources ahead of unlisted discovered packages`() = runTest {
        val (viewModel, _, _) = viewModel(
            packagesByMetric = mapOf(HealthMetric.STEPS to setOf("com.zzz", "com.fitbit", "com.samsung")),
            savedPriorities = mapOf(HealthMetric.STEPS to listOf("com.samsung", "com.fitbit")),
        )

        val stepsState = viewModel.uiState.value.metrics.first { it.metric == HealthMetric.STEPS }

        assertEquals(listOf("com.samsung", "com.fitbit", "com.zzz"), stepsState.sources.map { it.packageName })
    }

    @Test
    fun `onReorder persists exactly the order it's given`() = runTest {
        val (viewModel, _, priorityRepository) = viewModel(
            packagesByMetric = mapOf(HealthMetric.STEPS to setOf("com.fitbit", "com.samsung")),
        )

        viewModel.onReorder(HealthMetric.STEPS, listOf("com.samsung", "com.fitbit"))

        assertEquals(
            HealthMetric.STEPS to listOf("com.samsung", "com.fitbit"),
            priorityRepository.setPriorityCalls.last(),
        )
    }

    @Test
    fun `onReorder is reflected back into uiState via the priority repository`() = runTest {
        val (viewModel, _, _) = viewModel(
            packagesByMetric = mapOf(HealthMetric.STEPS to setOf("com.fitbit", "com.samsung")),
        )

        viewModel.onReorder(HealthMetric.STEPS, listOf("com.samsung", "com.fitbit"))

        val stepsState = viewModel.uiState.value.metrics.first { it.metric == HealthMetric.STEPS }
        assertEquals(listOf("com.samsung", "com.fitbit"), stepsState.sources.map { it.packageName })
    }

    @Test
    fun `reordering one metric leaves other metrics untouched`() = runTest {
        val (viewModel, _, _) = viewModel(
            packagesByMetric = mapOf(
                HealthMetric.STEPS to setOf("com.fitbit", "com.samsung"),
                HealthMetric.WEIGHT to setOf("com.withings"),
            ),
        )

        viewModel.onReorder(HealthMetric.STEPS, listOf("com.samsung", "com.fitbit"))

        val weightState = viewModel.uiState.value.metrics.first { it.metric == HealthMetric.WEIGHT }
        assertEquals(listOf("com.withings"), weightState.sources.map { it.packageName })
    }
}
