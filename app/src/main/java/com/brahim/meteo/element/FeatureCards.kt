package com.brahim.meteo.element

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.ui.currentLocale
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.util.Advice
import com.brahim.meteo.util.AdviceKind
import com.brahim.meteo.util.AlertKind
import com.brahim.meteo.util.AlertSeverity
import com.brahim.meteo.util.MoonInfo
import com.brahim.meteo.util.WeatherAlert
import com.brahim.meteo.util.formatHour
import com.brahim.meteo.util.formatNumber
import com.brahim.meteo.util.formatPrecipitation
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.formatWind
import com.brahim.meteo.util.moonInfo
import com.brahim.meteo.util.weatherAdvice
import java.time.Duration
import kotlin.math.roundToInt

@Composable
fun WeatherAlertsCard(alerts: List<WeatherAlert>, modifier: Modifier = Modifier) {
  if (alerts.isEmpty()) return
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
      SectionHeader(title = stringResource(R.string.alerts_title))
      alerts.forEach { alert ->
        val container =
            if (alert.severity == AlertSeverity.SEVERE) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.tertiaryContainer
        val content =
            if (alert.severity == AlertSeverity.SEVERE) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onTertiaryContainer
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(container)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
              imageVector = alert.kind.icon,
              contentDescription = null,
              tint = content,
              modifier = Modifier.size(22.dp),
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(alert.kind.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = content,
            )
            Text(
                text = stringResource(alert.kind.messageRes, alertValue(alert)),
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.82f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun alertValue(alert: WeatherAlert): String {
  val value = alert.value ?: return ""
  return when (alert.kind) {
    AlertKind.HEAT,
    AlertKind.COLD -> formatTemperature(value)
    AlertKind.WIND -> formatWind(value, 0)
    AlertKind.RAIN,
    AlertKind.SNOW -> formatPrecipitation(value, 1)
    AlertKind.UV -> value.formatNumber(0, currentLocale())
    AlertKind.THUNDERSTORM,
    AlertKind.FOG -> ""
  }
}

private val AlertKind.icon: ImageVector
  get() =
      when (this) {
        AlertKind.HEAT -> Icons.Outlined.WbSunny
        AlertKind.COLD -> Icons.Outlined.AcUnit
        AlertKind.WIND -> Icons.Outlined.Air
        AlertKind.RAIN -> Icons.Outlined.WaterDrop
        AlertKind.SNOW -> Icons.Outlined.Cloud
        AlertKind.UV -> Icons.Outlined.LightMode
        AlertKind.THUNDERSTORM -> Icons.Outlined.Thunderstorm
        AlertKind.FOG -> Icons.Outlined.BlurOn
      }

@Composable
fun SmartAdviceCard(
    hourly: List<HourlyForecast>,
    daily: List<DailyForecast>,
    modifier: Modifier = Modifier,
) {
  val advice = weatherAdvice(hourly, daily)
  if (advice.isEmpty()) return
  val animations = LocalAnimationsEnabled.current
  val featured = advice.first()

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
        modifier = Modifier.padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      SectionHeader(
          title = stringResource(R.string.advice_title),
          trailing = stringResource(R.string.advice_tip_count, advice.size),
          modifier = Modifier.padding(horizontal = 14.dp),
      )
      AnimatedContent(
          targetState = featured,
          transitionSpec = {
            if (!animations) {
              fadeIn(tween(0)) togetherWith fadeOut(tween(0))
            } else {
              (fadeIn(Motion.enter(Motion.MEDIUM_1)) +
                      scaleIn(Motion.enter(Motion.MEDIUM_1), initialScale = 0.96f)) togetherWith
                  (fadeOut(Motion.exit()) +
                      scaleOut(Motion.exit(), targetScale = 0.98f))
            }
          },
          label = "featured_advice",
          modifier = Modifier.padding(horizontal = 14.dp),
      ) { item ->
        FeaturedAdvice(item = item)
      }
      if (advice.size > 1) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          itemsIndexed(
              items = advice.drop(1),
              key = { _, item -> item.kind.name },
          ) { index, item ->
            AdviceCard(
                item = item,
                index = index,
                modifier = Modifier.width(178.dp).animateItem(),
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FeaturedAdvice(item: Advice, modifier: Modifier = Modifier) {
  val container = MaterialTheme.colorScheme.primaryContainer
  val content = MaterialTheme.colorScheme.onPrimaryContainer
  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = container,
      contentColor = content,
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
          modifier =
              Modifier.size(52.dp)
                  .clip(MaterialShapes.Cookie4Sided.toShape())
                  .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = item.kind.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(27.dp),
        )
      }
      Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Text(
            text = stringResource(item.kind.titleRes),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = adviceDetail(item),
            style = MaterialTheme.typography.bodyMedium,
            color = content.copy(alpha = 0.8f),
        )
      }
    }
  }
}

@Composable
private fun AdviceCard(item: Advice, index: Int, modifier: Modifier = Modifier) {
  val (container, content) =
      when (index % 3) {
        0 ->
            MaterialTheme.colorScheme.secondaryContainer to
                MaterialTheme.colorScheme.onSecondaryContainer
        1 ->
            MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
        else ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
      }
  Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      color = container,
      contentColor = content,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
          imageVector = item.kind.icon,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
      )
      Text(
          text = stringResource(item.kind.titleRes),
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
      )
      Text(
          text = adviceDetail(item),
          style = MaterialTheme.typography.bodySmall,
          color = content.copy(alpha = 0.78f),
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun adviceDetail(item: Advice): String {
  val value = item.value ?: 0.0
  return when (item.kind) {
    AdviceKind.UMBRELLA -> stringResource(item.kind.detailRes, value.roundToInt())
    AdviceKind.JACKET,
    AdviceKind.LAYERS,
    AdviceKind.HEAT,
    AdviceKind.HYDRATE -> stringResource(item.kind.detailRes, formatTemperature(value))
    AdviceKind.SUNSCREEN ->
        stringResource(item.kind.detailRes, value.formatNumber(0, currentLocale()))
    AdviceKind.WIND -> stringResource(item.kind.detailRes, formatWind(value, 0))
    AdviceKind.HUMIDITY -> stringResource(item.kind.detailRes, value.roundToInt())
    AdviceKind.STORM,
    AdviceKind.SNOW,
    AdviceKind.VISIBILITY,
    AdviceKind.COMFORT -> stringResource(item.kind.detailRes)
  }
}

private val AdviceKind.icon: ImageVector
  get() =
      when (this) {
        AdviceKind.UMBRELLA -> Icons.Outlined.WaterDrop
        AdviceKind.STORM -> Icons.Outlined.Thunderstorm
        AdviceKind.SNOW -> Icons.Outlined.AcUnit
        AdviceKind.JACKET,
        AdviceKind.LAYERS -> Icons.Outlined.AcUnit
        AdviceKind.HEAT -> Icons.Outlined.WbSunny
        AdviceKind.SUNSCREEN -> Icons.Outlined.LightMode
        AdviceKind.WIND -> Icons.Outlined.Air
        AdviceKind.VISIBILITY -> Icons.Outlined.BlurOn
        AdviceKind.HUMIDITY -> Icons.Outlined.WaterDrop
        AdviceKind.HYDRATE -> Icons.Outlined.WaterDrop
        AdviceKind.COMFORT -> Icons.Outlined.WbSunny
      }

@Composable
fun SunMoonCard(day: DailyForecast?, modifier: Modifier = Modifier) {
  if (day == null) return
  val offset = day.utcOffsetSeconds
  val sunrise = day.sunrise?.let { instant -> formatHour(instant, offset) } ?: "—"
  val sunset = day.sunset?.let { instant -> formatHour(instant, offset) } ?: "—"
  val moon: MoonInfo = moonInfo(day.date)
  val daylight =
      if (day.sunrise != null && day.sunset != null) {
        val duration = Duration.between(day.sunrise, day.sunset)
        stringResource(
            R.string.sun_moon_hours,
            duration.toHours().toInt().coerceAtLeast(0),
            (duration.toMinutes() % 60).toInt().coerceAtLeast(0),
        )
      } else "—"

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .clip(MaterialTheme.shapes.large)
              .background(
                  Brush.linearGradient(
                      listOf(
                          MaterialTheme.colorScheme.tertiaryContainer,
                          MaterialTheme.colorScheme.primaryContainer,
                      )
                  )
              ),
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
          text = stringResource(R.string.sun_moon_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        CelestialItem(
            icon = Icons.Outlined.WbSunny,
            title = stringResource(R.string.sun_rise),
            value = sunrise,
        )
        CelestialItem(
            icon = Icons.Outlined.DarkMode,
            title = stringResource(R.string.sun_set),
            value = sunset,
        )
        CelestialItem(
            icon = Icons.Outlined.DarkMode,
            title = stringResource(R.string.moon_phase),
            value = stringResource(moon.phase.labelRes),
        )
      }
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.sun_moon_daylight, daylight),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
        )
        Text(
            text = stringResource(R.string.moon_illumination, moon.illumination),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
        )
      }
    }
  }
}

@Composable
private fun CelestialItem(icon: ImageVector, title: String, value: String) {
  Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
      )
      Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
      )
    }
    Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}
