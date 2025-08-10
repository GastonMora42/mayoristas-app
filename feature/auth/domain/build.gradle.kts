plugins {
    alias(libs.plugins.android.library)  // ✅ Cambiado a android.library
    alias(libs.plugins.kotlin.android)   // ✅ Cambiado a kotlin.android
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mayoristas.feature.auth.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Dependencia al módulo de common
    implementation(project(":core:common"))

    // Dependencias básicas de Kotlin y Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Dependencias para Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Dependencias de testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}