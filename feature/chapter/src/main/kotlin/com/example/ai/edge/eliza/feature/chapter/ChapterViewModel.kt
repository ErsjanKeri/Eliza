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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Chapter screen.
 * Manages chapter content and screen layout state following NowInAndroid patterns.
 */
@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _contentState = MutableStateFlow<ChapterContentUiState>(ChapterContentUiState.Loading)
    private val _layoutState = MutableStateFlow<ChapterLayoutState>(ChapterLayoutState.FullScreen)
    
    private val defaultUserId = "user_default" // TODO: Get from actual user session

    /**
     * Combined UI state for the chapter screen.
     */
    val uiState: StateFlow<ChapterScreenUiState> = combine(
        _contentState,
        _layoutState
    ) { contentState, layoutState ->
        ChapterScreenUiState(
            contentState = contentState,
            layoutState = layoutState,
            isLoading = contentState is ChapterContentUiState.Loading
        )
    }
        .catch { exception ->
            _contentState.value = ChapterContentUiState.LoadFailed(
                exception as? Throwable ?: Exception(exception)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChapterScreenUiState()
        )

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

                // Check if chapter is completed
                val progress = progressRepository.getChapterProgress(chapterId, defaultUserId).firstOrNull()
                val isCompleted = progress?.isCompleted ?: false

                _contentState.value = ChapterContentUiState.Success(
                    chapter = chapter,
                    isCompleted = isCompleted
                )
                
            } catch (e: Exception) {
                _contentState.value = ChapterContentUiState.LoadFailed(e)
            }
        }
    }

    /**
     * Toggle between full screen and split screen layout.
     */
    fun toggleChatLayout() {
        _layoutState.value = when (_layoutState.value) {
            ChapterLayoutState.FullScreen -> ChapterLayoutState.SplitScreen
            ChapterLayoutState.SplitScreen -> ChapterLayoutState.FullScreen
        }
    }

    /**
     * Show chat in split screen mode.
     */
    fun showChat() {
        _layoutState.value = ChapterLayoutState.SplitScreen
    }

    /**
     * Hide chat and return to full screen mode.
     */
    fun hideChat() {
        _layoutState.value = ChapterLayoutState.FullScreen
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
                    progressRepository.completeChapter(chapterId, defaultUserId, 0L)
                }

                // Update UI state
                val currentState = _contentState.value
                if (currentState is ChapterContentUiState.Success) {
                    _contentState.value = currentState.copy(isCompleted = true)
                }
                
            } catch (e: Exception) {
                // TODO: Handle error appropriately (show snackbar, etc.)
            }
        }
    }



    /**
     * Handle image click events.
     */
    fun onImageClick(imagePath: String) {
        // TODO: Implement image zoom/fullscreen view
    }
} 