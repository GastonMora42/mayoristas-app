package com.mayoristas.feature.auth.data.repository

import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.data.remote.FirebaseAuthService
import com.mayoristas.feature.auth.data.local.BiometricAuthManager
import com.mayoristas.feature.auth.data.local.SecureTokenManager
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val biometricManager: BiometricAuthManager,
    private val tokenManager: SecureTokenManager
) : AuthRepository {
    
    override suspend fun login(credentials: LoginCredentials): Result<User> {
        return firebaseAuthService.login(credentials).also { result ->
            if (result is Result.Success) {
                tokenManager.saveUserSession(result.data.id)
            }
        }
    }
    
    override suspend fun register(credentials: RegisterCredentials): Result<User> {
        return firebaseAuthService.register(credentials)
    }
    
    override suspend fun loginWithGoogle(): Result<User> {
        // Implementar con Google Sign-In
        return Result.Error(Exception("No implementado aún"))
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearUserSession()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val userId = tokenManager.getCurrentUserId()
            if (userId != null) {
                // Obtener datos actuales del usuario
                Result.Success(null) // Implementar
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfile(profile: UserProfile): Result<User> {
        return Result.Error(Exception("No implementado aún"))
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return firebaseAuthService.sendPasswordResetEmail(email)
    }
    
    override suspend fun verifyEmail(): Result<Unit> {
        return Result.Error(Exception("No implementado aún"))
    }
    
    override suspend fun enableBiometric(): Result<Unit> {
        return if (biometricManager.isBiometricAvailable()) {
            tokenManager.enableBiometric()
            Result.Success(Unit)
        } else {
            Result.Error(Exception("Biometric no disponible"))
        }
    }
    
    override suspend fun loginWithBiometric(): Result<User> {
        return Result.Error(Exception("No implementado aún"))
    }
    
    override fun observeAuthState(): Flow<AuthState> = flow {
        emit(AuthState.Initial)
        // Implementar observación de estado
    }
}