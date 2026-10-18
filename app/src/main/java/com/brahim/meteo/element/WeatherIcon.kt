package com.brahim.meteo.element

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brahim.meteo.R
import com.brahim.meteo.util.weatherIconRes

@Composable
fun WeatherIcon(
    weatherCode: Int?,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  Icon(
      painter = painterResource(weatherIconRes(weatherCode, isDay)),
      contentDescription = stringResource(R.string.cd_weather_icon),
      tint = tint,
      modifier = modifier.size(size),
  )
}
