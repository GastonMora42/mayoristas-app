//feature/auth/data/build.gradle.kt

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mayoristas.feature.auth.data"
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
    // Dependencia al módulo de common y al módulo de dominio
    implementation(project(":core:common"))
    implementation(project(":feature:auth:domain"))
    
    // Dependencias de Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    
    // Dependencias de Datastore y biometría
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)
    implementation(libs.biometric)
    
    // Dependencias para Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Dependencias de testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
