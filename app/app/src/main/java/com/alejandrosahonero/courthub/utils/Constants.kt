package com.alejandrosahonero.courthub.utils

object Constants {
    const val COLLECTION_USERS = "users"
    const val COLLECTION_COURTS = "courts"
    const val COLLECTION_RESERVATIONS = "reservations"
    const val COLLECTION_NOTIFICATIONS = "notifications"

    const val ROLE_ADMIN = "admin"
    const val ROLE_CLIENT = "client"

    const val STATUS_CONFIRMED = "confirmed"
    const val STATUS_CANCELLED = "cancelled"
    const val STATUS_EXPIRED = "expired"

    const val ACCESS_VALID = "VALID"
    const val ACCESS_INVALID = "INVALID"
    const val ACCESS_USED = "USED"

    const val CANCELLATION_HOURS_LIMIT = 24L
    const val SLOT_DURATION_HOURS = 1L
}