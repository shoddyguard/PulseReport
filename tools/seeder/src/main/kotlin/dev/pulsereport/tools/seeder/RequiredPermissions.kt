package dev.pulsereport.tools.seeder

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord

/** The write permissions PulseReport Seeder needs to insert every [SeedData] record type. */
val REQUIRED_WRITE_PERMISSIONS: Set<String> = setOf(
    HealthPermission.getWritePermission(StepsRecord::class),
    HealthPermission.getWritePermission(HeartRateRecord::class),
    HealthPermission.getWritePermission(SleepSessionRecord::class),
    HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
    HealthPermission.getWritePermission(HydrationRecord::class),
    HealthPermission.getWritePermission(WeightRecord::class),
)

/** Used only by [SeedHealthConnectTest]'s own read-back sanity check, never by the UI path. */
val SANITY_CHECK_READ_PERMISSION: String = HealthPermission.getReadPermission(StepsRecord::class)
