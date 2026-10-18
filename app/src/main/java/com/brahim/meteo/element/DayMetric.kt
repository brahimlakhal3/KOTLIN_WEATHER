package com.brahim.meteo.element

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.util.formatDistance
import com.brahim.meteo.util.formatPrecipitation
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.formatWind

enum class DayMetric(@param:StringRes val labelRes: Int, val icon: AppIcon) {
  TEMPERATURE(R.string.metric_temperature, AppIcon.Vector(Icons.Outlined.Thermostat)),
  FEELS_LIKE(R.string.metric_feels_like, AppIcon.Vector(Icons.Outlined.DeviceThermostat)),
  HUMIDITY(R.string.metric_humidity, AppIcon.Drawable(R.drawable.humidity)),
  PRECIPITATION(R.string.metric_precipitation, AppIcon.Drawable(R.drawable.precipitation)),
  PROBABILITY(R.string.metric_probability, AppIcon.Vector(Icons.Outlined.Umbrella)),
  WIND(R.string.metric_wind, AppIcon.Drawable(R.drawable.wind)),
  GUSTS(R.string.metric_gusts, AppIcon.Vector(Icons.Outlined.Air)),
  VISIBILITY(R.string.metric_visibility, AppIcon.Drawable(R.drawable.visibility)),
  DEW_POINT(R.string.metric_dew_point, AppIcon.Vector(Icons.Outlined.WaterDrop));

  companion object {
    fun fromName(value: String?): DayMetric? =
        entries.firstOrNull { metric -> metric.name == value }
  }
}

fun DayMetric.read(hour: HourlyForecast): Double? =
    when (this) {
      DayMetric.TEMPERATURE -> hour.temperature
      DayMetric.FEELS_LIKE -> hour.apparentTemperature
      DayMetric.HUMIDITY -> hour.humidity?.toDouble()
      DayMetric.PRECIPITATION -> hour.precipitation
      DayMetric.PROBABILITY -> hour.precipitationProbability?.toDouble()
      DayMetric.WIND -> hour.windSpeed
      DayMetric.GUSTS -> hour.windGust
      DayMetric.VISIBILITY -> hour.visibility?.let { meters -> meters / 1_000.0 }
      DayMetric.DEW_POINT -> hour.dewPoint
    }

@Composable
fun DayMetric.text(value: Double?): String =
    when (this) {
      DayMetric.TEMPERATURE,
      DayMetric.FEELS_LIKE,
      DayMetric.DEW_POINT -> formatTemperature(value, 0)
      DayMetric.HUMIDITY,
      DayMetric.PROBABILITY ->
          value?.let { amount -> stringResource(R.string.unit_percent, amount.toInt()) } ?: "—"
      DayMetric.PRECIPITATION -> formatPrecipitation(value, 1)
      DayMetric.WIND,
      DayMetric.GUSTS -> formatWind(value, 0)
      DayMetric.VISIBILITY -> formatDistance(value, 1)
    }
