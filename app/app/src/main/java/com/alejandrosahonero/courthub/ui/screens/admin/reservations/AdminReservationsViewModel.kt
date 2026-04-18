package com.alejandrosahonero.courthub.ui.screens.admin.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ui/screens/admin/reservations/AdminReservationsViewModel.kt
data class AdminReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = ""
)

class AdminReservationsViewModel(
    private val reservationRepository: IReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReservationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            reservationRepository.getAllReservations().collect { list ->
                _uiState.update { it.copy(reservations = list, isLoading = false) }
            }
        }
    }

    fun onTabSelected(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun filteredReservations(): List<Reservation> {
        val q = _uiState.value.searchQuery
        val all = _uiState.value.reservations
        val byTab = when (_uiState.value.selectedTab) {
            1 -> all.filter { it.status == ReservationStatus.CONFIRMED }
            2 -> all.filter { it.status == ReservationStatus.CANCELLED }
            3 -> all.filter { it.status == ReservationStatus.EXPIRED }
            else -> all
        }
        return if (q.isBlank()) byTab
        else byTab.filter {
            it.courtName.contains(q, ignoreCase = true) ||
                    it.userName.contains(q, ignoreCase = true)
        }
    }

    companion object {
        fun factory(reservationRepository: IReservationRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminReservationsViewModel(reservationRepository) as T
            }
    }
}