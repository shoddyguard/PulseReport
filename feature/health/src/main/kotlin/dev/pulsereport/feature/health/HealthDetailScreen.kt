package dev.pulsereport.feature.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.metric.displayLabel
import dev.pulsereport.core.ui.theme.PulseTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The drill-down screen for one [HealthMetric], reached from the My Health hub or a dashboard tile. */
@Composable
fun HealthDetailScreen(
    metric: HealthMetric,
    date: LocalDate,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        DetailTopBar(metric = metric, date = date, onBack = onBack)
        when (metric) {
            HealthMetric.SLEEP -> SleepDetailContent(
                viewModel = hiltViewModel(),
                modifier = Modifier.weight(1f),
            )
            else -> SimpleMetricDetailContent(
                viewModel = hiltViewModel(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    metric: HealthMetric,
    date: LocalDate,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = if (date == LocalDate.now()) {
        "Today"
    } else {
        date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
    }

    Row(
        modifier = modifier.padding(top = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = PulseIcons.ChevronLeft,
                contentDescription = "Back",
                tint = PulseTheme.colors.textSecondary,
            )
        }
        Column {
            Text(
                text = metric.displayLabel(),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = PulseTheme.colors.textMuted,
            )
        }
    }
}
