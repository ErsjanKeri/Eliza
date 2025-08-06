# Core Design System Module

The `core:designsystem` module contains the visual building blocks of the Eliza application. It defines the app's theme, colors, typography, and custom UI components, ensuring a consistent and polished user experience across all features.

## Responsibilities

- **Theming:** Defines the application's theme, including the color palette, typography, and shapes.
- **Custom Components:** Provides a library of custom, reusable Jetpack Compose components that are used throughout the application.
- **Icons and Graphics:** Contains the custom icons and other graphical assets that are part of the app's design language.
- **Styling:** Defines the styles and attributes that are used to create the app's user interface.

## Dependencies

This module has the following dependencies:

- **Jetpack Compose:** For building the UI components.
- **Coil:** For image loading.
- **Richtext UI Material3:** For rendering markdown.

## Usage

To use the design system in your feature module, add it as a dependency in your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation(projects.core.designsystem)
}
```

You can then apply the `ElizaTheme` to your composables and use the custom components from this module to build your UI.
