package dev.pulsereport.feature.sources

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.pulsereport.core.model.HealthMetric
import dev.pulsereport.core.ui.component.TileShape
import dev.pulsereport.core.ui.icon.PulseIcons
import dev.pulsereport.core.ui.metric.accent
import dev.pulsereport.core.ui.metric.displayLabel
import dev.pulsereport.core.ui.metric.icon
import dev.pulsereport.core.ui.theme.MetricAccent
import dev.pulsereport.core.ui.theme.PulseTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data object SourcesRoute

fun NavGraphBuilder.sourcesScreen() {
    composable<SourcesRoute> {
        SourcesScreen()
    }
}

/** How long the "Saved" confirmation stays visible after a reorder. */
private const val SAVED_CONFIRMATION_MILLIS = 2_000L

/** Duration of a card's expand/collapse animation. */
private const val EXPAND_COLLAPSE_MILLIS = 200

@Composable
fun SourcesScreen(
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var saveVersion by remember { mutableIntStateOf(0) }
    var showSaved by remember { mutableStateOf(false) }
    LaunchedEffect(saveVersion) {
        if (saveVersion > 0) {
            showSaved = true
            delay(SAVED_CONFIRMATION_MILLIS)
            showSaved = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "sources_header", contentType = "header") {
            SourcesHeader(
                showSaved = showSaved,
                // Sources is still a bottom-nav tab, not pushed onto a back stack, so there's
                // nowhere to go back to yet. We'll wire a real pop once the screen sits under a
                // settings menu.
                onBackClick = {},
            )
        }
        items(
            items = uiState.metrics,
            key = { metricState -> metricState.metric.name },
            contentType = { "metric" },
        ) { metricState ->
            SourceMetricCard(
                metric = metricState.metric,
                sources = metricState.sources,
                onReorder = { orderedPackageNames ->
                    viewModel.onReorder(metricState.metric, orderedPackageNames)
                    saveVersion++
                },
            )
        }
    }
}

@Composable
private fun SourcesHeader(
    showSaved: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = PulseIcons.ChevronLeft,
                    contentDescription = "Back",
                    tint = PulseTheme.colors.textSecondary,
                )
            }
            Text(
                text = "Data priority",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = showSaved, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PulseIcons.CheckCircle,
                        contentDescription = null,
                        tint = PulseTheme.colors.accent,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PulseTheme.colors.accent,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
        Text(
            text = "Choose which source wins when data overlaps.",
            style = MaterialTheme.typography.bodyMedium,
            color = PulseTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun SourceMetricCard(
    metric: HealthMetric,
    sources: List<SourceUiState>,
    onReorder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = metric.accent(PulseTheme.colors)
    var expanded by rememberSaveable(metric.name) { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(EXPAND_COLLAPSE_MILLIS),
        label = "chevronRotation",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(TileShape)
            .background(PulseTheme.colors.card)
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (sources.isNotEmpty()) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = metric.icon(),
                    contentDescription = null,
                    tint = accent.accent,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = metric.displayLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent.accent,
                    modifier = Modifier.padding(start = 8.dp),
                )
                if (sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (sources.size == 1) "1 source" else "${sources.size} sources",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PulseTheme.colors.textSecondary,
                    )
                    Icon(
                        imageVector = PulseIcons.ChevronDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = PulseTheme.colors.textMuted,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(20.dp)
                            .rotate(chevronRotation),
                    )
                }
            }
        }
        if (sources.isEmpty()) {
            Text(
                text = "No apps have written this data yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = PulseTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {
            AnimatedContent(
                targetState = expanded,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(EXPAND_COLLAPSE_MILLIS / 2)) togetherWith
                        fadeOut(animationSpec = tween(EXPAND_COLLAPSE_MILLIS / 2))) using
                        SizeTransform(clip = true) { _, _ -> tween(EXPAND_COLLAPSE_MILLIS) }
                },
                label = "sourceDetails",
            ) {
                if (it) {
                    ReorderableSourceList(
                        sources = sources,
                        accent = accent,
                        onReorder = onReorder,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                } else {
                    Text(
                        text = "Preferred: ${sources.first().label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PulseTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * A short, drag-to-reorder list of [sources] within one metric's card. Dragging a row by its
 * handle reorders the local list live (so the rank badges and the "Preferred" pill preview
 * where things will land); [onReorder] fires once, on release, with the full final order -
 * never on every intermediate swap.
 */
@Composable
private fun ReorderableSourceList(
    sources: List<SourceUiState>,
    accent: MetricAccent,
    onReorder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 2026-07-19: a second drag would move the wrong row. Turned out `items` was being
    // recreated on every `sources` change, so the still-running drag coroutine from the
    // first drag was left holding a stale copy the screen no longer rendered from. Fix is to
    // keep one state object for the composable's whole lifetime and just sync its value -
    // that way every coroutine, old or new, is reading and writing the same live object.
    var items by remember { mutableStateOf(sources) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rowHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(sources) {
        if (draggedIndex == null) {
            items = sources
        }
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, source ->
            key(source.packageName) {
                if (index > 0) {
                    HorizontalDivider(color = PulseTheme.colors.navDivider, thickness = 1.dp)
                }
                val isDragged = draggedIndex == index
                val currentIndex by rememberUpdatedState(index)
                val currentSources by rememberUpdatedState(sources)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size -> rowHeightPx = size.height.toFloat() }
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffset else 0f
                            shadowElevation = if (isDragged) 6f else 0f
                            scaleX = if (isDragged) 1.02f else 1f
                            scaleY = if (isDragged) 1.02f else 1f
                        }
                        .zIndex(if (isDragged) 1f else 0f)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RankBadge(rank = index + 1)
                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .weight(1f),
                    ) {
                        Text(text = source.label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = source.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = PulseTheme.colors.textMuted,
                        )
                    }
                    if (index == 0) {
                        PreferredPill(accent = accent, modifier = Modifier.padding(end = 8.dp))
                    }
                    if (items.size > 1) {
                        Icon(
                            imageVector = PulseIcons.DragHandle,
                            contentDescription = "Drag to reorder ${source.label}",
                            tint = PulseTheme.colors.textMuted,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                                .pointerInput(source.packageName) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggedIndex = currentIndex
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, delta ->
                                            change.consume()
                                            val activeIndex = draggedIndex ?: return@detectDragGestures
                                            dragOffset += delta.y
                                            if (rowHeightPx <= 0f) return@detectDragGestures

                                            val moveBy = (dragOffset / rowHeightPx).roundToInt()
                                            if (moveBy == 0) return@detectDragGestures
                                            val targetIndex = (activeIndex + moveBy).coerceIn(0, items.lastIndex)
                                            if (targetIndex == activeIndex) return@detectDragGestures

                                            items = items.toMutableList()
                                                .apply { add(targetIndex, removeAt(activeIndex)) }
                                            dragOffset -= moveBy * rowHeightPx
                                            draggedIndex = targetIndex
                                        },
                                        onDragEnd = {
                                            draggedIndex = null
                                            dragOffset = 0f
                                            if (items.map { it.packageName } != currentSources.map { it.packageName }) {
                                                onReorder(items.map { it.packageName })
                                            }
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                            dragOffset = 0f
                                            items = currentSources
                                        },
                                    )
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = PulseTheme.colors.textMuted,
        )
    }
}

@Composable
private fun PreferredPill(accent: MetricAccent, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, accent.accent, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text = "Preferred",
            style = MaterialTheme.typography.labelMedium,
            color = accent.accent,
        )
    }
}
