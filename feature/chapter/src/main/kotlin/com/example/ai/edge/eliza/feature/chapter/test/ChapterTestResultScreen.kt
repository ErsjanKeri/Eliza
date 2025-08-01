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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
// Using existing icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    onRetakeQuestion: (Exercise) -> Unit,
    onBackToChapter: () -> Unit,
    onContinueLearning: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onRequestLocalHelp: (Exercise) -> Unit = {},
    onRequestVideoHelp: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElizaBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                TestResultTopBar(
                    chapterTitle = testResult.chapterTitle,
                    onBackClick = { onBackToChapter() }
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
                
                // All questions breakdown
                if (testResult.exercises.isNotEmpty()) {
                    AllQuestionsSection(
                        exercises = testResult.exercises,
                        userAnswers = testResult.userAnswers,
                        onRetakeQuestion = onRetakeQuestion,
                        onRequestLocalHelp = onRequestLocalHelp,
                        onRequestVideoHelp = onRequestVideoHelp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                ActionButtons(
                    testResult = testResult,
                    onRetakeTest = onRetakeTest,
                    onBackToChapter = onBackToChapter,
                    onContinueLearning = onContinueLearning,
                    onNavigateToHome = onNavigateToHome
                )
                
                // Bottom spacing for visual balance
                Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Centered circular progress with score
            Box(
                modifier = Modifier.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedScore },
                    modifier = Modifier.size(140.dp),
                    color = when {
                        testResult.score == 100 -> Color(0xFF26890C) // Green
                        testResult.score >= 80 -> MaterialTheme.colorScheme.primary // Blue
                        testResult.score >= 60 -> Color(0xFFD89E00) // Orange
                        else -> Color(0xFFE21B23) // Red
                    },
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${testResult.score}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = testResult.letterGrade,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "${testResult.correctAnswers} out of ${testResult.totalQuestions} correct",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            if (testResult.message.isNotEmpty()) {
                Text(
                    text = testResult.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



/**
 * Section showing all questions with their results and help options.
 */
@Composable
private fun AllQuestionsSection(
    exercises: List<Exercise>,
    userAnswers: List<Int>,
    onRetakeQuestion: (Exercise) -> Unit,
    onRequestLocalHelp: (Exercise) -> Unit = {},
    onRequestVideoHelp: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for accordion behavior - track which question is expanded
    var expandedQuestionIndex by remember { mutableStateOf(-1) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Question Results",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        exercises.forEachIndexed { index, exercise ->
            val userAnswer = userAnswers.getOrNull(index) ?: -1
            val isCorrect = userAnswer == exercise.correctAnswerIndex
            val isExpanded = expandedQuestionIndex == index
            
            QuestionResultCard(
                questionNumber = index + 1,
                exercise = exercise,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                isExpanded = isExpanded,
                onCardClick = { 
                    expandedQuestionIndex = if (isExpanded) -1 else index
                },
                onRetakeQuestion = { onRetakeQuestion(exercise) },
                onRequestLocalHelp = onRequestLocalHelp,
                onRequestVideoHelp = onRequestVideoHelp
            )
        }
    }
}

/**
 * Card showing a question result with status indicator and expandable details.
 */
@Composable
private fun QuestionResultCard(
    questionNumber: Int,
    exercise: Exercise,
    userAnswer: Int,
    isCorrect: Boolean,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onRetakeQuestion: () -> Unit,
    onRequestLocalHelp: (Exercise) -> Unit = {},
    onRequestVideoHelp: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) -90f else 0f,
        animationSpec = tween(300),
        label = "arrow_rotation"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Compact header showing question number and status - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status icon
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = if (isCorrect) "Correct" else "Incorrect",
                    tint = if (isCorrect) Color(0xFF26890C) else Color(0xFFE21B23),
                    modifier = Modifier.size(24.dp)
                )
                
                // Question number and brief text
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Question $questionNumber",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCorrect) "Correct" else "Incorrect",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) Color(0xFF26890C) else Color(0xFFE21B23)
                    )
                }
                
                // Animated expand/collapse arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Animated expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                QuestionExpandedContent(
                    exercise = exercise,
                    userAnswer = userAnswer,
                    isCorrect = isCorrect,
                    onRetakeQuestion = onRetakeQuestion,
                    onRequestLocalHelp = onRequestLocalHelp,
                    onRequestVideoHelp = onRequestVideoHelp
                )
            }
        }
    }
}

/**
 * Expanded content showing full question details and help options.
 */
@Composable
private fun QuestionExpandedContent(
    exercise: Exercise,
    userAnswer: Int,
    isCorrect: Boolean,
    onRetakeQuestion: () -> Unit,
    onRequestLocalHelp: (Exercise) -> Unit = {},
    onRequestVideoHelp: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Divider line to separate from header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        // Full question text with improved styling
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = exercise.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        // Answer comparison section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Your answer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (isCorrect) Color(0xFF26890C) else Color(0xFFE21B23),
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your answer:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (userAnswer >= 0 && userAnswer < exercise.options.size) {
                                exercise.options[userAnswer]
                            } else {
                                "No answer provided"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isCorrect) Color(0xFF26890C) else Color(0xFFE21B23)
                        )
                    }
                }
                
                // Correct answer (only show if user was wrong)
                if (!isCorrect) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color(0xFF26890C),
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Correct answer:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = exercise.options[exercise.correctAnswerIndex],
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF26890C)
                            )
                        }
                    }
                }
            }
        }
        
        // Exercise help section (only for wrong answers)
        if (!isCorrect) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Get help with this question:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Local AI Help button
                    OutlinedButton(
                        onClick = { onRequestLocalHelp(exercise) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Local AI Help",
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Local AI Help", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    
                    // Video Help button  
                    OutlinedButton(
                        onClick = { onRequestVideoHelp(exercise) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Video Help",
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Video Help", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        
        // Help options with network-aware dual buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Practice with this question:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Practice option
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Practice more:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                ElizaButton(
                    onClick = onRetakeQuestion,
                    text = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retake",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Retake Question")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
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
    onNavigateToHome: () -> Unit = {},
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
                onClick = onRetakeTest,
                text = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retake Test"
                        )
                        Text("Retake Test")
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
            
            // Add main home button
            ElizaOutlinedButton(
                onClick = onNavigateToHome,
                text = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Main Page"
                        )
                        Text("Main Home")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Failed - show retake test as primary action
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
            
            // Add main home button for failed case too
            ElizaOutlinedButton(
                onClick = onNavigateToHome,
                text = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Main Page"
                        )
                        Text("Main Home")
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
            onRetakeQuestion = { },
            onBackToChapter = { },
            onContinueLearning = { },
            onNavigateToHome = { },
            onRequestLocalHelp = { },
            onRequestVideoHelp = { }
        )
    }
} 