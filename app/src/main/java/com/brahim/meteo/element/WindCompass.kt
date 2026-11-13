package com.brahim.meteo.element

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.ui.currentLocale
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.util.formatWind
import com.brahim.meteo.util.windDirectionLabel
import kotlin.math.cos
import kotlin.math.sin

/**
 * A compass dial that points at the direction the wind blows from.
 *
 * The needle springs into place whenever the forecast changes, which makes the shift between a
 * south-westerly and a north-easterly easy to notice at a glance.
 *
 * @param directionDegrees the meteorological wind direction in degrees.
 * @param speed the sustained wind speed in kilometres per hour.
 * @param gust the highest gust in kilometres per hour.
 * @param modifier applied to the card.
 */
@Composable
fun WindCompassCard(
    directionDegrees: Int?,
    speed: Double?,
    gust: Double?,
    modifier: Modifier = Modifier,
) {
  val animations = LocalAnimationsEnabled.current
  val target = directionDegrees?.toFloat() ?: 0f
  val needle by
      animateFloatAsState(
          targetValue = target,
          animationSpec = if (animations) Motion.springy() else Motion.snap(),
          label = "wind_needle",
      )

  val ringColor = MaterialTheme.colorScheme.outlineVariant
  val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
  val needleColor = MaterialTheme.colorScheme.primary
  val hubColor = MaterialTheme.colorScheme.secondary

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      SectionHeader(title = stringResource(R.string.wind_compass_title))
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(modifier = Modifier.size(DIAL_SIZE), contentAlignment = Alignment.Center) {
          Canvas(modifier = Modifier.size(DIAL_SIZE)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            drawCircle(
                color = ringColor,
                radius = radius - 1.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx()),
            )
            drawCircle(
                color = ringColor.copy(alpha = 0.4f),
                radius = radius * 0.62f,
                style = Stroke(width = 1.dp.toPx()),
            )

            repeat(TICK_COUNT) { index ->
              val angle = index * (360f / TICK_COUNT)
              val major = index % (TICK_COUNT / 8) == 0
              val radians = Math.toRadians((angle - 90f).toDouble())
              val outer = radius - 5.dp.toPx()
              val inner = outer - (if (major) 9.dp.toPx() else 4.5.dp.toPx())
              drawLine(
                  color = if (major) tickColor else tickColor.copy(alpha = 0.5f),
                  start =
                      Offset(
                          center.x + cos(radians).toFloat() * inner,
                          center.y + sin(radians).toFloat() * inner,
                      ),
                  end =
                      Offset(
                          center.x + cos(radians).toFloat() * outer,
                          center.y + sin(radians).toFloat() * outer,
                      ),
                  strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx(),
                  cap = StrokeCap.Round,
              )
            }

            rotate(degrees = needle - 90f, pivot = center) {
              val arrow =
                  Path().apply {
                    moveTo(center.x, center.y - radius * 0.66f)
                    lineTo(center.x - radius * 0.17f, center.y + radius * 0.14f)
                    lineTo(center.x, center.y + radius * 0.02f)
                    lineTo(center.x + radius * 0.17f, center.y + radius * 0.14f)
                    close()
                  }
              drawPath(path = arrow, color = needleColor)
            }

            drawCircle(color = hubColor, radius = 5.dp.toPx(), center = center)
          }
          CompassLabel(text = "N", modifier = Modifier.align(Alignment.TopCenter))
          CompassLabel(text = "S", modifier = Modifier.align(Alignment.BottomCenter))
          CompassLabel(text = "W", modifier = Modifier.align(Alignment.CenterStart))
          CompassLabel(text = "E", modifier = Modifier.align(Alignment.CenterEnd))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
          WindReading(
              label = stringResource(R.string.metric_wind),
              value = formatWind(speed, 0),
          )
          WindReading(
              label = stringResource(R.string.metric_gusts),
              value = formatWind(gust, 0),
          )
          WindReading(
              label = stringResource(R.string.wind_compass_from),
              value = windDirectionLabel(directionDegrees, currentLocale()),
          )
        }
      }
    }
  }
}

@Composable
private fun CompassLabel(text: String, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
              .size(18.dp),
      contentAlignment = Alignment.Center,
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun WindReading(label: String, value: String) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

private val DIAL_SIZE = 118.dp
private const val TICK_COUNT = 32
