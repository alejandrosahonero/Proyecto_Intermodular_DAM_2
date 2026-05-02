package com.alejandrosahonero.courthub.ui.screens.client.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.model.ReservationStatus.CONFIRMED
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.usecase.reservation.CancelReservationUseCase
import com.alejandrosahonero.courthub.domain.usecase.reservation.GetUserReservationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val showAccessCodeDialog: Boolean = false,
    val selectedReservation: Reservation? = null,
    val error: String? = null,
    val unreadCount: Int = 0
)

class ReservationsViewModel(
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReservations()
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

    private fun loadReservations() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            getUserReservationsUseCase(user.uid).collect { list ->
                _uiState.update { it.copy(reservations = list, isLoading = false) }
            }
        }
    }

    fun onTabSelected(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    fun onShowAccessCode(reservation: Reservation) =
        _uiState.update { it.copy(showAccessCodeDialog = true, selectedReservation = reservation) }

    fun onDismissAccessCode() =
        _uiState.update { it.copy(showAccessCodeDialog = false, selectedReservation = null) }

    fun cancelReservation(reservation: Reservation) {
        viewModelScope.launch {
            cancelReservationUseCase(reservation)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun filteredReservations(): List<Reservation> {
        val all = _uiState.value.reservations
        return when (_uiState.value.selectedTab) {
            1 -> all.filter { it.status == CONFIRMED }
            2 -> all.filter { it.status == ReservationStatus.EXPIRED }
            3 -> all.filter { it.status == ReservationStatus.CANCELLED }
            else -> all
        }
    }

    companion object {
        fun factory(
            getUserReservationsUseCase: GetUserReservationsUseCase,
            cancelReservationUseCase: CancelReservationUseCase,
            authRepository: IAuthRepository,
            notificationRepository: INotificationRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReservationsViewModel(
                    getUserReservationsUseCase,
                    cancelReservationUseCase,
                    authRepository,
                    notificationRepository
                ) as T
        }
    }
}