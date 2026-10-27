package com.brahim.meteo.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import com.brahim.meteo.ui.currentLocale
import java.time.Instant

enum class TemperatureUnit {
  CELSIUS,
  FAHRENHEIT;

  companion object {
    fun from(value: String?): TemperatureUnit =
        entries.firstOrNull { entry -> entry.name == value } ?: CELSIUS
  }
}

enum class WindUnit {
  KILOMETRES_PER_HOUR,
  MILES_PER_HOUR,
  METRES_PER_SECOND,
  KNOTS;

  companion object {
    fun from(value: String?): WindUnit =
        entries.firstOrNull { entry -> entry.name == value } ?: KILOMETRES_PER_HOUR
  }
}

enum class PrecipitationUnit {
  MILLIMETRES,
  INCHES;

  companion object {
    fun from(value: String?): PrecipitationUnit =
        entries.firstOrNull { entry -> entry.name == value } ?: MILLIMETRES
  }
}

enum class DistanceUnit {
  KILOMETRES,
  MILES;

  companion object {
    fun from(value: String?): DistanceUnit =
        entries.firstOrNull { entry -> entry.name == value } ?: KILOMETRES
  }
}

enum class PressureUnit {
  HECTOPASCALS,
  INCHES_OF_MERCURY;

  companion object {
    fun from(value: String?): PressureUnit =
        entries.firstOrNull { entry -> entry.name == value } ?: HECTOPASCALS
  }
}

enum class TimeFormat {
  HOUR_24,
  HOUR_12;

  companion object {
    fun from(value: String?): TimeFormat =
        entries.firstOrNull { entry -> entry.name == value } ?: HOUR_24
  }
}

data class UnitPreferences(
    val temperature: TemperatureUnit = TemperatureUnit.CELSIUS,
    val wind: WindUnit = WindUnit.KILOMETRES_PER_HOUR,
    val precipitation: PrecipitationUnit = PrecipitationUnit.MILLIMETRES,
    val distance: DistanceUnit = DistanceUnit.KILOMETRES,
    val pressure: PressureUnit = PressureUnit.HECTOPASCALS,
    val timeFormat: TimeFormat = TimeFormat.HOUR_24,
    val temperatureDecimals: Int = 0,
)

val LocalUnits = staticCompositionLocalOf { UnitPreferences() }

private const val MILES_PER_KILOMETRE = 0.621371
private const val MILLIMETRES_PER_INCH = 25.4
private const val HPA_PER_INHG = 33.8639
private const val KMH_PER_MS = 3.6
private const val KMH_PER_KNOT = 1.852

private fun TemperatureUnit.convert(celsius: Double?): Double? =
    when (this) {
      TemperatureUnit.CELSIUS -> celsius
      TemperatureUnit.FAHRENHEIT -> celsius?.let { degrees -> degrees * 9 / 5 + 32 }
    }

private fun WindUnit.convert(kilometresPerHour: Double?): Double? =
    when (this) {
      WindUnit.KILOMETRES_PER_HOUR -> kilometresPerHour
      WindUnit.MILES_PER_HOUR -> kilometresPerHour?.let { value -> value * MILES_PER_KILOMETRE }
      WindUnit.METRES_PER_SECOND -> kilometresPerHour?.let { value -> value / KMH_PER_MS }
      WindUnit.KNOTS -> kilometresPerHour?.let { value -> value / KMH_PER_KNOT }
    }

private fun PrecipitationUnit.convert(millimetres: Double?): Double? =
    when (this) {
      PrecipitationUnit.MILLIMETRES -> millimetres
      PrecipitationUnit.INCHES -> millimetres?.let { value -> value / MILLIMETRES_PER_INCH }
    }

private fun DistanceUnit.convert(kilometres: Double?): Double? =
    when (this) {
      DistanceUnit.KILOMETRES -> kilometres
      DistanceUnit.MILES -> kilometres?.let { value -> value * MILES_PER_KILOMETRE }
    }

private fun PressureUnit.convert(hectopascals: Double?): Double? =
    when (this) {
      PressureUnit.HECTOPASCALS -> hectopascals
      PressureUnit.INCHES_OF_MERCURY -> hectopascals?.let { value -> value / HPA_PER_INHG }
    }

@Composable
fun formatTemperature(celsius: Double?, decimals: Int? = null): String {
  val units = LocalUnits.current
  return format(
      units.temperature.convert(celsius),
      R.string.unit_temperature,
      decimals ?: units.temperatureDecimals,
  )
}

@Composable
fun formatWind(kilometresPerHour: Double?, decimals: Int = 0): String {
  val units = LocalUnits.current
  @StringRes val template =
      when (units.wind) {
        WindUnit.KILOMETRES_PER_HOUR -> R.string.unit_kmh
        WindUnit.MILES_PER_HOUR -> R.string.unit_mph
        WindUnit.METRES_PER_SECOND -> R.string.unit_ms
        WindUnit.KNOTS -> R.string.unit_knots
      }
  return format(units.wind.convert(kilometresPerHour), template, decimals)
}

@Composable
fun formatDistance(kilometres: Double?, decimals: Int = 1): String {
  val units = LocalUnits.current
  @StringRes val template =
      if (units.distance == DistanceUnit.MILES) R.string.unit_miles else R.string.unit_km
  return format(units.distance.convert(kilometres), template, decimals)
}

@Composable
fun formatPrecipitation(millimetres: Double?, decimals: Int = 1): String {
  val units = LocalUnits.current
  val imperial = units.precipitation == PrecipitationUnit.INCHES
  return format(
      units.precipitation.convert(millimetres),
      if (imperial) R.string.unit_inches else R.string.unit_mm,
      if (imperial) decimals + 1 else decimals,
  )
}

@Composable
fun formatPressure(hectopascals: Double?, decimals: Int = 0): String {
  val units = LocalUnits.current
  val imperial = units.pressure == PressureUnit.INCHES_OF_MERCURY
  return format(
      units.pressure.convert(hectopascals),
      if (imperial) R.string.unit_inhg else R.string.unit_hpa,
      if (imperial) maxOf(decimals, 2) else decimals,
  )
}

@Composable
fun formatHour(instant: Instant, utcOffsetSeconds: Int): String {
  val units = LocalUnits.current
  return localDateTime(instant, utcOffsetSeconds)
      .formatHour(units.timeFormat, currentLocale())
}

@Composable
fun formatSyncTime(instant: Instant): String {
  val units = LocalUnits.current
  return instant.formatSyncTime(currentLocale(), timeFormat = units.timeFormat)
}

@Composable
private fun format(value: Double?, @StringRes template: Int, decimals: Int): String =
    if (value == null) "—"
    else stringResource(template, value.formatNumber(decimals, currentLocale()))
