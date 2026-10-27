package com.brahim.meteo.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Accent palettes used by the app. Backgrounds always stay neutral gray; only these accents and
 * their tonal containers change.
 */
enum class AccentColor(val base: Color, val alt: Color) {
  OCEAN(Color(0xFF0077B6), Color(0xFF00B4D8)),
  SKY(Color(0xFF0288D1), Color(0xFF4FC3F7)),
  CYAN(Color(0xFF00838F), Color(0xFF4DD0E1)),
  TEAL(Color(0xFF00695C), Color(0xFF4DB6AC)),
  MINT(Color(0xFF00897B), Color(0xFF69F0AE)),
  EMERALD(Color(0xFF2E7D32), Color(0xFF81C784)),
  LIME(Color(0xFF558B2F), Color(0xFFC6FF00)),
  SUN(Color(0xFFF9A825), Color(0xFFFFD54F)),
  AMBER(Color(0xFFEF6C00), Color(0xFFFFB74D)),
  CORAL(Color(0xFFD84315), Color(0xFFFF8A65)),
  ROSE(Color(0xFFC2185B), Color(0xFFF06292)),
  BERRY(Color(0xFF7B1FA2), Color(0xFFBA68C8)),
  VIOLET(Color(0xFF512DA8), Color(0xFFB39DDB)),
  INDIGO(Color(0xFF303F9F), Color(0xFF7986CB)),
  PEACH(Color(0xFFBF360C), Color(0xFFFFAB91)),
  GRAPHITE(Color(0xFF424242), Color(0xFF9E9E9E));

  companion object {
    fun from(value: String?): AccentColor =
        entries.firstOrNull { entry -> entry.name == value } ?: OCEAN
  }
}

data class AccentScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val inversePrimary: Color,
    val primaryFixed: Color,
    val primaryFixedDim: Color,
    val onPrimaryFixed: Color,
    val onPrimaryFixedVariant: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val onSecondaryFixedVariant: Color,
    val tertiaryFixed: Color,
    val tertiaryFixedDim: Color,
    val onTertiaryFixed: Color,
    val onTertiaryFixedVariant: Color,
)

private val darkInk = Color(0xFF141414)
private val lightInk = Color(0xFFFAFAFA)

private fun contrastRatio(first: Color, second: Color): Float {
  val lighter = maxOf(first.luminance(), second.luminance())
  val darker = minOf(first.luminance(), second.luminance())
  return (lighter + 0.05f) / (darker + 0.05f)
}

private fun onColor(color: Color): Color =
    if (contrastRatio(darkInk, color) >= contrastRatio(lightInk, color)) darkInk else lightInk

private fun blend(first: Color, second: Color, amount: Float): Color =
    lerp(first, second, amount.coerceIn(0f, 1f))

fun AccentColor.scheme(dark: Boolean): AccentScheme {
  val mixed = blend(base, alt, 0.5f)
  val primary = if (dark) blend(base, Color.White, 0.42f) else blend(base, Color.Black, 0.26f)
  val secondary = if (dark) blend(alt, Color.White, 0.44f) else blend(alt, Color.Black, 0.30f)
  val tertiary = if (dark) blend(mixed, Color.White, 0.40f) else blend(mixed, Color.Black, 0.32f)
  val primaryContainer =
      if (dark) blend(base, Color.Black, 0.52f) else blend(base, Color.White, 0.80f)
  val secondaryContainer =
      if (dark) blend(alt, Color.Black, 0.54f) else blend(alt, Color.White, 0.80f)
  val tertiaryContainer =
      if (dark) blend(mixed, Color.Black, 0.56f) else blend(mixed, Color.White, 0.80f)
  val onPrimaryContainer =
      if (dark) blend(base, Color.White, 0.82f) else blend(base, Color.Black, 0.62f)
  val onSecondaryContainer =
      if (dark) blend(alt, Color.White, 0.84f) else blend(alt, Color.Black, 0.64f)
  val onTertiaryContainer =
      if (dark) blend(mixed, Color.White, 0.84f) else blend(mixed, Color.Black, 0.64f)
  val fixed = blend(base, Color.White, 0.78f)
  val fixedDim = blend(base, Color.White, 0.58f)
  val fixedInk = blend(base, Color.Black, 0.64f)
  val secondaryFixed = blend(alt, Color.White, 0.78f)
  val secondaryFixedDim = blend(alt, Color.White, 0.58f)
  val secondaryFixedInk = blend(alt, Color.Black, 0.64f)
  val tertiaryFixed = blend(mixed, Color.White, 0.78f)
  val tertiaryFixedDim = blend(mixed, Color.White, 0.58f)
  val tertiaryFixedInk = blend(mixed, Color.Black, 0.64f)

  return AccentScheme(
      primary = primary,
      onPrimary = onColor(primary),
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      secondary = secondary,
      onSecondary = onColor(secondary),
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = onSecondaryContainer,
      tertiary = tertiary,
      onTertiary = onColor(tertiary),
      tertiaryContainer = tertiaryContainer,
      onTertiaryContainer = onTertiaryContainer,
      inversePrimary = blend(base, Color.White, 0.58f),
      primaryFixed = fixed,
      primaryFixedDim = fixedDim,
      onPrimaryFixed = fixedInk,
      onPrimaryFixedVariant = fixedInk,
      secondaryFixed = secondaryFixed,
      secondaryFixedDim = secondaryFixedDim,
      onSecondaryFixed = secondaryFixedInk,
      onSecondaryFixedVariant = secondaryFixedInk,
      tertiaryFixed = tertiaryFixed,
      tertiaryFixedDim = tertiaryFixedDim,
      onTertiaryFixed = tertiaryFixedInk,
      onTertiaryFixedVariant = tertiaryFixedInk,
  )
}
