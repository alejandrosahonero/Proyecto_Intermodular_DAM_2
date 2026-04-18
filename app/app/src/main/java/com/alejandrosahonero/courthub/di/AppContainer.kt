package com.alejandrosahonero.courthub.di

import android.content.Context
import com.alejandrosahonero.courthub.data.local.database.AppDatabase
import com.alejandrosahonero.courthub.data.repository.impl.AuthRepositoryImpl
import com.alejandrosahonero.courthub.data.repository.impl.CourtRepositoryImpl
import com.alejandrosahonero.courthub.data.repository.impl.NotificationRepositoryImpl
import com.alejandrosahonero.courthub.data.repository.impl.ReservationRepositoryImpl
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.domain.usecase.access.GenerateAccessCodeUseCase
import com.alejandrosahonero.courthub.domain.usecase.access.ValidateAccessCodeUseCase
import com.alejandrosahonero.courthub.domain.usecase.auth.LoginUseCase
import com.alejandrosahonero.courthub.domain.usecase.auth.LogoutUseCase
import com.alejandrosahonero.courthub.domain.usecase.auth.RegisterUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.DisableCourtUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.GetAvailableSlotsUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.GetCourtsUseCase
import com.alejandrosahonero.courthub.domain.usecase.reservation.CancelReservationUseCase
import com.alejandrosahonero.courthub.domain.usecase.reservation.CreateReservationUseCase
import com.alejandrosahonero.courthub.domain.usecase.reservation.GetUserReservationsUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {

    // ── Firebase ──────────────────────────────────────────────────────────────
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // ── Room ──────────────────────────────────────────────────────────────────
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val userDao = database.userDao()
    private val courtDao = database.courtDao()
    private val reservationDao = database.reservationDao()
    private val notificationDao = database.notificationDao()

    // ── Repositorios ─────────────────────────────────────────────────────────
    val authRepository: IAuthRepository = AuthRepositoryImpl(
        firebaseAuth = firebaseAuth,
        firestore = firestore,
        userDao = userDao
    )

    val courtRepository: ICourtRepository = CourtRepositoryImpl(
        firestore = firestore,
        courtDao = courtDao
    )

    val reservationRepository: IReservationRepository = ReservationRepositoryImpl(
        firestore = firestore,
        reservationDao = reservationDao
    )

    val notificationRepository: INotificationRepository = NotificationRepositoryImpl(
        firestore = firestore,
        notificationDao = notificationDao
    )

    // ── Use Cases — Auth ──────────────────────────────────────────────────────
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val logoutUseCase = LogoutUseCase(authRepository)

    // ── Use Cases — Court ─────────────────────────────────────────────────────
    val getCourtsUseCase = GetCourtsUseCase(courtRepository)
    val getAvailableSlotsUseCase = GetAvailableSlotsUseCase(courtRepository)
    val disableCourtUseCase = DisableCourtUseCase(courtRepository)

    // ── Use Cases — Reservation ───────────────────────────────────────────────
    val createReservationUseCase = CreateReservationUseCase(reservationRepository)
    val cancelReservationUseCase = CancelReservationUseCase(reservationRepository)
    val getUserReservationsUseCase = GetUserReservationsUseCase(reservationRepository)

    // ── Use Cases — Access ────────────────────────────────────────────────────
    val generateAccessCodeUseCase = GenerateAccessCodeUseCase()
    val validateAccessCodeUseCase = ValidateAccessCodeUseCase()
}