package com.brahim.meteo.util

import androidx.annotation.StringRes
import com.brahim.meteo.R

/**
 * The six bands of the United States air quality index.
 *
 * @param labelRes the translated name of the band.
 * @param maxValue the inclusive upper bound of the band.
 */
enum class AqiLevel(@param:StringRes val labelRes: Int, val maxValue: Int) {
  GOOD(R.string.aqi_good, 50),
  MODERATE(R.string.aqi_moderate, 100),
  UNHEALTHY_SENSITIVE(R.string.aqi_unhealthy_sensitive, 150),
  UNHEALTHY(R.string.aqi_unhealthy, 200),
  VERY_UNHEALTHY(R.string.aqi_very_unhealthy, 300),
  HAZARDOUS(R.string.aqi_hazardous, Int.MAX_VALUE),
}

/** Maps a US AQI reading to its band. */
fun aqiLevel(value: Double?): AqiLevel? {
  if (value == null) return null
  val rounded = value.toInt().coerceAtLeast(0)
  return AqiLevel.entries.firstOrNull { level -> rounded <= level.maxValue } ?: AqiLevel.HAZARDOUS
}

/** The European AQI bands, used when the US reading is unavailable. */
enum class EuropeanAqiLevel(@param:StringRes val labelRes: Int, val maxValue: Int) {
  GOOD(R.string.aqi_good, 20),
  FAIR(R.string.aqi_fair, 40),
  MODERATE(R.string.aqi_moderate, 60),
  POOR(R.string.aqi_poor, 80),
  VERY_POOR(R.string.aqi_very_poor, 100),
  EXTREMELY_POOR(R.string.aqi_extremely_poor, Int.MAX_VALUE),
}

/** Maps a European AQI reading to its band. */
fun europeanAqiLevel(value: Double?): EuropeanAqiLevel? {
  if (value == null) return null
  val rounded = value.toInt().coerceAtLeast(0)
  return EuropeanAqiLevel.entries.firstOrNull { level -> rounded <= level.maxValue }
      ?: EuropeanAqiLevel.EXTREMELY_POOR
}
