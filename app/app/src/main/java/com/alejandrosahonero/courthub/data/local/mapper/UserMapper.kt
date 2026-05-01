package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.data.model.local.UserEntity
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.model.UserRole

fun UserDto.toDomain(uid: String): User = User(
    uid = uid,
    name = name,
    email = email,
    phone = phone,
    role = UserRole.fromString(role),
    fcmToken = fcmToken,
    notificationsEnabled = notificationsEnabled,
    createdAt = createdAt?.toDate()?.time ?: 0L
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    name = name,
    email = email,
    phone = phone,
    role = role.value,
    fcmToken = fcmToken,
    notificationsEnabled = notificationsEnabled,
    createdAt = com.google.firebase.Timestamp(
        java.util.Date(createdAt)
    )
)

fun UserEntity.toDomain(): User = User(
    uid = uidFirebase,
    name = name,
    email = email,
    phone = phone,
    role = UserRole.fromString(role),
    fcmToken = fcmToken,
    notificationsEnabled = notificationsEnabled,
    createdAt = createdAt
)

fun User.toEntity(): UserEntity = UserEntity(
    uidFirebase = uid,
    name = name,
    email = email,
    phone = phone,
    role = role.value,
    fcmToken = fcmToken,
    notificationsEnabled = notificationsEnabled,
    createdAt = createdAt
)