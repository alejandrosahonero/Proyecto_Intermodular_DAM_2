package com.alejandrosahonero.courthub.domain.usecase.access

import com.alejandrosahonero.courthub.domain.model.AccessCodeStatus
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(val reason: String) : ValidationResult()
}

class ValidateAccessCodeUseCase {
    operator fun invoke(reservation: Reservation): ValidationResult {
        if (reservation.status != ReservationStatus.CONFIRMED)
            return ValidationResult.Failure("La reserva no está confirmada")

        if (reservation.accessCodeStatus != AccessCodeStatus.VALID)
            return ValidationResult.Failure("El código ya fue usado o está invalidado")

        return try {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val start = ZonedDateTime.of(
                LocalDate.parse(reservation.date),
                LocalTime.parse(reservation.startTime),
                ZoneId.systemDefault()
            )
            val end = ZonedDateTime.of(
                LocalDate.parse(reservation.date),
                LocalTime.parse(reservation.endTime),
                ZoneId.systemDefault()
            )
            if (now.isBefore(start) || now.isAfter(end))
                ValidationResult.Failure("El código está fuera del horario de la reserva")
            else
                ValidationResult.Success
        } catch (e: Exception) {
            ValidationResult.Failure("Error al validar el horario")
        }
    }
}