package com.alejandrosahonero.courthub.domain.usecase.court

import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.utils.DateUtils

data class TimeSlot(
    val hour: String,      // "HH:mm"
    val isAvailable: Boolean,
    val isMaintenance: Boolean = false
)

class GetAvailableSlotsUseCase(private val repository: ICourtRepository) {

    suspend operator fun invoke(
        courtId: String,
        date: String,
        disabledFrom: Long? = null,
        disabledUntil: Long? = null
    ): Result<List<TimeSlot>> {
        val occupiedResult = repository.getOccupiedSlots(courtId, date)
        if (occupiedResult.isFailure)
            return Result.failure(occupiedResult.exceptionOrNull()!!)

        val occupied = occupiedResult.getOrDefault(emptyList())

        val allSlots = (9..21).map { hour ->
            DateUtils.formatHour(hour)
        }

        val slots = allSlots.map { hour ->
            val isMaintenance = isInMaintenanceRange(hour, date, disabledFrom, disabledUntil)
            TimeSlot(
                hour = hour,
                isAvailable = !occupied.contains(hour) && !isMaintenance,
                isMaintenance = isMaintenance
            )
        }

        return Result.success(slots)
    }

    private fun isInMaintenanceRange(
        hour: String,
        date: String,
        disabledFrom: Long?,
        disabledUntil: Long?
    ): Boolean {
        if (disabledFrom == null || disabledUntil == null) return false
        return try {
            val slotMillis = java.time.ZonedDateTime.of(
                java.time.LocalDate.parse(date),
                java.time.LocalTime.parse(hour),
                java.time.ZoneId.systemDefault()
            ).toInstant().toEpochMilli()
            slotMillis in disabledFrom..disabledUntil
        } catch (e: Exception) {
            false
        }
    }
}