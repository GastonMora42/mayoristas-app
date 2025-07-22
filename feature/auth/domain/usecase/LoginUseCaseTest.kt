package com.mayoristas.feature.auth.domain.usecase

import com.mayoristas.core.common.dispatcher.DispatcherProvider
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class LoginUseCaseTest {
    
    private val authRepository = mockk<AuthRepository>()
    private val dispatcherProvider = mockk<DispatcherProvider>()
    private lateinit var loginUseCase: LoginUseCase
    
    @Before
    fun setup() {
        every { dispatcherProvider.io } returns Dispatchers.Unconfined
        loginUseCase = LoginUseCase(authRepository, dispatcherProvider)
    }
    
    @Test
    fun `login with valid credentials should return success`() = runTest {
        // Given
        val credentials = LoginCredentials("test@example.com", "password123")
        val expectedUser = User(
            id = "123",
            email = "test@example.com",
            displayName = "Test User",
            userType = UserType.SELLER,
            isVerified = true,
            profile = null,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { authRepository.login(credentials) } returns Result.Success(expectedUser)
        
        // When
        val result = loginUseCase(credentials)
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedUser, (result as Result.Success).data)
        coVerify { authRepository.login(credentials) }
    }
    
    @Test
    fun `login with invalid email should return error`() = runTest {
        // Given
        val credentials = LoginCredentials("invalid-email", "password123")
        
        // When
        val result = loginUseCase(credentials)
        
        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("Email inválido") == true)
        coVerify { authRepository wasNot Called }
    }
    
    @Test
    fun `login with short password should return error`() = runTest {
        // Given
        val credentials = LoginCredentials("test@example.com", "123")
        
        // When
        val result = loginUseCase(credentials)
        
        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("6 caracteres") == true)
        coVerify { authRepository wasNot Called }
    }
}