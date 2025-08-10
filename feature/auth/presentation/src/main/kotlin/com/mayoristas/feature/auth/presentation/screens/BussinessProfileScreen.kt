// feature/auth/presentation/src/main/kotlin/com/mayoristas/feature/auth/presentation/screens/BusinessProfileScreen.kt

@file:OptIn(ExperimentalMaterial3Api::class)

package com.mayoristas.feature.auth.presentation.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mayoristas.feature.auth.domain.model.*
import com.mayoristas.feature.auth.presentation.viewmodel.BusinessProfileViewModel
import com.mayoristas.feature.auth.presentation.viewmodel.BusinessProfileState
import com.mayoristas.feature.auth.presentation.viewmodel.BusinessProfileUIState

@Composable
fun BusinessProfileScreen(
    onProfileCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    viewModel: BusinessProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onProfileCompleted()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        ProfileTopBar(
            title = if (isEditMode) "Editar Perfil" else "Completar Perfil",
            onNavigateBack = onNavigateBack,
            progress = uiState.completionProgress
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            if (!isEditMode) {
                ProfileHeader()
            }
            
            // Sections
            BusinessInfoSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            LocationSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            ClothingCategoriesSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            BusinessPhotosSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            BusinessHoursSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            SocialMediaSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            DeliveryOptionsSection(
                profileState = profileState,
                viewModel = viewModel
            )
            
            // Save Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !profileState.isSaving && profileState.isValidForSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (isEditMode) "Guardar Cambios" else "Completar Perfil",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Error Dialog
    if (profileState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(profileState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun ProfileTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    progress: Float
) {
    Column {
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (progress > 0) {
            LinearProgressIndicator(
                progress = { progress }, // ✅ Usar lambda para evitar API experimental
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProfileHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "¡Completá tu perfil empresarial!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Cuanto más completo esté tu perfil, más confianza generarás en tus potenciales clientes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BusinessInfoSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Información del Negocio",
        icon = Icons.Default.Business
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = profileState.profile.companyName ?: "",
                onValueChange = viewModel::onCompanyNameChanged,
                label = { Text("Nombre del negocio *") },
                placeholder = { Text("Ej: Mayorista Rosario SA") },
                leadingIcon = { Icon(Icons.Default.Storefront, null) },
                modifier = Modifier.fillMaxWidth(),
                isError = profileState.companyNameError != null,
                supportingText = profileState.companyNameError?.let { { Text(it) } }
            )
            
            // Business Type Dropdown
            var businessTypeExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = businessTypeExpanded,
                onExpandedChange = { businessTypeExpanded = !businessTypeExpanded }
            ) {
                OutlinedTextField(
                    value = profileState.profile.businessType?.let { getBusinessTypeDisplayName(it) } ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de negocio *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = businessTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = businessTypeExpanded,
                    onDismissRequest = { businessTypeExpanded = false }
                ) {
                    BusinessType.values().forEach { businessType ->
                        DropdownMenuItem(
                            text = { Text(getBusinessTypeDisplayName(businessType)) },
                            onClick = {
                                viewModel.onBusinessTypeChanged(businessType)
                                businessTypeExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = profileState.profile.taxId ?: "",
                onValueChange = viewModel::onTaxIdChanged,
                label = { Text("CUIT/CUIL *") },
                placeholder = { Text("20-12345678-9") },
                leadingIcon = { Icon(Icons.Default.Assignment, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = profileState.taxIdError != null,
                supportingText = profileState.taxIdError?.let { { Text(it) } }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = profileState.profile.phoneNumber ?: "",
                    onValueChange = viewModel::onPhoneNumberChanged,
                    label = { Text("Teléfono") },
                    placeholder = { Text("011-1234-5678") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = profileState.profile.whatsappNumber ?: "",
                    onValueChange = viewModel::onWhatsAppNumberChanged,
                    label = { Text("WhatsApp *") },
                    placeholder = { Text("1123456789") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    isError = profileState.whatsappError != null,
                    supportingText = profileState.whatsappError?.let { { Text(it) } }
                )
            }
            
            OutlinedTextField(
                value = profileState.profile.description ?: "",
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Descripción del negocio") },
                placeholder = { Text("Contanos sobre tu negocio, productos principales, años de experiencia...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Years in business
            OutlinedTextField(
                value = profileState.profile.yearsInBusiness?.toString() ?: "",
                onValueChange = { viewModel.onYearsInBusinessChanged(it.toIntOrNull()) },
                label = { Text("Años en el rubro") },
                placeholder = { Text("5") },
                leadingIcon = { Icon(Icons.Default.DateRange, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LocationSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Ubicación",
        icon = Icons.Default.LocationOn
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = profileState.profile.address?.street ?: "",
                onValueChange = viewModel::onStreetChanged,
                label = { Text("Calle *") },
                placeholder = { Text("Av. Corrientes") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                isError = profileState.addressError != null
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = profileState.profile.address?.streetNumber ?: "",
                    onValueChange = viewModel::onStreetNumberChanged,
                    label = { Text("Número *") },
                    placeholder = { Text("1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = profileState.profile.address?.neighborhood ?: "",
                    onValueChange = viewModel::onNeighborhoodChanged,
                    label = { Text("Barrio") },
                    placeholder = { Text("Centro") },
                    modifier = Modifier.weight(2f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = profileState.profile.address?.city ?: "",
                    onValueChange = viewModel::onCityChanged,
                    label = { Text("Ciudad *") },
                    placeholder = { Text("Buenos Aires") },
                    modifier = Modifier.weight(1f),
                    isError = profileState.cityError != null
                )
                
                OutlinedTextField(
                    value = profileState.profile.address?.state ?: "",
                    onValueChange = viewModel::onStateChanged,
                    label = { Text("Provincia *") },
                    placeholder = { Text("CABA") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = profileState.profile.address?.postalCode ?: "",
                onValueChange = viewModel::onPostalCodeChanged,
                label = { Text("Código Postal") },
                placeholder = { Text("C1043AAZ") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Google Places integration button (placeholder)
            OutlinedButton(
                onClick = { /* TODO: Implement Google Places */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar dirección en el mapa")
            }
        }
    }
}

@Composable
private fun ClothingCategoriesSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Categorías de Ropa",
        icon = Icons.Default.Category,
        subtitle = "Seleccioná las categorías que manejás en tu negocio"
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(400.dp) // Fixed height for grid
        ) {
            items(ClothingCategory.values().toList()) { category ->
                ClothingCategoryCard(
                    category = category,
                    isSelected = profileState.profile.clothingCategories.contains(category),
                    onSelectionChanged = { isSelected ->
                        viewModel.onClothingCategoryChanged(category, isSelected)
                    }
                )
            }
        }
    }
}

@Composable
private fun ClothingCategoryCard(
    category: ClothingCategory,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                getCategoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                category.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BusinessPhotosSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Fotos del Negocio",
        icon = Icons.Default.PhotoCamera,
        subtitle = "Agregá fotos de tu local, productos o equipo de trabajo"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Photo upload button
            OutlinedButton(
                onClick = { /* TODO: Implement photo picker */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Fotos")
            }
            
            // Photo grid
            if (profileState.profile.businessPhotos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profileState.profile.businessPhotos) { photoUrl ->
                        Card(
                            modifier = Modifier.size(120.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Foto del negocio",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessHoursSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Horarios de Atención",
        icon = Icons.Default.Schedule
    ) {
        // TODO: Implement business hours picker
        Text(
            "Configurá los horarios en que atendés consultas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedButton(
            onClick = { /* TODO: Implement time picker */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccessTime, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configurar Horarios")
        }
    }
}

@Composable
private fun SocialMediaSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Redes Sociales",
        icon = Icons.Default.Share,
        subtitle = "Enlaces a tus redes sociales y página web"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = profileState.profile.socialMedia?.instagram ?: "",
                onValueChange = viewModel::onInstagramChanged,
                label = { Text("Instagram") },
                placeholder = { Text("@tunegocio") },
                leadingIcon = { Icon(Icons.Default.PhotoCamera, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = profileState.profile.socialMedia?.facebook ?: "",
                onValueChange = viewModel::onFacebookChanged,
                label = { Text("Facebook") },
                placeholder = { Text("facebook.com/tunegocio") },
                leadingIcon = { Icon(Icons.Default.Share, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = profileState.profile.socialMedia?.website ?: "",
                onValueChange = viewModel::onWebsiteChanged,
                label = { Text("Página Web") },
                placeholder = { Text("www.tunegocio.com") },
                leadingIcon = { Icon(Icons.Default.Public, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeliveryOptionsSection(
    profileState: BusinessProfileState,
    viewModel: BusinessProfileViewModel
) {
    ProfileSection(
        title = "Opciones de Entrega",
        icon = Icons.Default.LocalShipping,
        subtitle = "¿Cómo entregás tus productos?"
    ) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeliveryOption.values().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            viewModel.onDeliveryOptionChanged(option, 
                                !profileState.profile.deliveryOptions.contains(option))
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = profileState.profile.deliveryOptions.contains(option),
                        onCheckedChange = { isChecked ->
                            viewModel.onDeliveryOptionChanged(option, isChecked)
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        option.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
            
            content()
        }
    }
}

// Helper functions
private fun getBusinessTypeDisplayName(businessType: BusinessType): String {
    return when (businessType) {
        BusinessType.MANUFACTURER -> "Fabricante"
        BusinessType.DISTRIBUTOR -> "Distribuidor"
        BusinessType.WHOLESALER -> "Mayorista"
        BusinessType.RETAILER -> "Minorista"
        BusinessType.AGENT -> "Representante/Agente"
        BusinessType.IMPORTER -> "Importador"
    }
}

private fun getCategoryIcon(category: ClothingCategory): ImageVector {
    return when (category) {
        ClothingCategory.WOMENS_CLOTHING -> Icons.Default.Person
        ClothingCategory.MENS_CLOTHING -> Icons.Default.Person
        ClothingCategory.KIDS_CLOTHING -> Icons.Default.ChildFriendly
        ClothingCategory.FOOTWEAR -> Icons.Default.Sports
        ClothingCategory.ACCESSORIES -> Icons.Default.AccessTime
        ClothingCategory.UNDERWEAR -> Icons.Default.Storefront
        ClothingCategory.SPORTSWEAR -> Icons.Default.FitnessCenter
        ClothingCategory.WORKWEAR -> Icons.Default.Work
    }
}

@Preview(showBackground = true)
@Composable
private fun BusinessProfileScreenPreview() {
    MaterialTheme {
        BusinessProfileScreen(
            onProfileCompleted = {},
            onNavigateBack = {},
            isEditMode = false
        )
    }
}