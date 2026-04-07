package com.alejandrosahonero.courthub.domain.usecase.court

import com.alejandrosahonero.courthub.domain.repository.ICourtRepository

class DisableCourtUseCase(private val repository: ICourtRepository) {
    suspend operator fun invoke(
        courtId: String,
        reason: String,
        disabledFrom: Long,
        disabledUntil: Long
    ): Result<Unit> {
        if (reason.isBlank())
            return Result.failure(IllegalArgumentException("El motivo es obligatorio"))
        if (disabledFrom >= disabledUntil)
            return Result.failure(IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin"))
        return repository.disableCourt(courtId, reason, disabledFrom, disabledUntil)
    }
}