package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.Reservation
import kotlinx.coroutines.flow.Flow

interface IReservationRepository {

    // Flow con las reservas del usuario actual — se actualiza en tiempo real
    fun getUserReservations(userId: String): Flow<List<Reservation>>

    // Solo Admin — todas las reservas de todos los usuarios
    fun getAllReservations(): Flow<List<Reservation>>

    suspend fun getReservationById(reservationId: String): Result<Reservation>

    suspend fun createReservation(reservation: Reservation): Result<String> // devuelve el ID generado

    suspend fun cancelReservation(
        reservationId: String,
        reason: String
    ): Result<Unit>

    /**
     * Marca el código de acceso como USED tras el escaneo exitoso del admin.
     * También guarda el timestamp de scannedAt.
     */
    suspend fun markAccessCodeAsUsed(reservationId: String): Result<Unit>

    // Sincroniza reservas del usuario a Room para acceso offline
    suspend fun syncUserReservationsToLocal(userId: String): Result<Unit>
}