package com.brahim.meteo.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.data.model.City
import com.brahim.meteo.data.model.Place
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.weatherCondition

@Composable
fun CityWeatherCard(
    city: City,
    temperature: Double?,
    modifier: Modifier = Modifier,
    temperatureMax: Double? = null,
    temperatureMin: Double? = null,
    weatherCode: Int?,
    isDay: Boolean,
    isHome: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
  val condition = weatherCondition(weatherCode)
  Surface(
      onClick = onClick,
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
          modifier =
              Modifier.size(52.dp)
                  .clip(CircleShape)
                  .background(
                      Brush.linearGradient(
                          listOf(
                              MaterialTheme.colorScheme.primaryContainer,
                              MaterialTheme.colorScheme.tertiaryContainer,
                          )
                      )
                  ),
          contentAlignment = Alignment.Center,
      ) {
        WeatherIcon(
            weatherCode = weatherCode,
            isDay = isDay,
            size = 34.dp,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
              text = city.name,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
          )
          if (isHome) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = stringResource(R.string.city_home_badge),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
          }
        }
        Text(
            text = regionLabel(city.admin1, city.country),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = formatTemperature(temperature),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(condition.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (temperatureMax != null && temperatureMin != null) {
          Text(
              text =
                  stringResource(
                      R.string.city_high_low,
                      formatTemperature(temperatureMax),
                      formatTemperature(temperatureMin),
                  ),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
          )
        }
      }
      IconButton(onClick = onDelete) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.cd_delete_city, city.name),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
  Surface(
      onClick = onClick,
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = containerColor,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Row(
        modifier = Modifier.padding(start = 14.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainerHighest,
          modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
              imageVector = Icons.Outlined.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp),
          )
        }
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = regionLabel(place.admin1, place.country),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
          text = stringResource(R.string.search_add),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@Composable
private fun regionLabel(admin1: String?, country: String): String {
  val region = admin1?.takeIf { it.isNotBlank() && it != country }
  return if (region == null) country
  else stringResource(R.string.subtitle_region, region, country)
}
