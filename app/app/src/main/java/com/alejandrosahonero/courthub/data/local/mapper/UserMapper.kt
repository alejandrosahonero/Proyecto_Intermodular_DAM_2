package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.model.UserRole

fun UserDto.toDomain(uid: String): User = User(
    uid = uid,
    name = name,
    email = email,
    role = UserRole.fromString(role),
    fcmToken = fcmToken,
    createdAt = createdAt?.toDate()?.time ?: 0L
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    name = name,
    email = email,
    role = role.value,
    fcmToken = fcmToken,
    createdAt = com.google.firebase.Timestamp(
        java.util.Date(createdAt)
    )
)