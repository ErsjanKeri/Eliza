# Feature Home Module

The `feature:home` module is the main entry point for the user after they log in. It provides a dashboard that displays the user's current courses, their progress, and suggestions for what to learn next.

## Responsibilities

- **Dashboard:** Displays a summary of the user's learning activity, including their current courses and progress.
- **Course Navigation:** Allows the user to navigate to their courses and resume their learning.
- **Course Suggestions:** Shows a list of recommended courses to the user.

## Dependencies

This module has the following dependencies:

- **`:core:designsystem`:** For the UI components.
- **`:core:data`:** For accessing the user's data.
- **`:core:model`:** For the course and user data models.
- **`:core:common`:** For shared utilities.
- **`Jetpack Compose`:** For the UI.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous operations.

## Usage

The `homeScreen` is the main entry point for this feature. It is the start destination of the navigation graph in the `:app` module.
