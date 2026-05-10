package com.alejandrosahonero.courthub.domain.usecase

import com.alejandrosahonero.courthub.domain.model.AccessCodeStatus
import com.alejandrosahonero.courthub.domain.model.Reservation
import com.alejandrosahonero.courthub.domain.model.ReservationStatus
import com.alejandrosahonero.courthub.domain.repository.IReservationRepository
import com.alejandrosahonero.courthub.domain.usecase.reservation.CancelReservationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CancelReservationUseCaseTest {

    private val repository = mockk<IReservationRepository>()
    private val useCase = CancelReservationUseCase(repository)

    private fun reservation(
        hoursFromNow: Long,
        status: ReservationStatus = ReservationStatus.CONFIRMED
    ): Reservation {
        val start = ZonedDateTime.now(ZoneId.systemDefault()).plusHours(hoursFromNow)
        return Reservation(
            id = "res_1",
            userId = "user_1",
            courtId = "court_1",
            date = start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
            startTime = "%02d:00".format(start.hour),
            endTime = "%02d:00".format(start.hour + 1),
            status = status,
            accessCodeStatus = AccessCodeStatus.VALID
        )
    }

    @Test
    fun `returns failure when less than 24 hours remain`() = runTest {
        val result = useCase(reservation(hoursFromNow = 10))
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("24 horas") == true
        )
    }

    @Test
    fun `returns failure when reservation is already cancelled`() = runTest {
        val result = useCase(reservation(25, ReservationStatus.CANCELLED))
        assertTrue(result.isFailure)
    }

    @Test
    fun `calls repository when cancellation is valid`() = runTest {
        coEvery { repository.cancelReservation(any(), any()) } returns Result.success(Unit)
        val result = useCase(reservation(hoursFromNow = 25))
        assertTrue(result.isSuccess)
        coVerify { repository.cancelReservation("res_1", any()) }
    }
}
