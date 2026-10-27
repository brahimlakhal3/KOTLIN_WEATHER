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

  val SHOW_ALERTS = stringPreferencesKey("show_alerts")
}
