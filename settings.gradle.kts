
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
ect.name = "Mayoristas"

include(":app")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:ui")

// Feature modules
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")

include(":feature:seller-ecosystem:domain")
include(":feature:seller-ecosystem:data")
include(":feature:seller-ecosystem:presentation")

include(":feature:client-ecosystem:domain")
include(":feature:client-ecosystem:data")
include(":feature:client-ecosystem:presentation")

include(":feature:subscriptions:domain")
include(":feature:subscriptions:data")
include(":feature:subscriptions:presentation")
