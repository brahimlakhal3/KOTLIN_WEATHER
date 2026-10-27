package com.brahim.meteo.util

import androidx.annotation.StringRes
import com.brahim.meteo.R
import java.time.LocalDate
import kotlin.math.cos

enum class MoonPhase(@param:StringRes val labelRes: Int) {
  NEW(R.string.moon_new),
  WAXING_CRESCENT(R.string.moon_waxing_crescent),
  FIRST_QUARTER(R.string.moon_first_quarter),
  WAXING_GIBBOUS(R.string.moon_waxing_gibbous),
  FULL(R.string.moon_full),
  WANING_GIBBOUS(R.string.moon_waning_gibbous),
  LAST_QUARTER(R.string.moon_last_quarter),
  WANING_CRESCENT(R.string.moon_waning_crescent),
}

data class MoonInfo(val phase: MoonPhase, val illumination: Int)

/**
 * Approximate moon phase, accurate enough for a weather card. Reference new moon: 2000-01-06.
 */
fun moonInfo(date: LocalDate): MoonInfo {
  val referenceNewMoon = LocalDate.of(2000, 1, 6).toEpochDay()
  val age = ((date.toEpochDay() - referenceNewMoon) % SYNODIC_DAYS + SYNODIC_DAYS) % SYNODIC_DAYS
  val fraction = age / SYNODIC_DAYS
  val index = ((fraction * 8.0) + 0.5).toInt() % 8
  val illumination = ((1.0 - cos(2.0 * Math.PI * fraction)) / 2.0 * 100.0).toInt()
  return MoonInfo(phase = MoonPhase.entries[index], illumination = illumination)
}

private const val SYNODIC_DAYS = 29.530588853
