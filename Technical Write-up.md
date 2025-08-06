# ELIZA: AI-Powered Personalized Learning Platform
## Technical Architecture & Implementation Documentation

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [System Architecture](#system-architecture)
3. [Core Technologies](#core-technologies)
4. [Technical Challenges & Solutions](#technical-challenges--solutions)
5. [AI Integration Strategy](#ai-integration-strategy)
6. [Performance & Scalability](#performance--scalability)
7. [Technical Specifications](#technical-specifications)
8. [Future Roadmap](#future-roadmap)

---

## Executive Summary

ELIZA represents a breakthrough in accessible AI-powered education, designed to eliminate the traditional barriers of location, cost, and connectivity. Built around a sophisticated 13-module Android architecture with Gemma 3n at its core, ELIZA delivers personalized learning experiences through advanced Retrieval-Augmented Generation (RAG) technology, multimodal content understanding, and external video generation integration.

### Key Technical Achievements
- **Offline-First AI Processing**: Full-featured learning experience without internet connectivity
- **External Video Generation**: Educational video creation through ElizaServer API integration
- **Multimodal Content Understanding**: Combined text and image processing with Gemma models
- **Real-time Context Awareness**: Advanced RAG implementation for accurate, contextual responses

---

## System Architecture

ELIZA follows a **modular Android architecture** with clean separation of concerns across **13 modules**, based on NowInAndroid patterns, ensuring scalability, testability, and maintainability for enterprise-grade Android development.

### **App Module**
- **Single Entry Point**: Navigation & dependency injection setup
- **Main Components**: `MainActivity`, `ElizaApplication`, `ElizaNavHost`
- **UI Shell**: Top-level navigation and app state management

### **Core Layer (6 modules)**
- **`core:common`** - Shared utilities & extensions  
- **`core:data`** - Repository implementations & data sources (includes MockCourseRepository with 6 courses)
- **`core:database`** - Room database entities & DAOs
- **`core:designsystem`** - UI design tokens & components
- **`core:model`** - Core data models & domain objects (ChatContext, etc.)
- **`core:network`** - Network abstraction & monitoring

### **AI Layer (3 modules)**
- **`ai:modelmanager`** - Gemma model lifecycle & device selection
- **`ai:rag`** - Vector embeddings & semantic search
- **`ai:service`** - AI orchestration & response generation

### **Feature Layer (6 modules)**
- **`feature:chat`** - AI chat interface & conversation
- **`feature:chapter`** - Chapter reading & test system
- **`feature:course-progress`** - Learning progress & analytics
- **`feature:course-suggestions`** - AI-powered course recommendations
- **`feature:home`** - Dashboard & navigation
- **`feature:settings`** - User preferences & configuration

### **Key Architecture Principles**
- **Dependency Inversion**: Core modules don't depend on feature modules
- **Single Responsibility**: Each module has a focused purpose
- **Hilt Integration**: Production-ready dependency injection throughout
- **Clean Architecture**: Clear boundaries between UI, domain, and data layers
- **Context Management**: `ChatContext` sealed class for different chat scenarios (GeneralTutoring, ChapterReading, ExerciseSolving, Revision, CourseSuggestion)
- **Enhanced ViewModels**: `EnhancedChatViewModel` loads real data for proper context creation

---

## Core Technologies

### Gemma 3n Integration
ELIZA uses two Gemma 3n models for on-device AI inference, specifically chosen for their optimal balance of performance, quality, and mobile device compatibility.

#### **Available Models:**
- **Gemma-3n-E4B-it-int4** (4B parameters): High-performance model for complex reasoning (~4.4GB)
- **Gemma-3n-E2B-it-int4** (2B parameters): Lightweight model for resource-constrained devices (~3.1GB)

#### Primary Use Cases:
1. **Context-Aware Chat Responses**
   - Text generation with RAG enhancement
   - Educational explanations and tutoring
   - Exercise help and problem-solving guidance

2. **Multimodal Input Processing**
   - Combined text and image understanding
   - Visual concept recognition and explanation
   - Educational content analysis from photos
   

3. **Exercise Generation**
   - AI-powered practice question creation
   - Difficulty-adjusted problem variations
   - Contextual educational content generation

4. **Device-Aware Intelligence**
   - Automatic model selection based on device capabilities
   - Memory-optimized inference with crash detection
   - Hardware-adaptive processing for consistent performance

### Retrieval-Augmented Generation (RAG) Architecture

ELIZA implements a sophisticated dual-mode RAG system for context-aware educational responses.

#### **Two RAG Modes Available:**

##### **Basic RAG (Default, Always Reliable)**
- **Simple context feeding**: Provides the entire chapter content or exercise context directly to the AI model
- **No complex processing**: Fast, straightforward data retrieval from ChatContext
- **Context-specific providers**: ChapterRagProvider feeds full chapter markdown, ExerciseRagProvider provides complete exercise context
- **Fixed confidence scoring**: Basic providers return simple confidence values (0.5f-0.9f) based on content availability
- **Best for**: Exercise help, chapter questions, reliable responses with full context

##### **Enhanced RAG (Optional, Toggle-Based)**
- Vector-based semantic similarity search across ALL courses
- Cross-chapter content discovery using embeddings
- Universal Sentence Encoder for semantic understanding
- Automatic fallback to Basic RAG if vector search fails
- Confidence scores: 0.1f - 1.0f (0.3f indicates fallback occurred)
- **Best for**: Complex questions requiring broad knowledge

#### **Core Components:**
- **`RagProviderFactory`** - Provider selection based on toggle state
- **`EnhancedRagProvider`** - Vector search implementation with semantic similarity
- **Basic RAG Providers** - Context-specific content retrieval:
  - `ChapterRagProvider` - Feeds full chapter markdown content
  - `ExerciseRagProvider` - Provides complete exercise context
  - `RevisionRagProvider` - Multi-chapter content for review
  - `GeneralRagProvider` - Basic academic support
  - `CourseSuggestionRagProvider` - Course recommendation context
- **`TextEmbeddingService`** - MediaPipe-based embeddings using universal_sentence_encoder.tflite
- **`VectorStorageDatabase`** - Room database for content chunks and embeddings
- **`ContentChunkDao`** - DAO for chunk operations and similarity search
- **`RagIndexingService`** - Indexes educational content on app startup
- **`SystemInstructionProvider`** - Centralized, consistent AI instructions

#### **Enhanced RAG Process Flow:**
1. **App Initialization**: `RagIndexingService` indexes all MockCourseRepository content (6 courses) on startup
2. **User Query**: Embedding generation using Universal Sentence Encoder (MediaPipe)
3. **Vector Search**: Semantic similarity search against indexed educational content
4. **Content Retrieval**: Relevant chunk retrieval with confidence scoring (0.7f threshold)
5. **Prompt Enhancement**: Dynamic prompt enhancement with retrieved context + basic educational data
6. **AI Generation**: Gemma model response generation with grounded information
7. **Fallback Logic**: Transparent fallback to Basic RAG if Enhanced fails (confidence drops to 0.3f)

### External Video Generation Service

ELIZA integrates with **[ElizaServer](https://github.com/azizialtin/video-generation-service.git)** for educational video creation, providing dynamic visual explanations when internet connectivity is available.

#### **Process Flow:**
1. **Prompt Generation**: Convert educational context (chapter content, exercise details) into natural language prompts
2. **API Request**: Send prompt to ElizaServer via REST API (`VideoService.kt`)
3. **Status Polling**: Monitor video generation progress (queued → generating_script → rendering_video → completed)
4. **Video Download**: Retrieve and cache completed MP4 videos locally for offline playback
5. **Graceful Degradation**: Video features disabled when offline, with clear user feedback

#### **Integration Architecture:**
- **`VideoService`** interface for REST API communication with [ElizaServer](https://github.com/azizialtin/video-generation-service.git)
- **`VideoExplanationService`** for request orchestration and status management
- **`VideoPromptTemplates`** for context-to-prompt conversion
- **Local caching** system for offline video playback
- **Network-aware features** with automatic online/offline detection

#### **Video Request Types:**
- **Chapter Explanations**: Context-aware videos for chapter content questions
- **Exercise Help**: Step-by-step problem-solving videos with user's specific wrong answers
- **General Topics**: Educational videos for broad mathematical concepts

#### **Technical Specifications:**
- **Server Integration**: [ElizaServer](https://github.com/azizialtin/video-generation-service.git) REST API (external service)
- **Video Format**: MP4 with optimized file sizes  
- **Request Limits**: 1000 character prompt limit (for now), 300-1200 second duration
- **Offline Behavior**: Cached videos available, new requests require internet

---

## Technical Challenges & Solutions

### Challenge 1: Robust Offline AI Processing
**Problem**: Integrating powerful AI model (Gemma 3n) for local execution across diverse mobile hardware configurations.

**Solution**: 
- Advanced model quantization techniques for efficient inference
- Comprehensive resource management optimization
- Extensive cross-device testing and performance tuning
- Hardware-adaptive processing algorithms

**Impact**: Consistent, responsive AI performance without cloud dependency

### Challenge 2: Multimodal Input Processing
**Problem**: Enabling AI models to understand both text and visual educational content simultaneously.

**Solution**:
- Direct integration with Gemma models' native vision capabilities
- Combined text and image message processing pipeline
- Automatic image preprocessing (EXIF rotation, aspect ratio optimization)
- Gallery and camera integration with proper Android permissions

**Impact**: Students can upload photos of problems, diagrams, or textbook pages for AI analysis

### Challenge 3: External Video Integration
**Problem**: Providing high-quality educational videos without overwhelming local device resources.

**Solution**:
- External ElizaServer API integration for video generation
- Intelligent prompt generation from educational context
- Local video caching with efficient storage management
- Network-aware graceful degradation for offline scenarios
- Comprehensive error handling and retry mechanisms

**Impact**: Rich visual learning experience when online, with cached content available offline

### Challenge 4: Real-time Context Awareness
**Problem**: Maintaining contextual relevance across complex, multi-turn educational conversations.

**Solution**:
- Pre-indexed content embedding system
- High-performance vector database implementation
- Dynamic context injection into AI prompts
- Hallucination mitigation through grounded responses

**Impact**: Accurate, domain-specific educational guidance with minimal AI errors

---

## AI Integration Strategy

### Model Selection Rationale
**Gemma 3n Selection Criteria**:
- Superior on-device inference capabilities
- Balanced performance-to-resource ratio
- High-quality generative outputs
- Educational content optimization compatibility

### RAG Implementation Benefits
- **Accuracy Assurance**: Grounded responses reduce AI hallucinations
- **Knowledge Currency**: Real-time access to updated educational content
- **Domain Specificity**: Contextually relevant educational information
- **Scalable Knowledge Base**: Expandable content repository without model retraining
- **Vision for Universal Knowledge**: RAG architecture enables ELIZA's goal to contain the information of the entire world (like the Library of Alexandria), making personalized education accessible to everyone, everywhere

### Offline-First Philosophy
- **Universal Accessibility**: Removes internet connectivity barriers
- **Cost Effectiveness**: Eliminates ongoing API costs for users
- **Performance Reliability**: Consistent experience regardless of network conditions
- **Privacy Protection**: Local processing ensures data security

---

## Performance & Scalability

### Optimization Strategies
- **Model Quantization**: Reduced memory footprint without quality loss
- **Efficient Indexing**: Optimized vector storage and retrieval
- **Adaptive Processing**: Hardware-aware resource allocation
- **Caching Mechanisms**: Intelligent content pre-loading and storage

### Scalability Considerations
- **Modular Architecture**: Independent component scaling
- **Stateless Services**: Horizontal scaling compatibility
- **Database Optimization**: Vector storage performance tuning
- **Content Distribution**: Efficient knowledge base management

---

## Technical Specifications

### System Requirements
- **Minimum RAM**: 4GB for optimal Gemma 3n performance
- **Storage**: 3-4GB for AI models, 2GB+ free space recommended
- **Processing**: ARM64 or x64 architecture support
- **Operating System**: Android 8.0+ (API 26+), Target API 36
- **Network**: Optional (required only for video generation and model downloads)

### Performance Metrics
- **RAG Enhancement**: Basic RAG ~100-200ms, Enhanced RAG ~300-500ms overhead
- **Model Inference**: Depends on device capabilities and selected Gemma model
- **Video Generation**: Handled by external ElizaServer 
- **Offline Mode**: Full AI functionality available, video features require internet
- **Storage Requirements**: ~3-4GB for Gemma models, ~6MB for Universal Sentence Encoder, additional space for video cache and indexed content

### Data Management
- **Vector Database**: Room database with high-dimensional embedding storage
- **Content Repository**: MockCourseRepository with 6 courses, detailed chapters, and exercises
- **Content Indexing**: Automatic chunking and embedding generation during app startup
- **User Progress**: Local learning analytics with detailed attempt history
- **Multi-language Support**: Localized content and interface adaptation

---

## Future Roadmap

### Short-term Enhancements
- Advanced multimodal input support (audio, handwriting recognition)
- Improved video caching and offline playback optimization
- Enhanced RAG performance and cross-chapter content discovery
- Real-time collaborative learning features
- Additional Gemma model variants and performance optimizations

### Long-term Vision
- **Universal Knowledge Repository**: Expand ELIZA to contain an incredible amount of books, research papers, and educational information from around the world
- **Global Educational Accessibility**: Provide every student, anywhere they are, with personal tailored tutoring and learning experiences
- **Alexandria-Scale Knowledge Base**: Build a comprehensive knowledge repository that democratizes access to world-class education
- **Advanced personalization through federated learning**: Adaptive learning paths for millions of users
- **Augmented reality educational content delivery**: Immersive learning experiences
- **Blockchain-based credential and achievement systems**: Verified educational achievements

### Research Directions
- Edge computing optimization for AI processing
- Novel RAG architectures for educational applications
- Multimodal learning effectiveness studies
- Accessibility technology advancement

---

## Conclusion

ELIZA represents a significant advancement in AI-powered educational technology, successfully addressing the fundamental barriers to quality education through innovative technical solutions. The combination of dual Gemma 3n models, sophisticated dual-mode RAG architecture, external video integration, and offline-first design philosophy creates a truly accessible, personalized learning platform capable of serving millions of learners worldwide.

Our ultimate goal is revolutionary: **making high-quality, personalized education available to everyone, everywhere**. Education is a fundamental necessity, not a privilege. Through ELIZA's scalable RAG architecture and universal knowledge vision, we aim to ensure that every student - regardless of location, economic circumstances, or connectivity - can afford and access a personal tailored learning experience that rivals the world's best tutors.

The modular Android architecture demonstrates that advanced AI capabilities can be democratized without compromising quality or accessibility, setting a new standard for educational technology platforms. By combining on-device AI inference with intelligent external service integration, ELIZA achieves the optimal balance of capability, accessibility, and performance, paving the way toward universal educational equity.

---

*This technical documentation serves as a comprehensive guide to ELIZA's architecture, implementation strategies, and engineering achievements.*