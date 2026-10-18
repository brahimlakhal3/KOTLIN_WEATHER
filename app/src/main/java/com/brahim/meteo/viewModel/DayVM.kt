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
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.util.localDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayUiState(
    val city: City? = null,
    val days: List<DailyForecast> = emptyList(),
    val date: DailyForecast? = null,
    val hours: List<HourlyForecast> = emptyList(),
)

class DayVM(
    private val repository: MeteoRepository,
    private val latitude: Double,
    private val longitude: Double,
    private val epochDay: Long,
) : ViewModel() {

  private val city = MutableStateFlow<City?>(null)

  private val hoursFlow =
      repository
          .observeHourly(latitude, longitude)
          .map { items ->
            items.filter { item ->
              localDateTime(item.time, item.utcOffsetSeconds).toLocalDate().toEpochDay() == epochDay
            }
          }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), emptyList())

  private val daysFlow =
      repository
          .observeDaily(latitude, longitude)
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), emptyList())

  private val dayFlow =
      daysFlow.map { items -> items.firstOrNull { item -> item.date.toEpochDay() == epochDay } }

  val state: StateFlow<DayUiState> =
      combine(city, daysFlow, dayFlow, hoursFlow) { city, days, day, hours ->
            DayUiState(city = city, days = days, date = day, hours = hours)
          }
          .stateIn(
              viewModelScope,
              SharingStarted.WhileSubscribed(REPLAY_MILLIS),
              DayUiState(),
          )

  init {
    viewModelScope.launch { city.value = repository.findCity(latitude, longitude) }
  }

  companion object {
    private const val REPLAY_MILLIS = 5_000L

    fun factory(
        latitude: Double,
        longitude: Double,
        epochDay: Long,
    ): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        DayVM(
            repository = (application as MeteoApplication).container.repository,
            latitude = latitude,
            longitude = longitude,
            epochDay = epochDay,
        )
      }
    }
  }
}
