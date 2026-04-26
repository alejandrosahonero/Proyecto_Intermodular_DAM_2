package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toDto
import com.alejandrosahonero.courthub.data.model.firestore.CourtDto
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class CourtRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ICourtRepository {

    /**
     * Escucha Firestore en tiempo real con callbackFlow.
     * Cada vez que cambia algo en la colección "courts",
     * actualiza Room y emite la lista actualizada.
     */
    override fun getCourts(): Flow<List<Court>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_COURTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val courts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CourtDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(courts)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getCourtById(courtId: String): Result<Court> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_COURTS)
                .document(courtId).get().await()
            val dto = doc.toObject(CourtDto::class.java)
                ?: return Result.failure(Exception("Pista no encontrada"))
            Result.success(dto.toDomain(doc.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCourt(court: Court): Result<Unit> {
        return try {
            val dto = court.toDto()
            firestore.collection(Constants.COLLECTION_COURTS).add(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCourt(court: Court): Result<Unit> {
        return try {
            val dto = court.toDto()
            firestore.collection(Constants.COLLECTION_COURTS)
                .document(court.id).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCourt(courtId: String): Result<Unit> {
        return try {
            firestore.collection("courts").document(courtId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disableCourt(
        courtId: String,
        reason: String,
        disabledFrom: Long,
        disabledUntil: Long
    ): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_COURTS).document(courtId).update(
                mapOf(
                    "isEnabled" to false,
                    "disabledReason" to reason,
                    "disabledFrom" to Timestamp(Date(disabledFrom)),
                    "disabledUntil" to Timestamp(Date(disabledUntil))
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enableCourt(courtId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_COURTS).document(courtId).update(
                mapOf(
                    "isEnabled" to true,
                    "disabledReason" to null,
                    "disabledFrom" to null,
                    "disabledUntil" to null
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOccupiedSlots(
        courtId: String,
        date: String
    ): Result<List<String>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_RESERVATIONS)
                .whereEqualTo("courtId", courtId)
                .whereEqualTo("date", date)
                .whereEqualTo("status", Constants.STATUS_CONFIRMED)
                .get().await()
            val slots = snapshot.documents.mapNotNull { it.getString("startTime") }
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}