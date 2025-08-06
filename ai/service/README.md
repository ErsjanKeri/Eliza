# AI Service Module

The `ai:service` module is the core of the AI-powered features in the Eliza application. It encapsulates the business logic for interacting with the AI models, generating content, and providing intelligent responses to user queries.

## Responsibilities

- **Chat Service:** Provides the `ElizaChatService` and `RagEnhancedChatService` for managing chat conversations and generating AI-powered responses.
- **Content Generation:** Includes services for generating exercises (`ExerciseGenerationService`), course suggestions (`CourseSuggestionService`), and video explanations (`VideoExplanationService`).
- **Prompt Engineering:** Contains the prompt templates (`ExercisePromptTemplates`, `VideoPromptTemplates`, `CourseSuggestionPromptTemplates`) that are used to generate high-quality responses from the AI models.
- **Response Parsing:** Includes the `ExerciseResponseParser` for parsing the responses from the AI models into a structured format.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`:core:model`:** For the application's data models.
- **`:core:data`:** For accessing the application's data layer.
- **`:core:network`:** For making network requests to the AI services.

- **`:ai:modelmanager`:** For managing the AI models.
- **`:ai:rag`:** For the Retrieval-Augmented Generation functionality.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous programming.
- **`Serialization`:** For parsing JSON data.

## Usage

The services in this module are typically used by the ViewModels in the feature modules to power the AI-driven features. To use a service, inject it into your ViewModel using Hilt.
