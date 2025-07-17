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

// Core modules (foundation layer)
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:domain")
include(":core:ui")
include(":core:testing")

// AI-specific modules
include(":ai:modelmanager")
include(":ai:inference")
include(":ai:rag")

// Feature modules (UI layer)
include(":feature:chat")
include(":feature:courses")
include(":feature:camera")
include(":feature:progress")
include(":feature:settings")

// Check Java version compatibility
check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Eliza requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
