package com.brahim.meteo.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.brahim.meteo.R
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.ui.theme.Motion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
    )

@Composable
fun MenuButton(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(imageVector = Icons.Outlined.Menu, contentDescription = stringResource(R.string.menu))
  }
}

@Composable
fun BackButton(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = stringResource(R.string.back),
    )
  }
}

@Composable
fun RefreshButton(isSyncing: Boolean, onRefresh: () -> Unit) {
  val animations = LocalAnimationsEnabled.current
  val angle: Float =
      if (animations) {
        val transition = rememberInfiniteTransition(label = "refresh")
        val value by
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(Motion.LONG_2, easing = LinearEasing)
                    ),
                label = "angle",
            )
        value
      } else {
        0f
      }

  IconButton(onClick = onRefresh, enabled = !isSyncing) {
    Icon(
        imageVector = Icons.Outlined.Refresh,
        contentDescription = stringResource(R.string.refresh),
        modifier = Modifier.rotate(if (isSyncing) angle else 0f),
    )
  }
}
