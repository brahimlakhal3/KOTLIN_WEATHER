package com.brahim.meteo.data

import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.ForecastPayload
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.data.model.Place
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class WeatherApi(private val client: HttpClient = createClient()) {

  suspend fun forecast(latitude: Double, longitude: Double): ForecastPayload =
      withContext(Dispatchers.IO) {
        val payload =
            client
                .get(FORECAST_URL) {
                  parameter("latitude", latitude)
                  parameter("longitude", longitude)
                  parameter("timezone", TIMEZONE_AUTO)
                  parameter("forecast_days", FORECAST_DAYS)
                  parameter("hourly", HOURLY_FIELDS)
                  parameter("daily", DAILY_FIELDS)
                }
                .bodyAsText()
        parseForecast(payload)
      }

  suspend fun searchPlaces(query: String, language: String): List<Place> =
      withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val payload =
            client
                .get(GEOCODING_URL) {
                  parameter("name", query)
                  parameter("count", SEARCH_COUNT)
                  parameter("language", language)
                  parameter("format", FORMAT_JSON)
                }
                .bodyAsText()
        parsePlaces(payload)
      }

  private fun parseForecast(payload: String): ForecastPayload {
    val root = JSONObject(payload)
    val offsetSeconds = root.optInt("utc_offset_seconds", 0)
    val zone = ZoneOffset.ofTotalSeconds(offsetSeconds)
    val hourlyJson = root.optJSONObject("hourly")
    val dailyJson = root.optJSONObject("daily")

    val hourly = mutableListOf<HourlyForecast>()
    val times = hourlyJson?.optJSONArray("time")
    if (hourlyJson != null && times != null) {
      for (index in 0 until times.length()) {
        val time = times.localDateTimeAt(index, zone) ?: continue
        hourly +=
            HourlyForecast(
                time = time,
                utcOffsetSeconds = offsetSeconds,
                temperature = hourlyJson.doubleAt("temperature_2m", index) ?: continue,
                apparentTemperature = hourlyJson.doubleAt("apparent_temperature", index),
                humidity = hourlyJson.intAt("relative_humidity_2m", index),
                precipitation = hourlyJson.doubleAt("precipitation", index),
                precipitationProbability = hourlyJson.intAt("precipitation_probability", index),
                weatherCode = hourlyJson.intAt("weather_code", index) ?: UNKNOWN_CODE,
                isDay = hourlyJson.intAt("is_day", index) != 0,
                windSpeed = hourlyJson.doubleAt("wind_speed_10m", index),
                windGust = hourlyJson.doubleAt("wind_gusts_10m", index),
                windDirection = hourlyJson.intAt("wind_direction_10m", index),
                visibility = hourlyJson.doubleAt("visibility", index),
                dewPoint = hourlyJson.doubleAt("dew_point_2m", index),
            )
      }
    }

    val daily = mutableListOf<DailyForecast>()
    val dates = dailyJson?.optJSONArray("time")
    if (dailyJson != null && dates != null) {
      for (index in 0 until dates.length()) {
        val date = dates.localDateAt(index) ?: continue
        daily +=
            DailyForecast(
                date = date,
                utcOffsetSeconds = offsetSeconds,
                temperatureMax = dailyJson.doubleAt("temperature_2m_max", index),
                temperatureMin = dailyJson.doubleAt("temperature_2m_min", index),
                precipitationSum = dailyJson.doubleAt("precipitation_sum", index),
                weatherCode = dailyJson.intAt("weather_code", index) ?: UNKNOWN_CODE,
                sunrise = dailyJson.instantAt("sunrise", index, zone),
                sunset = dailyJson.instantAt("sunset", index, zone),
                uvIndexMax = dailyJson.doubleAt("uv_index_max", index),
            )
      }
    }

    return ForecastPayload(hourly = hourly, daily = daily)
  }

  private fun parsePlaces(payload: String): List<Place> {
    val results = JSONObject(payload).optJSONArray("results") ?: return emptyList()
    val places = mutableListOf<Place>()
    for (index in 0 until results.length()) {
      val item = results.optJSONObject(index) ?: continue
      val name = item.optString("name").takeIf { it.isNotBlank() } ?: continue
      val latitude = item.doubleOrNull("latitude") ?: continue
      val longitude = item.doubleOrNull("longitude") ?: continue
      places +=
          Place(
              name = name,
              country = item.optString("country").takeIf { it.isNotBlank() } ?: name,
              admin1 = item.optString("admin1").takeIf { it.isNotBlank() },
              latitude = latitude,
              longitude = longitude,
          )
    }
    return places
  }

  private fun JSONObject.doubleOrNull(key: String): Double? {
    if (!has(key) || isNull(key)) return null
    val value = optDouble(key)
    return if (value.isNaN()) null else value
  }

  private fun JSONObject.doubleAt(key: String, index: Int): Double? {
    val array = optJSONArray(key) ?: return null
    val value = array.optDouble(index)
    return if (value.isNaN()) null else value
  }

  private fun JSONObject.intAt(key: String, index: Int): Int? {
    val array = optJSONArray(key) ?: return null
    if (index !in 0 until array.length() || array.isNull(index)) return null
    return array.optInt(index)
  }

  private fun JSONObject.instantAt(key: String, index: Int, zone: ZoneOffset): Instant? =
      optJSONArray(key)?.localDateTimeAt(index, zone)

  private fun JSONArray.localDateTimeAt(index: Int, zone: ZoneOffset): Instant? {
    if (index !in 0 until length()) return null
    val raw = optString(index)
    if (raw.isBlank()) return null
    val parsed = runCatching { LocalDateTime.parse(raw) }.getOrNull() ?: return null
    return parsed.toInstant(zone)
  }

  private fun JSONArray.localDateAt(index: Int): LocalDate? {
    if (index !in 0 until length()) return null
    val raw = optString(index)
    if (raw.isBlank()) return null
    return runCatching { LocalDate.parse(raw) }.getOrNull()
  }

  companion object {
    private const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    private const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"
    private const val TIMEZONE_AUTO = "auto"
    private const val FORECAST_DAYS = 7
    private const val SEARCH_COUNT = 12
    private const val FORMAT_JSON = "json"
    private const val UNKNOWN_CODE = -1
    private const val TIMEOUT_MILLIS = 20_000L

    private const val HOURLY_FIELDS =
        "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation," +
            "precipitation_probability,weather_code,is_day,wind_speed_10m,wind_gusts_10m," +
            "wind_direction_10m,visibility,dew_point_2m"

    private const val DAILY_FIELDS =
        "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,sunrise,sunset," +
            "uv_index_max"

    private fun createClient() =
        HttpClient(CIO) {
          install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
          }
        }
  }
}
