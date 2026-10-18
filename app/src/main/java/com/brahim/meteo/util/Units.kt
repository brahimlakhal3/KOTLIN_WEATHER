package com.brahim.meteo.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import com.brahim.meteo.ui.currentLocale

enum class UnitSystem {
  METRIC,
  IMPERIAL;

  companion object {
    fun from(value: String?): UnitSystem =
        entries.firstOrNull { entry -> entry.name == value } ?: METRIC
  }
}

val LocalUnitSystem = staticCompositionLocalOf { UnitSystem.METRIC }

private const val MILES_PER_KILOMETRE = 0.621371
private const val MILLIMETRES_PER_INCH = 25.4

private fun UnitSystem.celsius(value: Double?): Double? =
    when (this) {
      UnitSystem.METRIC -> value
      UnitSystem.IMPERIAL -> value?.let { degrees -> degrees * 9 / 5 + 32 }
    }

private fun UnitSystem.kilometres(value: Double?): Double? =
    when (this) {
      UnitSystem.METRIC -> value
      UnitSystem.IMPERIAL -> value?.let { distance -> distance * MILES_PER_KILOMETRE }
    }

private fun UnitSystem.millimetres(value: Double?): Double? =
    when (this) {
      UnitSystem.METRIC -> value
      UnitSystem.IMPERIAL -> value?.let { amount -> amount / MILLIMETRES_PER_INCH }
    }

@Composable
fun formatTemperature(celsius: Double?, decimals: Int = 0): String =
    format(LocalUnitSystem.current.celsius(celsius), R.string.unit_temperature, decimals)

@Composable
fun formatWind(kilometresPerHour: Double?, decimals: Int = 0): String {
  val imperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
  return format(
      LocalUnitSystem.current.kilometres(kilometresPerHour),
      if (imperial) R.string.unit_mph else R.string.unit_kmh,
      decimals,
  )
}

@Composable
fun formatDistance(kilometres: Double?, decimals: Int = 1): String {
  val imperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
  return format(
      LocalUnitSystem.current.kilometres(kilometres),
      if (imperial) R.string.unit_miles else R.string.unit_km,
      decimals,
  )
}

@Composable
fun formatPrecipitation(millimetres: Double?, decimals: Int = 1): String {
  val imperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
  return format(
      LocalUnitSystem.current.millimetres(millimetres),
      if (imperial) R.string.unit_inches else R.string.unit_mm,
      if (imperial) decimals + 1 else decimals,
  )
}

@Composable
private fun format(value: Double?, @StringRes template: Int, decimals: Int): String =
    if (value == null) "—"
    else stringResource(template, value.formatNumber(decimals, currentLocale()))
