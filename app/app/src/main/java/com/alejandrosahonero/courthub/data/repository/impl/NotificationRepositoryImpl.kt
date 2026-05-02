package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.NotificationDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val notificationDao: NotificationDao
) : INotificationRepository {

    // Las notificaciones se sirven desde Room — FCM las inserta al recibirlas
    override fun getNotifications(userId: String): Flow<List<AppNotification>> =
        notificationDao.getNotificationsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true).await()
            notificationDao.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            // Batch update en Firestore
            val snapshot = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            notificationDao.markAllAsRead(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    override suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String
    ): Result<Unit> {
        return try {
            firestore.collection("notifications").add(
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