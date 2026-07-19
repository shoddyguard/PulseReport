package dev.pulsereport.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkBackground,
    surfaceContainer = DarkCard,
    surfaceVariant = DarkCard,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkTextMuted,
    primary = DarkAccent,
    onPrimary = OnAccent,
    secondaryContainer = DarkCard,
    onSecondaryContainer = DarkText,
)

private val LightScheme = lightColorScheme(
    background = LightBackground,
    surface = LightBackground,
    surfaceContainer = LightCard,
    surfaceVariant = LightCard,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = LightTextSecondary,
    outline = LightTextMuted,
    primary = LightAccent,
    onPrimary = OnAccent,
    secondaryContainer = LightCard,
    onSecondaryContainer = LightText,
)

/**
 * PulseReport theme: fixed brand palette from the design handoff (not dynamic
 * color), Manrope typography, plus the [PulseTheme] extension palette.
 */
@Composable
fun PulseReportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val pulseColors = if (darkTheme) DarkPulseColors else LightPulseColors

    CompositionLocalProvider(LocalPulseColors provides pulseColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = PulseTypography,
            content = content,
        )
    }
}
