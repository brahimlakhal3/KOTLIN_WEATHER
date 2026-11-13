package com.brahim.meteo.data.model

import java.time.Instant
import java.time.LocalDate

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
    val addedAt: Instant,
)

data class Place(
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
)

data class HourlyForecast(
    val time: Instant,
    val utcOffsetSeconds: Int,
    val temperature: Double,
    val apparentTemperature: Double?,
    val humidity: Int?,
    val precipitation: Double?,
    val precipitationProbability: Int?,
    val weatherCode: Int,
    val isDay: Boolean,
    val windSpeed: Double?,
    val windGust: Double?,
    val windDirection: Int?,
    val visibility: Double?,
    val dewPoint: Double?,
    val surfacePressure: Double? = null,
    val cloudCover: Double? = null,
)

data class DailyForecast(
    val date: LocalDate,
    val utcOffsetSeconds: Int,
    val temperatureMax: Double?,
    val temperatureMin: Double?,
    val precipitationSum: Double?,
    val weatherCode: Int,
    val sunrise: Instant?,
    val sunset: Instant?,
    val uvIndexMax: Double?,
    val precipitationProbabilityMax: Int? = null,
    val windSpeedMax: Double? = null,
    val windGustMax: Double? = null,
)

/**
 * The current air quality reading for a location.
 *
 * Values follow the European AQI scale for [europeanAqi] and the US EPA scale for [usAqi]; the
 * remaining fields are raw concentrations in micrograms per cubic metre, except carbon monoxide which
 * is reported in micrograms per cubic metre as well.
 */
data class AirQuality(
    val usAqi: Double? = null,
    val europeanAqi: Double? = null,
    val pm25: Double? = null,
    val pm10: Double? = null,
    val ozone: Double? = null,
    val nitrogenDioxide: Double? = null,
    val sulphurDioxide: Double? = null,
    val carbonMonoxide: Double? = null,
) {
  val isEmpty: Boolean
    get() =
        usAqi == null &&
            europeanAqi == null &&
            pm25 == null &&
            pm10 == null &&
            ozone == null &&
            nitrogenDioxide == null &&
            sulphurDioxide == null &&
            carbonMonoxide == null
}

data class ForecastPayload(
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val airQuality: AirQuality? = null,
)
