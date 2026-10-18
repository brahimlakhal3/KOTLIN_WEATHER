package com.brahim.meteo.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brahim.meteo.MeteoApplication
import com.brahim.meteo.data.MeteoRepository
import com.brahim.meteo.data.model.City
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.data.model.Place
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CityWeather(val city: City, val conditions: HourlyForecast?)

class CitiesVM(private val repository: MeteoRepository) : ViewModel() {

  val cities: StateFlow<List<CityWeather>> =
      repository
          .observeCities()
          .map { cities ->
            cities.map { city ->
              CityWeather(city, repository.currentConditions(city.latitude, city.longitude))
            }
          }
          .flowOn(Dispatchers.IO)
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), emptyList())

  val cityCount: StateFlow<Int> =
      repository
          .observeCityCount()
          .distinctUntilChanged()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), 0)

  private val ticker: Flow<Unit> = flow {
    while (true) {
      emit(Unit)
      delay(REFRESH_INTERVAL_MILLIS)
    }
  }

  init {
    viewModelScope.launch {
      combine(repository.observeCities(), ticker) { cities, _ -> cities }.collect { cities ->
        cities.forEach { city -> refreshIfStale(city.latitude, city.longitude) }
      }
    }
  }

  fun delete(id: Long) {
    viewModelScope.launch { repository.deleteCity(id) }
  }

  fun restore(city: City) {
    viewModelScope.launch {
      repository.saveCity(
          Place(
              name = city.name,
              country = city.country,
              admin1 = city.admin1,
              latitude = city.latitude,
              longitude = city.longitude,
          )
      )
    }
  }

  private suspend fun refreshIfStale(latitude: Double, longitude: Double) {
    val lastSync = repository.syncTime(latitude, longitude)
    val stale =
        lastSync == null || Duration.between(lastSync, Instant.now()).toMinutes() >= STALE_MINUTES
    if (stale) repository.refresh(latitude, longitude)
  }

  companion object {
    private const val REPLAY_MILLIS = 5_000L
    private const val STALE_MINUTES = 20L
    private const val REFRESH_INTERVAL_MILLIS = 300_000L

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        CitiesVM((application as MeteoApplication).container.repository)
      }
    }
  }
}
