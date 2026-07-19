package dev.pulsereport.feature.health.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.model.SleepStage
import dev.pulsereport.core.ui.metric.color
import dev.pulsereport.core.ui.theme.PulseReportTheme
import dev.pulsereport.core.ui.theme.PulseTheme
import dev.pulsereport.feature.health.SleepChartSegment

/** Lanes top to bottom: awake sits highest (matching how sleep-tracker hypnograms are usually drawn), deep sleep lowest. */
private val LANE_ORDER = listOf(SleepStage.AWAKE, SleepStage.REM, SleepStage.LIGHT, SleepStage.DEEP)
private const val LANE_GAP_FRACTION = 0.22f

/** A swimlane hypnogram: one lane per sleep stage, with a coloured block wherever [segments] records that stage. */
@Composable
fun SleepStagesChart(
    segments: List<SleepChartSegment>,
    modifier: Modifier = Modifier,
) {
    if (segments.isEmpty()) return

    val colors = PulseTheme.colors
    val laneColors = LANE_ORDER.map { stage -> stage.color(colors) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        val laneHeight = size.height / LANE_ORDER.size
        val laneGap = laneHeight * LANE_GAP_FRACTION
        val blockHeight = laneHeight - laneGap
        val corner = CornerRadius(3.dp.toPx(), 3.dp.toPx())

        segments.forEach { segment ->
            val laneIndex = LANE_ORDER.indexOf(segment.stage)
            if (laneIndex < 0) return@forEach

            val x0 = segment.startFraction.coerceIn(0f, 1f) * size.width
            val x1 = segment.endFraction.coerceIn(0f, 1f) * size.width
            val y0 = laneIndex * laneHeight + laneGap / 2

            drawRoundRect(
                color = laneColors[laneIndex],
                topLeft = Offset(x0, y0),
                size = Size((x1 - x0).coerceAtLeast(1f), blockHeight),
                cornerRadius = corner,
            )
        }
    }
}

@Preview
@Composable
private fun SleepStagesChartPreview() {
    PulseReportTheme(darkTheme = true) {
        SleepStagesChart(
            segments = listOf(
                SleepChartSegment(SleepStage.LIGHT, 0f, 0.08f),
                SleepChartSegment(SleepStage.DEEP, 0.08f, 0.32f),
                SleepChartSegment(SleepStage.LIGHT, 0.32f, 0.45f),
                SleepChartSegment(SleepStage.REM, 0.45f, 0.58f),
                SleepChartSegment(SleepStage.LIGHT, 0.58f, 0.7f),
                SleepChartSegment(SleepStage.AWAKE, 0.7f, 0.74f),
                SleepChartSegment(SleepStage.LIGHT, 0.74f, 0.86f),
                SleepChartSegment(SleepStage.REM, 0.86f, 1f),
            ),
        )
    }
}
