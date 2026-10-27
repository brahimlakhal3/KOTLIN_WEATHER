package com.brahim.meteo.data

import com.brahim.meteo.data.model.AirQuality
import com.brahim.meteo.data.model.City
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.data.model.Place
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class MeteoRepository(
    private val database: MeteoDatabase,
    private val api: WeatherApi,
) {

  fun observeCities(): Flow<List<City>> = database.observeCities()

  fun observeCityCount(): Flow<Int> = database.observeCityCount()

  fun observeHourly(latitude: Double, longitude: Double): Flow<List<HourlyForecast>> =
      database.observeHourly(latitude, longitude)

  fun observeDaily(latitude: Double, longitude: Double): Flow<List<DailyForecast>> =
      database.observeDaily(latitude, longitude)

  suspend fun findCity(latitude: Double, longitude: Double): City? =
      database.findCity(latitude, longitude)

  suspend fun currentConditions(latitude: Double, longitude: Double): HourlyForecast? =
      database.currentConditions(latitude, longitude)

  suspend fun today(latitude: Double, longitude: Double): DailyForecast? =
      database.today(latitude, longitude)

  suspend fun airQuality(latitude: Double, longitude: Double): AirQuality? =
      api.airQuality(latitude, longitude)

  suspend fun saveCity(place: Place): Long = database.saveCity(place)

  suspend fun deleteCity(id: Long) = database.deleteCity(id)

  suspend fun syncTime(latitude: Double, longitude: Double): Instant? =
      database.syncTime(latitude, longitude)

  suspend fun clearCache() = database.clearForecast()

  suspend fun searchPlaces(query: String, language: String): List<Place> =
      api.searchPlaces(query, language)

  suspend fun refresh(latitude: Double, longitude: Double): Boolean =
      try {
        val payload = api.forecast(latitude, longitude)
        database.replaceHourly(latitude, longitude, payload.hourly)
        database.replaceDaily(latitude, longitude, payload.daily)
        database.saveSyncTime(latitude, longitude, Instant.now())
        true
      } catch (error: CancellationException) {
        throw error
      } catch (error: Exception) {
        false
      }
}
