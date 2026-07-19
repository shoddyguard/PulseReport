package dev.pulsereport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.PulseReportTheme

/**
 * Opened from the Health Connect permission sheet when the user asks why
 * PulseReport wants access to their data.
 */
class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseReportTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                    ) {
                        Text(
                            text = "Privacy policy",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = "PulseReport reads your health data from Health Connect " +
                                "solely to show it to you in dashboards and to let you " +
                                "export it yourself. Your data stays on this device. " +
                                "Nothing is uploaded, shared, or sold, and no analytics " +
                                "are collected.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
