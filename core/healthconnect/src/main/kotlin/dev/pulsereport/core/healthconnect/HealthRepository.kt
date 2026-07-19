package dev.pulsereport.core.healthconnect

import dev.pulsereport.core.model.DailySteps
import dev.pulsereport.core.model.DailyValue
import dev.pulsereport.core.model.HealthConnectAvailability
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.model.HeartSummary
import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepSummary
import java.time.LocalDate

/** Read access to aggregated health data. */
interface HealthRepository {

    /** The Health Connect permission strings the app needs. */
    val requiredPermissions: Set<String>

    fun availability(): HealthConnectAvailability

    suspend fun hasAllPermissions(): Boolean

    /**
     * The package names of every app that has written [metric] data in the trailing
     * [days] calendar days. Used by the Sources screen to offer real choices instead of
     * an empty list.
     */
    suspend fun getContributingPackages(metric: HealthMetric, days: Int = 30): Set<String>

    /**
     * Step totals per day for the [days] calendar days ending on [anchor], oldest first.
     * Days without data are zero-filled. When more than one app has written steps for a
     * day, [priority] (highest-priority package first) picks the winning app; an empty
     * list combines every app's steps for that day.
     */
    suspend fun getDailySteps(anchor: LocalDate, days: Int, priority: List<String> = emptyList()): List<DailySteps>

    /**
     * The sleep session covering the night ending on [anchor], or null if there is none.
     * See [getDailySteps] for how [priority] resolves multiple writers.
     */
    suspend fun getSleep(anchor: LocalDate, priority: List<String> = emptyList()): SleepSummary?

    /**
     * The same sleep session as [getSleep], but with per-stage timing (including awake,
     * kept separate rather than folded into light) for the sleep detail screen's hypnogram.
     */
    suspend fun getSleepDetail(anchor: LocalDate, priority: List<String> = emptyList()): SleepDetail?

    /**
     * Heart rate for [anchor]: the day's lowest reading plus a chart-ready sample series.
     * See [getDailySteps] for how [priority] resolves multiple writers.
     */
    suspend fun getHeart(anchor: LocalDate, priority: List<String> = emptyList()): HeartSummary?

    /**
     * Total active calories burned on [anchor], in kilocalories. See [getDailySteps] for
     * how [priority] resolves multiple writers.
     */
    suspend fun getActiveCalories(anchor: LocalDate, priority: List<String> = emptyList()): Double

    /**
     * Total fluid intake on [anchor], in litres. See [getDailySteps] for how [priority]
     * resolves multiple writers.
     */
    suspend fun getHydration(anchor: LocalDate, priority: List<String> = emptyList()): Double

    /**
     * Weight per day for the [days] calendar days ending on [anchor], oldest first, in
     * kilograms. Days without a reading carry forward the most recent prior value. See
     * [getDailySteps] for how [priority] resolves multiple writers.
     */
    suspend fun getDailyWeight(anchor: LocalDate, days: Int, priority: List<String> = emptyList()): List<DailyValue>
}
