package com.mayoristas.feature.auth.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun login(credentials: LoginCredentials): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(
                credentials.email, 
                credentials.password
            ).await()
            
            val firebaseUser = result.user ?: return Result.Error(Exception("Usuario no encontrado"))
            
            // Obtener datos completos del usuario desde Firestore
            getCurrentUserFromFirestore(firebaseUser.uid)
            
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun register(credentials: RegisterCredentials): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(
                credentials.email,
                credentials.password
            ).await()
            
            val firebaseUser = result.user ?: return Result.Error(Exception("Error al crear usuario"))
            
            // Actualizar perfil de Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(credentials.displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Guardar datos en Firestore
            saveUserProfile(firebaseUser.uid, credentials)
            
            // Enviar email de verificación
            firebaseUser.sendEmailVerification().await()
            
            // Crear suscripción gratuita por defecto
            val defaultSubscription = UserSubscription(
                planType = SubscriptionPlan.FREE,
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 año
                isActive = true,
                autoRenew = false,
                paymentStatus = PaymentStatus.ACTIVE,
                mercadoPagoSubscriptionId = null,
                productsUsed = 0,
                productsLimit = SubscriptionPlan.FREE.productsLimit,
                featuresEnabled = SubscriptionPlan.FREE.features
            )
            
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = credentials.displayName,
                userType = credentials.userType,
                isVerified = false,
                profile = credentials.profile,
                subscription = defaultSubscription, // ✅ Agregado
                createdAt = System.currentTimeMillis()
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getCurrentUserFromFirestore(userId: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser 
                ?: return Result.Error(Exception("Usuario no autenticado"))
            
            val doc = firestore.collection("user_profiles").document(userId).get().await()
            
            if (doc.exists()) {
                val userProfileData = doc.toObject(UserProfileData::class.java)
                
                // Crear suscripción por defecto si no existe
                val subscription = userProfileData?.subscription ?: UserSubscription(
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
                
                val user = User(
                    id = userId,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    userType = userProfileData?.userType ?: UserType.CLIENT,
                    isVerified = firebaseUser.isEmailVerified,
                    profile = userProfileData?.profile,
                    subscription = subscription, // ✅ Agregado
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
                )
                
                Result.Success(user)
            } else {
                // Usuario en Auth pero sin perfil en Firestore
                Result.Error(Exception("Perfil de usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updateUserProfile(userId: String, profile: UserProfile): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser 
                ?: return Result.Error(Exception("Usuario no autenticado"))
            
            // Actualizar en Firestore
            val updates = mapOf(
                "profile" to profile,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("user_profiles")
                .document(userId)
                .update(updates)
                .await()
            
            // Retornar usuario actualizado
            getCurrentUserFromFirestore(userId)
            
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private suspend fun saveUserProfile(userId: String, credentials: RegisterCredentials) {
        val profileData = UserProfileData(
            userType = credentials.userType,
            profile = credentials.profile,
            subscription = UserSubscription(
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
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        firestore.collection("user_profiles")
            .document(userId)
            .set(profileData)
            .await()
    }
}

data class UserProfileData(
    val userType: UserType = UserType.CLIENT,
    val profile: UserProfile? = null,
    val subscription: UserSubscription? = null, // ✅ Agregado
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)