package com.brahim.meteo.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.brahim.meteo.R
import com.brahim.meteo.data.model.Place
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
suspend fun findCurrentPlace(context: Context): Place? {
  if (!hasLocationPermission(context)) return null
  return withContext(Dispatchers.IO) {
    val location = currentLocation(context) ?: return@withContext null
    reverseGeocode(context, location)
  }
}

@SuppressLint("MissingPermission")
private suspend fun currentLocation(context: Context): Location? {
  val manager = context.getSystemService(LocationManager::class.java) ?: return null
  val fineGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED
  // GPS needs a precise grant. With approximate-only access, keep to the network provider.
  val activeProviders =
      buildList {
            if (fineGranted) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
          }
          .filter { provider ->
            runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
          }
  val lastKnownProviders =
      (activeProviders + LocationManager.PASSIVE_PROVIDER).distinct()
  val lastKnown =
      lastKnownProviders
          .mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
          }
          .maxByOrNull { location -> location.time }
  val recent =
      lastKnown?.takeIf { location ->
        System.currentTimeMillis() - location.time in 0..FRESH_LOCATION_AGE_MILLIS
      }
  if (recent != null) return recent

  if (activeProviders.isEmpty()) return lastKnown

  val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        suspendCancellableCoroutine { continuation ->
          manager.getCurrentLocation(
              activeProviders.first(),
              null,
              ContextCompat.getMainExecutor(context),
          ) { location ->
            if (continuation.isActive) continuation.resume(location)
          }
        }
      } else {
        requestSingleLocation(manager, activeProviders.first())
      }
    } catch (error: SecurityException) {
      null
    }
  }
  return fresh ?: lastKnown
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
private suspend fun requestSingleLocation(
    manager: LocationManager,
    provider: String,
): Location? = suspendCancellableCoroutine { continuation ->
  val listener =
      object : LocationListener {
        override fun onLocationChanged(location: Location) {
          if (continuation.isActive) continuation.resume(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) = Unit

        override fun onProviderDisabled(provider: String) {
          if (continuation.isActive) continuation.resume(null)
        }
      }
  manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
  continuation.invokeOnCancellation { manager.removeUpdates(listener) }
}

private fun reverseGeocode(context: Context, location: Location): Place {
  val fallbackName = context.getString(R.string.current_location)
  val appLocale = context.resources.configuration.locales[0] ?: Locale.getDefault()
  val address =
      runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, appLocale)
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
          }
          .getOrNull()
  val name =
      listOfNotNull(address?.locality, address?.subAdminArea, address?.adminArea)
          .firstOrNull { value -> value.isNotBlank() }
          ?: fallbackName
  return Place(
      name = name,
      country = address?.countryName.orEmpty(),
      admin1 =
          listOfNotNull(address?.subAdminArea, address?.adminArea)
              .firstOrNull { value -> value.isNotBlank() },
      latitude = location.latitude,
      longitude = location.longitude,
  )
}

private const val LOCATION_TIMEOUT_MILLIS = 9_000L
private const val FRESH_LOCATION_AGE_MILLIS = 5 * 60_000L
