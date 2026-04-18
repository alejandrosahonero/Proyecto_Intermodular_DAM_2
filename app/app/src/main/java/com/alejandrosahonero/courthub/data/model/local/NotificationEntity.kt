package com.alejandrosahonero.courthub.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationIdFirebase: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Long // epoch millis
)