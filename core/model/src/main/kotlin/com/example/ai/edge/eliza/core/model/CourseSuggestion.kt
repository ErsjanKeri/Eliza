/*
 * Copyright 2025 The Eliza Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Recommendation for a specific chapter within a course.
 * Contains navigation information for direct chapter access.
 */
@Serializable
data class ChapterRecommendation(
    val chapterId: String,
    val chapterTitle: String,
    val chapterNumber: Int,
    val relevanceReason: String, // Why this chapter is relevant to the user's query
    val keyTopics: List<String>, // Main topics covered in this chapter
    val estimatedReadingTime: Int, // In minutes
    val isCompleted: Boolean = false
)

/**
 * Recommendation for an entire course.
 * Contains course overview and relevant chapters for the user's learning goal.
 */
@Serializable
data class CourseRecommendation(
    val courseId: String,
    val courseTitle: String,
    val subject: String, // e.g., "Mathematics", "Physics"
    val grade: String, // e.g., "Grade 10", "Advanced"
    val description: String,
    val relevanceReason: String, // Why this course helps with the user's query
    val recommendedChapters: List<ChapterRecommendation>, // Specific chapters most relevant
    val totalChapters: Int,
    val estimatedCompletionHours: Int,
    val difficultyLevel: String, // "Beginner", "Intermediate", "Advanced"
    val prerequisites: List<String>, // What the user should know before starting
    val learningOutcomes: List<String> // What the user will learn by completing this course
)

/**
 * Request for generating course suggestions based on user's learning goals.
 * Uses RAG-enhanced context from all available courses and chapters.
 */
@Serializable
data class CourseSuggestionRequest(
    val userQuery: String, // What the user wants to learn
    val userLevel: String?, // Optional: "beginner", "intermediate", "advanced"
    val preferredSubjects: List<String> = emptyList(), // Optional subject preferences
    val availableTimeHours: Int? = null, // Optional: how much time user has
    val currentProgress: List<String> = emptyList(), // CourseIds of completed/ongoing courses
    val ragContext: String // All available course and chapter content for context
) {
    val maxRecommendations: Int = 3 // Limit to 3 course recommendations for better UX
}

/**
 * Structured AI response for parsing course suggestion data.
 * Designed for LLM JSON output parsing.
 */
@Serializable
data class CourseSuggestionResponse(
    val reasoning: String, // AI's explanation of why these courses were selected
    @SerialName("recommendedCourses") // Map AI's "recommendedCourses" field correctly  
    val recommendedCourses: List<CourseRecommendationData>,
    val alternativeTopics: List<String> = emptyList(), // Optional field
    val studyPlan: String? = null, // Optional field
    val difficultyAssessment: String? = null // Optional field
) {
    
    @Serializable
    data class CourseRecommendationData(
        val courseId: String,
        val courseName: String,
        val relevanceScore: Int, // 1-10 how relevant this course is
        val relevanceExplanation: String,
        val recommendedChapters: List<ChapterRecommendationData>,
        val estimatedTimeToGoal: String, // e.g., "2-3 weeks", "1 month"
        val difficultyForUser: String, // "Perfect fit", "Slightly challenging", etc.
        val keyBenefits: List<String>
    )
    
    @Serializable
    data class ChapterRecommendationData(
        val chapterId: String,
        val chapterName: String,
        val chapterNumber: Int,
        val whyRelevant: String,
        val keyTopics: List<String>,
        val priority: Int // 1-5, where 1 is highest priority
    )
    
    /**
     * Convert to domain CourseRecommendation objects with proper metadata.
     * Uses the specified language for localized content.
     */
    fun toCourseRecommendations(
        availableCourses: List<Course>,
        language: SupportedLanguage
    ): List<CourseRecommendation> {
        return recommendedCourses.mapNotNull { aiCourse ->
            // Find the actual course from available courses
            val course = availableCourses.find { it.id == aiCourse.courseId }
            course?.let {
                CourseRecommendation(
                    courseId = course.id,
                    courseTitle = course.title.get(language), // Use user's language
                    subject = course.subject.name,
                    grade = course.grade,
                    description = course.description.get(language), // Use user's language
                    relevanceReason = aiCourse.relevanceExplanation,
                    recommendedChapters = aiCourse.recommendedChapters.mapNotNull { aiChapter ->
                        // Find the actual chapter from the course
                        val chapter = course.chapters.find { it.id == aiChapter.chapterId }
                        chapter?.let { actualChapter ->
                            ChapterRecommendation(
                                chapterId = actualChapter.id,
                                chapterTitle = actualChapter.title.get(language), // Use user's language
                                chapterNumber = actualChapter.chapterNumber,
                                relevanceReason = aiChapter.whyRelevant,
                                keyTopics = aiChapter.keyTopics,
                                estimatedReadingTime = actualChapter.estimatedReadingTime,
                                isCompleted = actualChapter.isCompleted
                            )
                        }
                    },
                    totalChapters = course.totalChapters,
                    estimatedCompletionHours = course.estimatedHours,
                    difficultyLevel = aiCourse.difficultyForUser,
                    prerequisites = listOf(), // TODO: Extract from course metadata
                    learningOutcomes = aiCourse.keyBenefits
                )
            }
        }
    }
}

/**
 * Complete suggestion result including the AI's reasoning and study guidance.
 */
@Serializable
data class CourseSuggestionResult(
    val query: String,
    val recommendations: List<CourseRecommendation>,
    val reasoning: String,
    val studyPlan: String,
    val alternativeTopics: List<String>,
    val totalEstimatedHours: Int,
    val confidence: Float, // 0.0-1.0 based on RAG quality and LLM confidence
    val generatedAt: Long = System.currentTimeMillis()
) {
    val isEmpty: Boolean
        get() = recommendations.isEmpty()
        
    val hasMultipleCourses: Boolean
        get() = recommendations.size > 1
        
    val primaryRecommendation: CourseRecommendation?
        get() = recommendations.firstOrNull()
}

/**
 * Result states for course suggestion process.
 * Follows the same pattern as ExerciseGeneration.kt
 */
sealed class CourseSuggestionState {
    object Idle : CourseSuggestionState()
    data class Loading(val message: String) : CourseSuggestionState()
    data class Success(val result: CourseSuggestionResult) : CourseSuggestionState()
    data class Error(val message: String, val throwable: Throwable? = null) : CourseSuggestionState()
}

/**
 * User interaction data for course suggestion analytics.
 */
@Serializable
data class CourseSuggestionInteraction(
    val sessionId: String,
    val userQuery: String,
    val selectedCourseId: String?,
    val selectedChapterId: String?,
    val interactionType: InteractionType,
    val timestamp: Long = System.currentTimeMillis()
) {
    @Serializable
    enum class InteractionType {
        QUERY_SUBMITTED,
        COURSE_CLICKED,
        CHAPTER_CLICKED,
        SUGGESTION_DISMISSED,
        QUERY_REFINED
    }
}