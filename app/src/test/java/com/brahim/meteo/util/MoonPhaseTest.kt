package com.brahim.meteo.util

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonPhaseTest {

  @Test
  fun referenceDateIsNewMoon() {
    val moon = moonInfo(LocalDate.of(2000, 1, 6))

    assertEquals(MoonPhase.NEW, moon.phase)
    assertTrue(moon.illumination <= 1)
  }

  @Test
  fun midCycleIsFullMoon() {
    val moon = moonInfo(LocalDate.of(2000, 1, 21))

    assertEquals(MoonPhase.FULL, moon.phase)
    assertTrue(moon.illumination >= 99)
  }

  @Test
  fun illuminationStaysInRangeAcrossCycle() {
    val start = LocalDate.of(2026, 1, 1)

    repeat(30) { day ->
      val moon = moonInfo(start.plusDays(day.toLong()))
      assertTrue(moon.illumination in 0..100)
    }
  }
}
