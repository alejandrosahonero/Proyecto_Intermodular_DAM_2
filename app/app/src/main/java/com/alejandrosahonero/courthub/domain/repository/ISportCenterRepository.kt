package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.SportCenter
import kotlinx.coroutines.flow.Flow

interface ISportCenterRepository {
    fun getSportCenters(): Flow<List<SportCenter>>
    suspend fun getSportCenterById(id: String): Result<SportCenter>
    suspend fun createSportCenter(center: SportCenter): Result<Unit>
    suspend fun updateSportCenter(center: SportCenter): Result<Unit>
    suspend fun deleteSportCenter(id: String): Result<Unit>
}
