package com.brahim.meteo.element

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    metrics.chunked(METRICS_PER_ROW).forEach { row ->
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        row.forEach { metric ->
          MetricCard(
              metric = metric,
              onClick = { onMetricClick(metric) },
              modifier = Modifier.weight(1f),
          )
        }
        if (row.size < METRICS_PER_ROW) Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun MetricCard(metric: Metric, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
      onClick = onClick,
      modifier = modifier,
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        IconBadge(icon = metric.icon, badgeSize = 32.dp, iconSize = 18.dp)
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
) {
  Surface(
      modifier = modifier.size(badgeSize),
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surfaceContainerHighest,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Box(contentAlignment = Alignment.Center) {
      IconView(icon = icon, size = iconSize)
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

private const val METRICS_PER_ROW = 2
