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

package com.example.ai.edge.eliza.feature.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Import Gallery-compatible classes from core.model
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gallery-style model download status panel
 * Copied exactly from Gallery's ModelDownloadStatusInfoPanel pattern
 */
@Composable
fun ModelDownloadPanel(
    model: Model,
    task: Task,
    modelManager: ElizaModelManager = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by modelManager.uiState.collectAsState()
    
    // Gallery's exact status derivation pattern
    val downloadStatus by remember {
        derivedStateOf { uiState.modelDownloadStatus[model.name] }
    }
    
    // Gallery's exact conditional display pattern with delays
    var shouldShowDownloadingAnimation by remember { mutableStateOf(false) }
    var downloadingAnimationConditionMet by remember { mutableStateOf(false) }
    var shouldShowDownloadModelButton by remember { mutableStateOf(false) }
    var downloadModelButtonConditionMet by remember { mutableStateOf(false) }

    // Gallery's exact condition logic
    downloadingAnimationConditionMet =
        downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS ||
        downloadStatus?.status == ModelDownloadStatusType.PARTIALLY_DOWNLOADED ||
        downloadStatus?.status == ModelDownloadStatusType.UNZIPPING
    
    downloadModelButtonConditionMet =
        downloadStatus?.status == ModelDownloadStatusType.FAILED ||
        downloadStatus?.status == ModelDownloadStatusType.NOT_DOWNLOADED

    // Gallery's exact delay pattern to prevent UI flickering
    LaunchedEffect(downloadingAnimationConditionMet) {
        if (downloadingAnimationConditionMet) {
            delay(100)
            shouldShowDownloadingAnimation = true
        } else {
            shouldShowDownloadingAnimation = false
        }
    }

    LaunchedEffect(downloadModelButtonConditionMet) {
        if (downloadModelButtonConditionMet) {
            delay(700)
            shouldShowDownloadModelButton = true
        } else {
            shouldShowDownloadModelButton = false
        }
    }

    // Main panel layout
    Box(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (downloadStatus?.status) {
            ModelDownloadStatusType.SUCCEEDED -> {
                // Model is ready - show success message
                Text(
                    text = "✅ ${model.name} is ready",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            
            else -> {
                // Gallery's animated visibility pattern
                AnimatedVisibility(
                    visible = shouldShowDownloadingAnimation,
                    enter = scaleIn(initialScale = 0.9f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.9f) + fadeOut(),
                ) {
                    ModelDownloadingContent(
                        model = model,
                        task = task,
                        downloadStatus = downloadStatus,
                        modelManager = modelManager
                    )
                }

                AnimatedVisibility(
                    visible = shouldShowDownloadModelButton,
                    enter = scaleIn(initialScale = 0.9f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.9f) + fadeOut(),
                ) {
                    ModelDownloadButton(
                        model = model,
                        task = task,
                        downloadStatus = downloadStatus,
                        modelManager = modelManager
                    )
                }
            }
        }
    }
}

/**
 * Gallery-style downloading content with progress
 */
@Composable
private fun ModelDownloadingContent(
    model: Model,
    task: Task,
    downloadStatus: ModelDownloadStatus?,
    modelManager: ElizaModelManager
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gallery's model info display
        Text(
            text = model.name,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        // Gallery's status text pattern
        val statusText = when (downloadStatus?.status) {
            ModelDownloadStatusType.IN_PROGRESS -> {
                val receivedMB = (downloadStatus.receivedBytes / 1024 / 1024).toInt()
                val totalMB = (downloadStatus.totalBytes / 1024 / 1024).toInt()
                "$receivedMB MB of $totalMB MB"
            }
            ModelDownloadStatusType.PARTIALLY_DOWNLOADED -> {
                val receivedMB = (downloadStatus.receivedBytes / 1024 / 1024).toInt()
                val totalMB = (downloadStatus.totalBytes / 1024 / 1024).toInt()
                "$receivedMB MB of $totalMB MB (resuming...)"
            }
            ModelDownloadStatusType.UNZIPPING -> "Unzipping..."
            else -> "Preparing download..."
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        // Gallery's progress indicator pattern
        when (downloadStatus?.status) {
            ModelDownloadStatusType.IN_PROGRESS,
            ModelDownloadStatusType.PARTIALLY_DOWNLOADED -> {
                val progress = if (downloadStatus.totalBytes > 0) {
                    downloadStatus.receivedBytes.toFloat() / downloadStatus.totalBytes.toFloat()
                } else 0f
                
                val animatedProgress = remember { Animatable(0f) }
                LaunchedEffect(progress) {
                    animatedProgress.animateTo(progress, animationSpec = tween(150))
                }
                
                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                )
            }
            
            ModelDownloadStatusType.UNZIPPING -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                )
            }
            
            else -> {
                CircularProgressIndicator()
            }
        }

        // Gallery's cancel button pattern
        if (downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS ||
            downloadStatus?.status == ModelDownloadStatusType.UNZIPPING) {
            IconButton(
                onClick = { 
                    // TODO: Implement cancel download
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel download"
                )
            }
        }

        // Gallery's background download message
        Text(
            text = "Feel free to switch apps or lock your device.\n" +
                   "The download will continue in the background.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * Gallery-style download button
 */
@Composable
private fun ModelDownloadButton(
    model: Model,
    task: Task,
    downloadStatus: ModelDownloadStatus?,
    modelManager: ElizaModelManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = model.name,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = model.info,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
        
        // Size display
        val sizeMB = (model.sizeInBytes / 1024 / 1024).toInt()
        Text(
            text = "$sizeMB MB",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Gallery's download button pattern with proper threading
        Button(
            onClick = { 
                if (!isProcessing) {
                    isProcessing = true
                    // EXACT Gallery pattern: Use Dispatchers.IO for network operations
                    scope.launch(Dispatchers.IO) {
                        try {
                            // Call the download method which now uses proper threading
                            withContext(Dispatchers.Main) {
                                modelManager.downloadModel(context, task, model)
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                            }
                        }
                    }
                }
            },
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = when {
                    isProcessing -> "Processing..."
                    downloadStatus?.status == ModelDownloadStatusType.FAILED -> "Retry Download"
                    else -> "Download Model"
                },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Error message for failed downloads
        if (downloadStatus?.status == ModelDownloadStatusType.FAILED) {
            Text(
                text = downloadStatus.errorMessage.ifEmpty { "Download failed" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
} 