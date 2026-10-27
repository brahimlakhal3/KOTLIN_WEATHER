package com.brahim.meteo.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val hourFormatter24 = DateTimeFormatter.ofPattern("HH:mm")
private val hourFormatter12 = DateTimeFormatter.ofPattern("h:mm a")
private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM")
private val syncFormatter24 = DateTimeFormatter.ofPattern("d MMM, HH:mm")
private val syncFormatter12 = DateTimeFormatter.ofPattern("d MMM, h:mm a")

fun localDateTime(instant: Instant, utcOffsetSeconds: Int): LocalDateTime =
    LocalDateTime.ofInstant(instant, ZoneOffset.ofTotalSeconds(utcOffsetSeconds))

fun LocalDateTime.formatHour(
    timeFormat: TimeFormat = TimeFormat.HOUR_24,
    locale: Locale = Locale.getDefault(),
): String =
    when (timeFormat) {
      TimeFormat.HOUR_24 -> format(hourFormatter24)
      TimeFormat.HOUR_12 -> format(hourFormatter12.withLocale(locale))
    }

fun Instant.formatSyncTime(
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
    timeFormat: TimeFormat = TimeFormat.HOUR_24,
): String {
  val formatter = if (timeFormat == TimeFormat.HOUR_12) syncFormatter12 else syncFormatter24
  return atZone(zoneId).toLocalDateTime().format(formatter.withLocale(locale))
}

fun LocalDateTime.formatSyncTime(
    locale: Locale,
    timeFormat: TimeFormat = TimeFormat.HOUR_24,
): String {
  val formatter = if (timeFormat == TimeFormat.HOUR_12) syncFormatter12 else syncFormatter24
  return format(formatter.withLocale(locale))
}

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
      else ->
          dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { character ->
            character.titlecase(locale)
          }
    }

fun windDirectionLabel(degrees: Int?, locale: Locale? = null): String {
  if (degrees == null) return "—"
  val directions =
      if (locale == null || locale.language == "fr") {
        listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO")
      } else {
        listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
      }
  val normalized = ((degrees % 360) + 360) % 360
  val index = ((normalized + 11.25) / 22.5).toInt() % directions.size
  return directions[index]
}
