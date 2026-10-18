package com.brahim.meteo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brahim.meteo.MeteoApplication
import com.brahim.meteo.config.AppConfigKeys
import com.brahim.meteo.config.appConfigStore
import com.brahim.meteo.data.MeteoRepository
import com.brahim.meteo.data.model.Place
import java.util.Locale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Place> = emptyList(),
    val hasSearched: Boolean = false,
    val hasError: Boolean = false,
)

@OptIn(FlowPreview::class)
class SearchVM(application: Application, private val repository: MeteoRepository) :
    AndroidViewModel(application) {

  private val query = MutableStateFlow("")
  private val searching = MutableStateFlow(false)
  private val results = MutableStateFlow<List<Place>>(emptyList())
  private val searched = MutableStateFlow(false)
  private val failed = MutableStateFlow(false)

  private val language =
      application.appConfigStore.data.map { preferences ->
        preferences[AppConfigKeys.LANGUAGE].orEmpty().ifBlank { Locale.getDefault().language }
      }

  val state: StateFlow<SearchUiState> =
      combine(query, searching, results, searched, failed) { query, searching, results, searched, failed ->
            SearchUiState(
                query = query,
                isSearching = searching,
                results = results,
                hasSearched = searched,
                hasError = failed,
            )
          }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

  init {
    viewModelScope.launch {
      combine(query, language) { query, language -> query.trim() to language }
          .distinctUntilChanged()
          .debounce(SEARCH_DEBOUNCE_MILLIS)
          .collect { (value, languageTag) -> search(value, languageTag) }
    }
  }

  fun onQueryChange(value: String) {
    query.value = value
  }

  fun clearQuery() {
    query.value = ""
    results.value = emptyList()
    searched.value = false
    failed.value = false
  }

  fun searchNow() {
    viewModelScope.launch { search(query.value.trim(), language.first()) }
  }

  fun addPlace(place: Place, onAdded: () -> Unit) {
    viewModelScope.launch {
      repository.saveCity(place)
      onAdded()
    }
  }

  private suspend fun search(value: String, languageTag: String) {
    if (value.length < MIN_QUERY_LENGTH) {
      results.value = emptyList()
      searched.value = false
      failed.value = false
      return
    }
    searching.value = true
    failed.value = false
    val found =
        try {
          repository.searchPlaces(value, languageTag)
        } catch (error: Exception) {
          failed.value = true
          emptyList()
        }
    results.value = found
    searching.value = false
    searched.value = true
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MILLIS = 350L
    private const val MIN_QUERY_LENGTH = 2

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        SearchVM(application, (application as MeteoApplication).container.repository)
      }
    }
  }
}
