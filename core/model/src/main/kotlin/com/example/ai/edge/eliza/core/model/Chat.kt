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
 */
@Serializable
data class ChatSession(
    val id: String,
    val title: String,
    val subject: Subject? = null,
    val courseId: String? = null,
    val lessonId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Represents a message in a chat session.
 * Based on Gallery's chat message structure but enhanced for tutoring.
 */
@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val mathSteps: List<MathStep> = emptyList(),
    val messageType: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val relatedExerciseId: String? = null,
    val relatedTrialId: String? = null
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
 */
@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    MATH_PROBLEM,
    STEP_BY_STEP_SOLUTION,
    CONCEPT_EXPLANATION
}

/**
 * Status of a message.
 */
@Serializable
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED,
    STREAMING
}

/**
 * Represents the state of AI model.
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