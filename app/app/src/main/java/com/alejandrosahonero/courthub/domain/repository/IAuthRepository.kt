package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.User

interface IAuthRepository {

    // Devuelve el User completo si hay sesión activa, null si no hay nadie logueado
    suspend fun getCurrentUser(): User?

    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(
        name: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun updateFcmToken(uid: String, token: String): Result<Unit>
}