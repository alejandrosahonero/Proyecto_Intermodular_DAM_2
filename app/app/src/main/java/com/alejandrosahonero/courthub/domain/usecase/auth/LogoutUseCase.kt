package com.alejandrosahonero.courthub.domain.usecase.auth

import com.alejandrosahonero.courthub.domain.repository.IAuthRepository

class LogoutUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(): Result<Unit> =
        repository.logout()
}