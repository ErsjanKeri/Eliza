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

package com.example.ai.edge.eliza.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.example.ai.edge.eliza.feature.home.navigation.HOME_BASE_ROUTE
import com.example.ai.edge.eliza.feature.home.navigation.homeSection
import com.example.ai.edge.eliza.feature.courseprogress.navigation.courseProgressScreen
import com.example.ai.edge.eliza.feature.courseprogress.navigation.navigateToCourseProgress
import com.example.ai.edge.eliza.feature.chapter.navigation.chapterScreen
import com.example.ai.edge.eliza.feature.chapter.navigation.chapterTestScreen
import com.example.ai.edge.eliza.feature.chapter.navigation.chapterTestResultScreen
import com.example.ai.edge.eliza.feature.chapter.navigation.navigateToChapter
import com.example.ai.edge.eliza.feature.chapter.navigation.navigateToChapterTest
import com.example.ai.edge.eliza.feature.chapter.navigation.navigateToChapterTestResult
import com.example.ai.edge.eliza.feature.chapter.navigation.CHAPTER_ROUTE
import com.example.ai.edge.eliza.feature.chat.navigation.chatSection
import com.example.ai.edge.eliza.feature.chat.navigation.navigateToChat
import com.example.ai.edge.eliza.feature.chat.navigation.navigateToChapterChat
import com.example.ai.edge.eliza.feature.chat.navigation.navigateToExerciseHelpChat
import com.example.ai.edge.eliza.ui.ElizaAppState

/**
 * Top-level navigation graph for the Eliza application. 
 * Navigation is organized following NowInAndroid patterns.
 *
 * The navigation graph defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun ElizaNavHost(
    appState: ElizaAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HOME_BASE_ROUTE,
        modifier = modifier,
    ) {
        homeSection(
            onCourseClick = { courseId ->
                // Navigate to course progress screen
                navController.navigateToCourseProgress(courseId)
            }
        )
        
        courseProgressScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onChapterClick = { chapterId ->
                navController.navigateToChapter(chapterId)
            }
        )
        
        chapterScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToTest = { chapterId ->
                navController.navigateToChapterTest(chapterId)
            },
            onNavigateToChat = { courseId, chapterId ->
                navController.navigateToChapterChat(courseId, chapterId)
            }
        )
        
        chapterTestScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToResults = { chapterId, score, correct, total ->
                navController.navigateToChapterTestResult(chapterId, score, correct, total)
            }
        )
        
        chapterTestResultScreen(
            onBackClick = { chapterId ->
                // Clear test screens and return to original chapter
                navController.navigateToChapter(chapterId, 
                    androidx.navigation.navOptions {
                        popUpTo("$CHAPTER_ROUTE/$chapterId") { inclusive = true }
                        launchSingleTop = true
                    }
                )
            },
            onNavigateToChapter = { chapterId ->
                // Clear test screens and return to original chapter
                navController.navigateToChapter(chapterId,
                    androidx.navigation.navOptions {
                        popUpTo("$CHAPTER_ROUTE/$chapterId") { inclusive = true }
                        launchSingleTop = true
                    }
                )
            },
            onNavigateToTest = { chapterId ->
                navController.navigateToChapterTest(chapterId)
            },
            onContinueLearning = {
                // TODO: Navigate to next chapter or course list
                navController.popBackStack()
            },
            onNavigateToHome = {
                // Navigate to main home page and clear stack
                navController.navigate(HOME_BASE_ROUTE, 
                    androidx.navigation.navOptions {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                )
            },
            onNavigateToExerciseHelp = { chapterId, exerciseId, questionText, userAnswer, correctAnswer ->
                // Navigate to exercise help chat with real chapter data
                navController.navigateToExerciseHelpChat(
                    courseId = "loading", // Will be resolved by EnhancedChatViewModel from chapterId
                    chapterId = chapterId,
                    exerciseId = exerciseId, // Now using actual exercise ID instead of constructing artificial one
                    userAnswer = userAnswer,
                    isTestQuestion = true
                )
            }
        )
        
        // Chat section - Gallery-style chat interface
        chatSection(
            onNavigateUp = {
                navController.popBackStack()
            }
        )
        
        // TODO: Add other sections as they're implemented
        // settingsSection()
        // coursesSection()
    }
} 