package com.alejandrosahonero.courthub.ui.screens.admin.home

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ui/screens/admin/home/AdminHomeViewModel.kt
data class AdminHomeUiState(
    val totalToday: Int = 0,
    val totalWeek: Int = 0,
    val revenueToday: Double = 0.0,
    val revenueWeek: Double = 0.0,
    val recentReservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true
)

class AdminHomeViewModel(
    private val reservationRepository: IReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            reservationRepository.getAllReservations().collect { reservations ->
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
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
        fun factory(reservationRepository: IReservationRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminHomeViewModel(reservationRepository) as T
            }
    }
}