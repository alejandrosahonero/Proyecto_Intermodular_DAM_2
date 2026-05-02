package com.alejandrosahonero.courthub.ui.screens.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.TextHint

// ui/screens/admin/AdminScaffold.kt
data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val adminNavItems = listOf(
    AdminNavItem("Panel", Icons.Default.Dashboard, Screen.AdminHome),
    AdminNavItem("Pistas", Icons.Default.SportsTennis, Screen.AdminCourts),
    AdminNavItem("Reservas", Icons.Default.DateRange, Screen.AdminReservations),
    AdminNavItem("QR", Icons.Default.QrCodeScanner, Screen.AdminScanner),
    AdminNavItem("Usuarios", Icons.Default.Group, Screen.AdminUsers),
    AdminNavItem("Alertas", Icons.Default.Notifications, Screen.AdminNotifications)
)

@Composable
fun AdminScaffold(
    navController: NavController,
    unreadCount: Int = 0,
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp
            ) {
                adminNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.screen.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.label == "Alertas" && unreadCount > 0) {
                                        Badge(
                                            modifier = Modifier.size(8.dp),
                                            containerColor = Red600
                                        )
                                    }
                                }
                            ) {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Red600,
                            selectedTextColor = Red600,
                            unselectedIconColor = TextHint,
                            unselectedTextColor = TextHint,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}