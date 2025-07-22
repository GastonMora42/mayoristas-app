package com.mayoristas.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val biometricAuthUseCase: BiometricAuthUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = loginUseCase(LoginCredentials(email, password))
            
            _authState.value = when (result) {
                is Result.Success -> AuthState.Success(result.data)
                is Result.Error -> AuthState.Error(
                    result.exception.message ?: "Error al iniciar sesión"
                )
                is Result.Loading -> AuthState.Loading
            }
        }
    }
    
    fun register(
        email: String,
        password: String,
        displayName: String,
        userType: UserType,
        profile: UserProfile
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val credentials = RegisterCredentials(
                email = email,
                password = password,
                displayName = displayName,
                userType = userType,
                profile = profile
            )
            
            val result = registerUseCase(credentials)
            
            _authState.value = when (result) {
                is Result.Success -> AuthState.Success(result.data)
                is Result.Error -> AuthState.Error(
                    result.exception.message ?: "Error al registrarse"
                )
                is Result.Loading -> AuthState.Loading
            }
        }
    }
    
    fun authenticateWithBiometric() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = biometricAuthUseCase()
            
            _authState.value = when (result) {
                is Result.Success -> AuthState.Success(result.data)
                is Result.Error -> AuthState.Error(
                    result.exception.message ?: "Error en autenticación biométrica"
                )
                is Result.Loading -> AuthState.Loading
            }
        }
    }
    
    fun updateUserType(userType: UserType) {
        _uiState.value = _uiState.value.copy(selectedUserType = userType)
    }
    
    fun clearError() {
        _authState.value = AuthState.Initial
    }
}

data class AuthUiState(
    val selectedUserType: UserType = UserType.CLIENT,
    val showBiometric: Boolean = false
)