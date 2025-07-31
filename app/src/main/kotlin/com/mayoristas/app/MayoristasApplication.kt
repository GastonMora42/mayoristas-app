package com.mayoristas.app

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MayoristasApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 🔥 Inicializar Firebase explícitamente
        initializeFirebase()
        
        Log.d("MayoristasApp", "🚀 Application initialized successfully")
    }
    
    private fun initializeFirebase() {
        try {
            // Firebase se inicializa automáticamente, pero podemos forzar la inicialización
            Firebase.initialize(this)
            Log.d("Firebase", "🔥 Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "❌ Firebase initialization failed: ${e.message}")
        }
    }
}