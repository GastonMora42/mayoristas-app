// ✅ Ahora es un módulo puro de Kotlin, sin dependencias de Android
plugins {
    alias(libs.plugins.kotlin.jvm) // Usamos el plugin de Kotlin JVM
    alias(libs.plugins.hilt) // Mantenemos Hilt, pero solo para su configuración básica
}

dependencies {
    // Dependencia al módulo de common, que también debería ser puro Kotlin
    implementation(project(":core:common"))

    // Dependencias básicas de Kotlin y Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Dependencias para Hilt
    // Solo necesitamos el compilador de Hilt aquí para procesar anotaciones,
    // pero no el Hilt de Android, ya que este no es un módulo de Android
    implementation(libs.hilt.android) // Nota: Esta dependencia a 'hilt.android' se mantiene si usas anotaciones como @Singleton
    kapt(libs.hilt.compiler)
    
    // Dependencias de testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
