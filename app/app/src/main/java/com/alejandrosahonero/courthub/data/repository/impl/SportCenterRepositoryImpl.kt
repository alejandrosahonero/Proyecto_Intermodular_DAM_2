package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toDto
import com.alejandrosahonero.courthub.data.model.firestore.SportCenterDto
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.domain.repository.ISportCenterRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SportCenterRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ISportCenterRepository {

    override fun getSportCenters(): Flow<List<SportCenter>> = callbackFlow {
        val listener = firestore.collection("sport_centers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val centers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SportCenterDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(centers)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getSportCenterById(id: String): Result<SportCenter> {
        return try {
            val doc = firestore.collection("sport_centers").document(id).get().await()
            val dto = doc.toObject(SportCenterDto::class.java)
                ?: return Result.failure(Exception("Centro no encontrado"))
            Result.success(dto.toDomain(doc.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSportCenter(center: SportCenter): Result<Unit> {
        return try {
            firestore.collection("sport_centers").add(center.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSportCenter(center: SportCenter): Result<Unit> {
        return try {
            firestore.collection("sport_centers")
                .document(center.id).set(center.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSportCenter(id: String): Result<Unit> {
        return try {
            firestore.collection("sport_centers").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
