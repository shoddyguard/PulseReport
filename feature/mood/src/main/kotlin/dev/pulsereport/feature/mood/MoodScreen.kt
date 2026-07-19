package dev.pulsereport.feature.mood

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
data object MoodRoute

fun NavGraphBuilder.moodScreen() {
    composable<MoodRoute> {
        MoodScreen()
    }
}

@HiltViewModel
class MoodViewModel @Inject constructor() : ViewModel()

@Composable
fun MoodScreen(
    modifier: Modifier = Modifier,
    viewModel: MoodViewModel = hiltViewModel(),
) {
    PlaceholderScreen(
        title = "Mood",
        description = "Log how you feel and correlate it with your health data. Coming soon.",
        modifier = modifier,
    )
}
