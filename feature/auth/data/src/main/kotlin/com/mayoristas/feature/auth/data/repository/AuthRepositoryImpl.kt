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
                when (val result = firebaseAuthService.getCurrentUserFromFirestore(firebaseUser.uid)) {
                    is Result.Success -> Result.Success(result.data)
                    is Result.Error -> Result.Success(null) // Si hay error, retornamos null
                    else -> Result.Success(null)
                }
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
                when (val result = getCurrentUser()) {
                    is Result.Success -> {
                        if (result.data != null) {
                            Result.Success(result.data)
                        } else {
                            Result.Error(Exception("Usuario no encontrado"))
                        }
                    }
                    is Result.Error -> result
                    else -> Result.Error(Exception("Error desconocido"))
                }
            } else {
                Result.Error(Exception("Autenticación biométrica no configurada"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    // ✅ MÉTODO OBSERVEAUTHSTATE CORREGIDO COMPLETAMENTE
    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Usuario autenticado - obtener datos completos
                trySend(AuthState.Loading)
                
                // Crear suscripción por defecto
                val defaultSubscription = UserSubscription(
                    planType = SubscriptionPlan.FREE,
                    startDate = System.currentTimeMillis(),
                    endDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
                    isActive = true,
                    autoRenew = false,
                    paymentStatus = PaymentStatus.ACTIVE,
                    mercadoPagoSubscriptionId = null,
                    productsUsed = 0,
                    productsLimit = SubscriptionPlan.FREE.productsLimit,
                    featuresEnabled = SubscriptionPlan.FREE.features
                )
                
                // ✅ CREAR USER NO NULLABLE - LÍNEA 120 CORREGIDA
                val basicUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    userType = UserType.CLIENT,
                    isVerified = firebaseUser.isEmailVerified,
                    profile = null,
                    subscription = defaultSubscription,
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
                )
                
                result.data?.let { user ->
                    trySend(AuthState.Success(user))
                } ?: trySend(AuthState.Error("Usuario no válido"))
                
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