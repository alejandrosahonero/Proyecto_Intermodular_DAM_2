package com.alejandrosahonero.courthub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandrosahonero.courthub.data.model.local.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    // Reservas del usuario actual ordenadas por fecha descendente
    @Query("""
        SELECT * FROM reservations 
        WHERE userId = :userId 
        ORDER BY date DESC, startTime DESC
    """)
    fun getReservationsByUser(userId: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE reservationIdFirebase = :id LIMIT 1")
    suspend fun getReservationById(id: String): ReservationEntity?

    @Query("""
        UPDATE reservations 
        SET accessCodeStatus = :status 
        WHERE reservationIdFirebase = :reservationId
    """)
    suspend fun updateAccessCodeStatus(reservationId: String, status: String)

    @Query("""
        UPDATE reservations 
        SET status = :status, cancelledAt = :cancelledAt, cancellationReason = :reason
        WHERE reservationIdFirebase = :reservationId
    """)
    suspend fun updateReservationStatus(
        reservationId: String,
        status: String,
        cancelledAt: Long?,
        reason: String?
    )

    // Limpia las reservas del usuario antes de una resincronización
    @Query("DELETE FROM reservations WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}