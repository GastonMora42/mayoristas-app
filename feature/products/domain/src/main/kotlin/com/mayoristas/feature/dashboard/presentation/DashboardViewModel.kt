// Crear archivo: feature/products/domain/src/main/kotlin/com/mayoristas/feature/dashboard/presentation/DashboardViewModel.kt

package com.mayoristas.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayoristas.feature.auth.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    fun loadDashboardData(user: User) {
        viewModelScope.launch {
            // TODO: Implementar carga de datos del dashboard
            _dashboardState.value = _dashboardState.value.copy(
                isLoading = false,
                sellerStats = SellerStats(
                    totalViews = 0,
                    totalContacts = 0,
                    totalFavorites = 0
                )
            )
        }
    }
}

data class DashboardState(
    val isLoading: Boolean = false,
    val sellerStats: SellerStats? = null,
    val featuredProducts: List<ProductItem> = emptyList(),
    val favoriteSellerListItems: List<SellerItem> = emptyList(),
    val recentActivities: List<ActivityItem> = emptyList()
)

data class SellerStats(
    val totalViews: Int = 0,
    val totalContacts: Int = 0,
    val totalFavorites: Int = 0
)

// Placeholder data classes
data class ProductItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val sellerName: String = "",
    val location: String = ""
)

data class SellerItem(
    val id: String = "",
    val name: String = "",
    val companyName: String = "",
    val imageUrl: String? = null,
    val rating: Float = 0f,
    val totalProducts: Int = 0,
    val location: String = ""
)

data class ActivityItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timeAgo: String = "",
    val icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.Info,
    val iconColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Blue
)