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
import com.example.ai.edge.eliza.core.data.repository.ChatRepository
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.ChatSession
import com.example.ai.edge.eliza.core.model.ChatType
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.feature.chat.ui.sidebar.SidebarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Enhanced ChatView components that need to load real data from repositories.
 * Handles loading course, chapter, and exercise data for proper ChatContext creation.
 * UPDATED: Now includes sidebar state management for hierarchical chat organization.
 */
@HiltViewModel
class EnhancedChatViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedChatUiState())
    val uiState: StateFlow<EnhancedChatUiState> = _uiState.asStateFlow()
    
    // Sidebar state management
    private val _sidebarState = MutableStateFlow(SidebarUiState())
    val sidebarState: StateFlow<SidebarUiState> = _sidebarState.asStateFlow()

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

    // SIDEBAR MANAGEMENT METHODS
    
    /**
     * Toggle sidebar visibility.
     */
    fun toggleSidebar() {
        val currentState = _sidebarState.value
        val wasVisible = currentState.isVisible
        
        _sidebarState.value = currentState.copy(
            isVisible = !wasVisible
        )
        
        // When sidebar opens for the first time with chapter context, ensure auto-expansion
        if (!wasVisible && _sidebarState.value.isVisible) {
            val chapterId = currentState.currentChapterId
            val courseId = currentState.currentCourseId
            
            if (chapterId != null && courseId != null) {
                // Auto-expand current course and chapter if not already expanded
                val shouldAutoExpandCourse = courseId !in currentState.expandedCourseIds
                val shouldAutoExpandChapter = chapterId !in currentState.expandedChapterIds
                
                if (shouldAutoExpandCourse || shouldAutoExpandChapter) {
                    _sidebarState.value = _sidebarState.value.copy(
                        expandedCourseIds = currentState.expandedCourseIds + courseId,
                        expandedChapterIds = currentState.expandedChapterIds + chapterId
                    )
                }
                
                // Load chat sessions when sidebar opens
                loadChatSessions()
            }
        }
    }
    
    /**
     * Initialize sidebar context for a chapter.
     */
    fun initializeSidebarContext(courseId: String, chapterId: String, userId: String = "user_demo") {
        _sidebarState.value = _sidebarState.value.copy(
            currentChapterId = chapterId,
            currentCourseId = courseId,
            userId = userId,
            expandedCourseIds = setOf(courseId),
            expandedChapterIds = setOf(chapterId)
        )
        
        // Load chat sessions if sidebar is visible
        if (_sidebarState.value.isVisible) {
            loadChatSessions()
        }
    }
    
    /**
     * Load chat sessions for the current chapter.
     */
    private fun loadChatSessions() {
        val currentState = _sidebarState.value
        val chapterId = currentState.currentChapterId
        val userId = currentState.userId
        
        if (chapterId == null) return
        
        viewModelScope.launch {
            _sidebarState.value = _sidebarState.value.copy(isLoading = true, error = null)
            
            try {
                val groupedSessions = chatRepository.getChatSessionsGroupedByType(chapterId, userId).firstOrNull()
                    ?: emptyMap()
                
                _sidebarState.value = _sidebarState.value.copy(
                    isLoading = false,
                    chatSessionsByType = groupedSessions,
                    error = null
                )
            } catch (e: Exception) {
                _sidebarState.value = _sidebarState.value.copy(
                    isLoading = false,
                    error = "Failed to load chat sessions: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select a chat session.
     */
    fun selectChatSession(session: ChatSession) {
        _sidebarState.value = _sidebarState.value.copy(
            activeSessionId = session.id
        )
        // TODO: Navigate to chat session
    }
    
    /**
     * Create a new chat session of the specified type.
     */
    fun createNewChatSession(
        chatType: ChatType,
        title: String? = null,
        sourceContext: String? = null,
        metadata: String = "{}"
    ) {
        val currentState = _sidebarState.value
        val chapterId = currentState.currentChapterId
        val courseId = currentState.currentCourseId
        val userId = currentState.userId
        
        if (chapterId == null || courseId == null) return
        
        viewModelScope.launch {
            try {
                val sessionTitle = title ?: when (chatType) {
                    ChatType.GENERAL_CHAPTER -> "Chapter Discussion"
                    ChatType.EXERCISE_HELP -> "Exercise Help"
                    ChatType.TEXT_SELECTION -> "Text Question"
                }
                
                val newSession = chatRepository.createChatSession(
                    title = sessionTitle,
                    chapterId = chapterId,
                    courseId = courseId,
                    userId = userId,
                    chatType = chatType,
                    sourceContext = sourceContext,
                    metadata = metadata
                )
                
                // Refresh sessions and select the new one
                loadChatSessions()
                _sidebarState.value = _sidebarState.value.copy(
                    activeSessionId = newSession.id
                )
                
                // TODO: Navigate to new chat session
                
            } catch (e: Exception) {
                _sidebarState.value = _sidebarState.value.copy(
                    error = "Failed to create chat session: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Expand/collapse course in sidebar.
     */
    fun toggleCourseExpansion(courseId: String) {
        val expandedCourses = _sidebarState.value.expandedCourseIds.toMutableSet()
        if (courseId in expandedCourses) {
            expandedCourses.remove(courseId)
        } else {
            expandedCourses.add(courseId)
        }
        
        _sidebarState.value = _sidebarState.value.copy(
            expandedCourseIds = expandedCourses
        )
    }
    
    /**
     * Expand/collapse chapter in sidebar.
     */
    fun toggleChapterExpansion(chapterId: String) {
        val expandedChapters = _sidebarState.value.expandedChapterIds.toMutableSet()
        if (chapterId in expandedChapters) {
            expandedChapters.remove(chapterId)
        } else {
            expandedChapters.add(chapterId)
        }
        
        _sidebarState.value = _sidebarState.value.copy(
            expandedChapterIds = expandedChapters
        )
    }

    /**
     * Clear current state.
     */
    fun clearState() {
        _uiState.value = EnhancedChatUiState()
        _sidebarState.value = SidebarUiState()
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