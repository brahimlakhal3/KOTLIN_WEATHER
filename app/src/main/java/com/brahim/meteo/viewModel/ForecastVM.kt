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
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ForecastUiState(
    val city: City? = null,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
    val syncedAt: Instant? = null,
    val isSyncing: Boolean = false,
    val hasError: Boolean = false,
) {
  val isLoading: Boolean
    get() = isSyncing && hourly.isEmpty()

  val hasContent: Boolean
    get() = hourly.isNotEmpty()
}

private data class SyncStatus(
    val isSyncing: Boolean = true,
    val hasError: Boolean = false,
    val syncedAt: Instant? = null,
)

class ForecastVM(
    private val repository: MeteoRepository,
    private val latitude: Double,
    private val longitude: Double,
) : ViewModel() {

  private val city = MutableStateFlow<City?>(null)

  private val status = MutableStateFlow(SyncStatus())

  private val hourlyFlow =
      repository
          .observeHourly(latitude, longitude)
          .map { items ->
            val now = Instant.now()
            items.filter { item -> item.time.plusSeconds(SECONDS_PER_HOUR) > now }.take(HOURS_SHOWN)
          }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), emptyList())

  private val dailyFlow =
      repository
          .observeDaily(latitude, longitude)
          .map { items ->
            val offset = items.firstOrNull()?.utcOffsetSeconds ?: 0
            val today = LocalDate.now(ZoneOffset.ofTotalSeconds(offset))
            items.filter { item -> !item.date.isBefore(today) }.take(DAYS_SHOWN)
          }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), emptyList())

  val state: StateFlow<ForecastUiState> =
      combine(city, hourlyFlow, dailyFlow, status) { city, hourly, daily, status ->
            ForecastUiState(
                city = city,
                hourly = hourly,
                daily = daily,
                syncedAt = status.syncedAt,
                isSyncing = status.isSyncing,
                hasError = status.hasError,
            )
          }
          .stateIn(
              viewModelScope,
              SharingStarted.WhileSubscribed(REPLAY_MILLIS),
              ForecastUiState(isSyncing = true),
          )

  init {
    viewModelScope.launch {
      city.value = repository.findCity(latitude, longitude)
      val lastSync = repository.syncTime(latitude, longitude)
      status.value = status.value.copy(syncedAt = lastSync, isSyncing = false)
      if (isStale(lastSync)) sync()
    }
  }

  fun refresh() {
    viewModelScope.launch { sync() }
  }

  private suspend fun sync() {
    status.value = status.value.copy(isSyncing = true, hasError = false)
    val success = repository.refresh(latitude, longitude)
    val lastSync = if (success) repository.syncTime(latitude, longitude) else status.value.syncedAt
    status.value = SyncStatus(isSyncing = false, hasError = !success, syncedAt = lastSync)
  }

  private fun isStale(lastSync: Instant?): Boolean =
      lastSync == null || Duration.between(lastSync, Instant.now()).toMinutes() >= STALE_MINUTES

  companion object {
    private const val HOURS_SHOWN = 24
    private const val DAYS_SHOWN = 7
    private const val SECONDS_PER_HOUR = 3_600L
    private const val STALE_MINUTES = 20L
    private const val REPLAY_MILLIS = 5_000L

    fun factory(latitude: Double, longitude: Double): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        ForecastVM(
            repository = (application as MeteoApplication).container.repository,
            latitude = latitude,
            longitude = longitude,
        )
      }
    }
  }
}
