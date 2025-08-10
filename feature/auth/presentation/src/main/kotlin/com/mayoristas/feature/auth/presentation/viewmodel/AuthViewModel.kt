package com.mayoristas.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.usecase.LoginUseCase
import com.mayoristas.feature.auth.domain.usecase.RegisterUseCase
import com.mayoristas.feature.auth.domain.usecase.BiometricAuthUseCase
import com.mayoristas.feature.auth.domain.repository.AuthRepository
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
    private val biometricAuthUseCase: BiometricAuthUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()
    
    // Login State
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    // Register State  
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { authState ->
                _uiState.value = _uiState.value.copy(
                    authState = authState,
                    isLoading = authState is AuthState.Loading
                )
            }
        }
    }
    
    // === LOGIN ACTIONS ===
    fun onEmailChanged(email: String) {
        _loginState.value = _loginState.value.copy(
            email = email,
            emailError = null
        )
    }
    
    fun onPasswordChanged(password: String) {
        _loginState.value = _loginState.value.copy(
            password = password,
            passwordError = null
        )
    }
    
    fun login() {
        val currentState = _loginState.value
        
        // Validaciones UI
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _loginState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        viewModelScope.launch {
            _loginState.value = currentState.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(authState = AuthState.Loading)
            
            val result = loginUseCase(
                LoginCredentials(
                    email = currentState.email,
                    password = currentState.password
                )
            )
            
            when (result) {
                is Result.Success -> {
                    _loginState.value = LoginState() // Reset state
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Success(result.data)
                    )
                }
                is Result.Error -> {
                    _loginState.value = currentState.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Error(result.exception.message ?: "Error desconocido")
                    )
                }
                else -> {
                    _loginState.value = currentState.copy(isLoading = false)
                    _uiState.value = _uiState.value.copy(authState = AuthState.Initial)
                }
            }
        }
    }
    
    // === REGISTER ACTIONS ===
    fun onRegisterEmailChanged(email: String) {
        _registerState.value = _registerState.value.copy(
            email = email,
            emailError = null
        )
    }
    
    fun onRegisterPasswordChanged(password: String) {
        _registerState.value = _registerState.value.copy(
            password = password,
            passwordError = null
        )
    }
    
    fun onDisplayNameChanged(displayName: String) {
        _registerState.value = _registerState.value.copy(
            displayName = displayName,
            displayNameError = null
        )
    }
    
    fun onUserTypeChanged(userType: UserType) {
        _registerState.value = _registerState.value.copy(
            userType = userType
        )
    }
    
    fun onCompanyNameChanged(companyName: String) {
        _registerState.value = _registerState.value.copy(
            profile = _registerState.value.profile.copy(
                companyName = companyName
            ),
            companyNameError = null
        )
    }
    
    fun onBusinessTypeChanged(businessType: BusinessType) {
        _registerState.value = _registerState.value.copy(
            profile = _registerState.value.profile.copy(
                businessType = businessType
            )
        )
    }
    
    fun onTaxIdChanged(taxId: String) {
        _registerState.value = _registerState.value.copy(
            profile = _registerState.value.profile.copy(
                taxId = taxId
            ),
            taxIdError = null
        )
    }
    
    fun register() {
        val currentState = _registerState.value
        
        // Validaciones UI
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val displayNameError = validateDisplayName(currentState.displayName)
        val companyNameError = validateCompanyName(currentState.profile.companyName)
        val taxIdError = validateTaxId(currentState.profile.taxId)
        
        if (listOf(emailError, passwordError, displayNameError, companyNameError, taxIdError)
                .any { it != null }) {
            _registerState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError,
                displayNameError = displayNameError,
                companyNameError = companyNameError,
                taxIdError = taxIdError
            )
            return
        }
        
        viewModelScope.launch {
            _registerState.value = currentState.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(authState = AuthState.Loading)
            
            val result = registerUseCase(
                RegisterCredentials(
                    email = currentState.email,
                    password = currentState.password,
                    displayName = currentState.displayName,
                    userType = currentState.userType,
                    profile = currentState.profile
                )
            )
            
            when (result) {
                is Result.Success -> {
                    _registerState.value = RegisterState() // Reset state
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Success(result.data),
                        showEmailVerificationDialog = true
                    )
                }
                is Result.Error -> {
                    _registerState.value = currentState.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Error(result.exception.message ?: "Error desconocido")
                    )
                }
                else -> {
                    _registerState.value = currentState.copy(isLoading = false)
                    _uiState.value = _uiState.value.copy(authState = AuthState.Initial)
                }
            }
        }
    }
    
    // === LOGOUT ===
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = authRepository.logout()
            
            when (result) {
                is Result.Success -> {
                    // Reset all states
                    _loginState.value = LoginState()
                    _registerState.value = RegisterState()
                    _uiState.value = AuthUIState(authState = AuthState.Initial)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
    
    // === BIOMETRIC AUTH ===
    fun loginWithBiometric() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                authState = AuthState.Loading
            )
            
            val result = biometricAuthUseCase()
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Success(result.data),
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message,
                        authState = AuthState.Error(result.exception.message ?: "Error biométrico")
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authState = AuthState.Initial
                    )
                }
            }
        }
    }
    
    // === PASSWORD RESET ===
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        showPasswordResetDialog = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message
                    )
                }
                else -> {}
            }
        }
    }
    
    // === UI ACTIONS ===
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        _loginState.value = _loginState.value.copy(error = null)
        _registerState.value = _registerState.value.copy(error = null)
    }
    
    fun dismissEmailVerificationDialog() {
        _uiState.value = _uiState.value.copy(showEmailVerificationDialog = false)
    }
    
    fun dismissPasswordResetDialog() {
        _uiState.value = _uiState.value.copy(showPasswordResetDialog = false)
    }
    
    // === VALIDATIONS ===
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                "Email inválido"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }
    
    private fun validateDisplayName(displayName: String): String? {
        return when {
            displayName.isBlank() -> "El nombre es obligatorio"
            displayName.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }
    
    private fun validateCompanyName(companyName: String?): String? {
        return when {
            companyName.isNullOrBlank() -> "El nombre de la empresa es obligatorio"
            companyName.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }
    
    private fun validateTaxId(taxId: String?): String? {
        return when {
            taxId.isNullOrBlank() -> "El CUIT/RUT es obligatorio"
            taxId.replace("-", "").length !in 10..13 -> "CUIT/RUT inválido"
            else -> null
        }
    }
}

// === UI STATE MODELS ===
data class AuthUIState(
    val authState: AuthState = AuthState.Initial,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEmailVerificationDialog: Boolean = false,
    val showPasswordResetDialog: Boolean = false
)

data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",  
    val userType: UserType = UserType.CLIENT,
    val profile: UserProfile = UserProfile(
        companyName = null,              // ✅ Corregido: usar null en lugar de variable no definida
        businessType = null,             // ✅ Corregido: usar null en lugar de variable no definida
        taxId = null,                    // ✅ Corregido: usar null en lugar de variable no definida
        phoneNumber = null,              // ✅ Corregido: usar null en lugar de variable no definida
        whatsappNumber = null,           
        address = null,
        clothingCategories = emptyList(),
        businessHours = null,            
        socialMedia = null,              
        businessPhotos = emptyList(),
        description = null,              
        yearsInBusiness = null,          
        certifications = emptyList(),
        minimumOrderValue = null,
        deliveryOptions = emptyList()
    ),
    val emailError: String? = null,
    val passwordError: String? = null,
    val displayNameError: String? = null,
    val companyNameError: String? = null,
    val taxIdError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)