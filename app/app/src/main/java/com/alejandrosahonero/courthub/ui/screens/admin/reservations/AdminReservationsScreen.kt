package com.alejandrosahonero.courthub.ui.screens.admin.reservations

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.ui.screens.admin.AdminScaffold
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Success
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.ui.theme.Warning
import com.alejandrosahonero.courthub.utils.toPriceString
import kotlinx.coroutines.launch

@Composable
fun AdminReservationsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: AdminReservationsViewModel = viewModel(
        factory = AdminReservationsViewModel.factory(app.container.reservationRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    AdminScaffold(navController = navController) { contentModifier ->
        Box(modifier = contentModifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Reservas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar pista o usuario...", color = TextHint) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red600,
                        unfocusedBorderColor = Outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Red600,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val tabs = listOf("Todos", "Confirmados", "Cancelados", "Expirados")
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
                                tabs[index],
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
                                "Sin reservas", style = MaterialTheme.typography.bodyLarge,
                                color = TextHint
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(filtered, key = { it.id }) { reservation ->
                                AdminReservationCard(
                                    reservation = reservation,
                                    onCancelRequest = { viewModel.onCancelRequest(reservation) }
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

    uiState.showCancelDialog?.let { reservation ->
        CancelReservationDialog(
            reservation = reservation,
            isCancelling = uiState.isCancelling,
            onDismiss = { viewModel.onDismissCancel() },
            onConfirm = { reason -> viewModel.cancelReservation(reservation, reason) }
        )
    }
}

@Composable
private fun AdminReservationCard(
    reservation: Reservation,
    onCancelRequest: () -> Unit
) {
    val (statusLabel, statusColor) = when (reservation.status) {
        ReservationStatus.CONFIRMED -> "CONFIRMADA" to Success
        ReservationStatus.CANCELLED -> "CANCELADA" to Error
        ReservationStatus.EXPIRED -> "EXPIRADA" to TextHint
    }

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
                    reservation.courtName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall, color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person, contentDescription = null,
                    tint = TextHint, modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    reservation.userName,
                    style = MaterialTheme.typography.bodySmall, color = TextHint
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, contentDescription = null,
                    tint = TextHint, modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    reservation.date,
                    style = MaterialTheme.typography.bodySmall, color = TextHint
                )
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
                reservation.totalPrice.toPriceString(),
                style = MaterialTheme.typography.titleMedium,
                color = Red600
            )

            if (reservation.status == ReservationStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancelRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    border = BorderStroke(1.dp, Error)
                ) {
                    Icon(
                        Icons.Default.Cancel, contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancelar y reembolsar", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun CancelReservationDialog(
    reservation: Reservation,
    isCancelling: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        containerColor = Surface,
        title = { Text("Cancelar Reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Reserva: ${reservation.courtName}\n" +
                            "Usuario: ${reservation.userName}\n" +
                            "Fecha: ${reservation.date} ${reservation.startTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Warning.copy(alpha = 0.1f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info, contentDescription = null,
                        tint = Warning, modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "El reembolso se procesará automáticamente",
                        style = MaterialTheme.typography.labelSmall,
                        color = Warning
                    )
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo de cancelación *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red600,
                        unfocusedBorderColor = Outline,
                        focusedLabelColor = Red600,
                        unfocusedLabelColor = TextHint,
                        cursorColor = Red600,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank() && !isCancelling
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        color = Red600,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmar", color = Error)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCancelling
            ) {
                Text("Cancelar", color = TextHint)
            }
        }
    )
}
