package dev.pulsereport.feature.dashboard

import dev.pulsereport.core.model.HealthMetric
import java.time.LocalDate
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
class DashboardViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads dashboard state from the repository on start`() = runTest {
        val viewModel = DashboardViewModel(FakeHealthRepository(), FakeSourcePriorityRepository())

        val state = viewModel.uiState.value

        assertEquals(8_432, state.steps.todaySteps)
        assertEquals(58, state.heart.restingBpm)
        assertEquals(LocalDate.now(), state.selectedDate)
    }

    @Test
    fun `onDateSelected reloads state for the newly selected date`() = runTest {
        val pastDate = LocalDate.now().minusDays(10)
        val repository = FakeHealthRepository(stepsByDate = mapOf(pastDate to 1_000L))
        val viewModel = DashboardViewModel(repository, FakeSourcePriorityRepository())

        viewModel.onDateSelected(pastDate)
        val state = viewModel.uiState.value

        assertEquals(pastDate, state.selectedDate)
        assertEquals(1_000, state.steps.todaySteps)
    }

    @Test
    fun `a saved source priority reaches the repository read for that metric`() = runTest {
        val healthRepository = FakeHealthRepository()
        val priorityRepository = FakeSourcePriorityRepository(
            initial = mapOf(HealthMetric.STEPS to listOf("com.fitbit", "com.samsung")),
        )

        DashboardViewModel(healthRepository, priorityRepository)

        assertEquals(listOf("com.fitbit", "com.samsung"), healthRepository.receivedPriorities[HealthMetric.STEPS])
    }

    @Test
    fun `a priority change after start triggers a reload with the new priority`() = runTest {
        val healthRepository = FakeHealthRepository()
        val priorityRepository = FakeSourcePriorityRepository()
        DashboardViewModel(healthRepository, priorityRepository)

        priorityRepository.setPriority(HealthMetric.HEART_RATE, listOf("com.garmin"))

        assertEquals(listOf("com.garmin"), healthRepository.receivedPriorities[HealthMetric.HEART_RATE])
    }

    @Test
    fun `derived values are computed from the raw numbers`() {
        val state = MockDashboardState

        assertEquals(0.8432f, state.steps.goalFraction, 0.0001f)
        assertEquals(3, state.water.filledSlots)
        assertEquals(612f / 700f, state.activeCalories.goalFraction, 0.0001f)
    }

    @Test
    fun `goal fraction caps at one when the goal is exceeded`() {
        val steps = StepsData(
            todaySteps = 12_000,
            goalSteps = 10_000,
            distanceKm = 8.6,
            weekSteps = emptyList(),
        )

        assertEquals(1f, steps.goalFraction, 0f)
    }
}
