package com.example.ai.edge.eliza.feature.chapter.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai.edge.eliza.ai.modelmanager.data.TASK_ELIZA_CHAT
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import com.example.ai.edge.eliza.ai.modelmanager.manager.ModelInitializationStatusType
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.GenerationResult
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.core.model.RelativeDifficulty
import com.example.ai.edge.eliza.core.model.Trial
import com.example.ai.edge.eliza.feature.chat.ui.ModelSelectorChip

/**
 * Dialog for selecting difficulty level for exercise generation.
 * Matches the specification's design with relative difficulty options.
 */
@Composable
fun DifficultySelectionDialog(
    originalExercise: Exercise,
    onDifficultySelected: (RelativeDifficulty, Model) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    modelManager: ElizaModelManager = hiltViewModel()
) {
    // Copy exact model selection logic from ChatView
    val uiState by modelManager.uiState.collectAsState()
    val context = LocalContext.current
    var selectedDifficulty by remember { mutableStateOf(RelativeDifficulty.SAME) }
    
    // Get the selected model for the chat task (same as ChatView)
    val task = TASK_ELIZA_CHAT
    val taskData = uiState.tasks.find { it.type.id == task.type.id }
    val modelsForTask = taskData?.models ?: emptyList()
    val selectedModel = uiState.selectedModel ?: modelsForTask.firstOrNull()
    val downloadStatus = selectedModel?.let { uiState.modelDownloadStatus[it.name] }
    val initStatus = selectedModel?.let { uiState.modelInitializationStatus[it.name] }
    val isModelReady = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED && 
                      initStatus?.status == ModelInitializationStatusType.INITIALIZED &&
                      selectedModel?.instance != null
    
    // Initialize model if available (same as ChatView)
    LaunchedEffect(selectedModel) {
        selectedModel?.let { model ->
            if (!isModelReady) {
                modelManager.initializeModel(context = context.applicationContext, task = task, model = model)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎲 Generate New Practice Question",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Model selector chip - Gallery's exact pattern (same as ChatView)
                ModelSelectorChip(
                    task = task,
                    selectedModel = selectedModel,
                    modelManager = modelManager,
                    onModelSelected = { model ->
                        // Initialize the selected model (same as ChatView)
                        modelManager.initializeModel(
                            context = context.applicationContext,
                            task = task,
                            model = model
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Original question preview
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Original Question:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = originalExercise.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Difficulty selection
                Text(
                    text = "Select Difficulty:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                // Difficulty options
                RelativeDifficulty.values().forEach { difficulty ->
                    DifficultyOption(
                        difficulty = difficulty,
                        isSelected = selectedDifficulty == difficulty,
                        onSelect = { selectedDifficulty = difficulty },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Concept focus display
                Text(
                    text = "Concept Focus: ${originalExercise.explanation.take(50)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Surface(
                shape = RoundedCornerShape(0.dp), // Square corners like home page
                color = if (isModelReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(48.dp)
            ) {
                Button(
                    onClick = { 
                        selectedModel?.let { model ->
                            onDifficultySelected(selectedDifficulty, model)
                        }
                    },
                    enabled = isModelReady,
                    shape = RoundedCornerShape(0.dp), // Square corners like home page
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isModelReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isModelReady) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isModelReady) "🤖 Generate Question" else "⏳ Loading Model...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        },
        dismissButton = {
            Surface(
                shape = RoundedCornerShape(0.dp), // Square corners like home page
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(48.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(0.dp), // Square corners like home page
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Individual difficulty option with radio button and description.
 */
@Composable
private fun DifficultyOption(
    difficulty: RelativeDifficulty,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = difficulty.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = difficulty.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog showing generation progress and results.
 * Handles loading, success, and error states.
 */
@Composable
fun ExerciseGenerationDialog(
    generationState: GenerationResult,
    onDismiss: () -> Unit,
    onPracticeQuestion: (Trial) -> Unit,
    onGenerateAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = if (generationState !is GenerationResult.Loading) onDismiss else { {} },
        title = {
            Text(
                text = when (generationState) {
                    is GenerationResult.Loading -> "⚡ Generating Question..."
                    is GenerationResult.Success -> "🎲 Generated Practice Question"
                    is GenerationResult.Error -> "❌ Generation Failed"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            when (generationState) {
                is GenerationResult.Loading -> {
                    LoadingContent(message = generationState.message)
                }
                is GenerationResult.Success -> {
                    GeneratedQuestionPreview(trial = generationState.trial)
                }
                is GenerationResult.Error -> {
                    ErrorContent(message = generationState.message)
                }
            }
        },
        confirmButton = {
            when (generationState) {
                is GenerationResult.Success -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Secondary button - Generate Another (square like home page)
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            TextButton(
                                onClick = onGenerateAnother,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🔄 Generate Another",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Primary button - Practice Question (square like home page)
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Button(
                                onClick = { onPracticeQuestion(generationState.trial) },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✅ Practice This Question",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
                is GenerationResult.Error -> {
                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Button(
                            onClick = onGenerateAnother,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Try Again",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
                else -> { /* Loading state - no buttons */ }
            }
        },
        dismissButton = if (generationState !is GenerationResult.Loading) {
            {
                TextButton(onClick = onDismiss) {
                    Text("❌ Discard")
                }
            }
        } else null,
        modifier = modifier
    )
}

/**
 * Loading content with progress indicator and message - improved symmetry and centering.
 */
@Composable
private fun LoadingContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Centered progress indicator with consistent sizing
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
            
            // Centered message text with better typography
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Error content with error message - improved centering and symmetry.
 */
@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error icon for visual consistency
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Please try again with a different difficulty level or check your internet connection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Preview of the generated question with all details.
 */
@Composable
private fun GeneratedQuestionPreview(
    trial: Trial,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Generation info
        Text(
            text = "⚡ Generated with Gemma 3n (${trial.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} Difficulty)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Question preview
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Question:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trial.questionText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Options preview
                trial.options.forEachIndexed { index, option ->
                    val isCorrect = index == trial.correctAnswerIndex
                    Text(
                        text = "${('A' + index)} $option ${if (isCorrect) "✅" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // AI explanation preview
                Text(
                    text = "AI Explanation:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trial.explanation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}