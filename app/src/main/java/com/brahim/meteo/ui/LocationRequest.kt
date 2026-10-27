package com.brahim.meteo.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.brahim.meteo.data.model.Place
import com.brahim.meteo.util.findCurrentPlace
import com.brahim.meteo.util.hasLocationPermission
import kotlinx.coroutines.launch

@Stable
class LocationRequestState(val isLoading: Boolean, private val launch: () -> Unit) {
  fun request() {
    if (!isLoading) launch()
  }
}

/**
 * Small reusable helper shared by onboarding, search and the cities screen. It asks for the
 * runtime permission once, resolves the current position, then reverse geocodes it.
 */
@Composable
fun rememberLocationRequest(
    onPlace: (Place) -> Unit,
    onFailure: () -> Unit,
): LocationRequestState {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val currentOnPlace by rememberUpdatedState(onPlace)
  val currentOnFailure by rememberUpdatedState(onFailure)
  var isLoading by remember { mutableStateOf(false) }

  fun resolve() {
    if (isLoading) return
    isLoading = true
    scope.launch {
      val place = findCurrentPlace(context)
      isLoading = false
      if (place == null) currentOnFailure() else currentOnPlace(place)
    }
  }

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()
      ) { result ->
        if (result.values.any { granted -> granted }) resolve() else currentOnFailure()
      }

  val request = {
    if (hasLocationPermission(context)) resolve()
    else
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
  }

  return remember(isLoading) { LocationRequestState(isLoading = isLoading, launch = request) }
}
