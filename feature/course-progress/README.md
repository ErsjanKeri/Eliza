# Feature Course Progress Module

The `feature:course-progress` module is responsible for displaying the user's progress in a course. It provides a visual overview of the chapters the user has completed and the ones that are still in progress.

## Responsibilities

- **Progress Tracking:** Displays the user's progress through a course, including the completion status of each chapter.
- **Chapter Navigation:** Allows the user to navigate to a specific chapter from the progress screen.
- **Score Display:** Shows the user's score on the chapter tests.

## Dependencies

This module has the following dependencies:

- **`:core:designsystem`:** For the UI components.
- **`:core:data`:** For accessing the course progress data.
- **`:core:model`:** For the course and chapter data models.
- **`:core:common`:** For shared utilities.
- **`Jetpack Compose`:** For the UI.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous operations.

## Usage

The `courseProgressScreen` is the main entry point for this feature. It can be added to the navigation graph in the `:app` module.
