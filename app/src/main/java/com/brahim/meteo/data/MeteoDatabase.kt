package com.brahim.meteo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.brahim.meteo.data.model.City
import com.brahim.meteo.data.model.DailyForecast
import com.brahim.meteo.data.model.HourlyForecast
import com.brahim.meteo.data.model.Place
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MeteoDatabase(context: Context, name: String? = DATABASE_NAME) :
    SQLiteOpenHelper(context, name, null, VERSION) {

  private val revision = MutableStateFlow(0L)

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(CREATE_CITY)
    db.execSQL(CREATE_HOURLY)
    db.execSQL(CREATE_DAILY)
    db.execSQL(CREATE_SYNC)
    db.execSQL(CREATE_CITY_INDEX)
    db.execSQL(CREATE_HOURLY_INDEX)
    db.execSQL(CREATE_DAILY_INDEX)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    if (oldVersion < 2) {
      // Keep saved cities: only forecast tables are rebuilt when the schema grows.
      db.execSQL("ALTER TABLE $TABLE_HOURLY ADD COLUMN $COLUMN_SURFACE_PRESSURE REAL")
    }
  }

  fun observeCities(): Flow<List<City>> = revision.map { queryCities() }.flowOn(Dispatchers.IO)

  fun observeHourly(latitude: Double, longitude: Double): Flow<List<HourlyForecast>> =
      revision.map { queryHourly(latitude, longitude) }.flowOn(Dispatchers.IO)

  fun observeDaily(latitude: Double, longitude: Double): Flow<List<DailyForecast>> =
      revision.map { queryDaily(latitude, longitude) }.flowOn(Dispatchers.IO)

  fun observeCityCount(): Flow<Int> = revision.map { queryCityCount() }.flowOn(Dispatchers.IO)

  suspend fun findCity(latitude: Double, longitude: Double): City? =
      withContext(Dispatchers.IO) { queryCity(latitude, longitude) }

  suspend fun currentConditions(latitude: Double, longitude: Double): HourlyForecast? =
      withContext(Dispatchers.IO) {
        val items = queryHourly(latitude, longitude)
        val now = Instant.now()
        items.lastOrNull { item -> !item.time.isAfter(now) } ?: items.firstOrNull()
      }

  suspend fun today(latitude: Double, longitude: Double): DailyForecast? =
      withContext(Dispatchers.IO) {
        val offset = queryHourly(latitude, longitude).firstOrNull()?.utcOffsetSeconds ?: 0
        val today = LocalDate.now(ZoneOffset.ofTotalSeconds(offset))
        queryDaily(latitude, longitude).firstOrNull { day -> day.date == today }
      }

  suspend fun saveCity(place: Place): Long = withContext(Dispatchers.IO) {
    queryCity(place.latitude, place.longitude)?.let { return@withContext it.id }
    val values =
        ContentValues().apply {
          put(COLUMN_NAME, place.name)
          put(COLUMN_COUNTRY, place.country)
          put(COLUMN_ADMIN1, place.admin1)
          put(COLUMN_LATITUDE, place.latitude)
          put(COLUMN_LONGITUDE, place.longitude)
          put(COLUMN_ADDED_AT, Instant.now().toEpochMilli())
        }
    val id = writableDatabase.insert(TABLE_CITY, null, values)
    notifyChange()
    id
  }

  suspend fun deleteCity(id: Long) = withContext(Dispatchers.IO) {
    writableDatabase.delete(TABLE_CITY, "$COLUMN_ID = ?", arrayOf(id.toString()))
    notifyChange()
  }

  suspend fun syncTime(latitude: Double, longitude: Double): Instant? = withContext(Dispatchers.IO) {
    readableDatabase
        .query(
            TABLE_SYNC,
            arrayOf(COLUMN_SYNCED_AT),
            locationSelection,
            locationArguments(latitude, longitude),
            null,
            null,
            null,
        )
        .use { cursor ->
          if (cursor.moveToFirst() && !cursor.isNull(0)) Instant.ofEpochMilli(cursor.getLong(0))
          else null
        }
  }

  suspend fun saveSyncTime(latitude: Double, longitude: Double, at: Instant) =
      withContext(Dispatchers.IO) {
        val values =
            ContentValues().apply {
              put(COLUMN_LATITUDE, latitude)
              put(COLUMN_LONGITUDE, longitude)
              put(COLUMN_SYNCED_AT, at.toEpochMilli())
            }
        writableDatabase.insertWithOnConflict(
            TABLE_SYNC,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
        notifyChange()
      }

  suspend fun replaceHourly(latitude: Double, longitude: Double, items: List<HourlyForecast>) =
      replace(
          latitude,
          longitude,
          TABLE_HOURLY,
          items.map { item -> item.toValues(latitude, longitude) },
      )

  suspend fun replaceDaily(latitude: Double, longitude: Double, items: List<DailyForecast>) =
      replace(
          latitude,
          longitude,
          TABLE_DAILY,
          items.map { item -> item.toValues(latitude, longitude) },
      )

  suspend fun clearForecast() = withContext(Dispatchers.IO) {
    writableDatabase.transaction {
      delete(TABLE_HOURLY, null, null)
      delete(TABLE_DAILY, null, null)
      delete(TABLE_SYNC, null, null)
    }
    notifyChange()
  }

  private suspend fun replace(
      latitude: Double,
      longitude: Double,
      table: String,
      rows: List<ContentValues>,
  ) = withContext(Dispatchers.IO) {
    writableDatabase.transaction {
      delete(table, locationSelection, locationArguments(latitude, longitude))
      rows.forEach { row -> insert(table, null, row) }
    }
    notifyChange()
  }

  private fun notifyChange() {
    revision.value += 1
  }

  private fun queryCities(): List<City> {
    val cities = mutableListOf<City>()
    readableDatabase
        .query(TABLE_CITY, null, null, null, null, null, "$COLUMN_NAME COLLATE NOCASE ASC")
        .use { cursor ->
          val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
          val nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME)
          val countryIndex = cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)
          val adminIndex = cursor.getColumnIndexOrThrow(COLUMN_ADMIN1)
          val latitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)
          val longitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)
          val addedIndex = cursor.getColumnIndexOrThrow(COLUMN_ADDED_AT)
          while (cursor.moveToNext()) {
            cities +=
                City(
                    id = cursor.getLong(idIndex),
                    name = cursor.getString(nameIndex),
                    country = cursor.getString(countryIndex),
                    admin1 = if (cursor.isNull(adminIndex)) null else cursor.getString(adminIndex),
                    latitude = cursor.getDouble(latitudeIndex),
                    longitude = cursor.getDouble(longitudeIndex),
                    addedAt = Instant.ofEpochMilli(cursor.getLong(addedIndex)),
                )
          }
        }
    return cities
  }

  private fun queryCity(latitude: Double, longitude: Double): City? {
    readableDatabase
        .query(
            TABLE_CITY,
            null,
            locationSelection,
            locationArguments(latitude, longitude),
            null,
            null,
            null,
        )
        .use { cursor ->
          if (!cursor.moveToFirst()) return null
          return City(
              id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
              name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
              country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)),
              admin1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADMIN1)),
              latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
              longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
              addedAt =
                  Instant.ofEpochMilli(
                      cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ADDED_AT))
                  ),
          )
        }
  }

  private fun queryCityCount(): Int {
    readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CITY", null).use { cursor ->
      return if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }
  }

  private fun queryHourly(latitude: Double, longitude: Double): List<HourlyForecast> {
    val items = mutableListOf<HourlyForecast>()
    val selection = "$locationSelection AND $COLUMN_TIME >= ?"
    val arguments = locationArguments(latitude, longitude) + Instant.now().minusSeconds(86_400).toEpochMilli().toString()
    readableDatabase
        .query(TABLE_HOURLY, null, selection, arguments, null, null, "$COLUMN_TIME ASC")
        .use { cursor ->
          val timeIndex = cursor.getColumnIndexOrThrow(COLUMN_TIME)
          val offsetIndex = cursor.getColumnIndexOrThrow(COLUMN_UTC_OFFSET)
          val temperatureIndex = cursor.getColumnIndexOrThrow(COLUMN_TEMPERATURE)
          val apparentIndex = cursor.getColumnIndexOrThrow(COLUMN_APPARENT)
          val humidityIndex = cursor.getColumnIndexOrThrow(COLUMN_HUMIDITY)
          val precipitationIndex = cursor.getColumnIndexOrThrow(COLUMN_PRECIPITATION)
          val probabilityIndex = cursor.getColumnIndexOrThrow(COLUMN_PRECIPITATION_PROBABILITY)
          val codeIndex = cursor.getColumnIndexOrThrow(COLUMN_WEATHER_CODE)
          val dayIndex = cursor.getColumnIndexOrThrow(COLUMN_IS_DAY)
          val windIndex = cursor.getColumnIndexOrThrow(COLUMN_WIND_SPEED)
          val gustIndex = cursor.getColumnIndexOrThrow(COLUMN_WIND_GUST)
          val directionIndex = cursor.getColumnIndexOrThrow(COLUMN_WIND_DIRECTION)
          val visibilityIndex = cursor.getColumnIndexOrThrow(COLUMN_VISIBILITY)
          val dewIndex = cursor.getColumnIndexOrThrow(COLUMN_DEW_POINT)
          val pressureIndex = cursor.getColumnIndexOrThrow(COLUMN_SURFACE_PRESSURE)
          while (cursor.moveToNext()) {
            items +=
                HourlyForecast(
                    time = Instant.ofEpochMilli(cursor.getLong(timeIndex)),
                    utcOffsetSeconds = cursor.getInt(offsetIndex),
                    temperature = cursor.getDouble(temperatureIndex),
                    apparentTemperature = cursor.nullableDouble(apparentIndex),
                    humidity = cursor.nullableInt(humidityIndex),
                    precipitation = cursor.nullableDouble(precipitationIndex),
                    precipitationProbability = cursor.nullableInt(probabilityIndex),
                    weatherCode = cursor.getInt(codeIndex),
                    isDay = cursor.getInt(dayIndex) == 1,
                    windSpeed = cursor.nullableDouble(windIndex),
                    windGust = cursor.nullableDouble(gustIndex),
                    windDirection = cursor.nullableInt(directionIndex),
                    visibility = cursor.nullableDouble(visibilityIndex),
                    dewPoint = cursor.nullableDouble(dewIndex),
                    surfacePressure = cursor.nullableDouble(pressureIndex),
                )
          }
        }
    return items
  }

  private fun queryDaily(latitude: Double, longitude: Double): List<DailyForecast> {
    val items = mutableListOf<DailyForecast>()
    readableDatabase
        .query(
            TABLE_DAILY,
            null,
            locationSelection,
            locationArguments(latitude, longitude),
            null,
            null,
            "$COLUMN_DATE ASC",
        )
        .use { cursor ->
          val dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE)
          val offsetIndex = cursor.getColumnIndexOrThrow(COLUMN_UTC_OFFSET)
          val maxIndex = cursor.getColumnIndexOrThrow(COLUMN_TEMPERATURE_MAX)
          val minIndex = cursor.getColumnIndexOrThrow(COLUMN_TEMPERATURE_MIN)
          val precipitationIndex = cursor.getColumnIndexOrThrow(COLUMN_PRECIPITATION_SUM)
          val codeIndex = cursor.getColumnIndexOrThrow(COLUMN_WEATHER_CODE)
          val sunriseIndex = cursor.getColumnIndexOrThrow(COLUMN_SUNRISE)
          val sunsetIndex = cursor.getColumnIndexOrThrow(COLUMN_SUNSET)
          val uvIndex = cursor.getColumnIndexOrThrow(COLUMN_UV_INDEX)
          while (cursor.moveToNext()) {
            items +=
                DailyForecast(
                    date = LocalDate.ofEpochDay(cursor.getLong(dateIndex)),
                    utcOffsetSeconds = cursor.getInt(offsetIndex),
                    temperatureMax = cursor.nullableDouble(maxIndex),
                    temperatureMin = cursor.nullableDouble(minIndex),
                    precipitationSum = cursor.nullableDouble(precipitationIndex),
                    weatherCode = cursor.getInt(codeIndex),
                    sunrise =
                        cursor.nullableLong(sunriseIndex)?.let { Instant.ofEpochMilli(it) },
                    sunset = cursor.nullableLong(sunsetIndex)?.let { Instant.ofEpochMilli(it) },
                    uvIndexMax = cursor.nullableDouble(uvIndex),
                )
          }
        }
    return items
  }

  private fun HourlyForecast.toValues(latitude: Double, longitude: Double) =
      ContentValues().apply {
        put(COLUMN_LATITUDE, latitude)
        put(COLUMN_LONGITUDE, longitude)
        put(COLUMN_TIME, time.toEpochMilli())
        put(COLUMN_UTC_OFFSET, utcOffsetSeconds)
        put(COLUMN_TEMPERATURE, temperature)
        put(COLUMN_APPARENT, apparentTemperature)
        put(COLUMN_HUMIDITY, humidity)
        put(COLUMN_PRECIPITATION, precipitation)
        put(COLUMN_PRECIPITATION_PROBABILITY, precipitationProbability)
        put(COLUMN_WEATHER_CODE, weatherCode)
        put(COLUMN_IS_DAY, if (isDay) 1 else 0)
        put(COLUMN_WIND_SPEED, windSpeed)
        put(COLUMN_WIND_GUST, windGust)
        put(COLUMN_WIND_DIRECTION, windDirection)
        put(COLUMN_VISIBILITY, visibility)
        put(COLUMN_DEW_POINT, dewPoint)
        put(COLUMN_SURFACE_PRESSURE, surfacePressure)
      }

  private fun DailyForecast.toValues(latitude: Double, longitude: Double) =
      ContentValues().apply {
        put(COLUMN_LATITUDE, latitude)
        put(COLUMN_LONGITUDE, longitude)
        put(COLUMN_DATE, date.toEpochDay())
        put(COLUMN_UTC_OFFSET, utcOffsetSeconds)
        put(COLUMN_TEMPERATURE_MAX, temperatureMax)
        put(COLUMN_TEMPERATURE_MIN, temperatureMin)
        put(COLUMN_PRECIPITATION_SUM, precipitationSum)
        put(COLUMN_WEATHER_CODE, weatherCode)
        put(COLUMN_SUNRISE, sunrise?.toEpochMilli())
        put(COLUMN_SUNSET, sunset?.toEpochMilli())
        put(COLUMN_UV_INDEX, uvIndexMax)
      }

  private val locationSelection: String = "$COLUMN_LATITUDE = ? AND $COLUMN_LONGITUDE = ?"

  private fun locationArguments(latitude: Double, longitude: Double): Array<String> =
      arrayOf(latitude.toString(), longitude.toString())

  private fun android.database.Cursor.nullableDouble(index: Int): Double? =
      if (isNull(index)) null else getDouble(index)

  private fun android.database.Cursor.nullableInt(index: Int): Int? =
      if (isNull(index)) null else getInt(index)

  private fun android.database.Cursor.nullableLong(index: Int): Long? =
      if (isNull(index)) null else getLong(index)

  companion object {
    const val DATABASE_NAME = "meteo.db"
    private const val VERSION = 2

    private const val TABLE_CITY = "city"
    private const val TABLE_HOURLY = "hourly_forecast"
    private const val TABLE_DAILY = "daily_forecast"
    private const val TABLE_SYNC = "forecast_sync"

    private const val COLUMN_ID = "id"
    private const val COLUMN_NAME = "name"
    private const val COLUMN_COUNTRY = "country"
    private const val COLUMN_ADMIN1 = "admin1"
    private const val COLUMN_ADDED_AT = "added_at_epoch"
    private const val COLUMN_LATITUDE = "latitude"
    private const val COLUMN_LONGITUDE = "longitude"
    private const val COLUMN_TIME = "time_epoch"
    private const val COLUMN_DATE = "date_epoch_day"
    private const val COLUMN_UTC_OFFSET = "utc_offset_seconds"
    private const val COLUMN_TEMPERATURE = "temperature"
    private const val COLUMN_TEMPERATURE_MAX = "temperature_max"
    private const val COLUMN_TEMPERATURE_MIN = "temperature_min"
    private const val COLUMN_APPARENT = "apparent_temperature"
    private const val COLUMN_HUMIDITY = "humidity"
    private const val COLUMN_PRECIPITATION = "precipitation"
    private const val COLUMN_PRECIPITATION_PROBABILITY = "precipitation_probability"
    private const val COLUMN_PRECIPITATION_SUM = "precipitation_sum"
    private const val COLUMN_WEATHER_CODE = "weather_code"
    private const val COLUMN_IS_DAY = "is_day"
    private const val COLUMN_WIND_SPEED = "wind_speed"
    private const val COLUMN_WIND_GUST = "wind_gust"
    private const val COLUMN_WIND_DIRECTION = "wind_direction"
    private const val COLUMN_VISIBILITY = "visibility"
    private const val COLUMN_DEW_POINT = "dew_point"
    private const val COLUMN_SURFACE_PRESSURE = "surface_pressure"
    private const val COLUMN_SUNRISE = "sunrise_epoch"
    private const val COLUMN_SUNSET = "sunset_epoch"
    private const val COLUMN_UV_INDEX = "uv_index_max"
    private const val COLUMN_SYNCED_AT = "synced_at_epoch"

    private const val CREATE_CITY =
        "CREATE TABLE $TABLE_CITY (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NAME TEXT NOT NULL, " +
            "$COLUMN_COUNTRY TEXT NOT NULL, " +
            "$COLUMN_ADMIN1 TEXT, " +
            "$COLUMN_LATITUDE REAL NOT NULL, " +
            "$COLUMN_LONGITUDE REAL NOT NULL, " +
            "$COLUMN_ADDED_AT INTEGER NOT NULL)"

    private const val CREATE_HOURLY =
        "CREATE TABLE $TABLE_HOURLY (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_LATITUDE REAL NOT NULL, " +
            "$COLUMN_LONGITUDE REAL NOT NULL, " +
            "$COLUMN_TIME INTEGER NOT NULL, " +
            "$COLUMN_UTC_OFFSET INTEGER NOT NULL, " +
            "$COLUMN_TEMPERATURE REAL NOT NULL, " +
            "$COLUMN_APPARENT REAL, " +
            "$COLUMN_HUMIDITY INTEGER, " +
            "$COLUMN_PRECIPITATION REAL, " +
            "$COLUMN_PRECIPITATION_PROBABILITY INTEGER, " +
            "$COLUMN_WEATHER_CODE INTEGER NOT NULL, " +
            "$COLUMN_IS_DAY INTEGER NOT NULL, " +
            "$COLUMN_WIND_SPEED REAL, " +
            "$COLUMN_WIND_GUST REAL, " +
            "$COLUMN_WIND_DIRECTION INTEGER, " +
            "$COLUMN_VISIBILITY REAL, " +
            "$COLUMN_DEW_POINT REAL, " +
            "$COLUMN_SURFACE_PRESSURE REAL)"

    private const val CREATE_DAILY =
        "CREATE TABLE $TABLE_DAILY (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_LATITUDE REAL NOT NULL, " +
            "$COLUMN_LONGITUDE REAL NOT NULL, " +
            "$COLUMN_DATE INTEGER NOT NULL, " +
            "$COLUMN_UTC_OFFSET INTEGER NOT NULL, " +
            "$COLUMN_TEMPERATURE_MAX REAL, " +
            "$COLUMN_TEMPERATURE_MIN REAL, " +
            "$COLUMN_PRECIPITATION_SUM REAL, " +
            "$COLUMN_WEATHER_CODE INTEGER NOT NULL, " +
            "$COLUMN_SUNRISE INTEGER, " +
            "$COLUMN_SUNSET INTEGER, " +
            "$COLUMN_UV_INDEX REAL)"

    private const val CREATE_SYNC =
        "CREATE TABLE $TABLE_SYNC (" +
            "$COLUMN_LATITUDE REAL NOT NULL, " +
            "$COLUMN_LONGITUDE REAL NOT NULL, " +
            "$COLUMN_SYNCED_AT INTEGER NOT NULL, " +
            "PRIMARY KEY ($COLUMN_LATITUDE, $COLUMN_LONGITUDE))"

    private const val CREATE_CITY_INDEX =
        "CREATE UNIQUE INDEX index_city_location ON $TABLE_CITY ($COLUMN_LATITUDE, $COLUMN_LONGITUDE)"

    private const val CREATE_HOURLY_INDEX =
        "CREATE UNIQUE INDEX index_hourly_location_time ON $TABLE_HOURLY " +
            "($COLUMN_LATITUDE, $COLUMN_LONGITUDE, $COLUMN_TIME)"

    private const val CREATE_DAILY_INDEX =
        "CREATE UNIQUE INDEX index_daily_location_date ON $TABLE_DAILY " +
            "($COLUMN_LATITUDE, $COLUMN_LONGITUDE, $COLUMN_DATE)"
  }
}
