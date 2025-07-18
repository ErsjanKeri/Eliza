markdown# AI Tutor App - Technical Design

## Architecture Overview

### High-Level Architecture
┌─────────────────────────────────────┐
│               :app                  │
├─────────────────────────────────────┤
│  :feature:chat    :feature:courses  │
│  :feature:camera  :feature:progress │
├─────────────────────────────────────┤
│  :core:ai        :core:data         │
│  :core:database  :core:ui           │
│  :core:common    :core:designsystem │
├─────────────────────────────────────┤
│  :ai:modelmanager :ai:inference     │
│  :ai:rag          :ai:utils         │
└─────────────────────────────────────┘

### Module Dependencies
- **:app** → all feature modules
- **:feature:*** → :core:*, :ai:*
- **:core:ai** → :ai:modelmanager, :ai:inference
- **:ai:*** → :core:common, :core:data

## Data Flow Architecture

### 1. Model Initialization Flow (MatFormer Architecture)
```mermaid
sequenceDiagram
    participant App
    participant ModelManager
    participant ModelRegistry
    participant WorkManager
    participant FileSystem
    
    App->>ModelManager: Initialize Gemma-3n
    ModelManager->>ModelRegistry: Get current variant
    ModelRegistry-->>ModelManager: E4B model (contains E2B)
    ModelManager->>WorkManager: Download model if needed
    WorkManager->>FileSystem: Save model file (4.4GB)
    FileSystem-->>WorkManager: Download complete
    WorkManager-->>ModelManager: Model ready
    ModelManager->>ModelManager: Initialize inference
    ModelManager-->>App: Model initialized
    Note over ModelRegistry: E2B available as nested subset
2. AI Chat Interaction Flow
mermaidsequenceDiagram
    participant User
    participant ChatUI
    participant AIService
    participant Gemma3n
    participant Database
    
    User->>ChatUI: Types question
    ChatUI->>AIService: Process question
    AIService->>Database: Get conversation context
    Database-->>AIService: Previous messages
    AIService->>Gemma3n: Generate response
    Gemma3n-->>AIService: Response text
    AIService->>Database: Save conversation
    AIService-->>ChatUI: Display response
    ChatUI-->>User: Show answer
3. Image Processing Flow
mermaidsequenceDiagram
    participant User
    participant CameraUI
    participant ImageProcessor
    participant Gemma3n
    participant ChatUI
    
    User->>CameraUI: Capture/upload image
    CameraUI->>ImageProcessor: Process image
    ImageProcessor->>Gemma3n: Analyze image
    Gemma3n-->>ImageProcessor: Extracted text/problem
    ImageProcessor->>Gemma3n: Solve problem
    Gemma3n-->>ImageProcessor: Solution steps
    ImageProcessor-->>ChatUI: Display solution
    ChatUI-->>User: Show solution
Data Models
Core Data Models
kotlindata class Course(
    val id: String,
    val title: String,
    val subject: Subject,
    val lessons: List<Lesson>,
    val createdAt: Long
)

data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val examples: List<Example>,
    val isCompleted: Boolean = false
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageUri: String? = null
)

data class UserProgress(
    val userId: String,
    val courseId: String,
    val lessonId: String,
    val completedAt: Long,
    val timeSpent: Long
)

data class ModelState(
    val isInitialized: Boolean,
    val isDownloading: Boolean,
    val downloadProgress: Float,
    val errorMessage: String? = null
)
Database Schema (Room)
kotlin@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val createdAt: Long
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val isCompleted: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageUri: String? = null
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val lessonId: String,
    val completedAt: Long,
    val timeSpent: Long
)
Component Architecture
1. AI Model Integration (from gallery-edge-ai)
kotlin@HiltViewModel
class ElizaModelManager @Inject constructor(
    private val context: Context,
    private val downloadRepository: ModelDownloadRepository,
    private val inferenceHelper: ElizaInferenceHelper,
    private val modelRegistry: ElizaModelRegistry
) : ViewModel() {
    
    // Uses MatFormer-based model registry for variant switching
    private val currentModel: Model
        get() = modelRegistry.getCurrentModel() ?: throw IllegalStateException("No model available")
    
    suspend fun switchToVariant(targetVariant: GemmaVariant)
    suspend fun initializeModel(): Flow<ModelInitializationResult>
    suspend fun downloadModel()
    fun getRecommendedVariant(): GemmaVariant
}
2. Chat Service with MatFormer Model Support
kotlin@Singleton
class ChatService @Inject constructor(
    private val modelManager: ElizaModelManager,
    private val chatRepository: ChatRepository,
    private val ragService: RagService
) {
    suspend fun sendMessage(
        sessionId: String,
        message: String,
        imageUri: Uri? = null
    ): Flow<ChatResponse>
    
    suspend fun generateMathSolution(problem: String): Result<String>
    suspend fun explainConcept(concept: String): Result<String>
    
    // New MatFormer capabilities
    suspend fun switchToOptimalVariant(deviceCapabilities: DeviceCapabilities)
    fun getCurrentVariant(): GemmaVariant
}
3. Course Content Service
kotlin@Singleton
class CourseService @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) {
    suspend fun getCourses(): Flow<List<Course>>
    suspend fun getLesson(lessonId: String): Flow<Lesson>
    suspend fun markLessonComplete(lessonId: String)
    suspend fun getUserProgress(): Flow<UserProgress>
}
User Interface Architecture
Navigation Structure
kotlinsealed class Screen(val route: String) {
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Courses : Screen("courses")
    object Camera : Screen("camera")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
}

// Bottom Navigation
@Composable
fun BottomNavigation() {
    val items = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.Courses,
        Screen.Camera,
        Screen.Progress
    )
    // Navigation implementation
}
Key UI Components
kotlin@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onImageCapture: (Uri) -> Unit,
    isLoading: Boolean
)

@Composable
fun CourseListScreen(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    userProgress: UserProgress
)

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onImageSelected: (Uri) -> Unit
)

@Composable
fun ModelDownloadDialog(
    downloadProgress: Float,
    isDownloading: Boolean,
    onCancel: () -> Unit
)
Performance Considerations
Memory Management

Model loaded lazily on first use
Conversation history limited to last 20 messages
Images compressed before processing
Proper lifecycle management for model instances

Storage Optimization

Model files stored in external storage
Database queries optimized with indices
Course content cached efficiently
Cleanup of old conversation data

Battery Optimization

Background processing limited
Model inference optimized for efficiency
Screen brightness awareness
Minimal network usage (offline-first)

Error Handling Strategy
Model-Related Errors

Model download failures → Retry mechanism
Initialization failures → Fallback to cached responses
Memory pressure → Graceful degradation
Inference errors → User-friendly error messages

User Experience Errors

Network issues → Offline mode indication
Camera permissions → Alternative upload option
Storage full → Cleanup suggestions
App crashes → Automatic recovery

Testing Strategy
Unit Tests

Model initialization logic
Chat message processing
Progress calculation
Data model validation

Integration Tests

End-to-end chat flow
Image processing pipeline
Course content loading
Progress persistence

Performance Tests

Model inference speed
Memory usage monitoring
Battery consumption
App startup time

Security Considerations
Data Privacy

All processing happens locally
No user data sent to external servers
Conversation history encrypted at rest
Image data processed locally only

Model Security

Model integrity verification
Secure model downloading
Protection against model tampering
Safe handling of user-generated content