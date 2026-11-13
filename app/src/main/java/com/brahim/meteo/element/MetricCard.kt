package com.brahim.meteo.element

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brahim.meteo.ui.theme.ExpressiveShape
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.ui.theme.expressiveShapeFor
import kotlin.math.abs

sealed interface AppIcon {
  data class Drawable(@param:DrawableRes val res: Int) : AppIcon

  data class Vector(val image: ImageVector) : AppIcon
}

data class Metric(
    val icon: AppIcon,
    val label: String,
    val value: String,
    val caption: String? = null,
    val dayMetric: DayMetric? = null,
)

@Composable
fun MetricGrid(
    metrics: List<Metric>,
    onMetricClick: (Metric) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    metrics.chunked(METRICS_PER_ROW).forEachIndexed { rowIndex, row ->
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        row.forEachIndexed { columnIndex, metric ->
          val index = rowIndex * METRICS_PER_ROW + columnIndex
          Reveal(index = index, modifier = Modifier.weight(1f)) {
            MetricCard(
                metric = metric,
                index = index,
                onClick = { onMetricClick(metric) },
            )
          }
        }
        if (row.size < METRICS_PER_ROW) Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun MetricCard(
    metric: Metric,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
) {
  val animations = LocalAnimationsEnabled.current
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by
      animateFloatAsState(
          targetValue = if (pressed) PRESSED_SCALE else 1f,
          animationSpec = if (animations) Motion.press else Motion.snap(),
          label = "metric_press",
      )

  val palette = abs(metric.label.hashCode()) % 3
  val badgeContainer =
      when (palette) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
      }
  val badgeContent =
      when (palette) {
        0 -> MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
      }

  Surface(
      onClick = onClick,
      modifier =
          modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
          },
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
      interactionSource = interactionSource,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        IconBadge(
            icon = metric.icon,
            badgeSize = 32.dp,
            iconSize = 18.dp,
            containerColor = badgeContainer,
            contentColor = badgeContent,
            shape = expressiveShapeFor(index),
        )
        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
          text = metric.value,
          style = MaterialTheme.typography.titleLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
      metric.caption?.let { caption ->
        Text(
            text = caption,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun IconBadge(
    icon: AppIcon,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 42.dp,
    iconSize: Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = CircleShape,
) {
  Surface(
      modifier = modifier.size(badgeSize),
      shape = shape,
      color = containerColor,
      contentColor = contentColor,
  ) {
    Box(contentAlignment = Alignment.Center) {
      IconView(icon = icon, size = iconSize, tint = contentColor)
    }
  }
}

@Composable
fun IconView(
    icon: AppIcon,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  when (icon) {
    is AppIcon.Drawable ->
        Icon(
            painter = painterResource(icon.res),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size),
        )
    is AppIcon.Vector ->
        Icon(
            imageVector = icon.image,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size),
        )
  }
}

/** The silhouette used by the badge of the first tile of the conditions grid. */
val PrimaryBadgeShape = ExpressiveShape.COOKIE_4

private const val METRICS_PER_ROW = 2
private const val PRESSED_SCALE = 0.965f
