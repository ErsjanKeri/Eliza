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

package com.example.ai.edge.eliza.core.data.repository

import com.example.ai.edge.eliza.core.model.ChatMessage
import com.example.ai.edge.eliza.core.model.ChatSession
import com.example.ai.edge.eliza.core.model.ImageMathProblem
import com.example.ai.edge.eliza.core.model.MathStep
import com.example.ai.edge.eliza.core.model.ModelState
import com.example.ai.edge.eliza.core.model.Subject
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat-related operations.
 * This interface defines all operations for managing AI tutoring conversations,
 * image processing, and model state management.
 */
interface ChatRepository {
    
    // Chat Session operations
    fun getAllChatSessions(): Flow<List<ChatSession>>
    fun getChatSessionById(sessionId: String): Flow<ChatSession?>
    fun getActiveChatSessions(): Flow<List<ChatSession>>
    fun getChatSessionsBySubject(subject: Subject): Flow<List<ChatSession>>
    fun getChatSessionsByCourse(courseId: String): Flow<List<ChatSession>>
    suspend fun createChatSession(title: String, subject: Subject? = null, courseId: String? = null): ChatSession
    suspend fun updateChatSession(session: ChatSession)
    suspend fun deleteChatSession(sessionId: String)
    suspend fun deactivateChatSession(sessionId: String)
    
    // Chat Message operations
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>>
    fun getMessageById(messageId: String): Flow<ChatMessage?>
    fun getRecentMessages(sessionId: String, limit: Int): Flow<List<ChatMessage>>
    suspend fun sendMessage(sessionId: String, message: String, imageUri: String? = null): Flow<ChatResponse>
    suspend fun sendImageMessage(sessionId: String, imageUri: String, question: String? = null): Flow<ChatResponse>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteMessagesBySession(sessionId: String)
    
    // AI Model operations
    fun getModelState(): Flow<ModelState>
    suspend fun initializeModel(): Flow<ModelInitializationResult>
    suspend fun downloadModel(): Flow<ModelDownloadProgress>
    suspend fun generateResponse(sessionId: String, prompt: String): Flow<AIResponse>
    suspend fun generateMathSolution(problem: String, steps: Boolean = true): Flow<AIResponse>
    suspend fun explainConcept(concept: String, subject: Subject): Flow<AIResponse>
    
    // Image Processing operations
    suspend fun processImage(imageUri: String): Flow<ImageProcessingResult>
    suspend fun extractMathProblem(imageUri: String): ImageMathProblem?
    fun getImageMathProblems(): Flow<List<ImageMathProblem>>
    suspend fun solveImageProblem(problemId: String): Flow<AIResponse>
    
    // Conversation management
    suspend fun clearConversation(sessionId: String)
    suspend fun exportConversation(sessionId: String, format: ExportFormat): String
    suspend fun getConversationSummary(sessionId: String): ConversationSummary
}

/**
 * Represents a chat response from the AI.
 */
data class ChatResponse(
    val messageId: String,
    val sessionId: String,
    val content: String,
    val mathSteps: List<MathStep> = emptyList(),
    val status: ChatResponseStatus,
    val confidence: Float = 0f,
    val processingTime: Long = 0L,
    val error: String? = null
)

/**
 * Status of a chat response.
 */
enum class ChatResponseStatus {
    GENERATING,
    PARTIAL,
    COMPLETE,
    ERROR
}

/**
 * Result of model initialization.
 */
data class ModelInitializationResult(
    val isSuccess: Boolean,
    val modelName: String,
    val initializationTime: Long,
    val memoryUsage: Long,
    val error: String? = null
)

/**
 * Progress of model download.
 */
data class ModelDownloadProgress(
    val progress: Float, // 0.0 to 1.0
    val status: ModelDownloadStatus,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val downloadSpeed: Long = 0L, // bytes per second
    val error: String? = null
)

/**
 * Model download status.
 */
enum class ModelDownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * AI response with metadata.
 */
data class AIResponse(
    val content: String,
    val mathSteps: List<MathStep> = emptyList(),
    val confidence: Float = 0f,
    val processingTime: Long = 0L,
    val tokensUsed: Int = 0,
                val model: String = "Gemma-3n-E4B-it-int4"
)

/**
 * Result of image processing.
 */
data class ImageProcessingResult(
    val imageUri: String,
    val extractedText: String,
    val mathProblem: ImageMathProblem?,
    val confidence: Float,
    val processingTime: Long,
    val error: String? = null
)

/**
 * Export formats for conversations.
 */
enum class ExportFormat {
    TEXT,
    JSON,
    PDF,
    MARKDOWN
}

/**
 * Summary of a conversation.
 */
data class ConversationSummary(
    val sessionId: String,
    val messageCount: Int,
    val topicsDiscussed: List<String>,
    val problemsSolved: Int,
    val averageResponseTime: Long,
    val totalTokensUsed: Int,
    val difficulty: String,
    val learningObjectives: List<String>
) 