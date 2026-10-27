package com.brahim.meteo.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.util.formatHour
import com.brahim.meteo.util.formatTemperature

private val ChipWidth = 88.dp

@Composable
fun HourlyStrip(
    items: List<HourlyForecast>,
    onClick: (HourlyForecast) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
  val currentTime = items.firstOrNull()?.time

  LazyRow(
      modifier = modifier.fillMaxWidth(),
      contentPadding = contentPadding,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Top,
  ) {
    itemsIndexed(items = items, key = { _, item -> item.time.toEpochMilli() }) { index, item ->
      HourChip(
          item = item,
          index = index,
          isCurrent = item.time == currentTime,
          onClick = { onClick(item) },
          modifier = Modifier.width(ChipWidth).animateItem(),
      )
    }
  }
}

@Composable
private fun HourChip(
    item: HourlyForecast,
    index: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val (container, content) =
      when {
        isCurrent -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        index % 3 == 0 ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        index % 3 == 1 ->
            MaterialTheme.colorScheme.secondaryContainer to
                MaterialTheme.colorScheme.onSecondaryContainer
        else ->
            MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
      }
  val detail = content.copy(alpha = 0.78f)

  Surface(
      onClick = onClick,
      modifier = modifier,
      shape = MaterialTheme.shapes.large,
      color = container,
      contentColor = content,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
          text = formatHour(item.time, item.utcOffsetSeconds),
          style = MaterialTheme.typography.labelLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
      WeatherIcon(
          weatherCode = item.weatherCode,
          isDay = item.isDay,
          size = 32.dp,
          tint = content,
      )
      AnimatedValue(
          text = formatTemperature(item.temperature),
          style = MaterialTheme.typography.titleMedium,
      )
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Icon(
            imageVector = Icons.Outlined.WaterDrop,
            contentDescription = stringResource(R.string.metric_probability),
            tint = detail,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text =
                item.precipitationProbability?.let { probability ->
                  stringResource(R.string.unit_percent, probability)
                } ?: "—",
            style = MaterialTheme.typography.labelSmall,
            color = detail,
            maxLines = 1,
        )
      }
    }
  }
}
