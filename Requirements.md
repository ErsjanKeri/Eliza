# AI Tutor App - Requirements Specification

## Project Overview
**Timeline**: 21 days  
**Target**: Functional MVP demo  
**AI Model**: Gemma-3n-E4B-it-int4 (4.4GB, text + vision)  
**Architecture**: nowinandroid modular + gallery-edge-ai integration

## User Stories with EARS Notation

### Epic 1: Core AI Tutoring Experience

#### Story 1.1: Model Setup and Initialization
**As a** student user  
**I want** the AI model to be automatically downloaded and ready to use  
**So that** I can start getting tutoring help immediately

**Acceptance Criteria:**
- WHEN the app is launched for the first time THE SYSTEM SHALL automatically download the Gemma-3n model with progress indicator
- WHEN the model download is complete THE SYSTEM SHALL initialize the model and show "Ready" status
- WHEN the model initialization fails THE SYSTEM SHALL display error message and retry option
- WHEN the model is ready THE SYSTEM SHALL enable the chat interface within 10 seconds

#### Story 1.2: Enhanced Full-Screen Chat Interface with Context Awareness ✨ **UPDATED**
**As a** student  
**I want** to access a powerful, full-screen chat interface with intelligent context organization  
**So that** I can have focused, context-aware conversations with proper RAG support

**Acceptance Criteria:**
- WHEN a user activates chat THE SYSTEM SHALL open a full-screen widget filling 99% of the screen
- WHEN chat opens THE SYSTEM SHALL completely hide chapter content and provide top-right return button
- WHEN a user selects text in chapter markdown THE SYSTEM SHALL provide "Ask Chat" option that starts prompt with quoted text
- WHEN a user creates chats THE SYSTEM SHALL organize them hierarchically: Course > Chapter > (General Chapter Chat + Question-Specific Chats)
- WHEN in general chat navigation THE SYSTEM SHALL show all chats across all courses/chapters
- WHEN in chapter-specific view THE SYSTEM SHALL only show chats within that chapter (others disabled)
- WHEN a user types a question THE SYSTEM SHALL respond using shared Gemma model with RAG context injection
- WHEN switching between chats THE SYSTEM SHALL preserve all conversation history with no limits
- WHEN AI responds THE SYSTEM SHALL automatically inject relevant chapter/course/question context via RAG

#### Story 1.3: Chapter Test System with "Best Attempt" Progress ✨ **IMPLEMENTED**
**As a** student  
**I want** to take chapter tests with persistent progress tracking that remembers my best performance  
**So that** I can practice questions multiple times without losing progress on questions I've already mastered

**Acceptance Criteria:**
- WHEN a user completes a chapter test THE SYSTEM SHALL save all individual answers as UserAnswer records for detailed tracking
- WHEN a user answers a question correctly THE SYSTEM SHALL mark it as permanently completed (Exercise.isCompleted = true)
- WHEN a user retakes a test THE SYSTEM SHALL show fresh questions (no pre-filling) but track best attempt progress separately
- WHEN a user completes a test THE SYSTEM SHALL display real user answers from saved data (not "No answer provided")
- WHEN a user navigates to test results THE SYSTEM SHALL reconstruct results from UserAnswer records if navigation data is lost
- WHEN a user gets 100% on a test THE SYSTEM SHALL mark the chapter as completed and unlock next chapter
- WHEN a user gets less than 100% THE SYSTEM SHALL allow unlimited retakes while preserving individual question progress
- WHEN a user views test results THE SYSTEM SHALL show both current test score AND permanent progress indicators

**Data Persistence Requirements:**
- WHEN a test is submitted THE SYSTEM SHALL save UserAnswer records for each question attempt
- WHEN a question is answered correctly THE SYSTEM SHALL update Exercise.isCompleted = true permanently
- WHEN calculating chapter completion THE SYSTEM SHALL use permanent progress (count of Exercise.isCompleted)
- WHEN loading test results THE SYSTEM SHALL reconstruct from saved UserAnswer data if ViewModel state is lost

#### Story 1.4: Enhanced Navigation System ✨ **IMPLEMENTED** 
**As a** student  
**I want** reliable navigation between test results and other screens without getting stuck  
**So that** I can smoothly move through the learning experience

**Acceptance Criteria:**
- WHEN a user clicks back button from test results THE SYSTEM SHALL navigate directly to chapter content (not create navigation loops)
- WHEN a user completes a test THE SYSTEM SHALL provide clear navigation options to chapter content and main home page
- WHEN a user wants to return to main app THE SYSTEM SHALL provide "Main Home" button that navigates to HOME_BASE_ROUTE
- WHEN navigation occurs THE SYSTEM SHALL maintain test result data without losing user answers or scores

#### Story 1.5: Intelligent Text Selection and Chat Integration ✨ **NEW**
**As a** student  
**I want** to select any text in chapter content and instantly ask questions about it  
**So that** I can get immediate explanations for specific concepts while reading

**Acceptance Criteria:**
- WHEN a user selects text in chapter markdown THE SYSTEM SHALL highlight the selection
- WHEN text is selected THE SYSTEM SHALL show "Ask Chat" contextual option
- WHEN user clicks "Ask Chat" THE SYSTEM SHALL open full-screen chat interface
- WHEN chat opens from text selection THE SYSTEM SHALL pre-fill prompt with quoted selected text
- WHEN chat starts from selection THE SYSTEM SHALL automatically tag conversation with chapter/course context
- WHEN user asks about selected text THE SYSTEM SHALL inject relevant section context via RAG
- WHEN returning to chapter THE SYSTEM SHALL preserve text selection state

#### Story 1.6: Video Explanation System (NEW FEATURE)
**As a** student  
**I want** to request video explanations for chapter concepts I don't understand  
**So that** I can get visual and auditory learning support beyond text responses

**Acceptance Criteria:**
- WHEN a user clicks "Request Video" button THE SYSTEM SHALL send POST request with chapter markdown and user question
- WHEN internet is available THE SYSTEM SHALL show video request button and process requests
- WHEN internet is unavailable THE SYSTEM SHALL hide video request button and show offline indicator
- WHEN video is received THE SYSTEM SHALL download and store locally (1-2MB max) and display in chat session
- WHEN video request fails THE SYSTEM SHALL show error message and suggest trying local AI response
- WHEN user accesses previous videos THE SYSTEM SHALL load from local storage instantly

**API Payload:**
```json
{
  "chapterMarkdown": "# Chapter content...",
  "userQuestion": "User's specific question",
  "userId": "unique_user_identifier"
}
```

#### Story 1.7: Gallery Model Integration and Shared AI Infrastructure ✨ **NEW**
**As a** student  
**I want** a unified AI model system that powers both testing and chat with optimal performance  
**So that** I can have consistent, high-quality AI interactions across all features

**Acceptance Criteria:**
- WHEN the app initializes THE SYSTEM SHALL load the shared Gemma model from gallery integration
- WHEN using chat or test features THE SYSTEM SHALL utilize the same model instance for consistency
- WHEN model is loaded THE SYSTEM SHALL optimize memory management for shared usage
- WHEN switching between test and chat THE SYSTEM SHALL maintain model state efficiently
- WHEN model processes requests THE SYSTEM SHALL provide consistent response quality across features

**Technical Questions for Implementation:**
- Which specific Gemma model variant should be utilized for optimal performance?
- How should memory management handle shared model usage between test and chat systems?
- What are the performance implications of model sharing vs separate instances?

#### Story 1.8: Exercise Help System with Full-Screen Chat Integration ✨ **COMPLETELY REDESIGNED**
**As a** student  
**I want** to get help with wrong answers through a seamless full-screen chat experience that copies the gallery chat interface  
**So that** I can understand my mistakes in an intuitive, conversational way with persistent help history

**Acceptance Criteria:**
- WHEN a user completes an exercise incorrectly in a test THE SYSTEM SHALL provide "Local AI Explanation" and "Video Explanation" buttons in test results
- WHEN user clicks any exercise help button THE SYSTEM SHALL open a full-screen chat interface (99% screen coverage) that copies gallery chat 100% exactly
- WHEN exercise help chat opens THE SYSTEM SHALL automatically create a new chat session with title format "Exercise #X Help: [question text]"
- WHEN user requests help for the same exercise multiple times THE SYSTEM SHALL create new numbered sessions: "(2)", "(3)", etc.
- WHEN exercise help chat loads THE SYSTEM SHALL pre-populate context: question text, user's answer, correct answer, and chapter content via RAG
- WHEN user interacts in exercise help chat THE SYSTEM SHALL provide contextually appropriate responses using chapter content and exercise details
- WHEN user requests video explanation THE SYSTEM SHALL integrate video as a chat message (not separate interface)
- WHEN user finishes getting help and returns THE SYSTEM SHALL preserve the exact accordion state (same question expanded) in test results
- WHEN user accesses exercise help chats later THE SYSTEM SHALL organize them hierarchically: Course > Chapter > Exercise Help category
- WHEN multiple exercise help sessions exist THE SYSTEM SHALL display them in organized list with proper numbering and timestamps

**Critical Technical Requirements:**
- **Complete Removal**: All existing split-screen exercise help components must be completely deleted
- **Gallery Integration**: Exercise help chat must copy gallery chat interface 100% exactly (same components, same behavior)
- **Session Management**: Each help request creates NEW chat session (never reuse existing sessions)
- **State Preservation**: Return navigation must preserve exact test results accordion state
- **RAG Context**: Automatic injection of chapter markdown + exercise details + user attempt history
- **Video Integration**: Video explanations work as chat messages within exercise help sessions
- **Hierarchical Organization**: Exercise help chats organized under proper course/chapter hierarchy

**OLD APPROACH (REMOVED)**: Split-screen "Generate New Trial" | "Ask for Explanation" layout  
**NEW APPROACH**: Full-screen chat sessions that integrate with existing chat system architecture

**Exercise Help Chat Categories:**
```
Course: Algebra Basics
  Chapter: Linear Equations
    📚 General Chapter Discussion
    ❓ Exercise Help                    ← NEW specialized category
      • Exercise #1 Help: Solve 2x + 5 = 15
      • Exercise #1 Help: Solve 2x + 5 = 15 (2)
      • Exercise #3 Help: Find x in 3x - 7 = 14
    📝 Text Questions
      • "How to solve complex equations?" (from text selection)
```

#### Story 1.9: Advanced RAG Context System ✨ **NEW**
**As a** student  
**I want** the AI to automatically understand the context of my learning and provide relevant responses  
**So that** I get personalized, contextually appropriate explanations without manual setup

**Acceptance Criteria:**
- WHEN a user chats within a chapter THE SYSTEM SHALL automatically inject chapter content as RAG context
- WHEN discussing a specific question THE SYSTEM SHALL include question, exercise, and related material in context
- WHEN user asks follow-up questions THE SYSTEM SHALL maintain conversation context across chat history
- WHEN switching between different chapter chats THE SYSTEM SHALL update RAG context appropriately
- WHEN providing responses THE SYSTEM SHALL reference specific sections, examples, or concepts from chapter content
- WHEN context is ambiguous THE SYSTEM SHALL ask clarifying questions to better target assistance

**Technical Questions for Implementation:**
- How granular should RAG context be? (entire chapter vs specific sections vs question-only)
- What is the optimal context window size for effective RAG responses?
- How should the system handle context conflicts when discussing multiple topics?
- Should RAG context include previous chat history, test attempts, or user progress data?

#### Story 1.10: Image-Based Problem Solving
**As a** student  
**I want** to take photos of math problems and get explanations  
**So that** I can understand problems from textbooks or assignments

**Acceptance Criteria:**
- WHEN a user uploads an image with math content THE SYSTEM SHALL analyze the image using Gemma-3n vision capabilities
- WHEN the image contains a math problem THE SYSTEM SHALL provide step-by-step solution
- WHEN the image is unclear or contains no math THE SYSTEM SHALL ask for a clearer image
- WHEN processing an image THE SYSTEM SHALL show loading indicator

#### Story 1.11: Course Content System with Chapter Organization
**As a** student  
**I want** to access structured math courses organized by chapters  
**So that** I can learn topics systematically with test and chat support

**Acceptance Criteria:**
- WHEN a user opens the courses section THE SYSTEM SHALL display available math topics (Algebra, Geometry, etc.)
- WHEN a user selects a course THE SYSTEM SHALL show chapters with progress indicators
- WHEN a user opens a chapter THE SYSTEM SHALL display content with test and chat interface options
- WHEN a user completes a chapter test with 100% THE SYSTEM SHALL mark chapter as complete and unlock next chapter
- WHEN a user returns to a chapter THE SYSTEM SHALL restore all previous progress, test results, and chat sessions

#### Story 1.12: Enhanced Progress Tracking with Test History ✨ **UPDATED**
**As a** student  
**I want** to see my learning progress including test attempts, best scores, and permanent question progress  
**So that** I can track my improvement and understand my learning patterns

**Acceptance Criteria:**
- WHEN a user completes test attempts THE SYSTEM SHALL save detailed UserAnswer records for each question
- WHEN a user opens the progress screen THE SYSTEM SHALL show chapters covered, test scores, attempt history, and permanent question progress
- WHEN a user views chapter progress THE SYSTEM SHALL display both latest test score and permanent completion status
- WHEN a user has multiple test attempts THE SYSTEM SHALL show score progression and improvement trends
- WHEN the app is reopened THE SYSTEM SHALL restore all progress, test history, and conversation data from persistent storage

### Epic 2: Performance and Reliability

#### Story 2.1: Efficient Model Management
**As a** user  
**I want** the app to run smoothly without consuming excessive resources  
**So that** I can use it on my device without issues

**Acceptance Criteria:**
- WHEN the model is loaded THE SYSTEM SHALL use no more than 6GB of RAM
- WHEN generating responses THE SYSTEM SHALL complete inference within 3 seconds
- WHEN the device is low on memory THE SYSTEM SHALL gracefully handle memory pressure
- WHEN the app is backgrounded THE SYSTEM SHALL efficiently manage model resources

#### Story 2.2: Offline Functionality with Smart Feature Management
**As a** user  
**I want** core functionality without internet connection with clear indication of online features  
**So that** I can learn anywhere with appropriate expectations

**Acceptance Criteria:**
- WHEN the device has no internet THE SYSTEM SHALL continue to provide AI tutoring and local explanations
- WHEN offline THE SYSTEM SHALL hide video request buttons and show offline indicators
- WHEN offline THE SYSTEM SHALL save all progress and conversations locally
- WHEN internet is restored THE SYSTEM SHALL enable video features and sync data
- WHEN switching between online/offline THE SYSTEM SHALL maintain seamless experience for available features

### Epic 3: Video and Network Management (NEW)

#### Story 3.1: Network-Aware Interface
**As a** user  
**I want** the interface to adapt based on my internet connectivity  
**So that** I understand which features are available

**Acceptance Criteria:**
- WHEN internet is detected THE SYSTEM SHALL show video request buttons with online indicator
- WHEN internet is lost THE SYSTEM SHALL hide video buttons and show offline mode indicator
- WHEN network is unstable THE SYSTEM SHALL show connection status and retry options
- WHEN video request is in progress THE SYSTEM SHALL show loading indicator and allow cancellation

#### Story 3.2: Local Video Management
**As a** user  
**I want** my requested videos to be stored locally and easily accessible  
**So that** I can review explanations without needing internet

**Acceptance Criteria:**
- WHEN a video is successfully downloaded THE SYSTEM SHALL store it locally with metadata
- WHEN a user requests a previously downloaded video THE SYSTEM SHALL load instantly from local storage
- WHEN storage space is low THE SYSTEM SHALL offer option to delete old videos
- WHEN user deletes the app THE SYSTEM SHALL clean up all locally stored videos

## Success Metrics for MVP Demo

### Technical Performance
- Model initialization time: < 10 seconds
- AI response time: < 3 seconds (95th percentile)
- Video request response time: < 15 seconds
- Memory usage: < 6GB peak
- App startup time: < 3 seconds
- Video storage efficiency: < 2MB per video
- **Test submission time: < 2 seconds** ✨ **NEW**
- **Test results load time: < 1 second** ✨ **NEW**
- **Navigation transition time: < 500ms** ✨ **NEW**

### User Experience
- Successfully solve uploaded math problems: 80% accuracy
- Complete basic tutoring conversation: 90% success rate
- **Successfully create exercise help chat from test results: 100% success rate** ✨ **NEW**
- **Navigate to full-screen exercise help chat: 100% success rate** ✨ **NEW**
- **Return from exercise help chat with preserved accordion state: 100% success rate** ✨ **NEW**
- Successfully request and view video explanations: 85% success rate
- Navigate between screens without crashes: 99% reliability
- Demonstrate core features including **exercise help chat system** in 5-minute demo: 100% completion
- **Complete chapter test without data loss: 100% success rate** ✨ **NEW**
- **View accurate test results showing real answers: 100% success rate** ✨ **NEW**
- **Navigate from test results without getting stuck: 100% success rate** ✨ **NEW**
- **Retake tests while preserving question progress: 100% success rate** ✨ **NEW**

### Exercise Help Chat System Performance ✨ **NEW CRITICAL**
- **Gallery chat integration accuracy: 100%** (UI must be identical)
- **Exercise help chat creation success rate: 100%**
- **Chat session numbering accuracy: 100%** (proper (2), (3) numbering)
- **Navigation state preservation: 100%** (accordion state maintained)
- **RAG context injection success rate: 100%** (chapter + exercise context)
- **Video message integration in chat: 100%** (videos work as chat messages)
- **Old split-screen component removal: 100%** (no remnants allowed)
- **Exercise help chat persistence: 100%** (chats saved and accessible later)

### Network Management
- Proper online/offline detection: 99% accuracy
- Graceful handling of network transitions: 100% success rate
- Video download success rate: 90% when online

### Test System Performance ✨ **NEW**
- **Test data persistence success rate: 100%**
- **UserAnswer record accuracy: 100%**
- **Exercise progress preservation: 100%**
- **Navigation data reconstruction: 100% success rate**

## Out of Scope for MVP

### Features Deferred to Future Versions
- Advanced course authoring tools
- Multi-user support
- Cloud synchronization of videos (all local storage only)
- Detailed analytics dashboard
- Advanced assessment tools
- Speech recognition
- Collaborative features
- Parent/teacher portals
- Video caching between users
- Advanced video features (playback speed, annotations)

## Technical Requirements

### Chapter Test System ✨ **NEW - IMPLEMENTED**
- **UserAnswer Entity**: Detailed tracking of every test attempt with exerciseId, userId, selectedAnswer, isCorrect, timestamp
- **Exercise Progress Persistence**: Exercise.isCompleted field for permanent "best attempt" progress
- **Test Result Reconstruction**: Load test results from UserAnswer records when navigation data is lost
- **Navigation Flow Management**: Direct navigation to chapter content, avoid back stack loops
- **Multi-attempt Support**: Fresh questions each test, but permanent progress tracking
- **Chapter Completion Logic**: 100% test score required, based on permanent Exercise.isCompleted status

### Video API Integration
- REST API endpoint for chapter video requests
- REST API endpoint for exercise video requests  
- Video format: MP4, max 2MB file size
- Local storage management for videos
- Network detection and management

### Data Storage Extensions
- **User-specific video explanation storage**
- **Chapter-based chat session organization**
- **Exercise help system data models**
- **Video metadata and file management**
- **UserAnswer tracking for detailed test attempt history** ✨ **NEW**
- **Exercise completion state persistence** ✨ **NEW**
- **Test result data reconstruction capabilities** ✨ **NEW**

### Navigation Architecture ✨ **NEW - IMPLEMENTED**
- **Direct chapter navigation from test results**
- **Main home page navigation option**
- **Navigation loop prevention**
- **State preservation during navigation transitions**
- **Multiple navigation paths: Chapter content, Main home, Test retake**

### Existing Infrastructure Enhancement
- Enhance existing SimpleNetworkMonitor (core:data) for actual connectivity detection
- Utilize existing NetworkMonitor.isOnline Flow<Boolean> for video feature availability
- Leverage existing ElizaAppState.isOffline for UI state management
- **Enhance ChapterTestViewModel for robust test data handling** ✨ **NEW**
- **Implement loadChapterForResults for navigation data recovery** ✨ **NEW**
- **Integrate ProgressRepository.recordAnswer for UserAnswer persistence** ✨ **NEW**

#### Story 1.8.1: Gallery Chat Interface Integration ✨ **NEW CRITICAL DEPENDENCY**
**As a** developer implementing exercise help  
**I want** to copy the gallery chat interface 100% exactly for exercise help functionality  
**So that** users have a consistent, proven chat experience across all AI interactions

**Acceptance Criteria:**
- WHEN implementing exercise help chat THE SYSTEM SHALL analyze gallery chat interface components extensively
- WHEN creating exercise help UI THE SYSTEM SHALL copy gallery chat composables 100% exactly (layout, styling, behavior)
- WHEN exercise help chat loads THE SYSTEM SHALL use identical message display, input handling, and navigation patterns as gallery
- WHEN video messages appear in exercise help THE SYSTEM SHALL use same video message components as gallery chat
- WHEN user interacts with exercise help chat THE SYSTEM SHALL provide identical user experience to gallery chat
- WHEN exercise help chat is implemented THE SYSTEM SHALL maintain all gallery chat features: message history, video integration, loading states
- WHEN development is complete THE SYSTEM SHALL have zero differences between gallery chat and exercise help chat interfaces

**Technical Requirements:**
- **Component Reuse**: Identify and copy exact gallery chat composables
- **UI Consistency**: Maintain identical visual design and behavior patterns
- **Feature Parity**: All gallery chat features must work in exercise help context
- **Code Standards**: Follow same architecture patterns and code organization as gallery

**Dependencies:**
- Gallery project analysis must be completed first
- Gallery chat interface components must be fully understood
- Exercise help context integration must not break gallery chat patterns

#### Story 1.8.2: Split-Screen Exercise Help Removal ✨ **NEW CRITICAL TASK**
**As a** developer cleaning up the codebase  
**I want** to completely remove all existing split-screen exercise help components  
**So that** there are no conflicts or confusion with the new full-screen chat approach

**Acceptance Criteria:**
- WHEN removing old exercise help THE SYSTEM SHALL delete all split-screen exercise help UI components completely
- WHEN cleaning up code THE SYSTEM SHALL remove all "Generate New Trial" | "Ask for Explanation" side-by-side layouts
- WHEN updating navigation THE SYSTEM SHALL remove all references to old exercise help interfaces
- WHEN verifying removal THE SYSTEM SHALL have zero remaining split-screen exercise help components
- WHEN testing the app THE SYSTEM SHALL show no traces of old exercise help UI anywhere
- WHEN build is complete THE SYSTEM SHALL compile successfully with all old components removed

**Removal Checklist:**
- [ ] Delete `ExerciseHelpInterface` component completely
- [ ] Remove split-screen layout components
- [ ] Delete old exercise help navigation
- [ ] Remove old help button implementations
- [ ] Clean up unused imports and references
- [ ] Verify no build errors after removal

### Exercise Help Chat System ✨ **NEW CRITICAL REQUIREMENT**
- **Gallery Chat Integration**: Copy gallery chat interface 100% exactly for exercise help
- **Split-Screen Removal**: Complete deletion of all existing exercise help UI components
- **Full-Screen Chat Sessions**: 99% screen coverage for exercise help (like gallery)
- **Chat Session Management**: Automatic creation with proper title format "Exercise #X Help: [question]"
- **Session Numbering**: Multiple help sessions numbered as (2), (3), etc.
- **Navigation State Preservation**: Return to test results with exact accordion state preserved
- **RAG Context Integration**: Automatic injection of chapter markdown + exercise details + user history
- **Video Message Integration**: Video explanations as chat messages (not separate interface)
- **Hierarchical Organization**: Exercise help chats under proper course/chapter hierarchy
- **Session Persistence**: All exercise help chats saved permanently for future reference

### Chat System Architecture ✨ **ENHANCED**
- **Gallery Component Reuse**: Exact same composables and patterns as gallery chat
- **Exercise Context Support**: Enhanced chat sessions with exercise-specific data
- **Multiple Chat Categories**: General Discussion + Exercise Help + Text Questions
- **Video Message Types**: Integrated video support within chat conversations
- **State Management**: Proper preservation of UI state across chat navigation transitions