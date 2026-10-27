package com.brahim.meteo.util

import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAlertsTest {

  @Test
  fun reportsSevereHeat() {
    val alerts = weatherAlerts(emptyList(), listOf(day(max = 41.0)))

    val heat = alerts.single { alert -> alert.kind == AlertKind.HEAT }
    assertEquals(AlertSeverity.SEVERE, heat.severity)
    assertEquals(41.0, heat.value ?: 0.0, 0.0)
  }

  @Test
  fun reportsColdWindRainAndUv() {
    val cold = weatherAlerts(emptyList(), listOf(day(min = -16.0)))
    val wind = weatherAlerts(listOf(hour(gust = 95.0)), emptyList())
    val rain = weatherAlerts(emptyList(), listOf(day(rain = 36.0)))
    val uv = weatherAlerts(emptyList(), listOf(day(uv = 12.0)))

    assertEquals(AlertSeverity.SEVERE, cold.single { it.kind == AlertKind.COLD }.severity)
    assertEquals(AlertSeverity.SEVERE, wind.single { it.kind == AlertKind.WIND }.severity)
    assertEquals(AlertSeverity.SEVERE, rain.single { it.kind == AlertKind.RAIN }.severity)
    assertEquals(AlertSeverity.SEVERE, uv.single { it.kind == AlertKind.UV }.severity)
  }

  @Test
  fun reportsSnowStormsAndFog() {
    val snow = weatherAlerts(listOf(hour(code = 75, precipitation = 4.0)), emptyList())
    val storm = weatherAlerts(listOf(hour(code = 95)), emptyList())
    val fog = weatherAlerts(listOf(hour(code = 45)), emptyList())

    assertEquals(AlertKind.SNOW, snow.single().kind)
    assertEquals(AlertKind.THUNDERSTORM, storm.single().kind)
    assertEquals(AlertKind.FOG, fog.single().kind)
  }

  @Test
  fun quietWeatherHasNoAlerts() {
    assertTrue(weatherAlerts(listOf(hour()), listOf(day())).isEmpty())
    assertTrue(weatherAlerts(emptyList(), emptyList()).isEmpty())
  }

  @Test
  fun keepsAtMostThreeSortedBySeverity() {
    val alerts =
        weatherAlerts(
            listOf(hour(gust = 95.0, code = 95, precipitation = 20.0)),
            listOf(day(max = 41.0, rain = 40.0, uv = 12.0)),
        )

    assertEquals(3, alerts.size)
    assertEquals(AlertSeverity.SEVERE, alerts.first().severity)
    assertEquals(alerts.size, alerts.distinctBy { it.kind }.size)
  }

  private fun hour(
      gust: Double? = 10.0,
      precipitation: Double? = 0.0,
      code: Int = 0,
  ) =
      HourlyForecast(
          time = Instant.parse("2026-07-01T12:00:00Z"),
          utcOffsetSeconds = 0,
          temperature = 20.0,
          apparentTemperature = 20.0,
          humidity = 50,
          precipitation = precipitation,
          precipitationProbability = 0,
          weatherCode = code,
          isDay = true,
          windSpeed = 10.0,
          windGust = gust,
          windDirection = 0,
          visibility = 10_000.0,
          dewPoint = 10.0,
      )

  private fun day(
      max: Double = 24.0,
      min: Double = 15.0,
      rain: Double = 0.0,
      uv: Double = 2.0,
  ) =
      DailyForecast(
          date = LocalDate.of(2026, 7, 1),
          utcOffsetSeconds = 0,
          temperatureMax = max,
          temperatureMin = min,
          precipitationSum = rain,
          weatherCode = 0,
          sunrise = null,
          sunset = null,
          uvIndexMax = uv,
      )
}
