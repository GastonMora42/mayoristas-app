package com.mayoristas.feature.auth.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val userType: UserType,
    val isVerified: Boolean,
    val profile: UserProfile?,
    val createdAt: Long
)

enum class UserType {
    SELLER, CLIENT
}

data class UserProfile(
    val companyName: String?,
    val businessType: BusinessType?,
    val taxId: String?, // CUIT/RUT para LatAm
    val phoneNumber: String?,
    val address: Address?,
    val certifications: List<String> = emptyList()
)

enum class BusinessType {
    MANUFACTURER, DISTRIBUTOR, RETAILER, SERVICE_PROVIDER
}

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String
)

// Authentication Models
data class LoginCredentials(
    val email: String,
    val password: String
)

data class RegisterCredentials(
    val email: String,
    val password: String,
    val displayName: String,
    val userType: UserType,
    val profile: UserProfile
)

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    data object BiometricRequired : AuthState()
}