package com.brahim.meteo.util

import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartAdviceTest {

  @Test
  fun stormAndSnowOutrankRainAdvice() {
    val advice =
        weatherAdvice(
            hourly =
                listOf(
                    hour(
                        temperature = 5.0,
                        feelsLike = 5.0,
                        code = 95,
                        rainChance = 90,
                        precipitation = 8.0,
                    ),
                    hour(
                        temperature = 5.0,
                        feelsLike = 5.0,
                        code = 75,
                        rainChance = 90,
                        precipitation = 8.0,
                    ),
                ),
            daily = listOf(day(max = 12.0, min = 2.0, rain = 8.0)),
        )

    assertEquals(AdviceKind.STORM, advice.first().kind)
    assertEquals(AdviceKind.SNOW, advice[1].kind)
    assertTrue(advice.any { it.kind == AdviceKind.UMBRELLA })
    assertEquals(4, advice.size)
  }

  @Test
  fun hotWeatherAddsHeatAndHydrationAdvice() {
    val advice =
        weatherAdvice(
            hourly = listOf(hour(temperature = 35.0, feelsLike = 39.0, humidity = 85)),
            daily = listOf(day(max = 38.0, min = 26.0, uv = 9.0)),
        )

    assertTrue(advice.any { it.kind == AdviceKind.HEAT })
    assertTrue(advice.any { it.kind == AdviceKind.HYDRATE })
    assertTrue(advice.any { it.kind == AdviceKind.SUNSCREEN })
    assertTrue(advice.any { it.kind == AdviceKind.HUMIDITY })
  }

  @Test
  fun coldWeatherAddsJacketAndLayersOnce() {
    val advice =
        weatherAdvice(
            hourly = listOf(hour(temperature = 2.0, feelsLike = -7.0)),
            daily = listOf(day(max = 3.0, min = -8.0)),
        )

    assertEquals(1, advice.count { it.kind == AdviceKind.JACKET })
    assertTrue(advice.any { it.kind == AdviceKind.LAYERS })
    assertEquals(AdviceKind.JACKET, advice.first().kind)
  }

  @Test
  fun quietWeatherFallsBackToComfortAdvice() {
    val advice = weatherAdvice(listOf(hour()), listOf(day()))

    assertEquals(listOf(AdviceKind.COMFORT), advice.map { it.kind })
  }

  @Test
  fun emptyForecastHasNoAdvice() {
    assertTrue(weatherAdvice(emptyList(), emptyList()).isEmpty())
  }

  @Test
  fun adviceIsDistinctAndLimited() {
    val advice =
        weatherAdvice(
            hourly =
                listOf(
                    hour(
                        temperature = 35.0,
                        feelsLike = 40.0,
                        humidity = 90,
                        gust = 70.0,
                        code = 95,
                        rainChance = 95,
                        precipitation = 12.0,
                    )
                ),
            daily = listOf(day(max = 39.0, min = 28.0, rain = 20.0, uv = 11.0)),
        )

    assertEquals(4, advice.size)
    assertEquals(advice.size, advice.distinctBy { it.kind }.size)
    assertEquals(advice.sortedByDescending { it.priority }, advice)
  }

  private fun hour(
      temperature: Double = 20.0,
      feelsLike: Double = 20.0,
      humidity: Int = 50,
      gust: Double = 10.0,
      code: Int = 0,
      rainChance: Int = 0,
      precipitation: Double = 0.0,
  ) =
      HourlyForecast(
          time = Instant.parse("2026-07-01T12:00:00Z"),
          utcOffsetSeconds = 0,
          temperature = temperature,
          apparentTemperature = feelsLike,
          humidity = humidity,
          precipitation = precipitation,
          precipitationProbability = rainChance,
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
