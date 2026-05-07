package com.alejandrosahonero.courthub.domain.usecase

import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val repository = mockk<IAuthRepository>()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `returns failure when email is blank`() = runTest {
        val result = useCase("", "password123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "Email y contraseña requeridos")
    }

    @Test
    fun `returns failure when password is blank`() = runTest {
        val result = useCase("user@test.com", "")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "Email y contraseña requeridos")
    }

    @Test
    fun `returns failure when both fields are blank`() = runTest {
        val result = useCase("", "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `calls repository when credentials are valid`() = runTest {
        coEvery { repository.login(any(), any()) } returns
                Result.failure(Exception("Firebase error"))

        val result = useCase("user@test.com", "password123")
        assertTrue(result.isFailure)
    }
}
