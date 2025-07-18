# Eliza AI Module

## Overview

This system implements Google's MatFormer (Matryoshka Transformer) architecture for Gemma 3n models, enabling seamless switching between different model variants without re-downloading. The architecture follows a clean layered approach with proper separation of concerns and dependency management.

## Key Features

### 🎯 MatFormer Architecture
- **Single Download**: Only need to download E4B model (4.4GB)
- **Nested Models**: E2B model is contained within E4B as a subset
- **Parameter Sharing**: Uses shared parameters and nested FFN blocks
- **PLE (Per-Layer Embeddings)**: Reduces memory footprint by offloading embeddings to CPU

### 📊 Available Variants
- **E4B (4B effective params)**: Highest quality, ~7GB memory
- **E2B (2B effective params)**: 2x faster inference, ~3.5GB memory

### 🏗️ Layered Architecture
- **Service Layer**: High-level orchestration and business logic
- **Inference Layer**: Pure AI model inference operations
- **Model Management Layer**: Model lifecycle and variant switching
- **RAG Layer**: Retrieval-Augmented Generation for context enhancement

## Directory Structure

The `ai` module is organized into four specialized sub-modules with clear separation of concerns:

```
ai/
├── service/          # 🎯 Orchestration layer - combines all AI capabilities
├── inference/        # 🧠 Pure inference operations with MediaPipe
├── modelmanager/     # 📦 Model lifecycle and variant management
│   ├── manager/      # Core model management logic
│   ├── download/     # Background model downloading
│   └── di/          # Dependency injection configuration
└── rag/             # 📚 Context-aware prompt enhancement
```

```mermaid
graph TD
    subgraph "AI Module Architecture"
        A[ai:service<br/>🎯 Orchestration]
    end

    subgraph "Core AI Modules"
        B[ai:inference<br/>🧠 Pure Inference]
        C[ai:modelmanager<br/>📦 Model Management]
        D[ai:rag<br/>📚 Context Enhancement]
    end

    subgraph "ModelManager Sub-packages"
        E[manager/<br/>Core Logic]
        F[download/<br/>Background Tasks]
        G[di/<br/>Dependency Injection]
    end

    A --> B
    A --> C
    A --> D
    C --> E
    C --> F
    C --> G

    style A fill:#e1f5fe
    style B fill:#e8f5e8
    style C fill:#fff3e0
    style D fill:#f3e5f5
```

## Module Responsibilities

### 🎯 Service Module (`ai:service`)
**Role**: High-level orchestration and business logic coordination

**Key Components**:
- **ElizaChatService**: Main service that coordinates RAG enhancement, variant selection, and inference execution
- Combines capabilities from inference, modelmanager, and rag modules
- Provides the primary API for feature modules to interact with AI functionality
- Handles educational context-aware variant switching

### ModelManager Module

The `modelmanager` module is responsible for managing the lifecycle of the AI models. It is further divided into two sub-packages:

-   **/download**: Contains the components responsible for downloading the AI models from remote sources.
    -   **DownloadWorker**: A `WorkManager` worker that handles the model download process in the background.
    -   **ModelDownloadRepository**: A repository that provides a clean API for downloading models and observing their progress.
    -   **WorkerConstants**: A set of constants used by the download worker and repository.
-   **/manager**: Contains the core logic for managing the AI models.
    -   **ElizaModelManager**: A `ViewModel` that manages the state of the AI model and provides a simple interface for initializing, switching, and interacting with the model.
    -   **ElizaModelRegistry**: A registry that manages the available model variants and their configurations.

### RAG Module

The `rag` module provides the components for Retrieval-Augmented Generation:

-   **RagProvider**: An interface that defines the contract for providing contextual data to the AI model.
-   **RagProviderFactory**: A factory that creates the appropriate RAG provider based on the current context.

#### `/manager` - Core Management Logic
- **ElizaModelManager**: ViewModel for model state management
- **ElizaModelRegistry**: Registry for available models and variants
- **ModelManagerModule**: Dependency injection configuration

#### `/download` - Background Download Operations
- **DownloadWorker**: WorkManager-based background downloading
- **ModelDownloadRepository**: Repository for download operations
- **WorkerConstants**: Shared constants for download operations

**Dependencies**: `core:model`, `core:data`, `core:common`

### 📚 RAG Module (`ai:rag`)
**Role**: Context-aware prompt enhancement and retrieval

**Key Components**:
- **RagProvider**: Interface for contextual content retrieval
- **RagProviderFactory**: Factory for context-specific providers
- **Context-specific providers**: ChapterRagProvider, ExerciseRagProvider, etc.
- Enhances prompts with relevant educational content
- Provides system instructions based on learning context

**Dependencies**: `core:model`, `core:data`

## Architecture Benefits

### 🔄 Circular Dependency Resolution
The layered architecture eliminates circular dependencies:
- **Previous Issue**: `ai:inference` ↔ `ai:modelmanager` circular dependency
- **Solution**: `ai:service` orchestrates both modules without creating cycles
- **Clean Dependencies**: Each layer depends only on lower layers

### 🎯 Separation of Concerns
- **Service**: Business logic and orchestration
- **Inference**: Pure AI operations
- **ModelManager**: Model lifecycle only
- **RAG**: Context enhancement only

### 🔧 Maintainability
- Each module has a single, well-defined responsibility
- Clear interfaces between modules
- Easy to test individual components
- Scalable for future AI capabilities

## MatFormer Variant Switching

The system provides intelligent variant switching based on educational context:

```kotlin
// Automatic context-aware variant selection
when (context) {
    is ChatContext.ChapterReading -> "E2B"    // Fast for reading assistance
    is ChatContext.ExerciseSolving -> "E4B"   // High quality for problem solving
    is ChatContext.Revision -> "E2B"          // Fast for quick reviews
    is ChatContext.GeneralTutoring -> "Auto"  // Device-adaptive
}
```

### Key Benefits
- **No Re-downloading**: Switch instantly between variants
- **Memory Efficiency**: PLE technique reduces GPU memory footprint
- **Performance Flexibility**: Context-aware quality vs. speed optimization
- **Device-Adaptive**: Automatically adapts to device capabilities

## Usage Examples

### Basic Chat Service Usage
```kotlin
@Inject lateinit var chatService: ElizaChatService

// Initialize and use the service
chatService.initializeModel()
chatService.sendMessage(
    sessionId = "session_123",
    message = "Explain quadratic equations",
    context = ChatContext.ChapterReading("algebra_101", "Quadratic Equations")
)
```

### Direct Model Management
```kotlin
@Inject lateinit var modelManager: ElizaModelManager

// Switch variants manually
modelManager.switchToVariant("gemma-3n-E4B")

// Get device-recommended variant
val recommendedVariant = modelManager.getRecommendedVariant()
modelManager.switchToVariant(recommendedVariant)
```

### Context-Aware RAG Enhancement
```kotlin
@Inject lateinit var ragFactory: RagProviderFactory

// Get context-specific RAG provider
val context = ChatContext.ExerciseSolving("lesson_42", "exercise_5", attempts = 2)
val ragProvider = ragFactory.createProvider(context)

// Enhance prompt with relevant content
val enhancedPrompt = ragProvider.enhancePrompt("How do I solve this?", context)
```

## Data Flow Architecture

```mermaid
graph TB
    subgraph "Feature Layer"
        A[User Input + Context]
    end

    subgraph "Service Layer (ai:service)"
        B[ElizaChatService]
        C[Context Analysis]
        D[Variant Selection]
        E[Orchestration]
    end

    subgraph "RAG Layer (ai:rag)"
        F[Content Retrieval]
        G[Prompt Enhancement]
        H[System Instructions]
    end

    subgraph "Model Management (ai:modelmanager)"
        I[Model Initialization]
        J[Variant Switching]
        K[Lifecycle Management]
    end

    subgraph "Inference Layer (ai:inference)"
        L[Session Management]
        M[Parameter Optimization]
        N[Streaming Inference]
    end

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    E --> I
    F --> G
    G --> H
    I --> J
    J --> K
    E --> L
    L --> M
    M --> N

    style B fill:#e1f5fe
    style E fill:#e1f5fe
    style F fill:#f3e5f5
    style I fill:#fff3e0
    style L fill:#e8f5e8
```

## Educational Context Integration

The system provides specialized behavior for different learning contexts:

### 📖 Chapter Reading
- **Variant**: E2B (fast responses)
- **RAG**: Current chapter content
- **Focus**: Explanations and clarifications

### 🧮 Exercise Solving
- **Variant**: E4B (high quality)
- **RAG**: Exercise context and hints
- **Focus**: Step-by-step problem solving

### 📝 Revision
- **Variant**: E2B (quick reviews)
- **RAG**: Previously studied content
- **Focus**: Reinforcement and practice

### 🎓 General Tutoring
- **Variant**: Device-adaptive
- **RAG**: General knowledge base
- **Focus**: Flexible educational support

## Performance Characteristics

### Memory Usage
- **E4B Variant**: ~7GB peak memory
- **E2B Variant**: ~3.5GB peak memory
- **PLE Optimization**: Reduces GPU memory by 40%

### Inference Speed
- **E4B**: Higher quality, ~3 seconds response time
- **E2B**: 2x faster inference, ~1.5 seconds response time
- **Context Switching**: <100ms variant switching

### Device Adaptation
- **High-end devices**: Automatic E4B selection
- **Mid-range devices**: Context-aware switching
- **Low-end devices**: E2B preference with quality fallbacks
