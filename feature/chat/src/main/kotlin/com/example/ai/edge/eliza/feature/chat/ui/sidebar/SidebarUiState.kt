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

package com.example.ai.edge.eliza.feature.chat.ui.sidebar

import com.example.ai.edge.eliza.core.model.ChatSession
import com.example.ai.edge.eliza.core.model.ChatType

/**
 * UI state for the toggleable chat sidebar.
 * Manages sidebar visibility, navigation context, and hierarchical organization.
 */
data class SidebarUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentChapterId: String? = null,
    val currentCourseId: String? = null,
    val currentCourseName: String? = null, // NEW: Course name from repository
    val currentChapterName: String? = null, // NEW: Chapter name from repository
    val userId: String = "user_demo", // TODO: Replace with actual user system
    val expandedCourseIds: Set<String> = emptySet(),
    val expandedChapterIds: Set<String> = emptySet(),
    val chatSessionsByType: Map<ChatType, List<ChatSession>> = emptyMap(),
    val activeSessionId: String? = null,
    val error: String? = null
) {
    
    /**
     * Helper to determine if the current chapter should be auto-expanded.
     */
    val shouldAutoExpandCurrentChapter: Boolean
        get() = currentChapterId != null && currentChapterId !in expandedChapterIds
    
    /**
     * Helper to get session count by type for display.
     */
    fun getSessionCountByType(chatType: ChatType): Int = 
        chatSessionsByType[chatType]?.size ?: 0
    
    /**
     * Helper to get numbered exercise help sessions.
     */
    fun getExerciseHelpSessionsWithNumbers(): List<Pair<ChatSession, Int>> {
        val exerciseHelp = chatSessionsByType[ChatType.EXERCISE_HELP] ?: emptyList()
        
        // Group by exercise (sourceContext) and add session numbers
        return exerciseHelp
            .groupBy { it.sourceContext }
            .flatMap { (exerciseId, sessions) ->
                sessions.sortedBy { it.createdAt }.mapIndexed { index, session ->
                    session to (index + 1)
                }
            }
            .sortedByDescending { it.first.lastMessageAt }
    }
    
    /**
     * Helper to format exercise help session title with numbering.
     */
    fun formatExerciseHelpTitle(session: ChatSession, sessionNumber: Int): String {
        val baseTitle = session.title.substringBefore(" (")
        return if (sessionNumber > 1) {
            "$baseTitle ($sessionNumber)"
        } else {
            baseTitle
        }
    }
}