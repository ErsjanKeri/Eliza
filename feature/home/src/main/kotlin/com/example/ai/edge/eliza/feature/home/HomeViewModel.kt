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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen following NowInAndroid's sophisticated patterns.
 * Manages course feed and progress data with proper state management.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    // Selected tab state
    private val _selectedTab = MutableStateFlow(HomeTab.CONTINUE_LEARNING)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    // Course feed state combining course data with progress
    val courseFeedState: StateFlow<CourseFeedUiState> = combine(
        courseRepository.getAllCourses(),
        progressRepository.getAllUserProgress(),
    ) { courses, progressList ->
        try {
            val progressMap = progressList.associateBy { it.courseId }
            
            // Separate courses into continuing vs new
            val continuingCourses = courses
                .filter { course -> 
                    progressMap[course.id]?.let { it.completedChapters > 0 } == true 
                }
                .map { course ->
                    CourseWithProgress(
                        course = course,
                        progress = progressMap[course.id]
                    )
                }
                .sortedByDescending { it.progress?.lastStudiedAt ?: 0L }

            val newCourses = courses
                .filter { course -> 
                    progressMap[course.id]?.completedChapters ?: 0 == 0 
                }
                .sortedBy { it.title }

            CourseFeedUiState.Success(
                continuingCourses = continuingCourses,
                newCourses = newCourses,
            )
        } catch (exception: Exception) {
            CourseFeedUiState.LoadFailed(exception)
        }
    }
        .catch { exception ->
            emit(CourseFeedUiState.LoadFailed(exception as? Throwable ?: Exception(exception)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CourseFeedUiState.Loading,
        )

    // Progress state with learning overview
    val progressState: StateFlow<ProgressUiState> = combine(
        progressRepository.getAllUserProgress(),
        progressRepository.getLearningStats("user_default"), // TODO: Use actual user ID
    ) { progressList, learningStats ->
        try {
            val progressMap = progressList.associateBy { it.courseId }
            
            // Create learning overview from available data
            val learningOverview = learningStats?.let { stats ->
                LearningOverview(
                    totalCoursesStarted = progressMap.values.count { it.completedChapters > 0 },
                    totalCoursesCompleted = progressMap.values.count { 
                        it.totalChapters > 0 && it.completedChapters >= it.totalChapters 
                    },
                    totalChaptersCompleted = progressMap.values.sumOf { it.completedChapters },
                    totalTimeSpentMinutes = progressMap.values.sumOf { it.timeSpentMinutes },
                    currentStreak = progressMap.values.maxOfOrNull { it.streakDays } ?: 0,
                    averageAccuracy = if (progressMap.values.sumOf { it.totalAnswers } > 0) {
                        (progressMap.values.sumOf { it.correctAnswers }.toFloat() / 
                         progressMap.values.sumOf { it.totalAnswers }.toFloat()) * 100f
                    } else 0f,
                )
            } ?: LearningOverview(
                totalCoursesStarted = progressMap.values.count { it.completedChapters > 0 },
                totalCoursesCompleted = progressMap.values.count { 
                    it.totalChapters > 0 && it.completedChapters >= it.totalChapters 
                },
                totalChaptersCompleted = progressMap.values.sumOf { it.completedChapters },
                totalTimeSpentMinutes = progressMap.values.sumOf { it.timeSpentMinutes },
                currentStreak = progressMap.values.maxOfOrNull { it.streakDays } ?: 0,
                averageAccuracy = if (progressMap.values.sumOf { it.totalAnswers } > 0) {
                    (progressMap.values.sumOf { it.correctAnswers }.toFloat() / 
                     progressMap.values.sumOf { it.totalAnswers }.toFloat()) * 100f
                } else 0f,
            )

            ProgressUiState.Success(
                overallProgress = learningOverview,
                courseProgress = progressMap,
            )
        } catch (exception: Exception) {
            ProgressUiState.LoadFailed(exception)
        }
    }
        .catch { exception ->
            emit(ProgressUiState.LoadFailed(exception as? Throwable ?: Exception(exception)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgressUiState.Loading,
        )

    // Combined UI state for the home screen
    val uiState: StateFlow<HomeScreenUiState> = combine(
        courseFeedState,
        progressState,
        selectedTab,
    ) { feedState, progressState, tab ->
        HomeScreenUiState(
            courseFeedState = feedState,
            progressState = progressState,
            selectedTab = tab,
            isSyncing = false, // TODO: Add sync manager integration
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeScreenUiState(),
        )

    // User actions
    
    /**
     * Updates the selected tab on the home screen.
     */
    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    /**
     * Called when a course is clicked to start or continue.
     */
    fun onCourseClick(courseId: String) {
        viewModelScope.launch {
            try {
                // TODO: Navigate to course detail or lesson screen
                // For now, we just update the last studied time
                val currentProgress = progressRepository.getUserProgressByCourse(courseId)
                // TODO: Update the progress with new last studied time
            } catch (exception: Exception) {
                // TODO: Handle error appropriately
            }
        }
    }

    /**
     * Called when user wants to start a new course.
     */
    fun onStartNewCourse(courseId: String) {
        viewModelScope.launch {
            try {
                // TODO: Create initial progress entry for the course
                // TODO: Navigate to first lesson
            } catch (exception: Exception) {
                // TODO: Handle error appropriately
            }
        }
    }

    /**
     * Refreshes all data from repositories.
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                // TODO: Trigger repository refresh if needed
                // Most repositories use Flow which auto-updates
            } catch (exception: Exception) {
                // TODO: Handle error appropriately
            }
        }
    }
} 