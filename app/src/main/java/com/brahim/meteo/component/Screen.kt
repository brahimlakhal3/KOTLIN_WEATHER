package com.brahim.meteo.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.brahim.meteo.R

enum class Screen(
    val route: String,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
) {
  Cities("cities", R.string.screen_weather, Icons.Outlined.Cloud),
  Search("search", R.string.screen_search, Icons.Outlined.Search),
  Settings("settings", R.string.screen_settings, Icons.Outlined.Settings);

  companion object {
    fun fromRoute(route: String?): Screen? =
        entries.firstOrNull { screen ->
          (route == screen.route) ||
              (route?.startsWith("${screen.route}/") == true) ||
              (route?.startsWith("${screen.route}?") == true)
        }
  }
}

object Routes {
  const val ARG_LATITUDE = "latitude"
  const val ARG_LONGITUDE = "longitude"
  const val ARG_EPOCH_DAY = "epochDay"
  const val ARG_METRIC = "metric"

  const val FORECAST = "forecast/{$ARG_LATITUDE}/{$ARG_LONGITUDE}"
  const val DAY = "day/{$ARG_LATITUDE}/{$ARG_LONGITUDE}/{$ARG_EPOCH_DAY}?$ARG_METRIC={$ARG_METRIC}"

  fun forecast(latitude: Double, longitude: Double): String = "forecast/$latitude/$longitude"

  fun day(latitude: Double, longitude: Double, epochDay: Long, metric: String? = null): String =
      if (metric == null) "day/$latitude/$longitude/$epochDay"
      else "day/$latitude/$longitude/$epochDay?$ARG_METRIC=$metric"
}
