package com.brahim.meteo.util

import androidx.annotation.StringRes
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast

enum class AdviceKind(
    @param:StringRes val titleRes: Int,
    @param:StringRes val detailRes: Int,
) {
  UMBRELLA(R.string.advice_umbrella_title, R.string.advice_umbrella_detail),
  STORM(R.string.advice_storm_title, R.string.advice_storm_detail),
  SNOW(R.string.advice_snow_title, R.string.advice_snow_detail),
  JACKET(R.string.advice_jacket_title, R.string.advice_jacket_detail),
  LAYERS(R.string.advice_layers_title, R.string.advice_layers_detail),
  HEAT(R.string.advice_heat_title, R.string.advice_heat_detail),
  SUNSCREEN(R.string.advice_sunscreen_title, R.string.advice_sunscreen_detail),
  WIND(R.string.advice_wind_title, R.string.advice_wind_detail),
  VISIBILITY(R.string.advice_visibility_title, R.string.advice_visibility_detail),
  HUMIDITY(R.string.advice_humidity_title, R.string.advice_humidity_detail),
  HYDRATE(R.string.advice_hydrate_title, R.string.advice_hydrate_detail),
  COMFORT(R.string.advice_comfort_title, R.string.advice_comfort_detail),
}

data class Advice(
    val kind: AdviceKind,
    val priority: Int,
    val value: Double? = null,
)

/**
 * Builds a short, ranked list of practical tips from the next 24 hours and today's outlook.
 * Values stay in metric units and are formatted by the UI, so the advice follows the app locale.
 */
fun weatherAdvice(
    hourly: List<HourlyForecast>,
    daily: List<DailyForecast>,
): List<Advice> {
  val current = hourly.firstOrNull() ?: return emptyList()
  val today = daily.firstOrNull()
  val next = hourly.take(24)
  val candidates = mutableListOf<Advice>()

  val rainChance = next.maxOfOrNull { hour -> hour.precipitationProbability ?: 0 } ?: 0
  val rainSum = today?.precipitationSum ?: next.mapNotNull { it.precipitation }.sum()
  val stormy =
      next.any { hour -> hour.weatherCode in STORM_CODES } ||
          (today?.weatherCode ?: -1) in STORM_CODES
  if (stormy) {
    candidates += Advice(AdviceKind.STORM, priority = 100, value = rainChance.toDouble())
  }

  val snowy =
      next.any { hour -> hour.weatherCode in SNOW_CODES } ||
          (today?.weatherCode ?: -1) in SNOW_CODES
  if (snowy) {
    candidates += Advice(AdviceKind.SNOW, priority = 95, value = rainSum)
  }

  val feelsLike = current.apparentTemperature ?: current.temperature
  val high = today?.temperatureMax ?: current.temperature
  val low = today?.temperatureMin ?: feelsLike
  if (high >= 33.0 || current.temperature >= 32.0) {
    candidates +=
        Advice(AdviceKind.HEAT, priority = 90, value = maxOf(high, current.temperature))
  }
  if (feelsLike <= -5.0 || low <= -5.0) {
    candidates +=
        Advice(AdviceKind.JACKET, priority = 88, value = minOf(feelsLike, low))
  }

  if (rainChance >= 35 || rainSum >= 2.0) {
    val chance = if (rainChance >= 35) rainChance else 50
    candidates += Advice(AdviceKind.UMBRELLA, priority = 80, value = chance.toDouble())
  }

  val uv = today?.uvIndexMax ?: 0.0
  if (uv >= 6.0 && current.isDay) {
    candidates += Advice(AdviceKind.SUNSCREEN, priority = 70, value = uv)
  }

  val wind = next.maxOfOrNull { hour -> hour.windGust ?: hour.windSpeed ?: 0.0 } ?: 0.0
  if (wind >= 45.0) {
    candidates += Advice(AdviceKind.WIND, priority = 65, value = wind)
  }

  val visibility = current.visibility ?: 10_000.0
  if (visibility < 3_000.0 || current.weatherCode == 45 || current.weatherCode == 48) {
    candidates += Advice(AdviceKind.VISIBILITY, priority = 60, value = visibility)
  }

  val humidity = current.humidity ?: 0
  if (humidity >= 80 && current.temperature >= 22.0) {
    candidates += Advice(AdviceKind.HUMIDITY, priority = 50, value = humidity.toDouble())
  }

  if (current.temperature >= 28.0 || high >= 30.0) {
    candidates +=
        Advice(AdviceKind.HYDRATE, priority = 45, value = maxOf(current.temperature, high))
  }

  if (feelsLike <= 8.0) {
    candidates += Advice(AdviceKind.JACKET, priority = 40, value = feelsLike)
  }
  if (feelsLike <= 16.0) {
    candidates += Advice(AdviceKind.LAYERS, priority = 30, value = feelsLike)
  }

  if (candidates.isEmpty()) {
    candidates += Advice(AdviceKind.COMFORT, priority = 0)
  }

  return candidates
      .sortedByDescending { advice -> advice.priority }
      .distinctBy { advice -> advice.kind }
      .take(4)
}

private val SNOW_CODES = setOf(71, 73, 75, 77, 85, 86)
private val STORM_CODES = setOf(95, 96, 99)
