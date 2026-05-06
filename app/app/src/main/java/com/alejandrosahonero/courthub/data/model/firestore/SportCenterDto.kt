package com.alejandrosahonero.courthub.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class SportCenterDto(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val phone: String = "",
    val email: String = "",
    val isEnabled: Boolean = true,
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null
) {
    constructor() : this("", "", "", "", 0.0, 0.0, null, "", "", true, null)
}
