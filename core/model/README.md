# Core Model Module

The `core:model` module contains the data models for the Eliza application. These are the plain Kotlin data classes that represent the objects in our application, such as `Course`, `Chapter`, and `Exercise`.

## Responsibilities

- **Data Definitions:** Defines the data classes that are used throughout the application.
- **Data Integrity:** Ensures that the data models are consistent and that they accurately represent the application's data.
- **Serialization:** Includes annotations for serialization, allowing the data models to be easily converted to and from JSON.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`Serialization`:** For JSON serialization.

## Usage

To use the data models in your module, add a dependency on `core:model` in your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation(projects.core.model)
}
```

The data classes from this module can then be used to represent the data in your application.
