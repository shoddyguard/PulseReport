package dev.pulsereport.feature.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pulsereport.core.ui.component.MiniBarChart
import dev.pulsereport.core.ui.component.PlaceholderScreen
import dev.pulsereport.core.ui.component.ProgressRing
import dev.pulsereport.core.ui.component.Sparkline
import dev.pulsereport.core.ui.component.TileShape
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.theme.PulseTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/** Backs every metric detail screen except Sleep - see [SleepDetailContent] for that one. */
@Composable
internal fun SimpleMetricDetailContent(
    viewModel: SimpleMetricDetailViewModel,
    modifier: Modifier = Modifier,
) {
    // Captured into a plain val (rather than branching on the `by` delegate directly) so the
    // `when` below can smart-cast each sealed variant.
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    if (state is SimpleMetricDetailUiState.Heart && !state.hasData) {
        PlaceholderScreen(
            title = "No heart rate recorded",
            description = "We didn't find any heart rate readings for this day in Health Connect.",
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            is SimpleMetricDetailUiState.Steps -> StepsDetail(state)
            is SimpleMetricDetailUiState.Weight -> WeightDetail(state)
            is SimpleMetricDetailUiState.Heart -> HeartDetail(state)
            is SimpleMetricDetailUiState.Calories -> CaloriesDetail(state)
            is SimpleMetricDetailUiState.Hydration -> HydrationDetail(state)
        }
    }
}

@Composable
private fun StepsDetail(data: SimpleMetricDetailUiState.Steps) {
    val accent = PulseTheme.colors.steps
    Column(modifier = Modifier.padding(start = 4.dp)) {
        Text(text = "%,d".format(data.todayCount), style = MaterialTheme.typography.displaySmall)
        Text(
            text = "${(data.goalFraction * 100).roundToInt()}% of ${"%,d".format(data.goalCount)} goal · ${data.distanceKm} km",
            style = MaterialTheme.typography.bodyMedium,
            color = PulseTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
    MetricCard {
        MiniBarChart(
            values = data.weekCounts.map(Int::toFloat),
            color = accent.accent,
            modifier = Modifier.height(80.dp),
        )
    }
}

@Composable
private fun WeightDetail(data: SimpleMetricDetailUiState.Weight) {
    val accent = PulseTheme.colors.weight
    val direction = if (data.weekDeltaKg <= 0) "−" else "+"
    Column(modifier = Modifier.padding(start = 4.dp)) {
        Text(
            text = buildAnnotatedString {
                append("%.1f ".format(data.currentKg))
                withStyle(MaterialTheme.typography.titleLarge.copy(color = PulseTheme.colors.textMuted).toSpanStyle()) {
                    append("kg")
                }
            },
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = "$direction%.1f kg this week".format(abs(data.weekDeltaKg)),
            style = MaterialTheme.typography.bodyMedium,
            color = accent.accent,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
    MetricCard {
        Sparkline(
            values = data.series,
            color = accent.accent,
            showAreaFill = true,
            showEndDot = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        )
    }
}

@Composable
private fun HeartDetail(data: SimpleMetricDetailUiState.Heart) {
    val accent = PulseTheme.colors.heart
    Column(modifier = Modifier.padding(start = 4.dp)) {
        Text(
            text = buildAnnotatedString {
                append("${data.restingBpm} ")
                withStyle(MaterialTheme.typography.titleLarge.copy(color = PulseTheme.colors.textMuted).toSpanStyle()) {
                    append("rest")
                }
            },
            style = MaterialTheme.typography.displaySmall,
        )
    }
    MetricCard {
        Sparkline(
            values = data.series,
            color = accent.accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        )
    }
}

@Composable
private fun CaloriesDetail(data: SimpleMetricDetailUiState.Calories) {
    val accent = PulseTheme.colors.calories
    MetricCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProgressRing(
                progress = data.goalFraction,
                color = accent.accent,
                trackColor = accent.border,
                size = 96.dp,
                strokeWidth = 8.dp,
            ) {
                Icon(
                    imageVector = PulseIcons.Fire,
                    contentDescription = null,
                    tint = accent.accent,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(text = "%,d".format(data.burnedKcal), style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "of ${"%,d".format(data.goalKcal)} kcal goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PulseTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun HydrationDetail(data: SimpleMetricDetailUiState.Hydration) {
    val accent = PulseTheme.colors.water
    MetricCard {
        Text(
            text = buildAnnotatedString {
                append("${data.consumedLitres} ")
                withStyle(MaterialTheme.typography.titleLarge.copy(color = PulseTheme.colors.textMuted).toSpanStyle()) {
                    append("/ ${data.goalLitres} L")
                }
            },
            style = MaterialTheme.typography.displaySmall,
        )
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(data.totalSlots) { index ->
                Icon(
                    imageVector = PulseIcons.WaterDrop,
                    contentDescription = null,
                    tint = if (index < data.filledSlots) accent.accent else accent.tintBase.copy(alpha = 0.30f),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseTheme.colors.card, TileShape)
            .padding(16.dp),
    ) {
        content()
    }
}
