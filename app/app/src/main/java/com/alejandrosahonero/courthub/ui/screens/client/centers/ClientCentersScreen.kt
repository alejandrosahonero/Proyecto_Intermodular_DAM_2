package com.alejandrosahonero.courthub.ui.screens.client.centers

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.screens.client.ClientScaffold
import com.alejandrosahonero.courthub.ui.theme.*
import com.google.android.gms.location.LocationServices

@Composable
fun ClientCentersScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: ClientCentersViewModel = viewModel(
        factory = ClientCentersViewModel.factory(app.container.sportCenterRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) requestLocation(context) { lat, lng ->
            viewModel.onLocationReceived(lat, lng)
        }
    }

    ClientScaffold(navController = navController) { contentModifier ->
        Column(modifier = contentModifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Centros Deportivos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar por nombre o ciudad...", color = TextHint) },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Filtro cercanía
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.sortByDistance,
                    onClick = {
                        if (uiState.userLatitude == null) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        viewModel.toggleSortByDistance()
                    },
                    label = { Text("Más cercanos") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.NearMe,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Red600,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )

                if (uiState.userLatitude != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Ubicación activa",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Red600)
                }
            } else if (uiState.filteredCenters.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Stadium, contentDescription = null,
                            tint = TextHint, modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sin centros deportivos",
                            style = MaterialTheme.typography.bodyLarge, color = TextHint
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiState.filteredCenters, key = { it.id }) { center ->
                        SportCenterCard(
                            center = center,
                            distanceKm = if (uiState.userLatitude != null && uiState.userLongitude != null)
                                viewModel.distanceKm(
                                    uiState.userLatitude!!, uiState.userLongitude!!,
                                    center.latitude, center.longitude
                                ) else null,
                            onClick = {
                                navController.navigate(
                                    Screen.SportCenterDetail.createRoute(center.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SportCenterCard(
    center: SportCenter,
    distanceKm: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)) {
                AsyncImage(
                    model = center.imageUrl,
                    contentDescription = center.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (distanceKm != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${"%.1f".format(distanceKm)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(center.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, contentDescription = null,
                        tint = TextHint, modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${center.address}, ${center.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint
                    )
                }
                if (center.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone, contentDescription = null,
                            tint = TextHint, modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            center.phone,
                            style = MaterialTheme.typography.bodySmall, color = TextHint
                        )
                    }
                }
            }
        }
    }
}

// Función helper para obtener ubicación
fun requestLocation(context: Context, onResult: (Double, Double) -> Unit) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    try {
        client.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { onResult(it.latitude, it.longitude) }
        }
    } catch (e: SecurityException) {
        // permiso no concedido
    }
}
