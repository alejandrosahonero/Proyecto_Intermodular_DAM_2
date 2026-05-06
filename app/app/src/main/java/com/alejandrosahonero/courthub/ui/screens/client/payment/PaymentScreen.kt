package com.alejandrosahonero.courthub.ui.screens.client.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.utils.toPriceString
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    val context = LocalContext.current

    if (showExpiryPicker) {
        val picker = android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, _ ->
                val month = monthOfYear + 1
                expiry = "%02d/${year.toString().takeLast(2)}".format(month)
                showExpiryPicker = false
            },
            LocalDate.now().year,
            LocalDate.now().monthValue - 1,
            LocalDate.now().dayOfMonth
        )
        picker.setOnCancelListener { showExpiryPicker = false }
        DisposableEffect(Unit) {
            picker.show()
            onDispose { picker.dismiss() }
        }
    }

    // Navegar cuando la reserva se creó correctamente
    LaunchedEffect(uiState.reservationId) {
        uiState.reservationId?.let {
            navController.navigate(Screen.ClientReservations.route) {
                popUpTo(Screen.ClientHome.route) { inclusive = false }
                launchSingleTop = true
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Expiración con picker
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Expiración",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextHint
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DateTimeChip(
                        label = expiry.ifEmpty { "MM/AA" },
                        icon = Icons.Default.CalendarMonth,
                        filled = expiry.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showExpiryPicker = true }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("CVV", style = MaterialTheme.typography.labelMedium, color = TextHint)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it.filter(Char::isDigit) },
                        placeholder = { Text("000", color = TextHint) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = paymentFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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

@Composable
private fun DateTimeChip(
    label: String,
    icon: ImageVector,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (filled) Red600.copy(alpha = 0.15f) else SurfaceVariant)
            .border(1.dp, if (filled) Red600 else Outline, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (filled) Red600 else TextHint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (filled) Color.White else TextHint
        )
    }
}
