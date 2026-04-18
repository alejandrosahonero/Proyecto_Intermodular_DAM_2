package com.alejandrosahonero.courthub.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class CourtDto(
    val name: String = "",
    val type: String = "",
    val pricePerHour: Double = 0.0,
    val isEnabled: Boolean = true,
    val description: String = "",
    val imageUrl: String? = null,
    val disabledReason: String? = null,

    @get:PropertyName("disabledFrom")
    @set:PropertyName("disabledFrom")
    var disabledFrom: Timestamp? = null,

    @get:PropertyName("disabledUntil")
    @set:PropertyName("disabledUntil")
    var disabledUntil: Timestamp? = null
) {
    constructor() : this("", "", 0.0, true, "", null, null, null, null)
}