package dev.pulsereport.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.PulseReportTheme
import dev.pulsereport.core.ui.theme.PulseTheme

/**
 * A compact bar chart with rounded bars. The last bar (today) is drawn at
 * full opacity, earlier bars at 60%, matching the mockup.
 */
@Composable
fun MiniBarChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
    ) {
        val max = values.max().coerceAtLeast(1f)
        val slotWidth = size.width / values.size
        val barWidth = slotWidth * 0.75f
        val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        val minBarHeight = corner.y * 2

        values.forEachIndexed { index, value ->
            val barHeight = ((value / max) * size.height).coerceAtLeast(minBarHeight)
            drawRoundRect(
                color = color,
                alpha = if (index == values.lastIndex) 1f else 0.6f,
                topLeft = Offset(
                    x = index * slotWidth + (slotWidth - barWidth) / 2f,
                    y = size.height - barHeight,
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = corner,
            )
        }
    }
}

@Preview
@Composable
private fun MiniBarChartPreview() {
    PulseReportTheme(darkTheme = true) {
        MiniBarChart(
            values = listOf(22f, 32f, 16f, 26f, 36f, 20f, 28f),
            color = PulseTheme.colors.steps.accent,
        )
    }
}
