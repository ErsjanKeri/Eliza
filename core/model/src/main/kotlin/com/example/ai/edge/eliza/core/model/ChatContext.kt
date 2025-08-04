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
 * UPDATED: Renamed lesson references to chapter references.
 */
@Serializable
sealed class ChatContext {
    /**
     * Context for reading and discussing chapter content.
     * Enhanced with full course and chapter information for superior RAG performance.
     */
    @Serializable
    data class ChapterReading(
        val courseId: String,
        val courseTitle: String,
        val courseSubject: String,
        val courseGrade: String,
        val chapterId: String,
        val chapterTitle: String,
        val chapterNumber: Int,
        val markdownContent: String? = null, // Full chapter content for RAG indexing
        val currentSection: String? = null,
        val readingProgress: Float = 0f,
        val totalChapters: Int,
        val completedChapters: Int = 0,
        val relatedChapterIds: List<String> = emptyList(), // For cross-chapter context
        val lastReadTime: Long = System.currentTimeMillis()
    ) : ChatContext()

    /**
     * Context for revision and practice after completing chapters.
     * This is used when the user is reviewing previously learned content.
     * UPDATED: Renamed lesson references to chapter references.
     */
    @Serializable
    data class Revision(
        val courseId: String,
        val completedChapterIds: List<String>, // RENAMED from completedLessonIds
        val topicsToRevise: List<String>,
        val weakAreas: List<String> = emptyList(),
        val lastRevisionTime: Long = System.currentTimeMillis()
    ) : ChatContext()

    /**
     * Context for general tutoring without specific course content.
     * This is used for general math questions not tied to specific chapters.
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
     * Enhanced with comprehensive context for RAG-powered exercise help.
     */
    @Serializable
    data class ExerciseSolving(
        val exerciseId: String,
        val exerciseNumber: Int,
        val questionText: String,
        val options: List<String> = emptyList(), // All multiple choice options (A, B, C, D)
        val userAnswer: String? = null,
        val correctAnswer: String? = null,
        val userAnswerIndex: Int? = null, // Index of selected option (0-3)
        val correctAnswerIndex: Int? = null, // Index of correct option (0-3)
        val chapterId: String,
        val chapterTitle: String,
        val chapterContent: String? = null, // Chapter content for context
        val courseId: String,
        val courseTitle: String,
        val courseSubject: String,
        val difficulty: String,
        val attempts: Int = 0,
        val isTestQuestion: Boolean = false, // Distinguish test vs practice
        val previousAttempts: List<String> = emptyList(), // History of wrong answers
        val hintsUsed: Int = 0,
        val startTime: Long = System.currentTimeMillis(),
        val relatedConcepts: List<String> = emptyList()
    ) : ChatContext()
    
    companion object {
        /**
         * Create a ChapterReading context from domain models.
         */
        fun createChapterReading(
            course: Course,
            chapter: Chapter,
            completedChapters: Int = 0,
            readingProgress: Float = 0f
        ): ChapterReading {
            return ChapterReading(
                courseId = course.id,
                courseTitle = course.title,
                courseSubject = course.subject.name,
                courseGrade = course.grade,
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                chapterNumber = chapter.chapterNumber,
                markdownContent = chapter.markdownContent,
                totalChapters = course.totalChapters,
                completedChapters = completedChapters,
                readingProgress = readingProgress
            )
        }
        
        /**
         * Create an ExerciseSolving context from domain models.
         */
        fun createExerciseSolving(
            course: Course,
            chapter: Chapter,
            exercise: Exercise,
            userAnswer: String? = null,
            userAnswerIndex: Int? = null,
            isTestQuestion: Boolean = false,
            attempts: Int = 0,
            previousAttempts: List<String> = emptyList()
        ): ExerciseSolving {
            // If userAnswerIndex is provided, use that; otherwise try to find it from userAnswer
            val resolvedUserAnswerIndex = userAnswerIndex ?: userAnswer?.let { answer ->
                exercise.options.indexOfFirst { it == answer }.takeIf { it >= 0 }
            }
            
            return ExerciseSolving(
                exerciseId = exercise.id,
                exerciseNumber = chapter.exercises.indexOfFirst { it.id == exercise.id } + 1,
                questionText = exercise.questionText,
                options = exercise.options,
                userAnswer = userAnswer,
                correctAnswer = exercise.options.getOrNull(exercise.correctAnswerIndex),
                userAnswerIndex = resolvedUserAnswerIndex,
                correctAnswerIndex = exercise.correctAnswerIndex,
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                chapterContent = chapter.markdownContent,
                courseId = course.id,
                courseTitle = course.title,
                courseSubject = course.subject.name,
                difficulty = exercise.difficulty.name,
                attempts = attempts,
                isTestQuestion = isTestQuestion,
                previousAttempts = previousAttempts
            )
        }
    }
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
    SUMMARY,           // High-level summaries for general queries
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