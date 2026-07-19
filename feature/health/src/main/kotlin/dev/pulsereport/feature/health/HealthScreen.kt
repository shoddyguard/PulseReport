package dev.pulsereport.feature.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.ui.component.TileShape
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.metric.accent
import dev.pulsereport.core.ui.metric.displayLabel
import dev.pulsereport.core.ui.metric.icon
import dev.pulsereport.core.ui.theme.PulseTheme
import java.time.LocalDate

/** The bottom-nav "My Health" hub: every area a user can drill into, one row per [HealthMetric]. */
@Composable
fun HealthScreen(
    onOpenDetail: (HealthMetric, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "My Health",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp, start = 4.dp, bottom = 4.dp),
        )
        HealthMetric.entries.forEach { metric ->
            HealthAreaRow(
                metric = metric,
                onClick = { onOpenDetail(metric, LocalDate.now()) },
            )
        }
    }
}

@Composable
private fun HealthAreaRow(
    metric: HealthMetric,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = metric.accent(PulseTheme.colors)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(TileShape)
            .background(PulseTheme.colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = metric.icon(),
            contentDescription = null,
            tint = accent.accent,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = metric.displayLabel(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        Icon(
            imageVector = PulseIcons.ChevronRight,
            contentDescription = null,
            tint = PulseTheme.colors.textMuted,
            modifier = Modifier.size(22.dp),
        )
    }
}
