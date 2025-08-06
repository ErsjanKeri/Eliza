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

package com.example.ai.edge.eliza.feature.courseprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.designsystem.component.ChapterNodeData
import com.example.ai.edge.eliza.core.model.Course
import com.example.ai.edge.eliza.core.model.Chapter
import com.example.ai.edge.eliza.core.model.ChapterProgress
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Course Progress screen.
 * Manages course data and user progress state.
 */
@HiltViewModel
class CourseProgressViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseProgressUiState())
    val uiState: StateFlow<CourseProgressUiState> = _uiState.asStateFlow()

    private val defaultUserId = "user_default" // TODO: Get from actual user session

    fun loadCourseProgress(courseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load course data
                val course = courseRepository.getCourseById(courseId).firstOrNull()
                
                if (course == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Course not found"
                    )
                    return@launch
                }

                // Load chapters for this course
                val chapters = courseRepository.getChaptersByCourse(courseId).firstOrNull() ?: emptyList()
                
                // Load user progress for each chapter
                val chapterProgressList = mutableListOf<ChapterProgress>()
                chapters.forEach { chapter ->
                    val progress = progressRepository.getChapterProgress(chapter.id, defaultUserId).firstOrNull()
                    if (progress != null) {
                        chapterProgressList.add(progress)
                    }
                }

                // Transform data into ChapterNodeData for the UI
                val userLanguage = userPreferencesRepository.getCurrentLanguage()
                val chapterNodes = chapters.map { chapter ->
                    val isCompleted = chapterProgressList.any { 
                        it.chapterId == chapter.id && it.isCompleted 
                    }
                    
                    ChapterNodeData(
                        id = chapter.id,
                        title = chapter.title.get(userLanguage),
                        isCompleted = isCompleted
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    courseId = courseId,
                    course = course,
                    chapters = chapters,
                    chapterProgress = chapterProgressList,
                    chapterNodes = chapterNodes,
                    currentLanguage = userLanguage,
                    courseTitle = course.title.get(userLanguage),
                    courseDescription = course.description.get(userLanguage),
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun onChapterClick(chapterId: String) {
        // TODO: Handle chapter click - navigate to chapter content
        println("Chapter clicked: $chapterId")
    }

    fun toggleHeaderExpanded() {
        _uiState.value = _uiState.value.copy(
            isHeaderExpanded = !_uiState.value.isHeaderExpanded
        )
    }
}

/**
 * UI State for the Course Progress screen
 */
data class CourseProgressUiState(
    val isLoading: Boolean = false,
    val courseId: String = "",
    val course: Course? = null,
    val chapters: List<Chapter> = emptyList(),
    val chapterProgress: List<ChapterProgress> = emptyList(),
    val chapterNodes: List<ChapterNodeData> = emptyList(),
    val error: String? = null,
    val isHeaderExpanded: Boolean = false, // Collapsed by default, shows only progress bar
    val currentLanguage: SupportedLanguage = SupportedLanguage.DEFAULT,
    // Localized content for UI display
    val courseTitle: String = "",
    val courseDescription: String = ""
) 