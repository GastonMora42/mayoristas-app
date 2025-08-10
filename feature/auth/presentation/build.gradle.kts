plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mayoristas.feature.auth.presentation"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":feature:auth:domain"))
    
    // ✅ COMPOSE BOM Y DEPENDENCIAS CORE
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.lifecycle)
    implementation(libs.androidx.compose.navigation)
    
    // ✅ LIFECYCLE Y VIEWMODEL
    implementation(libs.androidx.lifecycle.viewmodel)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") 

 
    // ✅ HILT
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    
    // ✅ COIL PARA IMÁGENES
    implementation(libs.coil)
    
    // ✅ MATERIAL ICONS EXTENDED
    implementation("androidx.compose.material:material-icons-extended:1.5.8")
    
    // ✅ ACTIVITY COMPOSE
    implementation("androidx.activity:activity-compose:1.8.2")
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}