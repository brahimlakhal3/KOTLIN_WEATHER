package com.brahim.meteo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.datastore.preferences.core.edit
import com.brahim.meteo.config.AppConfigKeys
import com.brahim.meteo.config.appConfigStore
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppConfigVM(application: Application) : AndroidViewModel(application) {

  private val store = application.appConfigStore

  val mode: StateFlow<String> =
      store.data
          .map { preferences -> preferences[AppConfigKeys.THEME_MODE].orEmpty() }
          .distinctUntilChanged()
          .stateIn(viewModelScope, SharingStarted.Eagerly, "")

  val language: StateFlow<String> =
      store.data
          .map { preferences ->
            preferences[AppConfigKeys.LANGUAGE].orEmpty().ifBlank {
              Locale.getDefault().language
            }
          }
          .distinctUntilChanged()
          .stateIn(viewModelScope, SharingStarted.Eagerly, Locale.getDefault().language)

  val textSize: StateFlow<String> =
      store.data
          .map { preferences -> preferences[AppConfigKeys.TEXT_SIZE].orEmpty() }
          .distinctUntilChanged()
          .stateIn(viewModelScope, SharingStarted.Eagerly, "")

  val units: StateFlow<String> =
      store.data
          .map { preferences -> preferences[AppConfigKeys.UNITS].orEmpty() }
          .distinctUntilChanged()
          .stateIn(viewModelScope, SharingStarted.Eagerly, "")

  fun setMode(mode: String) {
    viewModelScope.launch { store.edit { preferences -> preferences[AppConfigKeys.THEME_MODE] = mode } }
  }

  fun setLanguage(language: String) {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences[AppConfigKeys.LANGUAGE] = language
      }
    }
  }

  fun setTextSize(size: String) {
    viewModelScope.launch { store.edit { preferences -> preferences[AppConfigKeys.TEXT_SIZE] = size } }
  }

  fun setUnits(units: String) {
    viewModelScope.launch { store.edit { preferences -> preferences[AppConfigKeys.UNITS] = units } }
  }

  companion object {
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_ENGLISH = "en"

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { AppConfigVM(this[APPLICATION_KEY] as Application) }
    }
  }
}
