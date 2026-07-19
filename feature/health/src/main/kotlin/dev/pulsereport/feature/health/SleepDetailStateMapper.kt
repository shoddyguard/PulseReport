package dev.pulsereport.feature.health

import dev.pulsereport.core.model.SleepDetail
import dev.pulsereport.core.model.SleepStage
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

/** Legend/lane order, deepest sleep first, matching [dev.pulsereport.feature.health.ui.SleepStagesChart]'s lanes. */
private val STAGE_ORDER = listOf(SleepStage.DEEP, SleepStage.LIGHT, SleepStage.REM, SleepStage.AWAKE)

/** Maps a raw [SleepDetail] read into the sleep detail screen's state, or the no-data default when [detail] is null. */
fun mapSleepDetailState(detail: SleepDetail?): SleepDetailUiState {
    if (detail == null || detail.total.isZero) return SleepDetailUiState()

    val zone = ZoneId.systemDefault()
    val totalMillis = Duration.between(detail.start, detail.end).toMillis().coerceAtLeast(1).toFloat()
    val durationsByStage = detail.durationsByStage()

    return SleepDetailUiState(
        hasData = true,
        durationLabel = detail.total.toLabel(),
        bedTimeLabel = TIME_FORMAT.format(detail.start.atZone(zone)),
        wakeTimeLabel = TIME_FORMAT.format(detail.end.atZone(zone)),
        segments = detail.stages.map { segment ->
            SleepChartSegment(
                stage = segment.stage,
                startFraction = Duration.between(detail.start, segment.start).toMillis() / totalMillis,
                endFraction = Duration.between(detail.start, segment.end).toMillis() / totalMillis,
            )
        },
        stageTotals = STAGE_ORDER.mapNotNull { stage ->
            val duration = durationsByStage[stage] ?: Duration.ZERO
            if (duration.isZero) null else SleepStageTotal(stage = stage, durationLabel = duration.toLabel())
        },
    )
}

private fun Duration.toLabel(): String = "${toHours()}h ${toMinutesPart().toString().padStart(2, '0')}m"
