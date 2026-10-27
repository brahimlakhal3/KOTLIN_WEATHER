package com.brahim.meteo.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.R
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.element.AnimatedValue
import com.brahim.meteo.element.ChartPoint
import com.brahim.meteo.element.DayMetric
import com.brahim.meteo.element.LoadingState
import com.brahim.meteo.element.MetricChart
import com.brahim.meteo.element.MetricGrid
import com.brahim.meteo.element.IconView
import com.brahim.meteo.element.Reveal
import com.brahim.meteo.element.SummaryCard
import com.brahim.meteo.element.SunMoonCard
import com.brahim.meteo.element.read
import com.brahim.meteo.element.sunTimes
import com.brahim.meteo.element.text
import com.brahim.meteo.element.weatherMetrics
import com.brahim.meteo.ui.currentLocale
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.util.dayLabel
import com.brahim.meteo.util.formatHour
import com.brahim.meteo.util.formatPrecipitation
import com.brahim.meteo.util.formatShort
import com.brahim.meteo.util.formatTemperature
import com.brahim.meteo.util.uvIndexText
import com.brahim.meteo.util.weatherCondition
import com.brahim.meteo.viewModel.DayVM
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DayScreen(
    latitude: Double,
    longitude: Double,
    epochDay: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialMetric: DayMetric? = null,
    onSelectDay: (Long, DayMetric) -> Unit = { _, _ -> },
) {
  val viewModel: DayVM = viewModel(factory = DayVM.factory(latitude, longitude, epochDay))
  val state by viewModel.state.collectAsStateWithLifecycle()
  val animations = LocalAnimationsEnabled.current
  val locale = currentLocale()
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val offset = state.date?.utcOffsetSeconds ?: state.days.firstOrNull()?.utcOffsetSeconds ?: 0
  val todayDate = LocalDate.now(ZoneOffset.ofTotalSeconds(offset))
  val label =
      state.date?.date?.dayLabel(
          locale = locale,
          today = todayDate,
          todayLabel = stringResource(R.string.day_today),
          tomorrowLabel = stringResource(R.string.day_tomorrow),
      ) ?: stringResource(R.string.screen_weather)
  var selectedIndex by rememberSaveable(initialMetric) {
    mutableIntStateOf(initialMetric?.ordinal ?: 0)
  }
  val metric = DayMetric.entries[selectedIndex]
  val day = state.date
  val dayTemperature =
      if (day?.temperatureMax != null && day.temperatureMin != null) {
        formatTemperature(day.temperatureMax) + " / " + formatTemperature(day.temperatureMin)
      } else {
        formatTemperature(day?.temperatureMax)
      }
  val dayDetails = buildList {
    if (day?.temperatureMax != null) {
      add(stringResource(R.string.day_max, formatTemperature(day.temperatureMax)))
    }
    if (day?.temperatureMin != null) {
      add(stringResource(R.string.day_min, formatTemperature(day.temperatureMin)))
    }
    add(stringResource(weatherCondition(day?.weatherCode).labelRes))
  }
  val dayDate = day?.date?.formatShort(locale).orEmpty()
  val dayStats =
      stringResource(R.string.metric_precipitation) +
          " " +
          formatPrecipitation(day?.precipitationSum, 1) +
          " · " +
          stringResource(R.string.metric_uv_index) +
          " " +
          uvIndexText(day?.uvIndexMax, locale)

  Scaffold(
      modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        MediumFlexibleTopAppBar(
            title = { Text(text = label, maxLines = 1) },
            subtitle = {
              Text(
                  text = dayDate.ifBlank { stringResource(R.string.screen_weather) },
                  style = MaterialTheme.typography.bodyMedium,
              )
            },
            navigationIcon = { BackButton(onClick = onBack) },
            colors = appBarColors(),
            scrollBehavior = scrollBehavior,
        )
      },
  ) { innerPadding ->
    if (state.hours.isEmpty()) {
      Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        LoadingState(message = stringResource(R.string.state_loading))
      }
      return@Scaffold
    }

    LazyColumn(
        modifier = Modifier.padding(innerPadding).fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        DayStrip(
            days = state.days,
            selectedEpochDay = epochDay,
            today = todayDate,
            onSelectDay = { selectedDay -> onSelectDay(selectedDay, metric) },
        )
      }
      item {
        Reveal(index = 0, modifier = Modifier.padding(horizontal = 12.dp)) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryCard(
                weatherCode = day?.weatherCode,
                isDay = true,
                temperature = dayTemperature,
                details = dayDetails,
                trailing = dayDate,
            )
            Text(
                text = sunTimes(day),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
            Text(
                text = dayStats,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }
      item {
        Reveal(index = 1, modifier = Modifier.padding(horizontal = 12.dp)) {
          SunMoonCard(day = day)
        }
      }
      item {
        Reveal(index = 2, modifier = Modifier.padding(horizontal = 12.dp)) {
          DayChartCard(
              hours = state.hours,
              metric = metric,
              selectedIndex = selectedIndex,
              animations = animations,
              onSelect = { index -> selectedIndex = index },
          )
        }
      }
      item {
        Reveal(index = 2, modifier = Modifier.padding(horizontal = 12.dp)) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.metric_day_mean),
                style = MaterialTheme.typography.titleMedium,
            )
            MetricGrid(
                metrics = weatherMetrics(hours = state.hours, day = day, mean = true),
                onMetricClick = { metric ->
                  metric.dayMetric?.let { selected -> selectedIndex = selected.ordinal }
                },
            )
          }
        }
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
}

@Composable
private fun DayStrip(
    days: List<DailyForecast>,
    selectedEpochDay: Long,
    today: LocalDate,
    onSelectDay: (Long) -> Unit,
) {
  val locale = currentLocale()
  val selected = days.indexOfFirst { day -> day.date.toEpochDay() == selectedEpochDay }
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceAtLeast(0))

  LazyRow(
      state = listState,
      contentPadding = PaddingValues(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(items = days, key = { day -> day.date.toEpochDay() }) { day ->
      val epochDay = day.date.toEpochDay()
      FilterChip(
          selected = epochDay == selectedEpochDay,
          onClick = { if (epochDay != selectedEpochDay) onSelectDay(epochDay) },
          label = {
            Text(
                text =
                    day.date.dayLabel(
                        locale = locale,
                        today = today,
                        todayLabel = stringResource(R.string.day_today),
                        tomorrowLabel = stringResource(R.string.day_tomorrow),
                    )
            )
          },
      )
    }
  }
}

@Composable
private fun DayChartCard(
    hours: List<HourlyForecast>,
    metric: DayMetric,
    selectedIndex: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = DayMetric.entries.toList(), key = { item -> item.name }) { item ->
          val index = item.ordinal
          FilterChip(
              selected = selectedIndex == index,
              onClick = { onSelect(index) },
              label = { Text(stringResource(item.labelRes)) },
              leadingIcon = { IconView(icon = item.icon, size = 20.dp) },
          )
        }
      }

      AnimatedContent(
          targetState = metric,
          transitionSpec = {
            if (animations) {
              fadeIn(Motion.enter(Motion.SHORT_2)) togetherWith fadeOut(Motion.exit())
            } else {
              EnterTransition.None togetherWith ExitTransition.None
            }
          },
          label = "chart",
      ) { selected ->
        val points =
            hours.mapNotNull { hour ->
              selected.read(hour)?.let { value ->
                ChartPoint(
                    label = formatHour(hour.time, hour.utcOffsetSeconds),
                    value = value.toFloat(),
                    valueText = selected.text(value),
                )
              }
            }
        val peak = points.maxByOrNull { point -> point.value }
        val low = points.minByOrNull { point -> point.value }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            AnimatedValue(
                text = peak?.valueText ?: "—",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(selected.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
          }
          MetricChart(points = points, height = 150.dp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            axisLabels(hours).forEach { axis ->
              Text(
                  text = axis,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          if (peak != null && low != null) {
            Text(
                text =
                    stringResource(R.string.day_metric_low, low.valueText, low.label) +
                        " · " +
                        stringResource(R.string.day_metric_high, peak.valueText, peak.label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun axisLabels(hours: List<HourlyForecast>): List<String> {
  if (hours.isEmpty()) return emptyList()
  val indexes =
      listOf(0, hours.size / 4, hours.size / 2, hours.size * 3 / 4, hours.size - 1).distinct()
  return indexes.map { index ->
    formatHour(hours[index].time, hours[index].utcOffsetSeconds)
  }
}
