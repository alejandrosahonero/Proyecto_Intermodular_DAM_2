package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.NotificationDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toEntity
import com.alejandrosahonero.courthub.data.model.firestore.NotificationDto
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val notificationDao: NotificationDao
) : INotificationRepository {

    override fun getNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications: List<AppNotification> = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()

                // Sincronizamos con Room para historial offline
                launch(Dispatchers.IO) {
                    try {
                        notificationDao.deleteByUserId(userId)
                        val entities = notifications.map { it.toEntity() }
                        notificationDao.insertAll(entities)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String
    ): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS).add(
                mapOf(
                    "userId" to userId,
                    "title" to title,
                    "body" to body,
                    "type" to "reminder",
                    "isRead" to false,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}