package com.alejandrosahonero.courthub.ui.screens.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.screens.admin.AdminScaffold
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.utils.toInitials
import com.alejandrosahonero.courthub.utils.toPriceString

@Composable
fun AdminHomeScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: AdminHomeViewModel = viewModel(
        factory = AdminHomeViewModel.factory(
            app.container.reservationRepository,
            app.container.authRepository,
            app.container.notificationRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    AdminScaffold(
        navController = navController,
        unreadCount = uiState.unreadCount
    ) { contentModifier ->
        if (uiState.isLoading) {
            Box(modifier = contentModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Red600)
            }
            return@AdminScaffold
        }

        Column(
            modifier = contentModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Panel de Administración",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Bienvenido, ${uiState.currentUser?.name?.split(" ")?.firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Red600)
                        .clickable { navController.navigate(Screen.AdminProfile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.currentUser?.name?.toInitials() ?: "?",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fila de Acciones Rápidas (Ajustada para igualar altura)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Default.Group,
                    label = "Gestionar Usuarios",
                    onClick = { navController.navigate(Screen.AdminUsers.route) }
                )
                ActionCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Default.QrCodeScanner,
                    label = "Escanear QR",
                    onClick = { navController.navigate(Screen.AdminScanner.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // KPI ingresos hoy
            RevenueCard(
                value = uiState.revenueToday,
                label = "Ingresos Hoy",
                isWeekly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // KPI ingresos semana
            RevenueCard(
                value = uiState.revenueWeek,
                label = "Ingresos Semana",
                isWeekly = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            uiState.recentReservations.forEach { reservation ->
                RecentActivityItem(reservation = reservation)
                HorizontalDivider(color = Outline, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Red600, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(
                label, style = MaterialTheme.typography.bodySmall, color = TextHint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Red600.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Red600.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Red600, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = Red600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RevenueCard(value: Double, label: String, isWeekly: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Icon(
                    if (isWeekly) Icons.Default.TrendingUp else Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Red600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextHint)
            }
            Text(
                text = "$${"%.2f".format(value)}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentActivityItem(reservation: Reservation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(reservation.courtName, style = MaterialTheme.typography.bodyMedium)
            Text(reservation.userName, style = MaterialTheme.typography.bodySmall, color = TextHint)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                reservation.totalPrice.toPriceString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Red600
            )
            Text(
                "${reservation.date} ${reservation.startTime}",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}