package com.brahim.meteo.config

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.appConfigStore by preferencesDataStore(name = "app_config")

object AppConfigKeys {
  val THEME_MODE = stringPreferencesKey("theme_mode")
  val LANGUAGE = stringPreferencesKey("language")
  val TEXT_SIZE = stringPreferencesKey("text_size")
  val UNITS = stringPreferencesKey("units")
}
