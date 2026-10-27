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
import com.brahim.meteo.data.model.Place
import kotlinx.coroutines.launch

class OnboardingVM(private val repository: MeteoRepository) : ViewModel() {

  fun savePlace(place: Place, onSaved: () -> Unit, onError: () -> Unit = {}) {
    viewModelScope.launch {
      runCatching { repository.saveCity(place) }
          .onSuccess { onSaved() }
          .onFailure { onError() }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        OnboardingVM((application as MeteoApplication).container.repository)
      }
    }
  }
}
