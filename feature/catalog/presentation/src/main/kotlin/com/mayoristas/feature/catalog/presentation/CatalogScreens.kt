// feature/catalog/presentation/src/main/kotlin/com/mayoristas/feature/catalog/presentation/CatalogScreens.kt

package com.mayoristas.feature.catalog.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mayoristas.feature.auth.domain.model.ClothingCategory
import com.mayoristas.feature.auth.domain.model.User
import com.mayoristas.feature.products.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCatalogScreen(
    user: User,
    onNavigateToProductDetail: (String, String) -> Unit,
    onNavigateToSellerProfile: (String) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showFilters by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadFeaturedProducts()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar with Search
        SearchTopBar(
            searchQuery = searchState.query,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onSearchTriggered = viewModel::performSearch,
            onShowFilters = { showFilters = true },
            activeFiltersCount = searchState.activeFiltersCount
        )
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                
                searchState.query.isNotEmpty() -> {
                    SearchResultsContent(
                        results = catalogState.searchResults,
                        query = searchState.query,
                        onProductClick = onNavigateToProductDetail,
                        onSellerClick = onNavigateToSellerProfile,
                        onFavoriteClick = viewModel::toggleFavorite,
                        user = user
                    )
                }
                
                else -> {
                    CatalogHomeContent(
                        featuredProducts = catalogState.featuredProducts,
                        categories = ClothingCategory.values().toList(),
                        onProductClick = onNavigateToProductDetail,
                        onSellerClick = onNavigateToSellerProfile,
                        onCategoryClick = viewModel::selectCategory,
                        onFavoriteClick = viewModel::toggleFavorite,
                        user = user
                    )
                }
            }
            
            // Filters Bottom Sheet
            if (showFilters) {
                FiltersBottomSheet(
                    filters = searchState.filters,
                    onFiltersChanged = viewModel::updateFilters,
                    onApplyFilters = {
                        showFilters = false
                        viewModel.applyFilters()
                    },
                    onDismiss = { showFilters = false }
                )
            }
        }
    }
    
    // Error handling
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Show snackbar or handle error
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onShowFilters: () -> Unit,
    activeFiltersCount: Int
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar productos, marcas, vendedores...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChanged("") }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            onSearchTriggered()
                        }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                // Filters Button
                FilterChip(
                    selected = activeFiltersCount > 0,
                    onClick = onShowFilters,
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtros",
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text("Filtros")
                            
                            if (activeFiltersCount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Text(
                                        activeFiltersCount.toString(),
                                        modifier = Modifier.wrapContentSize(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                )
            }
            
            // Quick categories
            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ClothingCategory.values().take(6)) { category ->
                        AssistChip(
                            onClick = { /* TODO: Quick category selection */ },
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                Icon(
                                    getCategoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogHomeContent(
    featuredProducts: List<Product>,
    categories: List<ClothingCategory>,
    onProductClick: (String, String) -> Unit,
    onSellerClick: (String) -> Unit,
    onCategoryClick: (ClothingCategory) -> Unit,
    onFavoriteClick: (String) -> Unit,
    user: User
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome section
        item {
            WelcomeSection(user)
        }
        
        // Categories grid
        item {
            CategoriesSection(
                categories = categories,
                onCategoryClick = onCategoryClick
            )
        }
        
        // Featured products
        item {
            FeaturedProductsSection(
                products = featuredProducts,
                onProductClick = onProductClick,
                onSellerClick = onSellerClick,
                onFavoriteClick = onFavoriteClick,
                currentUser = user
            )
        }
        
        // Recent sellers
        item {
            RecentSellersSection(
                onSellerClick = onSellerClick
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    results: List<Product>,
    query: String,
    onProductClick: (String, String) -> Unit,
    onSellerClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    user: User
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Results header
        item {
            Text(
                "${results.size} resultados para \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Results grid
        items(results.chunked(2)) { productPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                productPair.forEach { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { onProductClick(product.id, product.sellerId) },
                        onSellerClick = { onSellerClick(product.sellerId) },
                        onFavoriteClick = { onFavoriteClick(product.id) },
                        currentUser = user,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if odd number of products
                if (productPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Empty state
        if (results.isEmpty()) {
            item {
                EmptySearchResults(query)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                "Buscando productos...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WelcomeSection(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "¡Encuentra los mejores productos mayoristas!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "${user.profile?.companyName ?: "Tu negocio"} merece los mejores proveedores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<ClothingCategory>,
    onCategoryClick: (ClothingCategory) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Categorías",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(categories.take(4)) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: ClothingCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                getCategoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturedProductsSection(
    products: List<Product>,
    onProductClick: (String, String) -> Unit,
    onSellerClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    currentUser: User
) {
    if (products.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Productos Destacados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { /* TODO: View all featured */ }) {
                Text("Ver todos")
            }
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onProductClick = { onProductClick(product.id, product.sellerId) },
                    onSellerClick = { onSellerClick(product.sellerId) },
                    onFavoriteClick = { onFavoriteClick(product.id) },
                    currentUser = currentUser,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onSellerClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    currentUser: User,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable { onProductClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = product.images.firstOrNull() ?: "https://via.placeholder.com/300x300?text=Producto",
                    contentDescription = product.basicInfo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Favorite button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.White
                    )
                }
                
                // Price badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        product.getDisplayPrice(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    product.basicInfo.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Seller info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSellerClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        product.sellerInfo.companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Minimum quantity
                Text(
                    "Mín: ${product.pricingInfo.minimumQuantity} ${product.pricingInfo.pricePerUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // WhatsApp button
                WhatsAppContactButton(
                    product = product,
                    buyer = currentUser,
                    variant = WhatsAppButtonVariant.OUTLINE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RecentSellersSection(
    onSellerClick: (String) -> Unit
) {
    // Mock data for recent sellers
    val mockSellers = listOf(
        SellerItem("1", "Ana García", "Textiles del Norte", null, 4.8f, 45, "Tucumán"),
        SellerItem("2", "Carlos Ruiz", "Confecciones Sur", null, 4.6f, 67, "Córdoba"),
        SellerItem("3", "María López", "Fashion Wholesale", null, 4.9f, 89, "Buenos Aires")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Vendedores Destacados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(mockSellers) { seller ->
                SellerCard(
                    seller = seller,
                    onClick = { onSellerClick(seller.id) }
                )
            }
        }
    }
}

@Composable
private fun SellerCard(
    seller: SellerItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Text(
                seller.companyName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF9800)
                )
                Text(
                    seller.rating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                "${seller.totalProducts} productos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Text(
            "No se encontraron productos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            "No hay resultados para \"$query\"\nIntentá con otros términos de búsqueda",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = { /* TODO: Clear search */ }
        ) {
            Text("Limpiar búsqueda")
        }
    }
}

// Filters Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersBottomSheet(
    filters: ProductFilter,
    onFiltersChanged: (ProductFilter) -> Unit,
    onApplyFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(
                    onClick = { onFiltersChanged(ProductFilter()) }
                ) {
                    Text("Limpiar todo")
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Category filters
                item {
                    FilterSection(
                        title = "Categorías",
                        content = {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(ClothingCategory.values().toList()) { category ->
                                    FilterChip(
                                        selected = filters.categories.contains(category),
                                        onClick = {
                                            val newCategories = if (filters.categories.contains(category)) {
                                                filters.categories - category
                                            } else {
                                                filters.categories + category
                                            }
                                            onFiltersChanged(filters.copy(categories = newCategories))
                                        },
                                        label = { Text(category.displayName) }
                                    )
                                }
                            }
                        }
                    )
                }
                
                // Price range filter
                item {
                    FilterSection(
                        title = "Rango de precios",
                        content = {
                            PriceRangeFilter(
                                priceRange = filters.priceRange,
                                onPriceRangeChanged = { newRange ->
                                    onFiltersChanged(filters.copy(priceRange = newRange))
                                }
                            )
                        }
                    )
                }
                
                // Gender filter
                item {
                    FilterSection(
                        title = "Género",
                        content = {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ClothingGender.values()) { gender ->
                                    FilterChip(
                                        selected = filters.genders.contains(gender),
                                        onClick = {
                                            val newGenders = if (filters.genders.contains(gender)) {
                                                filters.genders - gender
                                            } else {
                                                filters.genders + gender
                                            }
                                            onFiltersChanged(filters.copy(genders = newGenders))
                                        },
                                        label = { Text(gender.displayName) }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            
            // Apply button
            Button(
                onClick = onApplyFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar filtros")
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        content()
    }
}

@Composable
private fun PriceRangeFilter(
    priceRange: PriceRange?,
    onPriceRangeChanged: (PriceRange?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = priceRange?.min?.toInt()?.toString() ?: "",
                onValueChange = { newMin ->
                    val min = newMin.toDoubleOrNull()
                    if (min != null) {
                        val max = priceRange?.max ?: min + 1000
                        onPriceRangeChanged(PriceRange(min, max))
                    }
                },
                label = { Text("Precio mínimo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = priceRange?.max?.toInt()?.toString() ?: "",
                onValueChange = { newMax ->
                    val max = newMax.toDoubleOrNull()
                    if (max != null) {
                        val min = priceRange?.min ?: 0.0
                        onPriceRangeChanged(PriceRange(min, max))
                    }
                },
                label = { Text("Precio máximo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Quick price ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickRanges = listOf(
                "Menos de $5.000" to PriceRange(0.0, 5000.0),
                "$5.000 - $15.000" to PriceRange(5000.0, 15000.0),
                "$15.000 - $30.000" to PriceRange(15000.0, 30000.0),
                "Más de $30.000" to PriceRange(30000.0, Double.MAX_VALUE)
            )
            
            items(quickRanges) { (label, range) ->
                FilterChip(
                    selected = priceRange == range,
                    onClick = {
                        onPriceRangeChanged(if (priceRange == range) null else range)
                    },
                    label = { Text(label) }
                )
            }
        }
    }
}

// Helper function for category icons
private fun getCategoryIcon(category: ClothingCategory): ImageVector {
    return when (category) {
        ClothingCategory.WOMENS_CLOTHING -> Icons.Default.Woman
        ClothingCategory.MENS_CLOTHING -> Icons.Default.Man
        ClothingCategory.KIDS_CLOTHING -> Icons.Default.ChildCare
        ClothingCategory.FOOTWEAR -> Icons.Default.SportsBaseball
        ClothingCategory.ACCESSORIES -> Icons.Default.Watch
        ClothingCategory.UNDERWEAR -> Icons.Default.Checkroom
        ClothingCategory.SPORTSWEAR -> Icons.Default.FitnessCenter
        ClothingCategory.WORKWEAR -> Icons.Default.Work
    }
}

// Data classes
data class SellerItem(
    val id: String,
    val name: String,
    val companyName: String,
    val imageUrl: String?,
    val rating: Float,
    val totalProducts: Int,
    val location: String
)

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    MaterialTheme {
        // Preview implementation would go here
    }
}