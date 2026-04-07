package com.alejandrosahonero.courthub.domain.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.RESERVATION_CONFIRMED,
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

enum class NotificationType(val value: String) {
    RESERVATION_CONFIRMED("reservation_confirmed"),
    CANCELLATION("cancellation"),
    REMINDER("reminder"),
    MAINTENANCE("maintenance"),  // para notificar bloqueo de pista
    PAYMENT_RECEIVED("payment_received");

    companion object {
        fun fromString(value: String): NotificationType =
            entries.firstOrNull { it.value == value } ?: RESERVATION_CONFIRMED
    }
}