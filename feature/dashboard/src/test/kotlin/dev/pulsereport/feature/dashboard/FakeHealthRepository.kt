package dev.pulsereport.feature.dashboard

import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HealthConnectAvailability
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepSummary
import java.time.Duration
import java.time.LocalDate

/**
 * A [HealthRepository] fake for [DashboardViewModelTest]. Step counts vary by date so tests
 * can tell a reload for a new date apart from the initial load; every other metric returns
 * a fixed reading regardless of date. [receivedPriorities] records the last priority list
 * each metric's read was called with, so tests can assert priorities reach the repository.
 */
class FakeHealthRepository(
    private val stepsByDate: Map<LocalDate, Long> = emptyMap(),
    private val defaultSteps: Long = 8_432L,
) : HealthRepository {

    val receivedPriorities: MutableMap<HealthMetric, List<String>> = mutableMapOf()

    override val requiredPermissions: Set<String> = emptySet()

    override fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun hasAllPermissions(): Boolean = true

    override suspend fun getContributingPackages(metric: HealthMetric, days: Int): Set<String> = emptySet()

    override suspend fun getDailySteps(anchor: LocalDate, days: Int, priority: List<String>): List<DailySteps> {
        receivedPriorities[HealthMetric.STEPS] = priority
        return (days - 1 downTo 0).map { offset ->
            val date = anchor.minusDays(offset.toLong())
            DailySteps(date, stepsByDate[date] ?: defaultSteps)
        }
    }

    override suspend fun getSleep(anchor: LocalDate, priority: List<String>): SleepSummary {
        receivedPriorities[HealthMetric.SLEEP] = priority
        return SleepSummary(
            total = Duration.ofHours(7).plusMinutes(39),
            deep = Duration.ofMinutes(90),
            light = Duration.ofMinutes(300),
            rem = Duration.ofMinutes(69),
        )
    }

    override suspend fun getSleepDetail(anchor: LocalDate, priority: List<String>): SleepDetail? {
        receivedPriorities[HealthMetric.SLEEP] = priority
        return null
    }

    override suspend fun getHeart(anchor: LocalDate, priority: List<String>): HeartSummary {
        receivedPriorities[HealthMetric.HEART_RATE] = priority
        return HeartSummary(
            restingBpm = 58,
            series = listOf(12f, 14f, 8f, 24f, 16f, 26f, 14f, 18f),
        )
    }

    override suspend fun getActiveCalories(anchor: LocalDate, priority: List<String>): Double {
        receivedPriorities[HealthMetric.CALORIES] = priority
        return 612.0
    }

    override suspend fun getHydration(anchor: LocalDate, priority: List<String>): Double {
        receivedPriorities[HealthMetric.HYDRATION] = priority
        return 1.6
    }

    override suspend fun getDailyWeight(anchor: LocalDate, days: Int, priority: List<String>): List<DailyValue> {
        receivedPriorities[HealthMetric.WEIGHT] = priority
        return (days - 1 downTo 0).map { offset -> DailyValue(anchor.minusDays(offset.toLong()), 78.4) }
    }
}
