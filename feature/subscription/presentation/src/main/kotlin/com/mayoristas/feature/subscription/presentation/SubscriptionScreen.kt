// feature/subscription/presentation/src/main/kotlin/com/mayoristas/feature/subscription/presentation/SubscriptionScreen.kt

package com.mayoristas.feature.subscription.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mayoristas.feature.auth.domain.model.User
import com.mayoristas.feature.auth.domain.model.UserSubscription
import com.mayoristas.feature.auth.domain.model.SubscriptionPlan
import com.mayoristas.feature.auth.domain.model.PlanFeature
import com.mayoristas.feature.auth.domain.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val subscriptionState by viewModel.subscriptionState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(user) {
        viewModel.loadSubscriptionInfo(user)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Planes y Suscripciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Current subscription status
                    item {
                        CurrentSubscriptionCard(
                            subscription = subscriptionState.currentSubscription,
                            onManageClick = { viewModel.showManageSubscription() }
                        )
                    }
                    
                    // Usage stats
                    item {
                        UsageStatsCard(
                            subscription = subscriptionState.currentSubscription,
                            usage = subscriptionState.currentUsage
                        )
                    }
                    
                    // Available plans header
                    item {
                        PlansSectionHeader()
                    }
                    
                    // Plans grid
                    items(SubscriptionPlan.values().toList()) { plan ->
                        PlanCard(
                            plan = plan,
                            currentPlan = subscriptionState.currentSubscription?.planType,
                            onSelectPlan = {
                                if (plan != subscriptionState.currentSubscription?.planType) {
                                    onNavigateToPayment(plan.name)
                                }
                            },
                            isRecommended = plan == SubscriptionPlan.ACTIVE
                        )
                    }
                    
                    // Features comparison
                    item {
                        FeaturesComparisonSection()
                    }
                    
                    // FAQ Section
                    item {
                        FAQSection()
                    }
                }
            }
        }
    }
    
    // Manage subscription dialog
    if (uiState.showManageDialog) {
        ManageSubscriptionDialog(
            subscription = subscriptionState.currentSubscription,
            onDismiss = { viewModel.hideManageSubscription() },
            onCancelSubscription = { viewModel.cancelSubscription() },
            onReactivateSubscription = { viewModel.reactivateSubscription() }
        )
    }
    
    // Error handling
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Show snackbar
        }
    }
}

@Composable
private fun CurrentSubscriptionCard(
    subscription: UserSubscription?,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (subscription?.planType) {
                SubscriptionPlan.FREE -> MaterialTheme.colorScheme.surfaceVariant
                SubscriptionPlan.ACTIVE -> Color(0xFFFFF3E0) // Light orange
                SubscriptionPlan.FULL -> Color(0xFFE8F5E8) // Light green
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Plan Actual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        subscription?.planType?.displayName ?: "Sin plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (subscription?.planType != SubscriptionPlan.FREE) {
                        Text(
                            "$${subscription?.planType?.monthlyPrice?.toInt()}/mes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                PlanStatusBadge(subscription?.paymentStatus ?: PaymentStatus.EXPIRED)
            }
            
            // Subscription details
            if (subscription != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subscription.planType != SubscriptionPlan.FREE) {
                        SubscriptionDetailRow(
                            "Próxima facturación:",
                            formatDate(subscription.endDate)
                        )
                        
                        SubscriptionDetailRow(
                            "Renovación automática:",
                            if (subscription.autoRenew) "Activada" else "Desactivada"
                        )
                    }
                    
                    SubscriptionDetailRow(
                        "Productos utilizados:",
                        "${subscription.productsUsed}/${subscription.productsLimit}"
                    )
                }
                
                // Progress bar for product usage
                val usageProgress = subscription.productsUsed.toFloat() / subscription.productsLimit.toFloat()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = usageProgress,
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            usageProgress < 0.7f -> MaterialTheme.colorScheme.primary
                            usageProgress < 0.9f -> Color(0xFFFF9800) // Orange
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    if (usageProgress >= 0.8f) {
                        Text(
                            "⚠️ Te estás acercando al límite de productos",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (usageProgress >= 0.9f) MaterialTheme.colorScheme.error
                                    else Color(0xFFFF9800)
                        )
                    }
                }
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onManageClick) {
                        Text("Gestionar suscripción")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanStatusBadge(status: PaymentStatus) {
    val (color, text) = when (status) {
        PaymentStatus.ACTIVE -> Color(0xFF4CAF50) to "ACTIVO"
        PaymentStatus.PENDING -> Color(0xFFFF9800) to "PENDIENTE"
        PaymentStatus.FAILED -> Color(0xFFE91E63) to "FALLÓ"
        PaymentStatus.CANCELLED -> Color(0xFF9E9E9E) to "CANCELADO"
        PaymentStatus.EXPIRED -> Color(0xFF9E9E9E) to "EXPIRADO"
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
private fun UsageStatsCard(
    subscription: UserSubscription?,
    usage: SubscriptionUsage?
) {
    if (subscription == null || usage == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Estadísticas del Mes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UsageStatItem(
                    value = usage.productsViewed.toString(),
                    label = "Vistas de productos",
                    icon = Icons.Default.Visibility
                )
                
                UsageStatItem(
                    value = usage.whatsappContacts.toString(),
                    label = "Contactos WhatsApp",
                    icon = Icons.Default.WhatsApp
                )
                
                if (subscription.hasFeature(PlanFeature.DETAILED_STATS)) {
                    UsageStatItem(
                        value = "${usage.conversionRate}%",
                        label = "Tasa conversión",
                        icon = Icons.Default.TrendingUp
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageStatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlansSectionHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Elegí el plan que mejor se adapte a tu negocio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            "Todos los planes incluyen contacto por WhatsApp y soporte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    currentPlan: SubscriptionPlan?,
    onSelectPlan: () -> Unit,
    isRecommended: Boolean = false
) {
    val isCurrentPlan = plan == currentPlan
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isCurrentPlan) onSelectPlan() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentPlan -> MaterialTheme.colorScheme.primaryContainer
                isRecommended -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isRecommended && !isCurrentPlan) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRecommended) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with badges
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        plan.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentPlan) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (plan == SubscriptionPlan.FREE) {
                        Text(
                            "GRATIS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.Baseline,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "$${plan.monthlyPrice.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "/mes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (plan == SubscriptionPlan.FULL) {
                            Text(
                                "Precio por año: $${(plan.monthlyPrice * 10).toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
                
                // Badges
                if (isRecommended && !isCurrentPlan) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "RECOMENDADO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (isCurrentPlan) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "PLAN ACTUAL",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Product limit
            Text(
                "Hasta ${plan.productsLimit} publicaciones activas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isCurrentPlan) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurface
            )
            
            // Features list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plan.features.forEach { feature ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Text(
                            feature.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentPlan) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Action button
            if (!isCurrentPlan) {
                Button(
                    onClick = onSelectPlan,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan == SubscriptionPlan.FREE) 
                                        Color(0xFF4CAF50)
                                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        when (plan) {
                            SubscriptionPlan.FREE -> "Cambiar a Gratuito"
                            else -> "Seleccionar Plan"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturesComparisonSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Comparación de Características",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Comparison table
            val features = listOf(
                "Publicaciones" to mapOf(
                    SubscriptionPlan.FREE to "30",
                    SubscriptionPlan.ACTIVE to "60", 
                    SubscriptionPlan.FULL to "150"
                ),
                "WhatsApp" to mapOf(
                    SubscriptionPlan.FREE to "✓",
                    SubscriptionPlan.ACTIVE to "✓",
                    SubscriptionPlan.FULL to "✓"
                ),
                "Destacados" to mapOf(
                    SubscriptionPlan.FREE to "✗",
                    SubscriptionPlan.ACTIVE to "✓",
                    SubscriptionPlan.FULL to "✓"
                ),
                "Estadísticas" to mapOf(
                    SubscriptionPlan.FREE to "✗",
                    SubscriptionPlan.ACTIVE to "Básicas",
                    SubscriptionPlan.FULL to "Detalladas"
                ),
                "Soporte" to mapOf(
                    SubscriptionPlan.FREE to "Email",
                    SubscriptionPlan.ACTIVE to "Email + Chat",
                    SubscriptionPlan.FULL to "Prioritario"
                )
            )
            
            features.forEach { (featureName, planValues) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        featureName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        planValues.values.forEach { value ->
                            Text(
                                value,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (value == "✓") Color(0xFF4CAF50)
                                       else if (value == "✗") Color(0xFF9E9E9E)
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FAQSection() {
    var expandedFaq by remember { mutableStateOf(-1) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val faqs = listOf(
                "¿Puedo cambiar de plan en cualquier momento?" to 
                "Sí, podés cambiar tu plan cuando quieras. Los cambios se aplican inmediatamente y se prorratea el costo.",
                
                "¿Qué pasa si supero el límite de productos?" to 
                "Cuando alcances el límite, no podrás publicar más productos hasta que actualices tu plan o elimines productos existentes.",
                
                "¿Cómo funciona la facturación?" to 
                "Te cobramos mensualmente el día que te suscribiste. Podés ver todas tus facturas en la sección de facturación.",
                
                "¿Puedo cancelar mi suscripción?" to 
                "Sí, podés cancelar en cualquier momento. Tu plan seguirá activo hasta el final del período facturado.",
                
                "¿Hay descuentos por pago anual?" to 
                "Sí, el Plan Proveedor Full tiene un descuento especial si pagás por año completo."
            )
            
            faqs.forEachIndexed { index, (question, answer) ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                expandedFaq = if (expandedFaq == index) -1 else index
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            if (expandedFaq == index) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                    
                    AnimatedVisibility(visible = expandedFaq == index) {
                        Text(
                            answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                }
                
                if (index < faqs.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageSubscriptionDialog(
    subscription: UserSubscription?,
    onDismiss: () -> Unit,
    onCancelSubscription: () -> Unit,
    onReactivateSubscription: () -> Unit
) {
    if (subscription == null) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar Suscripción") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Plan actual: ${subscription.planType.displayName}")
                
                if (subscription.planType != SubscriptionPlan.FREE) {
                    Text("Próxima facturación: ${formatDate(subscription.endDate)}")
                    Text("Renovación automática: ${if (subscription.autoRenew) "Activada" else "Desactivada"}")
                }
                
                Text(
                    "¿Qué te gustaría hacer?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (subscription.paymentStatus == PaymentStatus.CANCELLED) {
                    TextButton(onClick = onReactivateSubscription) {
                        Text("Reactivar")
                    }
                } else if (subscription.planType != SubscriptionPlan.FREE) {
                    TextButton(
                        onClick = onCancelSubscription,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancelar suscripción")
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}

// Helper functions
@Composable
private fun SubscriptionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Data classes for subscription usage
data class SubscriptionUsage(
    val productsViewed: Int = 0,
    val whatsappContacts: Int = 0,
    val conversionRate: Double = 0.0,
    val periodStart: Long = 0L,
    val periodEnd: Long = 0L
)

@Preview(showBackground = true)
@Composable
private fun SubscriptionScreenPreview() {
    MaterialTheme {
        // Preview would show mock data
    }
}