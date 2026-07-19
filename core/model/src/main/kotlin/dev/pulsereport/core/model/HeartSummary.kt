package dev.pulsereport.core.model

/** A day's heart rate: [restingBpm] is the day's lowest reading, [series] a chart-ready sample. */
data class HeartSummary(
    val restingBpm: Int,
    val series: List<Float>,
)
