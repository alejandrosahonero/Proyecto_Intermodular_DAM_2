package com.alejandrosahonero.courthub.data.repository.impl

import com.alejandrosahonero.courthub.data.local.dao.UserDao
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.local.mapper.toEntity
import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.model.UserRole
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
            val doc = firestore.collection(Constants.COLLECTION_USERS)
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

            val doc = firestore.collection(Constants.COLLECTION_USERS)
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

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Error al iniciar sesión con Google"))

            // Verificamos si el usuario ya existe en Firestore
            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val doc = docRef.get().await()

            val user = if (!doc.exists()) {
                // Nuevo usuario — lo creamos
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    role = UserRole.CLIENT,
                    fcmToken = "",
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(
                    mapOf(
                        "uid" to newUser.uid,
                        "name" to newUser.name,
                        "email" to newUser.email,
                        "phone" to "",
                        "role" to newUser.role.value,
                        "fcmToken" to "",
                        "notificationsEnabled" to true,
                        "isEnabled" to true,
                        "createdAt" to Timestamp(java.util.Date(newUser.createdAt))
                    )
                ).await()
                newUser
            } else {
                val dto = doc.toObject(UserDto::class.java)
                    ?: return Result.failure(Exception("Error al leer usuario"))
                dto.toDomain(firebaseUser.uid)
            }

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
            firestore.collection(Constants.COLLECTION_USERS)
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
            firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationsEnabled(uid: String, enabled: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("notificationsEnabled", enabled).await()
            val local = userDao.getUserByUid(uid)
            if (local != null) userDao.insertUser(local.copy(notificationsEnabled = enabled))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setUserEnabled(uid: String, enabled: Boolean): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(uid)

            batch.update(userRef, "isEnabled", enabled)

            // Notificación al Administrador (historial)
            val adminNotificationRef =
                firestore.collection(Constants.COLLECTION_NOTIFICATIONS).document()
            val action = if (enabled) "habilitado" else "deshabilitado"
            batch.set(
                adminNotificationRef, mapOf(
                    "userId" to (firebaseAuth.currentUser?.uid ?: ""),
                    "title" to "Usuario $action",
                    "body" to "Has $action al usuario con ID $uid.",
                    "type" to "maintenance",
                    "isRead" to false,
                    "createdAt" to Timestamp.now()
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(uid: String, name: String, phone: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update(mapOf("name" to name, "phone" to phone)).await()
            val local = userDao.getUserByUid(uid)
            if (local != null) {
                userDao.insertUser(local.copy(name = name, phone = phone))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavorites(uid: String): Result<List<String>> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()

            @Suppress("UNCHECKED_CAST")
            val favorites = doc.get("favorites") as? List<String> ?: emptyList()
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(uid: String, courtId: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()

            @Suppress("UNCHECKED_CAST")
            val current = (doc.get("favorites") as? List<String> ?: emptyList()).toMutableList()
            val isNowFavorite = if (current.contains(courtId)) {
                current.remove(courtId)
                false
            } else {
                current.add(courtId)
                true
            }
            firestore.collection("users").document(uid)
                .update("favorites", current).await()
            Result.success(isNowFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}