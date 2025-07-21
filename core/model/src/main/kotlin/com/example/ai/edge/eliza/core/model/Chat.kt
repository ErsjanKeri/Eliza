/*
 * Copyright 2025 AI Edge Eliza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a chat session with the AI tutor.
 * UPDATED: Now chapter-centric with multiple sessions per chapter support.
 */
@Serializable
data class ChatSession(
    val id: String,
    val title: String,
    val chapterId: String, // REQUIRED: Always linked to a chapter
    val courseId: String,
    val userId: String, // NEW: User-specific sessions
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val messageCount: Int = 0,
    val videoCount: Int = 0 // NEW: Track videos in session
)

/**
 * Represents a message in a chat session.
 * UPDATED: Enhanced for video content and chapter context, based on Gallery's ChatMessage structure.
 */
@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val content: String,
    val isUser: Boolean, // Following Gallery's pattern (true = user, false = agent)
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType,
    val videoExplanation: VideoExplanation? = null, // NEW: Embedded video content
    val imageUri: String? = null,
    val mathSteps: List<MathStep> = emptyList(),
    val status: MessageStatus = MessageStatus.SENT,
    val relatedExerciseId: String? = null,
    val processingTimeMs: Long = 0L // NEW: Track AI response time
)

/**
 * Represents a step in a mathematical solution.
 */
@Serializable
data class MathStep(
    val stepNumber: Int,
    val description: String,
    val equation: String? = null,
    val explanation: String? = null
)

/**
 * Types of messages in the chat.
 * UPDATED: Added video message type for video explanations.
 */
@Serializable
enum class MessageType {
    TEXT,
    VIDEO, // NEW: For video explanation messages
    IMAGE,
    MATH_PROBLEM,
    STEP_BY_STEP_SOLUTION,
    CONCEPT_EXPLANATION
}

/**
 * Status of a message.
 * Following Gallery's message status pattern.
 */
@Serializable
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED,
    STREAMING // For real-time AI responses
}

/**
 * Represents the state of AI model.
 * Using Gallery's ModelState pattern but adapted for Eliza.
 */
@Serializable
data class ModelState(
    val isInitialized: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val errorMessage: String? = null,
    val modelName: String = "Gemma-3n-E4B-it-int4",
    val sizeInBytes: Long = 4405655031L,
    val estimatedPeakMemoryInBytes: Long = 6979321856L
)

/**
 * Represents a math problem extracted from an image.
 * Enhanced with better bounding box support.
 */
@Serializable
data class ImageMathProblem(
    val id: String,
    val imageUri: String,
    val extractedText: String,
    val problemType: ProblemType,
    val confidence: Float,
    val boundingBoxes: List<BoundingBox> = emptyList(),
    val processedAt: Long = System.currentTimeMillis()
)

/**
 * Represents a bounding box for detected text in an image.
 */
@Serializable
data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float
)

/**
 * Types of math problems that can be detected.
 */
@Serializable
enum class ProblemType {
    ALGEBRA,
    GEOMETRY,
    CALCULUS,
    EQUATION,
    GRAPH,
    WORD_PROBLEM,
    UNKNOWN
}

/**
 * Chat UI state following Gallery's pattern.
 * NEW: Manages multiple sessions and video content.
 */
@Serializable
data class ChatUiState(
    val inProgress: Boolean = false,
    val isResettingSession: Boolean = false,
    val preparing: Boolean = false,
    val activeSessionId: String? = null,
    val isOnline: Boolean = true, // NEW: Network state for video features
    val videoRequestInProgress: Boolean = false // NEW: Video request state
)

/**
 * Result of a video request operation.
 * NEW: For tracking video request progress and results.
 */
@Serializable
sealed class VideoRequestResult {
    @Serializable
    data object Loading : VideoRequestResult()
    
    @Serializable
    data class Success(val videoExplanation: VideoExplanation) : VideoRequestResult()
    
    @Serializable
    data class Error(val message: String, val canRetry: Boolean = true) : VideoRequestResult()
    
    @Serializable
    data object Offline : VideoRequestResult()
}

/**
 * Chat response from AI including processing metadata.
 * NEW: Enhanced response model for better user experience.
 */
@Serializable
data class ChatResponse(
    val message: ChatMessage,
    val processingTime: Long,
    val tokensGenerated: Int,
    val confidence: Float? = null
) 