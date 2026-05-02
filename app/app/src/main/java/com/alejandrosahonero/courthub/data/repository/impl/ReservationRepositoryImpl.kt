package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.ReservationDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toDto
import com.alejandrosahonero.courthub.data.local.mapper.toEntity
import com.alejandrosahonero.courthub.data.model.firestore.ReservationDto
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.utils.Constants
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
        val listener = firestore.collection(Constants.COLLECTION_RESERVATIONS)
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
            val doc = firestore.collection(Constants.COLLECTION_RESERVATIONS)
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
            val docRef = firestore.collection(Constants.COLLECTION_RESERVATIONS).add(dto).await()
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
            firestore.collection(Constants.COLLECTION_RESERVATIONS).document(reservationId).update(
                mapOf(
                    "status" to Constants.STATUS_CANCELLED,
                    "cancellationReason" to reason,
                    "accessCodeStatus" to Constants.ACCESS_INVALID,
                    "cancelledAt" to Timestamp(Date(cancelledAt))
                )
            ).await()
            reservationDao.updateReservationStatus(
                reservationId = reservationId,
                status = Constants.STATUS_CANCELLED,
                cancelledAt = cancelledAt,
                reason = reason
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelReservationByAdmin(
        reservation: Reservation,
        reason: String
    ): Result<Unit> {
        return try {
            val cancelledAt = System.currentTimeMillis()
            firestore.collection(Constants.COLLECTION_RESERVATIONS).document(reservation.id).update(
                mapOf(
                    "status" to Constants.STATUS_CANCELLED,
                    "cancellationReason" to reason,
                    "accessCodeStatus" to Constants.ACCESS_INVALID,
                    "cancelledAt" to Timestamp(Date(cancelledAt))
                )
            ).await()

            // Notificación al usuario en Firestore
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS).add(
                mapOf(
                    "userId" to reservation.userId,
                    "title" to "Reserva Cancelada",
                    "body" to "Tu reserva en ${reservation.courtName} para el ${reservation.date} (${reservation.startTime} - ${reservation.endTime}) ha sido cancelada por un administrador. Motivo: $reason. El importe ha sido reembolsado.",
                    "type" to "cancellation",
                    "isRead" to false,
                    "createdAt" to Timestamp.now()
                )
            ).await()

            reservationDao.updateReservationStatus(
                reservationId = reservation.id,
                status = Constants.STATUS_CANCELLED,
                cancelledAt = cancelledAt,
                reason = reason
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAccessCodeAsUsed(reservationId: String): Result<Unit> {
        return try {
            val scannedAt = System.currentTimeMillis()
            firestore.collection(Constants.COLLECTION_RESERVATIONS).document(reservationId).update(
                mapOf(
                    "accessCodeStatus" to Constants.ACCESS_USED,
                    "scannedAt" to Timestamp(Date(scannedAt))
                )
            ).await()
            reservationDao.updateAccessCodeStatus(reservationId, Constants.ACCESS_USED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserReservationsToLocal(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_RESERVATIONS)
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