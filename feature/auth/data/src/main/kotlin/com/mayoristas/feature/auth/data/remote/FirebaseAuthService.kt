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
            
            // Obtener datos adicionales del perfil desde Firestore
            val userProfile = getUserProfile(firebaseUser.uid)
            
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName,
                userType = userProfile?.userType ?: UserType.CLIENT,
                isVerified = firebaseUser.isEmailVerified,
                profile = userProfile?.profile,
                createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun register(credentials: RegisterCredentials): Result<User> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val result = firebaseAuth.createUserWithEmailAndPassword(
                credentials.email,
                credentials.password
            ).await()
            
            val firebaseUser = result.user ?: return Result.Error(Exception("Error al crear usuario"))
            
            // 2. Actualizar display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(credentials.displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // 3. Guardar datos adicionales en Firestore
            saveUserProfile(firebaseUser.uid, credentials)
            
            // 4. Enviar email de verificación
            firebaseUser.sendEmailVerification().await()
            
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = credentials.displayName,
                userType = credentials.userType,
                isVerified = false,
                profile = credentials.profile,
                createdAt = System.currentTimeMillis()
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            val firebaseUser = result.user ?: return Result.Error(Exception("Error con Google Auth"))
            
            // Si es nuevo usuario, necesita completar perfil B2B
            val userProfile = getUserProfile(firebaseUser.uid)
            if (userProfile == null && result.additionalUserInfo?.isNewUser == true) {
                // Redirigir a completar perfil empresarial
                return Result.Error(Exception("COMPLETE_PROFILE_REQUIRED"))
            }
            
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName,
                userType = userProfile?.userType ?: UserType.CLIENT,
                isVerified = firebaseUser.isEmailVerified,
                profile = userProfile?.profile,
                createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private suspend fun getUserProfile(userId: String): UserProfileData? {
        return try {
            val doc = firestore.collection("user_profiles").document(userId).get().await()
            doc.toObject(UserProfileData::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun saveUserProfile(userId: String, credentials: RegisterCredentials) {
        val profileData = UserProfileData(
            userType = credentials.userType,
            profile = credentials.profile
        )
        
        firestore.collection("user_profiles")
            .document(userId)
            .set(profileData)
            .await()
    }
}

// Modelo para Firestore
data class UserProfileData(
    val userType: UserType = UserType.CLIENT,
    val profile: UserProfile? = null
)