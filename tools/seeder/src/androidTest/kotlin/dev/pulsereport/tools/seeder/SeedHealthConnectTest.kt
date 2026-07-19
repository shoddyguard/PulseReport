package dev.pulsereport.tools.seeder

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Self-instrumented: grants its own write permissions (plus one read permission used only for
 * its own sanity check) via `pm grant` (GrantPermissionRule is historically unreliable for
 * `android.permission.health.*`), seeds 30 days of data, then reads yesterday+today's step
 * count back as a sanity check. Wrapped by the `seedHealthConnect` Gradle task.
 */
@RunWith(AndroidJUnit4::class)
class SeedHealthConnectTest {

    @Before
    fun grantHealthPermissions() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        (REQUIRED_WRITE_PERMISSIONS + SANITY_CHECK_READ_PERMISSION).forEach { permission ->
            uiAutomation.executeShellCommand("pm grant $targetPackage $permission").use { pfd ->
                // Note: draining the fd before it closes is required, or the grant may not apply.
                FileInputStream(pfd.fileDescriptor).use { it.readBytes() }
            }
        }
    }

    @Test
    fun seedsHealthConnectAndReadsBackRecentSteps() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        assumeTrue(
            "Health Connect is not available on this device/emulator image; skipping. " +
                "See docs/health-connect-testing.md.",
            HealthConnectClient.getSdkStatus(targetContext) == HealthConnectClient.SDK_AVAILABLE,
        )

        val client = HealthConnectClient.getOrCreate(targetContext)

        val granted = client.permissionController.getGrantedPermissions()
        assertTrue(
            "Missing Health Connect write permissions: ${REQUIRED_WRITE_PERMISSIONS - granted}. " +
                "See docs/health-connect-testing.md for troubleshooting `pm grant`.",
            granted.containsAll(REQUIRED_WRITE_PERMISSIONS),
        )

        HealthConnectSeeder(client, scale = BuildConfig.SEED_VALUE_SCALE).seed()

        // 2026-07-18: reading this back turned up two Health Connect quirks.
        //
        // aggregate(COUNT_TOTAL) silently returns 0 for this app's steps: Health Connect only
        // aggregates apps on a category's priority list, and `pm grant` doesn't add us to it
        // the way the real permission sheet would. readRecords isn't affected, so we use that
        // instead.
        //
        // TimeRangeFilter.between(LocalDateTime, LocalDateTime) silently matched nothing: its
        // zone handling didn't line up with SeedData's explicit offsets. The Instant overload
        // below doesn't have that problem.
        //
        // Yesterday is always fully populated; today can be smaller (or empty) if seeded
        // before 08:00 local time, since Health Connect rejects future-start records.
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    today.minusDays(1).atStartOfDay(zone).toInstant(),
                    today.plusDays(1).atStartOfDay(zone).toInstant(),
                ),
                dataOriginFilter = setOf(DataOrigin(targetContext.packageName)),
            ),
        )
        val expectedFloor = (4_000 * BuildConfig.SEED_VALUE_SCALE).toLong()
        val recentSteps = response.records.sumOf { it.count }
        assertTrue(
            "Expected at least $expectedFloor steps seeded for yesterday+today, got $recentSteps.",
            recentSteps >= expectedFloor,
        )
    }
}
