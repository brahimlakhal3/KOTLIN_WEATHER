package com.brahim.meteo.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.brahim.meteo.R

enum class AqiLevel(@param:StringRes val labelRes: Int, val color: Color) {
  GOOD(R.string.aqi_good, Color(0xFF2E7D32)),
  FAIR(R.string.aqi_fair, Color(0xFF689F38)),
  MODERATE(R.string.aqi_moderate, Color(0xFFF9A825)),
  POOR(R.string.aqi_poor, Color(0xFFEF6C00)),
  VERY_POOR(R.string.aqi_very_poor, Color(0xFFC62828)),
  EXTREME(R.string.aqi_extreme, Color(0xFF6A1B9A)),
}

fun aqiLevel(value: Int?): AqiLevel? =
    when {
      value == null -> null
      value <= 20 -> AqiLevel.GOOD
      value <= 40 -> AqiLevel.FAIR
      value <= 60 -> AqiLevel.MODERATE
      value <= 80 -> AqiLevel.POOR
      value <= 100 -> AqiLevel.VERY_POOR
      else -> AqiLevel.EXTREME
    }
