package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.domain.model.SupportSettings
import com.alejandrosahonero.courthub.domain.repository.ISupportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SupportRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ISupportRepository {

    override suspend fun getSupportSettings(): Result<SupportSettings> {
        return try {
            val doc = firestore.collection("settings")
                .document("support").get().await()
            val settings = SupportSettings(
                phone = doc.getString("phone") ?: "+34 900 123 456",
                email = doc.getString("email") ?: "soporte@courthub.com"
            )
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSupportSettings(settings: SupportSettings): Result<Unit> {
        return try {
            firestore.collection("settings").document("support")
                .set(mapOf("phone" to settings.phone, "email" to settings.email))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}