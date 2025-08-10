package com.mayoristas.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mayoristas.feature.auth.domain.model.BusinessType
import com.mayoristas.feature.auth.domain.model.UserType
import com.mayoristas.feature.auth.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    
    // Handle successful registration
    LaunchedEffect(uiState.authState) {
        if (uiState.authState is com.mayoristas.feature.auth.domain.model.AuthState.Success) {
            // Don't navigate immediately - show email verification dialog first
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header
            RegisterHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Registration Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Crear Cuenta Empresarial",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Únete a nuestra red de mayoristas y distribuidores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // User Type Selection
                    UserTypeSelector(
                        selectedUserType = registerState.userType,
                        onUserTypeSelected = viewModel::onUserTypeChanged
                    )
                    
                    // Personal Information
                    PersonalInfoSection(
                        registerState = registerState,
                        viewModel = viewModel,
                        focusManager = focusManager
                    )
                    
                    // Business Information
                    BusinessInfoSection(
                        registerState = registerState,
                        viewModel = viewModel,
                        focusManager = focusManager
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Register Button
                    Button(
                        onClick = { viewModel.register() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !registerState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (registerState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Crear Cuenta",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Terms and Privacy
                    Text(
                        text = "Al registrarte, aceptas nuestros Términos de Servicio y Política de Privacidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Error Dialog
    if (registerState.error != null || uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error de registro") },
            text = { 
                Text(
                    registerState.error ?: uiState.error ?: "Error desconocido"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearError() }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
    
    // Email Verification Dialog
    if (uiState.showEmailVerificationDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Cuenta creada exitosamente!") },
            text = { 
                Text("Se ha enviado un email de verificación a tu correo. Por favor verifica tu email antes de continuar.")
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.dismissEmailVerificationDialog()
                        onNavigateToLogin()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = "Business",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
        
        Text(
            "Registro Empresarial",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun UserTypeSelector(
    selectedUserType: UserType,
    onUserTypeSelected: (UserType) -> Unit
) {
    Column {
        Text(
            text = "Tipo de Usuario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserTypeCard(
                userType = UserType.SELLER,
                isSelected = selectedUserType == UserType.SELLER,
                onSelected = onUserTypeSelected,
                title = "Vendedor",
                description = "Vendo productos al por mayor",
                icon = Icons.Default.Storefront,
                modifier = Modifier.weight(1f)
            )
            
            UserTypeCard(
                userType = UserType.CLIENT,
                isSelected = selectedUserType == UserType.CLIENT,
                onSelected = onUserTypeSelected,
                title = "Comprador",
                description = "Compro productos para mi negocio",
                icon = Icons.Default.ShoppingCart,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UserTypeCard(
    userType: UserType,
    isSelected: Boolean,
    onSelected: (UserType) -> Unit,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = { onSelected(userType) },
                role = Role.RadioButton
            ),
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonalInfoSection(
    registerState: com.mayoristas.feature.auth.presentation.viewmodel.RegisterState,
    viewModel: AuthViewModel,
    focusManager: androidx.compose.ui.platform.FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        OutlinedTextField(
            value = registerState.displayName,
            onValueChange = viewModel::onDisplayNameChanged,
            label = { Text("Nombre completo") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Nombre")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = registerState.displayNameError != null,
            supportingText = registerState.displayNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = registerState.email,
            onValueChange = viewModel::onRegisterEmailChanged,
            label = { Text("Email empresarial") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = registerState.emailError != null,
            supportingText = registerState.emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        var passwordVisible by remember { mutableStateOf(false) }
        
        OutlinedTextField(
            value = registerState.password,
            onValueChange = viewModel::onRegisterPasswordChanged,
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Contraseña")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff 
                                     else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" 
                                           else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None 
                                 else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = registerState.passwordError != null,
            supportingText = registerState.passwordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun BusinessInfoSection(
    registerState: com.mayoristas.feature.auth.presentation.viewmodel.RegisterState,
    viewModel: AuthViewModel,
    focusManager: androidx.compose.ui.platform.FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Información Empresarial",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        OutlinedTextField(
            value = registerState.profile.companyName ?: "",
            onValueChange = viewModel::onCompanyNameChanged,
            label = { Text("Nombre de la empresa") },
            leadingIcon = {
                Icon(Icons.Default.Business, contentDescription = "Empresa")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = registerState.companyNameError != null,
            supportingText = registerState.companyNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Business Type Dropdown
        var businessTypeExpanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = businessTypeExpanded,
            onExpandedChange = { businessTypeExpanded = !businessTypeExpanded }
        ) {
            OutlinedTextField(
                value = registerState.profile.businessType?.let { getBusinessTypeDisplayName(it) } ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Tipo de negocio") },
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = "Tipo de negocio")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = businessTypeExpanded)
                },
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
            value = registerState.profile.taxId ?: "",
            onValueChange = viewModel::onTaxIdChanged,
            label = { Text("CUIT/RUT") },
            leadingIcon = {
                Icon(Icons.Default.Badge, contentDescription = "CUIT/RUT")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = registerState.taxIdError != null,
            supportingText = registerState.taxIdError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("20-12345678-9") }
        )
    }
}

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

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            onNavigateToHome = {}
        )
    }
}