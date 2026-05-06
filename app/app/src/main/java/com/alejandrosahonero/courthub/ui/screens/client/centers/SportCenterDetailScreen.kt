package com.alejandrosahonero.courthub.ui.screens.client.centers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint

@Composable
fun SportCenterDetailScreen(centerId: String, navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp

    var center by remember { mutableStateOf<SportCenter?>(null) }
    var courts by remember { mutableStateOf<List<Court>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(centerId) {
        app.container.sportCenterRepository.getSportCenterById(centerId)
            .onSuccess { center = it }

        app.container.courtRepository.getCourts()
            .collect { allCourts ->
                courts = allCourts.filter { it.centerId == centerId && it.isEnabled }
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Red600)
        }
        return
    }

    val c = center ?: return

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        // Header imagen + botón atrás
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)) {
            AsyncImage(
                model = c.imageUrl, contentDescription = c.name,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(c.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, contentDescription = null,
                    tint = Red600, modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${c.address}, ${c.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (c.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val context = LocalContext.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${c.phone}")
                            }
                        )
                    }
                ) {
                    Icon(
                        Icons.Default.Phone, contentDescription = null,
                        tint = Red600, modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(c.phone, style = MaterialTheme.typography.bodyMedium, color = Red600)
                }
            }

            if (c.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    c.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Pistas disponibles (${courts.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (courts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin pistas disponibles",
                        style = MaterialTheme.typography.bodyLarge, color = TextHint
                    )
                }
            } else {
                courts.forEach { court ->
                    CourtMiniCard(
                        court = court,
                        onClick = {
                            navController.navigate(Screen.CourtDetail.createRoute(court.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CourtMiniCard(court: Court, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = court.imageUrl, contentDescription = court.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(court.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    court.type.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$${court.pricePerHour.toInt()}/h",
                style = MaterialTheme.typography.titleSmall, color = Red600
            )
        }
    }
}
