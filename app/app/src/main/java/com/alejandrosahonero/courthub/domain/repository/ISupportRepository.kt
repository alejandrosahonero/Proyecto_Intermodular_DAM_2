package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.SupportSettings

interface ISupportRepository {
    suspend fun getSupportSettings(): Result<SupportSettings>
    suspend fun updateSupportSettings(settings: SupportSettings): Result<Unit>
}