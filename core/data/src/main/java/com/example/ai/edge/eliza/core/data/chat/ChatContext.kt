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

package com.example.ai.edge.eliza.core.data.chat

import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.UserAnswer
import kotlinx.serialization.Serializable

/**
 * Represents the context in which a chat conversation is taking place.
 * This allows the AI to provide contextually relevant responses based on
 * the educational situation.
 */
@Serializable
sealed class ChatContext {
    
    /**
     * Context for when the user is reading a specific chapter/lesson.
     * The AI will have access to chapter content for RAG-enhanced responses.
     */
    @Serializable
    data class ChapterContext(
        val courseId: String,
        val lessonId: String,
        val chapterId: String,
        val chapterTitle: String,
        val subject: Subject,
        val content: String,
        val currentSection: String? = null,
        val readingProgress: Float = 0f
    ) : ChatContext()
    
    /**
     * Context for when the user is in revision mode after getting answers wrong.
     * The AI will generate explanations and new practice questions.
     */
    @Serializable
    data class RevisionContext(
        val courseId: String,
        val lessonId: String,
        val exerciseId: String,
        val topicId: String,
        val subject: Subject,
        val wrongAnswers: List<UserAnswer>,
        val conceptsToReview: List<String>,
        val difficultyLevel: String,
        val mistakePatterns: List<String> = emptyList()
    ) : ChatContext()
    
    /**
     * Context for general problem-solving and tutoring.
     * The AI provides open-ended help without specific content constraints.
     */
    @Serializable
    data class GeneralContext(
        val subject: Subject? = null,
        val preferredDifficulty: String? = null,
        val learningGoals: List<String> = emptyList(),
        val userLevel: String? = null
    ) : ChatContext()
    
    /**
     * Context for exercise-specific help.
     * The AI provides targeted assistance for specific exercises.
     */
    @Serializable
    data class ExerciseContext(
        val courseId: String,
        val lessonId: String,
        val exerciseId: String,
        val exerciseType: String,
        val subject: Subject,
        val currentQuestion: String,
        val hintsUsed: Int = 0,
        val attemptsRemaining: Int = 3,
        val relatedConcepts: List<String> = emptyList()
    ) : ChatContext()
}

/**
 * Represents a chunk of content retrieved for RAG (Retrieval Augmented Generation).
 */
@Serializable
data class ContentChunk(
    val id: String,
    val title: String,
    val content: String,
    val source: String,
    val relevanceScore: Float,
    val chunkType: ContentChunkType,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Types of content chunks for RAG retrieval.
 */
@Serializable
enum class ContentChunkType {
    CHAPTER_SECTION,
    EXAMPLE,
    DEFINITION,
    FORMULA,
    PRACTICE_PROBLEM,
    EXPLANATION,
    THEOREM,
    CONCEPT_OVERVIEW
}

/**
 * Interface for providing context-aware content retrieval.
 */
interface RagProvider {
    /**
     * Retrieves relevant content chunks based on the user's query and current context.
     */
    suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int = 3
    ): List<ContentChunk>
    
    /**
     * Enhances a user prompt with relevant context information.
     */
    suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): String
    
    /**
     * Gets contextual system instructions for the AI based on the current context.
     */
    suspend fun getSystemInstructions(context: ChatContext): String
}

/**
 * Factory for creating appropriate RagProvider instances based on context.
 */
interface RagProviderFactory {
    fun createProvider(context: ChatContext): RagProvider
}

/**
 * Represents the enhanced prompt with context information.
 */
@Serializable
data class EnhancedPrompt(
    val originalPrompt: String,
    val enhancedPrompt: String,
    val context: ChatContext,
    val retrievedChunks: List<ContentChunk>,
    val systemInstructions: String
)

/**
 * Context-aware prompt enhancement result.
 */
@Serializable
data class PromptEnhancementResult(
    val enhancedPrompt: EnhancedPrompt,
    val confidence: Float,
    val processingTime: Long,
    val chunksUsed: Int
) 