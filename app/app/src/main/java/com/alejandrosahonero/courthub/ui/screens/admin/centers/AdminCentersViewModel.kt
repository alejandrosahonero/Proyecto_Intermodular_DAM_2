package com.alejandrosahonero.courthub.ui.screens.admin.centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.domain.repository.ISportCenterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCentersUiState(
    val centers: List<SportCenter> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val showCreateSheet: Boolean = false,
    val centerToEdit: SportCenter? = null,
    val showDeleteDialog: SportCenter? = null,
    val error: String? = null
)

class AdminCentersViewModel(
    private val sportCenterRepository: ISportCenterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCentersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCenters()
    }

    private fun loadCenters() {
        viewModelScope.launch {
            sportCenterRepository.getSportCenters()
                .catch { e ->
                    // Si falla al cerrar sesión, no cerramos la app
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { centers ->
                    _uiState.update { it.copy(centers = centers, isLoading = false) }
                }
        }
    }

    fun filteredCenters(): List<SportCenter> {
        val q = _uiState.value.searchQuery
        return _uiState.value.centers.filter {
            q.isBlank() ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.city.contains(q, ignoreCase = true)
        }
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun onShowCreate() = _uiState.update { it.copy(showCreateSheet = true) }
    fun onDismissCreate() = _uiState.update { it.copy(showCreateSheet = false) }
    fun onEditCenter(center: SportCenter) = _uiState.update { it.copy(centerToEdit = center) }
    fun onDismissEdit() = _uiState.update { it.copy(centerToEdit = null) }
    fun onDeleteRequest(center: SportCenter) =
        _uiState.update { it.copy(showDeleteDialog = center) }

    fun onDismissDelete() = _uiState.update { it.copy(showDeleteDialog = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun createCenter(center: SportCenter) {
        viewModelScope.launch {
            sportCenterRepository.createSportCenter(center)
                .onSuccess { _uiState.update { it.copy(showCreateSheet = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun updateCenter(center: SportCenter) {
        viewModelScope.launch {
            sportCenterRepository.updateSportCenter(center)
                .onSuccess { _uiState.update { it.copy(centerToEdit = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteCenter(id: String) {
        viewModelScope.launch {
            sportCenterRepository.deleteSportCenter(id)
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

    companion object {
        fun factory(sportCenterRepository: ISportCenterRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminCentersViewModel(sportCenterRepository) as T
            }
    }
}
