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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.component.ElizaBackground
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.core.designsystem.component.ElizaOutlinedButton
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme
import com.example.ai.edge.eliza.core.designsystem.theme.LocalTestColors
import com.example.ai.edge.eliza.core.model.ChapterTest
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.Difficulty

/**
 * Main screen for taking chapter tests with Kahoot-inspired design.
 * Features progress tracking, question navigation, and beautiful UI.
 */
@Composable
fun ChapterTestScreen(
    chapterTest: ChapterTest,
    onAnswerSelected: (Int) -> Unit,
    onNavigateToQuestion: (Int) -> Unit,
    onPreviousQuestion: () -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitTest: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElizaBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                ChapterTestTopBar(
                    chapterTitle = chapterTest.chapterTitle,
                    currentQuestion = chapterTest.currentQuestionIndex + 1,
                    totalQuestions = chapterTest.exercises.size,
                    progress = chapterTest.progress,
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Question progress indicators
                QuestionIndicators(
                    currentQuestion = chapterTest.currentQuestionIndex,
                    totalQuestions = chapterTest.exercises.size,
                    userAnswers = chapterTest.userAnswers,
                    onQuestionClick = onNavigateToQuestion
                )
                
                // Current question display
                val currentExercise = chapterTest.exercises[chapterTest.currentQuestionIndex]
                QuestionDisplay(
                    exercise = currentExercise,
                    questionNumber = chapterTest.currentQuestionIndex + 1,
                    userAnswer = chapterTest.userAnswers[chapterTest.currentQuestionIndex],
                    onAnswerSelected = onAnswerSelected
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Navigation controls
                NavigationControls(
                    canGoPrevious = chapterTest.canGoPrevious,
                    canGoNext = chapterTest.canGoNext,
                    isAllAnswered = chapterTest.isAllAnswered,
                    onPreviousClick = onPreviousQuestion,
                    onNextClick = onNextQuestion,
                    onSubmitClick = onSubmitTest
                )
            }
        }
    }
}

/**
 * Top app bar with progress indicator and chapter info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterTestTopBar(
    chapterTitle: String,
    currentQuestion: Int,
    totalQuestions: Int,
    progress: Float,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Chapter Test",
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
            actions = {
                Text(
                    text = "$currentQuestion/$totalQuestions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Progress bar
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 300),
            label = "progress"
        )
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Question indicator dots showing progress and allowing navigation.
 */
@Composable
private fun QuestionIndicators(
    currentQuestion: Int,
    totalQuestions: Int,
    userAnswers: List<Int?>,
    onQuestionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(totalQuestions) { index ->
            val isAnswered = userAnswers[index] != null
            val isCurrent = index == currentQuestion
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { onQuestionClick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isAnswered && !isCurrent) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Answered",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimary
                            isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Main question display with Kahoot-style answer options.
 */
@Composable
private fun QuestionDisplay(
    exercise: Exercise,
    questionNumber: Int,
    userAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Question text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = exercise.questionText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Start
                )
            }
        }
        
        // Answer options (Kahoot-style)
        KahootAnswerOptions(
            options = exercise.options,
            selectedAnswer = userAnswer,
            onAnswerSelected = onAnswerSelected
        )
    }
}

/**
 * Educational test answer options with distinctive colors.
 * Supports up to 20 different alternatives with accessible color choices.
 */
@Composable
private fun KahootAnswerOptions(
    options: List<String>,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use Eliza test colors - supports up to 20 options
    val testColors = LocalTestColors.current
    val optionColors = testColors.getColorsForCount(options.size)
    
    // Generate labels A, B, C, D, ... up to T (20 options)
    val optionLabels = (0 until options.size).map { 
        ('A' + it).toString()
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedAnswer == index
            val backgroundColor = if (isSelected) {
                optionColors[index]
            } else {
                MaterialTheme.colorScheme.surface
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAnswerSelected(index) },
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 6.dp else 2.dp
                ),
                border = if (isSelected) null else 
                    androidx.compose.foundation.BorderStroke(
                        2.dp, 
                        optionColors[index].copy(alpha = 0.3f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option indicator
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (isSelected) Color.White else optionColors[index],
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabels[index],
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) optionColors[index] else Color.White
                        )
                    }
                    
                    // Option text
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Navigation controls for moving between questions and submitting.
 */
@Composable
private fun NavigationControls(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    isAllAnswered: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        if (canGoPrevious) {
            ElizaOutlinedButton(
                onClick = onPreviousClick,
                text = { Text("← Previous") }
            )
        } else {
            Spacer(modifier = Modifier.width(100.dp))
        }
        
        // Next/Submit button
        if (canGoNext) {
            ElizaButton(
                onClick = onNextClick,
                text = { Text("Next →") }
            )
        } else if (isAllAnswered) {
            ElizaButton(
                onClick = onSubmitClick,
                text = { 
                    Text(
                        "🎯 Submit Test",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            )
        } else {
            ElizaOutlinedButton(
                onClick = { },
                enabled = false,
                text = { Text("Answer to continue") }
            )
        }
    }
}

/**
 * Preview of the chapter test screen.
 */
@Preview
@Composable
private fun ChapterTestScreenPreview() {
    ElizaTheme {
        val sampleExercises = listOf(
            Exercise(
                id = "1",
                chapterId = "chapter1",
                questionText = "What is the solution to 2x + 7 = 19?",
                options = listOf("x = 5", "x = 6", "x = 7", "x = 8"),
                correctAnswerIndex = 1,
                explanation = "Subtract 7, then divide by 2"
            ),
            Exercise(
                id = "2", 
                chapterId = "chapter1",
                questionText = "What is the slope of y = 3x + 5?",
                options = listOf("3", "5", "-3", "8"),
                correctAnswerIndex = 0,
                explanation = "The coefficient of x is the slope"
            )
        )
        
        val testState = ChapterTest(
            chapterId = "chapter1",
            chapterTitle = "Linear Equations",
            exercises = sampleExercises,
            currentQuestionIndex = 0,
            userAnswers = listOf(1, null)
        )
        
        ChapterTestScreen(
            chapterTest = testState,
            onAnswerSelected = { },
            onNavigateToQuestion = { },
            onPreviousQuestion = { },
            onNextQuestion = { },
            onSubmitTest = { },
            onBackClick = { }
        )
    }
} 