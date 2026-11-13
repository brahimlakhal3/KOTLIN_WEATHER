package com.brahim.meteo.element

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.util.formatHour
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay

/**
 * The path of the sun across the sky, drawn as an arc between sunrise and sunset.
 *
 * The marker slides along the arc to the current time, which makes the length of the day and the
 * time left before sunset readable at a glance.
 *
 * @param day today's forecast, used for the sunrise and sunset instants.
 * @param modifier applied to the card.
 */
@Composable
fun SolarArcCard(
    day: DailyForecast?,
    modifier: Modifier = Modifier,
) {
  val animations = LocalAnimationsEnabled.current
  var now by remember { mutableStateOf(Instant.now()) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(CLOCK_TICK_MILLIS)
      now = Instant.now()
    }
  }

  val sunrise = day?.sunrise
  val sunset = day?.sunset
  if (sunrise == null || sunset == null || !sunset.isAfter(sunrise)) return

  val totalMillis = Duration.between(sunrise, sunset).toMillis().coerceAtLeast(1L)
  val progress =
      when {
        now.isBefore(sunrise) -> 0f
        now.isAfter(sunset) -> 1f
        else -> (Duration.between(sunrise, now).toMillis().toFloat() / totalMillis).coerceIn(0f, 1f)
      }

  val animatedProgress by
      animateFloatAsState(
          targetValue = progress,
          animationSpec = if (animations) Motion.settle else Motion.snap(),
          label = "solar_progress",
      )

  val sunriseLabel = formatHour(sunrise, day.utcOffsetSeconds)
  val sunsetLabel = formatHour(sunset, day.utcOffsetSeconds)
  val daylight = Duration.between(sunrise, sunset)
  val daylightLabel =
      stringResource(
          R.string.sun_moon_hours,
          daylight.toHours().toInt().coerceAtLeast(0),
          (daylight.toMinutes() % 60).toInt().coerceAtLeast(0),
      )
  val isDaytime = !now.isBefore(sunrise) && !now.isAfter(sunset)

  val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
  val horizonColor = MaterialTheme.colorScheme.outlineVariant
  val arcColors =
      listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
  val sunColor =
      if (isDaytime) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline

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
      SectionHeader(title = stringResource(R.string.solar_arc_title))
      Canvas(modifier = Modifier.fillMaxWidth().height(ARC_HEIGHT)) {
        val horizon = size.height * 0.82f
        val control = Offset(size.width / 2f, horizon - size.height * 1.55f)
        val start = Offset(0f, horizon)
        val end = Offset(size.width, horizon)

        val arcPath =
            Path().apply {
              moveTo(start.x, start.y)
              quadraticBezierTo(control.x, control.y, end.x, end.y)
            }

        drawPath(
            path = arcPath,
            color = trackColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        val travelled = quadraticPoint(start, control, end, animatedProgress)
        val travelledPath =
            Path().apply {
              moveTo(start.x, start.y)
              quadraticBezierTo(control.x, control.y, travelled.x, travelled.y)
            }
        drawPath(
            path = travelledPath,
            brush = Brush.horizontalGradient(arcColors),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )

        drawLine(
            color = horizonColor,
            start = Offset(0f, horizon),
            end = Offset(size.width, horizon),
            strokeWidth = 1.dp.toPx(),
        )

        drawCircle(
            color = sunColor.copy(alpha = 0.22f),
            radius = 13.dp.toPx(),
            center = travelled,
        )
        drawCircle(color = sunColor, radius = 6.dp.toPx(), center = travelled)
      }
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        SolarPillar(
            icon = Icons.Outlined.WbTwilight,
            label = stringResource(R.string.sun_rise),
            value = sunriseLabel,
        )
        Text(
            text = stringResource(R.string.sun_moon_daylight, daylightLabel),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SolarPillar(
            icon = Icons.Outlined.NightsStay,
            label = stringResource(R.string.sun_set),
            value = sunsetLabel,
        )
      }
    }
  }
}

@Composable
private fun SolarPillar(icon: ImageVector, label: String, value: String) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
        modifier =
            Modifier.size(26.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(15.dp),
      )
    }
    Column {
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
          text = value,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** Evaluates a quadratic Bézier curve at [t]. */
private fun quadraticPoint(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
  val inverse = 1f - t
  return Offset(
      x = inverse * inverse * p0.x + 2f * inverse * t * p1.x + t * t * p2.x,
      y = inverse * inverse * p0.y + 2f * inverse * t * p1.y + t * t * p2.y,
  )
}

private val ARC_HEIGHT = 132.dp
private const val CLOCK_TICK_MILLIS = 60_000L
