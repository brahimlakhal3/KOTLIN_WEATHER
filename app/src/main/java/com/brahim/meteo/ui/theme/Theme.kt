package com.brahim.meteo.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.brahim.meteo.util.LocalUnitSystem
import com.brahim.meteo.util.UnitSystem

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

private class ThemePalette(
    val canvas: Color,
    val card: Color,
    val container: Color,
    val hero: Color,
    val accent: Color,
    val text: Color,
    val variant: Color,
    val outline: Color,
    val outlineVariant: Color,
)

private val VeryLightPalette =
    ThemePalette(
        canvas = Color(0xFFF9F9F9),
        card = Color(0xFFFAFAFA),
        container = Color(0xFFF0F0F0),
        hero = Color(0xFFE8E8E8),
        accent = Color(0xFF161616),
        text = Color(0xFF1A1A1A),
        variant = Color(0xFF565656),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFFE5E5E5),
    )

private val LightPalette =
    ThemePalette(
        canvas = Color(0xFFF2F2F2),
        card = Color(0xFFFAFAFA),
        container = Color(0xFFEAEAEA),
        hero = Color(0xFFE0E0E0),
        accent = Color(0xFF1F1F1F),
        text = Color(0xFF1F1F1F),
        variant = Color(0xFF4A4A4A),
        outline = Color(0xFF767676),
        outlineVariant = Color(0xFFD8D8D8),
    )

private val SoftPalette =
    ThemePalette(
        canvas = Color(0xFFE8E8E8),
        card = Color(0xFFF7F7F7),
        container = Color(0xFFE0E0E0),
        hero = Color(0xFFD6D6D6),
        accent = Color(0xFF161616),
        text = Color(0xFF191919),
        variant = Color(0xFF454545),
        outline = Color(0xFF6E6E6E),
        outlineVariant = Color(0xFFCFCFCF),
    )

private val DarkPalette =
    ThemePalette(
        canvas = Color(0xFF1A1A1A),
        card = Color(0xFF232323),
        container = Color(0xFF2B2B2B),
        hero = Color(0xFF333333),
        accent = Color(0xFFE4E4E4),
        text = Color(0xFFEAEAEA),
        variant = Color(0xFFB4B4B4),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFF3D3D3D),
    )

private val VeryDarkPalette =
    ThemePalette(
        canvas = Color(0xFF0B0B0B),
        card = Color(0xFF141414),
        container = Color(0xFF1C1C1C),
        hero = Color(0xFF262626),
        accent = Color(0xFFF5F5F5),
        text = Color(0xFFF2F2F2),
        variant = Color(0xFFA8A8A8),
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

private fun ThemePalette.scheme(): ColorScheme =
    lightColorScheme(
        primary = accent,
        onPrimary = card,
        primaryContainer = hero,
        onPrimaryContainer = text,
        inversePrimary = variant,
        secondary = variant,
        onSecondary = card,
        secondaryContainer = container,
        onSecondaryContainer = text,
        tertiary = variant,
        onTertiary = card,
        tertiaryContainer = hero,
        onTertiaryContainer = text,
        error = text,
        onError = card,
        errorContainer = container,
        onErrorContainer = text,
        background = canvas,
        onBackground = text,
        surface = canvas,
        onSurface = text,
        surfaceVariant = container,
        onSurfaceVariant = variant,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = accent,
        inverseSurface = text,
        inverseOnSurface = card,
        surfaceDim = canvas,
        surfaceBright = card,
        surfaceContainerLowest = card,
        surfaceContainerLow = card,
        surfaceContainer = container,
        surfaceContainerHigh = hero,
        surfaceContainerHighest = hero,
    )

@Composable
private fun animatedColorScheme(palette: ThemePalette): ColorScheme {
  val base = palette.scheme()
  val spec = Motion.standard<Color>(Motion.MEDIUM_2)

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
      error = animate(base.error),
      onError = animate(base.onError),
      errorContainer = animate(base.errorContainer),
      onErrorContainer = animate(base.onErrorContainer),
      background = animate(base.background),
      onBackground = animate(base.onBackground),
      surface = animate(base.surface),
      onSurface = animate(base.onSurface),
      surfaceVariant = animate(base.surfaceVariant),
      onSurfaceVariant = animate(base.onSurfaceVariant),
      outline = animate(base.outline),
      outlineVariant = animate(base.outlineVariant),
      inverseSurface = animate(base.inverseSurface),
      inverseOnSurface = animate(base.inverseOnSurface),
      surfaceDim = animate(base.surfaceDim),
      surfaceBright = animate(base.surfaceBright),
      surfaceContainerLowest = animate(base.surfaceContainerLowest),
      surfaceContainerLow = animate(base.surfaceContainerLow),
      surfaceContainer = animate(base.surfaceContainer),
      surfaceContainerHigh = animate(base.surfaceContainerHigh),
      surfaceContainerHighest = animate(base.surfaceContainerHighest),
  )
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    textSize: TextSize = TextSize.NORMAL,
    units: UnitSystem = UnitSystem.METRIC,
    content: @Composable () -> Unit,
) {
  val palette = themeMode.palette()
  val colors = animatedColorScheme(palette)
  val density = LocalDensity.current

  CompositionLocalProvider(
      LocalUnitSystem provides units,
      LocalDensity provides Density(density.density, density.fontScale * textSize.scale),
  ) {
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
  }
}
