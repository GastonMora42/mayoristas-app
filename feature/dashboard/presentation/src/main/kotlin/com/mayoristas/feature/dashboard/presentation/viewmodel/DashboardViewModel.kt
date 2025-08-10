// feature/dashboard/presentation/src/main/kotlin/com/mayoristas/feature/dashboard/presentation/viewmodel/DashboardViewModel.kt


package com.mayoristas.feature.dashboard.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
    // TODO: Add ProductRepository, AnalyticsRepository, etc.
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _uiState = MutableStateFlow(DashboardUIState())
    val uiState: StateFlow<DashboardUIState> = _uiState.asStateFlow()
    
    fun loadDashboardData(user: User) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            when (user.userType) {
                UserType.SELLER -> loadSellerDashboard(user)
                UserType.CLIENT -> loadBuyerDashboard(user)
            }
        }
    }
    
    private suspend fun loadSellerDashboard(user: User) {
        withContext(dispatcherProvider.io) {
            try {
                // Load seller stats (placeholder data for now)
                val stats = generateMockSellerStats(user)
                
                // Load recent activities
                val activities = generateMockActivities(user)
                
                _dashboardState.value = _dashboardState.value.copy(
                    sellerStats = stats,
                    recentActivities = activities
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading dashboard data"
                )
            }
        }
    }
    
    private suspend fun loadBuyerDashboard(user: User) {
        withContext(dispatcherProvider.io) {
            try {
                // Load featured products
                val featuredProducts = generateMockFeaturedProducts()
                
                // Load favorite sellers
                val favoriteSellerListItems = generateMockFavoriteSellers()
                
                _dashboardState.value = _dashboardState.value.copy(
                    featuredProducts = featuredProducts,
                    favoriteSellerListItems = favoriteSellerListItems
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading dashboard data"
                )
            }
        }
    }
    
    fun refreshData(user: User) {
        loadDashboardData(user)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // === MOCK DATA GENERATORS (Replace with real repository calls) ===
    private fun generateMockSellerStats(user: User): SellerStats {
        val subscription = user.subscription
        val productsPublished = subscription?.productsUsed ?: 0
        
        // Generate realistic mock data based on subscription level and profile
        val baseViews = when (subscription?.planType) {
            SubscriptionPlan.FREE -> (50..200).random()
            SubscriptionPlan.ACTIVE -> (200..800).random()
            SubscriptionPlan.FULL -> (500..1500).random()
            else -> 0
        }
        
        val baseContacts = (baseViews * (0.05..0.15).random()).toInt()
        val baseFavorites = (baseViews * (0.02..0.08).random()).toInt()
        
        return SellerStats(
            totalViews = baseViews,
            totalContacts = baseContacts,
            totalFavorites = baseFavorites,
            productsPublished = productsPublished,
            thisMonthViews = (baseViews * (0.3..0.7).random()).toInt(),
            thisMonthContacts = (baseContacts * (0.3..0.7).random()).toInt(),
            conversionRate = if (baseViews > 0) (baseContacts.toDouble() / baseViews.toDouble()) * 100 else 0.0,
            topProducts = generateTopProducts(user)
        )
    }
    
    private fun generateTopProducts(user: User): List<TopProductStats> {
        val categories = user.profile?.clothingCategories ?: emptyList()
        
        return listOf(
            TopProductStats(
                productId = "prod_1",
                name = if (categories.contains(ClothingCategory.WOMENS_CLOTHING)) 
                      "Vestido Casual Verano" else "Remera Básica",
                views = (80..150).random(),
                contacts = (5..25).random(),
                imageUrl = null
            ),
            TopProductStats(
                productId = "prod_2", 
                name = if (categories.contains(ClothingCategory.MENS_CLOTHING))
                      "Jean Clásico Hombre" else "Pantalón Deportivo",
                views = (60..120).random(),
                contacts = (3..20).random(),
                imageUrl = null
            ),
            TopProductStats(
                productId = "prod_3",
                name = if (categories.contains(ClothingCategory.KIDS_CLOTHING))
                      "Conjunto Infantil" else "Campera Abrigo",
                views = (40..90).random(),
                contacts = (2..15).random(),
                imageUrl = null
            )
        )
    }
    
    private fun generateMockActivities(user: User): List<ActivityItem> {
        val activities = mutableListOf<ActivityItem>()
        
        // Generate realistic activities based on user profile
        val companyName = user.profile?.companyName ?: "Tu negocio"
        
        activities.add(
            ActivityItem(
                id = "act_1",
                title = "Nuevo contacto por WhatsApp",
                subtitle = "Consulta sobre 'Vestido Casual Verano'",
                timeAgo = "Hace 2 horas",
                icon = Icons.Default.WhatsApp,
                iconColor = Color(0xFF25D366)
            )
        )
        
        activities.add(
            ActivityItem(
                id = "act_2",
                title = "Producto agregado a favoritos",
                subtitle = "'Jean Clásico Hombre' por Cliente ABC",
                timeAgo = "Hace 4 horas",
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFFE91E63)
            )
        )
        
        activities.add(
            ActivityItem(
                id = "act_3",
                title = "Perfil visualizado",
                subtitle = "3 nuevas visitas a tu perfil de $companyName",
                timeAgo = "Ayer",
                icon = Icons.Default.Visibility,
                iconColor = MaterialTheme.colorScheme.primary
            )
        )
        
        if (user.subscription?.planType != SubscriptionPlan.FREE) {
            activities.add(
                ActivityItem(
                    id = "act_4",
                    title = "Producto destacado",
                    subtitle = "'Pantalón Deportivo' apareció en búsquedas destacadas",
                    timeAgo = "Hace 2 días",
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFF9800)
                )
            )
        }
        
        activities.add(
            ActivityItem(
                id = "act_5",
                title = "Actualización de perfil",
                subtitle = "Agregaste nuevas fotos del negocio",
                timeAgo = "Hace 3 días",
                icon = Icons.Default.Update,
                iconColor = Color(0xFF4CAF50)
            )
        )
        
        return activities
    }
    
    private fun generateMockFeaturedProducts(): List<ProductItem> {
        return listOf(
            ProductItem(
                id = "feat_1",
                name = "Vestido Elegante Noche",
                price = 15000.0,
                imageUrl = null,
                sellerName = "Boutique Rosario",
                location = "Rosario, Santa Fe"
            ),
            ProductItem(
                id = "feat_2",
                name = "Jean Premium Hombre",
                price = 12500.0,
                imageUrl = null,
                sellerName = "Denim Factory",
                location = "CABA, Buenos Aires"
            ),
            ProductItem(
                id = "feat_3",
                name = "Conjunto Deportivo",
                price = 8900.0,
                imageUrl = null,
                sellerName = "SportWear Plus",
                location = "Córdoba, Córdoba"
            ),
            ProductItem(
                id = "feat_4",
                name = "Camisa Formal Slim",
                price = 7500.0,
                imageUrl = null,
                sellerName = "Confecciones del Sur",
                location = "Mar del Plata, Buenos Aires"
            ),
            ProductItem(
                id = "feat_5",
                name = "Zapatillas Running",
                price = 22000.0,
                imageUrl = null,
                sellerName = "Calzados Norte",
                location = "Tucumán, Tucumán"
            )
        )
    }
    
    private fun generateMockFavoriteSellers(): List<SellerItem> {
        return listOf(
            SellerItem(
                id = "seller_1",
                name = "María González",
                companyName = "Boutique Rosario",
                imageUrl = null,
                rating = 4.8f,
                totalProducts = 45,
                location = "Rosario, Santa Fe"
            ),
            SellerItem(
                id = "seller_2", 
                name = "Carlos Mendoza",
                companyName = "Denim Factory",
                imageUrl = null,
                rating = 4.6f,
                totalProducts = 67,
                location = "CABA, Buenos Aires"
            ),
            SellerItem(
                id = "seller_3",
                name = "Ana López",
                companyName = "SportWear Plus", 
                imageUrl = null,
                rating = 4.9f,
                totalProducts = 38,
                location = "Córdoba, Córdoba"
            ),
            SellerItem(
                id = "seller_4",
                name = "Roberto Silva",
                companyName = "Textiles del Litoral",
                imageUrl = null,
                rating = 4.5f,
                totalProducts = 89,
                location = "Santa Fe, Santa Fe"
            )
        )
    }
}

// === STATE DATA CLASSES ===
data class DashboardState(
    // Seller-specific data
    val sellerStats: SellerStats? = null,
    val recentActivities: List<ActivityItem> = emptyList(),
    
    // Buyer-specific data
    val featuredProducts: List<ProductItem> = emptyList(),
    val favoriteSellerListItems: List<SellerItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    
    // Common data
    val notifications: List<NotificationItem> = emptyList()
)

data class DashboardUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val refreshing: Boolean = false
)

data class SellerStats(
    val totalViews: Int,
    val totalContacts: Int,
    val totalFavorites: Int,
    val productsPublished: Int,
    val thisMonthViews: Int,
    val thisMonthContacts: Int,
    val conversionRate: Double, // Percentage of views that become contacts
    val topProducts: List<TopProductStats>
)

data class TopProductStats(
    val productId: String,
    val name: String,
    val views: Int,
    val contacts: Int,
    val imageUrl: String?
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    CONTACT, FAVORITE, SUBSCRIPTION, PRODUCT_APPROVED, GENERAL
}

// Import statements needed (these should be added to the actual file)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color