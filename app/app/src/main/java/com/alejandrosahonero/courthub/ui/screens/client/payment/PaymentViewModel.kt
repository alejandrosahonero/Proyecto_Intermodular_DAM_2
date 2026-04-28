package com.alejandrosahonero.courthub.ui.screens.client.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.AccessCodeStatus
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.usecase.access.GenerateAccessCodeUseCase
import com.alejandrosahonero.courthub.domain.usecase.reservation.CreateReservationUseCase
import com.alejandrosahonero.courthub.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val court: Court? = null,
    val isLoading: Boolean = false,
    val isPaying: Boolean = false,
    val error: String? = null,
    val reservationId: String? = null
)

class PaymentViewModel(
    private val courtId: String,
    private val date: String,
    private val startTime: String,
    private val courtRepository: ICourtRepository,
    private val createReservationUseCase: CreateReservationUseCase,
    private val generateAccessCodeUseCase: GenerateAccessCodeUseCase,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCourt()
    }

    private fun loadCourt() {
        viewModelScope.launch {
            courtRepository.getCourtById(courtId)
                .onSuccess { court -> _uiState.update { it.copy(court = court) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun pay(cardHolder: String, cardNumber: String, expiry: String, cvv: String) {
        val court = _uiState.value.court ?: return
        if (cardHolder.isBlank() || cardNumber.isBlank() || expiry.isBlank() || cvv.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true, error = null) }
            val user = authRepository.getCurrentUser()
            val accessCode = generateAccessCodeUseCase()
            val endTime = DateUtils.endTimeFromStart(startTime)
            val reservation = Reservation(
                userId = user?.uid ?: "",
                userName = user?.name ?: "",
                courtId = courtId,
                courtName = court.name,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = ReservationStatus.CONFIRMED,
                totalPrice = court.pricePerHour,
                paymentId = "stripe_sandbox_${System.currentTimeMillis()}",
                accessCode = accessCode,
                accessCodeStatus = AccessCodeStatus.VALID,
                qrData = "COURTHUB:${courtId}:${date}:${startTime}:${accessCode}",
                createdAt = System.currentTimeMillis()
            )
            createReservationUseCase(reservation)
                .onSuccess { id ->
                    _uiState.update {
                        it.copy(
                            isPaying = false,
                            reservationId = id
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isPaying = false, error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(
            courtId: String, date: String, startTime: String,
            courtRepository: ICourtRepository,
            createReservationUseCase: CreateReservationUseCase,
            generateAccessCodeUseCase: GenerateAccessCodeUseCase,
            authRepository: IAuthRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PaymentViewModel(
                    courtId, date, startTime, courtRepository,
                    createReservationUseCase, generateAccessCodeUseCase, authRepository
                ) as T
        }
    }
}