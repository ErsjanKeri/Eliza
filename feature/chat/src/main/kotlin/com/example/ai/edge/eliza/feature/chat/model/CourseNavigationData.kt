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

package com.example.ai.edge.eliza.feature.chat.model

import kotlinx.serialization.Serializable

/**
 * Data structure for course navigation actions in chat messages.
 * Used when AI suggests courses/chapters and provides navigation buttons.
 */
@Serializable
data class CourseNavigationAction(
    val type: CourseNavigationType,
    val courseId: String,
    val courseName: String,
    val chapterId: String? = null,
    val chapterName: String? = null,
    val displayText: String,
    val relevanceExplanation: String? = null
)

/**
 * Types of course navigation actions available in chat.
 */
@Serializable
enum class CourseNavigationType {
    /** Navigate to course overview/progress screen */
    COURSE_OVERVIEW,
    
    /** Navigate directly to a specific chapter */
    CHAPTER_DIRECT,
    
    /** Start course from beginning */
    START_COURSE
}

/**
 * Collection of course navigation actions for a single AI suggestion.
 * This allows AI to suggest multiple courses/chapters in one response.
 */
@Serializable
data class CourseNavigationData(
    val suggestions: List<CourseNavigationAction>,
    val aiConfidence: Float = 0.0f,
    val reasoningText: String? = null
) {
    companion object {
        /**
         * Create navigation data from AI course suggestion response.
         */
        fun fromCourseSuggestionResponse(
            response: com.example.ai.edge.eliza.core.model.CourseSuggestionResponse
        ): CourseNavigationData {
            val actions = mutableListOf<CourseNavigationAction>()
            
            response.recommendedCourses.forEach { course ->
                // Add main course overview action
                actions.add(
                    CourseNavigationAction(
                        type = CourseNavigationType.COURSE_OVERVIEW,
                        courseId = course.courseId,
                        courseName = course.courseName,
                        displayText = "View ${course.courseName}",
                        relevanceExplanation = course.relevanceExplanation
                    )
                )
                
                // Add specific chapter actions for top priority chapters
                course.recommendedChapters
                    .filter { it.priority <= 2 } // Only top 2 priority chapters
                    .forEach { chapter ->
                        actions.add(
                            CourseNavigationAction(
                                type = CourseNavigationType.CHAPTER_DIRECT,
                                courseId = course.courseId,
                                courseName = course.courseName,
                                chapterId = chapter.chapterId,
                                chapterName = chapter.chapterName,
                                displayText = "Start: ${chapter.chapterName}",
                                relevanceExplanation = chapter.whyRelevant
                            )
                        )
                    }
            }
            
            return CourseNavigationData(
                suggestions = actions,
                reasoningText = response.reasoning
            )
        }
        
        /**
         * Create empty navigation data for non-course-suggestion messages.
         */
        fun empty(): CourseNavigationData = CourseNavigationData(suggestions = emptyList())
    }
}