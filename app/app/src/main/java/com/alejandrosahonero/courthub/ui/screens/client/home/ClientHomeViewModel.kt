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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientHomeUiState(
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

class ClientHomeViewModel(
    private val getCourtsUseCase: GetCourtsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientHomeUiState())
    val uiState: StateFlow<ClientHomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadCourts()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun loadCourts() {
        viewModelScope.launch {
            getCourtsUseCase().collect { courts ->
                _uiState.update {
                    it.copy(
                        courts = courts,
                        filteredCourts = courts.filter { c -> c.isEnabled },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCourts = state.courts.filter { court ->
                    court.isEnabled &&
                            (query.isBlank() ||
                                    court.name.contains(query, ignoreCase = true) ||
                                    court.type.value.contains(query, ignoreCase = true))
                }
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }

    companion object {
        fun factory(
            getCourtsUseCase: GetCourtsUseCase,
            logoutUseCase: LogoutUseCase,
            authRepository: IAuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ClientHomeViewModel(getCourtsUseCase, logoutUseCase, authRepository) as T
        }
    }
}