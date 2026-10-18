package com.brahim.meteo.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class NumbersTest {

  @Test
  fun formatsWithRequestedDecimals() {
    assertEquals("21", 21.42.formatNumber(0, Locale.US))
    assertEquals("21.4", 21.42.formatNumber(1, Locale.US))
  }

  @Test
  fun followsLocaleSeparator() {
    assertEquals("21,4", 21.42.formatNumber(1, Locale.FRANCE))
  }

  @Test
  fun fallsBackForMissingValue() {
    val value: Double? = null

    assertEquals("—", value.formatNumber(0, Locale.US))
    assertEquals("12", (12.4 as Double?).formatNumber(0, Locale.US))
  }
}
