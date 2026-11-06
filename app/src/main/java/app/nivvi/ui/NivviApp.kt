package app.nivvi.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.nivvi.ui.screens.AlertsScreen
import app.nivvi.ui.screens.DeviceScreen
import app.nivvi.ui.screens.FamilyScreen
import app.nivvi.ui.screens.HistoryScreen
import app.nivvi.ui.screens.LiveScreen
import app.nivvi.ui.theme.DeepBlue

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Live : Screen("live", "Live", Icons.Default.Favorite)
    object History : Screen("history", "History", Icons.Default.History)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    object Device : Screen("device", "Device", Icons.Default.Settings)
    object Family : Screen("family", "Family", Icons.Default.Group)
}

val screens = listOf(Screen.Live, Screen.History, Screen.Alerts, Screen.Device, Screen.Family)

@Composable
fun NivviApp() {
    RequestBluetoothPermissions()
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlue,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Live.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Live.route) { LiveScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Device.route) { DeviceScreen() }
            composable(Screen.Family.route) { FamilyScreen() }
        }
    }
}
