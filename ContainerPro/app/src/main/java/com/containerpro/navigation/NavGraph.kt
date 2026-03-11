package com.containerpro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.containerpro.data.PreferencesManager
import com.containerpro.ui.theme.AppTheme
import com.containerpro.ui.screens.*
import com.containerpro.ui.screens.control.ServiceControlScreen

sealed class Screen(val route: String) {
    object Welcome      : Screen("welcome")
    object Home         : Screen("home")
    object Scan         : Screen("scan")
    object WifiConnect  : Screen("wifi_connect")
    object ModelSelect  : Screen("model_select")
    object DRR          : Screen("drr")
    object ServiceCheck : Screen("service_check")
    object Photos       : Screen("photos")
    object Reports      : Screen("reports")
    object Settings     : Screen("settings")
    object ServiceControl : Screen("service_control/{ipAddress}") {
        fun createRoute(ip: String) = "service_control/$ip"
    }
}

@Composable
fun AppNavGraph(
    preferencesManager: PreferencesManager,
    onThemeChange:    (AppTheme) -> Unit,
    onLanguageChange: (String)   -> Unit,
) {
    val navController = rememberNavController()

    val startDestination = if (preferencesManager.isLoggedIn())
        Screen.Home.route else Screen.Welcome.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController, preferencesManager)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, preferencesManager, onThemeChange, onLanguageChange)
        }
        composable(Screen.Scan.route) {
            ScanScreen(navController)
        }
        composable(Screen.WifiConnect.route) {
            WifiConnectScreen(navController)
        }
        composable(Screen.ModelSelect.route) {
            ModelSelectScreen(navController)
        }
        composable(Screen.DRR.route) {
            DRRScreen(navController)
        }
        composable(Screen.ServiceCheck.route) {
            ServiceCheckScreen(navController)
        }
        composable(Screen.Photos.route) {
            PhotosScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController      = navController,
                preferencesManager = preferencesManager,
                onThemeChange      = onThemeChange,
                onLanguageChange   = onLanguageChange,
            )
        }
        composable(
            route     = Screen.ServiceControl.route,
            arguments = listOf(navArgument("ipAddress") { type = NavType.StringType }),
        ) { back ->
            ServiceControlScreen(
                navController = navController,
                ipAddress     = back.arguments?.getString("ipAddress") ?: "",
            )
        }
    }
}
