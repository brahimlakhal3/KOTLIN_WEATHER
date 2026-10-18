package com.brahim.meteo.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import java.util.Locale

@StringRes
fun uvIndexLevelRes(value: Double): Int =
    when {
      value < 3.0 -> R.string.uv_level_low
      value < 6.0 -> R.string.uv_level_moderate
      value < 8.0 -> R.string.uv_level_high
      value < 11.0 -> R.string.uv_level_very_high
      else -> R.string.uv_level_extreme
    }

@Composable
fun uvIndexText(value: Double?, locale: Locale): String =
    if (value == null) "—"
    else value.formatNumber(0, locale) + " · " + stringResource(uvIndexLevelRes(value))
