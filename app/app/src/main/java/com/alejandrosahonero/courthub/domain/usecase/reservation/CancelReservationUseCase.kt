package com.alejandrosahonero.courthub.domain.usecase.reservation

import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository

class CancelReservationUseCase(private val repository: IReservationRepository) {
    suspend operator fun invoke(reservation: Reservation): Result<Unit> {
        if (!reservation.canBeCancelled())
            return Result.failure(
                IllegalStateException("No se puede cancelar con menos de 24 horas de antelación")
            )
        return repository.cancelReservation(
            reservationId = reservation.id,
            reason = "Cancelado por el usuario"
        )
    }
}