package com.alejandrosahonero.courthub.data.model.firestore

import com.alejandrosahonero.courthub.utils.Constants
import com.google.firebase.firestore.PropertyName

data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = Constants.ROLE_CLIENT,

    @get:PropertyName("fcmToken")
    @set:PropertyName("fcmToken")
    var fcmToken: String = "",

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: com.google.firebase.Timestamp? = null
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", "", Constants.ROLE_CLIENT, "", null)
}