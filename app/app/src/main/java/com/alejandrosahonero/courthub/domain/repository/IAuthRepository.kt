package com.alejandrosahonero.courthub.domain.repository

import com.alejandrosahonero.courthub.domain.model.User

interface IAuthRepository {

    // Devuelve el User completo si hay sesión activa, null si no hay nadie logueado
    suspend fun getCurrentUser(): User?

    suspend fun login(email: String, password: String): Result<User>

    suspend fun loginWithGoogle(idToken: String): Result<User>

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun updateFcmToken(uid: String, token: String): Result<Unit>

    suspend fun updateNotificationsEnabled(uid: String, enabled: Boolean): Result<Unit>

    suspend fun setUserEnabled(uid: String, enabled: Boolean): Result<Unit>

    suspend fun updateUserProfile(uid: String, name: String, phone: String): Result<Unit>

    suspend fun getFavorites(uid: String): Result<List<String>>

    suspend fun toggleFavorite(uid: String, courtId: String): Result<Boolean>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}