package com.mayoristas.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test Firebase setup
        testFirebaseConnection()
        
        setContent {
            MayoristasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun testFirebaseConnection() {
        try {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            
            Log.d("FIREBASE_TEST", "✅ Firebase Auth App: ${firebaseAuth.app.name}")
            Log.d("FIREBASE_TEST", "✅ Firestore App: ${firestore.app.name}")
            Log.d("FIREBASE_TEST", "✅ Firebase configurado correctamente!")
            
            // Test básico de conexión
            firestore.collection("test").document("connection")
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener {
                    Log.d("FIREBASE_TEST", "✅ Firestore write test successful!")
                }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE_TEST", "❌ Firestore write test failed: ${e.message}")
                }
                
        } catch (e: Exception) {
            Log.e("FIREBASE_TEST", "❌ Error Firebase setup: ${e.message}")
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🚀 ¡Bienvenido a Mayoristas.com!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "✅ Proyecto Android Configurado\n🔥 Firebase Integrado\n📱 Listo para Desarrollo",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
fun MayoristasTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MayoristasTheme {
        WelcomeScreen()
    }
}