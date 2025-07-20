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
 * Base class for different types of chat contexts.
 * This defines the current context of the conversation for RAG enhancement.
 */
@Serializable
sealed class ChatContext {
    /**
     * Context for reading and discussing chapter content.
     * This is used when the user is reading a specific chapter/lesson.
     */
    @Serializable
    data class ChapterReading(
        val courseId: String,
        val lessonId: String,
        val chapterTitle: String,
        val currentSection: String? = null,
        val readingProgress: Float = 0f,
        val lastReadTime: Long = System.currentTimeMillis()
    ) : ChatContext()

    /**
     * Context for revision and practice after completing lessons.
     * This is used when the user is reviewing previously learned content.
     */
    @Serializable
    data class Revision(
        val courseId: String,
        val completedLessonIds: List<String>,
        val topicsToRevise: List<String>,
        val weakAreas: List<String> = emptyList(),
        val lastRevisionTime: Long = System.currentTimeMillis()
    ) : ChatContext()

    /**
     * Context for general tutoring without specific course content.
     * This is used for general math questions not tied to specific lessons.
     */
    @Serializable
    data class GeneralTutoring(
        val subject: String? = null,
        val grade: String? = null,
        val previousTopics: List<String> = emptyList(),
        val sessionStartTime: Long = System.currentTimeMillis()
    ) : ChatContext()

    /**
     * Context for working on specific exercises or trials.
     * This is used when the user is actively solving problems.
     */
    @Serializable
    data class ExerciseSolving(
        val exerciseId: String,
        val lessonId: String,
        val courseId: String,
        val difficulty: String,
        val attempts: Int = 0,
        val hintsUsed: Int = 0,
        val startTime: Long = System.currentTimeMillis(),
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