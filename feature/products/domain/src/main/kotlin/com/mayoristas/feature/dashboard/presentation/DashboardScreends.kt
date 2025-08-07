// feature/dashboard/presentation/src/main/kotlin/com/mayoristas/feature/dashboard/presentation/DashboardScreens.kt

package com.mayoristas.feature.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mayoristas.feature.auth.domain.model.*

// === MAIN DASHBOARD COORDINATOR ===
@Composable
fun DashboardScreen(
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    LaunchedEffect(user) {
        viewModel.loadDashboardData(user)
    }
    
    when (user.userType) {
        UserType.SELLER -> SellerDashboard(
            user = user,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToProducts = onNavigateToProducts,
            onNavigateToSubscription = onNavigateToSubscription,
            onLogout = onLogout,
            viewModel = viewModel
        )
        UserType.CLIENT -> BuyerDashboard(
            user = user,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToSearch = onNavigateToSearch,
            onLogout = onLogout,
            viewModel = viewModel
        )
    }
}

// === SELLER DASHBOARD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboard(
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val subscription = user.subscription ?: UserSubscription(
        planType = SubscriptionPlan.FREE,
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
        isActive = true,
        autoRenew = false,
        paymentStatus = PaymentStatus.ACTIVE,
        productsUsed = 0,
        productsLimit = 30,
        featuresEnabled = SubscriptionPlan.FREE.features
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        SellerTopBar(
            user = user,
            onNavigateToProfile = onNavigateToProfile,
            onLogout = onLogout
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Section
            WelcomeSection(
                userName = user.displayName ?: "Vendedor",
                companyName = user.profile?.companyName ?: "Tu Negocio"
            )
            
            // Subscription Status
            SubscriptionStatusCard(
                subscription = subscription,
                onUpgradeClick = onNavigateToSubscription
            )
            
            // Quick Stats
            QuickStatsSection(
                subscription = subscription,
                stats = dashboardState.sellerStats
            )
            
            // Quick Actions
            QuickActionsSection(
                subscription = subscription,
                onAddProductClick = onNavigateToProducts,
                onViewProductsClick = onNavigateToProducts,
                onViewStatsClick = { /* TODO: Navigate to detailed stats */ }
            )
            
            // Recent Activity
            RecentActivitySection(
                activities = dashboardState.recentActivities
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerTopBar(
    user: User,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Mayoristas.com",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Panel de Vendedor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Notifications
            IconButton(onClick = { /* TODO: Notifications */ }) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Notifications, "Notificaciones")
                }
            }
            
            // Profile Menu
            var menuExpanded by remember { mutableStateOf(false) }
            
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    AsyncImage(
                        model = user.profile?.businessPhotos?.firstOrNull(),
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop,
                        fallback = { 
                            Icon(
                                Icons.Default.Store, 
                                "Perfil",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                }
                
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver Perfil") },
                        onClick = {
                            menuExpanded = false
                            onNavigateToProfile()
                        },
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Configuración") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Settings, null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión") },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        },
                        leadingIcon = { Icon(Icons.Default.Logout, null) }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun WelcomeSection(
    userName: String,
    companyName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "¡Hola, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    companyName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                
                Text(
                    "Gestioná tu negocio y aumentá tus ventas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionStatusCard(
    subscription: UserSubscription,
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (subscription.planType) {
                SubscriptionPlan.FREE -> MaterialTheme.colorScheme.surfaceVariant
                SubscriptionPlan.ACTIVE -> Color(0xFFFFF3E0) // Light orange
                SubscriptionPlan.FULL -> Color(0xFFE8F5E8) // Light green
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        subscription.planType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (subscription.planType != SubscriptionPlan.FREE) {
                        Text(
                            "$${subscription.planType.monthlyPrice.toInt()}/mes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                PlanBadge(subscription.planType)
            }
            
            // Products usage
            val usagePercentage = subscription.productsUsed.toFloat() / subscription.productsLimit.toFloat()
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Productos publicados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${subscription.productsUsed}/${subscription.productsLimit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                LinearProgressIndicator(
                    progress = usagePercentage,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        usagePercentage < 0.7f -> MaterialTheme.colorScheme.primary
                        usagePercentage < 0.9f -> Color(0xFFFF9800) // Orange
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            
            if (subscription.planType == SubscriptionPlan.FREE) {
                OutlinedButton(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar Plan")
                }
            }
        }
    }
}

@Composable
private fun PlanBadge(planType: SubscriptionPlan) {
    val (color, text) = when (planType) {
        SubscriptionPlan.FREE -> MaterialTheme.colorScheme.outline to "GRATIS"
        SubscriptionPlan.ACTIVE -> Color(0xFFFF9800) to "ACTIVO"
        SubscriptionPlan.FULL -> Color(0xFF4CAF50) to "FULL"
    }
    
    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickStatsSection(
    subscription: UserSubscription,
    stats: SellerStats?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Vistas",
            value = stats?.totalViews?.toString() ?: "0",
            icon = Icons.Default.Visibility,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Contactos",
            value = stats?.totalContacts?.toString() ?: "0",
            icon = Icons.Default.WhatsApp,
            color = Color(0xFF25D366), // WhatsApp green
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Favoritos",
            value = stats?.totalFavorites?.toString() ?: "0",
            icon = Icons.Default.Favorite,
            color = Color(0xFFE91E63), // Pink
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    subscription: UserSubscription,
    onAddProductClick: () -> Unit,
    onViewProductsClick: () -> Unit,
    onViewStatsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(getQuickActions(subscription)) { action ->
                QuickActionCard(
                    action = action,
                    onClick = when (action.id) {
                        "add_product" -> onAddProductClick
                        "view_products" -> onViewProductsClick
                        "view_stats" -> onViewStatsClick
                        else -> { }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = action.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = action.iconColor,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                action.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = action.textColor,
                textAlign = TextAlign.Center
            )
            
            if (action.subtitle != null) {
                Text(
                    action.subtitle!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = action.textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecentActivitySection(
    activities: List<ActivityItem>
) {
    if (activities.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Actividad Reciente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activities.take(5).forEach { activity ->
                    ActivityItemRow(activity)
                    if (activity != activities.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
                
                if (activities.size > 5) {
                    TextButton(
                        onClick = { /* TODO: Show all activities */ },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ver más actividades")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItemRow(activity: ActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            activity.icon,
            contentDescription = null,
            tint = activity.iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                activity.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            activity.timeAgo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// === BUYER DASHBOARD ===
@Composable
fun BuyerDashboard(
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple top bar for buyers
        BuyerTopBar(
            user = user,
            onNavigateToProfile = onNavigateToProfile,
            onLogout = onLogout
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome
            BuyerWelcomeSection(
                userName = user.displayName ?: "Comprador",
                companyName = user.profile?.companyName
            )
            
            // Search Section
            SearchSection(onSearchClick = onNavigateToSearch)
            
            // Categories
            CategoriesSection(onCategoryClick = { /* TODO */ })
            
            // Featured Products
            FeaturedProductsSection(
                products = dashboardState.featuredProducts,
                onProductClick = { /* TODO */ }
            )
            
            // Favorite Sellers
            FavoriteSellersSection(
                sellers = dashboardState.favoriteSellerListItems,
                onSellerClick = { /* TODO */ }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// === BUYER COMPONENTS ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyerTopBar(
    user: User,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Mayoristas.com",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Encuentra productos mayoristas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Favorites */ }) {
                Icon(Icons.Default.Favorite, "Favoritos")
            }
            
            // Profile menu (similar to seller)
            var menuExpanded by remember { mutableStateOf(false) }
            
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Person, "Perfil")
                }
                
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver Perfil") },
                        onClick = {
                            menuExpanded = false
                            onNavigateToProfile()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión") },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun BuyerWelcomeSection(
    userName: String,
    companyName: String?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "¡Hola, $userName! 🛍️",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            if (companyName != null) {
                Text(
                    companyName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                )
            }
            
            Text(
                "Descubrí productos mayoristas para tu negocio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SearchSection(onSearchClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                "Buscar productos, marcas, vendedores...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoriesSection(onCategoryClick: (ClothingCategory) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Categorías",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(ClothingCategory.values().take(4).toList()) { category ->
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    products: List<ProductItem>,
    onProductClick: (ProductItem) -> Unit
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
            
            TextButton(onClick = { /* TODO: View all */ }) {
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
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteSellersSection(
    sellers: List<SellerItem>,
    onSellerClick: (SellerItem) -> Unit
) {
    if (sellers.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Vendedores Recomendados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(sellers) { seller ->
                SellerCard(
                    seller = seller,
                    onClick = { onSellerClick(seller) }
                )
            }
        }
    }
}

// === DATA CLASSES ===
data class QuickAction(
    val id: String,
    val title: String,
    val subtitle: String?,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
    val textColor: Color
)

data class ActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconColor: Color
)

data class ProductItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String?,
    val sellerName: String,
    val location: String
)

data class SellerItem(
    val id: String,
    val name: String,
    val companyName: String,
    val imageUrl: String?,
    val rating: Float,
    val totalProducts: Int,
    val location: String
)

// === HELPER FUNCTIONS ===
private fun getQuickActions(subscription: UserSubscription): List<QuickAction> {
    return listOf(
        QuickAction(
            id = "add_product",
            title = "Agregar Producto",
            subtitle = "${subscription.productsUsed}/${subscription.productsLimit}",
            icon = Icons.Default.Add,
            backgroundColor = Color(0xFFE3F2FD), // Light blue
            iconColor = Color(0xFF1976D2), // Blue
            textColor = Color(0xFF1976D2)
        ),
        QuickAction(
            id = "view_products",
            title = "Mis Productos",
            subtitle = null,
            icon = Icons.Default.Inventory,
            backgroundColor = Color(0xFFF3E5F5), // Light purple
            iconColor = Color(0xFF7B1FA2), // Purple
            textColor = Color(0xFF7B1FA2)
        ),
        QuickAction(
            id = "view_stats",
            title = if (subscription.hasFeature(PlanFeature.DETAILED_STATS)) "Estadísticas" else "Stats Básicas",
            subtitle = null,
            icon = Icons.Default.Analytics,
            backgroundColor = Color(0xFFE8F5E8), // Light green
            iconColor = Color(0xFF388E3C), // Green
            textColor = Color(0xFF388E3C)
        )
    )
}

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

// Placeholder components
@Composable
private fun ProductCard(product: ProductItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column {
            // Product image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    "$${product.price.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    product.sellerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SellerCard(seller: SellerItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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
            
            Text(
                "${seller.totalProducts} productos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}