package com.brahim.meteo.viewModel

import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brahim.meteo.config.AppConfigKeys
import com.brahim.meteo.config.appConfigStore
import com.brahim.meteo.ui.AppLanguage
import com.brahim.meteo.ui.theme.AccentColor
import com.brahim.meteo.ui.theme.CornerStyle
import com.brahim.meteo.ui.theme.TextSize
import com.brahim.meteo.ui.theme.ThemeMode
import com.brahim.meteo.util.DistanceUnit
import com.brahim.meteo.util.PrecipitationUnit
import com.brahim.meteo.util.PressureUnit
import com.brahim.meteo.util.TemperatureUnit
import com.brahim.meteo.util.TimeFormat
import com.brahim.meteo.util.UnitPreferences
import com.brahim.meteo.util.WindUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppConfigState(
    val onboardingDone: Boolean = false,
    val startMode: String = "",
    val startScreen: String = AppConfigVM.START_SCREEN_WEATHER,
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null,
    val themeMode: String = "",
    val accentColor: String = "",
    val cornerStyle: String = "",
    val animations: Boolean = true,
    val language: String = AppLanguage.ENGLISH.code,
    val textSize: String = "",
    val temperatureUnit: String = "",
    val windUnit: String = "",
    val precipitationUnit: String = "",
    val distanceUnit: String = "",
    val pressureUnit: String = "",
    val timeFormat: String = "",
    val temperatureDecimals: Int = 0,
    val showAlerts: Boolean = true,
    val showAirQuality: Boolean = true,
) {
  val hasHomeCity: Boolean
    get() = homeLatitude != null && homeLongitude != null

  val theme: ThemeMode
    get() = ThemeMode.from(themeMode)

  val accent: AccentColor
    get() = AccentColor.from(accentColor)

  val corners: CornerStyle
    get() = CornerStyle.from(cornerStyle)

  val size: TextSize
    get() = TextSize.from(textSize)

  val appLanguage: AppLanguage
    get() = AppLanguage.from(language)

  val units: UnitPreferences
    get() =
        UnitPreferences(
            temperature = TemperatureUnit.from(temperatureUnit),
            wind = WindUnit.from(windUnit),
            precipitation = PrecipitationUnit.from(precipitationUnit),
            distance = DistanceUnit.from(distanceUnit),
            pressure = PressureUnit.from(pressureUnit),
            timeFormat = TimeFormat.from(timeFormat),
            temperatureDecimals = temperatureDecimals,
        )
}

class AppConfigVM(application: Application) : AndroidViewModel(application) {

  private val store = application.appConfigStore

  val config: StateFlow<AppConfigState?> =
      store.data
          .map { preferences -> preferences.toState() }
          .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  fun setMode(mode: String) = set(AppConfigKeys.THEME_MODE, mode)

  fun setAccent(accent: String) = set(AppConfigKeys.ACCENT_COLOR, accent)

  fun setCornerStyle(style: String) = set(AppConfigKeys.CORNER_STYLE, style)

  fun setAnimations(enabled: Boolean) = set(AppConfigKeys.ANIMATIONS, enabled.toString())

  fun setLanguage(language: String) {
    val code = AppLanguage.from(language).code
    set(AppConfigKeys.LANGUAGE, code)
  }

  fun setTextSize(size: String) = set(AppConfigKeys.TEXT_SIZE, size)

  fun setTemperatureUnit(unit: String) = set(AppConfigKeys.TEMPERATURE_UNIT, unit)

  fun setWindUnit(unit: String) = set(AppConfigKeys.WIND_UNIT, unit)

  fun setPrecipitationUnit(unit: String) = set(AppConfigKeys.PRECIPITATION_UNIT, unit)

  fun setDistanceUnit(unit: String) = set(AppConfigKeys.DISTANCE_UNIT, unit)

  fun setPressureUnit(unit: String) = set(AppConfigKeys.PRESSURE_UNIT, unit)

  fun setTimeFormat(format: String) = set(AppConfigKeys.TIME_FORMAT, format)

  fun setTemperatureDecimals(decimals: Int) =
      set(AppConfigKeys.TEMPERATURE_DECIMALS, decimals.coerceIn(0, 2).toString())

  fun setShowAlerts(enabled: Boolean) = set(AppConfigKeys.SHOW_ALERTS, enabled.toString())

  fun setShowAirQuality(enabled: Boolean) =
      set(AppConfigKeys.SHOW_AIR_QUALITY, enabled.toString())

  fun setHomeCity(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences[AppConfigKeys.HOME_CITY_LATITUDE] = latitude
        preferences[AppConfigKeys.HOME_CITY_LONGITUDE] = longitude
        // Once a city has been chosen, future launches open it directly instead of the
        // search fallback from onboarding.
        preferences[AppConfigKeys.START_SCREEN] = START_SCREEN_WEATHER
      }
    }
  }

  fun clearHomeCity() {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences.remove(AppConfigKeys.HOME_CITY_LATITUDE)
        preferences.remove(AppConfigKeys.HOME_CITY_LONGITUDE)
      }
    }
  }

  fun completeOnboarding(mode: String, latitude: Double? = null, longitude: Double? = null) {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences[AppConfigKeys.ONBOARDING_DONE] = true
        preferences[AppConfigKeys.START_MODE] = mode
        preferences[AppConfigKeys.START_SCREEN] =
            if (mode == START_MODE_SEARCH) START_SCREEN_SEARCH else START_SCREEN_WEATHER
        if (latitude != null && longitude != null) {
          preferences[AppConfigKeys.HOME_CITY_LATITUDE] = latitude
          preferences[AppConfigKeys.HOME_CITY_LONGITUDE] = longitude
        }
      }
    }
  }

  fun resetOnboarding() {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences[AppConfigKeys.ONBOARDING_DONE] = false
        preferences[AppConfigKeys.START_MODE] = ""
        preferences[AppConfigKeys.START_SCREEN] = START_SCREEN_WEATHER
        preferences.remove(AppConfigKeys.HOME_CITY_LATITUDE)
        preferences.remove(AppConfigKeys.HOME_CITY_LONGITUDE)
      }
    }
  }

  fun resetPreferences() {
    viewModelScope.launch {
      store.edit { preferences ->
        preferences.clear()
        // Resetting options should not unexpectedly replay onboarding.
        preferences[AppConfigKeys.ONBOARDING_DONE] = true
        preferences[AppConfigKeys.START_SCREEN] = START_SCREEN_WEATHER
      }
    }
  }

  private fun set(key: Preferences.Key<String>, value: String) {
    viewModelScope.launch { store.edit { preferences -> preferences[key] = value } }
  }

  companion object {
    const val START_MODE_LOCATION = "location"
    const val START_MODE_SEARCH = "search"
    const val START_SCREEN_WEATHER = "weather"
    const val START_SCREEN_SEARCH = "search"

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { AppConfigVM(this[APPLICATION_KEY] as Application) }
    }
  }
}

private fun Preferences.toState(): AppConfigState {
  val language =
      AppLanguage.entries
          .firstOrNull { entry -> entry.code == this[AppConfigKeys.LANGUAGE] }
          ?.code ?: AppLanguage.system().code
  return AppConfigState(
      onboardingDone = this[AppConfigKeys.ONBOARDING_DONE] ?: false,
      startMode = this[AppConfigKeys.START_MODE].orEmpty(),
      startScreen = this[AppConfigKeys.START_SCREEN] ?: AppConfigVM.START_SCREEN_WEATHER,
      homeLatitude = this[AppConfigKeys.HOME_CITY_LATITUDE],
      homeLongitude = this[AppConfigKeys.HOME_CITY_LONGITUDE],
      themeMode = this[AppConfigKeys.THEME_MODE].orEmpty(),
      accentColor = this[AppConfigKeys.ACCENT_COLOR].orEmpty(),
      cornerStyle = this[AppConfigKeys.CORNER_STYLE].orEmpty(),
      animations = this[AppConfigKeys.ANIMATIONS]?.toBooleanStrictOrNull() ?: true,
      language = language,
      textSize = this[AppConfigKeys.TEXT_SIZE].orEmpty(),
      temperatureUnit = this[AppConfigKeys.TEMPERATURE_UNIT].orEmpty(),
      windUnit = this[AppConfigKeys.WIND_UNIT].orEmpty(),
      precipitationUnit = this[AppConfigKeys.PRECIPITATION_UNIT].orEmpty(),
      distanceUnit = this[AppConfigKeys.DISTANCE_UNIT].orEmpty(),
      pressureUnit = this[AppConfigKeys.PRESSURE_UNIT].orEmpty(),
      timeFormat = this[AppConfigKeys.TIME_FORMAT].orEmpty(),
      temperatureDecimals =
          this[AppConfigKeys.TEMPERATURE_DECIMALS]?.toIntOrNull()?.coerceIn(0, 2) ?: 0,
      showAlerts = this[AppConfigKeys.SHOW_ALERTS]?.toBooleanStrictOrNull() ?: true,
      showAirQuality = this[AppConfigKeys.SHOW_AIR_QUALITY]?.toBooleanStrictOrNull() ?: true,
  )
}
