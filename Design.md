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
│  :core:network   :core:datastore    │ ← NEW: Network module
├─────────────────────────────────────┤
│  :ai:modelmanager :ai:inference     │
│  :ai:rag          :ai:service       │
└─────────────────────────────────────┘

### Module Dependencies
- **:app** → all feature modules
- **:feature:*** → :core:*, :ai:*
- **:core:ai** → :ai:modelmanager, :ai:inference
- **:core:network** → video API integration ← NEW
- **:ai:*** → :core:common, :core:data

## UI/UX Design Specifications

### 1. Chapter Interface Layout (Gallery-Style Split Screen)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Chapter: Linear Equations                        │
├─────────────────────────┬───────────────────────────────────────────────┤
│                         │                                               │
│   📖 CHAPTER CONTENT    │           💬 CHAT INTERFACE                   │
│                         │                                               │
│ # Linear Equations      │ ┌─────────────────────────────────────────┐   │
│                         │ │         Chat Sessions                   │   │
│ Linear equations are... │ │ ┌─────────────────────────────────────┐ │   │
│                         │ │ │ 📝 "Understanding basics"  [Active] │ │   │
│ ## Example 1:           │ │ │ 📝 "Practice problems"             │ │   │
│ Solve: 2x + 5 = 15      │ │ │ 📝 "Step-by-step solutions"       │ │   │
│                         │ │ │ [+ New Chat Session]               │ │   │
│ Step 1: Subtract 5...   │ │ └─────────────────────────────────────┘ │   │
│ Step 2: Divide by 2...  │ └─────────────────────────────────────────┘   │
│                         │                                               │
│ ## Practice Problems:   │ ┌─────────────────────────────────────────┐   │
│ 1. Solve: 3x - 7 = 8    │ │           Active Chat                   │   │
│ 2. Solve: x/4 + 2 = 6   │ │                                         │   │
│                         │ │ 👤 User: "I don't understand step 2"   │   │
│ [📊 Test Yourself]      │ │                                         │   │
│                         │ │ 🤖 AI: "Step 2 involves dividing both  │   │
│                         │ │     sides by the coefficient..."        │   │
│                         │ │                                         │   │
│                         │ │ 👤 User: "Can you show me visually?"   │   │
│                         │ │                                         │   │
│                         │ │ [🎥 REQUEST VIDEO] [💬 ASK MORE]        │   │
│                         │ │                                         │   │
│                         │ │ ⏳ Requesting video explanation...      │   │
│                         │ │                                         │   │
│                         │ │ 📹 [Video Player - 1.2MB]              │   │
│                         │ │    "Visual Guide to Linear Equations"  │   │
│                         │ │    ▶️ [0:00 / 2:30]                     │   │
│                         │ │                                         │   │
│                         │ └─────────────────────────────────────────┘   │
│                         │                                               │
│                         │ ┌─────────────────────────────────────────┐   │
│                         │ │ 💬 Type your question...                │   │
│                         │ │ [Send] [🎥 Request Video] [🌐 Online]   │   │
│                         │ └─────────────────────────────────────────┘   │
└─────────────────────────┴───────────────────────────────────────────────┘
```

### 2. Exercise Help Interface (Separate Section)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           🧪 EXERCISE HELP                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ❌ Exercise #3: Solve 2x + 7 = 19                                       │
│                                                                         │
│ Your Answer: ❌ x = 5    Correct Answer: ✅ x = 6                       │
│                                                                         │
│ ┌─────────────────────────┬─────────────────────────────────────────┐   │
│ │    🔄 GENERATE NEW      │         ❓ ASK FOR EXPLANATION         │   │
│ │        TRIAL            │                                         │   │
│ │                         │  ┌─────────────────────────────────┐   │   │
│ │ Create a new similar    │  │ 💬 Ask Locally (AI)             │   │   │
│ │ question to practice    │  │                                 │   │   │
│ │                         │  │ 🤖 "You subtracted 7 instead   │   │   │
│ │ [Generate Question]     │  │    of adding. The correct...    │   │   │
│ │                         │  └─────────────────────────────────┘   │   │
│ │                         │                                         │   │
│ │                         │  ┌─────────────────────────────────┐   │   │
│ │                         │  │ 🎥 Request Video (Online)       │   │   │
│ │                         │  │                                 │   │   │
│ │                         │  │ 📹 [Video Player - 0.8MB]      │   │   │
│ │                         │  │    "Why x=5 is Wrong"          │   │   │
│ │                         │  │    ▶️ [0:00 / 1:45]             │   │   │
│ │                         │  └─────────────────────────────────┘   │   │
│ └─────────────────────────┴─────────────────────────────────────────┘   │
│                                                                         │
│ 📚 Previous Explanations for this Exercise:                            │
│ • 🎥 "Why x=5 is Wrong" (2 days ago)                                   │
│ • 💬 "Step-by-step breakdown" (1 week ago)                             │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3. Network State Indicators

```
Online Mode:
┌─────────────────────────────────────────┐
│ [💬 Send] [🎥 Request Video] [🌐 Online] │
└─────────────────────────────────────────┘

Offline Mode:
┌─────────────────────────────────────────┐
│ [💬 Send] [⚠️ Videos unavailable offline]│
└─────────────────────────────────────────┘

Loading Video:
┌─────────────────────────────────────────┐
│ [💬 Send] [⏳ Requesting video...] [❌]  │
└─────────────────────────────────────────┘
```

### 4. Chat Session Management

```
┌─────────────────────────────────────────┐
│         📝 Chat Sessions                │
├─────────────────────────────────────────┤
│ 🔵 "Understanding basics" (Active)      │
│    └─ 5 messages, 2 videos             │
│                                         │
│ ⚫ "Practice problems"                   │
│    └─ 12 messages, 1 video             │
│                                         │
│ ⚫ "Step-by-step solutions"             │
│    └─ 8 messages, 0 videos             │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ ➕ Create New Chat Session          │ │
│ │ 💭 Name: [Enter session name...]    │ │
│ │ [Create] [Cancel]                   │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Data Flow Architecture

### 1. Video Request Flow
```mermaid
sequenceDiagram
    participant User
    participant ChatUI
    participant NetworkService
    participant VideoAPI
    participant LocalStorage
    participant VideoPlayer
    
    User->>ChatUI: Clicks "Request Video"
    ChatUI->>NetworkService: Check network status
    alt Online
        NetworkService-->>ChatUI: Network available
        ChatUI->>VideoAPI: POST chapter/exercise data
        VideoAPI-->>ChatUI: Video URL response
        ChatUI->>LocalStorage: Download and save video
        LocalStorage-->>ChatUI: Video saved locally
        ChatUI->>VideoPlayer: Display video in chat
        VideoPlayer-->>User: Video plays in chat
    else Offline
        NetworkService-->>ChatUI: No network
        ChatUI-->>User: Show "Videos unavailable offline"
    end
```

### 2. Chapter Chat Session Flow
```mermaid
sequenceDiagram
    participant User
    participant ChapterUI
    participant ChatService
    participant SessionManager
    participant AIInference
    participant Database
    
    User->>ChapterUI: Opens chapter
    ChapterUI->>SessionManager: Load chat sessions for chapter
    SessionManager->>Database: Get sessions by chapterId
    Database-->>SessionManager: Return session list
    SessionManager-->>ChapterUI: Display sessions
    
    User->>ChapterUI: Select/Create chat session
    ChapterUI->>ChatService: Initialize session
    
    User->>ChapterUI: Type question
    ChapterUI->>ChatService: Send message
    ChatService->>AIInference: Process with chapter context
    AIInference-->>ChatService: AI response
    ChatService->>Database: Save conversation
    ChatService-->>ChapterUI: Display response
```

### 3. Exercise Help Flow
```mermaid
sequenceDiagram
    participant User
    participant ExerciseUI
    participant HelpService
    participant VideoAPI
    participant AIService
    participant Database
    
    User->>ExerciseUI: Gets answer wrong
    ExerciseUI-->>User: Show help options
    
    alt Generate New Trial
        User->>ExerciseUI: Choose "Generate New"
        ExerciseUI->>AIService: Generate similar question
        AIService-->>ExerciseUI: New trial question
    else Ask for Explanation
        User->>ExerciseUI: Choose "Ask Explanation"
        alt Online - Request Video
            ExerciseUI->>VideoAPI: Send exercise + wrong answer
            VideoAPI-->>ExerciseUI: Video explanation
            ExerciseUI->>Database: Save explanation
        else Offline - Local AI
            ExerciseUI->>AIService: Generate local explanation
            AIService-->>ExerciseUI: Text explanation
        end
    end
```

## Updated Data Models

### Core Data Models (Renamed: Lessons → Chapters)
```kotlin
data class Course(
    val id: String,
    val title: String,
    val subject: Subject,
    val grade: String,
    val description: String,
    val chapters: List<Chapter>, // RENAMED from lessons
    val totalChapters: Int, // RENAMED from totalLessons
    val estimatedHours: Int,
    val imageUrl: String? = null,
    val isDownloaded: Boolean = false,
    val downloadUrl: String? = null,
    val sizeInBytes: Long = 0L,
    val version: String = "1.0",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Chapter( // RENAMED from Lesson
    val id: String,
    val courseId: String,
    val chapterNumber: Int, // RENAMED from lessonNumber
    val title: String,
    val markdownContent: String,
    val imageReferences: List<String> = emptyList(),
    val estimatedReadingTime: Int,
    val isCompleted: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val chatSessions: List<ChatSession> = emptyList(), // NEW
    val createdAt: Long = System.currentTimeMillis()
)

// NEW: Video Explanation Entity
data class VideoExplanation(
    val id: String,
    val userId: String, // User-specific, no sharing
    val chapterId: String? = null, // For chapter videos
    val exerciseId: String? = null, // For exercise videos
    val requestType: VideoRequestType,
    val userQuestion: String,
    val contextData: String, // JSON of chapter markdown or exercise data
    val videoUrl: String, // Original API URL
    val localFilePath: String, // Local storage path
    val fileSizeBytes: Long,
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)

enum class VideoRequestType {
    CHAPTER_EXPLANATION,
    EXERCISE_HELP
}

// Updated: Chapter-specific Chat Sessions
data class ChatSession(
    val id: String,
    val title: String,
    val chapterId: String, // Always linked to a chapter
    val courseId: String,
    val userId: String, // User-specific sessions
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val messageCount: Int = 0,
    val videoCount: Int = 0 // Track videos in session
)

// Updated: Enhanced Chat Messages
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType,
    val videoExplanation: VideoExplanation? = null, // NEW: Embedded video
    val mathSteps: List<MathStep> = emptyList(),
    val status: MessageStatus = MessageStatus.SENT,
    val relatedExerciseId: String? = null,
    val processingTimeMs: Long = 0L
)

enum class MessageType {
    TEXT,
    VIDEO, // NEW
    IMAGE,
    MATH_SOLUTION,
    EXERCISE_HELP
}

// NEW: Exercise Help System
data class ExerciseHelp(
    val id: String,
    val exerciseId: String,
    val userId: String,
    val incorrectAnswer: Int,
    val correctAnswer: Int,
    val userQuestion: String? = null,
    val helpType: HelpType,
    val explanation: String? = null, // For local AI explanations
    val videoExplanation: VideoExplanation? = null, // For video explanations
    val createdAt: Long = System.currentTimeMillis(),
    val wasHelpful: Boolean? = null // User feedback
)

enum class HelpType {
    LOCAL_AI,
    VIDEO_EXPLANATION,
    NEW_TRIAL
}

// NEW: Network State Management
data class NetworkState(
    val isOnline: Boolean,
    val connectionType: ConnectionType,
    val lastOnlineAt: Long = 0L,
    val videoRequestsEnabled: Boolean = false
)

enum class ConnectionType {
    NONE,
    WIFI,
    CELLULAR,
    UNKNOWN
}
```

### Database Schema Updates (Room)

```kotlin
@Entity(tableName = "chapters") // RENAMED from lessons
data class ChapterEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val chapterNumber: Int, // RENAMED
    val title: String,
    val markdownContent: String,
    val imageReferences: String, // JSON array
    val estimatedReadingTime: Int,
    val isCompleted: Boolean,
    val createdAt: Long
)

@Entity(tableName = "video_explanations") // NEW
data class VideoExplanationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val chapterId: String?,
    val exerciseId: String?,
    val requestType: String, // VideoRequestType enum
    val userQuestion: String,
    val contextData: String, // JSON
    val videoUrl: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val durationSeconds: Int,
    val createdAt: Long,
    val lastAccessedAt: Long
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val chapterId: String, // Always required
    val courseId: String,
    val userId: String, // NEW: User-specific
    val createdAt: Long,
    val lastMessageAt: Long,
    val isActive: Boolean,
    val messageCount: Int,
    val videoCount: Int // NEW
)

@Entity(tableName = "exercise_help") // NEW
data class ExerciseHelpEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val userId: String,
    val incorrectAnswer: Int,
    val correctAnswer: Int,
    val userQuestion: String?,
    val helpType: String, // HelpType enum
    val explanation: String?,
    val videoExplanationId: String?, // FK to video_explanations
    val createdAt: Long,
    val wasHelpful: Boolean?
)

@Entity(tableName = "network_state") // NEW
data class NetworkStateEntity(
    @PrimaryKey val userId: String,
    val isOnline: Boolean,
    val connectionType: String,
    val lastOnlineAt: Long,
    val videoRequestsEnabled: Boolean,
    val lastUpdated: Long
)
```

## API Integration Layer

### Video Request Service
```kotlin
interface VideoExplanationService {
    
    @POST("api/video/chapter")
    suspend fun requestChapterVideo(
        @Body request: ChapterVideoRequest
    ): VideoResponse
    
    @POST("api/video/exercise")
    suspend fun requestExerciseVideo(
        @Body request: ExerciseVideoRequest
    ): VideoResponse
}

data class ChapterVideoRequest(
    val chapterMarkdown: String,
    val userQuestion: String,
    val userId: String,
    val chapterId: String
)

data class ExerciseVideoRequest(
    val exerciseText: String,
    val options: List<String>,
    val correctChoice: Int,
    val incorrectChoice: Int,
    val userQuestion: String?,
    val userId: String,
    val exerciseId: String
)

data class VideoResponse(
    val videoUrl: String,
    val title: String,
    val durationSeconds: Int,
    val fileSizeBytes: Long,
    val format: String = "mp4",
    val success: Boolean,
    val errorMessage: String? = null
)
```

### Local Video Manager
```kotlin
@Singleton
class VideoManager @Inject constructor(
    private val context: Context,
    private val database: ElizaDatabase
) {
    
    suspend fun downloadAndStoreVideo(
        videoResponse: VideoResponse,
        explanation: VideoExplanation
    ): Result<String>
    
    suspend fun getLocalVideoPath(videoId: String): String?
    
    suspend fun deleteVideo(videoId: String): Result<Unit>
    
    suspend fun getStorageUsage(): StorageInfo
    
    suspend fun cleanupOldVideos(maxAgeMillis: Long): List<String>
}

data class StorageInfo(
    val totalVideos: Int,
    val totalSizeBytes: Long,
    val availableSpaceBytes: Long,
    val oldestVideoDate: Long
)
```

## Component Architecture

### 1. Chapter Chat Service
```kotlin
@Singleton
class ChapterChatService @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiService: ElizaChatService,
    private val videoService: VideoExplanationService,
    private val videoManager: VideoManager,
    private val networkMonitor: NetworkMonitor
) {
    
    suspend fun sendTextMessage(
        sessionId: String,
        message: String
    ): Flow<ChatResponse>
    
    suspend fun requestVideoExplanation(
        sessionId: String,
        chapterId: String,
        userQuestion: String
    ): Flow<VideoRequestResult>
    
    fun getSessionsForChapter(chapterId: String): Flow<List<ChatSession>>
    
    suspend fun createNewSession(
        chapterId: String,
        title: String
    ): ChatSession
}
```

### 2. Exercise Help Service
```kotlin
@Singleton  
class ExerciseHelpService @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val aiService: ElizaChatService,
    private val videoService: VideoExplanationService,
    private val networkMonitor: NetworkMonitor
) {
    
    suspend fun generateNewTrial(exerciseId: String): Trial
    
    suspend fun getLocalExplanation(
        exerciseId: String,
        incorrectAnswer: Int,
        userQuestion: String?
    ): ExerciseHelp
    
    suspend fun requestVideoExplanation(
        exerciseId: String,
        incorrectAnswer: Int,
        userQuestion: String?
    ): Flow<VideoRequestResult>
    
    fun getHelpHistory(exerciseId: String): Flow<List<ExerciseHelp>>
}
```

### 3. Network-Aware UI Components
```kotlin
@Composable
fun VideoRequestButton(
    onRequestVideo: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val networkState by networkMonitor.networkState.collectAsState()
    
    when {
        !networkState.isOnline -> {
            Button(
                onClick = { },
                enabled = false,
                modifier = modifier
            ) {
                Icon(Icons.Default.WifiOff, contentDescription = null)
                Text("Videos unavailable offline")
            }
        }
        isLoading -> {
            Button(
                onClick = { },
                enabled = false,
                modifier = modifier
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Text("Requesting video...")
            }
        }
        else -> {
            Button(
                onClick = onRequestVideo,
                modifier = modifier
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Text("Request Video")
            }
        }
    }
}

@Composable
fun ChatInterface(
    chapterId: String,
    sessions: List<ChatSession>,
    activeSessionId: String?,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onRequestVideo: (String) -> Unit,
    onCreateSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation with split layout matching design
}
```

## Performance Considerations

### Video Management
- Maximum 2MB per video file
- Local storage cleanup for videos older than 30 days
- Lazy loading of video thumbnails
- Background video downloads with progress indicators

### Chat Performance
- Limit to 50 messages per session for UI performance
- Lazy loading of older messages
- Efficient database queries with proper indexing
- Message pagination for large conversations

### Network Optimization
- Retry mechanism for failed video requests
- Graceful degradation when network is poor
- Request queuing for offline-to-online transitions
- Bandwidth-aware video quality selection

## Testing Strategy

### UI Testing
- Chapter interface layout responsiveness
- Chat session creation and switching
- Video player integration
- Network state change handling

### Integration Testing  
- Video download and playback flow
- Chat message persistence
- Exercise help system workflow
- Offline/online mode transitions

### Performance Testing
- Video storage and retrieval speed
- Chat interface responsiveness with many sessions
- Memory usage during video playback
- Battery consumption during video requests