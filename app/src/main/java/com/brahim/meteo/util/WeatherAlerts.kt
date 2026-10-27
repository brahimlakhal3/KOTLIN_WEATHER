package com.brahim.meteo.util

import androidx.annotation.StringRes
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast

enum class AlertKind(@param:StringRes val titleRes: Int, @param:StringRes val messageRes: Int) {
  HEAT(R.string.alert_heat_title, R.string.alert_heat_message),
  COLD(R.string.alert_cold_title, R.string.alert_cold_message),
  WIND(R.string.alert_wind_title, R.string.alert_wind_message),
  RAIN(R.string.alert_rain_title, R.string.alert_rain_message),
  SNOW(R.string.alert_snow_title, R.string.alert_snow_message),
  UV(R.string.alert_uv_title, R.string.alert_uv_message),
  THUNDERSTORM(R.string.alert_thunder_title, R.string.alert_thunder_message),
  FOG(R.string.alert_fog_title, R.string.alert_fog_message),
}

enum class AlertSeverity {
  WARNING,
  SEVERE,
}

/**
 * Value unit follows the alert kind: temperature in °C, wind in km/h, rain/snow in mm and UV as an
 * index. The UI converts it with the user's preferred unit system.
 */
data class WeatherAlert(
    val kind: AlertKind,
    val severity: AlertSeverity,
    val value: Double? = null,
)

fun weatherAlerts(
    hourly: List<HourlyForecast>,
    daily: List<DailyForecast>,
): List<WeatherAlert> {
  if (hourly.isEmpty() && daily.isEmpty()) return emptyList()
  val today = daily.firstOrNull()
  val alerts = mutableListOf<WeatherAlert>()

  today?.temperatureMax?.let { max ->
    if (max >= 33.0) {
      alerts +=
          WeatherAlert(
              kind = AlertKind.HEAT,
              severity = if (max >= 40.0) AlertSeverity.SEVERE else AlertSeverity.WARNING,
              value = max,
          )
    }
  }
  today?.temperatureMin?.let { min ->
    if (min <= -5.0) {
      alerts +=
          WeatherAlert(
              kind = AlertKind.COLD,
              severity = if (min <= -15.0) AlertSeverity.SEVERE else AlertSeverity.WARNING,
              value = min,
          )
    }
  }

  val gust = (hourly.take(24).mapNotNull { hour -> hour.windGust } + listOfNotNull(hourly.firstOrNull()?.windSpeed)).maxOrNull()
  if (gust != null && gust >= 60.0) {
    alerts +=
        WeatherAlert(
            kind = AlertKind.WIND,
            severity = if (gust >= 90.0) AlertSeverity.SEVERE else AlertSeverity.WARNING,
            value = gust,
        )
  }

  val rain = today?.precipitationSum ?: hourly.take(24).mapNotNull { it.precipitation }.sum()
  if (rain >= 15.0) {
    alerts +=
        WeatherAlert(
            kind = AlertKind.RAIN,
            severity = if (rain >= 35.0) AlertSeverity.SEVERE else AlertSeverity.WARNING,
            value = rain,
        )
  }

  val snowy =
      hourly.take(24).any { hour -> hour.weatherCode in SNOW_CODES } ||
          (today?.weatherCode ?: -1) in SNOW_CODES
  if (snowy) {
    alerts += WeatherAlert(AlertKind.SNOW, AlertSeverity.WARNING, today?.precipitationSum)
  }

  today?.uvIndexMax?.let { uv ->
    if (uv >= 8.0) {
      alerts +=
          WeatherAlert(
              kind = AlertKind.UV,
              severity = if (uv >= 11.0) AlertSeverity.SEVERE else AlertSeverity.WARNING,
              value = uv,
          )
    }
  }

  val stormy =
      hourly.take(24).any { hour -> hour.weatherCode in STORM_CODES } ||
          (today?.weatherCode ?: -1) in STORM_CODES
  if (stormy) alerts += WeatherAlert(AlertKind.THUNDERSTORM, AlertSeverity.SEVERE)

  val foggy = hourly.take(12).any { hour -> hour.weatherCode == 45 || hour.weatherCode == 48 }
  if (foggy) alerts += WeatherAlert(AlertKind.FOG, AlertSeverity.WARNING)

  return alerts
      .sortedByDescending { alert -> alert.severity.ordinal }
      .distinctBy { alert -> alert.kind }
      .take(3)
}

private val SNOW_CODES = setOf(71, 73, 75, 77, 85, 86)
private val STORM_CODES = setOf(95, 96, 99)
