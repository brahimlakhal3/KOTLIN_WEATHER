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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsVM(private val repository: MeteoRepository) : ViewModel() {

  val cityCount: StateFlow<Int> =
      repository
          .observeCityCount()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(REPLAY_MILLIS), 0)

  fun clearCache() {
    viewModelScope.launch { repository.clearCache() }
  }

  companion object {
    private const val REPLAY_MILLIS = 5_000L

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        SettingsVM((application as MeteoApplication).container.repository)
      }
    }
  }
}
