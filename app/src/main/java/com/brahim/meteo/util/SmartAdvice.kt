package com.brahim.meteo.util

import androidx.annotation.StringRes
import com.brahim.meteo.R
import com.brahim.meteo.data.model.AirQuality
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast

enum class AdviceKind(
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
) {
  UMBRELLA(R.string.advice_umbrella_title, R.string.advice_umbrella_message),
  JACKET(R.string.advice_jacket_title, R.string.advice_jacket_message),
  LAYERS(R.string.advice_layers_title, R.string.advice_layers_message),
  SUNSCREEN(R.string.advice_sunscreen_title, R.string.advice_sunscreen_message),
  WIND(R.string.advice_wind_title, R.string.advice_wind_message),
  AIR(R.string.advice_air_title, R.string.advice_air_message),
  VISIBILITY(R.string.advice_visibility_title, R.string.advice_visibility_message),
  HYDRATE(R.string.advice_hydrate_title, R.string.advice_hydrate_message),
}

data class Advice(val kind: AdviceKind)

fun weatherAdvice(
    hourly: List<HourlyForecast>,
    daily: List<DailyForecast>,
    airQuality: AirQuality?,
): List<Advice> {
  val current = hourly.firstOrNull() ?: return emptyList()
  val today = daily.firstOrNull()
  val next = hourly.take(24)
  val advice = mutableListOf<Advice>()

  val rainChance = next.maxOfOrNull { hour -> hour.precipitationProbability ?: 0 } ?: 0
  val rainSum = today?.precipitationSum ?: next.mapNotNull { it.precipitation }.sum()
  if (rainChance >= 40 || rainSum >= 3.0) advice += Advice(AdviceKind.UMBRELLA)

  val feelsLike = current.apparentTemperature ?: current.temperature
  when {
    feelsLike <= 8.0 -> advice += Advice(AdviceKind.JACKET)
    feelsLike <= 16.0 -> advice += Advice(AdviceKind.LAYERS)
  }

  val uv = today?.uvIndexMax ?: 0.0
  if (uv >= 6.0) advice += Advice(AdviceKind.SUNSCREEN)

  val wind = next.maxOfOrNull { hour -> hour.windGust ?: hour.windSpeed ?: 0.0 } ?: 0.0
  if (wind >= 35.0) advice += Advice(AdviceKind.WIND)

  val aqi = airQuality?.europeanAqi
  if (aqi != null && aqi > 60) advice += Advice(AdviceKind.AIR)

  val visibility = current.visibility ?: 10_000.0
  if (visibility < 3_000.0) advice += Advice(AdviceKind.VISIBILITY)

  if (current.temperature >= 28.0) advice += Advice(AdviceKind.HYDRATE)

  if (advice.isEmpty()) {
    advice += Advice(AdviceKind.SUNSCREEN.takeIf { current.isDay } ?: AdviceKind.LAYERS)
  }
  return advice.distinctBy { item -> item.kind }.take(4)
}
