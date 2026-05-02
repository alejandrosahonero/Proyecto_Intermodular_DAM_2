package com.alejandrosahonero.courthub.ui.screens.client.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.SupportSettings
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.repository.ISupportRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val supportSettings: SupportSettings = SupportSettings(),
    val isSavingProfile: Boolean = false,
    val unreadCount: Int = 0
)

class ProfileViewModel(
    private val authRepository: IAuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val supportRepository: ISupportRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUser()
        loadSupportSettings()
        loadUnreadCount()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(user = user, isLoading = false) }
        }
    }

    private fun loadSupportSettings() {
        viewModelScope.launch {
            supportRepository.getSupportSettings()
                .onSuccess { settings ->
                    _uiState.update { it.copy(supportSettings = settings) }
                }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.getUnreadCount(user.uid).collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }

    fun updateSupport(settings: SupportSettings) {
        viewModelScope.launch {
            supportRepository.updateSupportSettings(settings)
                .onSuccess { _uiState.update { it.copy(supportSettings = settings) } }
        }
    }

    fun updateProfile(name: String, phone: String) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) }
            authRepository.updateUserProfile(uid, name, phone)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSavingProfile = false,
                            user = it.user?.copy(name = name, phone = phone)
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isSavingProfile = false) } }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            authRepository.updateNotificationsEnabled(uid, enabled)
                .onSuccess {
                    _uiState.update { it.copy(user = it.user?.copy(notificationsEnabled = enabled)) }
                }
        }
    }

    companion object {
        fun factory(
            authRepository: IAuthRepository,
            logoutUseCase: LogoutUseCase,
            supportRepository: ISupportRepository,
            notificationRepository: INotificationRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(
                    authRepository,
                    logoutUseCase,
                    supportRepository,
                    notificationRepository
                ) as T
        }
    }
}