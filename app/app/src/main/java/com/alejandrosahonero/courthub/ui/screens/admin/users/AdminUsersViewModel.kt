package com.alejandrosahonero.courthub.ui.screens.admin.users

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.utils.NotificationWorker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUsersUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val showNotificationDialog: User? = null,
    val error: String? = null
)

class AdminUsersViewModel(
    application: Application,
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "client")
                    .get().await()
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserDto::class.java)?.toDomain(doc.id)
                }
                _uiState.update {
                    it.copy(users = users, filteredUsers = users, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredUsers = state.users.filter {
                    query.isBlank() ||
                            it.name.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun toggleUserEnabled(user: User) {
        viewModelScope.launch {
            authRepository.setUserEnabled(user.uid, !user.isEnabled)
                .onSuccess { loadUsers() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun onShowNotificationDialog(user: User) =
        _uiState.update { it.copy(showNotificationDialog = user) }

    fun onDismissNotificationDialog() =
        _uiState.update { it.copy(showNotificationDialog = null) }

    fun sendNotification(user: User, title: String, body: String) {
        viewModelScope.launch {
            notificationRepository.sendNotificationToUser(user.uid, title, body)
                .onSuccess {
                    // Trigger local notification for confirmation/validation
                    val data = Data.Builder()
                        .putString("title", "Notificación Enviada")
                        .putString("message", "Se ha enviado el mensaje a ${user.name}")
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInputData(data)
                        .build()

                    WorkManager.getInstance(getApplication()).enqueue(workRequest)

                    _uiState.update { it.copy(showNotificationDialog = null) }
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(
            application: Application,
            firestore: FirebaseFirestore,
            authRepository: IAuthRepository,
            notificationRepository: INotificationRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AdminUsersViewModel(
                    application,
                    firestore,
                    authRepository,
                    notificationRepository
                ) as T
        }
    }
}
