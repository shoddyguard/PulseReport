package dev.pulsereport.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.theme.PulseReportTheme
import dev.pulsereport.core.ui.theme.PulseTheme

/** A pill-shaped status chip with a leading icon (e.g. "4 synced"). */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = PulseIcons.CheckCircle,
) {
    Row(
        modifier = modifier
            .background(PulseTheme.colors.card, CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PulseTheme.colors.accent,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PulseTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Preview
@Composable
private fun StatusChipPreview() {
    PulseReportTheme(darkTheme = true) {
        StatusChip(text = "4 synced")
    }
}
