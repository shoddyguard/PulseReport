package dev.pulsereport.core.healthconnect

fun downsampleSeries(values: List<Float>, targetSize: Int): List<Float> {
    if (values.size <= targetSize) return values
    val step = values.size.toDouble() / targetSize
    return (0 until targetSize).map { index -> values[(index * step).toInt()] }
}
