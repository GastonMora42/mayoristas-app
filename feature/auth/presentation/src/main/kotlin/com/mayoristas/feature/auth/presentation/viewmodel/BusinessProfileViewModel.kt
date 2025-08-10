// feature/auth/presentation/src/main/kotlin/com/mayoristas/feature/auth/presentation/viewmodel/BusinessProfileViewModel.kt

package com.mayoristas.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.core.common.dispatcher.DispatcherProvider
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    
    private val _profileState = MutableStateFlow(BusinessProfileState())
    val profileState: StateFlow<BusinessProfileState> = _profileState.asStateFlow()
    
    private val _uiState = MutableStateFlow(BusinessProfileUIState())
    val uiState: StateFlow<BusinessProfileUIState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUserProfile()
    }
    
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    result.data?.let { user ->
                        _profileState.value = _profileState.value.copy(
                            profile = user.profile ?: UserProfile(
                                companyName = null,
                                businessType = null,
                                taxId = null,
                                phoneNumber = null,
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
                            )
                        )
                        updateCompletionProgress()
                    }
                }
                is Result.Error -> {
                    _profileState.value = _profileState.value.copy(
                        error = result.exception.message
                    )
                }
                else -> {}
            }
        }
    }
    
    // === BUSINESS INFO ACTIONS ===
    fun onCompanyNameChanged(companyName: String) {
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(companyName = companyName.trim()),
            companyNameError = null
        )
        updateValidationAndProgress()
    }
    
    fun onBusinessTypeChanged(businessType: BusinessType) {
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(businessType = businessType)
        )
        updateValidationAndProgress()
    }
    
    fun onTaxIdChanged(taxId: String) {
        val cleanTaxId = taxId.replace(Regex("[^0-9\\-]"), "")
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(taxId = cleanTaxId),
            taxIdError = null
        )
        updateValidationAndProgress()
    }
    
    fun onPhoneNumberChanged(phoneNumber: String) {
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(phoneNumber = phoneNumber.trim())
        )
    }
    
    fun onWhatsAppNumberChanged(whatsappNumber: String) {
        val cleanNumber = whatsappNumber.replace(Regex("[^0-9\\+]"), "")
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(whatsappNumber = cleanNumber),
            whatsappError = null
        )
        updateValidationAndProgress()
    }
    
    fun onDescriptionChanged(description: String) {
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                description = if (description.isBlank()) null else description.trim()
            )
        )
        updateValidationAndProgress()
    }
    
    fun onYearsInBusinessChanged(years: Int?) {
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(yearsInBusiness = years)
        )
        updateValidationAndProgress()
    }
    
    // === ADDRESS ACTIONS ===
    fun onStreetChanged(street: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(street = street.trim())
            ),
            addressError = null
        )
        updateValidationAndProgress()
    }
    
    fun onStreetNumberChanged(streetNumber: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(
                    streetNumber = if (streetNumber.isBlank()) null else streetNumber.trim()
                )
            )
        )
        updateValidationAndProgress()
    }
    
    fun onNeighborhoodChanged(neighborhood: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(
                    neighborhood = if (neighborhood.isBlank()) null else neighborhood.trim()
                )
            )
        )
    }
    
    fun onCityChanged(city: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(city = city.trim())
            ),
            cityError = null
        )
        updateValidationAndProgress()
    }
    
    fun onStateChanged(state: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(state = state.trim())
            )
        )
        updateValidationAndProgress()
    }
    
    fun onPostalCodeChanged(postalCode: String) {
        val currentAddress = _profileState.value.profile.address ?: Address(
            street = "", streetNumber = null, neighborhood = null,
            city = "", state = "", country = "Argentina", postalCode = ""
        )
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                address = currentAddress.copy(postalCode = postalCode.trim())
            )
        )
    }
    
    // === CLOTHING CATEGORIES ACTIONS ===
    fun onClothingCategoryChanged(category: ClothingCategory, isSelected: Boolean) {
        val currentCategories = _profileState.value.profile.clothingCategories.toMutableList()
        
        if (isSelected && !currentCategories.contains(category)) {
            currentCategories.add(category)
        } else if (!isSelected && currentCategories.contains(category)) {
            currentCategories.remove(category)
        }
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                clothingCategories = currentCategories.toList()
            )
        )
        updateValidationAndProgress()
    }
    
    // === SOCIAL MEDIA ACTIONS ===
    fun onInstagramChanged(instagram: String) {
        val currentSocial = _profileState.value.profile.socialMedia ?: SocialMediaLinks()
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                socialMedia = currentSocial.copy(
                    instagram = if (instagram.isBlank()) null else instagram.trim()
                )
            )
        )
        updateValidationAndProgress()
    }
    
    fun onFacebookChanged(facebook: String) {
        val currentSocial = _profileState.value.profile.socialMedia ?: SocialMediaLinks()
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                socialMedia = currentSocial.copy(
                    facebook = if (facebook.isBlank()) null else facebook.trim()
                )
            )
        )
    }
    
    fun onWebsiteChanged(website: String) {
        val currentSocial = _profileState.value.profile.socialMedia ?: SocialMediaLinks()
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                socialMedia = currentSocial.copy(
                    website = if (website.isBlank()) null else website.trim()
                )
            )
        )
    }
    
    // === DELIVERY OPTIONS ACTIONS ===
    fun onDeliveryOptionChanged(option: DeliveryOption, isSelected: Boolean) {
        val currentOptions = _profileState.value.profile.deliveryOptions.toMutableList()
        
        if (isSelected && !currentOptions.contains(option)) {
            currentOptions.add(option)
        } else if (!isSelected && currentOptions.contains(option)) {
            currentOptions.remove(option)
        }
        
        _profileState.value = _profileState.value.copy(
            profile = _profileState.value.profile.copy(
                deliveryOptions = currentOptions.toList()
            )
        )
        updateValidationAndProgress()
    }
    
    // === VALIDATION AND SAVE ===
    fun saveProfile() {
        val currentState = _profileState.value
        
        // Validate required fields
        val validationErrors = validateProfile(currentState.profile)
        
        if (validationErrors.isNotEmpty()) {
            _profileState.value = currentState.copy(
                companyNameError = validationErrors["companyName"],
                taxIdError = validationErrors["taxId"],
                whatsappError = validationErrors["whatsapp"],
                addressError = validationErrors["address"],
                cityError = validationErrors["city"]
            )
            return
        }
        
        viewModelScope.launch {
            _profileState.value = currentState.copy(isSaving = true)
            
            withContext(dispatcherProvider.io) {
                when (val result = authRepository.updateProfile(currentState.profile)) {
                    is Result.Success -> {
                        _profileState.value = _profileState.value.copy(
                            isSaving = false
                        )
                        _uiState.value = _uiState.value.copy(
                            isCompleted = true
                        )
                    }
                    is Result.Error -> {
                        _profileState.value = _profileState.value.copy(
                            isSaving = false,
                            error = result.exception.message ?: "Error al guardar el perfil"
                        )
                    }
                    else -> {
                        _profileState.value = _profileState.value.copy(isSaving = false)
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }
    
    // === PRIVATE HELPER METHODS ===
    private fun updateValidationAndProgress() {
        updateValidation()
        updateCompletionProgress()
    }
    
    private fun updateValidation() {
        val profile = _profileState.value.profile
        val isValid = validateRequiredFields(profile)
        
        _profileState.value = _profileState.value.copy(
            isValidForSave = isValid
        )
    }
    
    private fun updateCompletionProgress() {
        val profile = _profileState.value.profile
        val progress = calculateCompletionProgress(profile)
        
        _uiState.value = _uiState.value.copy(
            completionProgress = progress
        )
    }
    
    private fun validateRequiredFields(profile: UserProfile): Boolean {
        return profile.companyName?.isNotBlank() == true &&
               profile.businessType != null &&
               profile.taxId?.isNotBlank() == true &&
               ProfileValidation.isValidCUIT(profile.taxId!!) &&
               profile.whatsappNumber?.isNotBlank() == true &&
               ProfileValidation.isValidWhatsAppNumber(profile.whatsappNumber!!) &&
               profile.address?.street?.isNotBlank() == true &&
               profile.address?.city?.isNotBlank() == true &&
               profile.address?.state?.isNotBlank() == true
    }
    
    private fun validateProfile(profile: UserProfile): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Company name validation
        if (profile.companyName.isNullOrBlank()) {
            errors["companyName"] = "El nombre del negocio es obligatorio"
        } else if (profile.companyName!!.length < 2) {
            errors["companyName"] = "El nombre debe tener al menos 2 caracteres"
        }
        
        // Tax ID validation
        if (profile.taxId.isNullOrBlank()) {
            errors["taxId"] = "El CUIT es obligatorio"
        } else if (!ProfileValidation.isValidCUIT(profile.taxId!!)) {
            errors["taxId"] = "CUIT inválido. Formato: 20-12345678-9"
        }
        
        // WhatsApp validation
        if (profile.whatsappNumber.isNullOrBlank()) {
            errors["whatsapp"] = "El número de WhatsApp es obligatorio"
        } else if (!ProfileValidation.isValidWhatsAppNumber(profile.whatsappNumber!!)) {
            errors["whatsapp"] = "Número de WhatsApp inválido"
        }
        
        // Address validation
        if (profile.address?.street.isNullOrBlank()) {
            errors["address"] = "La dirección es obligatoria"
        }
        
        if (profile.address?.city.isNullOrBlank()) {
            errors["city"] = "La ciudad es obligatoria"
        }
        
        return errors
    }
    
    private fun calculateCompletionProgress(profile: UserProfile): Float {
        var completedFields = 0
        val totalFields = 12 // Total fields we consider for completion
        
        // Required fields (weighted more)
        if (!profile.companyName.isNullOrBlank()) completedFields += 2
        if (profile.businessType != null) completedFields += 1
        if (!profile.taxId.isNullOrBlank() && ProfileValidation.isValidCUIT(profile.taxId!!)) completedFields += 2
        if (!profile.whatsappNumber.isNullOrBlank() && ProfileValidation.isValidWhatsAppNumber(profile.whatsappNumber!!)) completedFields += 2
        if (!profile.address?.street.isNullOrBlank()) completedFields += 1
        if (!profile.address?.city.isNullOrBlank()) completedFields += 1
        
        // Optional but important fields
        if (!profile.description.isNullOrBlank()) completedFields += 1
        if (profile.clothingCategories.isNotEmpty()) completedFields += 1
        if (profile.deliveryOptions.isNotEmpty()) completedFields += 1
        if (profile.businessPhotos.isNotEmpty()) completedFields += 1
        if (profile.socialMedia?.hasAnyLink() == true) completedFields += 1
        if (profile.yearsInBusiness != null && profile.yearsInBusiness!! > 0) completedFields += 1
        
        return (completedFields.toFloat() / totalFields.toFloat()).coerceIn(0f, 1f)
    }
}

// === STATE CLASSES ===
data class BusinessProfileState(
    val profile: UserProfile = UserProfile(
        companyName = null,
        businessType = null,
        taxId = null,
        phoneNumber = null,
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
    val isSaving: Boolean = false,
    val isValidForSave: Boolean = false,
    val error: String? = null,
    
    // Field-specific errors
    val companyNameError: String? = null,
    val taxIdError: String? = null,
    val whatsappError: String? = null,
    val addressError: String? = null,
    val cityError: String? = null
)

data class BusinessProfileUIState(
    val completionProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val showLocationPicker: Boolean = false
)

// Extension function for SocialMediaLinks
private fun SocialMediaLinks.hasAnyLink(): Boolean {
    return !instagram.isNullOrBlank() || 
           !facebook.isNullOrBlank() || 
           !website.isNullOrBlank() || 
           !linkedIn.isNullOrBlank()
}