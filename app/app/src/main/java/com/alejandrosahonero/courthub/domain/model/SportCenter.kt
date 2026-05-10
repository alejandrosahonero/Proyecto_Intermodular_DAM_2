package com.alejandrosahonero.courthub.domain.model

data class SportCenter(
    val id: String = "",
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
    val createdAt: Long = 0L
)
