package com.alejandrosahonero.courthub.ui.screens.admin.courts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.usecase.court.DisableCourtUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCourtsUiState(
    val courts: List<Court> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val error: String? = null
)

class AdminCourtsViewModel(
    private val courtRepository: ICourtRepository,
    private val disableCourtUseCase: DisableCourtUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCourtsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCourts()
    }

    private fun loadCourts() {
        viewModelScope.launch {
            courtRepository.getCourts().collect { courts ->
                _uiState.update { it.copy(courts = courts, isLoading = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun filteredCourts(): List<Court> {
        val q = _uiState.value.searchQuery
        return _uiState.value.courts.filter {
            q.isBlank() || it.name.contains(q, ignoreCase = true) ||
                    it.type.value.contains(q, ignoreCase = true)
        }
    }

    fun disableCourt(courtId: String, reason: String, from: Long, until: Long) {
        viewModelScope.launch {
            disableCourtUseCase(courtId, reason, from, until)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun enableCourt(courtId: String) {
        viewModelScope.launch {
            courtRepository.enableCourt(courtId)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(
            courtRepository: ICourtRepository,
            disableCourtUseCase: DisableCourtUseCase
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AdminCourtsViewModel(courtRepository, disableCourtUseCase) as T
        }
    }
}