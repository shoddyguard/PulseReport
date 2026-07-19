package dev.pulsereport.feature.diary

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
data object DiaryRoute

fun NavGraphBuilder.diaryScreen() {
    composable<DiaryRoute> {
        DiaryScreen()
    }
}

@HiltViewModel
class DiaryViewModel @Inject constructor() : ViewModel()

@Composable
fun DiaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel(),
) {
    PlaceholderScreen(
        title = "Diary",
        description = "Keep daily notes alongside your health history. Coming soon.",
        modifier = modifier,
    )
}
