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

package com.example.ai.edge.eliza.feature.home

import com.example.ai.edge.eliza.core.model.Course
import com.example.ai.edge.eliza.core.model.UserProgress

/**
 * A sealed hierarchy describing the course feed state for the home screen.
 * Follows NowInAndroid's sophisticated state management patterns.
 */
sealed interface CourseFeedUiState {
    /**
     * The course feed state is loading.
     */
    data object Loading : CourseFeedUiState

    /**
     * The course feed state was unable to load.
     */
    data class LoadFailed(val exception: Throwable) : CourseFeedUiState

    /**
     * The course feed loaded successfully with courses and progress data.
     */
    data class Success(
        val continuingCourses: List<CourseWithProgress>,
        val newCourses: List<Course>,
    ) : CourseFeedUiState
}

/**
 * A sealed hierarchy describing the user progress state.
 */
sealed interface ProgressUiState {
    /**
     * Progress data is loading.
     */
    data object Loading : ProgressUiState

    /**
     * Progress data failed to load.
     */
    data class LoadFailed(val exception: Throwable) : ProgressUiState

    /**
     * Progress data loaded successfully.
     */
    data class Success(
        val overallProgress: LearningOverview,
        val courseProgress: Map<String, UserProgress>,
    ) : ProgressUiState
}

/**
 * Combined UI state for the entire home screen.
 * This represents the complete state following NowInAndroid patterns.
 */
data class HomeScreenUiState(
    val courseFeedState: CourseFeedUiState = CourseFeedUiState.Loading,
    val progressState: ProgressUiState = ProgressUiState.Loading,
    val isSyncing: Boolean = false,
    val selectedTab: HomeTab = HomeTab.CONTINUE_LEARNING,
) {
    /**
     * True if any data is currently loading.
     */
    val isLoading: Boolean
        get() = courseFeedState is CourseFeedUiState.Loading || 
                progressState is ProgressUiState.Loading

    /**
     * True if there's any error state.
     */
    val hasError: Boolean
        get() = courseFeedState is CourseFeedUiState.LoadFailed || 
                progressState is ProgressUiState.LoadFailed
}

/**
 * Tabs for the home screen interface.
 */
enum class HomeTab(val displayName: String) {
    CONTINUE_LEARNING("Continue Learning"),
    START_NEW_COURSE("Start New Course")
}

/**
 * Represents a course with its associated progress information.
 * UPDATED: Now uses chapter-based terminology.
 */
data class CourseWithProgress(
    val course: Course,
    val progress: UserProgress?,
) {
    /**
     * Progress percentage for display (0-100).
     */
    val progressPercentage: Float
        get() = progress?.completionPercentage ?: 0f

    /**
     * Whether this course has been started.
     */
    val isStarted: Boolean
        get() = progress != null && progress.completedChapters > 0 // UPDATED: completedLessons → completedChapters

    /**
     * Display text for progress.
     */
    val progressText: String
        get() = if (progress != null) {
            "${progress.completedChapters}/${progress.totalChapters} chapters" // UPDATED: lessons → chapters
        } else {
            "Not started"
        }

    /**
     * Time spent display text.
     */
    val timeSpentText: String
        get() = if (progress != null && progress.timeSpentMinutes > 0) {
            "${progress.timeSpentMinutes} min studied"
        } else {
            "Start learning"
        }
}

/**
 * Overall learning progress overview for the user.
 * UPDATED: Now uses chapter-based terminology.
 */
data class LearningOverview(
    val totalCoursesStarted: Int = 0,
    val totalCoursesCompleted: Int = 0,
    val totalChaptersCompleted: Int = 0, // UPDATED: totalLessonsCompleted → totalChaptersCompleted
    val totalTimeSpentMinutes: Long = 0,
    val currentStreak: Int = 0,
    val averageAccuracy: Float = 0f,
) {
    /**
     * Formatted time spent for display.
     */
    val formattedTimeSpent: String
        get() = when {
            totalTimeSpentMinutes < 60 -> "${totalTimeSpentMinutes}m"
            totalTimeSpentMinutes < 1440 -> "${totalTimeSpentMinutes / 60}h ${totalTimeSpentMinutes % 60}m"
            else -> "${totalTimeSpentMinutes / 1440}d ${(totalTimeSpentMinutes % 1440) / 60}h"
        }

    /**
     * Completion percentage across all courses.
     */
    val overallCompletionPercentage: Float
        get() = if (totalCoursesStarted > 0) {
            (totalCoursesCompleted.toFloat() / totalCoursesStarted.toFloat()) * 100f
        } else {
            0f
        }
} 