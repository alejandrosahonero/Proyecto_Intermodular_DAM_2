package com.alejandrosahonero.courthub.domain.usecase

import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.usecase.court.GetAvailableSlotsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAvailableSlotsUseCaseTest {

    private val repository = mockk<ICourtRepository>()
    private val useCase = GetAvailableSlotsUseCase(repository)

    @Test
    fun `returns 13 slots for a full day with no occupied slots`() = runTest {
        coEvery { repository.getOccupiedSlots(any(), any()) } returns
                Result.success(emptyList())

        val result = useCase("court_1", "2026-06-01")
        assertTrue(result.isSuccess)
        assertEquals(13, result.getOrNull()?.size)
    }

    @Test
    fun `marks occupied slots as not available`() = runTest {
        coEvery { repository.getOccupiedSlots(any(), any()) } returns
                Result.success(listOf("10:00", "11:00"))

        val slots = useCase("court_1", "2026-06-01").getOrNull()!!
        assertFalse(slots.first { it.hour == "10:00" }.isAvailable)
        assertFalse(slots.first { it.hour == "11:00" }.isAvailable)
        assertTrue(slots.first { it.hour == "09:00" }.isAvailable)
    }

    @Test
    fun `returns failure when repository fails`() = runTest {
        coEvery { repository.getOccupiedSlots(any(), any()) } returns
                Result.failure(Exception("Network error"))

        val result = useCase("court_1", "2026-06-01")
        assertTrue(result.isFailure)
    }

    @Test
    fun `all slots are unavailable when all are occupied`() = runTest {
        val allSlots = (9..21).map { "%02d:00".format(it) }
        coEvery { repository.getOccupiedSlots(any(), any()) } returns
                Result.success(allSlots)

        val slots = useCase("court_1", "2026-06-01").getOrNull()!!
        assertTrue(slots.none { it.isAvailable })
    }
}
