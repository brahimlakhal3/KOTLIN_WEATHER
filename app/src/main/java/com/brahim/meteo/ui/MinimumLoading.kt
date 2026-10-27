package com.brahim.meteo.ui

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/** Keep short operations from flashing a spinner for a single frame. */
const val MINIMUM_LOADING_MILLIS = 500L

/**
 * Returns a loading flag that stays true for at least [minimumMillis] once it has appeared.
 * The underlying operation is never delayed; only the visible loading state is extended.
 */
@Composable
fun rememberMinimumLoading(
    loading: Boolean,
    minimumMillis: Long = MINIMUM_LOADING_MILLIS,
): Boolean {
  var visible by remember { mutableStateOf(loading) }
  var startedAt by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

  LaunchedEffect(loading, minimumMillis) {
    if (loading) {
      visible = true
      startedAt = SystemClock.elapsedRealtime()
    } else if (visible) {
      val remaining = minimumMillis - (SystemClock.elapsedRealtime() - startedAt)
      if (remaining > 0L) delay(remaining)
      visible = false
    }
  }

  return visible
}
