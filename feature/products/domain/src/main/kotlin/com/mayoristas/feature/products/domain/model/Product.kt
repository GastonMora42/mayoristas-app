// feature/products/domain/src/main/kotlin/com/mayoristas/feature/products/domain/model/Product.kt

package com.mayoristas.feature.products.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.mayoristas.feature.auth.domain.model.ClothingCategory

@Parcelize
data class Product(
    val id: String = "",
    val sellerId: String,
    val sellerInfo: SellerInfo,
    val basicInfo: ProductBasicInfo,
    val clothingDetails: ClothingDetails,
    val pricingInfo: PricingInfo,
    val inventory: InventoryInfo,
    val images: List<String> = emptyList(),
    val status: ProductStatus = ProductStatus.DRAFT,
    val visibility: ProductVisibility = ProductVisibility.PUBLIC,
    val analytics: ProductAnalytics = ProductAnalytics(),
    val seo: ProductSEO = ProductSEO(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SellerInfo(
    val sellerId: String,
    val companyName: String,
    val displayName: String,
    val whatsappNumber: String,
    val location: String,
    val rating: Float = 0.0f,
    val profileImageUrl: String? = null
) : Parcelable

@Parcelize
data class ProductBasicInfo(
    val name: String,
    val description: String,
    val shortDescription: String? = null,
    val category: ClothingCategory,
    val subcategory: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val tags: List<String> = emptyList()
) : Parcelable

@Parcelize
data class ClothingDetails(
    val gender: ClothingGender,
    val season: Season,
    val materials: List<ClothingMaterial>,
    val sizes: List<ClothingSize>,
    val colors: List<ClothingColor>,
    val ageGroup: AgeGroup? = null,
    val style: ClothingStyle? = null,
    val occasion: List<ClothingOccasion> = emptyList(),
    val careInstructions: List<CareInstruction> = emptyList(),
    val countryOfOrigin: String? = null,
    val certifications: List<String> = emptyList()
) : Parcelable

@Parcelize
data class PricingInfo(
    val basePrice: Double,
    val currency: String = "ARS",
    val minimumQuantity: Int,
    val maximumQuantity: Int? = null,
    val bulkPricing: List<BulkPriceRule> = emptyList(),
    val pricePerUnit: String? = null, // "por unidad", "por docena", "por pack"
    val includesVAT: Boolean = true,
    val paymentTerms: String? = null, // "Contado", "30 días", etc.
    val discountPercentage: Double? = null,
    val originalPrice: Double? = null // For showing discounts
) : Parcelable

@Parcelize
data class BulkPriceRule(
    val minimumQuantity: Int,
    val pricePerUnit: Double,
    val discountPercentage: Double
) : Parcelable

@Parcelize
data class InventoryInfo(
    val totalStock: Int,
    val reservedStock: Int = 0,
    val availableStock: Int = totalStock - reservedStock,
    val lowStockThreshold: Int? = null,
    val restockDate: Long? = null,
    val trackInventory: Boolean = true,
    val allowBackorders: Boolean = false,
    val stockByVariant: Map<String, Int> = emptyMap() // For size/color combinations
) : Parcelable

@Parcelize
data class ProductAnalytics(
    val views: Int = 0,
    val uniqueViews: Int = 0,
    val favorites: Int = 0,
    val whatsappClicks: Int = 0,
    val shares: Int = 0,
    val lastViewedAt: Long? = null,
    val conversionRate: Double = 0.0,
    val averageTimeOnPage: Double = 0.0
) : Parcelable

@Parcelize
data class ProductSEO(
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val searchKeywords: List<String> = emptyList(),
    val searchScore: Double = 0.0
) : Parcelable

// === ENUMS ===
enum class ProductStatus(val displayName: String) {
    DRAFT("Borrador"),
    ACTIVE("Activo"),
    PAUSED("Pausado"),
    OUT_OF_STOCK("Sin Stock"),
    UNDER_REVIEW("En Revisión"),
    REJECTED("Rechazado"),
    ARCHIVED("Archivado")
}

enum class ProductVisibility(val displayName: String) {
    PUBLIC("Público"),
    PRIVATE("Privado"),
    HIDDEN("Oculto")
}

enum class ClothingGender(val displayName: String) {
    UNISEX("Unisex"),
    MALE("Hombre"), 
    FEMALE("Mujer"),
    KIDS_UNISEX("Niños/Niñas"),
    KIDS_MALE("Niños"),
    KIDS_FEMALE("Niñas"),
    BABY("Bebé")
}

enum class Season(val displayName: String) {
    SPRING_SUMMER("Primavera/Verano"),
    FALL_WINTER("Otoño/Invierno"),
    ALL_YEAR("Todo el año"),
    SPRING("Primavera"),
    SUMMER("Verano"),
    FALL("Otoño"),
    WINTER("Invierno")
}

enum class ClothingMaterial(val displayName: String, val isNatural: Boolean) {
    COTTON("Algodón", true),
    POLYESTER("Poliéster", false),
    COTTON_POLYESTER("Algodón/Poliéster", false),
    WOOL("Lana", true),
    SILK("Seda", true),
    LINEN("Lino", true),
    DENIM("Denim", false),
    LEATHER("Cuero", true),
    SYNTHETIC_LEATHER("Cuero Sintético", false),
    LYCRA("Lycra", false),
    SPANDEX("Spandex", false),
    MODAL("Modal", false),
    BAMBOO("Bambú", true),
    VISCOSE("Viscosa", false),
    NYLON("Nylon", false),
    FLEECE("Polar", false),
    MESH("Malla", false),
    JERSEY("Jersey", false),
    RIB("Rib", false)
}

enum class ClothingSize(val displayName: String, val sortOrder: Int) {
    // Standard sizes
    XXS("XXS", 1),
    XS("XS", 2),
    S("S", 3),
    M("M", 4),
    L("L", 5),
    XL("XL", 6),
    XXL("XXL", 7),
    XXXL("XXXL", 8),
    
    // Numeric sizes (shirts, pants, etc.)
    SIZE_36("36", 10),
    SIZE_38("38", 11),
    SIZE_40("40", 12),
    SIZE_42("42", 13),
    SIZE_44("44", 14),
    SIZE_46("46", 15),
    SIZE_48("48", 16),
    SIZE_50("50", 17),
    SIZE_52("52", 18),
    SIZE_54("54", 19),
    
    // Shoes
    SHOE_35("35", 20),
    SHOE_36("36", 21),
    SHOE_37("37", 22),
    SHOE_38("38", 23),
    SHOE_39("39", 24),
    SHOE_40("40", 25),
    SHOE_41("41", 26),
    SHOE_42("42", 27),
    SHOE_43("43", 28),
    SHOE_44("44", 29),
    SHOE_45("45", 30),
    
    // Kids
    KIDS_2("2 años", 40),
    KIDS_4("4 años", 41),
    KIDS_6("6 años", 42),
    KIDS_8("8 años", 43),
    KIDS_10("10 años", 44),
    KIDS_12("12 años", 45),
    KIDS_14("14 años", 46),
    KIDS_16("16 años", 47),
    
    // Generic/Custom
    ONE_SIZE("Talle único", 50),
    CUSTOM("Personalizado", 99)
}

enum class ClothingColor(val displayName: String, val hexColor: String) {
    WHITE("Blanco", "#FFFFFF"),
    BLACK("Negro", "#000000"),
    GRAY("Gris", "#808080"),
    NAVY("Azul Marino", "#000080"),
    BLUE("Azul", "#0000FF"),
    LIGHT_BLUE("Celeste", "#87CEEB"),
    RED("Rojo", "#FF0000"),
    PINK("Rosa", "#FFC0CB"),
    GREEN("Verde", "#008000"),
    YELLOW("Amarillo", "#FFFF00"),
    ORANGE("Naranja", "#FFA500"),
    PURPLE("Violeta", "#800080"),
    BROWN("Marrón", "#A52A2A"),
    BEIGE("Beige", "#F5F5DC"),
    CREAM("Crema", "#FFFDD0"),
    GOLD("Dorado", "#FFD700"),
    SILVER("Plateado", "#C0C0C0"),
    MULTICOLOR("Multicolor", "#000000")
}

enum class AgeGroup(val displayName: String) {
    ADULT("Adulto"),
    TEEN("Adolescente (13-17)"),
    CHILD("Niños (3-12)"),
    TODDLER("Niños pequeños (1-3)"),
    BABY("Bebé (0-12 meses)")
}

enum class ClothingStyle(val displayName: String) {
    CASUAL("Casual"),
    FORMAL("Formal"),
    SPORTY("Deportivo"),
    ELEGANT("Elegante"),
    TRENDY("Moderno"),
    CLASSIC("Clásico"),
    VINTAGE("Vintage"),
    BOHEMIAN("Bohemio"),
    MINIMALIST("Minimalista"),
    STREETWEAR("Urbano")
}

enum class ClothingOccasion(val displayName: String) {
    DAILY("Uso diario"),
    WORK("Trabajo"),
    PARTY("Fiesta"),
    WEDDING("Casamiento"),
    SPORT("Deporte"),
    BEACH("Playa"),
    NIGHT("Nocturno"),
    FORMAL_EVENT("Evento formal"),
    CASUAL_EVENT("Evento casual"),
    VACATION("Vacaciones")
}

enum class CareInstruction(val displayName: String, val icon: String) {
    MACHINE_WASH_COLD("Lavar en máquina agua fría", "🌡️"),
    MACHINE_WASH_WARM("Lavar en máquina agua tibia", "🌡️"),
    HAND_WASH("Lavar a mano", "👐"),
    DRY_CLEAN("Lavado en seco", "🏷️"),
    DO_NOT_BLEACH("No usar blanqueador", "❌"),
    TUMBLE_DRY_LOW("Secar en secarropa baja temperatura", "🔥"),
    AIR_DRY("Secar al aire", "💨"),
    IRON_LOW("Planchar temperatura baja", "🔥"),
    IRON_MEDIUM("Planchar temperatura media", "🔥"),
    IRON_HIGH("Planchar temperatura alta", "🔥"),
    DO_NOT_IRON("No planchar", "❌")
}

// === HELPER DATA CLASSES ===
@Parcelize
data class ProductVariant(
    val id: String,
    val size: ClothingSize,
    val color: ClothingColor,
    val sku: String? = null,
    val price: Double? = null, // Override base price if needed
    val stock: Int,
    val images: List<String> = emptyList()
) : Parcelable

@Parcelize
data class ProductFilter(
    val categories: List<ClothingCategory> = emptyList(),
    val subcategories: List<String> = emptyList(),
    val genders: List<ClothingGender> = emptyList(),
    val sizes: List<ClothingSize> = emptyList(),
    val colors: List<ClothingColor> = emptyList(),
    val materials: List<ClothingMaterial> = emptyList(),
    val priceRange: PriceRange? = null,
    val location: String? = null,
    val inStock: Boolean? = null,
    val seasons: List<Season> = emptyList(),
    val styles: List<ClothingStyle> = emptyList(),
    val sortBy: ProductSortBy = ProductSortBy.RELEVANCE
) : Parcelable

@Parcelize
data class PriceRange(
    val min: Double,
    val max: Double
) : Parcelable

enum class ProductSortBy(val displayName: String) {
    RELEVANCE("Relevancia"),
    NEWEST("Más recientes"),
    OLDEST("Más antiguos"),
    PRICE_LOW_HIGH("Precio: menor a mayor"),
    PRICE_HIGH_LOW("Precio: mayor a menor"),
    NAME_A_Z("Nombre: A-Z"),
    NAME_Z_A("Nombre: Z-A"),
    MOST_VIEWED("Más vistos"),
    MOST_FAVORITED("Más favoritos"),
    BEST_SELLER("Más vendidos")
}

// === VALIDATION AND BUSINESS LOGIC ===
object ProductValidation {
    
    fun validateBasicInfo(basicInfo: ProductBasicInfo): List<String> {
        val errors = mutableListOf<String>()
        
        if (basicInfo.name.isBlank()) {
            errors.add("El nombre del producto es obligatorio")
        } else if (basicInfo.name.length < 5) {
            errors.add("El nombre debe tener al menos 5 caracteres")
        } else if (basicInfo.name.length > 100) {
            errors.add("El nombre no puede tener más de 100 caracteres")
        }
        
        if (basicInfo.description.isBlank()) {
            errors.add("La descripción es obligatoria")
        } else if (basicInfo.description.length < 20) {
            errors.add("La descripción debe tener al menos 20 caracteres")
        } else if (basicInfo.description.length > 2000) {
            errors.add("La descripción no puede tener más de 2000 caracteres")
        }
        
        return errors
    }
    
    fun validatePricing(pricingInfo: PricingInfo): List<String> {
        val errors = mutableListOf<String>()
        
        if (pricingInfo.basePrice <= 0) {
            errors.add("El precio debe ser mayor a 0")
        }
        
        if (pricingInfo.minimumQuantity < 1) {
            errors.add("La cantidad mínima debe ser al menos 1")
        }
        
        pricingInfo.maximumQuantity?.let { max ->
            if (max < pricingInfo.minimumQuantity) {
                errors.add("La cantidad máxima no puede ser menor a la mínima")
            }
        }
        
        // Validate bulk pricing rules
        pricingInfo.bulkPricing.forEach { rule ->
            if (rule.minimumQuantity < pricingInfo.minimumQuantity) {
                errors.add("Las reglas de precio por volumen deben tener cantidad mínima >= ${pricingInfo.minimumQuantity}")
            }
            
            if (rule.pricePerUnit <= 0) {
                errors.add("El precio por unidad en reglas de volumen debe ser mayor a 0")
            }
        }
        
        return errors
    }
    
    fun validateInventory(inventory: InventoryInfo): List<String> {
        val errors = mutableListOf<String>()
        
        if (inventory.totalStock < 0) {
            errors.add("El stock total no puede ser negativo")
        }
        
        if (inventory.reservedStock < 0) {
            errors.add("El stock reservado no puede ser negativo")
        }
        
        if (inventory.reservedStock > inventory.totalStock) {
            errors.add("El stock reservado no puede ser mayor al stock total")
        }
        
        inventory.lowStockThreshold?.let { threshold ->
            if (threshold < 0) {
                errors.add("El umbral de stock bajo no puede ser negativo")
            }
        }
        
        return errors
    }
    
    fun validateImages(images: List<String>): List<String> {
        val errors = mutableListOf<String>()
        
        if (images.isEmpty()) {
            errors.add("Debe agregar al menos una imagen del producto")
        }
        
        if (images.size > 10) {
            errors.add("No puede agregar más de 10 imágenes")
        }
        
        return errors
    }
    
    fun validateProduct(product: Product): List<String> {
        val errors = mutableListOf<String>()
        
        errors.addAll(validateBasicInfo(product.basicInfo))
        errors.addAll(validatePricing(product.pricingInfo))
        errors.addAll(validateInventory(product.inventory))
        errors.addAll(validateImages(product.images))
        
        // Clothing-specific validations
        if (product.clothingDetails.sizes.isEmpty()) {
            errors.add("Debe seleccionar al menos un talle")
        }
        
        if (product.clothingDetails.colors.isEmpty()) {
            errors.add("Debe seleccionar al menos un color")
        }
        
        if (product.clothingDetails.materials.isEmpty()) {
            errors.add("Debe seleccionar al menos un material")
        }
        
        return errors
    }
}

// === EXTENSION FUNCTIONS ===
fun Product.isAvailable(): Boolean {
    return status == ProductStatus.ACTIVE && 
           inventory.availableStock > 0 &&
           visibility == ProductVisibility.PUBLIC
}

fun Product.canBePurchased(quantity: Int): Boolean {
    return isAvailable() && 
           quantity >= pricingInfo.minimumQuantity &&
           (pricingInfo.maximumQuantity == null || quantity <= pricingInfo.maximumQuantity!!) &&
           (inventory.trackInventory.not() || inventory.availableStock >= quantity)
}

fun Product.getPriceForQuantity(quantity: Int): Double {
    val applicableRule = pricingInfo.bulkPricing
        .filter { it.minimumQuantity <= quantity }
        .maxByOrNull { it.minimumQuantity }
    
    return applicableRule?.pricePerUnit ?: pricingInfo.basePrice
}

fun Product.isLowStock(): Boolean {
    val threshold = inventory.lowStockThreshold ?: 5
    return inventory.availableStock <= threshold
}

fun Product.getDisplayPrice(): String {
    val basePrice = pricingInfo.basePrice
    val currency = when (pricingInfo.currency) {
        "ARS" -> "$"
        "USD" -> "USD "
        else -> "${pricingInfo.currency} "
    }
    
    return "$currency${basePrice.toInt()}"
}

fun Product.getDisplayPriceRange(): String? {
    if (pricingInfo.bulkPricing.isEmpty()) return null
    
    val minPrice = pricingInfo.bulkPricing.minOf { it.pricePerUnit }
    val maxPrice = pricingInfo.basePrice
    
    val currency = when (pricingInfo.currency) {
        "ARS" -> "$"
        "USD" -> "USD "
        else -> "${pricingInfo.currency} "
    }
    
    return "$currency${minPrice.toInt()} - $currency${maxPrice.toInt()}"
}