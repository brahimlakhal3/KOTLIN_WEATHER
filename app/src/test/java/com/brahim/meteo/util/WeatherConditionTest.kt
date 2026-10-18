package com.brahim.meteo.util

import com.brahim.meteo.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {

  @Test
  fun mapsClearCodes() {
    assertEquals(WeatherCondition.CLEAR, weatherCondition(0))
    assertEquals(WeatherCondition.MAINLY_CLEAR, weatherCondition(1))
    assertEquals(WeatherCondition.PARTLY_CLOUDY, weatherCondition(2))
    assertEquals(WeatherCondition.OVERCAST, weatherCondition(3))
  }

  @Test
  fun mapsFogCodes() {
    assertEquals(WeatherCondition.FOG, weatherCondition(45))
    assertEquals(WeatherCondition.FOG, weatherCondition(48))
  }

  @Test
  fun mapsDrizzleAndRainCodes() {
    assertEquals(WeatherCondition.DRIZZLE, weatherCondition(51))
    assertEquals(WeatherCondition.DRIZZLE, weatherCondition(55))
    assertEquals(WeatherCondition.FREEZING_DRIZZLE, weatherCondition(57))
    assertEquals(WeatherCondition.RAIN, weatherCondition(63))
    assertEquals(WeatherCondition.FREEZING_RAIN, weatherCondition(67))
    assertEquals(WeatherCondition.RAIN_SHOWERS, weatherCondition(82))
  }

  @Test
  fun mapsSnowAndStormCodes() {
    assertEquals(WeatherCondition.SNOW, weatherCondition(75))
    assertEquals(WeatherCondition.SNOW_SHOWERS, weatherCondition(86))
    assertEquals(WeatherCondition.THUNDERSTORM, weatherCondition(95))
    assertEquals(WeatherCondition.THUNDERSTORM_HAIL, weatherCondition(99))
  }

  @Test
  fun fallsBackToUnknown() {
    assertEquals(WeatherCondition.UNKNOWN, weatherCondition(null))
    assertEquals(WeatherCondition.UNKNOWN, weatherCondition(-1))
    assertEquals(WeatherCondition.UNKNOWN, weatherCondition(200))
  }

  @Test
  fun picksDayOrNightIcon() {
    val day = weatherIconRes(0, isDay = true)
    val night = weatherIconRes(0, isDay = false)

    assertNotEquals(day, night)
    assertEquals(R.drawable.weather_sun, day)
    assertEquals(R.drawable.weather_moon, night)
  }

  @Test
  fun unknownCodeUsesCloudIcon() {
    assertEquals(R.drawable.weather_cloud, weatherIconRes(500, isDay = true))
  }

  @Test
  fun detectsPrecipitation() {
    assertTrue(weatherCondition(61).isPrecipitation())
    assertTrue(weatherCondition(95).isPrecipitation())
    assertTrue(weatherCondition(71).isPrecipitation())
    assertEquals(false, weatherCondition(0).isPrecipitation())
    assertEquals(false, weatherCondition(3).isPrecipitation())
  }
}
