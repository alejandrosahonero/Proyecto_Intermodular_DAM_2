package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.Court
import kotlinx.coroutines.flow.Flow

interface ICourtRepository {

    // Flow para que la HomeScreen reaccione en tiempo real a cambios en Firestore
    fun getCourts(): Flow<List<Court>>

    suspend fun getCourtById(courtId: String): Result<Court>

    // Solo Admin
    suspend fun createCourt(court: Court): Result<Unit>

    // Solo Admin
    suspend fun updateCourt(court: Court): Result<Unit>

    /**
     * Deshabilita una pista. La Cloud Function onCourtDisabled
     * se encargará de cancelar reservas y reembolsos automáticamente.
     */
    suspend fun disableCourt(
        courtId: String,
        reason: String,
        disabledFrom: Long,
        disabledUntil: Long
    ): Result<Unit>

    // Solo Admin — reactiva manualmente antes de disabledUntil
    suspend fun enableCourt(courtId: String): Result<Unit>

    /**
     * Devuelve los slots ocupados de una pista en una fecha concreta.
     * Formato de cada String: "HH:mm" (la hora de inicio del slot ocupado).
     * Los slots disponibles se calculan en el use case restando estos al total del día.
     */
    suspend fun getOccupiedSlots(courtId: String, date: String): Result<List<String>>
}