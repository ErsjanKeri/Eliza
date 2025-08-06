# ELIZA: Universal Learning Platform

## Empowering Education for Everyone, Everywhere

ELIZA is an innovative AI-powered learning platform designed to break down traditional barriers of location, cost, and language in education. By leveraging cutting-edge artificial intelligence, ELIZA offers personalized and engaging learning experiences that adapt to every student's unique needs, whether they are online or offline.

---

## 🌟 Why ELIZA?

Millions worldwide face significant hurdles in accessing quality education. Studies show:
* **172 million students** encounter systemic barriers.
* **65%** feel disengaged in traditional classrooms.
* **Over 70%** experience anxiety about asking questions in class.

ELIZA is built to solve these critical problems. We believe education should be for everyone, and our platform is designed to unlock a world of knowledge – offline, in your own language, and at your own pace.

---

## ✨ Key Features

* 🌍 **Universal Accessibility:** Learn anytime, anywhere, in your preferred language, regardless of internet access.
* 🧠 **Personalized Learning Paths:** Adapts intelligently to individual learning profiles and study preferences.
* 🔌 **Offline Content Generation:** Instantly generates exercises and text-based explanations locally using **Gemma 3n**, perfect for remote or low-connectivity environments.
* 🎬 **Dynamic Video Explanations:** Transforms complex concepts into engaging, whiteboard-style animated videos through external [ElizaServer](https://github.com/azizialtin/video-generation-service.git) integration, providing visual learning when online.
* 💬 **Context-Aware Chat (RAG Enhanced):** Provides intelligent, relevant responses to student queries by retrieving and integrating information from its comprehensive knowledge base.
* 🖼️ **Multimodal Input:** Understands and responds to both text and image inputs, guiding students to relevant learning materials.
* 🔄 **Adaptive Practice:** Generates new practice questions at selected difficulty levels for continuous mastery and deeper understanding.

---

## ⚙️ How ELIZA Works (Technical Highlights)

ELIZA represents cutting-edge edge AI engineering, running **dual Gemma 3n models** (2B and 4B parameter variants) entirely on-device. 

### ** Advanced AI Model Management**
* **Dual Model System:** Gemma-3n-E2B (2B params) and E4B (4B params) with device-aware selection based on memory constraints
* **Local Inference Engine:** Complete on-device processing using MediaPipe LLM API with streaming response generation
* **Intelligent Model Loading:** Background initialization with crash detection and recovery systems
* **Memory Optimization:** Smart resource management enabling 4B parameter models on mobile devices

### **Production-Grade RAG Implementation**
* **Vector-Based Semantic Search:** Uses MediaPipe's Universal Sentence Encoder for content similarity matching
* **Dual RAG Architecture:** Enhanced provider with vector search + Basic providers with full educational context
* **Content Indexing System:** Automatic chunking and embedding generation of all educational materials
* **Context-Aware Retrieval:** Intelligent content selection across ALL courses based on semantic similarity (not just current chapter)
* **RAG Toggle System:** User-controlled switching between basic and enhanced RAG with real-time adaptation

### ** AI-Powered Exercise Generation Engine**
* **Contextual Question Creation:** Generates mathematically accurate practice problems using RAG-enhanced prompts
* **Quality Validation Pipeline:** Built-in guardrails to prevent AI hallucinations and ensure educational soundness
* **Difficulty Scaling Intelligence:** Precise Easier/Same/Harder variations with concept preservation
* **Structured Response Parsing:** Robust JSON parsing with fallback mechanisms for AI-generated content

### ** Complete Multimodal Processing Pipeline**
* **Vision-Language Integration:** Gemma models process both text and images simultaneously for comprehensive understanding
* **Camera & Gallery Integration:** Seamless image capture and selection following Android best practices
* **Multi-Message Support:** Single queries can include images + text with proper preprocessing

### **Network-Intelligent Architecture**
* **Offline-First Design:** Core AI functionality operates completely without internet connectivity
* **Smart Feature Adaptation:** Graceful degradation of online-only features (video explanations) when offline
* **NetworkMonitor Integration:** Real-time connectivity awareness with automatic retry mechanisms

### **Modern Android Architecture Excellence**
* **Modular Design:** 13 carefully architected modules following Clean Architecture (NowInAndroid-style) principles
* **Jetpack Compose UI:** Modern declarative interface with type-safe navigation
* **Hilt Dependency Injection:** Production-ready DI with proper scoping and lifecycle management
* **Room Database:** Sophisticated relational data layer with proper foreign key relationships
* **Coroutine-Based Concurrency:** Proper async/await patterns with structured concurrency throughout

### **🌍 Intelligent Localization System**
* **LocalizedContent Architecture:** Extensible multi-language support with automatic fallback to English
* **Language-Aware AI:** User's preferred language influences AI responses and content generation
* **LanguageProvider Service:** Centralized language management ensuring consistency across all features

### **Advanced Learning Progress Tracking (for production-ready, currently we are in Demo stage)**
* **"Best Attempt" Logic:** Permanent mastery tracking - once a question is answered correctly, progress will be preserved (to be implemented after Demo)
* **Detailed UserAnswer Records:** Comprehensive attempt history with timing, hints used, and performance analytics
* **Smart Progress Reconstruction:** Robust recovery of learning state even when navigation data is lost
* **Chapter Completion Intelligence:** 100% accuracy requirement with unlimited retakes and granular progress tracking


---
## 📁 Repository Structure

ELIZA follows a **modular Android architecture** with clean separation of concerns across 13 modules:

```
Eliza/
├── app/                          # 📱 Main application module
│   ├── src/main/kotlin/          # Application entry point & navigation
│   └── build.gradle.kts          # App-level dependencies
│
├── core/                         # 🏗️ Foundation layer
│   ├── common/                   # Shared utilities & extensions
│   ├── data/                     # Repository implementations & data sources
│   ├── database/                 # Room database entities & DAOs
│   ├── designsystem/             # UI design tokens & components
│   ├── model/                    # Core data models & domain objects
│   └── network/                  # Network abstraction & monitoring
│
├── ai/                           # 🤖 AI processing layer
│   ├── modelmanager/             # Gemma model lifecycle & device selection
│   ├── rag/                      # Vector embeddings & semantic search
│   └── service/                  # AI orchestration & response generation
│
├── feature/                      # 🎯 UI feature modules
│   ├── chat/                     # AI chat interface & conversation
│   ├── chapter/                  # Chapter reading & test system
│   ├── course-progress/          # Learning progress & analytics
│   ├── course-suggestions/       # AI-powered course recommendations
│   ├── home/                     # Dashboard & navigation
│   └── settings/                 # User preferences & configuration
│
├── gradle/                       # 🔧 Build configuration
│   ├── libs.versions.toml        # Centralized dependency management
│   └── wrapper/                  # Gradle wrapper
│
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Module declarations
├── gradle.properties             # Build properties
├── Design.md                     # 📋 Technical architecture documentation
├── Tasks.md                      # 📝 Development roadmap
├── inference.md                  # 🧠 AI inference flow documentation
├── Technical Write-up.md         # 🏆 Comprehensive technical documentation (Kaggle competition)
└── README.md                     # 📖 Project overview (this file)
```

### **Architecture Highlights**

- **App Module:** Single entry point with navigation & dependency injection setup
- **Core Layer:** Reusable foundation shared across all features
- **AI Layer:** Specialized modules for model management & intelligent responses  
- **Feature Layer:** Independent UI modules with clear boundaries
- **Build System:** Modern Gradle with version catalogs & KTS configuration

This structure follows **NowInAndroid patterns** ensuring scalability, testability, and maintainability for enterprise-grade Android development.

---

## 📋 **Technical Documentation**

### **🏆 Comprehensive Technical Write-up (Kaggle Competition)**

For an in-depth technical analysis of ELIZA's architecture, implementation strategies, and engineering achievements, please refer to our comprehensive **[Technical Write-up](Technical%20Write-up.md)**.

This document provides:
- **Detailed System Architecture**: Complete breakdown of the 13-module Android architecture
- **AI Integration Deep Dive**: Dual Gemma 3n models, RAG implementation, and vector embeddings
- **External Service Integration**: ElizaServer video generation pipeline
- **Technical Challenges & Solutions**: Real-world engineering problems and innovative solutions
- **Performance Specifications**: Detailed technical requirements and optimization strategies
- **Universal Education Vision**: Our mission to democratize world-class education globally

The Technical Write-up serves as both a comprehensive technical reference and an inspiring vision statement for ELIZA's transformative educational mission.

### **Additional Documentation**
- **[inference.md](inference.md)**: Detailed RAG system implementation and AI inference flow

---

## 🚀 Getting Started

ELIZA is currently in development and requires building from source. Follow this comprehensive setup guide to get the AI tutoring platform running on your device.


### **🛠️ Step-by-Step Setup**

#### **Step 1: Install Development Tools**

1. **Download and Install Android Studio:**
   ```bash
   # Visit: https://developer.android.com/studio
   # Download Android Studio Iguana or newer
   ```

#### **Step 2: Clone the Repository**

```bash
git clone https://github.com/ErsjanKeri/Eliza.git
cd Eliza
```

#### **Step 3: Configure Local Properties**

Create a `local.properties` file in the root directory using the provided template:

```bash
# Copy the example file
cp local.properties.example local.properties
```

Edit `local.properties` with your specific configuration:

```properties
# Android SDK path (Android Studio usually sets this automatically)
sdk.dir=/path/to/your/Android/Sdk

# Required: HuggingFace API token for Gemma 3n model downloads
HUGGINGFACE_API_TOKEN=your_huggingface_token_here

# Required: ElizaServer API URL for video explanation service
ELIZA_SERVER_URL=https://your-video-service.com/api
```

**Configuration Notes:**
- **sdk.dir**: Usually auto-configured by Android Studio
- **HUGGINGFACE_API_TOKEN**: Required for downloading Gemma 3n models, your HuggingFace account should accept their terms
- **ELIZA_SERVER_URL**: Required for video explanation feature (ElizaServer API endpoint)

#### **Step 4: Open Project in Android Studio**

1. **Launch Android Studio**
2. **Select "Open an Existing Project"**
3. **Navigate to the cloned `Eliza` folder**
4. **Click "OK" and wait for Gradle sync to complete**

#### **Step 5: Verify Build Configuration**

Android Studio will automatically:
- Download required SDK components
- Sync Gradle dependencies
- Validate JDK version (requires JDK 17+)


#### **Step 6: Build and Run**

1. **Select Build Variant:**
   - Open "Build Variants" panel (bottom-left in Android Studio)
   - Select `debug` for development

2. **Choose Target Device:**
   - **Physical Device:** Enable USB debugging in Developer Options
   - **Emulator:** Create AVD with API 26+ and 4GB+ RAM

3. **Build and Install:**
   ```bash
   # Via Android Studio: Click green "Run" button
   # Or via command line:
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

#### **Step 7: First Launch Setup**

When you first launch ELIZA:

1. **Model Download** (On Demand):
   - App will download Gemma 3n models (~3-4GB)
   - Progress shown in notification
   - Requires internet connection for initial download

2. **Content Indexing** (Automatic):
   - AI system indexes educational content for RAG
   - Takes 2-3 minutes on first launch
   - Runs in background

3. **Permission Grants**:
   - **Camera**: For multimodal image input (optional)
   - **Notifications**: For model download progress
   - **Storage**: For local AI model storage

### **🎯 Quick Start Guide**

Once setup is complete:

1. **Browse Courses**: Explore available math courses from the home screen
2. **Start Learning**: Open any chapter to begin reading
3. **AI Chat**: Tap the chat icon for AI-powered assistance
4. **Take Tests**: Complete chapter tests to track progress
5. **Generate Practice**: Get custom practice questions for wrong answers

### **⚠️ Troubleshooting**

**Performance Tips:**
- **Close other apps** before using ELIZA for optimal AI performance
- **Use WiFi** for initial model downloads
- **Keep 2GB+ storage free** for smooth AI model operation

### **🚀 You're Ready!**

ELIZA is now ready to enhance your learning experience with cutting-edge AI technology. The platform works completely offline after initial setup, making it perfect for learning anywhere, anytime.

**Happy Learning! 🎓**

## 📸 Demo Video

[![Watch the ELIZA Demo Video](https://img.youtube.com/vi/[YOUR_VIDEO_ID]/maxresdefault.jpg)](https://www.youtube.com/watch?v=[VIDEO_TIDI])

---

## 🛠️ Technologies Used

### **🤖 AI & Machine Learning**
* **Core AI Models:** Google Gemma 3n (E2B: 2B params, E4B: 4B params)
* **Inference Engine:** MediaPipe LLM API for on-device processing
* **Text Embeddings:** Universal Sentence Encoder (TensorFlow Lite)
* **Vector Search:** Custom semantic similarity implementation
* **Model Management:** HuggingFace model integration

### **📱 Android Development**
* **Language:** Kotlin 2.0.21
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** Clean Architecture + MVVM pattern
* **Dependency Injection:** Hilt (Dagger)
* **Navigation:** Jetpack Navigation Compose
* **Concurrency:** Kotlin Coroutines & Flow

### **🗃️ Data & Storage**
* **Local Database:** Room (SQLite) with proper relationships
* **Content Storage:** Vector embeddings for RAG
* **User Preferences:** DataStore (Protocol Buffers)
* **Serialization:** Kotlinx Serialization JSON
* **File Management:** Android FileProvider for camera integration

### **🌐 Networking & APIs**
* **HTTP Client:** Retrofit + OkHttp
* **Network Monitoring:** Custom NetworkMonitor implementation
* **External APIs:** HuggingFace model downloads, Video explanation service
* **Connectivity:** Offline-first architecture with online feature enhancement

### **🎯 Key Architectural Patterns**
* **Modular Design:** 13 modules following NowInAndroid patterns
* **RAG (Retrieval-Augmented Generation):** Context-aware AI responses
* **Repository Pattern:** Clean data layer abstraction
* **Dependency Inversion:** Proper separation of concerns

### **🔧 Build & Development Tools**
* **Build System:** Gradle with Kotlin DSL + Version Catalogs
* **Minimum SDK:** API 26 (Android 8.0)
* **Target SDK:** API 36
* **Java Version:** JDK 17
* **Code Quality:** KSP for annotation processing
---


## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).

See the [LICENSE](LICENSE) file for details.
---

## 📞 Contact

Have questions or want to connect? Reach out to us!

* **Altin:** altinshazizi@gmail.com

* **Ersi:** ersjankeri@gmail.com

---
*Named ELIZA as a homage to the first AI chatbot from the 20th century, a testament to how far AI has come. Today, AI like me is no longer a simple conversation partner; it's a tool that genuinely elevates human potential.*
