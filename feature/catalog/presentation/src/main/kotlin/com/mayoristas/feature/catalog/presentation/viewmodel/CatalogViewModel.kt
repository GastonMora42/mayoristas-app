// feature/catalog/presentation/src/main/kotlin/com/mayoristas/feature/catalog/presentation/viewmodel/CatalogViewModel.kt

package com.mayoristas.feature.catalog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.core.common.dispatcher.DispatcherProvider
import com.mayoristas.core.common.result.Result
import com.mayoristas.feature.auth.domain.model.ClothingCategory
import com.mayoristas.feature.products.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    // TODO: Add ProductRepository, FavoritesRepository
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    
    private val _catalogState = MutableStateFlow(CatalogState())
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()
    
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private val _uiState = MutableStateFlow(CatalogUIState())
    val uiState: StateFlow<CatalogUIState> = _uiState.asStateFlow()
    
    init {
        loadFeaturedProducts()
    }
    
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            withContext(dispatcherProvider.io) {
                try {
                    // Simulate loading featured products
                    kotlinx.coroutines.delay(1000)
                    val featuredProducts = generateMockFeaturedProducts()
                    
                    _catalogState.value = _catalogState.value.copy(
                        featuredProducts = featuredProducts
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar productos"
                    )
                }
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        
        if (query.isNotEmpty()) {
            performSearchDebounced(query)
        } else {
            _catalogState.value = _catalogState.value.copy(searchResults = emptyList())
        }
    }
    
    private fun performSearchDebounced(query: String) {
        // Simple debounce implementation
        viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Wait for user to stop typing
            if (_searchState.value.query == query) {
                performSearch()
            }
        }
    }
    
    fun performSearch() {
        val query = _searchState.value.query
        if (query.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            withContext(dispatcherProvider.io) {
                try {
                    kotlinx.coroutines.delay(500) // Simulate API call
                    
                    val filters = _searchState.value.filters
                    val allProducts = generateMockSearchResults(query)
                    val filteredProducts = applyFiltersToProducts(allProducts, filters)
                    
                    _catalogState.value = _catalogState.value.copy(
                        searchResults = filteredProducts
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error en la búsqueda"
                    )
                }
            }
        }
    }
    
    fun selectCategory(category: ClothingCategory) {
        val currentFilters = _searchState.value.filters
        val newCategories = if (currentFilters.categories.contains(category)) {
            currentFilters.categories - category
        } else {
            listOf(category) // Replace with single selection
        }
        
        val newFilters = currentFilters.copy(categories = newCategories)
        _searchState.value = _searchState.value.copy(
            filters = newFilters,
            query = category.displayName
        )
        
        performSearch()
    }
    
    fun updateFilters(newFilters: ProductFilter) {
        _searchState.value = _searchState.value.copy(filters = newFilters)
    }
    
    fun applyFilters() {
        performSearch()
    }
    
    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement favorites repository
                val currentFavorites = _catalogState.value.favoriteProductIds.toMutableSet()
                
                if (currentFavorites.contains(productId)) {
                    currentFavorites.remove(productId)
                } else {
                    currentFavorites.add(productId)
                }
                
                _catalogState.value = _catalogState.value.copy(
                    favoriteProductIds = currentFavorites
                )
                
                // TODO: Save to backend
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar favoritos: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // === PRIVATE HELPER METHODS ===
    
    private fun applyFiltersToProducts(products: List<Product>, filters: ProductFilter): List<Product> {
        return products.filter { product ->
            // Category filter
            if (filters.categories.isNotEmpty() && !filters.categories.contains(product.basicInfo.category)) {
                return@filter false
            }
            
            // Gender filter
            if (filters.genders.isNotEmpty() && !filters.genders.contains(product.clothingDetails.gender)) {
                return@filter false
            }
            
            // Price range filter
            filters.priceRange?.let { range ->
                if (product.pricingInfo.basePrice < range.min || product.pricingInfo.basePrice > range.max) {
                    return@filter false
                }
            }
            
            // Size filter
            if (filters.sizes.isNotEmpty()) {
                val hasCommonSize = filters.sizes.any { size -> product.clothingDetails.sizes.contains(size) }
                if (!hasCommonSize) return@filter false
            }
            
            // Color filter
            if (filters.colors.isNotEmpty()) {
                val hasCommonColor = filters.colors.any { color -> product.clothingDetails.colors.contains(color) }
                if (!hasCommonColor) return@filter false
            }
            
            // Material filter
            if (filters.materials.isNotEmpty()) {
                val hasCommonMaterial = filters.materials.any { material -> product.clothingDetails.materials.contains(material) }
                if (!hasCommonMaterial) return@filter false
            }
            
            // In stock filter
            if (filters.inStock == true && !product.isAvailable()) {
                return@filter false
            }
            
            true
        }.sortedWith(getSortComparator(filters.sortBy))
    }
    
    private fun getSortComparator(sortBy: ProductSortBy): Comparator<Product> {
        return when (sortBy) {
            ProductSortBy.PRICE_LOW_HIGH -> compareBy { it.pricingInfo.basePrice }
            ProductSortBy.PRICE_HIGH_LOW -> compareByDescending { it.pricingInfo.basePrice }
            ProductSortBy.NAME_A_Z -> compareBy { it.basicInfo.name }
            ProductSortBy.NAME_Z_A -> compareByDescending { it.basicInfo.name }
            ProductSortBy.NEWEST -> compareByDescending { it.createdAt }
            ProductSortBy.OLDEST -> compareBy { it.createdAt }
            ProductSortBy.MOST_VIEWED -> compareByDescending { it.analytics.views }
            ProductSortBy.MOST_FAVORITED -> compareByDescending { it.analytics.favorites }
            else -> compareBy { it.basicInfo.name } // Default to name
        }
    }
    
    // === MOCK DATA GENERATORS ===
    
    private fun generateMockFeaturedProducts(): List<Product> {
        return listOf(
            createMockProduct(
                id = "featured_1",
                name = "Remera Básica Premium",
                price = 2500.0,
                category = ClothingCategory.WOMENS_CLOTHING,
                sellerName = "Textiles del Norte"
            ),
            createMockProduct(
                id = "featured_2",
                name = "Jean Clásico Azul",
                price = 8900.0,
                category = ClothingCategory.MENS_CLOTHING,
                sellerName = "Denim Factory"
            ),
            createMockProduct(
                id = "featured_3",
                name = "Vestido Casual Verano",
                price = 6500.0,
                category = ClothingCategory.WOMENS_CLOTHING,
                sellerName = "Boutique Mayorista"
            ),
            createMockProduct(
                id = "featured_4",
                name = "Zapatillas Deportivas",
                price = 15000.0,
                category = ClothingCategory.FOOTWEAR,
                sellerName = "Calzados Premium"
            ),
            createMockProduct(
                id = "featured_5",
                name = "Campera Abrigo Invierno",
                price = 12000.0,
                category = ClothingCategory.MENS_CLOTHING,
                sellerName = "Abrigos del Sur"
            )
        )
    }
    
    private fun generateMockSearchResults(query: String): List<Product> {
        val baseProducts = generateMockFeaturedProducts()
        
        // Generate additional products based on search query
        val additionalProducts = (1..15).map { index ->
            createMockProduct(
                id = "search_$index",
                name = "Producto $query $index",
                price = (1000..20000).random().toDouble(),
                category = ClothingCategory.values().random(),
                sellerName = "Vendedor ${('A'..'Z').random()}"
            )
        }
        
        return baseProducts + additionalProducts
    }
    
    private fun createMockProduct(
        id: String,
        name: String,
        price: Double,
        category: ClothingCategory,
        sellerName: String
    ): Product {
        return Product(
            id = id,
            sellerId = "seller_${id.hashCode()}",
            sellerInfo = SellerInfo(
                sellerId = "seller_${id.hashCode()}",
                companyName = sellerName,
                displayName = sellerName,
                whatsappNumber = "+5491123456789",
                location = "Buenos Aires, Argentina",
                rating = (4.0f..5.0f).random(),
                profileImageUrl = null
            ),
            basicInfo = ProductBasicInfo(
                name = name,
                description = "Descripción detallada del producto $name. Excelente calidad, materiales premium.",
                shortDescription = "Descripción corta de $name",
                category = category,
                subcategory = category.subcategories.randomOrNull(),
                brand = "Marca ${('A'..'Z').random()}",
                model = "Modelo ${(1..100).random()}",
                tags = listOf("calidad", "premium", "mayorista")
            ),
            clothingDetails = ClothingDetails(
                gender = ClothingGender.values().random(),
                season = Season.values().random(),
                materials = ClothingMaterial.values().take((1..3).random()),
                sizes = ClothingSize.values().take((2..6).random()),
                colors = ClothingColor.values().take((1..4).random()),
                ageGroup = AgeGroup.ADULT,
                style = ClothingStyle.values().random(),
                occasion = ClothingOccasion.values().take((1..3).random()),
                careInstructions = CareInstruction.values().take((2..4).random()),
                countryOfOrigin = "Argentina"
            ),
            pricingInfo = PricingInfo(
                basePrice = price,
                currency = "ARS",
                minimumQuantity = (6..50).random(),
                bulkPricing = listOf(
                    BulkPriceRule((12..24).random(), price * 0.9, 10.0),
                    BulkPriceRule((50..100).random(), price * 0.8, 20.0)
                ),
                pricePerUnit = "por unidad",
                includesVAT = true
            ),
            inventory = InventoryInfo(
                totalStock = (10..500).random(),
                reservedStock = 0,
                availableStock = (10..500).random(),
                lowStockThreshold = 10,
                trackInventory = true,
                allowBackorders = false
            ),
            images = listOf(
                "https://via.placeholder.com/400x400?text=${name.replace(" ", "+")}",
                "https://via.placeholder.com/400x400?text=${name.replace(" ", "+")}_2"
            ),
            status = ProductStatus.ACTIVE,
            visibility = ProductVisibility.PUBLIC,
            analytics = ProductAnalytics(
                views = (10..1000).random(),
                uniqueViews = (5..500).random(),
                favorites = (0..100).random(),
                whatsappClicks = (0..50).random()
            ),
            createdAt = System.currentTimeMillis() - (0..30L).random() * 24 * 60 * 60 * 1000,
            updatedAt = System.currentTimeMillis()
        )
    }
}

// === STATE DATA CLASSES ===

data class CatalogState(
    val featuredProducts: List<Product> = emptyList(),
    val searchResults: List<Product> = emptyList(),
    val favoriteProductIds: Set<String> = emptySet(),
    val categories: List<ClothingCategory> = ClothingCategory.values().toList()
)

data class SearchState(
    val query: String = "",
    val filters: ProductFilter = ProductFilter(),
    val isSearchActive: Boolean = false
) {
    val activeFiltersCount: Int
        get() = filters.categories.size + 
                filters.genders.size + 
                filters.sizes.size + 
                filters.colors.size + 
                filters.materials.size +
                (if (filters.priceRange != null) 1 else 0) +
                (if (filters.inStock == true) 1 else 0)
}

data class CatalogUIState(
    val isLoading: Boolean = false,
    val error: String? = null
)

// === BUYER PRODUCT DETAIL SCREEN ===

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProductDetailScreen(
    productId: String,
    sellerId: String,
    buyer: User,
    onNavigateBack: () -> Unit,
    onNavigateToSellerProfile: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val productState by viewModel.productState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }
    
    when {
        uiState.isLoading -> {
            LoadingProductDetail()
        }
        
        productState.product != null -> {
            ProductDetailContent(
                product = productState.product!!,
                buyer = buyer,
                onNavigateBack = onNavigateBack,
                onNavigateToSellerProfile = onNavigateToSellerProfile,
                onFavoriteToggle = { viewModel.toggleFavorite(productId) },
                isFavorite = productState.isFavorite
            )
        }
        
        uiState.error != null -> {
            ErrorProductDetail(
                error = uiState.error!!,
                onRetry = { viewModel.loadProduct(productId) },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
    product: Product,
    buyer: User,
    onNavigateBack: () -> Unit,
    onNavigateToSellerProfile: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isFavorite: Boolean
) {
    var selectedImageIndex by remember { mutableStateOf(0) }
    var showCustomMessageDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Detalle del Producto") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE91E63) else LocalContentColor.current
                    )
                }
                
                IconButton(onClick = { /* TODO: Share product */ }) {
                    Icon(Icons.Default.Share, "Compartir")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Product Images
            item {
                ProductImageCarousel(
                    images = product.images,
                    selectedIndex = selectedImageIndex,
                    onImageSelected = { selectedImageIndex = it }
                )
            }
            
            // Basic Info
            item {
                ProductBasicInfoSection(product)
            }
            
            // Seller Info Card
            item {
                SellerInfoCard(
                    seller = product.sellerInfo,
                    onSellerClick = onNavigateToSellerProfile
                )
            }
            
            // Pricing Info
            item {
                PricingInfoSection(product.pricingInfo)
            }
            
            // Product Details
            item {
                ProductDetailsSection(product.clothingDetails)
            }
            
            // Description
            item {
                ProductDescriptionSection(product.basicInfo)
            }
        }
        
        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Price summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            product.getDisplayPrice(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Mínimo ${product.pricingInfo.minimumQuantity} ${product.pricingInfo.pricePerUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Custom message button
                    OutlinedButton(
                        onClick = { showCustomMessageDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mensaje personalizado")
                    }
                    
                    // Main WhatsApp button
                    WhatsAppContactButton(
                        product = product,
                        buyer = buyer,
                        variant = WhatsAppButtonVariant.PRIMARY,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }
    
    // Custom Message Dialog
    if (showCustomMessageDialog) {
        CustomMessageDialog(
            product = product,
            buyer = buyer,
            onDismiss = { showCustomMessageDialog = false }
        )
    }
}

@Composable
private fun ProductImageCarousel(
    images: List<String>,
    selectedIndex: Int,
    onImageSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = images.getOrNull(selectedIndex) ?: images.firstOrNull(),
                contentDescription = "Imagen del producto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Thumbnail row
        if (images.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images.size) { index ->
                    Card(
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { onImageSelected(index) },
                        border = if (index == selectedIndex) 
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else null
                    ) {
                        AsyncImage(
                            model = images[index],
                            contentDescription = "Miniatura ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductBasicInfoSection(product: Product) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            product.basicInfo.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (product.basicInfo.brand != null) {
            Text(
                "Marca: ${product.basicInfo.brand}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Category and subcategory
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { },
                label = { Text(product.basicInfo.category.displayName) }
            )
            
            if (product.basicInfo.subcategory != null) {
                AssistChip(
                    onClick = { },
                    label = { Text(product.basicInfo.subcategory!!) }
                )
            }
        }
        
        // Tags
        if (product.basicInfo.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(product.basicInfo.tags) { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerInfoCard(
    seller: SellerInfo,
    onSellerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSellerClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seller avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Seller info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    seller.companyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    seller.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (seller.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            seller.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Arrow icon
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Ver perfil",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PricingInfoSection(pricingInfo: PricingInfo) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Información de Precios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Base price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Precio base:")
                Text(
                    "$${pricingInfo.basePrice.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Minimum quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cantidad mínima:")
                Text(
                    "${pricingInfo.minimumQuantity} ${pricingInfo.pricePerUnit}",
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Bulk pricing
            if (pricingInfo.bulkPricing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Descuentos por volumen:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                pricingInfo.bulkPricing.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Desde ${rule.minimumQuantity} unidades:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "$${rule.pricePerUnit.toInt()} (-${rule.discountPercentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsSection(clothingDetails: ClothingDetails) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Detalles del Producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Gender and season
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Género:", clothingDetails.gender.displayName)
                DetailItem("Temporada:", clothingDetails.season.displayName)
            }
            
            // Available sizes
            if (clothingDetails.sizes.isNotEmpty()) {
                Column {
                    Text(
                        "Talles disponibles:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(clothingDetails.sizes.sortedBy { it.sortOrder }) { size ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(size.displayName) }
                            )
                        }
                    }
                }
            }
            
            // Available colors
            if (clothingDetails.colors.isNotEmpty()) {
                Column {
                    Text(
                        "Colores disponibles:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(clothingDetails.colors) { color ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(color.hexColor)),
                                            CircleShape
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Text(
                                    color.displayName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Materials
            if (clothingDetails.materials.isNotEmpty()) {
                DetailItem(
                    "Materiales:",
                    clothingDetails.materials.joinToString { it.displayName }
                )
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProductDescriptionSection(basicInfo: ProductBasicInfo) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Descripción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                basicInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomMessageDialog(
    product: Product,
    buyer: User,
    onDismiss: () -> Unit
) {
    var customMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mensaje Personalizado") },
        text = {
            Column {
                Text(
                    "Escribí un mensaje personalizado para el vendedor:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Tu mensaje") },
                    placeholder = { Text("¿Podrías darme más información sobre...?") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            WhatsAppContactButton(
                product = product,
                buyer = buyer,
                customMessage = customMessage.ifEmpty { null },
                variant = WhatsAppButtonVariant.OUTLINE,
                modifier = Modifier
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun LoadingProductDetail() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorProductDetail(
    error: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Error al cargar producto",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(error)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Volver")
                }
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}

// === PRODUCT DETAIL VIEWMODEL ===

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    
    private val _productState = MutableStateFlow(ProductDetailState())
    val productState: StateFlow<ProductDetailState> = _productState.asStateFlow()
    
    private val _uiState = MutableStateFlow(ProductDetailUIState())
    val uiState: StateFlow<ProductDetailUIState> = _uiState.asStateFlow()
    
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            withContext(dispatcherProvider.io) {
                try {
                    kotlinx.coroutines.delay(1000) // Simulate API call
                    
                    // TODO: Load from repository
                    val product = createMockProduct(productId)
                    
                    _productState.value = _productState.value.copy(
                        product = product,
                        isFavorite = false // TODO: Load from favorites
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar producto"
                    )
                }
            }
        }
    }
    
    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                val newFavoriteState = !_productState.value.isFavorite
                _productState.value = _productState.value.copy(isFavorite = newFavoriteState)
                
                // TODO: Save to backend
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar favoritos"
                )
            }
        }
    }
    
    private fun createMockProduct(productId: String): Product {
        // Implementation similar to other mock product creators
        return Product(
            id = productId,
            sellerId = "seller_123",
            sellerInfo = SellerInfo(
                sellerId = "seller_123",
                companyName = "Textiles Premium SA",
                displayName = "Textiles Premium",
                whatsappNumber = "+5491123456789",
                location = "Buenos Aires, Argentina",
                rating = 4.8f
            ),
            basicInfo = ProductBasicInfo(
                name = "Remera Premium Algodón Pima",
                description = """
                    Remera de algodón Pima 100%, ideal para uso diario o deportivo. 
                    Confeccionada con materiales de primera calidad que garantizan durabilidad y confort.
                    
                    Características principales:
                    • Algodón Pima peruano de fibra larga
                    • Costuras reforzadas
                    • Preencogido para evitar deformación
                    • Cuello con tapacostura
                    • Disponible en múltiples colores
                """.trimIndent(),
                category = ClothingCategory.WOMENS_CLOTHING,
                brand = "Premium Cotton",
                tags = listOf("algodón", "premium", "básica", "unisex")
            ),
            clothingDetails = ClothingDetails(
                gender = ClothingGender.UNISEX,
                season = Season.ALL_YEAR,
                materials = listOf(ClothingMaterial.COTTON),
                sizes = listOf(ClothingSize.S, ClothingSize.M, ClothingSize.L, ClothingSize.XL, ClothingSize.XXL),
                colors = listOf(
                    ClothingColor.WHITE, ClothingColor.BLACK, ClothingColor.NAVY, 
                    ClothingColor.GRAY, ClothingColor.RED
                ),
                style = ClothingStyle.CASUAL,
                careInstructions = listOf(
                    CareInstruction.MACHINE_WASH_COLD,
                    CareInstruction.TUMBLE_DRY_LOW,
                    CareInstruction.IRON_MEDIUM
                )
            ),
            pricingInfo = PricingInfo(
                basePrice = 2500.0,
                currency = "ARS",
                minimumQuantity = 12,
                bulkPricing = listOf(
                    BulkPriceRule(24, 2200.0, 12.0),
                    BulkPriceRule(50, 2000.0, 20.0),
                    BulkPriceRule(100, 1800.0, 28.0)
                ),
                pricePerUnit = "por unidad",
                includesVAT = true
            ),
            inventory = InventoryInfo(
                totalStock = 250,
                availableStock = 250,
                trackInventory = true
            ),
            images = listOf(
                "https://via.placeholder.com/400x400?text=Remera+Premium+1",
                "https://via.placeholder.com/400x400?text=Remera+Premium+2",
                "https://via.placeholder.com/400x400?text=Remera+Premium+3"
            ),
            status = ProductStatus.ACTIVE,
            analytics = ProductAnalytics(
                views = 1247,
                uniqueViews = 892,
                favorites = 156,
                whatsappClicks = 89
            )
        )
    }
}

data class ProductDetailState(
    val product: Product? = null,
    val isFavorite: Boolean = false,
    val relatedProducts: List<Product> = emptyList()
)

data class ProductDetailUIState(
    val isLoading: Boolean = false,
    val error: String? = null
)