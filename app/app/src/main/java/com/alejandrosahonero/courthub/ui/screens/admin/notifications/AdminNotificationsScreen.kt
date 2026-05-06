package com.alejandrosahonero.courthub.ui.screens.admin.notifications

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.ui.screens.admin.AdminScaffold
import com.alejandrosahonero.courthub.ui.screens.client.notifications.NotificationsViewModel
import com.alejandrosahonero.courthub.ui.screens.shared.NotificationItem
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.TextHint

// ui/screens/admin/notifications/AdminNotificationsScreen.kt
@Composable
fun AdminNotificationsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.factory(
            app.container.notificationRepository,
            app.container.authRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    AdminScaffold(
        navController = navController,
        unreadCount = uiState.unreadCount
    ) { contentModifier ->
        Column(modifier = contentModifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notificaciones", style = MaterialTheme.typography.headlineMedium)
                    if (uiState.unreadCount > 0) {
                        Text(
                            "${uiState.unreadCount} sin leer",
                            style = MaterialTheme.typography.bodySmall, color = TextHint
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.unreadCount > 0) {
                        Text(
                            "Marcar leídas",
                            style = MaterialTheme.typography.labelMedium,
                            color = Red600,
                            modifier = Modifier.clickable { viewModel.markAllAsRead() }
                        )
                    }
                    if (uiState.notifications.isNotEmpty()) {
                        Text(
                            "Borrar todas",
                            style = MaterialTheme.typography.labelMedium,
                            color = Error,
                            modifier = Modifier.clickable { viewModel.deleteAll() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Red600)
                }
            } else if (uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications, contentDescription = null,
                            tint = TextHint, modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sin notificaciones",
                            style = MaterialTheme.typography.bodyLarge, color = TextHint
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) viewModel.markAsRead(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}