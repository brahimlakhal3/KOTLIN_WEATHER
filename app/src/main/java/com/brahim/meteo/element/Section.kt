package com.brahim.meteo.element

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
        modifier =
            Modifier.width(5.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
    )
    if (trailing != null) {
      Text(
          text = trailing,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun AnimatedValue(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
  if (!LocalAnimationsEnabled.current) {
    Text(text = text, style = style, modifier = modifier, color = color)
    return
  }
  AnimatedContent(
      targetState = text,
      transitionSpec = {
        (fadeIn(Motion.enter(Motion.SHORT_2)) +
                slideInVertically(Motion.enter(Motion.SHORT_2)) { height -> -height / 2 }) togetherWith
            (fadeOut(Motion.exit()) +
                slideOutVertically(Motion.exit()) { height -> height / 2 })
      },
      label = "value",
      modifier = modifier,
  ) { value ->
    Text(text = value, style = style, color = color)
  }
}

@Composable
fun Reveal(modifier: Modifier = Modifier, index: Int = 0, content: @Composable () -> Unit) {
  if (!LocalAnimationsEnabled.current) {
    androidx.compose.foundation.layout.Box(modifier = modifier) { content() }
    return
  }
  var visible by remember { mutableStateOf(false) }
  val slide: FiniteAnimationSpec<IntOffset> = Motion.stagger(index)
  val fade: FiniteAnimationSpec<Float> = Motion.stagger(index)

  LaunchedEffect(Unit) { visible = true }

  AnimatedVisibility(
      visible = visible,
      enter = fadeIn(fade) + slideInVertically(slide) { height -> height / 8 },
      exit = fadeOut(Motion.exit()),
      modifier = modifier,
  ) {
    content()
  }
}
