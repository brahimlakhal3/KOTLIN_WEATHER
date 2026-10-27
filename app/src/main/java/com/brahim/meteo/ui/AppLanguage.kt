package com.brahim.meteo.ui

import java.util.Locale

/**
 * Languages shipped with the app. Arabic is intentionally not included: the requested language
 * pack focuses on European and other widely used languages.
 */
enum class AppLanguage(val code: String, val nativeName: String) {
  ENGLISH("en", "English"),
  FRENCH("fr", "Français"),
  SPANISH("es", "Español"),
  GERMAN("de", "Deutsch"),
  ITALIAN("it", "Italiano"),
  PORTUGUESE("pt", "Português"),
  TURKISH("tr", "Türkçe"),
  RUSSIAN("ru", "Русский");

  companion object {
    fun from(code: String?): AppLanguage =
        entries.firstOrNull { language -> language.code == code } ?: system()

    fun system(): AppLanguage {
      val language = Locale.getDefault().language
      return entries.firstOrNull { entry -> entry.code == language } ?: ENGLISH
    }
  }
}
