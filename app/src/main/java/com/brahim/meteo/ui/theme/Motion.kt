package com.brahim.meteo.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object Motion {

  const val SHORT_1 = 100
  const val SHORT_2 = 160
  const val MEDIUM_1 = 240
  const val MEDIUM_2 = 340
  const val LONG_1 = 480
  const val LONG_2 = 720

  val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

  val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

  val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

  val press: FiniteAnimationSpec<Float> =
      spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessHigh, visibilityThreshold = 0.001f)

  val settle: FiniteAnimationSpec<Float> =
      spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium, visibilityThreshold = 0.001f)

  val gentle: FiniteAnimationSpec<Float> =
      spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

  /** A springy, slightly bouncy spec used for playful affordances such as the compass needle. */
  val bouncy: FiniteAnimationSpec<Float> =
      spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

  /** A fast, non-bouncy spring used when a value should settle without wobbling. */
  val crisp: FiniteAnimationSpec<Float> =
      spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)

  fun <T> springy(visibilityThreshold: T? = null): SpringSpec<T> =
      spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
          visibilityThreshold = visibilityThreshold,
      )

  fun <T> snap(): FiniteAnimationSpec<T> = tween(0)

  fun <T> gentleSpring(visibilityThreshold: T? = null): SpringSpec<T> =
      spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow,
          visibilityThreshold = visibilityThreshold,
      )

  fun <T> enter(durationMillis: Int = MEDIUM_1): FiniteAnimationSpec<T> =
      tween(durationMillis, easing = emphasizedDecelerate)

  fun <T> exit(durationMillis: Int = SHORT_2): FiniteAnimationSpec<T> =
      tween(durationMillis, easing = emphasizedAccelerate)

  fun <T> standard(durationMillis: Int = MEDIUM_1): FiniteAnimationSpec<T> =
      tween(durationMillis, easing = emphasized)

  fun <T> stagger(index: Int, step: Int = 40, durationMillis: Int = MEDIUM_2): FiniteAnimationSpec<T> =
      tween(durationMillis + index * step, easing = emphasizedDecelerate)

  /** A long, linear loop used for ambient motion such as drifting clouds or falling rain. */
  fun <T> drift(durationMillis: Int = 12_000): FiniteAnimationSpec<T> =
      tween(durationMillis, easing = androidx.compose.animation.core.LinearEasing)
}
