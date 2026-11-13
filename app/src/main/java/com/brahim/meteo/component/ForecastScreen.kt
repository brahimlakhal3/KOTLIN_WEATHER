package com.brahim.meteo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.R
import com.brahim.meteo.data.model.AirQuality
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.element.AirQualityCard
import com.brahim.meteo.element.AppIcon
import com.brahim.meteo.element.DailyRow
import com.brahim.meteo.element.DayMetric
import com.brahim.meteo.element.HeroWeatherCard
import com.brahim.meteo.element.HourlyStrip
import com.brahim.meteo.element.LoadingState
import com.brahim.meteo.element.Metric
import com.brahim.meteo.element.MetricGrid
import com.brahim.meteo.element.Reveal
import com.brahim.meteo.element.SectionHeader
import com.brahim.meteo.element.SmartAdviceCard
import com.brahim.meteo.element.SolarArcCard
import com.brahim.meteo.element.StateMessage
import com.brahim.meteo.element.WeatherAlertsCard
import com.brahim.meteo.element.WeatherBackdrop
import com.brahim.meteo.element.WindCompassCard
import com.brahim.meteo.element.weatherMetrics
import com.brahim.meteo.ui.currentLocale
import com.brahim.meteo.ui.rememberMinimumLoading
import com.brahim.meteo.util.dayLabel
import com.brahim.meteo.util.formatHour
import com.brahim.meteo.util.formatShort
import com.brahim.meteo.util.formatSyncTime
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.localDateTime
import com.brahim.meteo.util.moonInfo
import com.brahim.meteo.util.weatherAlerts
import com.brahim.meteo.util.weatherCondition
import com.brahim.meteo.viewModel.AppConfigVM
import com.brahim.meteo.viewModel.ForecastVM
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun ForecastScreen(
    latitude: Double,
    longitude: Double,
    onBack: () -> Unit,
    onOpenDay: (Long, DayMetric?) -> Unit,
    modifier: Modifier = Modifier,
) {
  val viewModel: ForecastVM = viewModel(factory = ForecastVM.factory(latitude, longitude))
  val configViewModel: AppConfigVM = viewModel(factory = AppConfigVM.Factory)
  val config by configViewModel.config.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val locale = currentLocale()
  val snackbarHostState = remember { SnackbarHostState() }
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val errorMessage = stringResource(R.string.snack_error)
  val showAlerts = config?.showAlerts ?: true
  val showLoading = rememberMinimumLoading(state.isLoading)
  val showSyncing = rememberMinimumLoading(state.isSyncing)
  val current = state.hourly.firstOrNull()

  LaunchedEffect(state.hasError) {
    if (state.hasError && state.hasContent) snackbarHostState.showSnackbar(errorMessage)
  }

  Box(modifier = modifier.fillMaxSize()) {
    WeatherBackdrop(
        weatherCode = current?.weatherCode,
        isDay = current?.isDay ?: true,
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    )
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          MediumFlexibleTopAppBar(
              title = {
                Text(
                    text = state.city?.name ?: stringResource(R.string.screen_weather),
                    maxLines = 1,
                )
              },
              subtitle = {
                current?.let { reading ->
                  Text(
                      text =
                          stringResource(
                              R.string.hero_feels_like,
                              formatTemperature(reading.apparentTemperature),
                          ) +
                              " · " +
                              stringResource(weatherCondition(reading.weatherCode).labelRes),
                      maxLines = 1,
                  )
                }
              },
              navigationIcon = { BackButton(onClick = onBack) },
              actions = {
                RefreshButton(isSyncing = showSyncing, onRefresh = viewModel::refresh)
              },
              colors = appBarColors(),
              scrollBehavior = scrollBehavior,
          )
        },
    ) { innerPadding ->
      when {
        showLoading ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
              LoadingState(message = stringResource(R.string.state_loading))
            }
        !state.hasContent ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
              StateMessage(
                  icon = Icons.Outlined.CloudOff,
                  title = stringResource(R.string.state_error_title),
                  message = stringResource(R.string.state_error_hint),
                  action = {
                    Button(onClick = viewModel::refresh) {
                      Text(stringResource(R.string.state_retry))
                    }
                  },
              )
            }
        else ->
            PullToRefreshBox(
                isRefreshing = showSyncing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) {
              ForecastContent(
                  hourly = state.hourly,
                  daily = state.daily,
                  airQuality = state.airQuality,
                  cityName = state.city?.name.orEmpty(),
                  syncedAt = state.syncedAt,
                  locale = locale,
                  showAlerts = showAlerts,
                  onOpenDay = onOpenDay,
              )
            }
      }
    }
  }
}

@Composable
private fun ForecastContent(
    hourly: List<HourlyForecast>,
    daily: List<DailyForecast>,
    airQuality: AirQuality?,
    cityName: String,
    syncedAt: Instant?,
    locale: Locale,
    showAlerts: Boolean,
    onOpenDay: (Long, DayMetric?) -> Unit,
) {
  val current = hourly.firstOrNull() ?: return
  val today = daily.firstOrNull()
  val syncLabel =
      syncedAt?.let { instant ->
        stringResource(R.string.last_updated, formatSyncTime(instant))
      } ?: stringResource(R.string.last_updated_unknown)
  val offset = current.utcOffsetSeconds
  val todayDate = LocalDate.now(ZoneOffset.ofTotalSeconds(offset))
  val weekLow = daily.mapNotNull { day -> day.temperatureMin }.minOrNull() ?: 0.0
  val weekHigh = daily.mapNotNull { day -> day.temperatureMax }.maxOrNull() ?: 1.0
  val highLow =
      if (today?.temperatureMax != null && today.temperatureMin != null) {
        stringResource(
            R.string.city_high_low,
            formatTemperature(today.temperatureMax),
            formatTemperature(today.temperatureMin),
        )
      } else null
  val feelsLike =
      current.apparentTemperature?.let { value ->
        stringResource(R.string.hero_feels_like, formatTemperature(value))
      }
  val alerts = if (showAlerts) weatherAlerts(hourly, daily) else emptyList()
  val moon = today?.date?.let { date -> moonInfo(date) }
  val moonValue = moon?.let { info -> stringResource(info.phase.labelRes) }
  val moonCaption = moon?.let { info -> stringResource(R.string.moon_illumination, info.illumination) }

  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Reveal(index = 0, modifier = Modifier.padding(horizontal = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          HeroWeatherCard(
              weatherCode = current.weatherCode,
              isDay = current.isDay,
              temperature = current.temperature,
              condition = stringResource(weatherCondition(current.weatherCode).labelRes),
              location = cityName.ifBlank { formatHour(current.time, current.utcOffsetSeconds) },
              highLow = highLow,
              feelsLike = feelsLike,
          )
          Text(
              text = syncLabel,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
          )
        }
      }
    }
    if (alerts.isNotEmpty()) {
      item {
        Reveal(index = 1, modifier = Modifier.padding(horizontal = 12.dp)) {
          WeatherAlertsCard(alerts = alerts)
        }
      }
    }
    item {
      Reveal(index = 2, modifier = Modifier.padding(horizontal = 12.dp)) {
        SmartAdviceCard(hourly = hourly, daily = daily)
      }
    }
    item {
      Reveal(index = 3, modifier = Modifier.padding(horizontal = 12.dp)) {
        SolarArcCard(day = today)
      }
    }
    item {
      Reveal(index = 4, modifier = Modifier.padding(horizontal = 12.dp)) {
        WindCompassCard(
            directionDegrees = current.windDirection,
            speed = current.windSpeed,
            gust = current.windGust,
        )
      }
    }
    if (airQuality != null && !airQuality.isEmpty) {
      item {
        Reveal(index = 5, modifier = Modifier.padding(horizontal = 12.dp)) {
          AirQualityCard(quality = airQuality)
        }
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = stringResource(R.string.section_hourly),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        HourlyStrip(items = hourly, onClick = { hour -> onOpenDay(epochDayOf(hour), null) })
      }
    }
    item {
      SectionHeader(
          title = stringResource(R.string.section_daily, daily.size),
          modifier = Modifier.padding(horizontal = 20.dp),
      )
    }
    items(items = daily, key = { day -> day.date.toEpochDay() }) { day ->
      DailyRow(
          item = day,
          label =
              day.date.dayLabel(
                  locale = locale,
                  today = todayDate,
                  todayLabel = stringResource(R.string.day_today),
                  tomorrowLabel = stringResource(R.string.day_tomorrow),
              ),
          dateLabel = day.date.formatShort(locale),
          weekLow = weekLow,
          weekHigh = weekHigh,
          onClick = { onOpenDay(day.date.toEpochDay(), null) },
          modifier = Modifier.padding(horizontal = 12.dp).animateItem(),
      )
    }
    item {
      SectionHeader(
          title = stringResource(R.string.section_details),
          modifier = Modifier.padding(horizontal = 20.dp),
      )
    }
    item {
      val metrics = weatherMetrics(hours = listOf(current), day = today, mean = false)
      MetricGrid(
          metrics =
              if (moonValue != null) {
                metrics +
                    Metric(
                        icon = AppIcon.Vector(Icons.Outlined.DarkMode),
                        label = stringResource(R.string.moon_phase),
                        value = moonValue,
                        caption = moonCaption,
                    )
              } else {
                metrics
              },
          onMetricClick = { metric ->
            onOpenDay(epochDayOf(current), metric.dayMetric ?: DayMetric.TEMPERATURE)
          },
          modifier = Modifier.padding(horizontal = 12.dp),
      )
    }
    item {
      Text(
          text = stringResource(R.string.attribution),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      )
    }
  }
}

private fun epochDayOf(hour: HourlyForecast): Long =
    localDateTime(hour.time, hour.utcOffsetSeconds).toLocalDate().toEpochDay()
