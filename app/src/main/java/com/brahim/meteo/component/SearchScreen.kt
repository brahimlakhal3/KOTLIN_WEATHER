package com.brahim.meteo.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.R
import com.brahim.meteo.data.model.Place
import com.brahim.meteo.element.LoadingState
import com.brahim.meteo.element.PlaceCard
import com.brahim.meteo.element.StateMessage
import com.brahim.meteo.ui.rememberLocationRequest
import com.brahim.meteo.ui.rememberMinimumLoading
import com.brahim.meteo.ui.theme.LocalAnimationsEnabled
import com.brahim.meteo.viewModel.SearchVM

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    onOpenMenu: () -> Unit,
    onOpenPlace: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
  val viewModel: SearchVM = viewModel(factory = SearchVM.Factory)
  val state by viewModel.state.collectAsStateWithLifecycle()
  val showSearching = rememberMinimumLoading(state.isSearching)
  val keyboard = LocalSoftwareKeyboardController.current
  val animations = LocalAnimationsEnabled.current
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val snackbarHostState = remember { SnackbarHostState() }
  var locationFailed by remember { mutableStateOf(false) }
  val locationMessage = stringResource(R.string.location_failed)
  val locationRequest =
      rememberLocationRequest(
          onPlace = { place ->
            keyboard?.hide()
            viewModel.addPlace(place) { onOpenPlace(place) }
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
                  text = stringResource(R.string.title_search),
                  style = MaterialTheme.typography.headlineMedium,
              )
            },
            navigationIcon = { MenuButton(onClick = onOpenMenu) },
            actions = {
              IconButton(onClick = { locationRequest.request() }) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = stringResource(R.string.search_use_location),
                )
              }
            },
            colors = appBarColors(),
            scrollBehavior = scrollBehavior,
        )
      },
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
      OutlinedTextField(
          value = state.query,
          onValueChange = viewModel::onQueryChange,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          placeholder = { Text(stringResource(R.string.search_hint)) },
          leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
          },
          trailingIcon = {
            AnimatedVisibility(
                visible = state.query.isNotEmpty(),
                enter = if (animations) fadeIn() else EnterTransition.None,
                exit = if (animations) fadeOut() else ExitTransition.None,
            ) {
              IconButton(onClick = viewModel::clearQuery) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.search_clear),
                )
              }
            }
          },
          singleLine = true,
          shape = MaterialTheme.shapes.extraLarge,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow() }),
          colors =
              OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                  unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                  unfocusedBorderColor = Color.Transparent,
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
              ),
      )

      AnimatedVisibility(
          visible = showSearching,
          enter = if (animations) fadeIn() + expandVertically() else EnterTransition.None,
          exit = if (animations) fadeOut() + shrinkVertically() else ExitTransition.None,
      ) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
          LinearWavyProgressIndicator(
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
              modifier = Modifier.fillMaxWidth().height(6.dp),
          )
        }
      }

      when {
        state.results.isNotEmpty() ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(
                  items = state.results,
                  key = { place -> "${place.name}-${place.latitude}-${place.longitude}" },
              ) { place ->
                PlaceCard(
                    place = place,
                    onClick = {
                      keyboard?.hide()
                      viewModel.addPlace(place) { onOpenPlace(place) }
                    },
                    modifier = Modifier.animateItem(),
                )
              }
            }
        state.hasError ->
            StateMessage(
                icon = Icons.Outlined.Cloud,
                title = stringResource(R.string.state_error_title),
                message = stringResource(R.string.state_error_hint),
            )
        showSearching -> LoadingState(message = stringResource(R.string.state_loading))
        state.hasSearched ->
            StateMessage(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.search_no_result_title),
                message = stringResource(R.string.search_no_result_hint),
            )
        else ->
            StateMessage(
                icon = Icons.Outlined.Search,
                title = stringResource(R.string.search_empty_title),
                message = stringResource(R.string.search_empty_hint),
            )
      }
    }
  }
}
