// feature/products/presentation/src/main/kotlin/com/mayoristas/feature/products/presentation/viewmodel/AddEditProductViewModel.kt

package com.mayoristas.feature.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.core.common.dispatcher.DispatcherProvider
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.ClothingCategory
import com.mayoristas.feature.auth.domain.repository.AuthRepository
import com.mayoristas.feature.products.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    // TODO: Add ProductRepository when implemented
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    
    private val _productState = MutableStateFlow(AddEditProductState())
    val productState: StateFlow<AddEditProductState> = _productState.asStateFlow()
    
    private val _uiState = MutableStateFlow(AddEditProductUIState())
    val uiState: StateFlow<AddEditProductUIState> = _uiState.asStateFlow()
    
    private var currentUserId: String? = null
    
    init {
        loadCurrentUser()
        initializeEmptyProduct()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    currentUserId = result.data?.id
                    result.data?.let { user ->
                        initializeProductWithUserInfo(user)
                    }
                }
                else -> {
                    _productState.value = _productState.value.copy(
                        error = "Error al obtener información del usuario"
                    )
                }
            }
        }
    }
    
    private fun initializeEmptyProduct() {
        val emptyProduct = Product(
            id = "",
            sellerId = "",
            sellerInfo = SellerInfo(
                sellerId = "",
                companyName = "",
                displayName = "",
                whatsappNumber = "",
                location = ""
            ),
            basicInfo = ProductBasicInfo(
                name = "",
                description = "",
                shortDescription = null,
                category = ClothingCategory.WOMENS_CLOTHING,
                subcategory = null,
                brand = null,
                model = null,
                tags = emptyList()
            ),
            clothingDetails = ClothingDetails(
                gender = ClothingGender.UNISEX,
                season = Season.ALL_YEAR,
                materials = emptyList(),
                sizes = emptyList(),
                colors = emptyList(),
                ageGroup = null,
                style = null,
                occasion = emptyList(),
                careInstructions = emptyList(),
                countryOfOrigin = "Argentina",
                certifications = emptyList()
            ),
            pricingInfo = PricingInfo(
                basePrice = 0.0,
                currency = "ARS",
                minimumQuantity = 1,
                maximumQuantity = null,
                bulkPricing = emptyList(),
                pricePerUnit = "por unidad",
                includesVAT = true,
                paymentTerms = null,
                discountPercentage = null,
                originalPrice = null
            ),
            inventory = InventoryInfo(
                totalStock = 0,
                reservedStock = 0,
                availableStock = 0,
                lowStockThreshold = null,
                restockDate = null,
                trackInventory = true,
                allowBackorders = false,
                stockByVariant = emptyMap()
            ),
            images = emptyList(),
            status = ProductStatus.DRAFT,
            visibility = ProductVisibility.PUBLIC,
            analytics = ProductAnalytics(),
            seo = ProductSEO()
        )
        
        _productState.value = _productState.value.copy(
            product = emptyProduct
        )
    }
    
    private fun initializeProductWithUserInfo(user: com.mayoristas.feature.auth.domain.model.User) {
        val sellerInfo = SellerInfo(
            sellerId = user.id,
            companyName = user.profile?.companyName ?: "",
            displayName = user.displayName ?: "",
            whatsappNumber = user.profile?.whatsappNumber ?: "",
            location = "${user.profile?.address?.city ?: ""}, ${user.profile?.address?.state ?: ""}".trim(' ', ','),
            rating = 0.0f,
            profileImageUrl = user.profile?.businessPhotos?.firstOrNull()
        )
        
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                sellerId = user.id,
                sellerInfo = sellerInfo
            )
        )
        
        updateValidationAndProgress()
    }
    
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load product from repository
            // For now, simulate loading with mock data
            withContext(dispatcherProvider.io) {
                try {
                    // Simulate network delay
                    kotlinx.coroutines.delay(1000)
                    
                    // Mock loaded product (replace with actual repository call)
                    val loadedProduct = createMockProduct(productId)
                    
                    _productState.value = _productState.value.copy(
                        product = loadedProduct,
                        isEditMode = true
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    
                    updateValidationAndProgress()
                    
                } catch (e: Exception) {
                    _productState.value = _productState.value.copy(
                        error = "Error al cargar el producto: ${e.message}"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }
    
    // === BASIC INFO ACTIONS ===
    fun onNameChanged(name: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(name = name)
            )
        )
        clearError("name")
        updateValidationAndProgress()
    }
    
    fun onDescriptionChanged(description: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(description = description)
            )
        )
        clearError("description")
        updateValidationAndProgress()
    }
    
    fun onShortDescriptionChanged(shortDescription: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(
                    shortDescription = if (shortDescription.isBlank()) null else shortDescription
                )
            )
        )
    }
    
    fun onCategoryChanged(category: ClothingCategory) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(
                    category = category,
                    subcategory = null // Reset subcategory when category changes
                )
            )
        )
        clearError("category")
        updateValidationAndProgress()
    }
    
    fun onSubcategoryChanged(subcategory: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(subcategory = subcategory)
            )
        )
    }
    
    fun onBrandChanged(brand: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(
                    brand = if (brand.isBlank()) null else brand
                )
            )
        )
    }
    
    fun onModelChanged(model: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(
                    model = if (model.isBlank()) null else model
                )
            )
        )
    }
    
    fun onTagsChanged(tags: List<String>) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                basicInfo = _productState.value.product.basicInfo.copy(tags = tags)
            )
        )
    }
    
    // === CLOTHING DETAILS ACTIONS ===
    fun onGenderChanged(gender: ClothingGender) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                clothingDetails = _productState.value.product.clothingDetails.copy(gender = gender)
            )
        )
        updateValidationAndProgress()
    }
    
    fun onSeasonChanged(season: Season) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                clothingDetails = _productState.value.product.clothingDetails.copy(season = season)
            )
        )
        updateValidationAndProgress()
    }
    
    fun onSizeToggled(size: ClothingSize, isSelected: Boolean) {
        val currentSizes = _productState.value.product.clothingDetails.sizes.toMutableList()
        
        if (isSelected && !currentSizes.contains(size)) {
            currentSizes.add(size)
        } else if (!isSelected && currentSizes.contains(size)) {
            currentSizes.remove(size)
        }
        
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                clothingDetails = _productState.value.product.clothingDetails.copy(
                    sizes = currentSizes.sortedBy { it.sortOrder }
                )
            )
        )
        clearError("sizes")
        updateValidationAndProgress()
    }
    
    fun onColorToggled(color: ClothingColor, isSelected: Boolean) {
        val currentColors = _productState.value.product.clothingDetails.colors.toMutableList()
        
        if (isSelected && !currentColors.contains(color)) {
            currentColors.add(color)
        } else if (!isSelected && currentColors.contains(color)) {
            currentColors.remove(color)
        }
        
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                clothingDetails = _productState.value.product.clothingDetails.copy(colors = currentColors)
            )
        )
        clearError("colors")
        updateValidationAndProgress()
    }
    
    fun onMaterialToggled(material: ClothingMaterial, isSelected: Boolean) {
        val currentMaterials = _productState.value.product.clothingDetails.materials.toMutableList()
        
        if (isSelected && !currentMaterials.contains(material)) {
            currentMaterials.add(material)
        } else if (!isSelected && currentMaterials.contains(material)) {
            currentMaterials.remove(material)
        }
        
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                clothingDetails = _productState.value.product.clothingDetails.copy(materials = currentMaterials)
            )
        )
        clearError("materials")
        updateValidationAndProgress()
    }
    
    // === PRICING ACTIONS ===
    fun onBasePriceChanged(price: Double) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                pricingInfo = _productState.value.product.pricingInfo.copy(basePrice = price)
            )
        )
        clearError("basePrice")
        updateValidationAndProgress()
    }
    
    fun onMinimumQuantityChanged(quantity: Int) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                pricingInfo = _productState.value.product.pricingInfo.copy(minimumQuantity = quantity)
            )
        )
        clearError("minimumQuantity")
        updateValidationAndProgress()
    }
    
    fun onPricePerUnitChanged(unit: String) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                pricingInfo = _productState.value.product.pricingInfo.copy(pricePerUnit = unit)
            )
        )
    }
    
    fun addBulkPriceRule(rule: BulkPriceRule) {
        val currentRules = _productState.value.product.pricingInfo.bulkPricing.toMutableList()
        currentRules.add(rule)
        currentRules.sortBy { it.minimumQuantity }
        
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                pricingInfo = _productState.value.product.pricingInfo.copy(bulkPricing = currentRules)
            )
        )
        updateValidationAndProgress()
    }
    
    fun removeBulkPriceRule(index: Int) {
        val currentRules = _productState.value.product.pricingInfo.bulkPricing.toMutableList()
        if (index in currentRules.indices) {
            currentRules.removeAt(index)
            
            _productState.value = _productState.value.copy(
                product = _productState.value.product.copy(
                    pricingInfo = _productState.value.product.pricingInfo.copy(bulkPricing = currentRules)
                )
            )
        }
    }
    
    fun clearBulkPricing() {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                pricingInfo = _productState.value.product.pricingInfo.copy(bulkPricing = emptyList())
            )
        )
    }
    
    // === INVENTORY ACTIONS ===
    fun onTotalStockChanged(stock: Int) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                inventory = _productState.value.product.inventory.copy(
                    totalStock = stock,
                    availableStock = stock - _productState.value.product.inventory.reservedStock
                )
            )
        )
        clearError("totalStock")
        updateValidationAndProgress()
    }
    
    fun onLowStockThresholdChanged(threshold: Int?) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                inventory = _productState.value.product.inventory.copy(lowStockThreshold = threshold)
            )
        )
    }
    
    fun onTrackInventoryChanged(track: Boolean) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                inventory = _productState.value.product.inventory.copy(trackInventory = track)
            )
        )
        updateValidationAndProgress()
    }
    
    fun onAllowBackordersChanged(allow: Boolean) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(
                inventory = _productState.value.product.inventory.copy(allowBackorders = allow)
            )
        )
    }
    
    // === IMAGE ACTIONS ===
    fun addImage() {
        // TODO: Implement image picker
        // For now, add a mock image URL
        val currentImages = _productState.value.product.images.toMutableList()
        if (currentImages.size < 10) {
            currentImages.add("https://via.placeholder.com/400x400?text=Producto")
            
            _productState.value = _productState.value.copy(
                product = _productState.value.product.copy(images = currentImages)
            )
            clearError("images")
            updateValidationAndProgress()
        }
    }
    
    fun removeImage(index: Int) {
        val currentImages = _productState.value.product.images.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            
            _productState.value = _productState.value.copy(
                product = _productState.value.product.copy(images = currentImages)
            )
            updateValidationAndProgress()
        }
    }
    
    fun reorderImages(fromIndex: Int, toIndex: Int) {
        val currentImages = _productState.value.product.images.toMutableList()
        if (fromIndex in currentImages.indices && toIndex in currentImages.indices) {
            val item = currentImages.removeAt(fromIndex)
            currentImages.add(toIndex, item)
            
            _productState.value = _productState.value.copy(
                product = _productState.value.product.copy(images = currentImages)
            )
        }
    }
    
    // === ADVANCED OPTIONS ===
    fun onStatusChanged(status: ProductStatus) {
        _productState.value = _productState.value.copy(
            product = _productState.value.product.copy(status = status)
        )
    }
    
    // === VALIDATION AND SAVE ===
    fun saveProduct() {
        val currentState = _productState.value
        
        // Validate product
        val errors = validateProductForSave(currentState.product)
        
        if (errors.isNotEmpty()) {
            _productState.value = currentState.copy(
                basicInfoErrors = errors.filterKeys { it in listOf("name", "description", "category") },
                clothingDetailsErrors = errors.filterKeys { it in listOf("sizes", "colors", "materials") },
                pricingErrors = errors.filterKeys { it in listOf("basePrice", "minimumQuantity") },
                inventoryErrors = errors.filterKeys { it in listOf("totalStock") },
                imagesError = errors["images"]
            )
            return
        }
        
        viewModelScope.launch {
            _productState.value = currentState.copy(isSaving = true)
            
            try {
                withContext(dispatcherProvider.io) {
                    // TODO: Save to repository
                    // Simulate saving
                    kotlinx.coroutines.delay(2000)
                    
                    val productId = if (currentState.isEditMode) {
                        currentState.product.id
                    } else {
                        UUID.randomUUID().toString()
                    }
                    
                    _productState.value = _productState.value.copy(
                        isSaving = false
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        showSuccessDialog = true,
                        savedProductId = productId
                    )
                }
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(
                    isSaving = false,
                    error = "Error al guardar el producto: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _productState.value = _productState.value.copy(error = null)
    }
    
    private fun clearError(field: String) {
        val currentState = _productState.value
        
        _productState.value = currentState.copy(
            basicInfoErrors = currentState.basicInfoErrors - field,
            clothingDetailsErrors = currentState.clothingDetailsErrors - field,
            pricingErrors = currentState.pricingErrors - field,
            inventoryErrors = currentState.inventoryErrors - field,
            imagesError = if (field == "images") null else currentState.imagesError
        )
    }
    
    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }
    
    // === PRIVATE HELPER METHODS ===
    private fun updateValidationAndProgress() {
        updateValidation()
        updateCompletionProgress()
    }
    
    private fun updateValidation() {
        val product = _productState.value.product
        val isValid = validateProductForSave(product).isEmpty()
        
        _productState.value = _productState.value.copy(
            isValidForSave = isValid
        )
    }
    
    private fun updateCompletionProgress() {
        val product = _productState.value.product
        val progress = calculateCompletionProgress(product)
        
        _uiState.value = _uiState.value.copy(
            completionProgress = progress
        )
    }
    
    private fun validateProductForSave(product: Product): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Basic info validation
        if (product.basicInfo.name.isBlank()) {
            errors["name"] = "El nombre del producto es obligatorio"
        } else if (product.basicInfo.name.length < 5) {
            errors["name"] = "El nombre debe tener al menos 5 caracteres"
        }
        
        if (product.basicInfo.description.isBlank()) {
            errors["description"] = "La descripción es obligatoria"
        } else if (product.basicInfo.description.length < 20) {
            errors["description"] = "La descripción debe tener al menos 20 caracteres"
        }
        
        // Clothing details validation
        if (product.clothingDetails.sizes.isEmpty()) {
            errors["sizes"] = "Debe seleccionar al menos un talle"
        }
        
        if (product.clothingDetails.colors.isEmpty()) {
            errors["colors"] = "Debe seleccionar al menos un color"
        }
        
        if (product.clothingDetails.materials.isEmpty()) {
            errors["materials"] = "Debe seleccionar al menos un material"
        }
        
        // Pricing validation
        if (product.pricingInfo.basePrice <= 0) {
            errors["basePrice"] = "El precio debe ser mayor a 0"
        }
        
        if (product.pricingInfo.minimumQuantity < 1) {
            errors["minimumQuantity"] = "La cantidad mínima debe ser al menos 1"
        }
        
        // Inventory validation
        if (product.inventory.totalStock < 0) {
            errors["totalStock"] = "El stock no puede ser negativo"
        }
        
        // Images validation
        if (product.images.isEmpty()) {
            errors["images"] = "Debe agregar al menos una imagen del producto"
        }
        
        return errors
    }
    
    private fun calculateCompletionProgress(product: Product): Float {
        var completedFields = 0
        val totalFields = 12
        
        // Required fields
        if (product.basicInfo.name.isNotBlank()) completedFields += 2
        if (product.basicInfo.description.length >= 20) completedFields += 2
        if (product.clothingDetails.sizes.isNotEmpty()) completedFields += 1
        if (product.clothingDetails.colors.isNotEmpty()) completedFields += 1
        if (product.clothingDetails.materials.isNotEmpty()) completedFields += 1
        if (product.pricingInfo.basePrice > 0) completedFields += 2
        if (product.pricingInfo.minimumQuantity >= 1) completedFields += 1
        if (product.inventory.totalStock >= 0) completedFields += 1
        if (product.images.isNotEmpty()) completedFields += 1
        
        return (completedFields.toFloat() / totalFields.toFloat()).coerceIn(0f, 1f)
    }
    
    private fun createMockProduct(productId: String): Product {
        return Product(
            id = productId,
            sellerId = currentUserId ?: "",
            sellerInfo = _productState.value.product.sellerInfo,
            basicInfo = ProductBasicInfo(
                name = "Remera Básica Algodón",
                description = "Remera básica de algodón 100%, ideal para uso diario. Corte regular, cuello redondo. Perfecta para combinar con jeans o pantalones casuales.",
                shortDescription = "Remera básica de algodón 100%",
                category = ClothingCategory.WOMENS_CLOTHING,
                subcategory = "Remeras",
                brand = "BasicWear",
                model = "Classic",
                tags = listOf("básica", "algodón", "casual")
            ),
            clothingDetails = ClothingDetails(
                gender = ClothingGender.UNISEX,
                season = Season.ALL_YEAR,
                materials = listOf(ClothingMaterial.COTTON),
                sizes = listOf(ClothingSize.S, ClothingSize.M, ClothingSize.L, ClothingSize.XL),
                colors = listOf(ClothingColor.WHITE, ClothingColor.BLACK, ClothingColor.NAVY),
                ageGroup = AgeGroup.ADULT,
                style = ClothingStyle.CASUAL,
                occasion = listOf(ClothingOccasion.DAILY, ClothingOccasion.CASUAL_EVENT),
                careInstructions = listOf(
                    CareInstruction.MACHINE_WASH_COLD,
                    CareInstruction.TUMBLE_DRY_LOW,
                    CareInstruction.IRON_MEDIUM
                ),
                countryOfOrigin = "Argentina"
            ),
            pricingInfo = PricingInfo(
                basePrice = 2500.0,
                currency = "ARS",
                minimumQuantity = 12,
                maximumQuantity = null,
                bulkPricing = listOf(
                    BulkPriceRule(24, 2200.0, 12.0),
                    BulkPriceRule(50, 2000.0, 20.0)
                ),
                pricePerUnit = "por unidad",
                includesVAT = true
            ),
            inventory = InventoryInfo(
                totalStock = 150,
                reservedStock = 0,
                availableStock = 150,
                lowStockThreshold = 20,
                trackInventory = true,
                allowBackorders = false
            ),
            images = listOf(
                "https://via.placeholder.com/400x400?text=Remera+1",
                "https://via.placeholder.com/400x400?text=Remera+2",
                "https://via.placeholder.com/400x400?text=Remera+3"
            ),
            status = ProductStatus.ACTIVE,
            visibility = ProductVisibility.PUBLIC
        )
    }
}

// === STATE DATA CLASSES ===
data class AddEditProductState(
    val product: Product = Product(
        id = "",
        sellerId = "",
        sellerInfo = SellerInfo("", "", "", "", ""),
        basicInfo = ProductBasicInfo("", "", null, ClothingCategory.WOMENS_CLOTHING),
        clothingDetails = ClothingDetails(
            ClothingGender.UNISEX, Season.ALL_YEAR, emptyList(), emptyList(), emptyList()
        ),
        pricingInfo = PricingInfo(0.0, "ARS", 1),
        inventory = InventoryInfo(0)
    ),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isValidForSave: Boolean = false,
    val error: String? = null,
    
    // Section-specific errors
    val basicInfoErrors: Map<String, String> = emptyMap(),
    val clothingDetailsErrors: Map<String, String> = emptyMap(),
    val pricingErrors: Map<String, String> = emptyMap(),
    val inventoryErrors: Map<String, String> = emptyMap(),
    val imagesError: String? = null
)

data class AddEditProductUIState(
    val isLoading: Boolean = false,
    val completionProgress: Float = 0f,
    val showSuccessDialog: Boolean = false,
    val savedProductId: String? = null
)