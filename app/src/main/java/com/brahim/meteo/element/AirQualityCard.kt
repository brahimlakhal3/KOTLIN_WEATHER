package com.brahim.meteo.element

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.AirQuality
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.util.aqiLevel
import com.brahim.meteo.util.europeanAqiLevel
import com.brahim.meteo.util.formatNumber
import com.brahim.meteo.ui.currentLocale
import kotlin.math.roundToInt

/**
 * The current air quality with a breakdown of the main pollutants.
 *
 * The headline number is the US AQI when Open-Meteo provides it, otherwise the European AQI. Each
 * pollutant gets a bar that grows out of the track when the card first appears.
 *
 * @param quality the latest reading, or `null` when it is not available.
 * @param modifier applied to the card.
 */
@Composable
fun AirQualityCard(
    quality: AirQuality?,
    modifier: Modifier = Modifier,
) {
  if (quality == null || quality.isEmpty) return
  val animations = LocalAnimationsEnabled.current
  val locale = currentLocale()

  val usAqi = quality.usAqi?.roundToInt()
  val europeanAqi = quality.europeanAqi?.roundToInt()
  val headline = usAqi ?: europeanAqi
  if (headline == null) return

  val level = aqiLevel(quality.usAqi)
  val europeanLevel = europeanAqiLevel(quality.europeanAqi)
  val labelRes = level?.labelRes ?: europeanLevel?.labelRes ?: R.string.aqi_good
  val indicatorColor = aqiColor(headline)

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SectionHeader(title = stringResource(R.string.air_quality_title))
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(
            modifier =
                Modifier.size(62.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = headline.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = indicatorColor,
            )
            Text(
                text = stringResource(R.string.air_quality_index),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Text(
              text = stringResource(labelRes),
              style = MaterialTheme.typography.titleMedium,
              color = indicatorColor,
          )
          Text(
              text =
                  stringResource(
                      R.string.air_quality_summary,
                      stringResource(labelRes),
                  ),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PollutantBar(
            label = stringResource(R.string.pollutant_pm25),
            value = quality.pm25,
            limit = PM25_LIMIT,
            unit = "µg/m³",
        )
        PollutantBar(
            label = stringResource(R.string.pollutant_pm10),
            value = quality.pm10,
            limit = PM10_LIMIT,
            unit = "µg/m³",
        )
        PollutantBar(
            label = stringResource(R.string.pollutant_ozone),
            value = quality.ozone,
            limit = OZONE_LIMIT,
            unit = "µg/m³",
        )
        PollutantBar(
            label = stringResource(R.string.pollutant_no2),
            value = quality.nitrogenDioxide,
            limit = NO2_LIMIT,
            unit = "µg/m³",
        )
      }
    }
  }
}

@Composable
private fun PollutantBar(
    label: String,
    value: Double?,
    limit: Double,
    unit: String,
) {
  val animations = LocalAnimationsEnabled.current
  val ratio = ((value ?: 0.0) / limit).coerceIn(0.0, 1.0).toFloat()
  val animated by
      animateFloatAsState(
          targetValue = ratio,
          animationSpec = if (animations) Motion.settle else Motion.snap(),
          label = "pollutant_$label",
      )

  Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
      Text(
          text =
              value?.let { reading ->
                "${reading.formatNumber(1, currentLocale())} $unit"
              } ?: "—",
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1,
      )
    }
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
      Box(
          modifier =
              Modifier.fillMaxWidth(animated.coerceAtLeast(0.02f))
                  .height(6.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
      )
    }
  }
}

/** The accent colour that represents an air quality reading. */
@Composable
private fun aqiColor(value: Int): Color {
  val scheme = MaterialTheme.colorScheme
  return when {
    value <= 50 -> scheme.primary
    value <= 100 -> scheme.tertiary
    value <= 150 -> scheme.secondary
    value <= 200 -> scheme.error
    else -> scheme.error
  }
}

/** The icon used by the air quality entry in the conditions grid. */
internal val AirQualityIcon = Icons.Outlined.Air

private const val PM25_LIMIT = 35.0
private const val PM10_LIMIT = 150.0
private const val OZONE_LIMIT = 120.0
private const val NO2_LIMIT = 100.0
