package com.brahim.meteo.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.brahim.meteo.R

enum class WeatherCondition(@param:StringRes val labelRes: Int) {
  CLEAR(R.string.condition_clear),
  MAINLY_CLEAR(R.string.condition_mainly_clear),
  PARTLY_CLOUDY(R.string.condition_partly_cloudy),
  OVERCAST(R.string.condition_overcast),
  FOG(R.string.condition_fog),
  DRIZZLE(R.string.condition_drizzle),
  FREEZING_DRIZZLE(R.string.condition_freezing_drizzle),
  RAIN(R.string.condition_rain),
  FREEZING_RAIN(R.string.condition_freezing_rain),
  SNOW(R.string.condition_snow),
  RAIN_SHOWERS(R.string.condition_rain_showers),
  SNOW_SHOWERS(R.string.condition_snow_showers),
  THUNDERSTORM(R.string.condition_thunderstorm),
  THUNDERSTORM_HAIL(R.string.condition_thunderstorm_hail),
  UNKNOWN(R.string.condition_unknown),
}

fun weatherCondition(code: Int?): WeatherCondition =
    when (code) {
      0 -> WeatherCondition.CLEAR
      1 -> WeatherCondition.MAINLY_CLEAR
      2 -> WeatherCondition.PARTLY_CLOUDY
      3 -> WeatherCondition.OVERCAST
      45, 48 -> WeatherCondition.FOG
      51, 53, 55 -> WeatherCondition.DRIZZLE
      56, 57 -> WeatherCondition.FREEZING_DRIZZLE
      61, 63, 65 -> WeatherCondition.RAIN
      66, 67 -> WeatherCondition.FREEZING_RAIN
      71, 73, 75, 77 -> WeatherCondition.SNOW
      80, 81, 82 -> WeatherCondition.RAIN_SHOWERS
      85, 86 -> WeatherCondition.SNOW_SHOWERS
      95 -> WeatherCondition.THUNDERSTORM
      96, 99 -> WeatherCondition.THUNDERSTORM_HAIL
      else -> WeatherCondition.UNKNOWN
    }

fun WeatherCondition.isPrecipitation(): Boolean =
    this == WeatherCondition.DRIZZLE ||
        this == WeatherCondition.FREEZING_DRIZZLE ||
        this == WeatherCondition.RAIN ||
        this == WeatherCondition.FREEZING_RAIN ||
        this == WeatherCondition.SNOW ||
        this == WeatherCondition.RAIN_SHOWERS ||
        this == WeatherCondition.SNOW_SHOWERS ||
        this == WeatherCondition.THUNDERSTORM ||
        this == WeatherCondition.THUNDERSTORM_HAIL

@DrawableRes
fun weatherIconRes(code: Int?, isDay: Boolean): Int =
    when (code) {
      0 -> if (isDay) R.drawable.weather_sun else R.drawable.weather_moon
      1, 2 -> if (isDay) R.drawable.weather_partly_day else R.drawable.weather_partly_night
      3 -> R.drawable.weather_overcast
      45, 48 -> R.drawable.weather_fog
      51, 53, 55 -> R.drawable.weather_drizzle
      56, 57, 66, 67 -> R.drawable.weather_sleet
      61, 63, 80, 81 -> R.drawable.weather_rain
      65, 82 -> R.drawable.weather_heavy_rain
      71, 73, 85 -> R.drawable.weather_snow
      75, 77, 86 -> R.drawable.weather_heavy_snow
      95 -> R.drawable.weather_storm
      96, 99 -> R.drawable.weather_storm_hail
      else -> R.drawable.weather_cloud
    }
