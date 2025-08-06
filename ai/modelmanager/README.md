# AI Model Manager Module

The `ai:modelmanager` module is responsible for managing the AI models used in the Eliza application. It handles the downloading, caching, and lifecycle of the models, ensuring that they are available when needed and that they are used efficiently.

## Responsibilities

- **Model Downloading:** Manages the downloading of AI models from remote sources, such as Hugging Face.
- **Model Caching:** Caches the downloaded models on the device to avoid repeated downloads.
- **Model Lifecycle:** Manages the lifecycle of the models, including loading them into memory and unloading them when they are no longer needed.
- **Device Capability Checking:** Includes a `DeviceCapabilityChecker` to ensure that the device has the necessary capabilities to run the AI models.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`:core:data`:** For accessing the application's data layer.
- **`:core:model`:** For the application's data models.

- **`:ai:rag`:** For the Retrieval-Augmented Generation functionality.
- **`Hilt`:** For dependency injection.
- **`WorkManager`:** For running background tasks, such as model downloads.
- **`MediaPipe`:** For AI model inference.

## Usage

The `ElizaModelManager` is the main entry point for interacting with this module. It can be injected into your ViewModels or other components using Hilt. The `LlmChatModelHelper` is used to run the inference on the models.
