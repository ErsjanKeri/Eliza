package com.example.ai.edge.eliza.feature.chapter.test

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai.edge.eliza.core.designsystem.component.ElizaBackground
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.core.model.Trial

/**
 * Screen for practicing with AI-generated trial questions.
 * Reuses existing test UI components for consistency.
 */
@Composable
fun TrialPracticeScreen(
    trial: Trial,
    onAnswerSubmitted: (Int, Boolean) -> Unit,
    onGenerateAnother: () -> Unit,
    onBackToResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    
    ElizaBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                TrialPracticeTopBar(
                    onBackClick = onBackToResults
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
                // Practice indicator
                PracticeHeader(trial = trial)
                
                // Question content
                QuestionContent(
                    trial = trial,
                    selectedAnswer = selectedAnswer,
                    isAnswered = isAnswered,
                    onAnswerSelected = { answerIndex ->
                        if (!isAnswered) {
                            selectedAnswer = answerIndex
                        }
                    }
                )
                
                // Submit/Results section
                if (!isAnswered) {
                    SubmitSection(
                        hasSelectedAnswer = selectedAnswer != null,
                        onSubmit = {
                            selectedAnswer?.let { answer ->
                                isAnswered = true
                                isCorrect = answer == trial.correctAnswerIndex
                                onAnswerSubmitted(answer, isCorrect)
                            }
                        }
                    )
                } else {
                    ResultSection(
                        trial = trial,
                        userAnswer = selectedAnswer!!,
                        isCorrect = isCorrect,
                        onGenerateAnother = onGenerateAnother,
                        onTryAgain = {
                            selectedAnswer = null
                            isAnswered = false
                            isCorrect = false
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Top app bar for trial practice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrialPracticeTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "🎲 Practice Question",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to results"
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Header showing this is a practice question.
 */
@Composable
private fun PracticeHeader(
    trial: Trial,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI-Generated Practice Question",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Difficulty: ${trial.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Main question content with options.
 */
@Composable
private fun QuestionContent(
    trial: Trial,
    selectedAnswer: Int?,
    isAnswered: Boolean,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question text
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = trial.questionText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Answer options
        trial.options.forEachIndexed { index, option ->
            AnswerOption(
                optionText = option,
                optionLetter = ('A' + index).toString(),
                isSelected = selectedAnswer == index,
                isCorrect = isAnswered && index == trial.correctAnswerIndex,
                isWrong = isAnswered && selectedAnswer == index && index != trial.correctAnswerIndex,
                isRevealed = isAnswered,
                onClick = { onAnswerSelected(index) }
            )
        }
    }
}

/**
 * Individual answer option - reusing the design pattern from test screens.
 */
@Composable
private fun AnswerOption(
    optionText: String,
    optionLetter: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCorrect && isRevealed -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        isWrong && isRevealed -> Color(0xFFF44336).copy(alpha = 0.2f)
        isSelected && !isRevealed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isCorrect && isRevealed -> Color(0xFF4CAF50)
        isWrong && isRevealed -> Color(0xFFF44336)
        isSelected && !isRevealed -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option letter
            Box(
                modifier = Modifier
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLetter,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = borderColor
                )
            }
            
            // Option text
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            // Status indicator
            if (isRevealed) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Submit button section.
 */
@Composable
private fun SubmitSection(
    hasSelectedAnswer: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElizaButton(
        onClick = onSubmit,
        enabled = hasSelectedAnswer,
        text = { Text("Submit Answer") },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Results section shown after answering.
 */
@Composable
private fun ResultSection(
    trial: Trial,
    userAnswer: Int,
    isCorrect: Boolean,
    onGenerateAnother: () -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrect) {
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                } else {
                    Color(0xFFF44336).copy(alpha = 0.2f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCorrect) "🎉 Correct!" else "❌ Incorrect",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                if (!isCorrect) {
                    Text(
                        text = "Correct answer: ${trial.options[trial.correctAnswerIndex]}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // AI Explanation
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI Explanation:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = trial.explanation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElizaButton(
                onClick = onTryAgain,
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Try Again")
                    }
                },
                modifier = Modifier.weight(1f)
            )
            
            ElizaButton(
                onClick = onGenerateAnother,
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Generate Another")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}