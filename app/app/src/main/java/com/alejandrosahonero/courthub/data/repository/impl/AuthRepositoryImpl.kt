package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.UserDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toEntity
import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.model.UserRole
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : IAuthRepository {

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        // Intentamos primero desde Room (más rápido)
        val local = userDao.getUserByUid(firebaseUser.uid)
        if (local != null) return local.toDomain()
        // Si no hay caché local, consultamos Firestore
        return try {
            val doc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get().await()
            val dto = doc.toObject(UserDto::class.java) ?: return null
            val user = dto.toDomain(firebaseUser.uid)
            userDao.insertUser(user.toEntity())
            user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Error al iniciar sesión"))

            val doc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get().await()
            val dto = doc.toObject(UserDto::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado en Firestore"))

            val user = dto.toDomain(firebaseUser.uid)
            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Error al crear el usuario"))

            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                role = UserRole.CLIENT,
                fcmToken = "",
                createdAt = System.currentTimeMillis()
            )

            // Guardamos en Firestore
            val dto = mapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "role" to user.role.value,
                "fcmToken" to user.fcmToken,
                "createdAt" to Timestamp(java.util.Date(user.createdAt))
            )
            firestore.collection("users")
                .document(user.uid)
                .set(dto).await()

            // Guardamos en Room
            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
            firebaseAuth.signOut()
            if (uid != null) userDao.deleteUserByUid(uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}