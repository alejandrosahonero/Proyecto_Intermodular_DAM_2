package com.alejandrosahonero.courthub.ui.screens.admin.reservations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.utils.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ui/screens/admin/reservations/AdminReservationsViewModel.kt
data class AdminReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val showCancelDialog: Reservation? = null,
    val isCancelling: Boolean = false,
    val error: String? = null
)

class AdminReservationsViewModel(
    application: Application,
    private val reservationRepository: IReservationRepository
) : AndroidViewModel(application) {

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

    fun onCancelRequest(reservation: Reservation) =
        _uiState.update { it.copy(showCancelDialog = reservation) }

    fun onDismissCancel() =
        _uiState.update { it.copy(showCancelDialog = null) }

    fun cancelReservation(reservation: Reservation, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            reservationRepository.cancelReservationByAdmin(
                reservation = reservation,
                reason = reason
            ).onSuccess {
                _uiState.update { it.copy(isCancelling = false, showCancelDialog = null) }
                sendLocalNotification(
                    "Reserva Cancelada",
                    "Se ha cancelado la reserva de ${reservation.userName} en ${reservation.courtName}"
                )
            }.onFailure { e ->
                _uiState.update { it.copy(isCancelling = false, error = e.message) }
            }
        }
    }

    private fun sendLocalNotification(title: String, message: String) {
        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

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
        fun factory(application: Application, reservationRepository: IReservationRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    AdminReservationsViewModel(application, reservationRepository) as T
            }
    }
}