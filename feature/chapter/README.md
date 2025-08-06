# Feature Chapter Module

The `feature:chapter` module is responsible for displaying the content of a chapter to the user. It provides a rich reading experience, with support for markdown, images, and interactive exercises.

## Responsibilities

- **Chapter Content:** Displays the markdown content of a chapter, including text, images, and formatted code.
- **Interactive Exercises:** Includes a test system that allows users to test their knowledge of the chapter content.
- **Exercise Generation:** Provides a mechanism for generating new practice questions at different difficulty levels.
- **Chat Integration:** Integrates with the `:feature:chat` module to allow users to ask questions about the chapter content.

## Dependencies

This module has the following dependencies:

- **`:core:designsystem`:** For the UI components.
- **`:core:data`:** For accessing the chapter data.
- **`:core:model`:** For the chapter and exercise data models.
- **`:core:common`:** For shared utilities.
- **`:ai:service`:** For the exercise generation and chat services.
- **`:ai:modelmanager`:** For managing the AI models.
- **`:feature:chat`:** For the chat functionality.
- **`Jetpack Compose`:** For the UI.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous operations.

## Usage

The `chapterScreen` is the main entry point for this feature. It can be added to the navigation graph in the `:app` module.
