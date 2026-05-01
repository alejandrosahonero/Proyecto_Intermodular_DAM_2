package com.alejandrosahonero.courthub.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uidFirebase: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: String,
    val fcmToken: String,
    val createdAt: Long // epoch millis
)