# App Module

The `app` module is the main entry point of the Eliza application. It is responsible for assembling the various feature and core modules into a cohesive application.

## Responsibilities

- **Application Entry Point:** Contains the `MainActivity` and `ElizaApplication` classes, which are the main entry points of the application.
- **Navigation:** Defines the top-level navigation graph for the application using Jetpack Navigation.
- **Dependency Injection:** Sets up the Hilt dependency injection framework for the application.
- **UI Shell:** Provides the main UI shell for the application, including the top app bar and bottom navigation bar.

## Dependencies

This module depends on all the feature and core modules in the project, as well as a number of external libraries:

- **Feature Modules:** `:feature:home`, `:feature:course-progress`, `:feature:chapter`, `:feature:chat`, `:feature:course-suggestions`, `:feature:settings`
- **Core Modules:** `:core:common`, `:core:data`, `:core:model`, `:core:designsystem`
- **AI Modules:** `:ai:modelmanager`, `:ai:inference`, `:ai:rag`, `:ai:service`
- **Android Core:** `androidx.core.ktx`, `androidx.lifecycle.runtime.ktx`, `androidx.activity.compose`
- **Compose:** `androidx.compose.bom`, `androidx.ui`, `androidx.material3`
- **Navigation:** `androidx.navigation.compose`
- **Hilt:** For dependency injection.
- **WorkManager:** For background tasks.
- **DataStore:** For storing user preferences.

## Usage

This module is the main application module and is not meant to be used as a library. To run the application, build and install this module on an Android device or emulator.
