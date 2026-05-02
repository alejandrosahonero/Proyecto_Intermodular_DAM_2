package com.alejandrosahonero.courthub.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class NotificationDto(
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null
) {
    constructor() : this("", "", "", "", false, null)
}