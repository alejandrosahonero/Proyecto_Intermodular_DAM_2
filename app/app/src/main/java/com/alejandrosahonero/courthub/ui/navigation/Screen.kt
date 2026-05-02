package com.alejandrosahonero.courthub.ui.navigation

sealed class Screen(val route: String) {

    // ── Auth ──────────────────────────────────────────────────────────────────
    data object Login : Screen("login")
    data object Register : Screen("register")

    // ── Client ────────────────────────────────────────────────────────────────
    data object ClientHome : Screen("client_home")
    data object ClientReservations : Screen("client_reservations")
    data object ClientNotifications : Screen("client_notifications")
    data object ClientProfile : Screen("client_profile")

    // Court detail recibe el ID de la pista como argumento
    data object CourtDetail : Screen("court_detail/{courtId}") {
        fun createRoute(courtId: String) = "court_detail/$courtId"
    }

    // Paso de reserva — recibe courtId
    data object ReservationStep : Screen("reservation_step/{courtId}") {
        fun createRoute(courtId: String) = "reservation_step/$courtId"
    }

    // Pago — recibe courtId, date, startTime, endTime, hours
    data object Payment : Screen("payment/{courtId}/{date}/{startTime}/{endTime}/{hours}") {
        fun createRoute(
            courtId: String,
            date: String,
            startTime: String,
            endTime: String,
            hours: Int
        ) =
            "payment/$courtId/$date/$startTime/$endTime/$hours"
    }

    // ── Admin ─────────────────────────────────────────────────────────────────
    data object AdminHome : Screen("admin_home")
    data object AdminCourts : Screen("admin_courts")
    data object AdminReservations : Screen("admin_reservations")
    data object AdminScanner : Screen("admin_scanner")
    data object AdminUsers : Screen("admin_users")
    data object AdminNotifications : Screen("admin_notifications")
    data object AdminProfile : Screen("admin_profile")
}