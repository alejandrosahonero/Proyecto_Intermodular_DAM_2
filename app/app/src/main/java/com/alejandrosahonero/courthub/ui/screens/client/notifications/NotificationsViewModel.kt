package com.alejandrosahonero.courthub.ui.screens.client.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val unreadCount: Int = 0
)

class NotificationsViewModel(
    private val notificationRepository: INotificationRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            launch {
                notificationRepository.getNotifications(user.uid).collect { list ->
                    _uiState.update { it.copy(notifications = list, isLoading = false) }
                }
            }
            launch {
                notificationRepository.getUnreadCount(user.uid).collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.markAllAsRead(user.uid)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.deleteAllNotifications(user.uid)
        }
    }

    companion object {
        fun factory(
            notificationRepository: INotificationRepository,
            authRepository: IAuthRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NotificationsViewModel(notificationRepository, authRepository) as T
        }
    }
}