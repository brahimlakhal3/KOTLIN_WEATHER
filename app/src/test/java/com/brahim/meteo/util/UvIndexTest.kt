package com.brahim.meteo.util

import com.brahim.meteo.R
import org.junit.Assert.assertEquals
import org.junit.Test

class UvIndexTest {

  @Test
  fun lowRangeUsesLowLevel() {
    assertEquals(R.string.uv_level_low, uvIndexLevelRes(0.0))
    assertEquals(R.string.uv_level_low, uvIndexLevelRes(2.9))
  }

  @Test
  fun moderateRangeUsesModerateLevel() {
    assertEquals(R.string.uv_level_moderate, uvIndexLevelRes(3.0))
    assertEquals(R.string.uv_level_moderate, uvIndexLevelRes(5.9))
  }

  @Test
  fun highRangeUsesHighLevel() {
    assertEquals(R.string.uv_level_high, uvIndexLevelRes(6.0))
    assertEquals(R.string.uv_level_high, uvIndexLevelRes(7.9))
  }

  @Test
  fun veryHighRangeUsesVeryHighLevel() {
    assertEquals(R.string.uv_level_very_high, uvIndexLevelRes(8.0))
    assertEquals(R.string.uv_level_very_high, uvIndexLevelRes(10.9))
  }

  @Test
  fun extremeRangeUsesExtremeLevel() {
    assertEquals(R.string.uv_level_extreme, uvIndexLevelRes(11.0))
    assertEquals(R.string.uv_level_extreme, uvIndexLevelRes(13.5))
  }
}
