package dev.pulsereport.feature.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pulsereport.core.ui.component.PlaceholderScreen
import dev.pulsereport.core.ui.component.TileShape
import dev.pulsereport.core.ui.metric.color
import dev.pulsereport.core.ui.metric.displayLabel
import dev.pulsereport.core.ui.theme.PulseTheme
import dev.pulsereport.feature.health.ui.SleepStagesChart

@Composable
internal fun SleepDetailContent(
    viewModel: SleepDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.hasData) {
        PlaceholderScreen(
            title = "No sleep recorded",
            description = "We didn't find a sleep session for this night in Health Connect.",
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
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = uiState.durationLabel,
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "${uiState.bedTimeLabel} - ${uiState.wakeTimeLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = PulseTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PulseTheme.colors.card, TileShape)
                .padding(16.dp),
        ) {
            SleepStagesChart(segments = uiState.segments)
            StageLegend(stageTotals = uiState.stageTotals, modifier = Modifier.padding(top = 14.dp))
        }
    }
}

@Composable
private fun StageLegend(
    stageTotals: List<SleepStageTotal>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stageTotals.forEach { total ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(total.stage.color(PulseTheme.colors), CircleShape),
                )
                Text(
                    text = total.stage.displayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                )
                Text(
                    text = total.durationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PulseTheme.colors.textSecondary,
                )
            }
        }
    }
}
