# AI RAG Module

The `ai:rag` module implements the Retrieval-Augmented Generation (RAG) functionality for the Eliza application. RAG enhances the responses of the AI models by providing them with relevant context from a knowledge base before they generate a response. This helps to ensure that the AI's responses are accurate, relevant, and grounded in the application's data.

## Responsibilities

- **RAG Providers:** Implements the `RagProvider` interface, which is responsible for retrieving the relevant context and enhancing the AI's prompt.
- **Content Indexing:** Includes the `RagIndexingService` for indexing the application's content and making it searchable.
- **Vector Storage:** Defines the Room database entities for storing the vector embeddings of the content.
- **Text Embedding:** Provides a `TextEmbeddingService` for converting text into vector embeddings using a local sentence encoder model.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`:core:data`:** For accessing the application's data layer.
- **`:core:model`:** For the application's data models.
- **`:core:database`:** For accessing the local database.
- **`Hilt`:** For dependency injection.
- **`Room`:** For the vector storage implementation.
- **`MediaPipe`:** For the text embedding service.

## Usage

The `RagProvider` is used by the `RagEnhancedChatService` in the `:ai:service` module to enhance the AI's prompts. The `RagIndexingService` is run on application startup to index the content.
