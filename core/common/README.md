# Core Common Module

The `core:common` module contains essential utility classes, extension functions, and base components that are shared across the entire Eliza application. It serves as a foundational layer, providing common functionalities that are not specific to any particular feature or domain.

## Responsibilities

- **Shared Utilities:** Provides common utility classes and functions, such as the `VideoThumbnailGenerator`, that can be reused in different parts of the application.
- **Extension Functions:** Contains extension functions that simplify common Android and Kotlin development tasks.
- **Base Classes:** May include base classes for common architectural components, such as ViewModels or Repositories, although none are present at the moment.

## Dependencies

This module is designed to be a foundational layer and has minimal dependencies on other modules within the project. It primarily depends on:

- **Kotlin Standard Library**
- **Android Core Libraries (androidx.core.ktx)**
- **Coroutines** for asynchronous operations.

## Usage

To use the components and utilities from this module, simply add it as a dependency in the `build.gradle.kts` file of the consuming module:

```kotlin
dependencies {
    implementation(projects.core.common)
}
```

This will make the shared utilities, like `VideoThumbnailGenerator`, available for use in the consuming module.
