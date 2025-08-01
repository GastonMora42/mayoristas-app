package com.mayoristas.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.mayoristas.core.common.result.Result
import com.mayoristas.core.common.security.SecureTokenManager
import com.mayoristas.feature.auth.data.local.BiometricAuthManager
import com.mayoristas.feature.auth.data.remote.FirebaseAuthService
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val biometricManager: BiometricAuthManager,
    private val tokenManager: SecureTokenManager,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override suspend fun login(credentials: LoginCredentials): Result<User> {
        return firebaseAuthService.login(credentials).also { result ->
            if (result is Result.Success) {
                tokenManager.saveUserSession(result.data.id)
            }
        }
    }
    
    override suspend fun register(credentials: RegisterCredentials): Result<User> {
        return firebaseAuthService.register(credentials).also { result ->
            if (result is Result.Success) {
                // Guardamos la sesión pero el usuario necesita verificar email
                tokenManager.saveUserSession(result.data.id)
            }
        }
    }
    
    override suspend fun loginWithGoogle(): Result<User> {
        // TODO: Implementar con Google Sign-In SDK
        return Result.Error(Exception("Google Sign-In no implementado aún"))
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // 1. Cerrar sesión en Firebase
            firebaseAuth.signOut()
            
            // 2. Limpiar tokens locales
            tokenManager.clearUserSession()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Obtener datos completos del usuario desde Firestore
                firebaseAuthService.getCurrentUserFromFirestore(firebaseUser.uid)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfile(profile: UserProfile): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firebaseAuthService.updateUserProfile(firebaseUser.uid, profile)
            } else {
                Result.Error(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return firebaseAuthService.sendPasswordResetEmail(email)
    }
    
    override suspend fun verifyEmail(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firebaseUser.sendEmailVerification()
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun enableBiometric(): Result<Unit> {
        return if (biometricManager.isBiometricAvailable()) {
            tokenManager.enableBiometric()
            Result.Success(Unit)
        } else {
            Result.Error(Exception("Autenticación biométrica no disponible"))
        }
    }
    
    override suspend fun loginWithBiometric(): Result<User> {
        return try {
            // Verificar si hay un usuario guardado y biométrico habilitado
            val userId = tokenManager.getCurrentUserId()
            val isBiometricEnabled = tokenManager.isBiometricEnabled()
            
            if (userId != null && isBiometricEnabled) {
                // Obtener datos del usuario actual
                getCurrentUser()
            } else {
                Result.Error(Exception("Autenticación biométrica no configurada"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // Usuario autenticado - obtener datos completos
                trySend(AuthState.Loading)
                
                // Aquí deberíamos obtener los datos completos del usuario
                // Por simplicidad, creamos un usuario básico
                val basicUser = User(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    userType = UserType.CLIENT, // Por defecto, se actualizará cuando obtengamos los datos reales
                    isVerified = user.isEmailVerified,
                    profile = null,
                    createdAt = user.metadata?.creationTimestamp ?: 0L
                )
                trySend(AuthState.Success(basicUser))
            } else {
                // Usuario no autenticado
                trySend(AuthState.Initial)
            }
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
}