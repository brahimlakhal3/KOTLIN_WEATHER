package com.brahim.meteo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.R
import com.brahim.meteo.data.model.City
import com.brahim.meteo.element.CityWeatherCard
import com.brahim.meteo.element.StateMessage
import com.brahim.meteo.ui.rememberLocationRequest
import com.brahim.meteo.util.isSameCity
import com.brahim.meteo.viewModel.CitiesVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CitiesScreen(
    onOpenMenu: () -> Unit,
    onOpenCity: (City) -> Unit,
    onAddCity: () -> Unit,
    modifier: Modifier = Modifier,
    homeLatitude: Double? = null,
    homeLongitude: Double? = null,
    onSetHomeCity: (City) -> Unit = {},
    onClearHomeCity: () -> Unit = {},
) {
  val viewModel: CitiesVM = viewModel(factory = CitiesVM.Factory)
  val cities by viewModel.cities.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val deletedMessage = stringResource(R.string.snack_deleted)
  val undoLabel = stringResource(R.string.action_cancel)
  var pendingDeletion by remember { mutableStateOf<City?>(null) }
  var menuExpanded by remember { mutableStateOf(false) }
  var locationFailed by remember { mutableStateOf(false) }
  val locationMessage = stringResource(R.string.location_failed)
  val locationRequest =
      rememberLocationRequest(
          onPlace = { place ->
            viewModel.addPlace(place) { city -> onOpenCity(city) }
          },
          onFailure = { locationFailed = true },
      )

  LaunchedEffect(locationFailed) {
    if (locationFailed) {
      snackbarHostState.showSnackbar(locationMessage)
      locationFailed = false
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        LargeFlexibleTopAppBar(
            title = {
              Text(
                  text = stringResource(R.string.screen_weather),
                  style = MaterialTheme.typography.headlineMedium,
              )
            },
            subtitle = {
              Text(
                  text = stringResource(R.string.app_tagline),
                  style = MaterialTheme.typography.bodyMedium,
              )
            },
            navigationIcon = { MenuButton(onClick = onOpenMenu) },
            colors = appBarColors(),
            scrollBehavior = scrollBehavior,
        )
      },
      floatingActionButton = {
        FloatingActionButtonMenu(
            expanded = menuExpanded,
            button = {
              ToggleFloatingActionButton(
                  checked = menuExpanded,
                  onCheckedChange = { menuExpanded = it },
              ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.cities_add),
                    modifier = Modifier.size(26.dp),
                )
              }
            },
        ) {
          FloatingActionButtonMenuItem(
              onClick = {
                menuExpanded = false
                onAddCity()
              },
              icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
              text = { Text(stringResource(R.string.cities_add_search)) },
          )
          FloatingActionButtonMenuItem(
              onClick = {
                menuExpanded = false
                locationRequest.request()
              },
              icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null) },
              text = { Text(stringResource(R.string.cities_add_location)) },
          )
        }
      },
  ) { innerPadding ->
    if (cities.isEmpty()) {
      Box(
          modifier =
              Modifier.padding(innerPadding)
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState()),
          contentAlignment = Alignment.Center,
      ) {
        StateMessage(
            icon = Icons.Outlined.Cloud,
            title = stringResource(R.string.cities_empty_title),
            message = stringResource(R.string.cities_empty_hint),
            action = {
              FilledTonalButton(onClick = onAddCity) {
                Text(stringResource(R.string.cities_add))
              }
            },
        )
      }
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
              PaddingValues(
                  start = 12.dp,
                  end = 12.dp,
                  top = innerPadding.calculateTopPadding() + 4.dp,
                  bottom = innerPadding.calculateBottomPadding() + 104.dp,
              ),
          verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item {
          Text(
              text = stringResource(R.string.cities_home_hint),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
          )
        }
        items(items = cities, key = { entry -> entry.city.id }) { entry ->
          CityWeatherCard(
              city = entry.city,
              temperature = entry.conditions?.temperature,
              temperatureMax = entry.today?.temperatureMax,
              temperatureMin = entry.today?.temperatureMin,
              weatherCode = entry.conditions?.weatherCode,
              isDay = entry.conditions?.isDay ?: true,
              isHome = isHomeCity(entry.city, homeLatitude, homeLongitude),
              onClick = { onOpenCity(entry.city) },
              onDelete = { pendingDeletion = entry.city },
              modifier = Modifier.animateItem(),
          )
        }
        item {
          Text(
              text = stringResource(R.string.attribution),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
          )
        }
      }
    }
  }

  pendingDeletion?.let { city ->
    AlertDialog(
        onDismissRequest = { pendingDeletion = null },
        icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
        title = { Text(stringResource(R.string.cities_delete_title)) },
        text = { Text(stringResource(R.string.cities_delete_text, city.name)) },
        confirmButton = {
          TextButton(
              onClick = {
                val wasHome = isHomeCity(city, homeLatitude, homeLongitude)
                viewModel.delete(city.id)
                if (wasHome) onClearHomeCity()
                pendingDeletion = null
                scope.launch {
                  val result =
                      snackbarHostState.showSnackbar(
                          message = deletedMessage,
                          actionLabel = undoLabel,
                      )
                  if (result == SnackbarResult.ActionPerformed) {
                    viewModel.restore(city)
                    if (wasHome) onSetHomeCity(city)
                  }
                }
              },
          ) {
            Text(stringResource(R.string.action_delete))
          }
        },
        dismissButton = {
          TextButton(onClick = { pendingDeletion = null }) {
            Text(stringResource(R.string.action_cancel))
          }
        },
    )
  }
}

private fun isHomeCity(city: City, latitude: Double?, longitude: Double?): Boolean =
    latitude != null &&
        longitude != null &&
        isSameCity(city.latitude, city.longitude, latitude, longitude)
