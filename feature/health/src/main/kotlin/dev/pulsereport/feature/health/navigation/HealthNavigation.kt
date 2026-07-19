package dev.pulsereport.feature.health.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.feature.health.HealthDetailScreen
import dev.pulsereport.feature.health.HealthScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object HealthRoute

/** [metric] is a [HealthMetric] name and [epochDay] a [LocalDate.toEpochDay]; both travel better as primitives than as custom nav types. */
@Serializable
data class HealthDetailRoute(val metric: String, val epochDay: Long)

/**
 * The [SavedStateHandle][androidx.lifecycle.SavedStateHandle] keys [HealthDetailRoute]'s args
 * land under, doubling as property-name references so a field rename can't silently desync
 * them. Detail ViewModels read these directly rather than via `SavedStateHandle.toRoute()` -
 * that helper round-trips through a real android.os.Bundle, which needs Robolectric to work in
 * a plain JUnit test and we didn't want to pull that in just for two primitives.
 */
val HEALTH_DETAIL_METRIC_KEY = HealthDetailRoute::metric.name
val HEALTH_DETAIL_EPOCH_DAY_KEY = HealthDetailRoute::epochDay.name

fun NavGraphBuilder.healthScreen(onOpenDetail: (HealthMetric, LocalDate) -> Unit) {
    composable<HealthRoute> {
        HealthScreen(onOpenDetail = onOpenDetail)
    }
}

fun NavGraphBuilder.healthDetailScreen(onBack: () -> Unit) {
    composable<HealthDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<HealthDetailRoute>()
        HealthDetailScreen(
            metric = HealthMetric.valueOf(route.metric),
            date = LocalDate.ofEpochDay(route.epochDay),
            onBack = onBack,
        )
    }
}
