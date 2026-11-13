package com.brahim.meteo.ui.theme

import androidx.compose.ui.graphics.Color
import com.brahim.meteo.util.WeatherCondition
import com.brahim.meteo.util.weatherCondition

/** The ambient motion drawn on top of the sky gradient. */
enum class SkyParticle { NONE, CLOUDS, RAIN, SNOW, STARS, FOG, STORM }

/** The luminous body that may sit behind the forecast header. */
enum class Celestial { NONE, SUN, MOON }

/**
 * The colour story of the sky for a given condition. Google Pixel style weather apps tie the whole
 * page to the current sky, so the palette is resolved from the weather code instead of the theme.
 */
data class SkyPalette(
    val colors: List<Color>,
    val particle: SkyParticle,
    val celestial: Celestial,
    val particleColor: Color,
)

private val ClearDay = listOf(Color(0xFF0B5FBF), Color(0xFF2C7BE5), Color(0xFF63A8F0), Color(0xFFA8D4F5))
private val ClearNight = listOf(Color(0xFF05070F), Color(0xFF0D1730), Color(0xFF16264A), Color(0xFF22385F))
private val PartlyDay = listOf(Color(0xFF1E63B8), Color(0xFF3D8AD6), Color(0xFF7FB6E8), Color(0xFFBFD9F0))
private val PartlyNight = listOf(Color(0xFF070B18), Color(0xFF111C36), Color(0xFF1D2E4E), Color(0xFF2C4166))
private val Overcast = listOf(Color(0xFF44525F), Color(0xFF5E6E7C), Color(0xFF8393A1), Color(0xFFAEBBC6))
private val Foggy = listOf(Color(0xFF5A6670), Color(0xFF79858E), Color(0xFF9EA8AF), Color(0xFFC5CCD1))
private val Rainy = listOf(Color(0xFF152A3D), Color(0xFF1F3C55), Color(0xFF2C5473), Color(0xFF3E6C90))
private val Snowy = listOf(Color(0xFF45556B), Color(0xFF62738A), Color(0xFF8B9AAF), Color(0xFFB6C3D0))
private val Stormy = listOf(Color(0xFF0D1020), Color(0xFF191C33), Color(0xFF272347), Color(0xFF3A2F5C))

/**
 * Resolves the sky palette for the given [weatherCode] and [isDay] flag.
 *
 * @param weatherCode the WMO weather interpretation code reported by Open-Meteo.
 * @param isDay whether the location is currently in daylight.
 */
fun skyPalette(weatherCode: Int?, isDay: Boolean): SkyPalette {
  val condition = weatherCondition(weatherCode)
  val colors: List<Color>
  val particle: SkyParticle
  val celestial: Celestial
  val particleColor: Color

  when (condition) {
    WeatherCondition.CLEAR,
    WeatherCondition.MAINLY_CLEAR -> {
      colors = if (isDay) ClearDay else ClearNight
      particle = if (isDay) SkyParticle.NONE else SkyParticle.STARS
      celestial = if (isDay) Celestial.SUN else Celestial.MOON
      particleColor = if (isDay) Color(0xFFFFF3C4) else Color(0xFFDDE7FF)
    }
    WeatherCondition.PARTLY_CLOUDY -> {
      colors = if (isDay) PartlyDay else PartlyNight
      particle = SkyParticle.CLOUDS
      celestial = if (isDay) Celestial.SUN else Celestial.MOON
      particleColor = Color(0xFFF3F8FF)
    }
    WeatherCondition.OVERCAST -> {
      colors = Overcast
      particle = SkyParticle.CLOUDS
      celestial = Celestial.NONE
      particleColor = Color(0xFFE8EEF3)
    }
    WeatherCondition.FOG -> {
      colors = Foggy
      particle = SkyParticle.FOG
      celestial = Celestial.NONE
      particleColor = Color(0xFFEFF3F5)
    }
    WeatherCondition.DRIZZLE,
    WeatherCondition.FREEZING_DRIZZLE -> {
      colors = Rainy
      particle = SkyParticle.RAIN
      celestial = Celestial.NONE
      particleColor = Color(0xFF9FC4DE)
    }
    WeatherCondition.RAIN,
    WeatherCondition.FREEZING_RAIN,
    WeatherCondition.RAIN_SHOWERS -> {
      colors = Rainy
      particle = SkyParticle.RAIN
      celestial = Celestial.NONE
      particleColor = Color(0xFFB7D6EC)
    }
    WeatherCondition.SNOW,
    WeatherCondition.SNOW_SHOWERS -> {
      colors = Snowy
      particle = SkyParticle.SNOW
      celestial = Celestial.NONE
      particleColor = Color(0xFFF2F7FB)
    }
    WeatherCondition.THUNDERSTORM,
    WeatherCondition.THUNDERSTORM_HAIL -> {
      colors = Stormy
      particle = SkyParticle.STORM
      celestial = Celestial.NONE
      particleColor = Color(0xFFCFC6F5)
    }
    WeatherCondition.UNKNOWN -> {
      colors = if (isDay) PartlyDay else PartlyNight
      particle = SkyParticle.NONE
      celestial = Celestial.NONE
      particleColor = Color(0xFFFFFFFF)
    }
  }

  return SkyPalette(
      colors = colors,
      particle = particle,
      celestial = celestial,
      particleColor = particleColor,
  )
}
