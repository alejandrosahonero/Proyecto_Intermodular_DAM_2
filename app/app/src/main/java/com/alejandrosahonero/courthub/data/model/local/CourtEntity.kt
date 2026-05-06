package com.alejandrosahonero.courthub.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courts")
data class CourtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courtIdFirebase: String,
    val name: String,
    val type: String,
    val pricePerHour: Double,
    val isEnabled: Boolean,
    val description: String,
    val imageUrl: String?,
    val centerId: String,
    val disabledReason: String?,
    val disabledFrom: Long?,  // epoch millis
    val disabledUntil: Long?  // epoch millis
)