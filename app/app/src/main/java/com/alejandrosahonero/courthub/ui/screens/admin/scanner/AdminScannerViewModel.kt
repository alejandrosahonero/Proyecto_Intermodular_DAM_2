package com.alejandrosahonero.courthub.ui.screens.admin.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.domain.usecase.access.ValidateAccessCodeUseCase
import com.alejandrosahonero.courthub.domain.usecase.access.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ScannerState {
    data object Idle : ScannerState()
    data object Loading : ScannerState()
    data object Success : ScannerState()
    data class Failure(val reason: String) : ScannerState()
}

data class AdminScannerUiState(
    val scannerState: ScannerState = ScannerState.Idle,
    val unreadCount: Int = 0
)

class AdminScannerViewModel(
    private val reservationRepository: IReservationRepository,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository,
    private val validateAccessCodeUseCase: ValidateAccessCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminScannerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUnreadCount()
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                notificationRepository.getUnreadCount(user.uid).collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
            }
        }
    }

    fun onQrScanned(qrData: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(scannerState = ScannerState.Loading) }
            // qrData formato: COURTHUB:{courtId}:{date}:{startTime}:{endTime}:{accessCode}
            val parts = qrData.split(":")
            if (parts.size < 6 || parts[0] != "COURTHUB") {
                _uiState.update { it.copy(scannerState = ScannerState.Failure("QR inválido")) }
                return@launch
            }
            val accessCode = parts[5]
            reservationRepository.getAllReservations()
                .collect { reservations ->
                    val reservation = reservations.firstOrNull {
                        it.accessCode == accessCode
                    }
                    if (reservation == null) {
                        _uiState.update {
                            it.copy(scannerState = ScannerState.Failure("Reserva no encontrada"))
                        }
                        return@collect
                    }
                    val result = validateAccessCodeUseCase(reservation)
                    if (result is ValidationResult.Success) {
                        reservationRepository.markAccessCodeAsUsed(reservation.id)
                        _uiState.update { it.copy(scannerState = ScannerState.Success) }
                    } else {
                        _uiState.update {
                            it.copy(
                                scannerState = ScannerState.Failure(
                                    (result as ValidationResult.Failure).reason
                                )
                            )
                        }
                    }
                    return@collect
                }
        }
    }

    fun reset() = _uiState.update { it.copy(scannerState = ScannerState.Idle) }

    companion object {
        fun factory(
            reservationRepository: IReservationRepository,
            authRepository: IAuthRepository,
            notificationRepository: INotificationRepository,
            validateAccessCodeUseCase: ValidateAccessCodeUseCase
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AdminScannerViewModel(
                    reservationRepository,
                    authRepository,
                    notificationRepository,
                    validateAccessCodeUseCase
                ) as T
        }
    }
}