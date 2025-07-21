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

#### Story 1.2: Chapter-Based Chat Interface
**As a** student  
**I want** to ask the AI tutor questions about specific chapters with organized chat sessions  
**So that** I can have focused conversations about particular topics

**Acceptance Criteria:**
- WHEN a user opens a chapter THE SYSTEM SHALL display content on left and chat interface on right (Gallery-style layout)
- WHEN a user creates a new chat session THE SYSTEM SHALL allow naming and organizing multiple conversations per chapter
- WHEN a user types a question THE SYSTEM SHALL respond using local Gemma-3n inference within 3 seconds
- WHEN a user switches between chat sessions THE SYSTEM SHALL preserve all conversation history
- WHEN the AI cannot answer a question THE SYSTEM SHALL suggest requesting a video explanation

#### Story 1.3: Video Explanation System (NEW FEATURE)
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

#### Story 1.4: Image-Based Problem Solving
**As a** student  
**I want** to take photos of math problems and get explanations  
**So that** I can understand problems from textbooks or assignments

**Acceptance Criteria:**
- WHEN a user uploads an image with math content THE SYSTEM SHALL analyze the image using Gemma-3n vision capabilities
- WHEN the image contains a math problem THE SYSTEM SHALL provide step-by-step solution
- WHEN the image is unclear or contains no math THE SYSTEM SHALL ask for a clearer image
- WHEN processing an image THE SYSTEM SHALL show loading indicator

#### Story 1.5: Exercise Testing and Help System (NEW FEATURE)
**As a** student  
**I want** to test myself on chapter content and get help with wrong answers  
**So that** I can identify and address my knowledge gaps

**Acceptance Criteria:**
- WHEN a user completes an exercise incorrectly THE SYSTEM SHALL provide two options: "Generate New Trial" or "Ask for Explanation"
- WHEN user selects "Generate New Trial" THE SYSTEM SHALL create a new similar question using AI
- WHEN user selects "Ask for Explanation" THE SYSTEM SHALL offer local AI explanation or video explanation (if online)
- WHEN requesting exercise video explanation THE SYSTEM SHALL send POST with exercise, options, correct/incorrect choices, and user question
- WHEN explanation is received THE SYSTEM SHALL display in separate Exercise Help section
- WHEN user accesses Exercise Help THE SYSTEM SHALL show history of all explanations for that exercise

**Exercise Video API Payload:**
```json
{
  "exerciseText": "Complete exercise question",
  "options": ["option1", "option2", "option3", "option4"],
  "correctChoice": 0,
  "incorrectChoice": 2,
  "userQuestion": "Why is my answer wrong?",
  "userId": "unique_user_identifier"
}
```

#### Story 1.6: Course Content System with Chapter Organization
**As a** student  
**I want** to access structured math courses organized by chapters  
**So that** I can learn topics systematically with chat support

**Acceptance Criteria:**
- WHEN a user opens the courses section THE SYSTEM SHALL display available math topics (Algebra, Geometry, etc.)
- WHEN a user selects a course THE SYSTEM SHALL show chapters with progress indicators
- WHEN a user opens a chapter THE SYSTEM SHALL display content with chat interface sidebar
- WHEN a user completes reading a chapter THE SYSTEM SHALL mark it as complete and unlock next chapter
- WHEN a user returns to a chapter THE SYSTEM SHALL restore all previous chat sessions

#### Story 1.7: Enhanced Progress Tracking
**As a** student  
**I want** to see my learning progress including chat activity and video requests  
**So that** I can track my improvement and learning patterns

**Acceptance Criteria:**
- WHEN a user completes activities THE SYSTEM SHALL save progress locally including chat sessions and video requests
- WHEN a user opens the progress screen THE SYSTEM SHALL show chapters covered, time spent, videos requested, and chat activity
- WHEN a user has no progress data THE SYSTEM SHALL display welcome message with getting started tips
- WHEN the app is reopened THE SYSTEM SHALL restore previous progress, conversation history, and locally stored videos

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

### User Experience
- Successfully solve uploaded math problems: 80% accuracy
- Complete basic tutoring conversation: 90% success rate
- Successfully request and view video explanations: 85% success rate
- Navigate between screens without crashes: 99% reliability
- Demonstrate core features including video system in 5-minute demo: 100% completion

### Network Management
- Proper online/offline detection: 99% accuracy
- Graceful handling of network transitions: 100% success rate
- Video download success rate: 90% when online

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

## New Technical Requirements

### Video API Integration
- REST API endpoint for chapter video requests
- REST API endpoint for exercise video requests  
- Video format: MP4, max 2MB file size
- Local storage management for videos
- Network detection and management

### Data Storage Extensions
- User-specific video explanation storage
- Chapter-based chat session organization
- Exercise help system data models
- Video metadata and file management

### Existing Infrastructure Enhancement
- Enhance existing SimpleNetworkMonitor (core:data) for actual connectivity detection
- Utilize existing NetworkMonitor.isOnline Flow<Boolean> for video feature availability
- Leverage existing ElizaAppState.isOffline for UI state management