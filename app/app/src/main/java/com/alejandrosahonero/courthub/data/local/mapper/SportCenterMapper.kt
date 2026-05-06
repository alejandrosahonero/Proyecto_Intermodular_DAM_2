package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.SportCenterDto
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.google.firebase.Timestamp

fun SportCenterDto.toDomain(id: String): SportCenter = SportCenter(
    id = id,
    name = name,
    description = description,
    address = address,
    city = city,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    phone = phone,
    email = email,
    isEnabled = isEnabled,
    createdAt = createdAt?.toDate()?.time ?: 0L
)

fun SportCenter.toDto(): SportCenterDto = SportCenterDto(
    name = name,
    description = description,
    address = address,
    city = city,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    phone = phone,
    email = email,
    isEnabled = isEnabled,
    createdAt = Timestamp(java.util.Date(createdAt))
)
