package com.alejandrosahonero.courthub.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.R
import com.alejandrosahonero.courthub.domain.model.UserRole
import com.alejandrosahonero.courthub.ui.navigation.Screen
import com.alejandrosahonero.courthub.ui.theme.Outline
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.TextHint
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CourtHubApp
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(
            loginUseCase = app.container.loginUseCase,
            registerUseCase = app.container.registerUseCase
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedUser) {
        uiState.loggedUser?.let { user ->
            if (user.role == UserRole.ADMIN) {
                navController.navigate(Screen.AdminHome.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.ClientHome.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
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
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Imagen — ocupa el espacio sobrante por encima del formulario ──
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),  // ocupa solo lo que sobra
                contentScale = ContentScale.Crop
            )

            // ── Formulario — tamaño fijo, siempre visible ─────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    color = Red600,
                    thickness = 2.dp,
                    modifier = Modifier.width(40.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = loginTextFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextHint
                            )
                        }
                    },
                    colors = loginTextFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Red600,
                                uncheckedColor = TextHint
                            )
                        )
                        Text(
                            text = "Recuérdame",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "¿Olvidaste la contraseña?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Red600,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar Sesión", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GoogleSignInButton(
                    text = "Iniciar sesión con Google",
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿Aún no tienes cuenta? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Regístrate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Red600,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Red600,
    unfocusedBorderColor = Outline,
    focusedLabelColor = Red600,
    unfocusedLabelColor = TextHint,
    cursorColor = Red600,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)