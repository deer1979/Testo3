package com.boxcontairner.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.boxcontairner.ui.auth.LoginScreen
import com.boxcontairner.ui.containers.ContainerListScreen
import com.boxcontairner.ui.containers.detail.ContainerDetailScreen
import com.boxcontairner.ui.history.HistoryScreen
import com.boxcontairner.ui.settings.SettingsScreen

/**
 * Destinos de navegación. Se mantiene el patrón de strings por ahora.
 * Cuando el proyecto migre a Navigation type-safe con @Serializable, ver MIGRATION_NOTES.md (S7).
 */
sealed class Screen(val route: String) {
    data object Login    : Screen("login")
    data object Home     : Screen("home")
    data object History  : Screen("history")
    data object Settings : Screen("settings")
    data object Detail   : Screen("detail/{containerId}?editMode={editMode}") {
        fun route(id: String, editMode: Boolean = false) = "detail/$id?editMode=$editMode"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            ContainerListScreen(
                onContainerClick = { id ->
                    navController.navigate(Screen.Detail.route(id, editMode = false))
                },
                onContainerScanned = { id ->
                    navController.navigate(Screen.Detail.route(id, editMode = true))
                }
            )
        }

        composable(Screen.History.route) { HistoryScreen() }

        composable(Screen.Settings.route) {
            SettingsScreen(onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType },
                navArgument("editMode") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            ContainerDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
