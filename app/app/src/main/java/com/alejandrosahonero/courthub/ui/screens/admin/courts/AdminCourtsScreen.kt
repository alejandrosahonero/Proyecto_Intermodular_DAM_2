package com.alejandrosahonero.courthub.ui.screens.admin.courts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.Court
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AdminCourtsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: AdminCourtsViewModel = viewModel(
        factory = AdminCourtsViewModel.factory(
            app.container.courtRepository,
            app.container.disableCourtUseCase
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearError()
        }
    }

    AdminScaffold(navController = navController) { contentModifier ->
        Box(modifier = contentModifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Administrar Pistas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar pistas...", color = TextHint) },
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

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Red600)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullToRefresh(
                                state = pullToRefreshState,
                                isRefreshing = uiState.isRefreshing,
                                onRefresh = { viewModel.refresh() }
                            )
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(viewModel.filteredCourts(), key = { it.id }) { court ->
                                AdminCourtCard(
                                    court = court,
                                    onDisable = { viewModel.onDisableRequest(court) },
                                    onEnable = { viewModel.enableCourt(court.id) },
                                    onEdit = { viewModel.onEditCourt(court) },
                                    onDelete = { viewModel.onDeleteRequest(court) }
                                )
                            }
                        }
                        Indicator(
                            state = pullToRefreshState,
                            isRefreshing = uiState.isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter),
                            color = Red600
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Disable sheet
    uiState.courtToDisable?.let { court ->
        DisableCourtSheet(
            court = court,
            onDismiss = { viewModel.onDismissDisable() },
            onConfirm = { reason, from, until ->
                viewModel.disableCourt(court.id, reason, from, until)
                viewModel.onDismissDisable()
            }
        )
    }

    // Edit sheet
    uiState.courtToEdit?.let { court ->
        EditCourtSheet(
            court = court,
            onDismiss = { viewModel.onDismissEdit() },
            onConfirm = { updated -> viewModel.updateCourt(updated) }
        )
    }

    // Delete dialog
    uiState.showDeleteDialog?.let { court ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            containerColor = Surface,
            title = { Text("Eliminar pista") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar ${court.name}? Esta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCourt(court.id) }) {
                    Text("Eliminar", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) {
                    Text("Cancelar", color = TextHint)
                }
            }
        )
    }
}

@Composable
private fun AdminCourtCard(
    court: Court,
    onDisable: () -> Unit,
    onEnable: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = court.imageUrl,
                    contentDescription = court.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (!court.isEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(Warning, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "En Mantenimiento",
                            style = MaterialTheme.typography.labelSmall, color = Color.Black
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(court.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    court.type.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${court.pricePerHour.toPriceString()}/hora",
                    style = MaterialTheme.typography.titleSmall,
                    color = Red600
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (court.isEnabled) {
                        OutlinedButton(
                            onClick = onDisable,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Warning),
                            border = BorderStroke(1.dp, Warning),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Block, contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deshabilitar", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = onEnable,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Habilitar", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Error.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisableCourtSheet(
    court: Court,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, from: Long, until: Long) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var fromTime by remember { mutableStateOf("") }
    var untilDate by remember { mutableStateOf("") }
    var untilTime by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Deshabilitar Pista", style = MaterialTheme.typography.titleLarge)
            Text(court.name, style = MaterialTheme.typography.bodyMedium, color = Red600)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Motivo *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Desde", style = MaterialTheme.typography.labelMedium, color = TextHint)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fromDate,
                    onValueChange = { fromDate = it },
                    label = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = disableSheetFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = fromTime,
                    onValueChange = { fromTime = it },
                    label = { Text("HH:mm") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = disableSheetFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Hasta", style = MaterialTheme.typography.labelMedium, color = TextHint)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = untilDate,
                    onValueChange = { untilDate = it },
                    label = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = disableSheetFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = untilTime,
                    onValueChange = { untilTime = it },
                    label = { Text("HH:mm") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = disableSheetFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            inputError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    try {
                        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        val from = LocalDateTime.parse("$fromDate $fromTime", fmt)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val until = LocalDateTime.parse("$untilDate $untilTime", fmt)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (reason.isBlank()) {
                            inputError = "El motivo es obligatorio"
                            return@Button
                        }
                        inputError = null
                        onConfirm(reason, from, until)
                    } catch (e: Exception) {
                        inputError = "Formato de fecha/hora incorrecto"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red600)
            ) {
                Text("Confirmar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCourtSheet(
    court: Court,
    onDismiss: () -> Unit,
    onConfirm: (Court) -> Unit
) {
    var name by remember { mutableStateOf(court.name) }
    var description by remember { mutableStateOf(court.description) }
    var price by remember { mutableStateOf(court.pricePerHour.toString()) }
    var imageUrl by remember { mutableStateOf(court.imageUrl ?: "") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Editar Pista", style = MaterialTheme.typography.titleLarge)
            Text(court.name, style = MaterialTheme.typography.bodyMedium, color = Red600)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio/hora *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de imagen") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            inputError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val priceDouble = price.toDoubleOrNull()
                    if (name.isBlank()) {
                        inputError = "El nombre es obligatorio"
                        return@Button
                    }
                    if (priceDouble == null || priceDouble <= 0) {
                        inputError = "Precio inválido"
                        return@Button
                    }
                    inputError = null
                    onConfirm(
                        court.copy(
                            name = name,
                            description = description,
                            pricePerHour = priceDouble,
                            imageUrl = imageUrl.ifBlank { null }
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red600)
            ) {
                Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun disableSheetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Red600,
    unfocusedBorderColor = Outline,
    focusedLabelColor = Red600,
    unfocusedLabelColor = TextHint,
    cursorColor = Red600,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)