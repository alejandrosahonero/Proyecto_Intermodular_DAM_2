package com.alejandrosahonero.courthub.domain.usecase

import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.RegisterUseCase
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val repository = mockk<IAuthRepository>()
    private val useCase = RegisterUseCase(repository)

    @Test
    fun `returns failure when name is blank`() = runTest {
        val result = useCase("", "user@test.com", "pass123", "pass123")
        assertTrue(result.isFailure)
        assertEquals("Todos los campos son obligatorios", result.exceptionOrNull()?.message)
    }

    @Test
    fun `returns failure when passwords do not match`() = runTest {
        val result = useCase("Juan", "user@test.com", "pass123", "pass456")
        assertTrue(result.isFailure)
        assertEquals("Las contraseñas no coinciden", result.exceptionOrNull()?.message)
    }

    @Test
    fun `returns failure when password is too short`() = runTest {
        val result = useCase("Juan", "user@test.com", "abc", "abc")
        assertTrue(result.isFailure)
        assertEquals(
            "La contraseña debe tener al menos 6 caracteres",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `returns failure when email is blank`() = runTest {
        val result = useCase("Juan", "", "pass123", "pass123")
        assertTrue(result.isFailure)
    }
}
