package com.alejandrosahonero.courthub.ui.screens.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ui/screens/admin/home/AdminHomeViewModel.kt
data class AdminHomeUiState(
    val totalToday: Int = 0,
    val totalWeek: Int = 0,
    val revenueToday: Double = 0.0,
    val revenueWeek: Double = 0.0,
    val recentReservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val unreadCount: Int = 0
)

class AdminHomeViewModel(
    private val reservationRepository: IReservationRepository,
    private val authRepository: IAuthRepository,
    private val notificationRepository: com.alejandrosahonero.courthub.domain.repository.INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
        loadUnreadCount()
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
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

    private fun loadStats() {
        viewModelScope.launch {
            reservationRepository.getAllReservations().collect { reservations ->
                val today = DateUtils.todayString()
                val weekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)

                val confirmed = reservations.filter { it.status == ReservationStatus.CONFIRMED }
                val todayList = confirmed.filter { it.date == today }
                val weekList = confirmed.filter { it.date >= weekAgo }

                _uiState.update {
                    it.copy(
                        totalToday = todayList.size,
                        totalWeek = weekList.size,
                        revenueToday = todayList.sumOf { r -> r.totalPrice },
                        revenueWeek = weekList.sumOf { r -> r.totalPrice },
                        recentReservations = reservations.take(5),
                        isLoading = false
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            reservationRepository: IReservationRepository,
            authRepository: IAuthRepository,
            notificationRepository: com.alejandrosahonero.courthub.domain.repository.INotificationRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AdminHomeViewModel(
                    reservationRepository,
                    authRepository,
                    notificationRepository
                ) as T
        }
    }
}