package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.NotificationDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
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
            firestore.collection("notifications")
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
            val snapshot = firestore.collection("notifications")
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
}