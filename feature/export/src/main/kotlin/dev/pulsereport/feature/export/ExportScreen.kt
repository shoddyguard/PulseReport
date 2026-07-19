package dev.pulsereport.feature.export

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pulsereport.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object ExportRoute

fun NavGraphBuilder.exportScreen() {
    composable<ExportRoute> {
        ExportScreen()
    }
}

@HiltViewModel
class ExportViewModel @Inject constructor() : ViewModel()

@Composable
fun ExportScreen(
    modifier: Modifier = Modifier,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    PlaceholderScreen(
        title = "Export",
        description = "Share your data with an LLM or healthcare professional. Coming soon.",
        modifier = modifier,
    )
}
