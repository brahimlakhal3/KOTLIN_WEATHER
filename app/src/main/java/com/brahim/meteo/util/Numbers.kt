package com.brahim.meteo.util

import java.util.Locale

fun Double.formatNumber(decimals: Int, locale: Locale): String =
    String.format(locale, "%.${decimals}f", this)

fun Double?.formatNumber(decimals: Int, locale: Locale, fallback: String = "—"): String =
    this?.formatNumber(decimals, locale) ?: fallback
