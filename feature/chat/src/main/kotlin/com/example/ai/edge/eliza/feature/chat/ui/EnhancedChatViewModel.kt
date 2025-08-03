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

package com.example.ai.edge.eliza.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Enhanced ChatView components that need to load real data from repositories.
 * Handles loading course, chapter, and exercise data for proper ChatContext creation.
 */
@HiltViewModel
class EnhancedChatViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedChatUiState())
    val uiState: StateFlow<EnhancedChatUiState> = _uiState.asStateFlow()

    /**
     * Load chapter context for chapter reading chat.
     */
    fun loadChapterContext(courseId: String, chapterId: String, readingProgress: Float = 0f) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load course data
                val course = courseRepository.getCourseById(courseId).firstOrNull()
                if (course == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Course not found: $courseId"
                    )
                    return@launch
                }

                // Load chapter data
                val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
                if (chapter == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Chapter not found: $chapterId"
                    )
                    return@launch
                }

                // Create proper ChatContext from real data
                val chatContext = ChatContext.ChapterReading(
                    courseId = course.id,
                    courseTitle = course.title,
                    courseSubject = course.subject.name,
                    courseGrade = course.grade,
                    chapterId = chapter.id,
                    chapterTitle = chapter.title,
                    chapterNumber = chapter.chapterNumber,
                    markdownContent = chapter.markdownContent,
                    totalChapters = course.totalChapters,
                    readingProgress = readingProgress
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    chatContext = chatContext,
                    title = "Chat: ${chapter.title}",
                    course = course,
                    chapter = chapter
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load chapter data: ${e.message}"
                )
            }
        }
    }

    /**
     * Load exercise context for exercise help chat.
     */
    fun loadExerciseContext(
        courseId: String,
        chapterId: String,
        exerciseId: String,
        userAnswer: String? = null,
        isTestQuestion: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load chapter data first to get the real courseId if needed
                val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
                if (chapter == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Chapter not found: $chapterId"
                    )
                    return@launch
                }
                
                // Use the courseId from chapter data (more reliable than parameter)
                val realCourseId = chapter.courseId
                
                // Load course data using the real courseId from chapter
                val course = courseRepository.getCourseById(realCourseId).firstOrNull()
                if (course == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Course not found: $realCourseId"
                    )
                    return@launch
                }

                // Load exercise data
                val exercise = courseRepository.getExerciseById(exerciseId).firstOrNull()
                if (exercise == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Exercise not found: $exerciseId"
                    )
                    return@launch
                }

                // Find exercise number within chapter
                val exerciseNumber = chapter.exercises.indexOfFirst { it.id == exerciseId } + 1

                // Create proper ChatContext from real data
                val chatContext = ChatContext.ExerciseSolving(
                    exerciseId = exercise.id,
                    exerciseNumber = exerciseNumber,
                    questionText = exercise.questionText,
                    userAnswer = userAnswer,
                    correctAnswer = exercise.options[exercise.correctAnswerIndex],
                    chapterId = chapter.id,
                    chapterTitle = chapter.title,
                    chapterContent = chapter.markdownContent,
                    courseId = realCourseId,
                    courseTitle = course.title,
                    courseSubject = course.subject.name,
                    difficulty = exercise.difficulty.name,
                    isTestQuestion = isTestQuestion
                )

                // Create initial context message
                val exerciseContext = """
                    Exercise #$exerciseNumber Help: ${chapter.title}
                    
                    Question: ${exercise.questionText}
                    ${userAnswer?.let { "Your answer: $it" } ?: ""}
                    
                    I'm here to help you understand this problem better. What would you like to know?
                """.trimIndent()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    chatContext = chatContext,
                    title = "Exercise #$exerciseNumber Help",
                    course = course,
                    chapter = chapter,
                    exercise = exercise,
                    initialMessage = exerciseContext
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load exercise data: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear current state.
     */
    fun clearState() {
        _uiState.value = EnhancedChatUiState()
    }
}

/**
 * UI state for enhanced chat views.
 */
data class EnhancedChatUiState(
    val isLoading: Boolean = false,
    val chatContext: ChatContext? = null,
    val title: String = "Chat",
    val course: com.example.ai.edge.eliza.core.model.Course? = null,
    val chapter: com.example.ai.edge.eliza.core.model.Chapter? = null,
    val exercise: com.example.ai.edge.eliza.core.model.Exercise? = null,
    val initialMessage: String? = null,
    val error: String? = null
)