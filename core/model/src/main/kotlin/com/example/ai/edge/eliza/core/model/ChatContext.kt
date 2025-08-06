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
    
    /**
     * Context for course recommendation and discovery.
     * Used when the user is asking about what they should learn or which courses to take.
     */
    @Serializable
    data class CourseSuggestion(
        val userQuery: String, // What the user wants to learn
        val userLevel: String? = null, // User's specified experience level
        val preferredSubjects: List<String> = emptyList(), // Subject preferences
        val availableTimeHours: Int? = null, // How much time user has to study
        val currentCourses: List<String> = emptyList(), // Currently enrolled course IDs
        val completedCourses: List<String> = emptyList(), // Previously completed course IDs
        val learningGoals: List<String> = emptyList(), // Specific goals from user query
        val sessionStartTime: Long = System.currentTimeMillis(),
        val conversationHistory: List<String> = emptyList() // Previous queries in this session
    ) : ChatContext()
    
    companion object {
        /**
         * Create a ChapterReading context from domain models.
         * Uses the specified language for RAG processing and AI interactions.
         * IMPORTANT: Language should be the user's preferred language, not hardcoded English.
         */
        fun createChapterReading(
            course: Course,
            chapter: Chapter,
            language: SupportedLanguage,
            completedChapters: Int = 0,
            readingProgress: Float = 0f
        ): ChapterReading {
            return ChapterReading(
                courseId = course.id,
                courseTitle = course.title.get(language), // Use user's language for RAG
                courseSubject = course.subject.name,
                courseGrade = course.grade,
                chapterId = chapter.id,
                chapterTitle = chapter.title.get(language), // Use user's language for RAG
                chapterNumber = chapter.chapterNumber,
                markdownContent = chapter.markdownContent.get(language), // Use user's language for RAG
                totalChapters = course.totalChapters,
                completedChapters = completedChapters,
                readingProgress = readingProgress
            )
        }
        
        /**
         * Create an ExerciseSolving context from domain models.
         * Uses the specified language for RAG processing and AI interactions.
         * IMPORTANT: Language should be the user's preferred language, not hardcoded English.
         */
        fun createExerciseSolving(
            course: Course,
            chapter: Chapter,
            exercise: Exercise,
            language: SupportedLanguage,
            userAnswer: String? = null,
            userAnswerIndex: Int? = null,
            isTestQuestion: Boolean = false,
            attempts: Int = 0,
            previousAttempts: List<String> = emptyList()
        ): ExerciseSolving {
            // Convert options to user's language for RAG processing
            val localizedOptions = exercise.options.map { it.get(language) }
            
            // If userAnswerIndex is provided, use that; otherwise try to find it from userAnswer
            val resolvedUserAnswerIndex = userAnswerIndex ?: userAnswer?.let { answer ->
                localizedOptions.indexOfFirst { it == answer }.takeIf { it >= 0 }
            }
            
            return ExerciseSolving(
                exerciseId = exercise.id,
                exerciseNumber = chapter.exercises.indexOfFirst { it.id == exercise.id } + 1,
                questionText = exercise.questionText.get(language), // Use user's language for RAG
                options = localizedOptions, // Use user's language for RAG
                userAnswer = userAnswer,
                correctAnswer = localizedOptions.getOrNull(exercise.correctAnswerIndex),
                userAnswerIndex = resolvedUserAnswerIndex,
                correctAnswerIndex = exercise.correctAnswerIndex,
                chapterId = chapter.id,
                chapterTitle = chapter.title.get(language), // Use user's language for RAG
                chapterContent = chapter.markdownContent.get(language), // Use user's language for RAG
                courseId = course.id,
                courseTitle = course.title.get(language), // Use user's language for RAG
                courseSubject = course.subject.name,
                difficulty = exercise.difficulty.name,
                attempts = attempts,
                isTestQuestion = isTestQuestion,
                previousAttempts = previousAttempts
            )
        }
        
        /**
         * Create a CourseSuggestion context from user query and preferences.
         */
        fun createCourseSuggestion(
            userQuery: String,
            userLevel: String? = null,
            preferredSubjects: List<String> = emptyList(),
            availableTimeHours: Int? = null,
            allUserProgress: List<UserProgress> = emptyList(),
            conversationHistory: List<String> = emptyList()
        ): CourseSuggestion {
            // Extract course IDs from user progress data
            val enrolledCourses = allUserProgress.map { it.courseId }
            val completedCourses = allUserProgress.filter { it.completionPercentage >= 100f }.map { it.courseId }
            
            return CourseSuggestion(
                userQuery = userQuery,
                userLevel = userLevel,
                preferredSubjects = preferredSubjects,
                availableTimeHours = availableTimeHours,
                currentCourses = enrolledCourses,
                completedCourses = completedCourses,
                learningGoals = extractLearningGoals(userQuery),
                conversationHistory = conversationHistory
            )
        }
        
        /**
         * Extract learning goals from user query using simple keyword matching.
         * This can be enhanced with NLP in the future.
         */
        private fun extractLearningGoals(query: String): List<String> {
            val goals = mutableListOf<String>()
            val lowerQuery = query.lowercase()
            
            // Common learning goal patterns
            when {
                "learn" in lowerQuery || "understand" in lowerQuery -> goals.add("Learn new concepts")
                "prepare" in lowerQuery || "exam" in lowerQuery -> goals.add("Exam preparation")
                "improve" in lowerQuery || "better" in lowerQuery -> goals.add("Skill improvement")
                "beginner" in lowerQuery || "start" in lowerQuery -> goals.add("Beginner introduction")
                "advanced" in lowerQuery || "expert" in lowerQuery -> goals.add("Advanced mastery")
                "review" in lowerQuery || "refresh" in lowerQuery -> goals.add("Knowledge review")
            }
            
            return goals.ifEmpty { listOf("General learning") }
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