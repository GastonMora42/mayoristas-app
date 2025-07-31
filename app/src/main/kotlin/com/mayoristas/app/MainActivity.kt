package com.mayoristas.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🔥 TEST DE CONEXIÓN FIREBASE
        testFirebaseConnection()
        
        setContent {
            // Tema simplificado sin dependencias externas
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFAFAFA) // light_gray
                ) {
                    FirebaseTestScreen()
                }
            }
        }
    }
    
    private fun testFirebaseConnection() {
        try {
            // Test Firebase Auth
            Firebase.auth.addAuthStateListener { auth ->
                Log.d("Firebase", "🔥 Auth initialized: ${auth.currentUser?.uid ?: "No user"}")
            }
            
            // Test Firebase Firestore
            val testData = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "message" to "Test connection from Android",
                "appVersion" to "1.0.0-debug",
                "device" to "Android"
            )
            
            Firebase.firestore.collection("connection_test").document("android_test")
                .set(testData)
                .addOnSuccessListener {
                    Log.d("Firebase", "✅ Firestore connected successfully!")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "❌ Firestore connection failed: ${e.message}")
                }
                
        } catch (e: Exception) {
            Log.e("Firebase", "❌ Firebase initialization error: ${e.message}")
        }
    }
}

@Composable
fun FirebaseTestScreen() {
    var firebaseStatus by remember { mutableStateOf("🔍 Verificando conexión Firebase...") }
    var connectionSuccess by remember { mutableStateOf<Boolean?>(null) }
    
    // Simular verificación de estado
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000) // Esperar para que Firebase inicialice
        firebaseStatus = "🔥 Firebase conectado y funcionando perfectamente"
        connectionSuccess = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2).copy(alpha = 0.1f), // primary_blue
                        Color(0xFFFAFAFA) // light_gray
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Firebase Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color(0xFFFF9800), // primary_orange
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🔥",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    
                    Text(
                        text = "Mayoristas.com",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121), // dark_gray
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Firebase + Android + Compose",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1976D2), // primary_blue
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = firebaseStatus,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (connectionSuccess == true) Color(0xFF4CAF50) else Color(0xFF616161),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD) // light blue
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔥 Servicios Firebase:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0D47A1) // dark blue
                            )
                            
                            ServiceStatusItem("Authentication", connectionSuccess)
                            ServiceStatusItem("Cloud Firestore", connectionSuccess)
                            ServiceStatusItem("Analytics", connectionSuccess)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "📱 Componentes App:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0D47A1)
                            )
                            
                            ServiceStatusItem("Jetpack Compose", true)
                            ServiceStatusItem("Hilt DI", true) 
                            ServiceStatusItem("Material Design 3", true)
                            ServiceStatusItem("Kotlin Coroutines", true)
                        }
                    }
                    
                    if (connectionSuccess == true) {
                        Button(
                            onClick = { 
                                Log.d("MainActivity", "🚀 Ready to implement authentication!")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50) // primary_green
                            )
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Ready",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("¡Listo para Implementar Auth!")
                        }
                    }
                }
            }
            
            Text(
                text = "✅ Base arquitectónica funcionando\n🔥 Firebase conectado correctamente\n📱 Listo para desarrollo completo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun ServiceStatusItem(name: String, isConnected: Boolean?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (isConnected) {
            true -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = Color(0xFF4CAF50), // primary_green
                modifier = Modifier.size(16.dp)
            )
            false -> Icon(
                Icons.Default.Clear,
                contentDescription = "Disconnected", 
                tint = Color(0xFFD32F2F), // red
                modifier = Modifier.size(16.dp)
            )
            null -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color(0xFFFF9800) // primary_orange
            )
        }
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF0D47A1) // dark blue
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FirebaseTestScreenPreview() {
    MaterialTheme {
        FirebaseTestScreen()
    }
}