package com.mayoristas.feature.auth.data.local

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mayoristas.core.common.result.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun isBiometricAvailable(): Boolean {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or 
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    suspend fun authenticateWithBiometric(activity: FragmentActivity): Result<Boolean> {
        return suspendCoroutine { continuation ->
            val biometricPrompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(Result.Success(true))
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(Result.Error(Exception(errString.toString())))
                    }
                    
                    override fun onAuthenticationFailed() {
                        continuation.resume(Result.Error(Exception("Autenticación biométrica falló")))
                    }
                }
            )
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso seguro")
                .setSubtitle("Usa tu huella o reconocimiento facial para acceder")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
            
            biometricPrompt.authenticate(promptInfo)
        }
    }
}