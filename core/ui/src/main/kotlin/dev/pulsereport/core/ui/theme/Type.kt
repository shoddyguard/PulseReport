package dev.pulsereport.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Tabular numerals, used wherever the mockup sets font-variant-numeric. */
const val TABULAR_NUMBERS = "tnum"

private fun style(
    size: Int,
    weight: FontWeight,
    lineHeight: Float = 1.3f,
    letterSpacing: Float = 0f,
    tabularNumbers: Boolean = false,
) = TextStyle(
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = (size * lineHeight).sp,
    letterSpacing = letterSpacing.em,
    fontFeatureSettings = if (tabularNumbers) TABULAR_NUMBERS else null,
)

// Mockup px sizes map one to one onto sp at its 380px phone frame.
val PulseTypography = Typography(
    // Steps hero number
    displaySmall = style(30, FontWeight.W800, lineHeight = 1f, tabularNumbers = true),
    // Weight value
    headlineMedium = style(28, FontWeight.W800, lineHeight = 1f, tabularNumbers = true),
    // Sleep and heart values
    headlineSmall = style(24, FontWeight.W800, lineHeight = 1f, tabularNumbers = true),
    // Screen title ("Today")
    titleLarge = style(23, FontWeight.W800, letterSpacing = -0.02f),
    // Card titles ("Pulse score")
    titleMedium = style(15, FontWeight.W800),
    // Tile labels ("Steps", "Sleep")
    titleSmall = style(13, FontWeight.W700),
    // Body copy
    bodyLarge = style(15, FontWeight.W500, lineHeight = 1.5f),
    bodyMedium = style(12, FontWeight.W600),
    bodySmall = style(11, FontWeight.W600),
    // Chips and small emphasised text
    labelLarge = style(13, FontWeight.W700),
    labelMedium = style(11, FontWeight.W700),
    // Nav labels
    labelSmall = style(10, FontWeight.W700),
)
