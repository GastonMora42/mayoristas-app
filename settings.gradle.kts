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

include(":app")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:ui")

// Feature modules - Auth only for now
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")