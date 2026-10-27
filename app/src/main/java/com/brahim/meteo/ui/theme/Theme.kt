package com.brahim.meteo.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.brahim.meteo.util.LocalUnits
import com.brahim.meteo.util.UnitPreferences

enum class ThemeMode {
  AUTO,
  VERY_LIGHT,
  LIGHT,
  SOFT,
  DARK,
  VERY_DARK;

  companion object {
    fun from(value: String?): ThemeMode =
        entries.firstOrNull { entry -> entry.name == value } ?: AUTO
  }
}

enum class TextSize(val scale: Float) {
  SMALL(0.9f),
  NORMAL(1f),
  LARGE(1.15f);

  companion object {
    fun from(value: String?): TextSize =
        entries.firstOrNull { entry -> entry.name == value } ?: NORMAL
  }
}

val LocalAnimationsEnabled = staticCompositionLocalOf { true }

@Composable
fun ThemeMode.isDark(): Boolean =
    when (this) {
      ThemeMode.VERY_LIGHT,
      ThemeMode.LIGHT,
      ThemeMode.SOFT -> false
      ThemeMode.DARK,
      ThemeMode.VERY_DARK -> true
      ThemeMode.AUTO -> isSystemInDarkTheme()
    }

/**
 * Neutral backgrounds. They deliberately never receive an accent tint: the user asked for the
 * background to stay gray in every theme.
 */
private class ThemePalette(
    val canvas: Color,
    val card: Color,
    val container: Color,
    val hero: Color,
    val text: Color,
    val variant: Color,
    val outline: Color,
    val outlineVariant: Color,
)

private val VeryLightPalette =
    ThemePalette(
        canvas = Color(0xFFF7F7F7),
        card = Color(0xFFFAFAFA),
        container = Color(0xFFEEEEEE),
        hero = Color(0xFFE4E4E4),
        text = Color(0xFF191919),
        variant = Color(0xFF565656),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFFE2E2E2),
    )

private val LightPalette =
    ThemePalette(
        canvas = Color(0xFFF1F1F1),
        card = Color(0xFFF9F9F9),
        container = Color(0xFFE9E9E9),
        hero = Color(0xFFDEDEDE),
        text = Color(0xFF1D1D1D),
        variant = Color(0xFF4A4A4A),
        outline = Color(0xFF767676),
        outlineVariant = Color(0xFFD6D6D6),
    )

private val SoftPalette =
    ThemePalette(
        canvas = Color(0xFFE8E8E8),
        card = Color(0xFFF6F6F6),
        container = Color(0xFFDFDFDF),
        hero = Color(0xFFD3D3D3),
        text = Color(0xFF181818),
        variant = Color(0xFF454545),
        outline = Color(0xFF6E6E6E),
        outlineVariant = Color(0xFFCFCFCF),
    )

private val DarkPalette =
    ThemePalette(
        canvas = Color(0xFF1A1A1A),
        card = Color(0xFF232323),
        container = Color(0xFF2B2B2B),
        hero = Color(0xFF343434),
        text = Color(0xFFEAEAEA),
        variant = Color(0xFFB6B6B6),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFF3D3D3D),
    )

private val VeryDarkPalette =
    ThemePalette(
        canvas = Color(0xFF0B0B0B),
        card = Color(0xFF141414),
        container = Color(0xFF1C1C1C),
        hero = Color(0xFF262626),
        text = Color(0xFFF3F3F3),
        variant = Color(0xFFAAAAAA),
        outline = Color(0xFF7A7A7A),
        outlineVariant = Color(0xFF2A2A2A),
    )

@Composable
private fun ThemeMode.palette(): ThemePalette =
    when (this) {
      ThemeMode.VERY_LIGHT -> VeryLightPalette
      ThemeMode.LIGHT -> LightPalette
      ThemeMode.SOFT -> SoftPalette
      ThemeMode.DARK -> DarkPalette
      ThemeMode.VERY_DARK -> VeryDarkPalette
      ThemeMode.AUTO -> if (isSystemInDarkTheme()) DarkPalette else LightPalette
    }

private fun ThemePalette.scheme(accent: AccentScheme, dark: Boolean): ColorScheme {
  val error = if (dark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
  val onError = if (dark) Color(0xFF690005) else Color.White
  val errorContainer = if (dark) Color(0xFF93000A) else Color(0xFFFFDAD6)
  val onErrorContainer = if (dark) Color(0xFFFFDAD6) else Color(0xFF410002)
  val inverseSurface = if (dark) text else Color(0xFF303030)

  return lightColorScheme(
      primary = accent.primary,
      onPrimary = accent.onPrimary,
      primaryContainer = accent.primaryContainer,
      onPrimaryContainer = accent.onPrimaryContainer,
      inversePrimary = accent.inversePrimary,
      secondary = accent.secondary,
      onSecondary = accent.onSecondary,
      secondaryContainer = accent.secondaryContainer,
      onSecondaryContainer = accent.onSecondaryContainer,
      tertiary = accent.tertiary,
      onTertiary = accent.onTertiary,
      tertiaryContainer = accent.tertiaryContainer,
      onTertiaryContainer = accent.onTertiaryContainer,
      background = canvas,
      onBackground = text,
      surface = canvas,
      onSurface = text,
      surfaceVariant = container,
      onSurfaceVariant = variant,
      surfaceTint = accent.primary,
      inverseSurface = inverseSurface,
      inverseOnSurface = if (dark) Color(0xFF1A1A1A) else Color(0xFFF2F2F2),
      error = error,
      onError = onError,
      errorContainer = errorContainer,
      onErrorContainer = onErrorContainer,
      outline = outline,
      outlineVariant = outlineVariant,
      scrim = Color.Black,
      surfaceBright = card,
      surfaceContainer = container,
      surfaceContainerHigh = hero,
      surfaceContainerHighest = hero,
      surfaceContainerLow = card,
      surfaceContainerLowest = card,
      surfaceDim = canvas,
      primaryFixed = accent.primaryFixed,
      primaryFixedDim = accent.primaryFixedDim,
      onPrimaryFixed = accent.onPrimaryFixed,
      onPrimaryFixedVariant = accent.onPrimaryFixedVariant,
      secondaryFixed = accent.secondaryFixed,
      secondaryFixedDim = accent.secondaryFixedDim,
      onSecondaryFixed = accent.onSecondaryFixed,
      onSecondaryFixedVariant = accent.onSecondaryFixedVariant,
      tertiaryFixed = accent.tertiaryFixed,
      tertiaryFixedDim = accent.tertiaryFixedDim,
      onTertiaryFixed = accent.onTertiaryFixed,
      onTertiaryFixedVariant = accent.onTertiaryFixedVariant,
  )
}

@Composable
private fun animatedColorScheme(
    palette: ThemePalette,
    accent: AccentScheme,
    dark: Boolean,
    animationsEnabled: Boolean,
): ColorScheme {
  val base = palette.scheme(accent, dark)
  val spec =
      if (animationsEnabled) Motion.standard<Color>(Motion.MEDIUM_2)
      else snap<Color>()

  @Composable
  fun animate(color: Color): Color =
      animateColorAsState(targetValue = color, animationSpec = spec, label = "scheme").value

  return base.copy(
      primary = animate(base.primary),
      onPrimary = animate(base.onPrimary),
      primaryContainer = animate(base.primaryContainer),
      onPrimaryContainer = animate(base.onPrimaryContainer),
      inversePrimary = animate(base.inversePrimary),
      secondary = animate(base.secondary),
      onSecondary = animate(base.onSecondary),
      secondaryContainer = animate(base.secondaryContainer),
      onSecondaryContainer = animate(base.onSecondaryContainer),
      tertiary = animate(base.tertiary),
      onTertiary = animate(base.onTertiary),
      tertiaryContainer = animate(base.tertiaryContainer),
      onTertiaryContainer = animate(base.onTertiaryContainer),
      background = animate(base.background),
      onBackground = animate(base.onBackground),
      surface = animate(base.surface),
      onSurface = animate(base.onSurface),
      surfaceVariant = animate(base.surfaceVariant),
      onSurfaceVariant = animate(base.onSurfaceVariant),
      surfaceTint = animate(base.surfaceTint),
      inverseSurface = animate(base.inverseSurface),
      inverseOnSurface = animate(base.inverseOnSurface),
      error = animate(base.error),
      onError = animate(base.onError),
      errorContainer = animate(base.errorContainer),
      onErrorContainer = animate(base.onErrorContainer),
      outline = animate(base.outline),
      outlineVariant = animate(base.outlineVariant),
      surfaceBright = animate(base.surfaceBright),
      surfaceContainer = animate(base.surfaceContainer),
      surfaceContainerHigh = animate(base.surfaceContainerHigh),
      surfaceContainerHighest = animate(base.surfaceContainerHighest),
      surfaceContainerLow = animate(base.surfaceContainerLow),
      surfaceContainerLowest = animate(base.surfaceContainerLowest),
      surfaceDim = animate(base.surfaceDim),
  )
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    accentColor: AccentColor = AccentColor.OCEAN,
    cornerStyle: CornerStyle = CornerStyle.EXPRESSIVE,
    textSize: TextSize = TextSize.NORMAL,
    animationsEnabled: Boolean = true,
    units: UnitPreferences = UnitPreferences(),
    content: @Composable () -> Unit,
) {
  val dark = themeMode.isDark()
  val palette = themeMode.palette()
  val accent = accentColor.scheme(dark)
  val colors = animatedColorScheme(palette, accent, dark, animationsEnabled)
  val density = LocalDensity.current

  CompositionLocalProvider(
      LocalUnits provides units,
      LocalAnimationsEnabled provides animationsEnabled,
      LocalDensity provides Density(density.density, density.fontScale * textSize.scale),
  ) {
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme =
            if (animationsEnabled) MotionScheme.expressive() else MotionScheme.standard(),
        typography = AppTypography,
        shapes = cornerStyle.shapes,
        content = content,
    )
  }
}
