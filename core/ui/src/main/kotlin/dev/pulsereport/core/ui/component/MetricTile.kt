package dev.pulsereport.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.MetricAccent
import dev.pulsereport.core.ui.theme.TileAlpha

/** Corner radius shared by all cards and tiles in the design. */
val TileShape = RoundedCornerShape(22.dp)

@Composable
fun MetricTile(
    accent: MetricAccent,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    gradient: TileGradient? = null,
    headerTrailing: @Composable (() -> Unit)? = null,
    headerBottomPadding: Int = 10,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val background = when (gradient) {
        null -> Modifier.background(accent.tint)
        else -> Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    accent.tintBase.copy(alpha = gradient.startAlpha),
                    accent.tintBase.copy(alpha = gradient.endAlpha),
                ),
            ),
        )
    }
    val borderColor = when (gradient) {
        null -> accent.border
        else -> accent.tintBase.copy(alpha = gradient.borderAlpha)
    }

    Column(
        modifier = modifier
            .clip(TileShape)
            .then(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(1.dp, borderColor, TileShape)
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent.accent,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = accent.accent,
                modifier = Modifier.padding(start = 8.dp),
            )
            if (headerTrailing != null) {
                Spacer(modifier = Modifier.weight(1f))
                headerTrailing()
            }
        }
        Spacer(modifier = Modifier.height(headerBottomPadding.dp))
        content()
    }
}

/** Gradient strengths used by the steps and diary tiles. */
data class TileGradient(
    val startAlpha: Float,
    val endAlpha: Float,
    val borderAlpha: Float,
) {
    companion object {
        val Strong = TileGradient(
            startAlpha = TileAlpha.GRADIENT_STRONG_START,
            endAlpha = TileAlpha.GRADIENT_STRONG_END,
            borderAlpha = TileAlpha.GRADIENT_STRONG_BORDER,
        )
        val Soft = TileGradient(
            startAlpha = TileAlpha.GRADIENT_SOFT_START,
            endAlpha = TileAlpha.GRADIENT_SOFT_END,
            borderAlpha = TileAlpha.GRADIENT_SOFT_BORDER,
        )
    }
}
