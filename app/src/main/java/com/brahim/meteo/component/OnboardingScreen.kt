package com.brahim.meteo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.R
import com.brahim.meteo.element.WeatherIcon
import com.brahim.meteo.ui.rememberLocationRequest
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.viewModel.AppConfigVM
import com.brahim.meteo.viewModel.OnboardingVM

@Composable
fun OnboardingScreen(configViewModel: AppConfigVM) {
  val onboardingViewModel: OnboardingVM = viewModel(factory = OnboardingVM.Factory)
  var locationFailed by remember { mutableStateOf(false) }
  var saving by remember { mutableStateOf(false) }
  val animations = LocalAnimationsEnabled.current
  val locationRequest =
      rememberLocationRequest(
          onPlace = { place ->
            saving = true
            onboardingViewModel.savePlace(
                place = place,
                onSaved = {
                  saving = false
                  configViewModel.completeOnboarding(
                      mode = AppConfigVM.START_MODE_LOCATION,
                      latitude = place.latitude,
                      longitude = place.longitude,
                  )
                },
                onError = {
                  saving = false
                  locationFailed = true
                },
            )
          },
          onFailure = { locationFailed = true },
      )

  // No chooser: ask for the current location immediately and go straight to the city.
  LaunchedEffect(Unit) { locationRequest.request() }

  val busy = locationRequest.isLoading || saving
  val startSearch = {
    if (!busy) {
      configViewModel.completeOnboarding(AppConfigVM.START_MODE_SEARCH)
    }
  }

  val title =
      when {
        locationFailed -> stringResource(R.string.onboarding_location_failed)
        saving -> stringResource(R.string.onboarding_opening_title)
        else -> stringResource(R.string.onboarding_finding_title)
      }
  val body =
      stringResource(
          if (locationFailed) R.string.onboarding_search_hint
          else R.string.onboarding_finding_body
      )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      OnboardingHero(animations = animations)

      Spacer(modifier = Modifier.height(26.dp))
      Text(
          text = title,
          style = MaterialTheme.typography.displaySmallEmphasized,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
          text = body,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(28.dp))

      if (busy) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )
      } else if (locationFailed) {
        Button(onClick = startSearch, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.onboarding_search_title))
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextButton(
            onClick = {
              locationFailed = false
              locationRequest.request()
            }
        ) {
          Text(stringResource(R.string.state_retry))
        }
      } else {
        // The first frame before LaunchedEffect starts the request.
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )
      }

      Spacer(modifier = Modifier.height(22.dp))
      Text(
          text = stringResource(R.string.onboarding_footer),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun OnboardingHero(animations: Boolean) {
  Box(
      modifier = Modifier.fillMaxWidth().height(190.dp),
      contentAlignment = Alignment.Center,
  ) {
    Box(
        modifier =
            Modifier.size(178.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    )
                )
    )
    Box(
        modifier =
            Modifier.size(126.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        )
                    )
                )
    )
    WeatherIcon(
        weatherCode = 0,
        isDay = true,
        size = 84.dp,
        tint = MaterialTheme.colorScheme.onPrimary,
    )
    if (animations) {
      Box(
          modifier =
              Modifier.align(Alignment.TopStart)
                  .padding(start = 24.dp, top = 16.dp)
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.secondary)
      )
      Box(
          modifier =
              Modifier.align(Alignment.BottomEnd)
                  .padding(end = 30.dp, bottom = 18.dp)
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
      )
    }
  }
}

