package com.alejandrosahonero.courthub.domain.model

data class Court(
    val id: String = "",
    val name: String = "",
    val type: CourtType = CourtType.OTHER,
    val pricePerHour: Double = 0.0,
    val isEnabled: Boolean = true,
    val description: String = "",
    val imageUrl: String? = null,
    val centerId: String = "",
    // Estos tres campos solo tienen valor cuando la pista está deshabilitada
    val disabledReason: String? = null,
    val disabledFrom: Long? = null,   // epoch millis
    val disabledUntil: Long? = null   // epoch millis
)

enum class CourtType(val value: String) {
    CRISTAL("Cristal"),
    MURO("Muro"),
    FUTBOL("Fútbol"),
    TENIS("Tenis"),
    PADEL("Pádel"),
    BALONCESTO("Baloncesto"),
    OTHER("Otro");

    companion object {
        fun fromString(value: String): CourtType =
            entries.firstOrNull { it.value == value } ?: OTHER
    }
}