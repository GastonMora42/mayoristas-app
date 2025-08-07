// feature/auth/domain/src/main/kotlin/com/mayoristas/feature/auth/domain/model/User.kt

package com.mayoristas.feature.auth.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val userType: UserType,
    val isVerified: Boolean,
    val profile: UserProfile?,
    val subscription: UserSubscription?,
    val createdAt: Long,
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class UserType {
    SELLER, CLIENT
}

@Parcelize
data class UserProfile(
    val companyName: String?,
    val businessType: BusinessType?,
    val taxId: String?, // CUIT/RUT para LatAm
    val phoneNumber: String?,
    val whatsappNumber: String?, // Número específico para WhatsApp Business
    val address: Address?,
    val clothingCategories: List<ClothingCategory> = emptyList(), // Categorías que maneja
    val businessHours: BusinessHours?,
    val socialMedia: SocialMediaLinks?,
    val businessPhotos: List<String> = emptyList(), // URLs de fotos del local/empresa
    val description: String?, // Descripción del negocio
    val yearsInBusiness: Int?, // Años en el rubro
    val certifications: List<String> = emptyList(),
    val minimumOrderValue: Double? = null, // Monto mínimo de compra
    val deliveryOptions: List<DeliveryOption> = emptyList()
) : Parcelable

enum class BusinessType {
    MANUFACTURER,      // Fabricante
    DISTRIBUTOR,       // Distribuidor
    WHOLESALER,        // Mayorista
    RETAILER,          // Minorista
    AGENT,            // Representante/Agente
    IMPORTER          // Importador
}

@Parcelize
data class Address(
    val street: String,
    val streetNumber: String?,
    val neighborhood: String?, // Barrio
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val googlePlaceId: String? = null // Para integración con Google Places
) : Parcelable

// Categorías específicas de ropa
enum class ClothingCategory(val displayName: String, val subcategories: List<String>) {
    WOMENS_CLOTHING("Ropa Femenina", listOf(
        "Vestidos", "Blusas", "Pantalones", "Faldas", "Jeans", 
        "Ropa Interior", "Trajes", "Ropa Deportiva"
    )),
    MENS_CLOTHING("Ropa Masculina", listOf(
        "Camisas", "Pantalones", "Jeans", "Trajes", "Ropa Interior", 
        "Ropa Deportiva", "Polos", "Chombas"
    )),
    KIDS_CLOTHING("Ropa Infantil", listOf(
        "Bebés (0-2 años)", "Niños (3-8 años)", "Preadolescentes (9-14 años)",
        "Uniformes Escolares", "Ropa de Juego"
    )),
    FOOTWEAR("Calzado", listOf(
        "Zapatos Formales", "Zapatillas", "Botas", "Sandalias", 
        "Calzado Deportivo", "Calzado Infantil"
    )),
    ACCESSORIES("Accesorios", listOf(
        "Carteras", "Cinturones", "Sombreros", "Bufandas", 
        "Joyas de Fantasía", "Relojes"
    )),
    UNDERWEAR("Lencería y Ropa Interior", listOf(
        "Lencería Femenina", "Ropa Interior Masculina", 
        "Ropa Interior Infantil", "Medias y Calcetines"
    )),
    SPORTSWEAR("Ropa Deportiva", listOf(
        "Fitness", "Running", "Fútbol", "Yoga", "Natación", "Gimnasio"
    )),
    WORKWEAR("Ropa de Trabajo", listOf(
        "Uniformes", "Ropa de Seguridad", "Delantales", "Overoles"
    ))
}

@Parcelize
data class BusinessHours(
    val monday: DaySchedule?,
    val tuesday: DaySchedule?,
    val wednesday: DaySchedule?,
    val thursday: DaySchedule?,
    val friday: DaySchedule?,
    val saturday: DaySchedule?,
    val sunday: DaySchedule?
) : Parcelable

@Parcelize
data class DaySchedule(
    val isOpen: Boolean,
    val openTime: String?, // Format: "HH:mm"
    val closeTime: String?, // Format: "HH:mm"
    val breakStart: String? = null, // Horario de descanso
    val breakEnd: String? = null
) : Parcelable

@Parcelize
data class SocialMediaLinks(
    val instagram: String? = null,
    val facebook: String? = null,
    val website: String? = null,
    val linkedIn: String? = null
) : Parcelable

enum class DeliveryOption(val displayName: String) {
    PICKUP("Retiro en local"),
    LOCAL_DELIVERY("Envío local"),
    NATIONAL_SHIPPING("Envío nacional"),
    INTERNATIONAL_SHIPPING("Envío internacional")
}

// Suscripciones y planes
@Parcelize
data class UserSubscription(
    val planType: SubscriptionPlan,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean,
    val autoRenew: Boolean,
    val paymentStatus: PaymentStatus,
    val mercadoPagoSubscriptionId: String? = null,
    val productsUsed: Int = 0, // Productos publicados actualmente
    val productsLimit: Int, // Límite según el plan
    val featuresEnabled: List<PlanFeature> = emptyList()
) : Parcelable

enum class SubscriptionPlan(
    val displayName: String,
    val monthlyPrice: Double,
    val productsLimit: Int,
    val features: List<PlanFeature>
) {
    FREE("Plan Catálogo", 0.0, 30, listOf(
        PlanFeature.WHATSAPP_CONTACT,
        PlanFeature.BASIC_VISIBILITY
    )),
    ACTIVE("Plan Vendedor Activo", 25000.0, 60, listOf(
        PlanFeature.WHATSAPP_CONTACT,
        PlanFeature.BASIC_VISIBILITY,
        PlanFeature.FEATURED_ROTATION,
        PlanFeature.BASIC_STATS,
        PlanFeature.BETTER_SEARCH_POSITION
    )),
    FULL("Plan Proveedor Full", 50000.0, 150, listOf(
        PlanFeature.WHATSAPP_CONTACT,
        PlanFeature.BASIC_VISIBILITY,
        PlanFeature.FEATURED_ROTATION,
        PlanFeature.BASIC_STATS,
        PlanFeature.BETTER_SEARCH_POSITION,
        PlanFeature.PRIORITY_IN_SEARCH,
        PlanFeature.DETAILED_STATS,
        PlanFeature.PROFESSIONAL_PROFILE,
        PlanFeature.EXTERNAL_LINK
    ))
}

enum class PlanFeature(val displayName: String) {
    WHATSAPP_CONTACT("Contacto por WhatsApp"),
    BASIC_VISIBILITY("Visibilidad básica"),
    FEATURED_ROTATION("Productos destacados (rotativo)"),
    BASIC_STATS("Estadísticas básicas"),
    BETTER_SEARCH_POSITION("Mejor posición en búsquedas"),
    PRIORITY_IN_SEARCH("Prioridad en búsquedas"),
    DETAILED_STATS("Estadísticas detalladas"),
    PROFESSIONAL_PROFILE("Perfil profesional"),
    EXTERNAL_LINK("Link externo")
}

enum class PaymentStatus {
    PENDING,
    ACTIVE,
    FAILED,
    CANCELLED,
    EXPIRED
}

// Credenciales mejoradas para registro
data class RegisterCredentials(
    val email: String,
    val password: String,
    val displayName: String,
    val userType: UserType,
    val profile: UserProfile
)

data class LoginCredentials(
    val email: String,
    val password: String
)

// Estados de autenticación
sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    data object BiometricRequired : AuthState()
    data class ProfileIncomplete(val user: User) : AuthState() // Para forzar completar perfil
}

// Extensiones útiles
fun User.canPublishProducts(): Boolean {
    return subscription?.isActive == true || subscription?.planType == SubscriptionPlan.FREE
}

fun User.getProductsRemaining(): Int {
    val subscription = this.subscription ?: return 0
    return subscription.productsLimit - subscription.productsUsed
}

fun User.hasFeature(feature: PlanFeature): Boolean {
    return subscription?.featuresEnabled?.contains(feature) == true
}

fun ClothingCategory.getAllOptions(): List<String> {
    return listOf(displayName) + subcategories
}

// Validation helpers
object ProfileValidation {
    fun isValidCUIT(cuit: String): Boolean {
        val cleanCuit = cuit.replace("-", "")
        return cleanCuit.matches(Regex("\\d{11}")) && validateCUITChecksum(cleanCuit)
    }
    
    private fun validateCUITChecksum(cuit: String): Boolean {
        val weights = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
        val digits = cuit.map { it.toString().toInt() }
        
        val sum = digits.take(10).zip(weights) { digit, weight -> 
            digit * weight 
        }.sum()
        
        val remainder = sum % 11
        val checkDigit = when (remainder) {
            0, 1 -> remainder
            else -> 11 - remainder
        }
        
        return checkDigit == digits[10]
    }
    
    fun isValidWhatsAppNumber(number: String): Boolean {
        val cleanNumber = number.replace(Regex("[\\s\\-\\(\\)\\+]"), "")
        return cleanNumber.matches(Regex("\\d{10,15}")) // Internacional format
    }
    
    fun isValidBusinessEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) &&
               !email.contains("gmail.com", ignoreCase = true) && // Preferir emails empresariales
               !email.contains("yahoo.com", ignoreCase = true) &&
               !email.contains("hotmail.com", ignoreCase = true)
    }
}