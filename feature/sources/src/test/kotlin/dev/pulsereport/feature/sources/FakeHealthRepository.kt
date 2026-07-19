package dev.pulsereport.feature.sources

import dev.pulsereport.core.healthconnect.HealthRepository
import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HealthConnectAvailability
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepSummary
import java.time.LocalDate

/** A [HealthRepository] fake for [SourcesViewModelTest]; only [getContributingPackages] is exercised. */
class FakeHealthRepository(
    private val packagesByMetric: Map<HealthMetric, Set<String>> = emptyMap(),
) : HealthRepository {

    override val requiredPermissions: Set<String> = emptySet()

    override fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun hasAllPermissions(): Boolean = true

    override suspend fun getContributingPackages(metric: HealthMetric, days: Int): Set<String> =
        packagesByMetric[metric].orEmpty()

    override suspend fun getDailySteps(anchor: LocalDate, days: Int, priority: List<String>): List<DailySteps> =
        emptyList()

    override suspend fun getSleep(anchor: LocalDate, priority: List<String>): SleepSummary? = null

    override suspend fun getSleepDetail(anchor: LocalDate, priority: List<String>): SleepDetail? = null

    override suspend fun getHeart(anchor: LocalDate, priority: List<String>): HeartSummary? = null

    override suspend fun getActiveCalories(anchor: LocalDate, priority: List<String>): Double = 0.0

    override suspend fun getHydration(anchor: LocalDate, priority: List<String>): Double = 0.0

    override suspend fun getDailyWeight(anchor: LocalDate, days: Int, priority: List<String>): List<DailyValue> =
        emptyList()
}
