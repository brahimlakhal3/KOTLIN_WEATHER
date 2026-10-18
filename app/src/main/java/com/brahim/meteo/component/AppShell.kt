package com.brahim.meteo.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brahim.meteo.BuildConfig
import com.brahim.meteo.R
import com.brahim.meteo.element.DayMetric
import com.brahim.meteo.ui.theme.Motion
import com.brahim.meteo.viewModel.AppConfigVM
import kotlinx.coroutines.launch

@Composable
fun AppShell(configViewModel: AppConfigVM) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentScreen = Screen.fromRoute(backStackEntry?.destination?.route)

  BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

  val isDrawerOpen = drawerState.isOpen || (drawerState.targetValue == DrawerValue.Open)

  ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = (currentScreen != null) || drawerState.isOpen,
      drawerContent = {
        AppDrawer(
            current = currentScreen,
            isOpen = isDrawerOpen,
            onSelect = { screen ->
              scope.launch { drawerState.close() }
              if (screen != currentScreen) {
                navController.navigate(screen.route) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
        )
      },
  ) {
    val drawerProgress by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1f else 0f,
        animationSpec = Motion.gentleSpring(),
        label = "drawer_content_progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              val scale = 1f - (0.05f * drawerProgress)
              scaleX = scale
              scaleY = scale
              shadowElevation = (12 * drawerProgress).dp.toPx()
              shape = RoundedCornerShape((16 * drawerProgress).dp)
              clip = drawerProgress > 0.01f
            },
    ) {
      NavHost(
          navController = navController,
          startDestination = Screen.Cities.route,
          enterTransition = {
            fadeIn(Motion.enter()) +
                slideInHorizontally(Motion.enter()) { width -> width / 8 }
          },
          exitTransition = {
            fadeOut(Motion.exit()) +
                slideOutHorizontally(Motion.exit()) { width -> -width / 8 }
          },
          popEnterTransition = {
            fadeIn(Motion.enter()) +
                slideInHorizontally(Motion.enter()) { width -> -width / 8 }
          },
          popExitTransition = {
            fadeOut(Motion.exit()) +
                slideOutHorizontally(Motion.exit()) { width -> width / 8 }
          },
      ) {
        composable(Screen.Cities.route) {
          CitiesScreen(
              onOpenMenu = { scope.launch { drawerState.open() } },
              onOpenCity = { city ->
                navController.navigate(Routes.forecast(city.latitude, city.longitude))
              },
              onAddCity = { navController.navigate(Screen.Search.route) },
          )
        }
        composable(Screen.Search.route) {
          SearchScreen(
              onOpenMenu = { scope.launch { drawerState.open() } },
              onOpenPlace = { place ->
                navController.navigate(Routes.forecast(place.latitude, place.longitude)) {
                  popUpTo(Screen.Search.route) { inclusive = true }
                  launchSingleTop = true
                }
              },
          )
        }
        composable(Screen.Settings.route) {
          SettingsScreen(
              configViewModel = configViewModel,
              onOpenMenu = { scope.launch { drawerState.open() } },
          )
        }
        composable(Routes.FORECAST) { entry ->
          val latitude = entry.arguments?.getString(Routes.ARG_LATITUDE)?.toDoubleOrNull()
          val longitude = entry.arguments?.getString(Routes.ARG_LONGITUDE)?.toDoubleOrNull()
          if (latitude != null && longitude != null) {
            ForecastScreen(
                latitude = latitude,
                longitude = longitude,
                onBack = { navController.popBackStack() },
                onOpenDay = { epochDay, metric ->
                  navController.navigate(Routes.day(latitude, longitude, epochDay, metric?.name))
                },
            )
          }
        }
        composable(
            route = Routes.DAY,
            arguments =
                listOf(
                    navArgument(Routes.ARG_METRIC) {
                      type = NavType.StringType
                      defaultValue = ""
                    },
                ),
        ) { entry ->
          val latitude = entry.arguments?.getString(Routes.ARG_LATITUDE)?.toDoubleOrNull()
          val longitude = entry.arguments?.getString(Routes.ARG_LONGITUDE)?.toDoubleOrNull()
          val epochDay = entry.arguments?.getString(Routes.ARG_EPOCH_DAY)?.toLongOrNull()
          val metric = entry.arguments?.getString(Routes.ARG_METRIC)?.takeIf { it.isNotEmpty() }
          if (latitude != null && longitude != null && epochDay != null) {
            DayScreen(
                latitude = latitude,
                longitude = longitude,
                epochDay = epochDay,
                onBack = { navController.popBackStack() },
                initialMetric = DayMetric.fromName(metric),
                onSelectDay = { selectedDay, selectedMetric ->
                  navController.navigate(
                      Routes.day(latitude, longitude, selectedDay, selectedMetric.name),
                  ) {
                    popUpTo(Routes.day(latitude, longitude, epochDay)) { inclusive = true }
                    launchSingleTop = true
                  }
                },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AppDrawer(
    current: Screen?,
    isOpen: Boolean,
    onSelect: (Screen) -> Unit,
) {
  ModalDrawerSheet(
      modifier = Modifier.width(320.dp),
      drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    DrawerHeader(isOpen = isOpen)

    val dividerAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.7f else 0f,
        animationSpec = tween(durationMillis = Motion.MEDIUM_1, easing = Motion.emphasizedDecelerate),
        label = "divider_alpha_1",
    )

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = dividerAlpha),
    )
    Spacer(modifier = Modifier.height(12.dp))

    Screen.entries.forEachIndexed { index, screen ->
      val isSelected = screen == current

      val itemAlpha by animateFloatAsState(
          targetValue = if (isOpen) 1f else 0f,
          animationSpec = tween(
              durationMillis = Motion.MEDIUM_2,
              delayMillis = if (isOpen) 60 + (index * 35) else 0,
              easing = Motion.emphasizedDecelerate,
          ),
          label = "item_alpha_$index",
      )
      val itemOffsetX by animateFloatAsState(
          targetValue = if (isOpen) 0f else -36f,
          animationSpec = spring(
              dampingRatio = Spring.DampingRatioNoBouncy,
              stiffness = Spring.StiffnessMediumLow,
          ),
          label = "item_offset_$index",
      )

      val iconScale by animateFloatAsState(
          targetValue = if (isSelected) 1.15f else 1.0f,
          animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMedium,
          ),
          label = "item_icon_scale_$index",
      )

      NavigationDrawerItem(
          label = {
            Text(
                text = stringResource(screen.titleRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                  scaleX = iconScale
                  scaleY = iconScale
                },
            )
          },
          selected = isSelected,
          onClick = { onSelect(screen) },
          modifier = Modifier
              .graphicsLayer {
                alpha = itemAlpha
                translationX = itemOffsetX
              }
              .padding(NavigationDrawerItemDefaults.ItemPadding),
          colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    val footerAlpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(
            durationMillis = Motion.MEDIUM_2,
            delayMillis = if (isOpen) 180 else 0,
            easing = Motion.emphasizedDecelerate,
        ),
        label = "footer_alpha",
    )

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = dividerAlpha),
    )

    Text(
        text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .graphicsLayer { alpha = footerAlpha }
            .padding(start = 28.dp, top = 20.dp, bottom = 24.dp),
    )
  }
}

@Composable
private fun DrawerHeader(isOpen: Boolean) {
  val headerAlpha by animateFloatAsState(
      targetValue = if (isOpen) 1f else 0f,
      animationSpec = tween(
          durationMillis = Motion.MEDIUM_2,
          easing = Motion.emphasizedDecelerate,
      ),
      label = "header_alpha",
  )
  val headerTranslationX by animateFloatAsState(
      targetValue = if (isOpen) 0f else -32f,
      animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow,
      ),
      label = "header_translation",
  )
  val iconRotation by animateFloatAsState(
      targetValue = if (isOpen) 0f else -25f,
      animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow,
      ),
      label = "header_icon_rotation",
  )

  Row(
      modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer {
            alpha = headerAlpha
            translationX = headerTranslationX
          }
          .padding(start = 28.dp, top = 32.dp, end = 24.dp, bottom = 24.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
      Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer { rotationZ = iconRotation },
        )
      }
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
          text = stringResource(R.string.app_tagline),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
