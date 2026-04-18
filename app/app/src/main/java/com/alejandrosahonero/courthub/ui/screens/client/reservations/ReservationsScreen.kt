package com.alejandrosahonero.courthub.ui.screens.client.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.ui.screens.client.ClientScaffold
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Success
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.ui.theme.Warning
import kotlinx.coroutines.launch

@Composable
fun ReservationsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: ReservationsViewModel = viewModel(
        factory = ReservationsViewModel.factory(
            app.container.getUserReservationsUseCase,
            app.container.cancelReservationUseCase,
            app.container.authRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearError()
        }
    }

    ClientScaffold(navController = navController) { contentModifier ->
        Box(modifier = contentModifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mis Reservas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                val tabs = listOf("Todas", "Confirmadas", "Expiradas", "Canceladas")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(tabs.size) { index ->
                        val selected = uiState.selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) Red600 else SurfaceVariant)
                                .clickable { viewModel.onTabSelected(index) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tabs[index],
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else TextHint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Red600)
                    }
                } else {
                    val filtered = viewModel.filteredReservations()
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin reservas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextHint
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(filtered, key = { it.id }) { reservation ->
                                ReservationCard(
                                    reservation = reservation,
                                    onShowQr = { viewModel.onShowAccessCode(reservation) },
                                    onCancel = { viewModel.cancelReservation(reservation) }
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Dialog QR/OTP
    if (uiState.showAccessCodeDialog) {
        uiState.selectedReservation?.let { reservation ->
            AccessCodeDialog(
                reservation = reservation,
                onDismiss = { viewModel.onDismissAccessCode() }
            )
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    onShowQr: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.courtName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = reservation.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, contentDescription = null,
                    tint = TextHint, modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(reservation.date, style = MaterialTheme.typography.bodySmall, color = TextHint)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.Schedule, contentDescription = null,
                    tint = TextHint, modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${reservation.startTime} - ${reservation.endTime}",
                    style = MaterialTheme.typography.bodySmall, color = TextHint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${reservation.totalPrice.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = Red600
            )

            if (reservation.status == ReservationStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onShowQr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                ) {
                    Icon(
                        Icons.Default.QrCode, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Código QR / OTP", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val canCancel = reservation.canBeCancelled()
                if (!canCancel) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Warning.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning, contentDescription = null,
                            tint = Warning, modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "No se puede cancelar la reserva 24 horas antes",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning
                        )
                    }
                } else {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Cancelar reserva",
                            style = MaterialTheme.typography.labelLarge,
                            color = Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ReservationStatus) {
    val (label, color) = when (status) {
        ReservationStatus.CONFIRMED -> "CONFIRMADA" to Success
        ReservationStatus.CANCELLED -> "CANCELADA" to Error
        ReservationStatus.EXPIRED -> "EXPIRADA" to TextHint
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun AccessCodeDialog(reservation: Reservation, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Código de Acceso", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextHint)
                    }
                }

                Text(
                    text = reservation.courtName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )

                Spacer(modifier = Modifier.height(20.dp))

                // QR placeholder — se implementará con ZXing en utils/
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reservation.qrData,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Código OTP",
                    style = MaterialTheme.typography.labelMedium, color = TextHint
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reservation.accessCode,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp,
                        color = Red600
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Muestra este código al llegar a la instalación",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}