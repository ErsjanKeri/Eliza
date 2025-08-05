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

package com.example.ai.edge.eliza.feature.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ai.edge.eliza.feature.chat.ui.ChatView
import com.example.ai.edge.eliza.feature.chat.ui.EnhancedChapterChatView
import com.example.ai.edge.eliza.feature.chat.ui.EnhancedCourseSuggestionChatView
import com.example.ai.edge.eliza.feature.chat.ui.EnhancedExerciseHelpChatView
import com.example.ai.edge.eliza.feature.chat.ui.ExerciseHelpChatView

const val CHAT_ROUTE = "chat_route"
const val CHAPTER_CHAT_ROUTE = "chapter_chat_route"
const val EXERCISE_HELP_CHAT_ROUTE = "exercise_help_chat_route"
const val COURSE_SUGGESTIONS_CHAT_ROUTE = "course_suggestions_chat_route"

/**
 * Navigation to general chat screen (legacy)
 */
fun NavController.navigateToChat(title: String) {
    this.navigate("$CHAT_ROUTE/$title")
}

/**
 * Navigation to chapter-based chat with full context
 */
fun NavController.navigateToChapterChat(
    courseId: String,
    chapterId: String,
    readingProgress: Float = 0f
) {
    this.navigate("$CHAPTER_CHAT_ROUTE/$courseId/$chapterId/$readingProgress")
}

/**
 * Navigation to exercise help chat with enhanced context
 */
fun NavController.navigateToExerciseHelpChat(
    courseId: String,
    chapterId: String,
    exerciseId: String,
    userAnswer: String? = null,
    isTestQuestion: Boolean = false
) {
    val encodedUserAnswer = userAnswer?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: "null"
    this.navigate("$EXERCISE_HELP_CHAT_ROUTE/$courseId/$chapterId/$exerciseId/$encodedUserAnswer/$isTestQuestion")
}

/**
 * Navigation to course suggestions chat for general learning guidance
 */
fun NavController.navigateToCourseSuggestionsChat() {
    this.navigate(COURSE_SUGGESTIONS_CHAT_ROUTE)
}

/**
 * Chat section for the navigation graph
 */
fun NavGraphBuilder.chatSection(
    onNavigateUp: () -> Unit,
    // NEW: Course navigation functions for course suggestion buttons
    onNavigateToCourse: (String) -> Unit = {},
    onNavigateToChapter: (String, String) -> Unit = { _, _ -> }
) {
    // Legacy general chat screen
    composable(
        route = "$CHAT_ROUTE/{title}",
        arguments = listOf(
            navArgument("title") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val title = backStackEntry.arguments?.getString("title") ?: "Chat"
        
        ChatView(
            title = title,
            onNavigateUp = onNavigateUp,
            showSidebarToggle = true // Enable sidebar for general chat
        )
    }
    
    // Enhanced chapter-based chat screen
    composable(
        route = "$CHAPTER_CHAT_ROUTE/{courseId}/{chapterId}/{readingProgress}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("chapterId") { type = NavType.StringType },
            navArgument("readingProgress") { type = NavType.FloatType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
        val readingProgress = backStackEntry.arguments?.getFloat("readingProgress") ?: 0f
        
        EnhancedChapterChatView(
            courseId = courseId,
            chapterId = chapterId,
            readingProgress = readingProgress,
            onNavigateUp = onNavigateUp
        )
    }
    
    // Enhanced exercise help chat screen  
    composable(
        route = "$EXERCISE_HELP_CHAT_ROUTE/{courseId}/{chapterId}/{exerciseId}/{userAnswer}/{isTestQuestion}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("chapterId") { type = NavType.StringType },
            navArgument("exerciseId") { type = NavType.StringType },
            navArgument("userAnswer") { type = NavType.StringType },
            navArgument("isTestQuestion") { type = NavType.BoolType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
        val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
        val userAnswer = backStackEntry.arguments?.getString("userAnswer")?.let {
            if (it == "null") null else java.net.URLDecoder.decode(it, "UTF-8")
        }
        val isTestQuestion = backStackEntry.arguments?.getBoolean("isTestQuestion") ?: false
        
        EnhancedExerciseHelpChatView(
            courseId = courseId,
            chapterId = chapterId,
            exerciseId = exerciseId,
            userAnswer = userAnswer,
            isTestQuestion = isTestQuestion,
            onNavigateUp = onNavigateUp
        )
    }
    
    // Enhanced course suggestions chat screen
    composable(COURSE_SUGGESTIONS_CHAT_ROUTE) {
        EnhancedCourseSuggestionChatView(
            onNavigateUp = onNavigateUp,
            onNavigateToCourse = onNavigateToCourse,
            onNavigateToChapter = onNavigateToChapter
        )
    }
} 