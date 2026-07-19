package dev.pulsereport.tools.seeder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Shown when the user taps the privacy policy link in the Health Connect permission sheet. */
class SeederRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Text(
                        text = "PulseReport Seeder writes fake data into Health Connect for local " +
                            "development and testing only. It never reads or shares your data.",
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}
