package com.alejandrosahonero.courthub.domain.usecase.auth

import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository

class RegisterUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(IllegalArgumentException("Todos los campos son obligatorios"))
        if (password != confirmPassword)
            return Result.failure(IllegalArgumentException("Las contraseñas no coinciden"))
        if (password.length < 6)
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 6 caracteres"))
        return repository.register(name, email, password)
    }
}