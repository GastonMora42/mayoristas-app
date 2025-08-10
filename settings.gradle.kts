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

// ✅ Módulos principales
include(":app")
include(":core:common")
include(":core:network") 
include(":core:database")
include(":core:ui")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")

// ✅ Módulos de productos (crear temporalmente)
include(":feature:products:domain")
include(":feature:products:data")
include(":feature:products:presentation")