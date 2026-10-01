package com.fitifinance.comrade.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.ThemeMode

private val CampusScheme = lightColorScheme(
    primary = CampusPrimary,
    primaryContainer = CampusPrimaryContainer,
    secondary = CampusSecondary,
    tertiary = CampusAccentGold,
    background = CampusBackground,
    surface = CampusSurface,
    surfaceVariant = CampusSurfaceVariant,
    onPrimary = CampusOnPrimary,
    onSurface = CampusOnSurface,
    error = WarningRed
)

private val MidnightScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color.White,
    primaryContainer = MidnightPrimaryContainer,
    onPrimaryContainer = MidnightOnSurface,
    secondary = MidnightSecondary,
    onSecondary = MidnightBackground,
    tertiary = MidnightTertiary,
    background = MidnightBackground,
    onBackground = MidnightOnSurface,
    surface = MidnightSurface,
    onSurface = MidnightOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightOnSurfaceMuted,
    outline = MidnightOutline,
    error = MidnightError
)

private val BarScheme = darkColorScheme(
    primary = BarPrimary,
    primaryContainer = BarPrimaryContainer,
    secondary = BarSecondary,
    background = BarBackground,
    surface = BarSurface,
    surfaceVariant = BarSurfaceVariant,
    onPrimary = BarOnPrimary,
    onSurface = BarOnSurface,
    error = BarAccentCoral
)

private val KibandaScheme = lightColorScheme(
    primary = KibandaPrimary,
    primaryContainer = KibandaPrimaryContainer,
    secondary = KibandaSecondary,
    background = KibandaBackground,
    surface = KibandaSurface,
    surfaceVariant = KibandaSurfaceVariant,
    onPrimary = KibandaOnPrimary,
    onSurface = KibandaOnSurface,
    error = WarningRed
)

/** Consistent, generous rounding used across every card, sheet, and button in the app. */
val FitiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Wraps content in the color scheme matching the current Location-Aware
 * Context Engine mode: Campus (refined navy, light), Bar (AMOLED dark,
 * emerald fintech accent), or Kibanda (warm amber/green, light).
 */
@Composable
fun FitiFinanceTheme(themeMode: ThemeMode = ThemeMode.MIDNIGHT, content: @Composable () -> Unit) {
    val colorScheme = when (themeMode) {
        ThemeMode.MIDNIGHT -> MidnightScheme
        ThemeMode.CAMPUS -> CampusScheme
        ThemeMode.BAR -> BarScheme
        ThemeMode.KIBANDA -> KibandaScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitiTypography,
        shapes = FitiShapes,
        content = content
    )
}
