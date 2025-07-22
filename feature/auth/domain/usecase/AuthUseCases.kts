package com.mayoristas.feature.auth.domain.usecase

import com.mayoristas.core.common.dispatcher.DispatcherProvider
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<User> {
        return withContext(dispatcherProvider.io) {
            // Validación de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(credentials.email).matches()) {
                return@withContext Result.Error(Exception("Email inválido"))
            }
            
            // Validación de contraseña
            if (credentials.password.length < 6) {
                return@withContext Result.Error(Exception("La contraseña debe tener al menos 6 caracteres"))
            }
            
            authRepository.login(credentials)
        }
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(credentials: RegisterCredentials): Result<User> {
        return withContext(dispatcherProvider.io) {
            // Validaciones específicas B2B
            validateBusinessRegistration(credentials)?.let { error ->
                return@withContext Result.Error(Exception(error))
            }
            
            authRepository.register(credentials)
        }
    }
    
    private fun validateBusinessRegistration(credentials: RegisterCredentials): String? {
        return when {
            credentials.profile.companyName.isNullOrBlank() -> 
                "El nombre de la empresa es obligatorio"
            credentials.profile.businessType == null -> 
                "Debe seleccionar el tipo de negocio"
            credentials.profile.taxId.isNullOrBlank() -> 
                "El CUIT/RUT es obligatorio para empresas"
            !isValidTaxId(credentials.profile.taxId) -> 
                "CUIT/RUT inválido"
            else -> null
        }
    }
    
    private fun isValidTaxId(taxId: String): Boolean {
        // Validación básica para CUIT argentino (11 dígitos)
        return taxId.replace("-", "").length in 10..13
    }
}

class BiometricAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<User> {
        return withContext(dispatcherProvider.io) {
            authRepository.loginWithBiometric()
        }
    }
}