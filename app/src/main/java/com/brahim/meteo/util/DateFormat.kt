package com.brahim.meteo.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")

private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM")

private val syncFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm")

fun localDateTime(instant: Instant, utcOffsetSeconds: Int): LocalDateTime =
    LocalDateTime.ofInstant(instant, ZoneOffset.ofTotalSeconds(utcOffsetSeconds))

fun LocalDateTime.formatHour(): String = format(hourFormatter)

fun Instant.formatSyncTime(locale: Locale, zoneId: ZoneId = ZoneId.systemDefault()): String =
    atZone(zoneId).toLocalDateTime().format(syncFormatter.withLocale(locale))

fun LocalDateTime.formatSyncTime(locale: Locale): String =
    format(syncFormatter.withLocale(locale))

fun LocalDate.formatShort(locale: Locale): String = format(shortDateFormatter.withLocale(locale))

fun LocalDate.dayLabel(
    locale: Locale,
    today: LocalDate,
    todayLabel: String,
    tomorrowLabel: String,
): String =
    when (this) {
      today -> todayLabel
      today.plusDays(1) -> tomorrowLabel
      else -> dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { character ->
        character.titlecase(locale)
      }
    }

fun windDirectionLabel(degrees: Int?): String {
  if (degrees == null) return "—"
  val directions =
      listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO")
  val normalized = ((degrees % 360) + 360) % 360
  val index = ((normalized + 11.25) / 22.5).toInt() % directions.size
  return directions[index]
}
