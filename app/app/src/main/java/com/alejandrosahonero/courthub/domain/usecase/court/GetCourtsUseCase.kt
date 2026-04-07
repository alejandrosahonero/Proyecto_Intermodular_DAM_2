package com.alejandrosahonero.courthub.domain.usecase.court

import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import kotlinx.coroutines.flow.Flow

class GetCourtsUseCase(private val repository: ICourtRepository) {
    operator fun invoke(): Flow<List<Court>> =
        repository.getCourts()
}