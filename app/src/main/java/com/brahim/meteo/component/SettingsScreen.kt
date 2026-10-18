package com.brahim.meteo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.BuildConfig
import com.brahim.meteo.R
import com.brahim.meteo.element.AppIcon
import com.brahim.meteo.element.IconBadge
import com.brahim.meteo.element.Reveal
import com.brahim.meteo.ui.theme.TextSize
import com.brahim.meteo.ui.theme.ThemeMode
import com.brahim.meteo.util.UnitSystem
import com.brahim.meteo.viewModel.AppConfigVM
import com.brahim.meteo.viewModel.SettingsVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    configViewModel: AppConfigVM,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val mode by configViewModel.mode.collectAsStateWithLifecycle()
  val language by configViewModel.language.collectAsStateWithLifecycle()
  val textSize by configViewModel.textSize.collectAsStateWithLifecycle()
  val units by configViewModel.units.collectAsStateWithLifecycle()
  val settingsViewModel: SettingsVM = viewModel(factory = SettingsVM.Factory)
  val cityCount by settingsViewModel.cityCount.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val clearedMessage = stringResource(R.string.snack_cache_cleared)
  var confirmClear by remember { mutableStateOf(false) }
  val themeMode = ThemeMode.from(mode)
  val size = TextSize.from(textSize)
  val unitSystem = UnitSystem.from(units)

  Scaffold(
      modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        LargeTopAppBar(
            title = {
              Text(
                  text = stringResource(R.string.screen_settings),
                  style = MaterialTheme.typography.headlineMedium,
              )
            },
            navigationIcon = { MenuButton(onClick = onOpenMenu) },
            colors = appBarColors(),
            scrollBehavior = scrollBehavior,
        )
      },
  ) { innerPadding ->
    Column(
        modifier =
            Modifier.padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Reveal(index = 0) {
        SettingsCard(
            icon = Icons.Outlined.Palette,
            title = stringResource(R.string.settings_appearance),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(items = ThemeMode.entries.toList(), key = { entry -> entry.name }) { entry ->
                FilterChip(
                    selected = themeMode == entry,
                    onClick = { configViewModel.setMode(entry.name) },
                    label = { Text(stringResource(entry.labelRes)) },
                )
              }
            }
            SettingBlock(title = stringResource(R.string.settings_text_size)) {
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TextSize.entries.forEachIndexed { index, entry ->
                  SegmentedButton(
                      selected = size == entry,
                      onClick = { configViewModel.setTextSize(entry.name) },
                      shape =
                          SegmentedButtonDefaults.itemShape(
                              index = index,
                              count = TextSize.entries.size,
                          ),
                      label = { Text(stringResource(entry.labelRes)) },
                  )
                }
              }
            }
          }
        }
      }

      Reveal(index = 1) {
        SettingsCard(
            icon = Icons.Outlined.Language,
            title = stringResource(R.string.settings_language),
        ) {
          val languages = listOf(AppConfigVM.LANGUAGE_FRENCH, AppConfigVM.LANGUAGE_ENGLISH)
          val selectedIndex = languages.indexOf(language).coerceAtLeast(0)
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            languages.forEachIndexed { index, code ->
              SegmentedButton(
                  selected = selectedIndex == index,
                  onClick = { configViewModel.setLanguage(code) },
                  shape =
                      SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
                  label = { Text(stringResource(languageNameRes(code))) },
              )
            }
          }
        }
      }

      Reveal(index = 2) {
        SettingsCard(
            icon = Icons.Outlined.Straighten,
            title = stringResource(R.string.settings_units),
        ) {
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            UnitSystem.entries.forEachIndexed { index, entry ->
              SegmentedButton(
                  selected = unitSystem == entry,
                  onClick = { configViewModel.setUnits(entry.name) },
                  shape =
                      SegmentedButtonDefaults.itemShape(
                          index = index,
                          count = UnitSystem.entries.size,
                      ),
                  label = { Text(stringResource(entry.labelRes)) },
              )
            }
          }
        }
      }

      Reveal(index = 3) {
        SettingsCard(
            icon = Icons.Outlined.Storage,
            title = stringResource(R.string.settings_data),
        ) {
          ListItem(
              headlineContent = { Text(stringResource(R.string.settings_cache_title)) },
              supportingContent = { Text(stringResource(R.string.settings_cache_count, cityCount)) },
              leadingContent = { Icon(Icons.Outlined.DeleteSweep, contentDescription = null) },
              trailingContent = {
                FilledTonalButton(
                    onClick = { confirmClear = true },
                    enabled = cityCount > 0,
                ) {
                  Text(stringResource(R.string.action_clear))
                }
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
        }
      }

      Reveal(index = 4) {
        SettingsCard(icon = Icons.Outlined.Info, title = stringResource(R.string.settings_about)) {
          ListItem(
              headlineContent = { Text(stringResource(R.string.app_name)) },
              supportingContent = {
                Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME))
              },
              leadingContent = { Icon(Icons.Outlined.Cloud, contentDescription = null) },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
          ListItem(
              headlineContent = {
                Text(
                    text = stringResource(R.string.settings_about_text),
                    style = MaterialTheme.typography.bodyMedium,
                )
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
          ListItem(
              headlineContent = {
                Text(
                    text = stringResource(R.string.attribution),
                    style = MaterialTheme.typography.bodyMedium,
                )
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
          ListItem(
              headlineContent = {
                Text(
                    text = stringResource(R.string.attribution_icons),
                    style = MaterialTheme.typography.bodyMedium,
                )
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
        }
      }
    }
  }

  if (confirmClear) {
    AlertDialog(
        onDismissRequest = { confirmClear = false },
        icon = { Icon(Icons.Outlined.DeleteSweep, contentDescription = null) },
        title = { Text(stringResource(R.string.settings_clear_cache_title)) },
        text = { Text(stringResource(R.string.settings_clear_cache_text)) },
        confirmButton = {
          TextButton(
              onClick = {
                settingsViewModel.clearCache()
                confirmClear = false
                scope.launch { snackbarHostState.showSnackbar(clearedMessage) }
              },
          ) {
            Text(stringResource(R.string.action_clear))
          }
        },
        dismissButton = {
          TextButton(onClick = { confirmClear = false }) {
            Text(stringResource(R.string.action_cancel))
          }
        },
    )
  }
}

@Composable
private fun SettingsCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = AppIcon.Vector(icon), badgeSize = 40.dp, iconSize = 22.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      content()
    }
  }
}

@Composable
private fun SettingBlock(title: String, content: @Composable () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    content()
  }
}

private val ThemeMode.labelRes: Int
  get() =
      when (this) {
        ThemeMode.AUTO -> R.string.theme_system
        ThemeMode.VERY_LIGHT -> R.string.theme_very_light
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.SOFT -> R.string.theme_soft
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.VERY_DARK -> R.string.theme_very_dark
      }

private val TextSize.labelRes: Int
  get() =
      when (this) {
        TextSize.SMALL -> R.string.text_size_small
        TextSize.NORMAL -> R.string.text_size_normal
        TextSize.LARGE -> R.string.text_size_large
      }

private val UnitSystem.labelRes: Int
  get() =
      when (this) {
        UnitSystem.METRIC -> R.string.units_metric
        UnitSystem.IMPERIAL -> R.string.units_imperial
      }

private fun languageNameRes(code: String): Int =
    when (code) {
      AppConfigVM.LANGUAGE_ENGLISH -> R.string.language_english
      else -> R.string.language_french
    }
