package com.alejandrosahonero.courthub.domain.usecase.auth

import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository

class LoginUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(IllegalArgumentException("Email y contraseña requeridos"))
        return repository.login(email, password)
    }
}