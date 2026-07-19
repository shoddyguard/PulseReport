package dev.pulsereport.core.ui.metric

import androidx.compose.ui.graphics.vector.ImageVector
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.theme.MetricAccent
import dev.pulsereport.core.ui.theme.PulseColors

/** Shared icon/color/label presentation for a [HealthMetric], used anywhere it's listed: Sources, the My Health hub, dashboard tile navigation. */
fun HealthMetric.accent(colors: PulseColors): MetricAccent = when (this) {
    HealthMetric.STEPS -> colors.steps
    HealthMetric.SLEEP -> colors.sleep
    HealthMetric.HEART_RATE -> colors.heart
    HealthMetric.CALORIES -> colors.calories
    HealthMetric.HYDRATION -> colors.water
    HealthMetric.WEIGHT -> colors.weight
}

fun HealthMetric.icon(): ImageVector = when (this) {
    HealthMetric.STEPS -> PulseIcons.Walk
    HealthMetric.SLEEP -> PulseIcons.Bedtime
    HealthMetric.HEART_RATE -> PulseIcons.Heart
    HealthMetric.CALORIES -> PulseIcons.Fire
    HealthMetric.HYDRATION -> PulseIcons.WaterDrop
    HealthMetric.WEIGHT -> PulseIcons.Weight
}

fun HealthMetric.displayLabel(): String = when (this) {
    HealthMetric.STEPS -> "Steps"
    HealthMetric.SLEEP -> "Sleep"
    HealthMetric.HEART_RATE -> "Heart"
    HealthMetric.CALORIES -> "Active calories"
    HealthMetric.HYDRATION -> "Hydration"
    HealthMetric.WEIGHT -> "Weight"
}
