package com.alejandrosahonero.courthub.ui.screens.admin.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.SupportSettings
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.screens.admin.AdminScaffold
import com.alejandrosahonero.courthub.ui.screens.client.profile.ProfileViewModel
import com.alejandrosahonero.courthub.ui.screens.shared.ProfileActionItem
import com.alejandrosahonero.courthub.ui.screens.shared.ProfileItem
import com.alejandrosahonero.courthub.ui.screens.shared.ProfileSection
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Surface
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.utils.toInitials

@Composable
fun AdminProfileScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as CourtHubApp
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            app.container.authRepository,
            app.container.logoutUseCase,
            app.container.supportRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    AdminScaffold(navController = navController) { contentModifier ->
        if (uiState.isLoading) {
            Box(modifier = contentModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Red600)
            }
            return@AdminScaffold
        }

        val user = uiState.user ?: return@AdminScaffold

        Column(
            modifier = contentModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Red600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.toInitials(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Administrador",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Red600
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProfileSection(title = "Información de Cuenta") {
                ProfileItem(
                    icon = Icons.Default.MailOutline,
                    label = "Correo",
                    value = user.email
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSection(title = "Configuración") {
                ProfileActionItem(
                    icon = Icons.Default.Notifications,
                    label = "Notificaciones",
                    subtitle = "Gestiona tus preferencias de notificaciones",
                    onClick = {}
                )
                ProfileActionItem(
                    icon = Icons.Default.Settings,
                    label = "Configuración de Cuenta",
                    subtitle = "Actualiza los detalles de tu cuenta",
                    onClick = {}
                )
                ProfileActionItem(
                    icon = Icons.Default.SupportAgent,
                    label = "Datos de Soporte",
                    subtitle = "Editar teléfono y email de soporte",
                    onClick = { showSupportDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceVariant,
                    contentColor = Error
                )
            ) {
                Icon(
                    Icons.Default.Logout, contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Surface,
            title = { Text("Cerrar sesión") },
            text = {
                Text(
                    "¿Seguro que quieres cerrar sesión?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Cerrar sesión", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextHint)
                }
            }
        )
    }

    if (showSupportDialog) {
        EditSupportDialog(
            current = uiState.supportSettings,
            onDismiss = { showSupportDialog = false },
            onConfirm = { settings ->
                viewModel.updateSupport(settings)
                showSupportDialog = false
            }
        )
    }
}

@Composable
private fun EditSupportDialog(
    current: SupportSettings,
    onDismiss: () -> Unit,
    onConfirm: (SupportSettings) -> Unit
) {
    var phone by remember { mutableStateOf(current.phone) }
    var email by remember { mutableStateOf(current.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text("Datos de Soporte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
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
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
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
            TextButton(onClick = {
                onConfirm(SupportSettings(phone = phone, email = email))
            }) {
                Text("Guardar", color = Red600)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextHint)
            }
        }
    )
}