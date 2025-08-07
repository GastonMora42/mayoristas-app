// app/src/main/kotlin/com/mayoristas/app/navigation/MayoristasNavigation.kt

package com.mayoristas.app.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.mayoristas.feature.auth.domain.model.AuthState
import com.mayoristas.feature.auth.domain.model.User
import com.mayoristas.feature.auth.domain.model.UserType
import com.mayoristas.feature.auth.presentation.viewmodel.AuthViewModel

@Composable
fun MayoristasNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = authState.authState) {
        is AuthState.Initial, is AuthState.Loading -> {
            // Show loading screen
            LoadingScreen()
        }
        
        is AuthState.Success -> {
            // Main app navigation
            MainAppNavigation(
                navController = navController,
                user = state.user,
                onLogout = { authViewModel.logout() }
            )
        }
        
        is AuthState.Error -> {
            // Auth screens (login/register)
            AuthNavigation(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        else -> {
            // Auth screens (login/register)
            AuthNavigation(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
private fun MainAppNavigation(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit
) {
    // Check if profile is complete
    val isProfileComplete = user.profile?.companyName?.isNotBlank() == true &&
                           user.profile?.whatsappNumber?.isNotBlank() == true &&
                           user.profile?.address?.city?.isNotBlank() == true
    
    if (!isProfileComplete) {
        // Force profile completion
        BusinessProfileNavigation(
            onProfileCompleted = { /* Refresh user data */ }
        )
        return
    }
    
    when (user.userType) {
        UserType.SELLER -> SellerMainNavigation(
            navController = navController,
            user = user,
            onLogout = onLogout
        )
        UserType.CLIENT -> BuyerMainNavigation(
            navController = navController,
            user = user,
            onLogout = onLogout
        )
    }
}

// === SELLER NAVIGATION ===
@Composable
private fun SellerMainNavigation(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Products,
        BottomNavItem.Analytics,
        BottomNavItem.Profile
    )
    
    Scaffold(
        bottomBar = {
            SellerBottomNavBar(
                navController = navController,
                items = bottomNavItems
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home/Dashboard
            composable("home") {
                DashboardScreen(
                    user = user,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToProducts = { navController.navigate("products") },
                    onNavigateToSearch = { }, // Not used for sellers
                    onNavigateToSubscription = { navController.navigate("subscription") },
                    onLogout = onLogout
                )
            }
            
            // Products Management
            composable("products") {
                ProductsListScreen(
                    user = user,
                    onNavigateToAddProduct = { navController.navigate("add_product") },
                    onNavigateToEditProduct = { productId -> 
                        navController.navigate("edit_product/$productId")
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            
            // Add Product
            composable("add_product") {
                AddEditProductScreen(
                    productId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onProductSaved = { productId ->
                        navController.navigate("product_detail/$productId") {
                            popUpTo("products")
                        }
                    }
                )
            }
            
            // Edit Product
            composable(
                "edit_product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                AddEditProductScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onProductSaved = { savedId ->
                        navController.navigate("product_detail/$savedId") {
                            popUpTo("products")
                        }
                    }
                )
            }
            
            // Product Detail
            composable(
                "product_detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    user = user,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { 
                        navController.navigate("edit_product/$productId")
                    }
                )
            }
            
            // Analytics
            composable("analytics") {
                AnalyticsScreen(
                    user = user,
                    onNavigateToSubscription = { navController.navigate("subscription") }
                )
            }
            
            // Profile
            composable("profile") {
                BusinessProfileScreen(
                    onProfileCompleted = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    isEditMode = true
                )
            }
            
            // Subscription Management
            composable("subscription") {
                SubscriptionScreen(
                    user = user,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPayment = { plan ->
                        navController.navigate("payment/$plan")
                    }
                )
            }
            
            // Payment
            composable(
                "payment/{planType}",
                arguments = listOf(navArgument("planType") { type = NavType.StringType })
            ) { backStackEntry ->
                val planType = backStackEntry.arguments?.getString("planType") ?: ""
                PaymentScreen(
                    planType = planType,
                    user = user,
                    onNavigateBack = { navController.popBackStack() },
                    onPaymentSuccess = { 
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// === BUYER NAVIGATION ===
@Composable
private fun BuyerMainNavigation(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )
    
    Scaffold(
        bottomBar = {
            BuyerBottomNavBar(
                navController = navController,
                items = bottomNavItems
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home/Dashboard
            composable("home") {
                DashboardScreen(
                    user = user,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToProducts = { }, // Not used for buyers
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToSubscription = { }, // Not used for buyers
                    onLogout = onLogout
                )
            }
            
            // Search & Catalog
            composable("search") {
                SearchCatalogScreen(
                    user = user,
                    onNavigateToProductDetail = { productId, sellerId ->
                        navController.navigate("product_detail/$productId/$sellerId")
                    },
                    onNavigateToSellerProfile = { sellerId ->
                        navController.navigate("seller_profile/$sellerId")
                    }
                )
            }
            
            // Product Detail (for buyers)
            composable(
                "product_detail/{productId}/{sellerId}",
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType },
                    navArgument("sellerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
                
                BuyerProductDetailScreen(
                    productId = productId,
                    sellerId = sellerId,
                    buyer = user,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSellerProfile = { 
                        navController.navigate("seller_profile/$sellerId")
                    }
                )
            }
            
            // Seller Profile (for buyers)
            composable(
                "seller_profile/{sellerId}",
                arguments = listOf(navArgument("sellerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
                
                SellerProfileScreen(
                    sellerId = sellerId,
                    buyer = user,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProductDetail = { productId ->
                        navController.navigate("product_detail/$productId/$sellerId")
                    }
                )
            }
            
            // Favorites
            composable("favorites") {
                FavoritesScreen(
                    user = user,
                    onNavigateToProductDetail = { productId, sellerId ->
                        navController.navigate("product_detail/$productId/$sellerId")
                    }
                )
            }
            
            // Profile
            composable("profile") {
                BusinessProfileScreen(
                    onProfileCompleted = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    isEditMode = true
                )
            }
        }
    }
}

// === BOTTOM NAVIGATION ===
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    object Home : BottomNavItem("home", "Inicio", Icons.Default.Home)
    object Products : BottomNavItem("products", "Productos", Icons.Default.Inventory)
    object Search : BottomNavItem("search", "Buscar", Icons.Default.Search)
    object Analytics : BottomNavItem("analytics", "Estadísticas", Icons.Default.Analytics)
    object Favorites : BottomNavItem("favorites", "Favoritos", Icons.Default.Favorite)
    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}

@Composable
private fun SellerBottomNavBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { 
                    Text(
                        item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BuyerBottomNavBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { 
                    Text(
                        item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

// === AUTH NAVIGATION ===
@Composable
private fun AuthNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register") {
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    // This will be handled by the parent composable
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // This will be handled by the parent composable
                }
            )
        }
    }
}

@Composable
private fun BusinessProfileNavigation(
    onProfileCompleted: () -> Unit
) {
    BusinessProfileScreen(
        onProfileCompleted = onProfileCompleted,
        onNavigateBack = { }, // Can't go back when profile is required
        isEditMode = false
    )
}

// === LOADING SCREEN ===
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            
            Text(
                "Cargando...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// === PLACEHOLDER SCREENS (to be implemented) ===
@Composable
private fun ProductsListScreen(
    user: User,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (String) -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    // TODO: Implement products list for sellers
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Lista de Productos")
            Button(onClick = onNavigateToAddProduct) {
                Text("Agregar Producto")
            }
        }
    }
}

@Composable
private fun ProductDetailScreen(
    productId: String,
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    // TODO: Implement product detail for sellers
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Detalle de Producto: $productId")
            Button(onClick = onNavigateToEdit) {
                Text("Editar")
            }
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }
    }
}

@Composable
private fun AnalyticsScreen(
    user: User,
    onNavigateToSubscription: () -> Unit
) {
    // TODO: Implement analytics screen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Estadísticas")
    }
}

@Composable
private fun SubscriptionScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit
) {
    // TODO: Implement subscription management
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Gestión de Suscripción")
    }
}

@Composable
private fun PaymentScreen(
    planType: String,
    user: User,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    // TODO: Implement MercadoPago payment
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Pago: $planType")
    }
}

@Composable
private fun SearchCatalogScreen(
    user: User,
    onNavigateToProductDetail: (String, String) -> Unit,
    onNavigateToSellerProfile: (String) -> Unit
) {
    // TODO: Implement search and catalog for buyers
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Catálogo de Productos")
    }
}

@Composable
private fun BuyerProductDetailScreen(
    productId: String,
    sellerId: String,
    buyer: User,
    onNavigateBack: () -> Unit,
    onNavigateToSellerProfile: () -> Unit
) {
    // TODO: Implement product detail for buyers with WhatsApp button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Producto: $productId")
    }
}

@Composable
private fun SellerProfileScreen(
    sellerId: String,
    buyer: User,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    // TODO: Implement seller profile for buyers
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Perfil de Vendedor: $sellerId")
    }
}

@Composable
private fun FavoritesScreen(
    user: User,
    onNavigateToProductDetail: (String, String) -> Unit
) {
    // TODO: Implement favorites screen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Favoritos")
    }
}