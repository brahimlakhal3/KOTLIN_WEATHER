package com.brahim.meteo

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.component.AppShell
import com.brahim.meteo.component.MeteoSplashScreen
import com.brahim.meteo.component.OnboardingScreen
import com.brahim.meteo.ui.theme.AppTheme
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.ui.theme.isDark
import com.brahim.meteo.viewModel.AppConfigVM
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun MainScreen() {
  val activity = LocalActivity.current
  val configViewModel: AppConfigVM = viewModel(factory = AppConfigVM.Factory)
  val config by configViewModel.config.collectAsStateWithLifecycle()
  val state = config
  var splashFinished by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(SPLASH_MIN_MILLIS)
    splashFinished = true
  }

  if (state == null) {
    val dark = isSystemInDarkTheme()
    SystemBars(darkTheme = dark, activity = activity)
    MeteoSplashScreen(darkTheme = dark)
    return
  }

  SystemBars(darkTheme = state.theme.isDark(), activity = activity)

  AppTheme(
      themeMode = state.theme,
      accentColor = state.accent,
      cornerStyle = state.corners,
      textSize = state.size,
      animationsEnabled = state.animations,
      units = state.units,
  ) {
    ProvideAppLocale(state.language) {
      Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          if (state.onboardingDone) {
            AppShell(
                configViewModel = configViewModel,
                startScreen = state.startScreen,
                homeLatitude = state.homeLatitude,
                homeLongitude = state.homeLongitude,
            )
          } else {
            OnboardingScreen(configViewModel = configViewModel)
          }
        }
        AnimatedVisibility(
            visible = !splashFinished,
            enter = fadeIn(tween(0)),
            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = SPLASH_EXIT_MILLIS,
                            easing = Motion.emphasizedAccelerate,
                        )
                ) +
                    scaleOut(
                        animationSpec =
                            tween(
                                durationMillis = SPLASH_EXIT_MILLIS,
                                easing = Motion.emphasizedAccelerate,
                            ),
                        targetScale = 1.04f,
                    ),
            modifier = Modifier.fillMaxSize(),
        ) {
          MeteoSplashScreen(
              darkTheme = state.theme.isDark(),
              animations = state.animations,
          )
        }
      }
    }
  }
}

private const val SPLASH_MIN_MILLIS = 850L
private const val SPLASH_EXIT_MILLIS = 420

@Composable
private fun SystemBars(darkTheme: Boolean, activity: Activity?) {
  val view = LocalView.current
  SideEffect {
    val window = activity?.window ?: return@SideEffect
    val controller = WindowInsetsControllerCompat(window, view)
    controller.isAppearanceLightStatusBars = !darkTheme
    controller.isAppearanceLightNavigationBars = !darkTheme
  }
}

@Composable
private fun ProvideAppLocale(language: String, content: @Composable () -> Unit) {
  val context = LocalContext.current
  val currentConfiguration = LocalConfiguration.current
  // The localized context below is a plain configuration context, not the Activity. Compose
  // resolves ActivityResultRegistryOwner from LocalContext, so keep the Activity's owner
  // explicitly available for rememberLauncherForActivityResult().
  val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
  val localized =
      remember(language, context, currentConfiguration) {
        val configuration = Configuration(currentConfiguration)
        if (language.isNotBlank()) configuration.setLocale(Locale.forLanguageTag(language))
        configuration to context.createConfigurationContext(configuration)
      }
  CompositionLocalProvider(
      LocalConfiguration provides localized.first,
      LocalContext provides localized.second,
      LocalResources provides localized.second.resources,
      LocalActivityResultRegistryOwner provides checkNotNull(activityResultRegistryOwner),
      content = content,
  )
}
