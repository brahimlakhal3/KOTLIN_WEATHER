package com.brahim.meteo.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatTest {

  private val locale = Locale.FRANCE
  private val offset = ZoneOffset.ofHours(2)

  @Test
  fun convertsInstantToLocalDateTimeWithOffset() {
    val instant = Instant.parse("2026-07-01T10:30:00Z")
    val local = localDateTime(instant, offset.totalSeconds)

    assertEquals(LocalDateTime.of(2026, 7, 1, 12, 30), local)
  }

  @Test
  fun convertsInstantToLocalDateTimeWithNegativeOffset() {
    val instant = Instant.parse("2026-07-01T02:00:00Z")
    val local = localDateTime(instant, ZoneOffset.ofHours(-5).totalSeconds)

    assertEquals(LocalDateTime.of(2026, 6, 30, 21, 0), local)
  }

  @Test
  fun formatsHour() {
    assertEquals("08:05", LocalDateTime.of(2026, 3, 4, 8, 5).formatHour())
  }

  @Test
  fun formatsTwelveHourClock() {
    assertEquals(
        "3:05 PM",
        LocalDateTime.of(2026, 3, 4, 15, 5).formatHour(TimeFormat.HOUR_12, Locale.US),
    )
  }

  @Test
  fun formatsSyncTimeWithTwelveHourClock() {
    val instant = Instant.parse("2026-03-04T15:15:00Z")

    assertEquals(
        "4 Mar, 4:15 PM",
        instant.formatSyncTime(
            Locale.US,
            ZoneId.of("Europe/Paris"),
            timeFormat = TimeFormat.HOUR_12,
        ),
    )
  }

  @Test
  fun formatsShortDate() {
    assertEquals("4 mars", LocalDate.of(2026, 3, 4).formatShort(locale))
  }

  @Test
  fun labelsTodayAndTomorrow() {
    val today = LocalDate.of(2026, 3, 4)

    assertEquals(
        "Aujourd'hui",
        today.dayLabel(locale, today, todayLabel = "Aujourd'hui", tomorrowLabel = "Demain"),
    )
    assertEquals(
        "Demain",
        today.plusDays(1).dayLabel(locale, today, "Aujourd'hui", "Demain"),
    )
  }

  @Test
  fun labelsWeekdayWithCapitalLetter() {
    val today = LocalDate.of(2026, 3, 4)
    val label = today.plusDays(2).dayLabel(locale, today, "Aujourd'hui", "Demain")

    assertEquals("Vendredi", label)
  }

  @Test
  fun formatsSyncTimeInGivenZone() {
    val instant = Instant.parse("2026-03-04T09:15:00Z")

    assertEquals(
        "4 mars, 10:15",
        instant.formatSyncTime(locale, ZoneId.of("Europe/Paris")),
    )
  }

  @Test
  fun resolvesWindDirections() {
    assertEquals("N", windDirectionLabel(0))
    assertEquals("E", windDirectionLabel(90))
    assertEquals("S", windDirectionLabel(180))
    assertEquals("O", windDirectionLabel(270))
    assertEquals("N", windDirectionLabel(360))
    assertEquals("NNE", windDirectionLabel(20))
    assertEquals("—", windDirectionLabel(null))
  }

  @Test
  fun normalizesNegativeWindDirection() {
    assertEquals("O", windDirectionLabel(-90))
  }
}
