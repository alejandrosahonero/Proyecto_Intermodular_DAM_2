package com.alejandrosahonero.courthub.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alejandrosahonero.courthub.CourtHubApp
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReservationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CourtHubApp
        val user = app.container.authRepository.getCurrentUser() ?: return Result.success()

        if (user.notificationsEnabled.not()) return Result.success()

        app.container.reservationRepository
            .getUserReservations(user.uid)
            .take(1)
            .collect { reservations ->
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                reservations
                    .filter { it.status == ReservationStatus.CONFIRMED }
                    .forEach { reservation ->
                        try {
                            val reservationStart = ZonedDateTime.of(
                                LocalDate.parse(reservation.date),
                                LocalTime.parse(reservation.startTime),
                                ZoneId.systemDefault()
                            )
                            val hoursUntil = Duration.between(now, reservationStart).toHours()
                            // Notifica si la reserva es en las próximas 25 horas
                            if (hoursUntil in 0..25) {
                                NotificationHelper.showReminderNotification(
                                    context = applicationContext,
                                    courtName = reservation.courtName,
                                    date = reservation.date,
                                    startTime = reservation.startTime,
                                    endTime = reservation.endTime
                                )
                            }
                        } catch (e: Exception) {
                            // ignorar reservas con fecha inválida
                        }
                    }
            }
        return Result.success()
    }
}
