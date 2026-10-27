package com.brahim.meteo.element

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.ui.currentLocale
import com.brahim.meteo.util.formatDistance
import com.brahim.meteo.util.formatPrecipitation
import com.brahim.meteo.util.formatPressure
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.formatWind
import com.brahim.meteo.util.uvIndexText
import com.brahim.meteo.util.windDirectionLabel
import kotlin.math.roundToInt

@Composable
fun weatherMetrics(
    hours: List<HourlyForecast>,
    day: DailyForecast?,
    mean: Boolean,
): List<Metric> {
  val locale = currentLocale()
  val maxCaption = stringResource(R.string.metric_day_max)

  fun value(selector: (HourlyForecast) -> Double?): Double? {
    if (!mean) return hours.firstOrNull()?.let(selector)
    val values = hours.mapNotNull(selector)
    return if (values.isEmpty()) null else values.average()
  }

  val windValue = formatWind(value { hour -> hour.windSpeed }, 0)
  val wind =
      if (mean) windValue
      else
          windValue +
              hours
                  .firstOrNull()
                  ?.windDirection
                  ?.let { degrees -> " " + windDirectionLabel(degrees, locale) }
                  .orEmpty()

  return listOf(
      Metric(
          icon = AppIcon.Drawable(R.drawable.humidity),
          label = stringResource(R.string.metric_humidity),
          value = percent(value { hour -> hour.humidity?.toDouble() }),
          dayMetric = DayMetric.HUMIDITY,
      ),
      Metric(
          icon = AppIcon.Drawable(R.drawable.wind),
          label = stringResource(R.string.metric_wind),
          value = wind,
          dayMetric = DayMetric.WIND,
      ),
      Metric(
          icon = AppIcon.Vector(Icons.Outlined.Air),
          label = stringResource(R.string.metric_gusts),
          value = formatWind(value { hour -> hour.windGust }, 0),
          dayMetric = DayMetric.GUSTS,
      ),
      Metric(
          icon = AppIcon.Drawable(R.drawable.precipitation),
          label = stringResource(R.string.metric_precipitation),
          value = formatPrecipitation(value { hour -> hour.precipitation }, 1),
          dayMetric = DayMetric.PRECIPITATION,
      ),
      Metric(
          icon = AppIcon.Vector(Icons.Outlined.Umbrella),
          label = stringResource(R.string.metric_probability),
          value = percent(value { hour -> hour.precipitationProbability?.toDouble() }),
          dayMetric = DayMetric.PROBABILITY,
      ),
      Metric(
          icon = AppIcon.Drawable(R.drawable.visibility),
          label = stringResource(R.string.metric_visibility),
          value =
              formatDistance(
                  value { hour -> hour.visibility?.toDouble() }?.let { meters -> meters / 1_000.0 },
                  1,
              ),
          dayMetric = DayMetric.VISIBILITY,
      ),
      Metric(
          icon = AppIcon.Vector(Icons.Outlined.WaterDrop),
          label = stringResource(R.string.metric_dew_point),
          value = formatTemperature(value { hour -> hour.dewPoint }),
          dayMetric = DayMetric.DEW_POINT,
      ),
      Metric(
          icon = AppIcon.Vector(Icons.Outlined.Speed),
          label = stringResource(R.string.metric_pressure),
          value = formatPressure(value { hour -> hour.surfacePressure }),
          dayMetric = DayMetric.PRESSURE,
      ),
      Metric(
          icon = AppIcon.Drawable(R.drawable.uv_index),
          label = stringResource(R.string.metric_uv_index),
          value = uvIndexText(day?.uvIndexMax, locale),
          caption = if (mean) maxCaption else null,
      ),
  )
}

@Composable
private fun percent(value: Double?): String =
    value?.let { amount -> stringResource(R.string.unit_percent, amount.roundToInt()) } ?: "—"
