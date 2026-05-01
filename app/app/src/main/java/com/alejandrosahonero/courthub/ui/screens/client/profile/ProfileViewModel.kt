package com.alejandrosahonero.courthub.ui.screens.client.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.SupportSettings
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.ISupportRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val supportSettings: SupportSettings = SupportSettings()
)

class ProfileViewModel(
    private val authRepository: IAuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val supportRepository: ISupportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(user = user, isLoading = false) }
        }
        viewModelScope.launch {
            supportRepository.getSupportSettings()
                .onSuccess { settings ->
                    _uiState.update { it.copy(supportSettings = settings) }
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

    companion object {
        fun factory(
            authRepository: IAuthRepository,
            logoutUseCase: LogoutUseCase,
            supportRepository: ISupportRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(authRepository, logoutUseCase, supportRepository) as T
        }
    }
}