package com.alejandrosahonero.courthub.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.ui.screens.admin.courts.AdminCourtsScreen
import com.alejandrosahonero.courthub.ui.screens.admin.home.AdminHomeScreen
import com.alejandrosahonero.courthub.ui.screens.admin.reservations.AdminReservationsScreen
import com.alejandrosahonero.courthub.ui.screens.admin.scanner.AdminScannerScreen
import com.alejandrosahonero.courthub.ui.screens.auth.LoginScreen
import com.alejandrosahonero.courthub.ui.screens.auth.RegisterScreen
import com.alejandrosahonero.courthub.ui.screens.client.courts.CourtDetailScreen
import com.alejandrosahonero.courthub.ui.screens.client.home.ClientHomeScreen
import com.alejandrosahonero.courthub.ui.screens.client.notifications.NotificationsScreen
import com.alejandrosahonero.courthub.ui.screens.client.payment.PaymentScreen
import com.alejandrosahonero.courthub.ui.screens.client.profile.ProfileScreen
import com.alejandrosahonero.courthub.ui.screens.client.reservations.ReservationsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as CourtHubApp

    // Punto de inicio: Login. Cuando implementemos auth persistente
    // esto cambiará para detectar si ya hay sesión activa.
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // ── Client ────────────────────────────────────────────────────────────
        composable(Screen.ClientHome.route) {
            ClientHomeScreen(navController = navController)
        }
        composable(Screen.ClientReservations.route) {
            ReservationsScreen(navController = navController)
        }
        composable(Screen.ClientNotifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Screen.ClientProfile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.CourtDetail.route) { backStackEntry ->
            val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
            CourtDetailScreen(courtId = courtId, navController = navController)
        }
        composable(Screen.ReservationStep.route) { backStackEntry ->
            val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
            PlaceholderScreen("Reservation Step: $courtId")
        }
        composable(Screen.Payment.route) { backStackEntry ->
            val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val startTime = backStackEntry.arguments?.getString("startTime") ?: ""
            PaymentScreen(courtId, date, startTime, navController)
        }

        // ── Admin ─────────────────────────────────────────────────────────────
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController = navController)
        }
        composable(Screen.AdminCourts.route) {
            AdminCourtsScreen(navController = navController)
        }
        composable(Screen.AdminReservations.route) {
            AdminReservationsScreen(navController = navController)
        }
        composable(Screen.AdminScanner.route) {
            AdminScannerScreen(navController = navController)
        }
        composable(Screen.AdminUsers.route) {
            PlaceholderScreen("Admin Usuarios")
        }
        composable(Screen.AdminNotifications.route) {
            PlaceholderScreen("Admin Notificaciones")
        }
        composable(Screen.AdminProfile.route) {
            PlaceholderScreen("Admin Perfil")
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}