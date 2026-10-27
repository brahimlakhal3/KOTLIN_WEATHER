package com.brahim.meteo.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.util.formatHour

@Composable
fun SummaryCard(
    weatherCode: Int?,
    isDay: Boolean,
    temperature: String,
    trailing: String,
    modifier: Modifier = Modifier,
    info: String? = null,
    details: List<String> = emptyList(),
) {
  val itemList = when {
    details.isNotEmpty() -> details
    info.isNullOrBlank() -> emptyList()
    else -> info.split(" · ")
  }

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .clip(MaterialTheme.shapes.large)
              .background(
                  Brush.linearGradient(
                      listOf(
                          MaterialTheme.colorScheme.primaryContainer,
                          MaterialTheme.colorScheme.secondaryContainer,
                          MaterialTheme.colorScheme.tertiaryContainer,
                      )
                  )
              )
  ) {
    Box(
        modifier =
            Modifier.align(Alignment.TopEnd)
                .offset(x = 26.dp, y = (-30).dp)
                .size(126.dp)
                .background(
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.06f),
                    CircleShape,
                )
    )
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          WeatherIcon(
              weatherCode = weatherCode,
              isDay = isDay,
              size = 44.dp,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          AnimatedValue(
              text = temperature,
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        SummaryChip(text = trailing)
      }

      if (itemList.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
          itemList.forEach { item ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
              Text(
                  text = item,
                  style = MaterialTheme.typography.labelLarge,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryChip(text: String) {
  Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surfaceContainer,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

@Composable
fun sunTimes(today: DailyForecast?): String {
  val offset = today?.utcOffsetSeconds ?: 0
  val sunrise = today?.sunrise?.let { instant -> formatHour(instant, offset) } ?: "—"
  val sunset = today?.sunset?.let { instant -> formatHour(instant, offset) } ?: "—"
  return stringResource(R.string.sun_times, sunrise, sunset)
}
