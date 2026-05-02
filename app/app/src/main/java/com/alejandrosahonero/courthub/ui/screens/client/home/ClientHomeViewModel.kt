package com.alejandrosahonero.courthub.ui.screens.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.LogoutUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.GetCourtsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientHomeUiState(
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val unreadCount: Int = 0
)

class ClientHomeViewModel(
    private val getCourtsUseCase: GetCourtsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: IAuthRepository,
    private val notificationRepository: com.alejandrosahonero.courthub.domain.repository.INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientHomeUiState())
    val uiState: StateFlow<ClientHomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadCourts()
        loadUnreadCount()
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.getUnreadCount(user.uid).collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun loadCourts() {
        viewModelScope.launch {
            getCourtsUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { courts ->
                    _uiState.update {
                        it.copy(
                            courts = courts,
                            filteredCourts = applyFilter(courts, it.searchQuery),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun applyFilter(courts: List<Court>, query: String): List<Court> =
        courts.filter { c ->
            c.isEnabled &&
                    (query.isBlank() ||
                            c.name.contains(query, ignoreCase = true) ||
                            c.type.value.contains(query, ignoreCase = true))
        }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCourts = applyFilter(state.courts, query)
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            getCourtsUseCase()
                .take(1)
                .collect { courts ->
                    _uiState.update {
                        it.copy(
                            courts = courts,
                            filteredCourts = applyFilter(courts, it.searchQuery),
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            getCourtsUseCase: GetCourtsUseCase,
            logoutUseCase: LogoutUseCase,
            authRepository: IAuthRepository,
            notificationRepository: com.alejandrosahonero.courthub.domain.repository.INotificationRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ClientHomeViewModel(
                    getCourtsUseCase,
                    logoutUseCase,
                    authRepository,
                    notificationRepository
                ) as T
        }
    }
}