package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.ReservationDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toDto
import com.alejandrosahonero.courthub.data.local.mapper.toEntity
import com.alejandrosahonero.courthub.data.model.firestore.ReservationDto
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReservationRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val reservationDao: ReservationDao
) : IReservationRepository {

    override fun getUserReservations(userId: String): Flow<List<Reservation>> =
        reservationDao.getReservationsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAllReservations(): Flow<List<Reservation>> = callbackFlow {
        val listener = firestore.collection("reservations")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val reservations = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ReservationDto::class.java)?.toDomain(doc.id)
                }
                trySend(reservations)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getReservationById(reservationId: String): Result<Reservation> {
        return try {
            val doc = firestore.collection("reservations")
                .document(reservationId).get().await()
            val dto = doc.toObject(ReservationDto::class.java)
                ?: return Result.failure(Exception("Reserva no encontrada"))
            Result.success(dto.toDomain(doc.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReservation(reservation: Reservation): Result<String> {
        return try {
            val dto = reservation.toDto()
            val docRef = firestore.collection("reservations").add(dto).await()
            // Guardamos también en Room para acceso offline
            val saved = reservation.copy(id = docRef.id)
            reservationDao.insertReservation(saved.toEntity())
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelReservation(
        reservationId: String,
        reason: String
    ): Result<Unit> {
        return try {
            val cancelledAt = System.currentTimeMillis()
            firestore.collection("reservations").document(reservationId).update(
                mapOf(
                    "status" to "cancelled",
                    "cancellationReason" to reason,
                    "accessCodeStatus" to "INVALID",
                    "cancelledAt" to Timestamp(Date(cancelledAt))
                )
            ).await()
            reservationDao.updateReservationStatus(
                reservationId = reservationId,
                status = "cancelled",
                cancelledAt = cancelledAt
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAccessCodeAsUsed(reservationId: String): Result<Unit> {
        return try {
            val scannedAt = System.currentTimeMillis()
            firestore.collection("reservations").document(reservationId).update(
                mapOf(
                    "accessCodeStatus" to "USED",
                    "scannedAt" to Timestamp(Date(scannedAt))
                )
            ).await()
            reservationDao.updateAccessCodeStatus(reservationId, "USED")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserReservationsToLocal(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .get().await()
            val entities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ReservationDto::class.java)?.toDomain(doc.id)?.toEntity()
            }
            reservationDao.deleteByUserId(userId)
            reservationDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}