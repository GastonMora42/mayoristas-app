// feature/products/presentation/src/main/kotlin/com/mayoristas/feature/products/presentation/screens/AddEditProductScreen.kt

package com.mayoristas.feature.products.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mayoristas.feature.auth.domain.model.ClothingCategory
import com.mayoristas.feature.products.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String? = null, // null for add, productId for edit
    onNavigateBack: () -> Unit,
    onProductSaved: (String) -> Unit, // Return productId when saved
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val productState by viewModel.productState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val isEditMode = productId != null
    
    // Load product if editing
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        }
    }
    
    // Handle save success
    LaunchedEffect(uiState.savedProductId) {
        uiState.savedProductId?.let { savedId ->
            onProductSaved(savedId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        ProductTopBar(
            title = if (isEditMode) "Editar Producto" else "Nuevo Producto",
            onNavigateBack = onNavigateBack,
            onSave = { viewModel.saveProduct() },
            isSaving = productState.isSaving,
            canSave = productState.isValidForSave
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Progress indicator
            if (!isEditMode) {
                ProductCreationProgress(
                    progress = uiState.completionProgress
                )
            }
            
            // Form sections
            ProductImagesSection(
                images = productState.product.images,
                onAddImage = viewModel::addImage,
                onRemoveImage = viewModel::removeImage,
                onReorderImages = viewModel::reorderImages,
                error = productState.imagesError
            )
            
            BasicInfoSection(
                basicInfo = productState.product.basicInfo,
                viewModel = viewModel,
                errors = productState.basicInfoErrors
            )
            
            ClothingDetailsSection(
                clothingDetails = productState.product.clothingDetails,
                viewModel = viewModel,
                errors = productState.clothingDetailsErrors
            )
            
            PricingSection(
                pricingInfo = productState.product.pricingInfo,
                viewModel = viewModel,
                errors = productState.pricingErrors
            )
            
            InventorySection(
                inventory = productState.product.inventory,
                viewModel = viewModel,
                errors = productState.inventoryErrors
            )
            
            AdvancedOptionsSection(
                product = productState.product,
                viewModel = viewModel
            )
            
            // Save Button (Mobile-friendly)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Button(
                    onClick = { viewModel.saveProduct() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !productState.isSaving && productState.isValidForSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    if (productState.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        if (isEditMode) "Guardar Cambios" else "Publicar Producto",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Error Dialog
    if (productState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(productState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Entendido")
                }
            }
        )
    }
    
    // Success Dialog
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text("¡Producto guardado!")
                }
            },
            text = { 
                Text(
                    if (isEditMode) "Los cambios se guardaron correctamente"
                    else "Tu producto se publicó exitosamente y ya está visible para los compradores"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.dismissSuccessDialog()
                        onNavigateBack()
                    }
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    canSave: Boolean
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = !isSaving && canSave
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ProductCreationProgress(progress: Float) {
    if (progress > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Progreso de creación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ProductImagesSection(
    images: List<String>,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onReorderImages: (Int, Int) -> Unit,
    error: String?
) {
    ProductSection(
        title = "Fotos del Producto",
        icon = Icons.Default.PhotoCamera,
        subtitle = "Agregá hasta 10 fotos. La primera será la imagen principal.",
        error = error
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Add image button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onAddImage() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(
                    2.dp, 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        if (images.isEmpty()) "Agregar primera foto" else "Agregar más fotos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        "${images.size}/10",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Images grid
            if (images.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height((images.size / 3 + 1) * 120.dp)
                ) {
                    items(images.size) { index ->
                        ImageCard(
                            imageUrl = images[index],
                            isMain = index == 0,
                            onRemove = { onRemoveImage(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCard(
    imageUrl: String,
    isMain: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen del producto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Main image badge
            if (isMain) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Principal",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicInfoSection(
    basicInfo: ProductBasicInfo,
    viewModel: AddEditProductViewModel,
    errors: Map<String, String>
) {
    val focusManager = LocalFocusManager.current
    
    ProductSection(
        title = "Información Básica",
        icon = Icons.Default.Info,
        subtitle = "Información principal del producto"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = basicInfo.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Nombre del producto *") },
                placeholder = { Text("Ej: Remera Básica Algodón") },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = errors.containsKey("name"),
                supportingText = errors["name"]?.let { { Text(it) } }
            )
            
            // Category selection
            var categoryExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = basicInfo.category.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Categoría *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = errors.containsKey("category")
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ClothingCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                viewModel.onCategoryChanged(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Subcategory (if category has subcategories)
            if (basicInfo.category.subcategories.isNotEmpty()) {
                var subcategoryExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = basicInfo.subcategory ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Subcategoría") },
                        leadingIcon = { Icon(Icons.Default.Subdirectory, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        basicInfo.category.subcategories.forEach { subcategory ->
                            DropdownMenuItem(
                                text = { Text(subcategory) },
                                onClick = {
                                    viewModel.onSubcategoryChanged(subcategory)
                                    subcategoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = basicInfo.brand ?: "",
                    onValueChange = viewModel::onBrandChanged,
                    label = { Text("Marca") },
                    placeholder = { Text("Ej: Nike") },
                    leadingIcon = { Icon(Icons.Default.BusinessCenter, null) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = basicInfo.model ?: "",
                    onValueChange = viewModel::onModelChanged,
                    label = { Text("Modelo") },
                    placeholder = { Text("Ej: Air Max") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = basicInfo.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Descripción detallada *") },
                placeholder = { Text("Describí las características, materiales, cuidados, etc.") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                isError = errors.containsKey("description"),
                supportingText = errors["description"]?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = basicInfo.shortDescription ?: "",
                onValueChange = viewModel::onShortDescriptionChanged,
                label = { Text("Descripción corta") },
                placeholder = { Text("Resumen en una línea") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ClothingDetailsSection(
    clothingDetails: ClothingDetails,
    viewModel: AddEditProductViewModel,
    errors: Map<String, String>
) {
    ProductSection(
        title = "Detalles de la Prenda",
        icon = Icons.Default.Checkroom,
        subtitle = "Características específicas del producto de ropa"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Gender selection
            GenderSelectionRow(
                selectedGender = clothingDetails.gender,
                onGenderSelected = viewModel::onGenderChanged
            )
            
            // Season selection
            SeasonSelectionRow(
                selectedSeason = clothingDetails.season,
                onSeasonSelected = viewModel::onSeasonChanged
            )
            
            // Sizes selection
            SizeSelectionGrid(
                selectedSizes = clothingDetails.sizes,
                onSizeToggled = viewModel::onSizeToggled,
                error = errors["sizes"]
            )
            
            // Colors selection
            ColorSelectionGrid(
                selectedColors = clothingDetails.colors,
                onColorToggled = viewModel::onColorToggled,
                error = errors["colors"]
            )
            
            // Materials selection
            MaterialSelectionGrid(
                selectedMaterials = clothingDetails.materials,
                onMaterialToggled = viewModel::onMaterialToggled,
                error = errors["materials"]
            )
        }
    }
}

@Composable
private fun GenderSelectionRow(
    selectedGender: ClothingGender,
    onGenderSelected: (ClothingGender) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Género *",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ClothingGender.values()) { gender ->
                FilterChip(
                    selected = selectedGender == gender,
                    onClick = { onGenderSelected(gender) },
                    label = { Text(gender.displayName) }
                )
            }
        }
    }
}

@Composable
private fun SeasonSelectionRow(
    selectedSeason: Season,
    onSeasonSelected: (Season) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Temporada *",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Season.values()) { season ->
                FilterChip(
                    selected = selectedSeason == season,
                    onClick = { onSeasonSelected(season) },
                    label = { Text(season.displayName) }
                )
            }
        }
    }
}

@Composable
private fun SizeSelectionGrid(
    selectedSizes: List<ClothingSize>,
    onSizeToggled: (ClothingSize, Boolean) -> Unit,
    error: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Talles disponibles *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (error != null) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "${selectedSizes.size} seleccionados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(ClothingSize.values().sortedBy { it.sortOrder }.take(16)) { size ->
                FilterChip(
                    selected = selectedSizes.contains(size),
                    onClick = { onSizeToggled(size, !selectedSizes.contains(size)) },
                    label = { 
                        Text(
                            size.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ColorSelectionGrid(
    selectedColors: List<ClothingColor>,
    onColorToggled: (ClothingColor, Boolean) -> Unit,
    error: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Colores disponibles *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (error != null) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "${selectedColors.size} seleccionados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(240.dp)
        ) {
            items(ClothingColor.values().toList()) { color ->
                ColorChip(
                    color = color,
                    isSelected = selectedColors.contains(color),
                    onToggle = { onColorToggled(color, !selectedColors.contains(color)) }
                )
            }
        }
    }
}

@Composable
private fun ColorChip(
    color: ClothingColor,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Color circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(android.graphics.Color.parseColor(color.hexColor)))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(50)
                    )
            )
            
            Text(
                color.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MaterialSelectionGrid(
    selectedMaterials: List<ClothingMaterial>,
    onMaterialToggled: (ClothingMaterial, Boolean) -> Unit,
    error: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Materiales *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (error != null) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "${selectedMaterials.size} seleccionados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(ClothingMaterial.values().toList()) { material ->
                MaterialChip(
                    material = material,
                    isSelected = selectedMaterials.contains(material),
                    onToggle = { onMaterialToggled(material, !selectedMaterials.contains(material)) }
                )
            }
        }
    }
}

@Composable
private fun MaterialChip(
    material: ClothingMaterial,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { 
            Column {
                Text(
                    material.displayName,
                    style = MaterialTheme.typography.bodySmall
                )
                if (material.isNatural) {
                    Text(
                        "Natural",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PricingSection(
    pricingInfo: PricingInfo,
    viewModel: AddEditProductViewModel,
    errors: Map<String, String>
) {
    ProductSection(
        title = "Precios y Cantidades",
        icon = Icons.Default.AttachMoney,
        subtitle = "Configurá precios mayoristas y cantidades mínimas"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = if (pricingInfo.basePrice > 0) pricingInfo.basePrice.toInt().toString() else "",
                    onValueChange = { viewModel.onBasePriceChanged(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Precio base *") },
                    placeholder = { Text("2500") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(2f),
                    isError = errors.containsKey("basePrice"),
                    supportingText = errors["basePrice"]?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = pricingInfo.minimumQuantity.toString(),
                    onValueChange = { viewModel.onMinimumQuantityChanged(it.toIntOrNull() ?: 1) },
                    label = { Text("Mín. *") },
                    placeholder = { Text("10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = errors.containsKey("minimumQuantity")
                )
            }
            
            // Price per unit selector
            var priceUnitExpanded by remember { mutableStateOf(false) }
            val priceUnits = listOf("por unidad", "por docena", "por pack de 6", "por pack de 12")
            
            ExposedDropdownMenuBox(
                expanded = priceUnitExpanded,
                onExpandedChange = { priceUnitExpanded = !priceUnitExpanded }
            ) {
                OutlinedTextField(
                    value = pricingInfo.pricePerUnit ?: "por unidad",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Precio por") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priceUnitExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = priceUnitExpanded,
                    onDismissRequest = { priceUnitExpanded = false }
                ) {
                    priceUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                viewModel.onPricePerUnitChanged(unit)
                                priceUnitExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Bulk pricing toggle
            var showBulkPricing by remember { mutableStateOf(pricingInfo.bulkPricing.isNotEmpty()) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Precios por volumen",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Switch(
                    checked = showBulkPricing,
                    onCheckedChange = { 
                        showBulkPricing = it
                        if (!it) {
                            viewModel.clearBulkPricing()
                        }
                    }
                )
            }
            
            AnimatedVisibility(visible = showBulkPricing) {
                BulkPricingSection(
                    bulkPricing = pricingInfo.bulkPricing,
                    onAddRule = viewModel::addBulkPriceRule,
                    onRemoveRule = viewModel::removeBulkPriceRule
                )
            }
        }
    }
}

@Composable
private fun BulkPricingSection(
    bulkPricing: List<BulkPriceRule>,
    onAddRule: (BulkPriceRule) -> Unit,
    onRemoveRule: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        bulkPricing.forEachIndexed { index, rule ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Desde ${rule.minimumQuantity} unidades",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        "$${rule.pricePerUnit.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { onRemoveRule(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        if (bulkPricing.size < 3) {
            TextButton(
                onClick = { 
                    // Show dialog to add new rule (simplified for demo)
                    val newRule = BulkPriceRule(
                        minimumQuantity = (bulkPricing.maxOfOrNull { it.minimumQuantity } ?: 10) + 10,
                        pricePerUnit = 2000.0,
                        discountPercentage = 10.0
                    )
                    onAddRule(newRule)
                }
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar descuento por volumen")
            }
        }
    }
}

@Composable
private fun InventorySection(
    inventory: InventoryInfo,
    viewModel: AddEditProductViewModel,
    errors: Map<String, String>
) {
    ProductSection(
        title = "Stock e Inventario",
        icon = Icons.Default.Inventory,
        subtitle = "Control de stock y disponibilidad"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = inventory.totalStock.toString(),
                    onValueChange = { viewModel.onTotalStockChanged(it.toIntOrNull() ?: 0) },
                    label = { Text("Stock total *") },
                    placeholder = { Text("100") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = errors.containsKey("totalStock"),
                    supportingText = errors["totalStock"]?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = inventory.lowStockThreshold?.toString() ?: "",
                    onValueChange = { viewModel.onLowStockThresholdChanged(it.toIntOrNull()) },
                    label = { Text("Alerta stock") },
                    placeholder = { Text("10") },
                    leadingIcon = { Icon(Icons.Default.Warning, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Seguir inventario",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Switch(
                    checked = inventory.trackInventory,
                    onCheckedChange = viewModel::onTrackInventoryChanged
                )
            }
            
            if (inventory.trackInventory) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Permitir pedidos sin stock",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Los clientes pueden pedir productos agotados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = inventory.allowBackorders,
                        onCheckedChange = viewModel::onAllowBackordersChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedOptionsSection(
    product: Product,
    viewModel: AddEditProductViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Opciones Avanzadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product status
                    var statusExpanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = product.status.displayName,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Estado del producto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            ProductStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = {
                                        viewModel.onStatusChanged(status)
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Tags input
                    OutlinedTextField(
                        value = product.basicInfo.tags.joinToString(", "),
                        onValueChange = { viewModel.onTagsChanged(it.split(",").map { tag -> tag.trim() }.filter { it.isNotEmpty() }) },
                        label = { Text("Etiquetas") },
                        placeholder = { Text("verano, casual, algodón") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductSection(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    error: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (error != null) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (error != null) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (error != null) {
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditProductScreenPreview() {
    MaterialTheme {
        AddEditProductScreen(
            productId = null,
            onNavigateBack = {},
            onProductSaved = {}
        )
    }
}