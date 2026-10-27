package com.brahim.meteo.util

import com.brahim.meteo.data.model.AirQuality
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AirQualityTest {

  @Test
  fun mapsEuropeanAqiThresholds() {
    assertNull(aqiLevel(null))
    assertEquals(AqiLevel.GOOD, aqiLevel(0))
    assertEquals(AqiLevel.GOOD, aqiLevel(20))
    assertEquals(AqiLevel.FAIR, aqiLevel(21))
    assertEquals(AqiLevel.FAIR, aqiLevel(40))
    assertEquals(AqiLevel.MODERATE, aqiLevel(41))
    assertEquals(AqiLevel.POOR, aqiLevel(61))
    assertEquals(AqiLevel.VERY_POOR, aqiLevel(81))
    assertEquals(AqiLevel.EXTREME, aqiLevel(101))
  }

  @Test
  fun reportsHighestPollenCount() {
    val airQuality =
        AirQuality(
            time = Instant.parse("2026-07-01T12:00:00Z"),
            utcOffsetSeconds = 0,
            europeanAqi = 30,
            usAqi = 25,
            pm25 = 8.0,
            pm10 = 12.0,
            ozone = 60.0,
            nitrogenDioxide = 10.0,
            sulphurDioxide = 2.0,
            carbonMonoxide = 150.0,
            alderPollen = 4.0,
            birchPollen = 18.0,
            grassPollen = 7.0,
            mugwortPollen = null,
            olivePollen = 2.0,
            ragweedPollen = null,
        )

    assertEquals(18.0, airQuality.pollen ?: 0.0, 0.0)
  }
}
