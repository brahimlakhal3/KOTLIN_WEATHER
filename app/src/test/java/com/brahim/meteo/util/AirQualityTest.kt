package com.brahim.meteo.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AirQualityTest {

  @Test
  fun usAqiBandsAreMapped() {
    assertEquals(AqiLevel.GOOD, aqiLevel(0.0))
    assertEquals(AqiLevel.GOOD, aqiLevel(50.0))
    assertEquals(AqiLevel.MODERATE, aqiLevel(51.0))
    assertEquals(AqiLevel.MODERATE, aqiLevel(100.0))
    assertEquals(AqiLevel.UNHEALTHY_SENSITIVE, aqiLevel(101.0))
    assertEquals(AqiLevel.UNHEALTHY, aqiLevel(160.0))
    assertEquals(AqiLevel.VERY_UNHEALTHY, aqiLevel(220.0))
    assertEquals(AqiLevel.HAZARDOUS, aqiLevel(320.0))
  }

  @Test
  fun missingAqiHasNoLevel() {
    assertNull(aqiLevel(null))
    assertNull(europeanAqiLevel(null))
  }

  @Test
  fun europeanAqiBandsAreMapped() {
    assertEquals(EuropeanAqiLevel.GOOD, europeanAqiLevel(10.0))
    assertEquals(EuropeanAqiLevel.FAIR, europeanAqiLevel(25.0))
    assertEquals(EuropeanAqiLevel.MODERATE, europeanAqiLevel(50.0))
    assertEquals(EuropeanAqiLevel.POOR, europeanAqiLevel(70.0))
    assertEquals(EuropeanAqiLevel.VERY_POOR, europeanAqiLevel(90.0))
    assertEquals(EuropeanAqiLevel.EXTREMELY_POOR, europeanAqiLevel(120.0))
  }
}
