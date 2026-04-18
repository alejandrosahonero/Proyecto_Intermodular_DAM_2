package com.alejandrosahonero.courthub.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ReservationDto(
    val userId: String = "",
    val userName: String = "",
    val courtId: String = "",
    val courtName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "confirmed",
    val totalPrice: Double = 0.0,
    val paymentId: String = "",
    val accessCode: String = "",
    val accessCodeStatus: String = "VALID",
    val qrData: String = "",
    val cancellationReason: String? = null,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null,

    @get:PropertyName("cancelledAt")
    @set:PropertyName("cancelledAt")
    var cancelledAt: Timestamp? = null,

    @get:PropertyName("scannedAt")
    @set:PropertyName("scannedAt")
    var scannedAt: Timestamp? = null
) {
    constructor() : this(
        "", "", "", "", "", "", "", "confirmed",
        0.0, "", "", "VALID", "", null, null, null, null
    )
}