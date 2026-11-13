package com.brahim.meteo.element

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion

/**
 * The headline of the forecast: the current temperature, the condition and the day's range, floating
 * on top of the living sky.
 *
 * @param weatherCode the WMO weather interpretation code reported by Open-Meteo.
 * @param isDay whether the location is currently in daylight.
 * @param temperature the current temperature in degrees Celsius.
 * @param condition the translated name of the current condition.
 * @param location the name of the place.
 * @param highLow the formatted "H 18° · L 9°" range.
 * @param feelsLike the formatted "feels like" value.
 * @param modifier applied to the card.
 */
@Composable
fun HeroWeatherCard(
    weatherCode: Int?,
    isDay: Boolean,
    temperature: Double,
    condition: String,
    location: String,
    highLow: String?,
    feelsLike: String?,
    modifier: Modifier = Modifier,
) {
  val animations = LocalAnimationsEnabled.current
  val pulse: Float
  if (animations) {
    val transition = rememberInfiniteTransition(label = "hero")
    val value by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(4_600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "hero_pulse",
        )
    pulse = value
  } else {
    pulse = 0.5f
  }

  val glass = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
  val border = Color.White.copy(alpha = 0.30f)
  val content = Color.White

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.extraLarge,
      color = glass,
      contentColor = content,
      border = BorderStroke(1.dp, border),
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = location,
            style = MaterialTheme.typography.titleMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Box(
            modifier =
                Modifier.size(86.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.10f + 0.10f * pulse),
                                    Color.Transparent,
                                )
                        )
                    ),
            contentAlignment = Alignment.Center,
        ) {
          WeatherIcon(
              weatherCode = weatherCode,
              isDay = isDay,
              size = 62.dp,
              tint = content,
          )
        }
      }
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        AnimatedTemperature(
            celsius = temperature,
            style = MaterialTheme.typography.displayMedium,
            color = content,
        )
        Text(
            text = condition,
            style = MaterialTheme.typography.titleMedium,
            color = content.copy(alpha = 0.92f),
            modifier = Modifier.padding(bottom = 10.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
      }
      val chips = listOfNotNull(highLow, feelsLike)
      if (chips.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
          chips.forEach { chip ->
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f),
                contentColor = content,
            ) {
              Text(
                  text = chip,
                  style = MaterialTheme.typography.labelLarge,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}

/** The motion used by the hero icon. Kept public so previews can reuse it. */
val HeroPulseDuration = Motion.LONG_2
