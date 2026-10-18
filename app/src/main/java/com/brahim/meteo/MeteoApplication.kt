package com.brahim.meteo

import android.app.Application
import android.content.Context
import com.brahim.meteo.data.MeteoDatabase
import com.brahim.meteo.data.MeteoRepository
import com.brahim.meteo.data.WeatherApi

class AppContainer(context: Context) {

  private val database = MeteoDatabase(context)

  private val api = WeatherApi()

  val repository = MeteoRepository(database, api)
}

class MeteoApplication : Application() {

  val container: AppContainer by lazy { AppContainer(this) }
}
