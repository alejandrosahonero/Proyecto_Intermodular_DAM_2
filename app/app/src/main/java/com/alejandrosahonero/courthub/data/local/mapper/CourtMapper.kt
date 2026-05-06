package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.CourtDto
import com.alejandrosahonero.courthub.data.model.local.CourtEntity
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.CourtType

fun CourtDto.toDomain(id: String): Court = Court(
    id = id,
    name = name,
    type = CourtType.fromString(type),
    pricePerHour = pricePerHour,
    isEnabled = isEnabled,
    description = description,
    imageUrl = imageUrl,
    centerId = centerId,
    disabledReason = disabledReason,
    disabledFrom = disabledFrom?.toDate()?.time,
    disabledUntil = disabledUntil?.toDate()?.time
)

fun Court.toDto(): CourtDto = CourtDto(
    name = name,
    type = type.value,
    pricePerHour = pricePerHour,
    isEnabled = isEnabled,
    description = description,
    imageUrl = imageUrl,
    centerId = centerId,
    disabledReason = disabledReason,
    disabledFrom = disabledFrom?.let {
        com.google.firebase.Timestamp(java.util.Date(it))
    },
    disabledUntil = disabledUntil?.let {
        com.google.firebase.Timestamp(java.util.Date(it))
    }
)

fun CourtEntity.toDomain(): Court = Court(
    id = courtIdFirebase,
    name = name,
    type = CourtType.fromString(type),
    pricePerHour = pricePerHour,
    isEnabled = isEnabled,
    description = description,
    imageUrl = imageUrl,
    centerId = centerId,
    disabledReason = disabledReason,
    disabledFrom = disabledFrom,
    disabledUntil = disabledUntil
)

fun Court.toEntity(): CourtEntity = CourtEntity(
    courtIdFirebase = id,
    name = name,
    type = type.value,
    pricePerHour = pricePerHour,
    isEnabled = isEnabled,
    description = description,
    imageUrl = imageUrl,
    centerId = centerId,
    disabledReason = disabledReason,
    disabledFrom = disabledFrom,
    disabledUntil = disabledUntil
)