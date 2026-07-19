package dev.pulsereport.feature.health

import androidx.lifecycle.SavedStateHandle
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepStage
import dev.pulsereport.core.model.SleepStageSegment
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepDetailViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun savedStateHandle(date: LocalDate) =
        SavedStateHandle(mapOf("metric" to HealthMetric.SLEEP.name, "epochDay" to date.toEpochDay()))

    @Test
    fun `loads sleep detail for the date carried by the route`() = runTest {
        val date = LocalDate.of(2026, 7, 18)
        val start = Instant.parse("2026-07-18T22:00:00Z")
        val end = Instant.parse("2026-07-19T06:00:00Z")
        val detail = SleepDetail(
            start = start,
            end = end,
            total = Duration.between(start, end),
            stages = listOf(SleepStageSegment(SleepStage.LIGHT, start, end)),
        )
        val healthRepository = FakeHealthRepository(sleepDetail = detail)

        val viewModel = SleepDetailViewModel(
            savedStateHandle(date),
            healthRepository,
            FakeSourcePriorityRepository(),
        )

        assertEquals(true, viewModel.uiState.value.hasData)
        assertEquals("8h 00m", viewModel.uiState.value.durationLabel)
    }

    @Test
    fun `a saved sleep source priority reaches the repository read`() = runTest {
        val healthRepository = FakeHealthRepository()
        val priorityRepository = FakeSourcePriorityRepository(
            initial = mapOf(HealthMetric.SLEEP to listOf("com.garmin")),
        )

        SleepDetailViewModel(savedStateHandle(LocalDate.now()), healthRepository, priorityRepository)

        assertEquals(listOf("com.garmin"), healthRepository.receivedPriorities[HealthMetric.SLEEP])
    }

    @Test
    fun `no sleep session for the night produces the no-data state`() = runTest {
        val viewModel = SleepDetailViewModel(
            savedStateHandle(LocalDate.now()),
            FakeHealthRepository(sleepDetail = null),
            FakeSourcePriorityRepository(),
        )

        assertEquals(false, viewModel.uiState.value.hasData)
    }
}
