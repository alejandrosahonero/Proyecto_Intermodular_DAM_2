package com.alejandrosahonero.courthub.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reservationIdFirebase: String,
    val userId: String,
    val userName: String,
    val courtId: String,
    val courtName: String,
    val date: String,       // "YYYY-MM-DD"
    val startTime: String,  // "HH:mm"
    val endTime: String,    // "HH:mm"
    val status: String,
    val totalPrice: Double,
    val paymentId: String,
    val accessCode: String,
    val accessCodeStatus: String,
    val qrData: String,
    val cancellationReason: String?,
    val createdAt: Long,
    val cancelledAt: Long?,
    val scannedAt: Long?
)