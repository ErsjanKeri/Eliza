# Core Data Module

The `core:data` module is responsible for managing the application's data layer. It handles data retrieval from various sources, such as the network and the local database, and exposes it to the rest of the application through a clean and consistent API.

## Responsibilities

- **Repository Pattern:** Implements the repository pattern to abstract the data sources from the rest of the application.
- **Data Caching:** Provides a mechanism for caching data locally to improve performance and provide offline access.
- **Data Synchronization:** Manages the synchronization of data between the local database and the remote server.
- **Network Monitoring:** Includes a `NetworkMonitor` to provide information about the device's network connectivity.

## Dependencies

This module depends on several other modules in the project:

- **`:core:common`:** For shared utilities and base classes.
- **`:core:model`:** For the application's data models.
- **`:core:database`:** For accessing the local database.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous programming.
- **`Serialization`:** For parsing JSON data.

## Usage

To use the repositories and other components from this module, add it as a dependency in the `build.gradle.kts` file of the consuming module:

```kotlin
dependencies {
    implementation(projects.core.data)
}
```

The repositories can then be injected into your ViewModels or other components using Hilt.
