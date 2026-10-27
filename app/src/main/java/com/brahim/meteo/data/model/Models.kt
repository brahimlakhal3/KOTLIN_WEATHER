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
)

data class ForecastPayload(
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
)

data class AirQuality(
    val time: Instant,
    val utcOffsetSeconds: Int,
    val europeanAqi: Int?,
    val usAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
    val nitrogenDioxide: Double?,
    val sulphurDioxide: Double?,
    val carbonMonoxide: Double?,
    val alderPollen: Double?,
    val birchPollen: Double?,
    val grassPollen: Double?,
    val mugwortPollen: Double?,
    val olivePollen: Double?,
    val ragweedPollen: Double?,
) {
  val pollen: Double?
    get() =
        listOfNotNull(alderPollen, birchPollen, grassPollen, mugwortPollen, olivePollen, ragweedPollen)
            .maxOrNull()
}
