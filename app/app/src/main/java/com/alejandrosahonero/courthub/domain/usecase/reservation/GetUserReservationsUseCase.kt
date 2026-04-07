package com.alejandrosahonero.courthub.domain.usecase.reservation

import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import kotlinx.coroutines.flow.Flow

class GetUserReservationsUseCase(private val repository: IReservationRepository) {
    operator fun invoke(userId: String): Flow<List<Reservation>> =
        repository.getUserReservations(userId)
}