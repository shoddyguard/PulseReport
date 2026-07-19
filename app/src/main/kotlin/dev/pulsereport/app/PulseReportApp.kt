package dev.pulsereport.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.pulsereport.core.ui.component.PulseNavItem
import dev.pulsereport.core.ui.component.PulseNavigationBar
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.feature.dashboard.navigation.DashboardRoute
import dev.pulsereport.feature.dashboard.navigation.dashboardScreen
import dev.pulsereport.feature.diary.DiaryRoute
import dev.pulsereport.feature.diary.diaryScreen
import dev.pulsereport.feature.export.ExportRoute
import dev.pulsereport.feature.export.exportScreen
import dev.pulsereport.feature.health.navigation.HealthDetailRoute
import dev.pulsereport.feature.health.navigation.HealthRoute
import dev.pulsereport.feature.health.navigation.healthDetailScreen
import dev.pulsereport.feature.health.navigation.healthScreen
import dev.pulsereport.feature.sources.SourcesRoute
import dev.pulsereport.feature.sources.sourcesScreen
import kotlin.reflect.KClass

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val navItem: PulseNavItem,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = DashboardRoute,
        routeClass = DashboardRoute::class,
        navItem = PulseNavItem("Home", PulseIcons.Home, PulseIcons.HomeFill),
    ),
    TopLevelDestination(
        route = HealthRoute,
        routeClass = HealthRoute::class,
        navItem = PulseNavItem("My Health", PulseIcons.HealthOutlined, PulseIcons.Heart),
    ),
    TopLevelDestination(
        route = DiaryRoute,
        routeClass = DiaryRoute::class,
        navItem = PulseNavItem("Diary", PulseIcons.Book, PulseIcons.BookFill),
    ),
    TopLevelDestination(
        route = ExportRoute,
        routeClass = ExportRoute::class,
        navItem = PulseNavItem("Export", PulseIcons.Share, PulseIcons.ShareFill),
    ),
    TopLevelDestination(
        route = SourcesRoute,
        routeClass = SourcesRoute::class,
        navItem = PulseNavItem("Sources", PulseIcons.Tune, PulseIcons.TuneFill),
    ),
)

@Composable
fun PulseReportApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedIndex = topLevelDestinations.indexOfFirst { destination ->
        currentDestination?.hasRoute(destination.routeClass) == true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PulseNavigationBar(
                items = topLevelDestinations.map(TopLevelDestination::navItem),
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    navController.navigateToTab(topLevelDestinations[index].route)
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            dashboardScreen(
                onOpenDiary = { navController.navigateToTab(DiaryRoute) },
                onOpenMetric = { metric, date -> navController.navigate(HealthDetailRoute(metric.name, date.toEpochDay())) },
            )
            healthScreen(
                onOpenDetail = { metric, date ->
                    navController.navigate(HealthDetailRoute(metric.name, date.toEpochDay()))
                },
            )
            healthDetailScreen(onBack = { navController.popBackStack() })
            diaryScreen()
            exportScreen()
            sourcesScreen()
        }
    }
}

private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
