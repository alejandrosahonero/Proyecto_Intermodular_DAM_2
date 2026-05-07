package com.alejandrosahonero.courthub.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ReservationTest {

    private fun makeReservation(hoursFromNow: Long): Reservation {
        val start = ZonedDateTime.now(ZoneId.systemDefault()).plusHours(hoursFromNow)
        return Reservation(
            id = "test_id",
            userId = "user_1",
            courtId = "court_1",
            date = start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
            startTime = "%02d:00".format(start.hour),
            endTime = "%02d:00".format(start.hour + 1),
            status = ReservationStatus.CONFIRMED,
            accessCodeStatus = AccessCodeStatus.VALID
        )
    }

    @Test
    fun `canBeCancelled returns true when more than 24 hours remain`() {
        val reservation = makeReservation(hoursFromNow = 25)
        assertTrue(reservation.canBeCancelled())
    }

    @Test
    fun `canBeCancelled returns false when less than 24 hours remain`() {
        val reservation = makeReservation(hoursFromNow = 10)
        assertFalse(reservation.canBeCancelled())
    }

    @Test
    fun `canBeCancelled returns false when reservation is already cancelled`() {
        val reservation = makeReservation(hoursFromNow = 25)
            .copy(status = ReservationStatus.CANCELLED)
        assertFalse(reservation.canBeCancelled())
    }

    @Test
    fun `canBeCancelled returns false when reservation is expired`() {
        val reservation = makeReservation(hoursFromNow = 25)
            .copy(status = ReservationStatus.EXPIRED)
        assertFalse(reservation.canBeCancelled())
    }
}
