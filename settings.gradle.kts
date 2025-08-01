pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Mayoristas"

// SOLO EL MÓDULO PRINCIPAL POR AHORA
include(":app")

// COMENTAMOS LOS OTROS MÓDULOS HASTA CREARLOS FÍSICAMENTE
// include(":core:common")
// include(":core:network") 
// include(":core:database")
// include(":core:ui")
// include(":feature:auth:domain")
// include(":feature:auth:data")
// include(":feature:auth:presentation")