package dev.pulsereport.tools.seeder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SeederActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    SeederScreen()
                }
            }
        }
    }
}

@Composable
private fun SeederScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { HealthConnectClient.getOrCreate(context) }
    var status by remember { mutableStateOf("Ready to seed Health Connect.") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(REQUIRED_WRITE_PERMISSIONS)) {
            scope.launch { status = runSeed(client) }
        } else {
            status = "Permissions denied, cannot seed."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(text = status)
        Button(onClick = {
            if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
                status = "Health Connect is not available on this device."
                return@Button
            }
            scope.launch {
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(REQUIRED_WRITE_PERMISSIONS)) {
                    status = runSeed(client)
                } else {
                    permissionLauncher.launch(REQUIRED_WRITE_PERMISSIONS)
                }
            }
        }) {
            Text("Seed 30 days")
        }
    }
}

private suspend fun runSeed(client: HealthConnectClient): String =
    try {
        val count = HealthConnectSeeder(client, scale = BuildConfig.SEED_VALUE_SCALE).seed()
        "Inserted $count records."
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Seeding failed: ${e.message}"
    }
