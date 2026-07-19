package dev.pulsereport.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.PulseReportTheme
import dev.pulsereport.core.ui.theme.PulseTheme

/** One segment of a [SegmentedBar]: a relative [weight] and its [color]. */
data class BarSegment(
    val weight: Float,
    val color: Color,
)

/** A slim horizontal bar split into weighted colored segments (sleep stages). */
@Composable
fun SegmentedBar(
    segments: List<BarSegment>,
    modifier: Modifier = Modifier,
) {
    if (segments.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        segments.forEach { segment ->
            Box(
                modifier = Modifier
                    .weight(segment.weight)
                    .fillMaxHeight()
                    .background(segment.color),
            )
        }
    }
}

@Preview
@Composable
private fun SegmentedBarPreview() {
    PulseReportTheme(darkTheme = true) {
        val colors = PulseTheme.colors
        SegmentedBar(
            segments = listOf(
                BarSegment(2f, colors.sleepStageDeep),
                BarSegment(5f, colors.sleepStageCore),
                BarSegment(2f, colors.sleepStageRem),
            ),
        )
    }
}
