package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {

    // Flow en tiempo real — el badge de notificaciones se actualiza solo
    fun getNotifications(userId: String): Flow<List<AppNotification>>

    suspend fun markAsRead(notificationId: String): Result<Unit>

    suspend fun markAllAsRead(userId: String): Result<Unit>

    suspend fun deleteAllNotifications(userId: String): Result<Unit>

    // Cuenta las no leídas para el badge de la barra de navegación
    fun getUnreadCount(userId: String): Flow<Int>

    suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String
    ): Result<Unit>
}