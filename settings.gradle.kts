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

// Solo incluir el módulo principal por ahora
include(":app")

// TEMPORALMENTE COMENTAMOS LOS OTROS MÓDULOS HASTA QUE ESTÉN FUNCIONANDO
// Core modules
// include(":core:common")
// include(":core:network") 
// include(":core:database")
// include(":core:ui")

// Feature modules
// include(":feature:auth:domain")
// include(":feature:auth:data")
// include(":feature:auth:presentation")