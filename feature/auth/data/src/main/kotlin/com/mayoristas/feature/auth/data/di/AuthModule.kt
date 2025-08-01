package com.mayoristas.feature.auth.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mayoristas.core.common.security.SecureTokenManager
import com.mayoristas.feature.auth.data.local.BiometricAuthManager
import com.mayoristas.feature.auth.data.remote.FirebaseAuthService
import com.mayoristas.feature.auth.data.repository.AuthRepositoryImpl
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseAuthService = FirebaseAuthService(firebaseAuth, firestore)
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthService: FirebaseAuthService,
        biometricManager: BiometricAuthManager,
        tokenManager: SecureTokenManager,
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(
        firebaseAuthService,
        biometricManager,
        tokenManager,
        firebaseAuth
    )
}