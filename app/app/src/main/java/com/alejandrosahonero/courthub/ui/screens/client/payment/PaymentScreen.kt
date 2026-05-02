package com.alejandrosahonero.courthub.ui.screens.client.payment

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.utils.toPriceString
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(
    courtId: String,
    date: String,
    startTime: String,
    endTime: String,
    hours: Int,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModel.factory(
            courtId, date, startTime, endTime, hours,
            app.container.courtRepository,
            app.container.createReservationUseCase,
            app.container.generateAccessCodeUseCase,
            app.container.authRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    var showExpiryPicker by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedYear by remember { mutableStateOf(2026) }

    // Navegar cuando la reserva se creó correctamente
    LaunchedEffect(uiState.reservationId) {
        uiState.reservationId?.let {
            navController.navigate(Screen.ClientReservations.route) {
                popUpTo(Screen.ClientHome.route)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Botón atrás
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resumen de la reserva
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen de la reserva",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextHint
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.court?.name ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$date  •  $startTime - $endTime ($hours h)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Outline)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val totalPrice = (uiState.court?.pricePerHour ?: 0.0) * hours
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = totalPrice.toPriceString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Red600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aviso QR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Red600.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Red600,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu código de acceso QR se generará después del pago",
                    style = MaterialTheme.typography.bodySmall,
                    color = Red600
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Datos de tarjeta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Red600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Datos de Tarjeta", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                label = { Text("Titular de la tarjeta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = paymentFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it.filter(Char::isDigit) },
                label = { Text("Número de tarjeta") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = paymentFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // Expiración con picker
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = if (selectedMonth != 0)
                            "%02d/${selectedYear.toString().takeLast(2)}".format(selectedMonth)
                        else "",
                        onValueChange = {},
                        label = { Text("MM/AA") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExpiryPicker = true },
                        colors = paymentFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.clickable { showExpiryPicker = true }
                            )
                        }
                    )
                }

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 4) cvv = it.filter(Char::isDigit) },
                    label = { Text("CVV") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                    colors = paymentFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.pay(cardHolder, cardNumber, expiry, cvv) },
                enabled = !uiState.isPaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red600)
            ) {
                if (uiState.isPaying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    val totalPrice = (uiState.court?.pricePerHour ?: 0.0) * hours
                    Text(
                        "Pagar ${totalPrice.toPriceString()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showExpiryPicker) {
            ExpiryPickerDialog(
                initialMonth = selectedMonth,
                initialYear = selectedYear,
                onDismiss = { showExpiryPicker = false },
                onConfirm = { month, year ->
                    selectedMonth = month
                    selectedYear = year
                    expiry = "%02d/${year.toString().takeLast(2)}".format(month)
                    showExpiryPicker = false
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ExpiryPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val months = (1..12).map { "%02d".format(it) }
    val years = (currentYear..currentYear + 10).map { it.toString() }

    var selectedMonth by remember { mutableStateOf(initialMonth.coerceIn(1, 12)) }
    var selectedYear by remember {
        mutableStateOf(
            initialYear.coerceIn(
                currentYear,
                currentYear + 10
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text("Fecha de vencimiento") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Picker de mes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mes", style = MaterialTheme.typography.labelMedium, color = TextHint)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollPicker(
                        items = months,
                        selected = selectedMonth - 1,
                        onSelected = { selectedMonth = it + 1 }
                    )
                }

                // Picker de año
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Año", style = MaterialTheme.typography.labelMedium, color = TextHint)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollPicker(
                        items = years,
                        selected = selectedYear - currentYear,
                        onSelected = { selectedYear = currentYear + it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMonth, selectedYear) }) {
                Text("Confirmar", color = Red600)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextHint)
            }
        }
    )
}

@Composable
private fun ScrollPicker(
    items: List<String>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected)

    LaunchedEffect(selected) {
        listState.animateScrollToItem(selected)
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        onSelected(listState.firstVisibleItemIndex)
    }

    Box(
        modifier = Modifier
            .height(140.dp)
            .width(80.dp)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 48.dp)
        ) {
            items(items.size) { index ->
                val isSelected = listState.firstVisibleItemIndex == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Red600.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) Red600 else TextHint,
                        fontWeight = if (isSelected)
                            FontWeight.Bold
                        else
                            FontWeight.Normal
                    )
                }
            }
        }

        // Líneas decorativas del item seleccionado
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-22).dp),
            color = Outline
        )
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 22.dp),
            color = Outline
        )
    }
}

@Composable
private fun paymentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Red600,
    unfocusedBorderColor = Outline,
    focusedLabelColor = Red600,
    unfocusedLabelColor = TextHint,
    cursorColor = Red600,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)