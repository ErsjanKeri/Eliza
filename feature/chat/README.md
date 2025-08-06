# Feature Chat Module

The `feature:chat` module provides the chat functionality for the Eliza application. It allows users to interact with the AI tutor, ask questions, and get help with their learning.

## Responsibilities

- **Chat UI:** Provides the `ChatScreen`, which is the main UI for the chat feature.
- **Message Handling:** Manages the sending and receiving of chat messages.
- **AI Integration:** Integrates with the `:ai:service` module to generate AI-powered responses.
- **Session Management:** Manages the chat sessions, allowing users to have multiple conversations with the AI tutor.

## Dependencies

This module has the following dependencies:

- **`:core:designsystem`:** For the UI components.
- **`:core:data`:** For accessing the chat data.
- **`:core:model`:** For the chat and message data models.
- **`:core:network`:** For making network requests.
- **`:core:common`:** For shared utilities.
- **`:ai:service`:** For the chat service.
- **`:ai:modelmanager`:** For managing the AI models.
- **`:ai:rag`:** For the Retrieval-Augmented Generation functionality.
- **`Jetpack Compose`:** For the UI.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous operations.

## Usage

The `chatScreen` is the main entry point for this feature. It can be added to the navigation graph in the `:app` module.
