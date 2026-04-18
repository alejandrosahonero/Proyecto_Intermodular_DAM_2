package com.alejandrosahonero.courthub.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",       // desnormalizado desde Firestore
    val courtId: String = "",
    val courtName: String = "",      // desnormalizado desde Firestore
    val date: String = "",           // formato "YYYY-MM-DD"
    val startTime: String = "",      // formato "HH:mm"
    val endTime: String = "",        // formato "HH:mm"
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val totalPrice: Double = 0.0,
    val paymentId: String = "",
    val accessCode: String = "",     // OTP 6 dígitos
    val accessCodeStatus: AccessCodeStatus = AccessCodeStatus.VALID,
    val qrData: String = "",
    val cancellationReason: String? = null,
    val createdAt: Long = 0L,
    val cancelledAt: Long? = null,
    val scannedAt: Long? = null
) {
    /**
     * Regla de negocio: solo se puede cancelar si faltan más de 24 horas
     * para el inicio de la reserva. Este cálculo vive aquí, en el dominio,
     * no en el ViewModel ni en el repositorio.
     */
    fun canBeCancelled(): Boolean {
        if (status != ReservationStatus.CONFIRMED) return false
        return try {
            val reservationStart = ZonedDateTime.of(
                LocalDate.parse(date),
                LocalTime.parse(startTime),
                ZoneId.systemDefault()
            )
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val hoursUntilStart = java.time.Duration.between(now, reservationStart).toHours()
            hoursUntilStart > 24
        } catch (e: Exception) {
            false
        }
    }
}

enum class ReservationStatus(val value: String) {
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    EXPIRED("expired");

    companion object {
        fun fromString(value: String): ReservationStatus =
            entries.firstOrNull { it.value == value } ?: CONFIRMED
    }
}

enum class AccessCodeStatus(val value: String) {
    VALID("VALID"),
    INVALID("INVALID"),
    USED("USED");

    companion object {
        fun fromString(value: String): AccessCodeStatus =
            entries.firstOrNull { it.value == value } ?: INVALID
    }
}