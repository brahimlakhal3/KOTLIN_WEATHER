package com.brahim.meteo.element

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.brahim.meteo.ui.theme.Celestial
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.ui.theme.SkyParticle
import com.brahim.meteo.ui.theme.skyPalette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * The living sky behind the forecast.
 *
 * Google Pixel style weather apps colour the whole page with the current sky and let it breathe with
 * the conditions: drifting clouds, falling rain, twinkling stars, a glowing sun or moon. The
 * gradient itself cross-fades whenever the weather changes, so refreshing the forecast morphs the
 * page instead of cutting to a new picture.
 *
 * @param weatherCode the WMO weather interpretation code reported by Open-Meteo.
 * @param isDay whether the location is currently in daylight.
 * @param modifier applied to the backdrop container.
 * @param content drawn on top of the sky.
 */
@Composable
fun WeatherBackdrop(
    weatherCode: Int?,
    isDay: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
  val palette = skyPalette(weatherCode, isDay)
  val animations = LocalAnimationsEnabled.current

  val colors =
      palette.colors.mapIndexed { index, color ->
        animateColorAsState(
            targetValue = color,
            animationSpec = Motion.standard(Motion.LONG_2),
            label = "sky_color_$index",
        )
      }

  val phase: Float
  val pulse: Float
  if (animations) {
    val transition = rememberInfiniteTransition(label = "sky")
    val drifting by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(DRIFT_MILLIS, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "drift",
        )
    val breathing by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(BREATH_MILLIS, easing = Motion.emphasized),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "breath",
        )
    phase = drifting
    pulse = breathing
  } else {
    phase = 0f
    pulse = 0.5f
  }

  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawRect(
          brush = Brush.verticalGradient(colors.map { state -> state.value }),
          size = size,
      )
      drawCelestial(palette.celestial, palette.particleColor, pulse)
      when (palette.particle) {
        SkyParticle.NONE -> Unit
        SkyParticle.CLOUDS -> drawClouds(palette.particleColor, phase)
        SkyParticle.RAIN -> drawRain(palette.particleColor, phase)
        SkyParticle.STORM -> {
          drawRain(palette.particleColor, phase)
          drawLightning(palette.particleColor, phase)
        }
        SkyParticle.SNOW -> drawSnow(palette.particleColor, phase)
        SkyParticle.STARS -> drawStars(palette.particleColor, phase)
        SkyParticle.FOG -> drawFog(palette.particleColor, phase)
      }
      drawVignette(backgroundColor)
    }
    content()
  }
}

private fun DrawScope.drawCelestial(
    celestial: Celestial,
    color: Color,
    pulse: Float,
) {
  if (celestial == Celestial.NONE) return
  val center = Offset(size.width * 0.78f, size.height * 0.14f)
  val radius = size.minDimension * (0.20f + 0.03f * pulse)
  drawCircle(
      brush =
          Brush.radialGradient(
              colors = listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0f)),
              center = center,
              radius = radius * 2.6f,
          ),
      radius = radius * 2.6f,
      center = center,
  )
  drawCircle(color = color.copy(alpha = 0.75f), radius = radius * 0.42f, center = center)
}

private fun DrawScope.drawClouds(color: Color, phase: Float) {
  val drift = phase * size.width
  repeat(CLOUD_COUNT) { index ->
    val seed = pseudoRandom(index)
    val x = ((seed * size.width) + drift * (0.25f + seed * 0.6f)) % (size.width * 1.4f) -
        size.width * 0.2f
    val y = size.height * (0.04f + pseudoRandom(index + 91) * 0.42f)
    val radius = size.minDimension * (0.16f + pseudoRandom(index + 17) * 0.20f)
    drawCircle(
        brush =
            Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(x, y),
                radius = radius,
            ),
        radius = radius,
        center = Offset(x, y),
    )
  }
}

private fun DrawScope.drawRain(color: Color, phase: Float) {
  val length = size.minDimension * 0.06f
  repeat(RAIN_COUNT) { index ->
    val seed = pseudoRandom(index)
    val speed = 0.55f + pseudoRandom(index + 31) * 0.85f
    val x = pseudoRandom(index + 57) * size.width
    val travelled = (phase * speed * (size.height + length * 4f) + index * 53f)
    val y = travelled % (size.height + length * 4f) - length * 2f
    drawLine(
        color = color.copy(alpha = 0.32f),
        start = Offset(x, y),
        end = Offset(x - length * 0.32f, y + length),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round,
    )
  }
}

private fun DrawScope.drawSnow(color: Color, phase: Float) {
  repeat(SNOW_COUNT) { index ->
    val seed = pseudoRandom(index)
    val speed = 0.18f + pseudoRandom(index + 23) * 0.30f
    val x = pseudoRandom(index + 71) * size.width
    val sway = sin(phase * TWO_PI + index) * size.minDimension * 0.035f
    val travelled = phase * speed * (size.height + 60f) + index * 71f
    val y = travelled % (size.height + 60f) - 30f
    val radius = 1.2f + pseudoRandom(index + 13) * 2.4f
    drawCircle(
        color = color.copy(alpha = 0.72f),
        radius = radius,
        center = Offset(x + sway, y),
    )
  }
}

private fun DrawScope.drawStars(color: Color, phase: Float) {
  repeat(STAR_COUNT) { index ->
    val seed = pseudoRandom(index)
    val x = pseudoRandom(index + 5) * size.width
    val y = pseudoRandom(index + 101) * size.height * 0.72f
    val twinkle = 0.30f + 0.70f * abs(sin(phase * TWO_PI + index * 1.7f))
    val radius = 0.8f + pseudoRandom(index + 41) * 1.7f
    drawCircle(
        color = color.copy(alpha = 0.85f * twinkle),
        radius = radius,
        center = Offset(x, y),
    )
  }
}

private fun DrawScope.drawFog(color: Color, phase: Float) {
  repeat(FOG_BAND_COUNT) { index ->
    val seed = pseudoRandom(index)
    val bandHeight = size.height * 0.16f
    val y = size.height * (0.35f + seed * 0.6f) +
        sin(phase * TWO_PI + index) * size.height * 0.02f
    val offset = (phase * size.width * (0.10f + seed * 0.12f)) % size.width
    drawRect(
        brush =
            Brush.horizontalGradient(
                colors = listOf(Color.Transparent, color.copy(alpha = 0.16f), Color.Transparent),
                startX = -offset,
                endX = size.width - offset,
            ),
        topLeft = Offset(0f, y),
        size = Size(size.width, bandHeight),
    )
  }
}

private fun DrawScope.drawLightning(color: Color, phase: Float) {
  val cycle = phase * 3f
  if (cycle % 1f > FLASH_WINDOW) return
  val fade = 1f - (cycle % 1f) / FLASH_WINDOW
  drawRect(
      color = color.copy(alpha = 0.16f * fade),
      size = size,
  )
}

/**
 * Fades the sky into the page background at the top and the bottom, so the gradient never ends on a
 * hard edge against the app bar or the lower cards.
 */
private fun DrawScope.drawVignette(backgroundColor: Color) {
  drawRect(
      brush =
          Brush.verticalGradient(
              colors =
                  listOf(
                      backgroundColor.copy(alpha = 0.92f),
                      backgroundColor.copy(alpha = 0.35f),
                      Color.Transparent,
                  ),
              startY = 0f,
              endY = size.height * 0.18f,
          ),
      size = size,
  )
  drawRect(
      brush =
          Brush.verticalGradient(
              colors =
                  listOf(
                      Color.Transparent,
                      backgroundColor.copy(alpha = 0.40f),
                      backgroundColor.copy(alpha = 0.96f),
                  ),
              startY = size.height * 0.52f,
              endY = size.height,
          ),
      size = size,
  )
}

/** A cheap deterministic pseudo random value in `[0, 1)` derived from [seed]. */
private fun pseudoRandom(seed: Int): Float {
  val x = sin(seed * 12.9898f) * 43758.5453f
  return x - kotlin.math.floor(x)
}

private const val DRIFT_MILLIS = 18_000
private const val BREATH_MILLIS = 4_200
private const val CLOUD_COUNT = 7
private const val RAIN_COUNT = 52
private const val SNOW_COUNT = 46
private const val STAR_COUNT = 64
private const val FOG_BAND_COUNT = 4
private const val FLASH_WINDOW = 0.12f
private const val TWO_PI = 6.2831855f

/** Kept for callers that want to align the backdrop with the system bars. */
val BackdropTopInset = 0.dp
