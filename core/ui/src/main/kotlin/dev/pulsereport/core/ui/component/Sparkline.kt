package dev.pulsereport.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.PulseReportTheme
import dev.pulsereport.core.ui.theme.PulseTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width

/**
 * A simple line chart of [values]. Optionally fills the area under the line
 * with a vertical accent gradient and marks the final point with a dot.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    showAreaFill: Boolean = false,
    showEndDot: Boolean = false,
) {
    if (values.size < 2) return

    Canvas(modifier = modifier) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0f } ?: 1f
        // Leave headroom so the stroke and dot are not clipped.
        val padY = 4.dp.toPx()
        val usableHeight = size.height - padY * 2

        val points = values.mapIndexed { index, value ->
            Offset(
                x = index * (size.width / (values.size - 1)),
                y = padY + (1f - (value - min) / range) * usableHeight,
            )
        }

        if (showAreaFill) {
            val area = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0f)),
                ),
            )
        }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = line,
            color = color,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        if (showEndDot) {
            drawCircle(color = color, radius = 3.5.dp.toPx(), center = points.last())
        }
    }
}

@Preview
@Composable
private fun SparklinePreview() {
    PulseReportTheme(darkTheme = true) {
        Sparkline(
            values = listOf(14f, 20f, 12f, 24f, 22f, 32f, 30f),
            color = PulseTheme.colors.weight.accent,
            modifier = Modifier.width(240.dp).height(46.dp),
            showAreaFill = true,
            showEndDot = true,
        )
    }
}
