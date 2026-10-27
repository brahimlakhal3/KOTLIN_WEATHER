package com.brahim.meteo.config

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.appConfigStore by preferencesDataStore(name = "app_config")

object AppConfigKeys {
  val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
  val START_MODE = stringPreferencesKey("start_mode")
  val START_SCREEN = stringPreferencesKey("start_screen")
  val HOME_CITY_LATITUDE = doublePreferencesKey("home_city_latitude")
  val HOME_CITY_LONGITUDE = doublePreferencesKey("home_city_longitude")

  val THEME_MODE = stringPreferencesKey("theme_mode")
  val ACCENT_COLOR = stringPreferencesKey("accent_color")
  val CORNER_STYLE = stringPreferencesKey("corner_style")
  val ANIMATIONS = stringPreferencesKey("animations")

  val LANGUAGE = stringPreferencesKey("language")
  val TEXT_SIZE = stringPreferencesKey("text_size")

  val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
  val WIND_UNIT = stringPreferencesKey("wind_unit")
  val PRECIPITATION_UNIT = stringPreferencesKey("precipitation_unit")
  val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
  val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
  val TIME_FORMAT = stringPreferencesKey("time_format")
  val TEMPERATURE_DECIMALS = stringPreferencesKey("temperature_decimals")

  val SHOW_ALERTS = stringPreferencesKey("show_alerts")
  val SHOW_AIR_QUALITY = stringPreferencesKey("show_air_quality")
}
