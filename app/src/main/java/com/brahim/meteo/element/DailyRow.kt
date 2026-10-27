package com.brahim.meteo.element

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.weatherCondition

@Composable
fun DailyRow(
    item: DailyForecast,
    label: String,
    dateLabel: String,
    weekLow: Double,
    weekHigh: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val condition = weatherCondition(item.weatherCode)

  Surface(
      onClick = onClick,
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(modifier = Modifier.width(76.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
      }
      WeatherIcon(weatherCode = item.weatherCode, isDay = true, size = 30.dp)
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(condition.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TemperatureRangeBar(
            low = item.temperatureMin,
            high = item.temperatureMax,
            weekLow = weekLow,
            weekHigh = weekHigh,
        )
      }
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AnimatedValue(
            text = formatTemperature(item.temperatureMax),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = formatTemperature(item.temperatureMin),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun TemperatureRangeBar(
    low: Double?,
    high: Double?,
    weekLow: Double,
    weekHigh: Double,
    modifier: Modifier = Modifier,
) {
  val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
  val barBrush =
      Brush.horizontalGradient(
          listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.secondary,
              MaterialTheme.colorScheme.tertiary,
          )
      )
  val span = (weekHigh - weekLow).takeIf { value -> value > 0.5 } ?: 1.0
  val start = (low ?: weekLow).minus(weekLow).div(span).coerceIn(0.0, 1.0).toFloat()
  val end =
      (high ?: weekHigh).minus(weekLow).div(span).coerceIn(0.0, 1.0).toFloat().coerceAtLeast(
          start + 0.08f
      )

  Box(modifier = modifier.fillMaxWidth().height(6.dp)) {
    Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
      val radius = size.height / 2f
      drawRoundRect(color = trackColor, cornerRadius = CornerRadius(radius, radius))
      val left = size.width * start
      val right = size.width * end.coerceAtMost(1f)
      drawRoundRect(
          brush = barBrush,
          topLeft = Offset(left, 0f),
          size = Size((right - left).coerceAtLeast(size.height), size.height),
          cornerRadius = CornerRadius(radius, radius),
      )
    }
  }
}
