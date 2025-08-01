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

import com.example.ai.edge.eliza.core.model.Chapter

/**
 * A sealed hierarchy describing the chapter content state.
 * Follows NowInAndroid's sophisticated state management patterns.
 */
sealed interface ChapterContentUiState {
    /**
     * The chapter content state is loading.
     */
    data object Loading : ChapterContentUiState

    /**
     * The chapter content state was unable to load.
     */
    data class LoadFailed(val exception: Throwable) : ChapterContentUiState

    /**
     * The chapter content loaded successfully.
     */
    data class Success(
        val chapter: Chapter,
        val isCompleted: Boolean = false,
    ) : ChapterContentUiState
}

/**
 * UI state for the chapter screen.
 * Simplified to remove split screen layout since we now use full-screen chat navigation.
 */
data class ChapterScreenUiState(
    val contentState: ChapterContentUiState = ChapterContentUiState.Loading,
    val isLoading: Boolean = false,
) {
    /**
     * True if chapter content is loaded successfully.
     */
    val hasContent: Boolean
        get() = contentState is ChapterContentUiState.Success

    /**
     * True if there's any error state.
     */
    val hasError: Boolean
        get() = contentState is ChapterContentUiState.LoadFailed
        
    /**
     * Get the chapter if available.
     */
    val chapter: Chapter?
        get() = when (contentState) {
            is ChapterContentUiState.Success -> contentState.chapter
            else -> null
        }
} 