package com.brahim.meteo.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityMatchTest {

  @Test
  fun gpsJitterWithinToleranceIsTheSameCity() {
    assertTrue(isSameCity(48.8566, 2.3522, 48.8571, 2.3530))
    assertTrue(
        isSameCity(
            48.8566,
            2.3522,
            48.8566 + CITY_MATCH_DEGREES * 0.5,
            2.3522,
        )
    )
  }

  @Test
  fun distantPlacesAreDifferentCities() {
    assertFalse(
        isSameCity(
            48.8566,
            2.3522,
            48.8566 + CITY_MATCH_DEGREES * 1.5,
            2.3522,
        )
    )
    assertFalse(
        isSameCity(
            48.8566,
            2.3522,
            48.8566,
            2.3522 + CITY_MATCH_DEGREES * 1.5,
        )
    )
  }
}
