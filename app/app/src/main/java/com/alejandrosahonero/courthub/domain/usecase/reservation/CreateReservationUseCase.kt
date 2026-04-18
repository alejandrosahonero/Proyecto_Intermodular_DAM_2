package com.alejandrosahonero.courthub.domain.usecase.reservation

import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository

class CreateReservationUseCase(private val repository: IReservationRepository) {
    suspend operator fun invoke(reservation: Reservation): Result<String> {
        if (reservation.courtId.isBlank() || reservation.date.isBlank()
            || reservation.startTime.isBlank()
        ) return Result.failure(IllegalArgumentException("Datos de reserva incompletos"))
        return repository.createReservation(reservation)
    }
}