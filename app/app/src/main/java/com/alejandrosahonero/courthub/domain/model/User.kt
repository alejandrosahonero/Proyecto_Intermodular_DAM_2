package com.alejandrosahonero.courthub.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val fcmToken: String = "",
    val notificationsEnabled: Boolean = true,
    val isEnabled: Boolean = true,
    val createdAt: Long = 0L // epoch millis
)

enum class UserRole(val value: String) {
    ADMIN("admin"),
    CLIENT("client");

    companion object {
        // Convierte el String que viene de Firestore ("admin"/"client")
        // al enum correspondiente. Si el valor no existe, devuelve CLIENT por defecto.
        fun fromString(value: String): UserRole =
            entries.firstOrNull { it.value == value } ?: CLIENT
    }
}