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
    val filteredCourts: List<Court> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val error: String? = null,
    val courtToEdit: Court? = null,
    val showDeleteDialog: Court? = null,
    val courtToDisable: Court? = null,
    val isRefreshing: Boolean = false
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
                _uiState.update {
                    it.copy(
                        courts = courts,
                        filteredCourts = filterCourts(courts, it.searchQuery),
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun filterCourts(courts: List<Court>, query: String): List<Court> =
        courts.filter {
            query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.type.value.contains(query, ignoreCase = true)
        }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCourts = filterCourts(it.courts, query)
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateCourt(court: Court) {
        viewModelScope.launch {
            courtRepository.updateCourt(court)
                .onSuccess { _uiState.update { it.copy(courtToEdit = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteCourt(courtId: String) {
        viewModelScope.launch {
            courtRepository.deleteCourt(courtId)
                .onSuccess { _uiState.update { it.copy(showDeleteDialog = null) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message,
                            showDeleteDialog = null
                        )
                    }
                }
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

    fun onDisableRequest(court: Court) = _uiState.update { it.copy(courtToDisable = court) }
    fun onDismissDisable() = _uiState.update { it.copy(courtToDisable = null) }
    fun onEditCourt(court: Court) = _uiState.update { it.copy(courtToEdit = court) }
    fun onDismissEdit() = _uiState.update { it.copy(courtToEdit = null) }
    fun onDeleteRequest(court: Court) = _uiState.update { it.copy(showDeleteDialog = court) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteDialog = null) }
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