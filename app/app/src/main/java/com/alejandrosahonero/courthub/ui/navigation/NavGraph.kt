package com.alejandrosahonero.courthub.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.UserRole
import com.alejandrosahonero.courthub.ui.screens.admin.centers.AdminCentersScreen
import com.alejandrosahonero.courthub.ui.screens.admin.courts.AdminCourtsScreen
import com.alejandrosahonero.courthub.ui.screens.admin.home.AdminHomeScreen
import com.alejandrosahonero.courthub.ui.screens.admin.notifications.AdminNotificationsScreen
import com.alejandrosahonero.courthub.ui.screens.admin.profile.AdminProfileScreen
import com.alejandrosahonero.courthub.ui.screens.admin.reservations.AdminReservationsScreen
import com.alejandrosahonero.courthub.ui.screens.admin.scanner.AdminScannerScreen
import com.alejandrosahonero.courthub.ui.screens.admin.users.AdminUsersScreen
import com.alejandrosahonero.courthub.ui.screens.auth.LoginScreen
import com.alejandrosahonero.courthub.ui.screens.auth.RegisterScreen
import com.alejandrosahonero.courthub.ui.screens.client.centers.ClientCentersScreen
import com.alejandrosahonero.courthub.ui.screens.client.centers.SportCenterDetailScreen
import com.alejandrosahonero.courthub.ui.screens.client.courts.CourtDetailScreen
import com.alejandrosahonero.courthub.ui.screens.client.home.ClientHomeScreen
import com.alejandrosahonero.courthub.ui.screens.client.notifications.NotificationsScreen
import com.alejandrosahonero.courthub.ui.screens.client.payment.PaymentScreen
import com.alejandrosahonero.courthub.ui.screens.client.profile.ProfileScreen
import com.alejandrosahonero.courthub.ui.screens.client.reservations.ReservationsScreen
import com.alejandrosahonero.courthub.ui.theme.Red600

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as CourtHubApp
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = app.container.authRepository.getCurrentUser()
        startDestination = when {
            user == null -> Screen.Login.route
            user.role == UserRole.ADMIN -> Screen.AdminHome.route
            else -> Screen.ClientHome.route
        }
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Red600)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
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
        composable(Screen.ClientCenters.route) {
            ClientCentersScreen(navController = navController)
        }
        composable(Screen.SportCenterDetail.route) { backStackEntry ->
            val centerId = backStackEntry.arguments?.getString("centerId") ?: ""
            SportCenterDetailScreen(centerId = centerId, navController = navController)
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
            CourtDetailScreen(courtId = courtId, navController = navController)
        }
        composable(Screen.Payment.route) { backStackEntry ->
            val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val startTime = backStackEntry.arguments?.getString("startTime") ?: ""
            val endTime = backStackEntry.arguments?.getString("endTime") ?: ""
            val hours = backStackEntry.arguments?.getString("hours")?.toIntOrNull() ?: 1
            PaymentScreen(courtId, date, startTime, endTime, hours, navController)
        }

        // ── Admin ─────────────────────────────────────────────────────────────
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController = navController)
        }
        composable(Screen.AdminCenters.route) {
            AdminCentersScreen(navController = navController)
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
            AdminUsersScreen(navController = navController)
        }
        composable(Screen.AdminNotifications.route) {
            AdminNotificationsScreen(navController = navController)
        }
        composable(Screen.AdminProfile.route) {
            AdminProfileScreen(navController = navController)
        }
    }
}