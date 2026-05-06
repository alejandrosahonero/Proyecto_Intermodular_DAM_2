package com.alejandrosahonero.courthub.ui.screens.admin.centers

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.ui.screens.admin.AdminScaffold
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import kotlinx.coroutines.launch

@Composable
fun AdminCentersScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: AdminCentersViewModel = viewModel(
        factory = AdminCentersViewModel.factory(app.container.sportCenterRepository)
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

    AdminScaffold(navController = navController) { contentModifier ->
        Box(modifier = contentModifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Centros Deportivos",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(
                        onClick = { viewModel.onShowCreate() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Red600)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar...", color = TextHint) },
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(viewModel.filteredCenters(), key = { it.id }) { center ->
                            AdminSportCenterCard(
                                center = center,
                                onEdit = { viewModel.onEditCenter(center) },
                                onDelete = { viewModel.onDeleteRequest(center) }
                            )
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

    if (uiState.showCreateSheet) {
        SportCenterFormSheet(
            title = "Nuevo Centro",
            initial = SportCenter(),
            onDismiss = { viewModel.onDismissCreate() },
            onConfirm = { viewModel.createCenter(it) }
        )
    }

    uiState.centerToEdit?.let { center ->
        SportCenterFormSheet(
            title = "Editar Centro",
            initial = center,
            onDismiss = { viewModel.onDismissEdit() },
            onConfirm = { viewModel.updateCenter(it) }
        )
    }

    uiState.showDeleteDialog?.let { center ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            containerColor = Surface,
            title = { Text("Eliminar centro") },
            text = {
                Text(
                    "¿Eliminar ${center.name}? Las pistas asociadas quedarán sin centro.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCenter(center.id) }) {
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
private fun AdminSportCenterCard(
    center: SportCenter,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)) {
                AsyncImage(
                    model = center.imageUrl, contentDescription = center.name,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(center.name, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, contentDescription = null,
                        tint = TextHint, modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${center.address}, ${center.city}",
                        style = MaterialTheme.typography.bodySmall, color = TextHint
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Edit, contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", style = MaterialTheme.typography.labelLarge)
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
private fun SportCenterFormSheet(
    title: String,
    initial: SportCenter,
    onDismiss: () -> Unit,
    onConfirm: (SportCenter) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var address by remember { mutableStateOf(initial.address) }
    var city by remember { mutableStateOf(initial.city) }
    var phone by remember { mutableStateOf(initial.phone) }
    var email by remember { mutableStateOf(initial.email) }
    var imageUrl by remember { mutableStateOf(initial.imageUrl ?: "") }
    var latStr by remember { mutableStateOf(if (initial.latitude != 0.0) initial.latitude.toString() else "") }
    var lngStr by remember { mutableStateOf(if (initial.longitude != 0.0) initial.longitude.toString() else "") }
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
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                Triple(name, "Nombre *", { v: String -> name = v }),
                Triple(address, "Dirección *", { v: String -> address = v }),
                Triple(city, "Ciudad *", { v: String -> city = v }),
                Triple(phone, "Teléfono", { v: String -> phone = v }),
                Triple(email, "Email", { v: String -> email = v }),
                Triple(imageUrl, "URL de imagen", { v: String -> imageUrl = v }),
                Triple(latStr, "Latitud", { v: String -> latStr = v }),
                Triple(lngStr, "Longitud", { v: String -> lngStr = v })
            ).forEach { (value, label, onChange) ->
                OutlinedTextField(
                    value = value, onValueChange = onChange,
                    label = { Text(label) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = disableSheetFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                colors = disableSheetFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            inputError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Error)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank() || city.isBlank()) {
                        inputError = "Nombre, dirección y ciudad son obligatorios"
                        return@Button
                    }
                    inputError = null
                    onConfirm(
                        initial.copy(
                            name = name,
                            description = description,
                            address = address,
                            city = city,
                            phone = phone,
                            email = email,
                            imageUrl = imageUrl.ifBlank { null },
                            latitude = latStr.toDoubleOrNull() ?: 0.0,
                            longitude = lngStr.toDoubleOrNull() ?: 0.0,
                            createdAt = if (initial.createdAt == 0L)
                                System.currentTimeMillis() else initial.createdAt
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red600)
            ) {
                Text("Guardar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun disableSheetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Red600,
    unfocusedBorderColor = Outline,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Red600,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant,
    focusedLabelColor = Red600,
    unfocusedLabelColor = TextHint
)
