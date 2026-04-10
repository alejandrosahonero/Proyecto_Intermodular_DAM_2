package com.alejandrosahonero.courthub.data.local.mapper

import com.alejandrosahonero.courthub.data.model.firestore.ReservationDto
import com.alejandrosahonero.courthub.data.model.local.ReservationEntity
import com.alejandrosahonero.courthub.domain.model.AccessCodeStatus
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.google.firebase.Timestamp
import java.util.Date

fun ReservationDto.toDomain(id: String): Reservation = Reservation(
    id = id,
    userId = userId,
    userName = userName,
    courtId = courtId,
    courtName = courtName,
    date = date,
    startTime = startTime,
    endTime = endTime,
    status = ReservationStatus.fromString(status),
    totalPrice = totalPrice,
    paymentId = paymentId,
    accessCode = accessCode,
    accessCodeStatus = AccessCodeStatus.fromString(accessCodeStatus),
    qrData = qrData,
    cancellationReason = cancellationReason,
    createdAt = createdAt?.toDate()?.time ?: 0L,
    cancelledAt = cancelledAt?.toDate()?.time,
    scannedAt = scannedAt?.toDate()?.time
)

fun Reservation.toDto(): ReservationDto = ReservationDto(
    userId = userId,
    userName = userName,
    courtId = courtId,
    courtName = courtName,
    date = date,
    startTime = startTime,
    endTime = endTime,
    status = status.value,
    totalPrice = totalPrice,
    paymentId = paymentId,
    accessCode = accessCode,
    accessCodeStatus = accessCodeStatus.value,
    qrData = qrData,
    cancellationReason = cancellationReason,
    createdAt = Timestamp(Date(createdAt)),
    cancelledAt = cancelledAt?.let { Timestamp(Date(it)) },
    scannedAt = scannedAt?.let { Timestamp(Date(it)) }
)

fun ReservationEntity.toDomain(): Reservation = Reservation(
    id = reservationIdFirebase,
    userId = userId,
    userName = userName,
    courtId = courtId,
    courtName = courtName,
    date = date,
    startTime = startTime,
    endTime = endTime,
    status = ReservationStatus.fromString(status),
    totalPrice = totalPrice,
    paymentId = paymentId,
    accessCode = accessCode,
    accessCodeStatus = AccessCodeStatus.fromString(accessCodeStatus),
    qrData = qrData,
    cancellationReason = cancellationReason,
    createdAt = createdAt,
    cancelledAt = cancelledAt,
    scannedAt = scannedAt
)

fun Reservation.toEntity(): ReservationEntity = ReservationEntity(
    reservationIdFirebase = id,
    userId = userId,
    userName = userName,
    courtId = courtId,
    courtName = courtName,
    date = date,
    startTime = startTime,
    endTime = endTime,
    status = status.value,
    totalPrice = totalPrice,
    paymentId = paymentId,
    accessCode = accessCode,
    accessCodeStatus = accessCodeStatus.value,
    qrData = qrData,
    cancellationReason = cancellationReason,
    createdAt = createdAt,
    cancelledAt = cancelledAt,
    scannedAt = scannedAt
)