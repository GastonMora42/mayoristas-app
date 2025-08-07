// feature/whatsapp/domain/src/main/kotlin/com/mayoristas/feature/whatsapp/domain/WhatsAppIntegration.kt

package com.mayoristas.feature.whatsapp.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mayoristas.feature.auth.domain.model.User
import com.mayoristas.feature.products.domain.model.Product
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppIntegration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whatsappAnalytics: WhatsAppAnalytics
) {
    
    /**
     * Abre WhatsApp con un mensaje pre-formateado para consultar sobre un producto
     */
    fun contactSellerAboutProduct(
        product: Product,
        buyer: User,
        customMessage: String? = null
    ): WhatsAppResult {
        try {
            val sellerWhatsApp = product.sellerInfo.whatsappNumber
            
            if (!isValidWhatsAppNumber(sellerWhatsApp)) {
                return WhatsAppResult.Error("Número de WhatsApp del vendedor inválido")
            }
            
            val message = buildProductInquiryMessage(
                product = product,
                buyer = buyer,
                customMessage = customMessage
            )
            
            val success = openWhatsApp(sellerWhatsApp, message)
            
            if (success) {
                // Track analytics
                whatsappAnalytics.trackProductContact(
                    productId = product.id,
                    sellerId = product.sellerId,
                    buyerId = buyer.id,
                    contactType = WhatsAppContactType.PRODUCT_INQUIRY
                )
                
                return WhatsAppResult.Success(
                    message = "WhatsApp abierto correctamente",
                    whatsappNumber = sellerWhatsApp
                )
            } else {
                return WhatsAppResult.Error("No se pudo abrir WhatsApp")
            }
            
        } catch (e: Exception) {
            return WhatsAppResult.Error("Error al contactar vendedor: ${e.message}")
        }
    }
    
    /**
     * Abre WhatsApp para contactar directamente un vendedor (desde su perfil)
     */
    fun contactSeller(
        seller: User,
        buyer: User,
        customMessage: String? = null
    ): WhatsAppResult {
        try {
            val sellerWhatsApp = seller.profile?.whatsappNumber
            
            if (sellerWhatsApp == null || !isValidWhatsAppNumber(sellerWhatsApp)) {
                return WhatsAppResult.Error("Número de WhatsApp del vendedor inválido")
            }
            
            val message = buildSellerContactMessage(
                seller = seller,
                buyer = buyer,
                customMessage = customMessage
            )
            
            val success = openWhatsApp(sellerWhatsApp, message)
            
            if (success) {
                whatsappAnalytics.trackProductContact(
                    productId = null,
                    sellerId = seller.id,
                    buyerId = buyer.id,
                    contactType = WhatsAppContactType.SELLER_PROFILE
                )
                
                return WhatsAppResult.Success(
                    message = "WhatsApp abierto correctamente",
                    whatsappNumber = sellerWhatsApp
                )
            } else {
                return WhatsAppResult.Error("No se pudo abrir WhatsApp")
            }
            
        } catch (e: Exception) {
            return WhatsAppResult.Error("Error al contactar vendedor: ${e.message}")
        }
    }
    
    /**
     * Compartir producto por WhatsApp
     */
    fun shareProduct(
        product: Product,
        sharer: User
    ): WhatsAppResult {
        try {
            val message = buildProductShareMessage(product, sharer)
            val success = openWhatsAppForSharing(message)
            
            if (success) {
                whatsappAnalytics.trackProductContact(
                    productId = product.id,
                    sellerId = product.sellerId,
                    buyerId = sharer.id,
                    contactType = WhatsAppContactType.PRODUCT_SHARE
                )
                
                return WhatsAppResult.Success(
                    message = "WhatsApp abierto para compartir",
                    whatsappNumber = null
                )
            } else {
                return WhatsAppResult.Error("No se pudo abrir WhatsApp")
            }
            
        } catch (e: Exception) {
            return WhatsAppResult.Error("Error al compartir producto: ${e.message}")
        }
    }
    
    // === PRIVATE METHODS ===
    
    private fun openWhatsApp(phoneNumber: String, message: String): Boolean {
        return try {
            val cleanNumber = cleanWhatsAppNumber(phoneNumber)
            val encodedMessage = Uri.encode(message)
            
            // Try WhatsApp Business first, then regular WhatsApp
            val whatsappBusinessUri = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMessage"
            val whatsappUri = "https://wa.me/$cleanNumber?text=$encodedMessage"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappBusinessUri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // Check if WhatsApp is installed
            if (isWhatsAppInstalled()) {
                context.startActivity(intent)
                true
            } else {
                // Fallback to web WhatsApp
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(whatsappUri)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun openWhatsAppForSharing(message: String): Boolean {
        return try {
            val encodedMessage = Uri.encode(message)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isWhatsAppInstalled()) {
                context.startActivity(intent)
                true
            } else {
                // Fallback to generic sharing
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(genericIntent, "Compartir producto"))
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isWhatsAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0) // WhatsApp Business
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun buildProductInquiryMessage(
        product: Product,
        buyer: User,
        customMessage: String?
    ): String {
        val companyName = buyer.profile?.companyName ?: buyer.displayName ?: "Cliente"
        val location = buyer.profile?.address?.let { "${it.city}, ${it.state}" } ?: ""
        
        val baseMessage = """
            🛍️ *Consulta sobre producto - Mayoristas.com*
            
            Hola! Soy $companyName${if (location.isNotEmpty()) " de $location" else ""}.
            
            Me interesa el siguiente producto:
            📦 *${product.basicInfo.name}*
            💰 Precio: ${product.getDisplayPrice()}
            📏 Mínimo: ${product.pricingInfo.minimumQuantity} unidades
            
            ${customMessage ?: "¿Podrías darme más información sobre disponibilidad y condiciones de venta?"}
            
            Gracias!
        """.trimIndent()
        
        return baseMessage
    }
    
    private fun buildSellerContactMessage(
        seller: User,
        buyer: User,
        customMessage: String?
    ): String {
        val buyerCompany = buyer.profile?.companyName ?: buyer.displayName ?: "Cliente"
        val sellerCompany = seller.profile?.companyName ?: seller.displayName ?: "Vendedor"
        val location = buyer.profile?.address?.let { "${it.city}, ${it.state}" } ?: ""
        
        val baseMessage = """
            👋 *Contacto desde Mayoristas.com*
            
            Hola $sellerCompany!
            
            Soy $buyerCompany${if (location.isNotEmpty()) " de $location" else ""}.
            
            ${customMessage ?: "Me gustaría conocer más sobre tus productos y condiciones comerciales."}
            
            Muchas gracias!
        """.trimIndent()
        
        return baseMessage
    }
    
    private fun buildProductShareMessage(
        product: Product,
        sharer: User
    ): String {
        val message = """
            🛍️ *Te recomiendo este producto mayorista*
            
            📦 ${product.basicInfo.name}
            🏢 ${product.sellerInfo.companyName}
            💰 ${product.getDisplayPrice()} ${product.pricingInfo.pricePerUnit}
            📏 Mínimo: ${product.pricingInfo.minimumQuantity} unidades
            
            📱 Contactá directo por WhatsApp: wa.me/${cleanWhatsAppNumber(product.sellerInfo.whatsappNumber)}
            
            *Mayoristas.com* - Tu marketplace B2B de confianza
        """.trimIndent()
        
        return message
    }
    
    private fun cleanWhatsAppNumber(phoneNumber: String): String {
        return phoneNumber
            .replace(Regex("[^0-9+]"), "") // Remove everything except digits and +
            .removePrefix("+")
            .let { number ->
                // Add Argentina country code if missing
                if (number.length == 10 && !number.startsWith("54")) {
                    "54$number"
                } else {
                    number
                }
            }
    }
    
    private fun isValidWhatsAppNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        
        val cleanNumber = cleanWhatsAppNumber(phoneNumber)
        return cleanNumber.length in 10..15 && cleanNumber.all { it.isDigit() }
    }
}

// === DATA CLASSES AND ENUMS ===

sealed class WhatsAppResult {
    data class Success(
        val message: String,
        val whatsappNumber: String?
    ) : WhatsAppResult()
    
    data class Error(
        val error: String
    ) : WhatsAppResult()
}

enum class WhatsAppContactType {
    PRODUCT_INQUIRY,    // Consulta sobre producto específico
    SELLER_PROFILE,     // Contacto desde perfil del vendedor
    PRODUCT_SHARE,      // Compartir producto
    BULK_INQUIRY        // Consulta por volumen
}

data class WhatsAppContact(
    val id: String,
    val productId: String?,
    val sellerId: String,
    val buyerId: String,
    val contactType: WhatsAppContactType,
    val whatsappNumber: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

// === ANALYTICS ===

@Singleton
class WhatsAppAnalytics @Inject constructor(
    // TODO: Add Firebase Analytics or your analytics provider
) {
    
    fun trackProductContact(
        productId: String?,
        sellerId: String,
        buyerId: String,
        contactType: WhatsAppContactType
    ) {
        try {
            // Track the contact event
            val eventData = mapOf(
                "product_id" to (productId ?: ""),
                "seller_id" to sellerId,
                "buyer_id" to buyerId,
                "contact_type" to contactType.name,
                "timestamp" to System.currentTimeMillis()
            )
            
            // TODO: Send to Firebase Analytics
            logAnalyticsEvent("whatsapp_contact", eventData)
            
            // TODO: Save to database for seller analytics
            saveContactToDatabase(productId, sellerId, buyerId, contactType)
            
        } catch (e: Exception) {
            // Log error but don't fail the WhatsApp action
            println("Analytics error: ${e.message}")
        }
    }
    
    private fun logAnalyticsEvent(eventName: String, parameters: Map<String, Any>) {
        // TODO: Implement Firebase Analytics
        // FirebaseAnalytics.getInstance(context).logEvent(eventName, Bundle().apply {
        //     parameters.forEach { (key, value) ->
        //         putString(key, value.toString())
        //     }
        // })
    }
    
    private fun saveContactToDatabase(
        productId: String?,
        sellerId: String,
        buyerId: String,
        contactType: WhatsAppContactType
    ) {
        // TODO: Save to Firestore for seller dashboard analytics
        // This will help sellers see which products generate more contacts
    }
}

// === WHATSAPP BUTTON COMPONENTS ===

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WhatsApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WhatsAppContactButton(
    product: Product,
    buyer: User,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    variant: WhatsAppButtonVariant = WhatsAppButtonVariant.PRIMARY
) {
    val context = LocalContext.current
    val whatsAppIntegration = remember { 
        // TODO: Get from DI container
        WhatsAppIntegration(context, WhatsAppAnalytics())
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    when (variant) {
        WhatsAppButtonVariant.PRIMARY -> {
            Button(
                onClick = {
                    isLoading = true
                    val result = whatsAppIntegration.contactSellerAboutProduct(product, buyer, customMessage)
                    isLoading = false
                    
                    when (result) {
                        is WhatsAppResult.Error -> {
                            errorMessage = result.error
                            showErrorDialog = true
                        }
                        is WhatsAppResult.Success -> {
                            // Success is handled by opening WhatsApp
                        }
                    }
                },
                modifier = modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366), // WhatsApp green
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Icon(
                    Icons.Default.WhatsApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    "Contactar por WhatsApp",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        WhatsAppButtonVariant.OUTLINE -> {
            OutlinedButton(
                onClick = {
                    isLoading = true
                    val result = whatsAppIntegration.contactSellerAboutProduct(product, buyer, customMessage)
                    isLoading = false
                    
                    when (result) {
                        is WhatsAppResult.Error -> {
                            errorMessage = result.error
                            showErrorDialog = true
                        }
                        is WhatsAppResult.Success -> {
                            // Success is handled by opening WhatsApp
                        }
                    }
                },
                modifier = modifier,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = null,
                    width = 2.dp,
                    // color = Color(0xFF25D366)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF25D366),
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Icon(
                    Icons.Default.WhatsApp,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    "WhatsApp",
                    color = Color(0xFF25D366),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        WhatsAppButtonVariant.ICON_ONLY -> {
            IconButton(
                onClick = {
                    val result = whatsAppIntegration.contactSellerAboutProduct(product, buyer, customMessage)
                    when (result) {
                        is WhatsAppResult.Error -> {
                            errorMessage = result.error
                            showErrorDialog = true
                        }
                        is WhatsAppResult.Success -> {
                            // Success is handled by opening WhatsApp
                        }
                    }
                },
                modifier = modifier
            ) {
                Icon(
                    Icons.Default.WhatsApp,
                    contentDescription = "Contactar por WhatsApp",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error al abrir WhatsApp") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun WhatsAppSellerContactButton(
    seller: User,
    buyer: User,
    modifier: Modifier = Modifier,
    customMessage: String? = null
) {
    val context = LocalContext.current
    val whatsAppIntegration = remember { 
        WhatsAppIntegration(context, WhatsAppAnalytics())
    }
    
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    OutlinedButton(
        onClick = {
            val result = whatsAppIntegration.contactSeller(seller, buyer, customMessage)
            when (result) {
                is WhatsAppResult.Error -> {
                    errorMessage = result.error
                    showErrorDialog = true
                }
                is WhatsAppResult.Success -> {
                    // Success is handled by opening WhatsApp
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            Icons.Default.WhatsApp,
            contentDescription = null,
            tint = Color(0xFF25D366),
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            "Contactar Vendedor",
            color = Color(0xFF25D366)
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error al abrir WhatsApp") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun WhatsAppShareButton(
    product: Product,
    sharer: User,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val whatsAppIntegration = remember { 
        WhatsAppIntegration(context, WhatsAppAnalytics())
    }
    
    IconButton(
        onClick = {
            whatsAppIntegration.shareProduct(product, sharer)
        },
        modifier = modifier
    ) {
        Icon(
            Icons.Default.Share,
            contentDescription = "Compartir por WhatsApp",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class WhatsAppButtonVariant {
    PRIMARY,      // Botón principal verde
    OUTLINE,      // Botón con borde
    ICON_ONLY     // Solo icono
}