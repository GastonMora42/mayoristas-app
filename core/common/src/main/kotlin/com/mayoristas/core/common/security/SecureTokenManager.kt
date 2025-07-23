package com.mayoristas.core.common.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("auth_prefs")

@Singleton
class SecureTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            "secure_auth_prefs",
            MasterKeys.AES256_GCM_SPEC,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SBC,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
    }
    
    suspend fun saveUserSession(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
    
    suspend fun getCurrentUserId(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }
    
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        encryptedPrefs.edit().clear().apply()
    }
    
    fun enableBiometric() {
        encryptedPrefs.edit()
            .putBoolean(BIOMETRIC_ENABLED_KEY, true)
            .apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(BIOMETRIC_ENABLED_KEY, false)
    }
}