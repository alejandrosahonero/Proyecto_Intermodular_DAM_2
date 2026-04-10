package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.NotificationDto
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.model.NotificationType
import com.google.firebase.Timestamp
import java.util.Date

fun NotificationDto.toDomain(id: String): AppNotification = AppNotification(
    id = id,
    userId = userId,
    title = title,
    body = body,
    type = NotificationType.fromString(type),
    isRead = isRead,
    createdAt = createdAt?.toDate()?.time ?: 0L
)

fun AppNotification.toDto(): NotificationDto = NotificationDto(
    userId = userId,
    title = title,
    body = body,
    type = type.value,
    isRead = isRead,
    createdAt = Timestamp(Date(createdAt))
)