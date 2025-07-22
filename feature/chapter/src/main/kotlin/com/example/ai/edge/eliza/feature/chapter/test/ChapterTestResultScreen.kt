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

package com.example.ai.edge.eliza.feature.chapter.test

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.component.ElizaBackground
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.core.designsystem.component.ElizaOutlinedButton
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme
import com.example.ai.edge.eliza.core.model.TestResult
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.Difficulty

/**
 * Screen showing test results with score, wrong questions, and action options.
 */
@Composable
fun ChapterTestResultScreen(
    testResult: TestResult,
    onRetakeTest: () -> Unit,
    onRequestHelp: (Exercise) -> Unit,
    onBackToChapter: () -> Unit,
    onContinueLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElizaBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                TestResultTopBar(
                    chapterTitle = testResult.chapterTitle,
                    onBackClick = onBackToChapter
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score display
                ScoreDisplay(testResult = testResult)
                
                // Completion status
                CompletionStatus(testResult = testResult)
                
                // Wrong questions breakdown (if any)
                if (testResult.wrongExercises.isNotEmpty()) {
                    WrongQuestionsSection(
                        wrongExercises = testResult.wrongExercises,
                        userAnswers = testResult.userAnswers,
                        onRequestHelp = onRequestHelp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                ActionButtons(
                    testResult = testResult,
                    onRetakeTest = onRetakeTest,
                    onBackToChapter = onBackToChapter,
                    onContinueLearning = onContinueLearning
                )
            }
        }
    }
}

/**
 * Top app bar for test results.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestResultTopBar(
    chapterTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Test Results",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to chapter"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

/**
 * Animated score display with circular progress.
 */
@Composable
private fun ScoreDisplay(
    testResult: TestResult,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = testResult.score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score emoji based on performance
            val scoreEmoji = when {
                testResult.score == 100 -> "🎉"
                testResult.score >= 80 -> "😊"
                testResult.score >= 60 -> "🙂"
                else -> "😔"
            }
            
            Text(
                text = scoreEmoji,
                style = MaterialTheme.typography.displayLarge
            )
            
            // Circular progress with score
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedScore },
                    modifier = Modifier.size(120.dp),
                    color = when {
                        testResult.score == 100 -> Color(0xFF26890C) // Green
                        testResult.score >= 80 -> MaterialTheme.colorScheme.primary // Blue
                        testResult.score >= 60 -> Color(0xFFD89E00) // Orange
                        else -> Color(0xFFE21B23) // Red
                    },
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${testResult.score}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = testResult.letterGrade,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "${testResult.correctAnswers} out of ${testResult.totalQuestions} correct",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = testResult.message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Completion status indicator.
 */
@Composable
private fun CompletionStatus(
    testResult: TestResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (testResult.isPassing) {
                Color(0xFF26890C).copy(alpha = 0.1f) // Light green
            } else {
                Color(0xFFE21B23).copy(alpha = 0.1f) // Light red
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (testResult.isPassing) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = if (testResult.isPassing) "Passed" else "Not passed",
                tint = if (testResult.isPassing) Color(0xFF26890C) else Color(0xFFE21B23),
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (testResult.isPassing) Icons.Default.CheckCircle else Icons.Default.Clear,
                        contentDescription = if (testResult.isPassing) "Complete" else "Incomplete",
                        tint = if (testResult.isPassing) Color(0xFF26890C) else Color(0xFFE21B23)
                    )
                    Text(
                        text = if (testResult.isPassing) "Chapter Complete!" else "Chapter Not Complete",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (testResult.isPassing) Color(0xFF26890C) else Color(0xFFE21B23)
                    )
                }
                
                Text(
                    text = if (testResult.isPassing) {
                        "Congratulations! You can proceed to the next chapter."
                    } else {
                        "You need 100% to complete this chapter. Try again!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Section showing wrong questions and help options.
 */
@Composable
private fun WrongQuestionsSection(
    wrongExercises: List<Exercise>,
    userAnswers: List<Int>,
    onRequestHelp: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📝 Questions to Review",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        wrongExercises.forEach { exercise ->
            WrongQuestionCard(
                exercise = exercise,
                userAnswer = userAnswers.getOrNull(
                    // Find the original index of this exercise
                    // This is a simplification - in real app, you'd track indices properly
                    wrongExercises.indexOf(exercise)
                ) ?: -1,
                onRequestHelp = { onRequestHelp(exercise) }
            )
        }
    }
}

/**
 * Card showing a wrong question with help options.
 */
@Composable
private fun WrongQuestionCard(
    exercise: Exercise,
    userAnswer: Int,
    onRequestHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = exercise.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            // Show user's wrong answer and correct answer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Your answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (userAnswer >= 0 && userAnswer < exercise.options.size) {
                            exercise.options[userAnswer]
                        } else {
                            "No answer"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE21B23) // Red for wrong answer
                    )
                }
                
                Column {
                    Text(
                        text = "Correct answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.options[exercise.correctAnswerIndex],
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF26890C) // Green for correct answer
                    )
                }
            }
            
            // Help options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElizaOutlinedButton(
                    onClick = onRequestHelp,
                    text = { Text("❓ Request Explanation") },
                    modifier = Modifier.weight(1f)
                )
                
                ElizaOutlinedButton(
                    onClick = onRequestHelp, // Could be different action for trials
                    text = { Text("🔄 Generate New Trial") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Action buttons for test completion.
 */
@Composable
private fun ActionButtons(
    testResult: TestResult,
    onRetakeTest: () -> Unit,
    onBackToChapter: () -> Unit,
    onContinueLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (testResult.isPassing) {
            // Passed - show continue option
            ElizaButton(
                onClick = onContinueLearning,
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Continue",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "Continue to Next Chapter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            ElizaOutlinedButton(
                onClick = onBackToChapter,
                text = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Return to Chapter"
                        )
                        Text("Return to Chapter")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Failed - show retake option
            ElizaButton(
                onClick = onRetakeTest,
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retake Test",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "Retake Test",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            ElizaOutlinedButton(
                onClick = onBackToChapter,
                text = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Chapter"
                        )
                        Text("Back to Chapter")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview of the test result screen.
 */
@Preview
@Composable
private fun ChapterTestResultScreenPreview() {
    ElizaTheme {
        val wrongExercises = listOf(
            Exercise(
                id = "2",
                chapterId = "chapter1",
                questionText = "What is the slope of y = 3x + 5?",
                options = listOf("3", "5", "-3", "8"),
                correctAnswerIndex = 0,
                explanation = "The coefficient of x is the slope"
            )
        )
        
        val testResult = TestResult(
            chapterId = "chapter1",
            chapterTitle = "Linear Equations", 
            score = 80,
            correctAnswers = 4,
            totalQuestions = 5,
            wrongExercises = wrongExercises,
            userAnswers = listOf(1, 1, 0, 0, 0), // User got question 2 wrong (answered 1 instead of 0)
            timeSpentSeconds = 180
        )
        
        ChapterTestResultScreen(
            testResult = testResult,
            onRetakeTest = { },
            onRequestHelp = { },
            onBackToChapter = { },
            onContinueLearning = { }
        )
    }
} 