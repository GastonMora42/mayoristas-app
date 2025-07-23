package com.mayoristas.feature.auth.domain.repository

import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<User>
    suspend fun register(credentials: RegisterCredentials): Result<User>
    suspend fun loginWithGoogle(): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateProfile(profile: UserProfile): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun verifyEmail(): Result<Unit>
    suspend fun enableBiometric(): Result<Unit>
    suspend fun loginWithBiometric(): Result<User>
    fun observeAuthState(): Flow<AuthState>
}