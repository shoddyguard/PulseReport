package dev.pulsereport.tools.seeder

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val DELETE_WINDOW_DAYS = 60L

/**
 * Seeds [SeedData] into Health Connect, attributed to whichever app runs this code (the
 * seeder).
 *
 * We go with delete-then-insert for idempotency: `deleteRecords` only touches records this
 * app wrote, so re-running never double-counts. We could track `clientRecordId` and upsert
 * instead, but delete-then-insert is a lot less code, and this only ever runs against a
 * freshly wiped emulator anyway.
 */
class HealthConnectSeeder(private val client: HealthConnectClient, private val scale: Double = 1.0) {

    suspend fun seed(): Int {
        val records = SeedData.generate(scale = scale)
        val deleteWindow = TimeRangeFilter.between(
            Instant.now().minus(DELETE_WINDOW_DAYS, ChronoUnit.DAYS),
            Instant.now(),
        )

        client.deleteRecords(StepsRecord::class, deleteWindow)
        client.insertRecords(records.steps)

        client.deleteRecords(HeartRateRecord::class, deleteWindow)
        client.insertRecords(records.heartRate)

        client.deleteRecords(SleepSessionRecord::class, deleteWindow)
        client.insertRecords(records.sleep)

        client.deleteRecords(ActiveCaloriesBurnedRecord::class, deleteWindow)
        client.insertRecords(records.activeCalories)

        client.deleteRecords(HydrationRecord::class, deleteWindow)
        client.insertRecords(records.hydration)

        client.deleteRecords(WeightRecord::class, deleteWindow)
        client.insertRecords(records.weight)

        return records.totalCount
    }
}
