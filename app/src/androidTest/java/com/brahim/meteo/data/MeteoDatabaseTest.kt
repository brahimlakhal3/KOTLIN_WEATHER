package com.brahim.meteo.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.data.model.Place
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeteoDatabaseTest {

  private lateinit var database: MeteoDatabase

  private val latitude = 48.8566
  private val longitude = 2.3522
  private val offsetSeconds = 7_200

  @Before
  fun setUp() {
    database = MeteoDatabase(ApplicationProvider.getApplicationContext(), name = null)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun storesCityAndFindsItByLocation() = runBlocking {
    val id = database.saveCity(place("Paris"))
    val stored = database.findCity(latitude, longitude)

    assertTrue(id > 0)
    assertNotNull(stored)
    assertEquals("Paris", stored?.name)
    assertEquals("France", stored?.country)
    assertEquals("Île-de-France", stored?.admin1)
    assertEquals(1, database.observeCityCount().first())
  }

  @Test
  fun reusesExistingCityForSameLocation() = runBlocking {
    val first = database.saveCity(place("Paris"))
    val second = database.saveCity(place("Paris"))

    assertEquals(first, second)
    assertEquals(1, database.observeCities().first().size)
  }

  @Test
  fun deletesCity() = runBlocking {
    val id = database.saveCity(place("Lyon"))
    database.deleteCity(id)

    assertNull(database.findCity(latitude, longitude))
    assertEquals(0, database.observeCityCount().first())
  }

  @Test
  fun replacesHourlyForecastAndKeepsLatestValues() = runBlocking {
    val now = Instant.now()
    val hours =
        listOf(
            hourly(now.plusSeconds(3_600), 18.0, code = 1),
            hourly(now, 16.5, code = 3),
            hourly(now.plusSeconds(7_200), 20.0, code = 0),
        )

    database.replaceHourly(latitude, longitude, hours)
    database.replaceHourly(latitude, longitude, hours)

    val stored = database.observeHourly(latitude, longitude).first()
    assertEquals(3, stored.size)
    assertEquals(16.5, stored.first().temperature, 0.001)
    assertEquals(7_200, stored.first().utcOffsetSeconds)
    assertEquals(20.0, stored.last().temperature, 0.001)
  }

  @Test
  fun readsCurrentConditionsFromNearestPastHour() = runBlocking {
    val now = Instant.now()
    database.replaceHourly(
        latitude,
        longitude,
        listOf(
            hourly(now.minusSeconds(3_600), 15.0, code = 3),
            hourly(now.minusSeconds(60), 17.0, code = 1),
            hourly(now.plusSeconds(3_600), 19.0, code = 0),
        ),
    )

    val current = database.currentConditions(latitude, longitude)

    assertNotNull(current)
    assertEquals(17.0, current?.temperature ?: 0.0, 0.001)
    assertEquals(1, current?.weatherCode)
  }

  @Test
  fun replacesDailyForecastWithLocalDates() = runBlocking {
    val today = LocalDate.of(2026, 5, 12)
    val days =
        listOf(
            daily(today, 22.0, 11.0),
            daily(today.plusDays(1), 24.0, 13.0),
        )

    database.replaceDaily(latitude, longitude, days)

    val stored = database.observeDaily(latitude, longitude).first()
    assertEquals(2, stored.size)
    assertEquals(today, stored.first().date)
    assertEquals(22.0, stored.first().temperatureMax ?: 0.0, 0.001)
    assertEquals(today.plusDays(1), stored.last().date)
    assertEquals(Instant.ofEpochMilli(1_700_000_000_000), stored.first().sunrise)
  }

  @Test
  fun storesAndReadsSyncTime() = runBlocking {
    val syncedAt = Instant.ofEpochMilli(1_760_000_000_000)

    assertNull(database.syncTime(latitude, longitude))
    database.saveSyncTime(latitude, longitude, syncedAt)
    assertEquals(syncedAt, database.syncTime(latitude, longitude))

    database.saveSyncTime(latitude, longitude, syncNow)
    assertEquals(syncNow, database.syncTime(latitude, longitude))
  }

  @Test
  fun clearsForecastAndSyncMetadata() = runBlocking {
    val now = Instant.now()
    database.replaceHourly(latitude, longitude, listOf(hourly(now, 18.0, code = 1)))
    database.replaceDaily(latitude, longitude, listOf(daily(LocalDate.of(2026, 5, 12), 22.0, 11.0)))
    database.saveSyncTime(latitude, longitude, now)

    database.clearForecast()

    assertTrue(database.observeHourly(latitude, longitude).first().isEmpty())
    assertTrue(database.observeDaily(latitude, longitude).first().isEmpty())
    assertNull(database.syncTime(latitude, longitude))
    assertEquals(1L, database.saveCity(place("Paris")))
  }

  private val syncNow = Instant.ofEpochMilli(1_760_000_600_000)

  private fun place(name: String) =
      Place(
          name = name,
          country = "France",
          admin1 = "Île-de-France",
          latitude = latitude,
          longitude = longitude,
      )

  private fun hourly(time: Instant, temperature: Double, code: Int) =
      HourlyForecast(
          time = time,
          utcOffsetSeconds = offsetSeconds,
          temperature = temperature,
          apparentTemperature = temperature - 1,
          humidity = 60,
          precipitation = 0.4,
          precipitationProbability = 25,
          weatherCode = code,
          isDay = true,
          windSpeed = 12.5,
          windGust = 25.0,
          windDirection = 180,
          visibility = 10_000.0,
          dewPoint = 9.5,
      )

  private fun daily(date: LocalDate, max: Double, min: Double) =
      DailyForecast(
          date = date,
          utcOffsetSeconds = offsetSeconds,
          temperatureMax = max,
          temperatureMin = min,
          precipitationSum = 2.5,
          weatherCode = 61,
          sunrise = Instant.ofEpochMilli(1_700_000_000_000),
          sunset = Instant.ofEpochMilli(1_700_040_000_000),
          uvIndexMax = 5.0,
      )
}
