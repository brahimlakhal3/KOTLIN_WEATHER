package com.brahim.meteo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahim.meteo.BuildConfig
import com.brahim.meteo.R
import com.brahim.meteo.element.AppIcon
import com.brahim.meteo.element.IconBadge
import com.brahim.meteo.element.Reveal
import com.brahim.meteo.ui.AppLanguage
import com.brahim.meteo.ui.theme.AccentColor
import com.brahim.meteo.ui.theme.CornerStyle
import com.brahim.meteo.ui.theme.TextSize
import com.brahim.meteo.ui.theme.ThemeMode
import com.brahim.meteo.util.DistanceUnit
import com.brahim.meteo.util.PrecipitationUnit
import com.brahim.meteo.util.PressureUnit
import com.brahim.meteo.util.TemperatureUnit
import com.brahim.meteo.util.TimeFormat
import com.brahim.meteo.util.WindUnit
import com.brahim.meteo.viewModel.AppConfigVM
import com.brahim.meteo.viewModel.SettingsVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    configViewModel: AppConfigVM,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val config by configViewModel.config.collectAsStateWithLifecycle()
  val state = config ?: return
  val settingsViewModel: SettingsVM = viewModel(factory = SettingsVM.Factory)
  val cityCount by settingsViewModel.cityCount.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val clearedMessage = stringResource(R.string.snack_cache_cleared)
  val resetMessage = stringResource(R.string.settings_reset_done)
  var confirmClear by remember { mutableStateOf(false) }
  var confirmReset by remember { mutableStateOf(false) }
  var confirmReplay by remember { mutableStateOf(false) }

  Scaffold(
      modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        LargeFlexibleTopAppBar(
            title = {
              Text(
                  text = stringResource(R.string.screen_settings),
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
  ) { innerPadding ->
    Column(
        modifier =
            Modifier.padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Reveal(index = 0) {
        SettingsCard(
            icon = Icons.Outlined.Palette,
            title = stringResource(R.string.settings_appearance),
            accentIndex = 0,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(items = ThemeMode.entries.toList(), key = { entry -> entry.name }) { entry ->
                FilterChip(
                    selected = state.theme == entry,
                    onClick = { configViewModel.setMode(entry.name) },
                    label = { Text(stringResource(entry.labelRes)) },
                )
              }
            }
            SettingBlock(title = stringResource(R.string.settings_accent)) {
              LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = AccentColor.entries.toList(), key = { entry -> entry.name }) { entry ->
                  AccentSwatch(
                      accent = entry,
                      selected = state.accent == entry,
                      onClick = { configViewModel.setAccent(entry.name) },
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                  text = stringResource(state.accent.labelRes),
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
              )
            }
            SettingBlock(title = stringResource(R.string.settings_corners)) {
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CornerStyle.entries.forEachIndexed { index, entry ->
                  SegmentedButton(
                      selected = state.corners == entry,
                      onClick = { configViewModel.setCornerStyle(entry.name) },
                      shape =
                          SegmentedButtonDefaults.itemShape(
                              index = index,
                              count = CornerStyle.entries.size,
                          ),
                      label = { Text(stringResource(entry.labelRes)) },
                  )
                }
              }
            }
            SettingBlock(title = stringResource(R.string.settings_text_size)) {
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TextSize.entries.forEachIndexed { index, entry ->
                  SegmentedButton(
                      selected = state.size == entry,
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
            SettingSwitch(
                title = stringResource(R.string.settings_animations),
                description = stringResource(R.string.settings_animations_desc),
                checked = state.animations,
                onCheckedChange = configViewModel::setAnimations,
            )
          }
        }
      }

      Reveal(index = 1) {
        SettingsCard(
            icon = Icons.Outlined.Language,
            title = stringResource(R.string.settings_language),
            accentIndex = 1,
        ) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = AppLanguage.entries.toList(), key = { entry -> entry.code }) { entry ->
              FilterChip(
                  selected = state.appLanguage == entry,
                  onClick = { configViewModel.setLanguage(entry.code) },
                  label = { Text(entry.nativeName) },
              )
            }
          }
        }
      }

      Reveal(index = 2) {
        SettingsCard(
            icon = Icons.Outlined.Straighten,
            title = stringResource(R.string.settings_units),
            accentIndex = 2,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            UnitChoice(
                title = stringResource(R.string.settings_temperature_unit),
                options = TemperatureUnit.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.temperature.name,
                onSelect = configViewModel::setTemperatureUnit,
            )
            UnitChoice(
                title = stringResource(R.string.settings_wind_unit),
                options = WindUnit.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.wind.name,
                onSelect = configViewModel::setWindUnit,
            )
            UnitChoice(
                title = stringResource(R.string.settings_precipitation_unit),
                options =
                    PrecipitationUnit.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.precipitation.name,
                onSelect = configViewModel::setPrecipitationUnit,
            )
            UnitChoice(
                title = stringResource(R.string.settings_distance_unit),
                options = DistanceUnit.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.distance.name,
                onSelect = configViewModel::setDistanceUnit,
            )
            UnitChoice(
                title = stringResource(R.string.settings_pressure_unit),
                options = PressureUnit.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.pressure.name,
                onSelect = configViewModel::setPressureUnit,
            )
            UnitChoice(
                title = stringResource(R.string.settings_time_format),
                options = TimeFormat.entries.map { it.name to stringResource(it.labelRes) },
                selected = state.units.timeFormat.name,
                onSelect = configViewModel::setTimeFormat,
            )
            UnitChoice(
                title = stringResource(R.string.settings_decimals),
                options =
                    (0..2).map { decimals ->
                        decimals.toString() to
                            stringResource(R.string.decimals_count, decimals)
                    },
                selected = state.temperatureDecimals.toString(),
                onSelect = { value -> configViewModel.setTemperatureDecimals(value.toInt()) },
            )
          }
        }
      }

      Reveal(index = 3) {
        SettingsCard(
            icon = Icons.Outlined.Tune,
            title = stringResource(R.string.settings_weather_display),
            accentIndex = 0,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingSwitch(
                title = stringResource(R.string.settings_show_alerts),
                description = stringResource(R.string.settings_show_alerts_desc),
                checked = state.showAlerts,
                onCheckedChange = configViewModel::setShowAlerts,
            )
            SettingSwitch(
                title = stringResource(R.string.settings_show_air_quality),
                description = stringResource(R.string.settings_show_air_quality_desc),
                checked = state.showAirQuality,
                onCheckedChange = configViewModel::setShowAirQuality,
            )
          }
        }
      }

      Reveal(index = 4) {
        SettingsCard(
            icon = Icons.Outlined.Storage,
            title = stringResource(R.string.settings_data),
            accentIndex = 1,
        ) {
          ListItem(
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
          ) {
            Text(stringResource(R.string.settings_cache_title))
          }
          ListItem(
              supportingContent = { Text(stringResource(R.string.settings_reset_text)) },
              leadingContent = { Icon(Icons.Outlined.RestartAlt, contentDescription = null) },
              trailingContent = {
                TextButton(onClick = { confirmReset = true }) {
                  Text(stringResource(R.string.action_reset))
                }
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(stringResource(R.string.settings_reset))
          }
          ListItem(
              supportingContent = {
                Text(stringResource(R.string.settings_replay_onboarding_desc))
              },
              leadingContent = { Icon(Icons.Outlined.Cloud, contentDescription = null) },
              trailingContent = {
                TextButton(onClick = { confirmReplay = true }) {
                  Text(stringResource(R.string.action_replay))
                }
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(stringResource(R.string.settings_replay_onboarding))
          }
        }
      }

      Reveal(index = 5) {
        SettingsCard(
            icon = Icons.Outlined.Info,
            title = stringResource(R.string.settings_about),
            accentIndex = 2,
        ) {
          ListItem(
              supportingContent = {
                Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME))
              },
              leadingContent = { Icon(Icons.Outlined.Cloud, contentDescription = null) },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(stringResource(R.string.app_name))
          }
          ListItem(
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(
                text = stringResource(R.string.settings_about_text),
                style = MaterialTheme.typography.bodyMedium,
            )
          }
          ListItem(
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(
                text = stringResource(R.string.attribution),
                style = MaterialTheme.typography.bodyMedium,
            )
          }
          ListItem(
              colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          ) {
            Text(
                text = stringResource(R.string.attribution_icons),
                style = MaterialTheme.typography.bodyMedium,
            )
          }
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

  if (confirmReset) {
    AlertDialog(
        onDismissRequest = { confirmReset = false },
        icon = { Icon(Icons.Outlined.RestartAlt, contentDescription = null) },
        title = { Text(stringResource(R.string.settings_reset_title)) },
        text = { Text(stringResource(R.string.settings_reset_text)) },
        confirmButton = {
          TextButton(
              onClick = {
                configViewModel.resetPreferences()
                confirmReset = false
                scope.launch { snackbarHostState.showSnackbar(resetMessage) }
              },
          ) {
            Text(stringResource(R.string.action_reset))
          }
        },
        dismissButton = {
          TextButton(onClick = { confirmReset = false }) {
            Text(stringResource(R.string.action_cancel))
          }
        },
    )
  }

  if (confirmReplay) {
    AlertDialog(
        onDismissRequest = { confirmReplay = false },
        icon = { Icon(Icons.Outlined.Cloud, contentDescription = null) },
        title = { Text(stringResource(R.string.settings_replay_onboarding)) },
        text = { Text(stringResource(R.string.onboarding_finding_body)) },
        confirmButton = {
          TextButton(
              onClick = {
                configViewModel.resetOnboarding()
                confirmReplay = false
              },
          ) {
            Text(stringResource(R.string.action_replay))
          }
        },
        dismissButton = {
          TextButton(onClick = { confirmReplay = false }) {
            Text(stringResource(R.string.action_cancel))
          }
        },
    )
  }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    accentIndex: Int,
    content: @Composable () -> Unit,
) {
  val container =
      when (accentIndex % 3) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
      }
  val contentColor =
      when (accentIndex % 3) {
        0 -> MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
      }
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(
            icon = AppIcon.Vector(icon),
            badgeSize = 40.dp,
            iconSize = 22.dp,
            containerColor = container,
            contentColor = contentColor,
        )
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

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyLarge)
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun UnitChoice(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
  SettingBlock(title = title) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = options, key = { option -> option.first }) { (value, label) ->
        FilterChip(
            selected = selected == value,
            onClick = { onSelect(value) },
            label = { Text(label) },
        )
      }
    }
  }
}

@Composable
private fun AccentSwatch(accent: AccentColor, selected: Boolean, onClick: () -> Unit) {
  val label = stringResource(accent.labelRes)
  val checkColor = if (accent.base.luminance() > 0.55f) Color(0xFF171717) else Color.White
  Box(
      modifier =
          Modifier.size(48.dp)
              .background(
                  brush = Brush.linearGradient(listOf(accent.base, accent.alt)),
                  shape = CircleShape,
              )
              .border(
                  width = if (selected) 3.dp else 1.dp,
                  color =
                      if (selected) MaterialTheme.colorScheme.onBackground
                      else MaterialTheme.colorScheme.outlineVariant,
                  shape = CircleShape,
              )
              .selectable(
                  selected = selected,
                  onClick = onClick,
                  role = Role.RadioButton,
              )
              .semantics { contentDescription = label },
      contentAlignment = Alignment.Center,
  ) {
    if (selected) {
      Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = checkColor,
          modifier = Modifier.size(22.dp),
      )
    }
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

private val CornerStyle.labelRes: Int
  get() =
      when (this) {
        CornerStyle.EXPRESSIVE -> R.string.corner_expressive
        CornerStyle.ROUND -> R.string.corner_round
        CornerStyle.COMPACT -> R.string.corner_compact
      }

private val AccentColor.labelRes: Int
  get() =
      when (this) {
        AccentColor.OCEAN -> R.string.accent_ocean
        AccentColor.SKY -> R.string.accent_sky
        AccentColor.CYAN -> R.string.accent_cyan
        AccentColor.TEAL -> R.string.accent_teal
        AccentColor.MINT -> R.string.accent_mint
        AccentColor.EMERALD -> R.string.accent_emerald
        AccentColor.LIME -> R.string.accent_lime
        AccentColor.SUN -> R.string.accent_sun
        AccentColor.AMBER -> R.string.accent_amber
        AccentColor.CORAL -> R.string.accent_coral
        AccentColor.ROSE -> R.string.accent_rose
        AccentColor.BERRY -> R.string.accent_berry
        AccentColor.VIOLET -> R.string.accent_violet
        AccentColor.INDIGO -> R.string.accent_indigo
        AccentColor.PEACH -> R.string.accent_peach
        AccentColor.GRAPHITE -> R.string.accent_graphite
      }

private val TemperatureUnit.labelRes: Int
  get() =
      when (this) {
        TemperatureUnit.CELSIUS -> R.string.unit_celsius
        TemperatureUnit.FAHRENHEIT -> R.string.unit_fahrenheit
      }

private val WindUnit.labelRes: Int
  get() =
      when (this) {
        WindUnit.KILOMETRES_PER_HOUR -> R.string.wind_kmh
        WindUnit.MILES_PER_HOUR -> R.string.wind_mph
        WindUnit.METRES_PER_SECOND -> R.string.wind_ms
        WindUnit.KNOTS -> R.string.wind_knots
      }

private val PrecipitationUnit.labelRes: Int
  get() =
      when (this) {
        PrecipitationUnit.MILLIMETRES -> R.string.precip_mm
        PrecipitationUnit.INCHES -> R.string.precip_inches
      }

private val DistanceUnit.labelRes: Int
  get() =
      when (this) {
        DistanceUnit.KILOMETRES -> R.string.distance_km
        DistanceUnit.MILES -> R.string.distance_miles
      }

private val PressureUnit.labelRes: Int
  get() =
      when (this) {
        PressureUnit.HECTOPASCALS -> R.string.pressure_hpa
        PressureUnit.INCHES_OF_MERCURY -> R.string.pressure_inhg
      }

private val TimeFormat.labelRes: Int
  get() =
      when (this) {
        TimeFormat.HOUR_24 -> R.string.time_24h
        TimeFormat.HOUR_12 -> R.string.time_12h
      }
