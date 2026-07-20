package dev.pulsereport.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.ui.component.BarSegment
import dev.pulsereport.core.ui.component.MetricTile
import dev.pulsereport.core.ui.component.MiniBarChart
import dev.pulsereport.core.ui.component.ProgressRing
import dev.pulsereport.core.ui.component.SegmentedBar
import dev.pulsereport.core.ui.component.Sparkline
import dev.pulsereport.core.ui.component.StatusChip
import dev.pulsereport.core.ui.component.TileGradient
import dev.pulsereport.core.ui.component.TileShape
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.theme.PulseTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** How far back the date picker lets you go, matching the seeder's 30-day window. */
private const val SELECTABLE_DAYS_BACK = 29L
private val RefreshTriggerDistance = 48.dp

@Composable
fun DashboardScreen(
    onOpenDiary: () -> Unit,
    onOpenMetric: (HealthMetric, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        state = pullToRefreshState,
        threshold = RefreshTriggerDistance,
        modifier = modifier
            .fillMaxSize(),
        indicator = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardHeader(
                selectedDate = uiState.selectedDate,
                syncedSourceCount = uiState.syncedSourceCount,
                isRefreshing = uiState.isRefreshing,
                onDateSelected = viewModel::onDateSelected,
            )
            if (pullToRefreshState.distanceFraction > 0f || uiState.isRefreshing) {
                SyncIndicator()
            }
            PulseScoreCard()
            StepsTile(
                data = uiState.steps,
                onClick = { onOpenMetric(HealthMetric.STEPS, uiState.selectedDate) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SleepTile(
                    data = uiState.sleep,
                    onClick = { onOpenMetric(HealthMetric.SLEEP, uiState.selectedDate) },
                    modifier = Modifier.weight(1f),
                )
                HeartTile(
                    data = uiState.heart,
                    onClick = { onOpenMetric(HealthMetric.HEART_RATE, uiState.selectedDate) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActiveCaloriesTile(
                    data = uiState.activeCalories,
                    onClick = { onOpenMetric(HealthMetric.CALORIES, uiState.selectedDate) },
                    modifier = Modifier.weight(1f),
                )
                WaterTile(
                    data = uiState.water,
                    onClick = { onOpenMetric(HealthMetric.HYDRATION, uiState.selectedDate) },
                    modifier = Modifier.weight(1f),
                )
            }
            WeightTile(
                data = uiState.weight,
                onClick = { onOpenMetric(HealthMetric.WEIGHT, uiState.selectedDate) },
            )
            DiaryCtaTile(onClick = onOpenDiary)
        }
    }
}

@Composable
private fun SyncIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = PulseTheme.colors.accent,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun DashboardHeader(
    selectedDate: LocalDate,
    syncedSourceCount: Int,
    isRefreshing: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val dateText = selectedDate.format(DateTimeFormatter.ofPattern("EEE · d MMM", Locale.getDefault()))
    val titleText = if (selectedDate == today) {
        "Today"
    } else {
        selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clickable { showDatePicker = true }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PulseTheme.colors.textMuted,
                )
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Icon(
                imageVector = PulseIcons.ChevronDown,
                contentDescription = "Choose date",
                tint = PulseTheme.colors.textMuted,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(20.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        StatusChip(text = if (isRefreshing) "Syncing" else "$syncedSourceCount synced")
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(38.dp)
                .background(PulseTheme.colors.avatarBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PulseIcons.Person,
                contentDescription = "Profile",
                tint = PulseTheme.colors.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }

    if (showDatePicker) {
        val earliestSelectable = today.minusDays(SELECTABLE_DAYS_BACK)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = utcTimeMillis.toLocalDateUtc()
                    return !date.isBefore(earliestSelectable) && !date.isAfter(today)
                }

                override fun isSelectableYear(year: Int): Boolean =
                    year in earliestSelectable.year..today.year
            },
        )
        val pickerColors = DatePickerDefaults.colors(containerColor = PulseTheme.colors.card)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateUtc()?.let(onDateSelected)
                        showDatePicker = false
                    },
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            colors = pickerColors,
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = pickerColors,
            )
        }
    }
}

/**
 * Material3's [DatePicker] represents dates as UTC-midnight epoch millis regardless of the
 * device's time zone, so conversion has to go through [ZoneOffset.UTC] rather than the
 * system zone or selecting near a DST boundary would land on the wrong day.
 */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun PulseScoreCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PulseTheme.colors.card, TileShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ProgressRing(
            progress = 0f,
            color = PulseTheme.colors.accent,
            trackColor = PulseTheme.colors.ringTrack,
            size = 58.dp,
        ) {
            Text(
                text = "–",
                style = MaterialTheme.typography.headlineSmall,
                color = PulseTheme.colors.textMuted,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pulse score",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Not enough data yet. Keep syncing to unlock your score.",
                style = MaterialTheme.typography.bodyMedium,
                color = PulseTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Chevron()
    }
}

@Composable
private fun StepsTile(
    data: StepsData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.steps
    MetricTile(
        accent = accent,
        icon = PulseIcons.Walk,
        label = "Steps",
        gradient = TileGradient.Strong,
        onClick = onClick,
        headerTrailing = {
            Text(
                text = "Goal %,d".format(data.goalSteps),
                style = MaterialTheme.typography.labelMedium,
                color = PulseTheme.colors.textSecondary,
            )
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "%,d".format(data.todaySteps),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "${(data.goalFraction * 100).roundToInt()}% · ${data.distanceKm} km",
                style = MaterialTheme.typography.bodyMedium,
                color = accent.accent,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
            )
        }
        MiniBarChart(
            values = data.weekSteps.map(Int::toFloat),
            color = accent.accent,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun SleepTile(
    data: SleepData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PulseTheme.colors
    MetricTile(
        accent = colors.sleep,
        icon = PulseIcons.Bedtime,
        label = "Sleep",
        headerBottomPadding = 12,
        onClick = onClick,
        modifier = modifier,
    ) {
        val hours = data.duration.toHours()
        val minutes = data.duration.toMinutesPart()
        Text(
            text = "${hours}h ${minutes}m",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Score ${data.score} · ${data.scoreLabel}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        )
        SegmentedBar(
            segments = listOf(
                BarSegment(data.stageWeights[0], colors.sleepStageDeep),
                BarSegment(data.stageWeights[1], colors.sleepStageCore),
                BarSegment(data.stageWeights[2], colors.sleepStageRem),
            ),
        )
    }
}

@Composable
private fun HeartTile(
    data: HeartData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.heart
    MetricTile(
        accent = accent,
        icon = PulseIcons.Heart,
        label = "Heart",
        headerBottomPadding = 12,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = buildAnnotatedString {
                append("${data.restingBpm} ")
                withStyle(
                    MaterialTheme.typography.bodyMedium
                        .copy(color = PulseTheme.colors.textMuted)
                        .toSpanStyle(),
                ) {
                    append("rest")
                }
            },
            style = MaterialTheme.typography.headlineSmall,
        )
        Sparkline(
            values = data.series,
            color = accent.accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun ActiveCaloriesTile(
    data: ActiveCaloriesData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.calories
    MetricTile(
        accent = accent,
        icon = PulseIcons.Fire,
        label = "Active",
        headerBottomPadding = 8,
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProgressRing(
                progress = data.goalFraction,
                color = accent.accent,
                trackColor = accent.border,
                size = 56.dp,
            ) {
                Icon(
                    imageVector = PulseIcons.Fire,
                    contentDescription = null,
                    tint = accent.accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = "%,d".format(data.burnedKcal),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "of %,d kcal".format(data.goalKcal),
                    style = MaterialTheme.typography.bodySmall,
                    color = PulseTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun WaterTile(
    data: WaterData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.water
    MetricTile(
        accent = accent,
        icon = PulseIcons.WaterDrop,
        label = "Water",
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = buildAnnotatedString {
                append("${data.consumedLitres} ")
                withStyle(
                    MaterialTheme.typography.bodyMedium
                        .copy(color = PulseTheme.colors.textMuted)
                        .toSpanStyle(),
                ) {
                    append("/ ${data.goalLitres} L")
                }
            },
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(data.totalSlots) { index ->
                Icon(
                    imageVector = PulseIcons.WaterDrop,
                    contentDescription = null,
                    tint = if (index < data.filledSlots) {
                        accent.accent
                    } else {
                        accent.tintBase.copy(alpha = 0.30f)
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun WeightTile(
    data: WeightData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.weight
    MetricTile(
        accent = accent,
        icon = PulseIcons.Weight,
        label = "Weight",
        headerBottomPadding = 8,
        onClick = onClick,
        headerTrailing = {
            val direction = if (data.weekDeltaKg <= 0) "−" else "+"
            Text(
                text = "$direction%.1f kg this week".format(kotlin.math.abs(data.weekDeltaKg)),
                style = MaterialTheme.typography.labelMedium,
                color = accent.accent,
            )
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    append("%.1f ".format(data.currentKg))
                    withStyle(
                        MaterialTheme.typography.labelLarge
                            .copy(color = PulseTheme.colors.textMuted)
                            .toSpanStyle(),
                    ) {
                        append("kg")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
            )
            Sparkline(
                values = data.series,
                color = accent.accent,
                showAreaFill = true,
                showEndDot = true,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
            )
        }
    }
}

@Composable
private fun DiaryCtaTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PulseTheme.colors.diary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(TileShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.tintBase.copy(alpha = TileGradient.Soft.startAlpha),
                        accent.tintBase.copy(alpha = TileGradient.Soft.endAlpha),
                    ),
                ),
            )
            .border(1.dp, accent.tintBase.copy(alpha = TileGradient.Soft.borderAlpha), TileShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accent.tintBase.copy(alpha = 0.24f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PulseIcons.EditNote,
                contentDescription = null,
                tint = accent.accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Add today to your diary",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Note how you feel to correlate with the data.",
                style = MaterialTheme.typography.bodyMedium,
                color = PulseTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Chevron()
    }
}

@Composable
private fun Chevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector = PulseIcons.ChevronRight,
        contentDescription = null,
        tint = PulseTheme.colors.textMuted,
        modifier = modifier.size(22.dp),
    )
}
