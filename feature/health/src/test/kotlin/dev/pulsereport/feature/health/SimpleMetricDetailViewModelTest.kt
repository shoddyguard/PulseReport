package dev.pulsereport.feature.health

import androidx.lifecycle.SavedStateHandle
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimpleMetricDetailViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun savedStateHandle(metric: HealthMetric, date: LocalDate) =
        SavedStateHandle(mapOf("metric" to metric.name, "epochDay" to date.toEpochDay()))

    @Test
    fun `loads steps for the date carried by the route`() = runTest {
        val date = LocalDate.of(2026, 7, 18)
        val healthRepository = FakeHealthRepository(stepsByDate = mapOf(date to 8_432L))

        val viewModel = SimpleMetricDetailViewModel(
            savedStateHandle(HealthMetric.STEPS, date),
            healthRepository,
            FakeSourcePriorityRepository(),
        )

        val state = viewModel.uiState.value
        check(state is SimpleMetricDetailUiState.Steps)
        assertEquals(8_432, state.todayCount)
    }

    @Test
    fun `loads heart for the HEART_RATE metric`() = runTest {
        val healthRepository = FakeHealthRepository(heart = HeartSummary(restingBpm = 58, series = listOf(1f, 2f)))

        val viewModel = SimpleMetricDetailViewModel(
            savedStateHandle(HealthMetric.HEART_RATE, LocalDate.now()),
            healthRepository,
            FakeSourcePriorityRepository(),
        )

        val state = viewModel.uiState.value
        check(state is SimpleMetricDetailUiState.Heart)
        assertTrue(state.hasData)
        assertEquals(58, state.restingBpm)
    }

    @Test
    fun `a saved source priority for the route's metric reaches the repository read`() = runTest {
        val healthRepository = FakeHealthRepository()
        val priorityRepository = FakeSourcePriorityRepository(
            initial = mapOf(HealthMetric.CALORIES to listOf("com.fitbit")),
        )

        SimpleMetricDetailViewModel(
            savedStateHandle(HealthMetric.CALORIES, LocalDate.now()),
            healthRepository,
            priorityRepository,
        )

        assertEquals(listOf("com.fitbit"), healthRepository.receivedPriorities[HealthMetric.CALORIES])
    }

    @Test
    fun `hydration and weight default to zero when unseeded`() = runTest {
        val hydrationViewModel = SimpleMetricDetailViewModel(
            savedStateHandle(HealthMetric.HYDRATION, LocalDate.now()),
            FakeHealthRepository(),
            FakeSourcePriorityRepository(),
        )
        val weightViewModel = SimpleMetricDetailViewModel(
            savedStateHandle(HealthMetric.WEIGHT, LocalDate.now()),
            FakeHealthRepository(),
            FakeSourcePriorityRepository(),
        )

        val hydrationState = hydrationViewModel.uiState.value
        val weightState = weightViewModel.uiState.value
        check(hydrationState is SimpleMetricDetailUiState.Hydration)
        check(weightState is SimpleMetricDetailUiState.Weight)
        assertEquals(0.0, hydrationState.consumedLitres, 0.001)
        assertEquals(0.0, weightState.currentKg, 0.001)
    }
}
