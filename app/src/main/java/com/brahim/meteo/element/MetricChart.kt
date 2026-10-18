package com.brahim.meteo.element

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brahim.meteo.ui.theme.Motion
import kotlin.math.abs
import kotlin.math.roundToInt

data class ChartPoint(val label: String, val value: Float, val valueText: String)

@Composable
fun MetricChart(points: List<ChartPoint>, modifier: Modifier = Modifier, height: Dp = 152.dp) {
  val lineColor = MaterialTheme.colorScheme.primary
  val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
  val gridColor = MaterialTheme.colorScheme.outlineVariant
  val haloColor = MaterialTheme.colorScheme.surface
  val tooltipColor = MaterialTheme.colorScheme.inverseSurface
  val tooltipContent = MaterialTheme.colorScheme.inverseOnSurface
  val hourStyle =
      MaterialTheme.typography.labelSmall.copy(color = tooltipContent.copy(alpha = 0.75f))
  val valueStyle = MaterialTheme.typography.titleSmall.copy(color = tooltipContent)
  val textMeasurer = rememberTextMeasurer()
  val progress = remember { Animatable(0f) }
  var selected by remember(points) { mutableStateOf<Int?>(null) }

  LaunchedEffect(points) {
    progress.snapTo(0f)
    progress.animateTo(1f, Motion.standard(Motion.LONG_1))
  }

  Canvas(
      modifier =
          modifier
              .fillMaxWidth()
              .height(height)
              .pointerInput(points) {
                awaitEachGesture {
                  if (points.size < 2) return@awaitEachGesture
                  val step = size.width / (points.size - 1).toFloat()
                  val down = awaitFirstDown(requireUnconsumed = false)
                  selected = (down.position.x / step).roundToInt().coerceIn(0, points.lastIndex)
                  while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) break
                    selected =
                        (change.position.x / step).roundToInt().coerceIn(0, points.lastIndex)
                    val delta = change.positionChange()
                    if (abs(delta.x) >= abs(delta.y)) change.consume()
                  }
                }
              }
  ) {
    if (points.size < 2) return@Canvas

    val values = points.map { point -> point.value }
    val lowest = values.min()
    val highest = values.max()
    val span = (highest - lowest).takeIf { value -> value > 0.01f } ?: 1f
    val step = size.width / (points.size - 1).toFloat()
    val padding = 10.dp.toPx()
    val verticalSpan = size.height - padding * 2

    val offsets =
        points.indices.map { index ->
          val fraction = (values[index] - lowest) / span
          val target = padding + (1f - fraction) * verticalSpan
          Offset(
              x = step * index,
              y = size.height - (size.height - target) * progress.value,
          )
        }

    val line = Path().apply {
      moveTo(offsets.first().x, offsets.first().y)
      for (index in 1 until offsets.size) {
        val previous = offsets[index - 1]
        val current = offsets[index]
        val middle = Offset((previous.x + current.x) / 2f, (previous.y + current.y) / 2f)
        quadraticTo(previous.x, previous.y, middle.x, middle.y)
      }
      lineTo(offsets.last().x, offsets.last().y)
    }

    val area = Path().apply {
      addPath(line)
      lineTo(offsets.last().x, size.height)
      lineTo(offsets.first().x, size.height)
      close()
    }

    val hairline = 1.dp.toPx()
    drawLine(
        color = gridColor,
        start = Offset(0f, hairline / 2f),
        end = Offset(size.width, hairline / 2f),
        strokeWidth = hairline,
    )
    drawLine(
        color = gridColor,
        start = Offset(0f, size.height - hairline / 2f),
        end = Offset(size.width, size.height - hairline / 2f),
        strokeWidth = hairline,
    )

    drawPath(path = area, color = fillColor)
    drawPath(
        path = line,
        color = lineColor,
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    offsets.forEachIndexed { index, offset ->
      if (index % 2 == 0 || index == offsets.lastIndex) {
        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = offset)
      }
    }

    val selectedIndex = selected ?: return@Canvas
    if (selectedIndex !in offsets.indices) return@Canvas
    val offset = offsets[selectedIndex]
    val point = points[selectedIndex]

    drawLine(
        color = gridColor,
        start = Offset(offset.x, 0f),
        end = Offset(offset.x, size.height),
        strokeWidth = hairline,
    )
    drawCircle(color = haloColor, radius = 8.dp.toPx(), center = offset)
    drawCircle(color = lineColor, radius = 5.dp.toPx(), center = offset)

    val hourLayout = textMeasurer.measure(point.label, hourStyle)
    val valueLayout = textMeasurer.measure(point.valueText, valueStyle)
    val padH = 12.dp.toPx()
    val padV = 8.dp.toPx()
    val gap = 2.dp.toPx()
    val boxWidth = maxOf(hourLayout.size.width, valueLayout.size.width).toFloat() + padH * 2
    val boxHeight =
        (hourLayout.size.height + valueLayout.size.height).toFloat() + gap + padV * 2
    val centerX =
        if (boxWidth >= size.width) size.width / 2f
        else offset.x.coerceIn(boxWidth / 2f, size.width - boxWidth / 2f)
    val top = (offset.y - boxHeight - 14.dp.toPx()).coerceAtLeast(0f)
    val left = centerX - boxWidth / 2f

    drawRoundRect(
        color = tooltipColor,
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(12.dp.toPx()),
    )
    drawText(hourLayout, topLeft = Offset(centerX - hourLayout.size.width / 2f, top + padV))
    drawText(
        valueLayout,
        topLeft = Offset(
            centerX - valueLayout.size.width / 2f,
            top + padV + hourLayout.size.height + gap,
        ),
    )
  }
}
