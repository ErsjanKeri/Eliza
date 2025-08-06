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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai.edge.eliza.core.model.SupportedLanguage
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
import com.example.ai.edge.eliza.feature.chat.ui.MemoryWarningDialog

/**
 * Dialog for selecting difficulty level for exercise generation.
 * Matches the specification's design with relative difficulty options.
 */
@Composable
fun DifficultySelectionDialog(
    originalExercise: Exercise,
    userLanguage: SupportedLanguage,
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
    val isModelCancelled = initStatus?.status == ModelInitializationStatusType.CANCELLED_DUE_TO_MEMORY
    
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
        shape = RoundedCornerShape(0.dp), // Square corners like rest of app
        containerColor = MaterialTheme.colorScheme.surface, // Clean white background
        title = {
            Text(
                text = "Generate New Practice Question",
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
                        // FIXED: Add missing selectModel call - Gallery's exact pattern
                        modelManager.selectModel(model)
                        // Initialize the selected model
                        modelManager.initializeModel(
                            context = context.applicationContext,
                            task = task,
                            model = model
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Original question preview - 100% width with white text
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer // Light blue background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(0.dp), // Square corners consistency
                    modifier = Modifier.fillMaxWidth() // 100% width as requested
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Original Question:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White // Pure white text as requested
                        )
                        Text(
                            text = originalExercise.questionText.get(userLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White, // Pure white text as requested
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
                    text = "Concept Focus: ${originalExercise.explanation.get(userLanguage).take(50)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Surface(
                shape = RoundedCornerShape(0.dp), // Square corners like home page
                color = when {
                    isModelCancelled -> MaterialTheme.colorScheme.errorContainer
                    isModelReady -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.height(48.dp)
            ) {
                Button(
                    onClick = { 
                        if (!isModelCancelled) {
                            selectedModel?.let { model ->
                                onDifficultySelected(selectedDifficulty, model)
                            }
                        }
                    },
                    enabled = isModelReady && !isModelCancelled,
                    shape = RoundedCornerShape(0.dp), // Square corners like home page
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isModelCancelled -> MaterialTheme.colorScheme.errorContainer
                            isModelReady -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = when {
                            isModelCancelled -> MaterialTheme.colorScheme.onErrorContainer
                            isModelReady -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isModelCancelled -> Icons.Filled.Close
                                isModelReady -> Icons.Filled.Build
                                else -> Icons.Filled.Info
                            },
                            contentDescription = null,
                            tint = when {
                                isModelCancelled -> MaterialTheme.colorScheme.onErrorContainer
                                isModelReady -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when {
                                isModelCancelled -> "Eliza cannot run on this device :("
                                isModelReady -> "Generate Question"
                                else -> "Eliza is getting ready..."
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
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
    
    // Memory Warning Dialog - Same pattern as ChatView.kt lines 349-369
    val warningModel = uiState.memoryWarningModel
    val warningCompatibility = uiState.memoryWarningCompatibility
    val warningDeviceInfo = uiState.memoryWarningDeviceInfo
    
    if (uiState.showMemoryWarning && 
        warningModel != null && 
        warningCompatibility != null && 
        warningDeviceInfo != null) {
        
        MemoryWarningDialog(
            model = warningModel,
            compatibility = warningCompatibility,
            deviceInfo = warningDeviceInfo,
            onProceedAnyway = {
                modelManager.proceedWithMemoryWarning(context, task)
            },
            onSwitchToSaferModel = {
                modelManager.switchToSaferModel(context, task)
            },
            onCancel = {
                modelManager.cancelDueToMemoryWarning()
            }
        )
    }
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
    userLanguage: SupportedLanguage,
    onDismiss: () -> Unit,
    onPracticeQuestion: (Trial) -> Unit,
    onGenerateAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = if (generationState !is GenerationResult.Loading) onDismiss else { {} },
        shape = RoundedCornerShape(0.dp), // Square corners like the rest of the app
        containerColor = MaterialTheme.colorScheme.surface, // Clean white background
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (generationState) {
                        is GenerationResult.Loading -> Icons.Filled.Build
                        is GenerationResult.Success -> Icons.Filled.Create
                        is GenerationResult.Error -> Icons.Filled.Close
                    },
                    contentDescription = null,
                    tint = when (generationState) {
                        is GenerationResult.Loading -> MaterialTheme.colorScheme.primary
                        is GenerationResult.Success -> MaterialTheme.colorScheme.primary
                        is GenerationResult.Error -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = when (generationState) {
                        is GenerationResult.Loading -> "Generating Question..."
                        is GenerationResult.Success -> "Generated Practice Question"
                        is GenerationResult.Error -> "Generation Failed"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        text = {
            when (generationState) {
                is GenerationResult.Loading -> {
                    LoadingContent(message = generationState.message)
                }
                is GenerationResult.Success -> {
                    GeneratedQuestionPreview(trial = generationState.trial, userLanguage = userLanguage)
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Generate Another",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Practice This Question",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Discard")
                    }
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
    userLanguage: SupportedLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Generation info
        Text(
            // TODO replace ⚡
            text = "Generated with Gemma 3n (${trial.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} Difficulty)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Question preview
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface // Clean white background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(0.dp) // Square corners consistency
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
                    text = trial.questionText.get(userLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                // Elegant spacing between question and alternatives
                Spacer(modifier = Modifier.height(16.dp))
                
                // Options preview - styled as cards with 100% width and elegant spacing
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Elegant spacing between alternatives
                ) {
                    trial.options.forEachIndexed { index, option ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant // Uniform background for all options
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadows
                            shape = RoundedCornerShape(0.dp), // Square corners consistency
                            modifier = Modifier.fillMaxWidth() // 100% width as requested
                        ) {
                            Text(
                                text = "${('A' + index)} $option", // No indication of correct answer
                                style = MaterialTheme.typography.bodyMedium, // Slightly larger text for better readability
                                color = MaterialTheme.colorScheme.onSurfaceVariant, // Uniform text color
                                modifier = Modifier.padding(16.dp) // More generous padding for elegance
                            )
                        }
                    }
                }
                // removed the ai explanation as we do not need it 
            }
        }
    }
}