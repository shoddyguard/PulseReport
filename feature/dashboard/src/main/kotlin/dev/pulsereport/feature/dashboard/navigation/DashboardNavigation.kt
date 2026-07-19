package dev.pulsereport.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.feature.dashboard.DashboardScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

fun NavGraphBuilder.dashboardScreen(
    onOpenDiary: () -> Unit,
    onOpenMetric: (HealthMetric, LocalDate) -> Unit,
) {
    composable<DashboardRoute> {
        DashboardScreen(onOpenDiary = onOpenDiary, onOpenMetric = onOpenMetric)
    }
}
