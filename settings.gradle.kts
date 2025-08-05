pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Eliza"

// Enable type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Main app module
include(":app")

// Core modules (foundation layer) - only include existing modules
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:network")

// AI-specific modules
include(":ai:modelmanager")
include(":ai:inference")
include(":ai:rag")
include(":ai:service")

// Feature modules (UI layer) - only include existing modules
include(":feature:home")
include(":feature:chat")
include(":feature:course-progress")
include(":feature:chapter")
include(":feature:course-suggestions")
include(":feature:settings")

// Check Java version compatibility
check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Eliza requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
