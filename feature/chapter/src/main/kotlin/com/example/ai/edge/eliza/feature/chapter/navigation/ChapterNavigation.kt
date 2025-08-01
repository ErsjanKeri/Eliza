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

package com.example.ai.edge.eliza.feature.chapter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.feature.chapter.ChapterScreen
import com.example.ai.edge.eliza.feature.chapter.test.ChapterTestScreen
import com.example.ai.edge.eliza.feature.chapter.test.ChapterTestResultScreen
import com.example.ai.edge.eliza.feature.chapter.test.ChapterTestViewModel
import com.example.ai.edge.eliza.core.model.TestState
import com.example.ai.edge.eliza.core.model.TestResult
import com.example.ai.edge.eliza.feature.chat.navigation.navigateToExerciseHelpChat

const val CHAPTER_ROUTE = "chapter_route"
const val CHAPTER_TEST_ROUTE = "chapter_test_route"
const val CHAPTER_TEST_RESULT_ROUTE = "chapter_test_result_route"
const val CHAPTER_ID_ARG = "chapterId"
const val TEST_ACTION_ARG = "action"
const val TEST_SCORE_ARG = "testScore"
const val TEST_CORRECT_ARG = "testCorrect"
const val TEST_TOTAL_ARG = "testTotal"

/**
 * Navigate to the chapter screen.
 */
fun NavController.navigateToChapter(
    chapterId: String,
    navOptions: NavOptions? = null
) = navigate("$CHAPTER_ROUTE/$chapterId", navOptions)

/**
 * Navigate to the chapter test screen.
 */
fun NavController.navigateToChapterTest(
    chapterId: String,
    navOptions: NavOptions? = null
) = navigate("$CHAPTER_TEST_ROUTE/$chapterId", navOptions)

/**
 * Navigate to the chapter test result screen.
 */
fun NavController.navigateToChapterTestResult(
    chapterId: String,
    testScore: Int,
    correctAnswers: Int,
    totalQuestions: Int,
    navOptions: NavOptions? = null
) = navigate("$CHAPTER_TEST_RESULT_ROUTE/$chapterId/$testScore/$correctAnswers/$totalQuestions", navOptions)

/**
 * Chapter screen composable for the navigation graph.
 * 
 * Shows markdown content with full-screen chat functionality.
 * 
 * @param onBackClick - Called when the back button is clicked
 * @param onNavigateToTest - Called when the test button is clicked
 * @param onNavigateToResults - Called when the results button is clicked
 * @param onNavigateToChat - Called when the chat button is clicked to open full-screen chat
 */
fun NavGraphBuilder.chapterScreen(
    onBackClick: () -> Unit,
    onNavigateToTest: (String) -> Unit,
    onNavigateToResults: (String) -> Unit = onNavigateToTest, // Default to same behavior
    onNavigateToChat: (String, String) -> Unit, // (chapterId, chapterTitle) -> navigate to chat
) {
    composable(
        route = "$CHAPTER_ROUTE/{$CHAPTER_ID_ARG}",
        arguments = listOf(
            navArgument(CHAPTER_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getString(CHAPTER_ID_ARG) ?: ""
        ChapterScreen(
            chapterId = chapterId,
            onBackClick = onBackClick,
            onNavigateToTest = { onNavigateToTest(chapterId) },
            onNavigateToResults = { onNavigateToResults(chapterId) },
            onRetakeTest = { onNavigateToTest(chapterId) }, // Retake uses same navigation as start test
            onNavigateToChat = { chapterTitle -> onNavigateToChat(chapterId, chapterTitle) }
        )
    }
}

/**
 * Chapter test screen composable for the navigation graph.
 * 
 * Shows Kahoot-style test interface with question navigation.
 * 
 * @param onBackClick - Called when the back button is clicked  
 * @param onNavigateToResults - Called when test is completed
 */
fun NavGraphBuilder.chapterTestScreen(
    onBackClick: () -> Unit,
    onNavigateToResults: (String, Int, Int, Int) -> Unit,
) {
    composable(
        route = "$CHAPTER_TEST_ROUTE/{$CHAPTER_ID_ARG}?$TEST_ACTION_ARG={$TEST_ACTION_ARG}",
        arguments = listOf(
            navArgument(CHAPTER_ID_ARG) {
                type = NavType.StringType
            },
            navArgument(TEST_ACTION_ARG) {
                type = NavType.StringType
                defaultValue = "start"
            }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getString(CHAPTER_ID_ARG) ?: ""
        val action = backStackEntry.arguments?.getString(TEST_ACTION_ARG) ?: "start"
        val viewModel: ChapterTestViewModel = hiltViewModel()
        
        // Call appropriate ViewModel method based on action parameter
        androidx.compose.runtime.LaunchedEffect(chapterId, action) {
            when (action) {
                "start" -> viewModel.startTest(chapterId)
                "results" -> viewModel.showResults(chapterId)
                "retake" -> viewModel.retakeTest(chapterId)
                else -> viewModel.startTest(chapterId) // Default to start
            }
        }
        
        val testState by viewModel.testState.collectAsState()
        
        when (testState) {
            is TestState.InProgress -> {
                ChapterTestScreen(
                    chapterTest = (testState as TestState.InProgress).test,
                    onAnswerSelected = viewModel::selectAnswer,
                    onNavigateToQuestion = viewModel::navigateToQuestion,
                    onPreviousQuestion = viewModel::previousQuestion,
                    onNextQuestion = viewModel::nextQuestion,
                    onSubmitTest = viewModel::submitTest,
                    onBackClick = onBackClick
                )
            }
            is TestState.Completed -> {
                val result = (testState as TestState.Completed).result
                // Navigate to results with test data
                androidx.compose.runtime.LaunchedEffect(result) {
                    onNavigateToResults(chapterId, result.score, result.correctAnswers, result.totalQuestions)
                }
                
                // Show loading while navigating
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading results...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            is TestState.Error -> {
                // Show error and allow manual back navigation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Test Error",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = (testState as TestState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    ElizaButton(
                        onClick = onBackClick,
                        text = { Text("Back to Chapter") }
                    )
                }
            }
            else -> {
                // Loading state - show loading spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Starting test...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Chapter test result screen composable for the navigation graph.
 * 
 * Shows test results with score and action options.
 * 
 * @param onBackClick - Called when back button is clicked
 * @param onNavigateToChapter - Called when returning to chapter
 * @param onNavigateToTest - Called when retaking test
 * @param onContinueLearning - Called when continuing to next chapter
 */
fun NavGraphBuilder.chapterTestResultScreen(
    onBackClick: (String) -> Unit,
    onNavigateToChapter: (String) -> Unit,
    onNavigateToTest: (String) -> Unit,
    onContinueLearning: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToExerciseHelp: (Int, String, String, String) -> Unit,
) {
    composable(
        route = "$CHAPTER_TEST_RESULT_ROUTE/{$CHAPTER_ID_ARG}/{$TEST_SCORE_ARG}/{$TEST_CORRECT_ARG}/{$TEST_TOTAL_ARG}",
        arguments = listOf(
            navArgument(CHAPTER_ID_ARG) {
                type = NavType.StringType
            },
            navArgument(TEST_SCORE_ARG) {
                type = NavType.IntType
            },
            navArgument(TEST_CORRECT_ARG) {
                type = NavType.IntType
            },
            navArgument(TEST_TOTAL_ARG) {
                type = NavType.IntType
            }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getString(CHAPTER_ID_ARG) ?: ""
        val testScore = backStackEntry.arguments?.getInt(TEST_SCORE_ARG) ?: 0
        val correctAnswers = backStackEntry.arguments?.getInt(TEST_CORRECT_ARG) ?: 0
        val totalQuestions = backStackEntry.arguments?.getInt(TEST_TOTAL_ARG) ?: 0
        
        // Get ViewModel to access full test data
        val viewModel: ChapterTestViewModel = hiltViewModel()
        val testState by viewModel.testState.collectAsState()
        
        // Try to get the full test result from ViewModel if available
        val testResult = when (testState) {
            is TestState.Completed -> (testState as TestState.Completed).result
            else -> {
                // Fallback: Create minimal result and trigger data loading
                androidx.compose.runtime.LaunchedEffect(chapterId) {
                    viewModel.loadChapterForResults(chapterId)
                }
                
                TestResult(
                    chapterId = chapterId,
                    chapterTitle = "Chapter Test",
                    score = testScore,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    wrongExercises = emptyList(),
                    userAnswers = emptyList(),
                    timeSpentSeconds = 0L,
                    exercises = emptyList() // Will be loaded by ViewModel
                )
            }
        }
        
        ChapterTestResultScreen(
            testResult = testResult,
            onRetakeTest = { viewModel.retakeTest(chapterId) },
            onRetakeQuestion = { exercise ->
                // TODO: Navigate to single question test
            },
            onBackToChapter = { onBackClick(chapterId) },
            onContinueLearning = onContinueLearning,
            onNavigateToHome = onNavigateToHome,
            onRequestLocalHelp = { exercise ->
                // Navigate to exercise help chat with local AI
                val exerciseNumber = testResult.exercises.indexOf(exercise) + 1
                val userAnswerText = testResult.userAnswers.getOrNull(testResult.exercises.indexOf(exercise))?.let { answerIndex ->
                    if (answerIndex >= 0 && answerIndex < exercise.options.size) {
                        exercise.options[answerIndex]
                    } else {
                        "No answer"
                    }
                } ?: "No answer"
                val correctAnswerText = exercise.options[exercise.correctAnswerIndex]
                
                onNavigateToExerciseHelp(exerciseNumber, exercise.questionText, userAnswerText, correctAnswerText)
            },
            onRequestVideoHelp = { exercise ->
                // Navigate to exercise help chat with video request (same as local for now)
                val exerciseNumber = testResult.exercises.indexOf(exercise) + 1
                val userAnswerText = testResult.userAnswers.getOrNull(testResult.exercises.indexOf(exercise))?.let { answerIndex ->
                    if (answerIndex >= 0 && answerIndex < exercise.options.size) {
                        exercise.options[answerIndex]
                    } else {
                        "No answer"
                    }
                } ?: "No answer"
                val correctAnswerText = exercise.options[exercise.correctAnswerIndex]
                
                onNavigateToExerciseHelp(exerciseNumber, exercise.questionText, userAnswerText, correctAnswerText)
            }
        )
    }
} 