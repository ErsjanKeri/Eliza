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

package com.example.ai.edge.eliza.feature.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Chapter screen.
 * 
 * Handles chapter content loading and state management.
 * Simplified to remove split screen logic since we now use full-screen chat navigation.
 */
@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    // Default user ID for demo purposes
    private val defaultUserId = "default_user"
    
    private val _contentState = MutableStateFlow<ChapterContentUiState>(ChapterContentUiState.Loading)
    private val _refreshTrigger = MutableStateFlow(0)
    
    /**
     * Combined UI state for the chapter screen.
     */
    val uiState: StateFlow<ChapterScreenUiState> = combine(
        _contentState,
        _refreshTrigger
    ) { contentState, _ ->
        ChapterScreenUiState(
            contentState = contentState,
            isLoading = contentState is ChapterContentUiState.Loading
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), // Keep the state for 5 seconds after the last emission
        ChapterScreenUiState(
            contentState = ChapterContentUiState.Loading,
            isLoading = true
        )
    )

    init {
        // Initialize with loading state
        _contentState.value = ChapterContentUiState.Loading
    }

    /**
     * Load chapter content by ID.
     */
    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            _contentState.value = ChapterContentUiState.Loading
            
            try {
                // Load chapter from repository
                val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
                
                if (chapter == null) {
                    _contentState.value = ChapterContentUiState.LoadFailed(
                        Exception("Chapter not found: $chapterId")
                    )
                    return@launch
                }

                // Load test attempt information to update chapter state
                val progress = progressRepository.getChapterProgress(chapterId, defaultUserId).firstOrNull()
                val isCompleted = progress?.isCompleted ?: false
                
                // Get latest test attempts count and score from UserAnswer records
                val userAnswers = chapter.exercises.flatMap { exercise ->
                    progressRepository.getUserAnswersByExercise(exercise.id, defaultUserId).firstOrNull() ?: emptyList()
                }
                
                // Filter test attempts (non-trial)
                val testAnswers = userAnswers.filter { it.trialId == null }
                
                val testAttempts = if (testAnswers.isNotEmpty()) {
                    // Count how many exercises have test answers (indicates test was attempted)
                    testAnswers.distinctBy { it.exerciseId }.size
                } else {
                    0
                }
                
                // Calculate latest test score based on most recent test answers
                val latestTestScore = if (testAnswers.isNotEmpty() && testAttempts >= chapter.exercises.size) {
                    // Get the most recent answer for each exercise
                    val latestAnswersByExercise = testAnswers
                        .groupBy { it.exerciseId }
                        .mapValues { (_, answers) -> answers.maxByOrNull { it.answeredAt } }
                        .values
                        .filterNotNull()
                    
                    val correctAnswers = latestAnswersByExercise.count { it.isCorrect }
                    val totalQuestions = chapter.exercises.size
                    if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
                } else null
                
                // Get timestamp of last test attempt
                val lastTestAttempt = if (testAnswers.isNotEmpty()) {
                    testAnswers.maxOfOrNull { it.answeredAt }
                } else null
                
                // Update chapter with test information
                val updatedChapter = chapter.copy(
                    testScore = latestTestScore,
                    testAttempts = testAttempts,
                    lastTestAttempt = lastTestAttempt
                )

                _contentState.value = ChapterContentUiState.Success(
                    chapter = updatedChapter,
                    isCompleted = isCompleted
                )
                
            } catch (e: Exception) {
                _contentState.value = ChapterContentUiState.LoadFailed(e)
            }
        }
    }

    /**
     * Refresh chapter data (useful when returning from test results).
     */
    fun refreshChapter() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    /**
     * Mark chapter as completed.
     */
    fun markChapterCompleted(chapterId: String) {
        viewModelScope.launch {
            try {
                // Update progress in repository
                val currentProgress = progressRepository.getChapterProgress(chapterId, defaultUserId).firstOrNull()
                
                if (currentProgress != null) {
                    val updatedProgress = currentProgress.copy(
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    progressRepository.updateChapterProgress(updatedProgress)
                } else {
                    // Start chapter first, then mark as completed
                    progressRepository.startChapter(chapterId, defaultUserId)
                    val progress = progressRepository.getChapterProgress(chapterId, defaultUserId).firstOrNull()
                    if (progress != null) {
                        val completedProgress = progress.copy(
                            isCompleted = true,
                            completedAt = System.currentTimeMillis()
                        )
                        progressRepository.updateChapterProgress(completedProgress)
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle image clicks (placeholder for future functionality).
     */
    fun onImageClick(imageUrl: String) {
        // TODO: Implement image viewer functionality
    }
} 