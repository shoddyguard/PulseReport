package dev.pulsereport.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Alpha constants for the tinted metric tiles, kept together for tuning. */
object TileAlpha {
    const val TINT = 0.13f
    const val BORDER = 0.22f
    const val GRADIENT_STRONG_START = 0.30f
    const val GRADIENT_STRONG_END = 0.12f
    const val GRADIENT_STRONG_BORDER = 0.38f
    const val GRADIENT_SOFT_START = 0.26f
    const val GRADIENT_SOFT_END = 0.09f
    const val GRADIENT_SOFT_BORDER = 0.34f
}

@Immutable
data class MetricAccent(
    val accent: Color,
    val tintBase: Color,
) {
    val tint: Color get() = tintBase.copy(alpha = TileAlpha.TINT)
    val border: Color get() = tintBase.copy(alpha = TileAlpha.BORDER)
}

/** Brand colors that have no natural slot in the material3 ColorScheme. */
@Immutable
data class PulseColors(
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val card: Color,
    val avatarBackground: Color,
    val navDivider: Color,
    val ringTrack: Color,
    val steps: MetricAccent,
    val sleep: MetricAccent,
    val heart: MetricAccent,
    val calories: MetricAccent,
    val water: MetricAccent,
    val weight: MetricAccent,
    val diary: MetricAccent,
    val sleepStageDeep: Color,
    val sleepStageCore: Color,
    val sleepStageRem: Color,
    val sleepStageAwake: Color,
)

val DarkPulseColors = PulseColors(
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    accent = DarkAccent,
    onAccent = OnAccent,
    card = DarkCard,
    avatarBackground = Color.White.copy(alpha = 0.09f),
    navDivider = Color.White.copy(alpha = 0.07f),
    ringTrack = Color.White.copy(alpha = 0.08f),
    steps = MetricAccent(accent = Teal400, tintBase = Teal400),
    sleep = MetricAccent(accent = Purple400, tintBase = Purple400),
    heart = MetricAccent(accent = Rose400, tintBase = Rose400),
    calories = MetricAccent(accent = Orange400, tintBase = Orange400),
    water = MetricAccent(accent = Sky400, tintBase = Sky400),
    weight = MetricAccent(accent = Blue400, tintBase = Blue400),
    diary = MetricAccent(accent = Amber400, tintBase = Amber400),
    sleepStageDeep = SleepStageDeepDark,
    sleepStageCore = Purple400,
    sleepStageRem = SleepStageRemDark,
    sleepStageAwake = SleepStageAwakeDark,
)

val LightPulseColors = PulseColors(
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    accent = LightAccent,
    onAccent = OnAccent,
    card = LightCard,
    avatarBackground = Color.Black.copy(alpha = 0.06f),
    navDivider = Color.Black.copy(alpha = 0.08f),
    ringTrack = Color.Black.copy(alpha = 0.07f),
    steps = MetricAccent(accent = Teal600, tintBase = Teal400),
    sleep = MetricAccent(accent = Purple600, tintBase = Purple400),
    heart = MetricAccent(accent = Rose600, tintBase = Rose400),
    calories = MetricAccent(accent = Orange600, tintBase = Orange400),
    water = MetricAccent(accent = Sky600, tintBase = Sky400),
    weight = MetricAccent(accent = Blue600, tintBase = Blue400),
    diary = MetricAccent(accent = Amber600, tintBase = Amber400),
    sleepStageDeep = SleepStageDeepLight,
    sleepStageCore = Purple600,
    sleepStageRem = SleepStageRemLight,
    sleepStageAwake = SleepStageAwakeLight,
)

val LocalPulseColors = staticCompositionLocalOf { DarkPulseColors }

/** Accessor for the PulseReport extension palette, e.g. `PulseTheme.colors.accent`. */
object PulseTheme {
    val colors: PulseColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPulseColors.current
}
