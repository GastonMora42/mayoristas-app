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

// ✅ Ahora incluimos todos los módulos para que Gradle pueda encontrarlos
include(":app")
include(":core:common")
include(":core:network") 
include(":core:database")
include(":core:ui")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")
