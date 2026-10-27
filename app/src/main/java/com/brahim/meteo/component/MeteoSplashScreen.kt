package com.brahim.meteo.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.element.WeatherIcon
import com.brahim.meteo.ui.theme.Motion
import kotlin.math.PI
import kotlin.math.sin

/**
 * The system splash prevents a blank launch frame; this lightweight overlay adds the expressive
 * motion while the first DataStore/forecast values are warming up underneath it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeteoSplashScreen(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    animations: Boolean = true,
) {
  val background = if (darkTheme) Color(0xFF0B0B0B) else Color(0xFFF1F1F1)
  val textColor = if (darkTheme) Color(0xFFF3F3F3) else Color(0xFF1D1D1D)
  val mutedColor = if (darkTheme) Color(0xFFB6B6B6) else Color(0xFF4A4A4A)
  val accent = if (darkTheme) Color(0xFF8FD3F4) else Color(0xFF3B6E8F)
  val transition = rememberInfiniteTransition(label = "splash")

  val pulse by
      transition.animateFloat(
          initialValue = 0.94f,
          targetValue = 1.04f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis = 1_250, easing = Motion.emphasized),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "splash_pulse",
      )
  val rotation by
      transition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis = 14_000, easing = LinearEasing),
              ),
          label = "splash_rotation",
      )
  val dotPhase by
      transition.animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis = 1_300, easing = LinearEasing),
              ),
          label = "splash_dots",
      )

  Box(
      modifier = modifier.fillMaxSize().background(background),
      contentAlignment = Alignment.Center,
  ) {
    Box(
        modifier =
            Modifier.size(250.dp)
                .offset(x = 120.dp, y = (-210).dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (darkTheme) 0.10f else 0.08f)),
    )
    Box(
        modifier =
            Modifier.size(170.dp)
                .offset(x = (-130).dp, y = 230.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (darkTheme) 0.08f else 0.06f)),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Box(
          modifier =
              Modifier.size(156.dp)
                  .graphicsLayer {
                    scaleX = if (animations) pulse else 1f
                    scaleY = if (animations) pulse else 1f
                    rotationZ = if (animations) rotation * 0.06f else 0f
                  }
                  .clip(MaterialShapes.Cookie9Sided.toShape())
                  .background(
                      Brush.linearGradient(
                          listOf(
                              accent.copy(alpha = 0.98f),
                              accent.copy(alpha = 0.58f),
                          )
                      )
                  ),
          contentAlignment = Alignment.Center,
      ) {
        WeatherIcon(
            weatherCode = 2,
            isDay = true,
            size = 86.dp,
            tint = Color.White,
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
      Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineLarge,
          color = textColor,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = stringResource(R.string.app_tagline),
          style = MaterialTheme.typography.bodyMedium,
          color = mutedColor,
          textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(22.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
          val wave = ((sin(dotPhase.toDouble() * 2.0 * PI - index * 0.8) + 1.0) / 2.0).toFloat()
          val alpha = if (animations) 0.25f + 0.75f * wave else 0.55f
          Box(
              modifier =
                  Modifier.size(7.dp)
                      .clip(CircleShape)
                      .background(accent.copy(alpha = alpha)),
          )
        }
      }
    }
  }
}
