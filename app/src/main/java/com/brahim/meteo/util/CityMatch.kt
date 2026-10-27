package com.brahim.meteo.util

import kotlin.math.abs

/**
 * GPS fixes move slightly between requests. A ~1 km box is enough to recognise the same place
 * without merging genuinely different nearby towns.
 */
const val CITY_MATCH_DEGREES = 0.01

fun isSameCity(
    latitude: Double,
    longitude: Double,
    otherLatitude: Double,
    otherLongitude: Double,
): Boolean =
    abs(latitude - otherLatitude) <= CITY_MATCH_DEGREES &&
        abs(longitude - otherLongitude) <= CITY_MATCH_DEGREES
