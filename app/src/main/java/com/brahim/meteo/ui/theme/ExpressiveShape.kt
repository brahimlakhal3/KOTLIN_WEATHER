package com.brahim.meteo.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * A continuous-corner "squircle" outline.
 *
 * A classic [androidx.compose.foundation.shape.RoundedCornerShape] always blends the corner arc
 * with the straight edge, which reads as a rectangle with cut-off corners. A superellipse instead
 * grows the corner curvature out of the edge itself, producing the softer, continuous silhouette
 * used by Pixel style surfaces.
 *
 * The corner radius supplied by the caller is used as the blend factor between a crisp rectangle
 * (radius `0`) and a full ellipse (radius = half of the shortest side). The [squareness] value
 * controls the default exponent: `2` is an ellipse, `4` a classic squircle and `10` almost a
 * rectangle.
 */
class SquircleShape(
    topStart: CornerSize,
    topEnd: CornerSize = topStart,
    bottomEnd: CornerSize = topStart,
    bottomStart: CornerSize = topStart,
    private val squareness: Float = DEFAULT_SQUARENESS,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

  override fun createOutline(
      size: Size,
      topStart: Float,
      topEnd: Float,
      bottomEnd: Float,
      bottomStart: Float,
      layoutDirection: LayoutDirection,
  ): Outline {
    val path = Path()
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return Outline.Generic(path)

    val radius = max(max(topStart, topEnd), max(bottomEnd, bottomStart))
    val shortest = min(width, height) / 2f
    val blend = if (shortest <= 0f) 0f else (radius / shortest).coerceIn(0f, 1f)
    val exponent = (squareness - (squareness - MIN_EXPONENT) * blend).coerceIn(MIN_EXPONENT, MAX_EXPONENT)
    val power = 2f / exponent

    val centerX = width / 2f
    val centerY = height / 2f
    val radiusX = width / 2f
    val radiusY = height / 2f

    var first = true
    for (step in 0..OUTLINE_STEPS) {
      val angle = 2f * Math.PI.toFloat() * step / OUTLINE_STEPS
      val cosAngle = cos(angle)
      val sinAngle = sin(angle)
      val x = centerX + radiusX * sign(cosAngle) * abs(cosAngle).pow(power)
      val y = centerY + radiusY * sign(sinAngle) * abs(sinAngle).pow(power)
      if (first) {
        path.moveTo(x, y)
        first = false
      } else {
        path.lineTo(x, y)
      }
    }
    path.close()
    return Outline.Generic(path)
  }

  override fun copy(
      topStart: CornerSize,
      topEnd: CornerSize,
      bottomEnd: CornerSize,
      bottomStart: CornerSize,
  ): CornerBasedShape =
      SquircleShape(
          topStart = topStart,
          topEnd = topEnd,
          bottomEnd = bottomEnd,
          bottomStart = bottomStart,
          squareness = squareness,
      )

  companion object {
    const val DEFAULT_SQUARENESS = 5f
    private const val MIN_EXPONENT = 2f
    private const val MAX_EXPONENT = 12f
    private const val OUTLINE_STEPS = 96
  }
}

/**
 * The Material 3 Expressive shape vocabulary, trimmed down to the silhouettes that read well on a
 * phone screen. Every entry maps to an official [MaterialShapes] polygon.
 */
enum class ExpressiveShape {
  COOKIE_4,
  COOKIE_9,
  COOKIE_12,
  GEM,
  BOOM,
  ARCH,
  PILL,
  FLOWER,
  PIXEL_CIRCLE,
  PUFFY,
  BURST,
  CLAM_SHELL,
  SUNNY,
  PENTAGON,
  TRIANGLE,
}

/** Resolves an [ExpressiveShape] into a drawable Compose [Shape]. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveShape.shape(): Shape =
    when (this) {
      ExpressiveShape.COOKIE_4 -> MaterialShapes.Cookie4Sided.toShape()
      ExpressiveShape.COOKIE_9 -> MaterialShapes.Cookie9Sided.toShape()
      ExpressiveShape.COOKIE_12 -> MaterialShapes.Cookie12Sided.toShape()
      ExpressiveShape.GEM -> MaterialShapes.Gem.toShape()
      ExpressiveShape.BOOM -> MaterialShapes.Boom.toShape()
      ExpressiveShape.ARCH -> MaterialShapes.Arch.toShape()
      ExpressiveShape.PILL -> MaterialShapes.Pill.toShape()
      ExpressiveShape.FLOWER -> MaterialShapes.Flower.toShape()
      ExpressiveShape.PIXEL_CIRCLE -> MaterialShapes.PixelCircle.toShape()
      ExpressiveShape.PUFFY -> MaterialShapes.Puffy.toShape()
      ExpressiveShape.BURST -> MaterialShapes.Burst.toShape()
      ExpressiveShape.CLAM_SHELL -> MaterialShapes.ClamShell.toShape()
      ExpressiveShape.SUNNY -> MaterialShapes.Sunny.toShape()
      ExpressiveShape.PENTAGON -> MaterialShapes.Pentagon.toShape()
      ExpressiveShape.TRIANGLE -> MaterialShapes.Triangle.toShape()
    }

/**
 * A stable per-index shape selection, so a row of cards can carry a lively silhouette mix without
 * looking random.
 */
private val SHAPE_CYCLE =
    listOf(
        ExpressiveShape.COOKIE_4,
        ExpressiveShape.GEM,
        ExpressiveShape.PILL,
        ExpressiveShape.BOOM,
        ExpressiveShape.PUFFY,
        ExpressiveShape.FLOWER,
        ExpressiveShape.COOKIE_9,
        ExpressiveShape.PIXEL_CIRCLE,
    )

/** Picks a deterministic expressive shape for the given [index]. */
@Composable
fun expressiveShapeFor(index: Int): Shape = SHAPE_CYCLE[index.mod(SHAPE_CYCLE.size)].shape()
