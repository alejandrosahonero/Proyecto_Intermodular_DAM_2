package com.alejandrosahonero.courthub.ui.screens.client.courts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.usecase.court.TimeSlot
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.ui.theme.Warning
import com.alejandrosahonero.courthub.utils.toPriceString
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailScreen(
    courtId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as CourtHubApp
    val viewModel: CourtDetailViewModel = viewModel(
        factory = CourtDetailViewModel.factory(
            courtId = courtId,
            courtRepository = app.container.courtRepository,
            getAvailableSlotsUseCase = app.container.getAvailableSlotsUseCase
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Red600)
        }
        return
    }

    val court = uiState.court ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Imagen con botón atrás ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AsyncImage(
                model = court.imageUrl,
                contentDescription = court.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
        }

        // ── Info de la pista ──────────────────────────────────────────────────
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = court.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = court.type.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${court.pricePerHour.toPriceString()}/hora",
                style = MaterialTheme.typography.titleLarge,
                color = Red600,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = court.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red600),
                enabled = court.isEnabled
            ) {
                Text("Reservar pista", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // ── BottomSheet de reserva ────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            ReservationStepSheet(
                uiState = uiState,
                onDateSelected = { viewModel.onDateSelected(it) },
                onSlotSelected = { viewModel.onSlotSelected(it) },
                onContinue = {
                    val date = uiState.selectedDate
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val slot = uiState.selectedSlot ?: return@ReservationStepSheet
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        navController.navigate(
                            Screen.Payment.createRoute(court.id, date, slot)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ReservationStepSheet(
    uiState: CourtDetailUiState,
    onDateSelected: (LocalDate) -> Unit,
    onSlotSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Título
        Text(
            text = "Reservar Pista",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = uiState.court?.name ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Red600
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Selector de fecha ─────────────────────────────────────────────────
        Text(
            text = "Selecciona una fecha",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        DateSelector(
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Selector de hora ──────────────────────────────────────────────────
        Text(
            text = "Selecciona una hora",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Leyenda
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = Color.White, label = "Disponible")
            LegendItem(color = TextHint, label = "Ocupada")
            LegendItem(color = Warning, label = "Mantenimiento")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.slotsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Red600, modifier = Modifier.size(28.dp))
            }
        } else {
            SlotGrid(
                slots = uiState.slots,
                selectedSlot = uiState.selectedSlot,
                onSlotSelected = onSlotSelected
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            enabled = uiState.selectedSlot != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red600,
                disabledContainerColor = SurfaceVariant
            )
        ) {
            Text(
                text = if (uiState.selectedSlot != null)
                    "Continuar al Pago" else "Selecciona un horario",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    // Mostramos 7 días desde hoy
    val today = LocalDate.now()
    val dates = (0..13).map { today.plusDays(it.toLong()) }

    var startIndex by remember { mutableStateOf(0) }
    val visibleDates = dates.drop(startIndex).take(5)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (startIndex > 0) startIndex-- },
            enabled = startIndex > 0
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = if (startIndex > 0) Color.White else TextHint
            )
        }

        visibleDates.forEach { date ->
            val isSelected = date == selectedDate
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Red600 else SurfaceVariant)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale("es"))
                        .uppercase()
                        .take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else TextHint
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) Color.White else Color.White
                )
                Text(
                    text = date.month
                        .getDisplayName(TextStyle.SHORT, Locale("es"))
                        .lowercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else TextHint
                )
            }
        }

        IconButton(
            onClick = { if (startIndex + 5 < dates.size) startIndex++ }
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (startIndex + 5 < dates.size) Color.White else TextHint
            )
        }
    }
}

@Composable
private fun SlotGrid(
    slots: List<TimeSlot>,
    selectedSlot: String?,
    onSlotSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp),
        contentPadding = PaddingValues(2.dp)
    ) {
        items(slots) { slot ->
            val isSelected = slot.hour == selectedSlot
            val bgColor = when {
                isSelected -> Red600
                slot.isMaintenance -> Warning.copy(alpha = 0.15f)
                !slot.isAvailable -> SurfaceVariant
                else -> SurfaceVariant
            }
            val textColor = when {
                isSelected -> Color.White
                slot.isMaintenance -> Warning
                !slot.isAvailable -> TextHint
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Red600 else Outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = slot.isAvailable) {
                        onSlotSelected(slot.hour)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (slot.isMaintenance || !slot.isAvailable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!slot.isAvailable && !slot.isMaintenance) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = slot.hour,
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = slot.hour,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextHint
        )
    }
}

// Necesario para el background circular del botón atrás
private val CircleShape = androidx.compose.foundation.shape.CircleShape