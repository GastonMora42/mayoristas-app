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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
                tokenManager.saveUserSession(result.data.id)
            }
        }
    }
    
    override suspend fun loginWithGoogle(): Result<User> {
        return Result.Error(Exception("Google Sign-In no implementado aún"))
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
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
                when (val result = firebaseAuthService.getCurrentUserFromFirestore(firebaseUser.uid)) {
                    is Result.Success -> Result.Success(result.data)
                    is Result.Error -> Result.Success(null)
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
            val userId = tokenManager.getCurrentUserId()
            val isBiometricEnabled = tokenManager.isBiometricEnabled()
            
            if (userId != null && isBiometricEnabled) {
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
    
    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
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
                
                // Crear usuario básico NO NULLABLE
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
                
                // Obtener datos completos de Firestore
                GlobalScope.launch {
                    try {
                        when (val result = firebaseAuthService.getCurrentUserFromFirestore(firebaseUser.uid)) {
                            is Result.Success -> {
                                // IMPORTANTE: Usar basicUser si result.data es null
                                val userData: User = result.data ?: basicUser
                                trySend(AuthState.Success(userData))
                            }
                            is Result.Error -> {
                                trySend(AuthState.Success(basicUser))
                            }
                            else -> {
                                trySend(AuthState.Success(basicUser))
                            }
                        }
                    } catch (e: Exception) {
                        trySend(AuthState.Success(basicUser))
                    }
                }
            } else {
                trySend(AuthState.Initial)
            }
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
}