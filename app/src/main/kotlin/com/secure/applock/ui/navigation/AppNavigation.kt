package com.secure.applock.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.secure.applock.ui.screen.*
import com.secure.applock.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object AppSelect : Screen("app_select")
    object Settings  : Screen("settings")
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController  = rememberNavController()
    val navBackStack   by navController.currentBackStackEntryAsState()
    val currentDest    = navBackStack?.destination
    val scheme         = MaterialTheme.colorScheme

    val bottomItems = listOf(
        Triple(Screen.Home,     "Home",     Icons.Filled.Home),
        Triple(Screen.Settings, "Settings", Icons.Filled.Settings),
    )

    Scaffold(
        containerColor = scheme.background,
        bottomBar = {
            NavigationBar(containerColor = scheme.surface, tonalElevation = 0.dp) {
                bottomItems.forEach { (screen, label, icon) ->
                    val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon     = { Icon(icon, label) },
                        label    = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = scheme.primary,
                            selectedTextColor   = scheme.primary,
                            indicatorColor      = scheme.primary.copy(0.15f),
                            unselectedIconColor = scheme.onSurface.copy(0.5f),
                            unselectedTextColor = scheme.onSurface.copy(0.5f),
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel, onNavigateToApps = { navController.navigate(Screen.AppSelect.route) })
            }
            composable(Screen.AppSelect.route) {
                AppSelectScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
