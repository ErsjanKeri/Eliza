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

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai.edge.eliza.ai.modelmanager.data.TASK_ELIZA_CHAT
// Import Gallery-compatible ModelDownloadStatusType from core.model
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import com.example.ai.edge.eliza.ai.service.ElizaChatViewModel

/**
 * Full-screen ChatView that replicates Gallery's chat interface exactly.
 * Shows model selection, download status, and chat interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    initialMessages: List<ChatMessage> = emptyList(),
    chatViewModel: ElizaChatViewModel = hiltViewModel(),
    modelManager: ElizaModelManager = hiltViewModel()
) {
    val uiState by modelManager.uiState.collectAsState()
    val context = LocalContext.current
    var messages by remember { mutableStateOf(initialMessages) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Get the selected model for the chat task
    val task = TASK_ELIZA_CHAT
    val taskData = uiState.tasks.find { it.type.id == task.type.id }
    val modelsForTask = taskData?.models ?: emptyList()
    val selectedModel = uiState.selectedModel ?: modelsForTask.firstOrNull()
    val downloadStatus = selectedModel?.let { uiState.modelDownloadStatus[it.name] }
    val isModelReady = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
    
    // Initialize model if available
    LaunchedEffect(selectedModel) {
        selectedModel?.let { model ->
            if (!isModelReady) {
                modelManager.initializeModel(context = context.applicationContext, task = task, model = model)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Model selector chip - Gallery's exact pattern
            ModelSelectorChip(
                task = task,
                selectedModel = selectedModel,
                modelManager = modelManager,
                onModelSelected = { model ->
                    // Initialize the selected model
                    modelManager.initializeModel(
                        context = context.applicationContext,
                        task = task,
                        model = model
                    )
                }
            )
            
            // Main content area
            Box(modifier = Modifier.weight(1f)) {
                if (isModelReady && selectedModel != null) {
                    // Show chat interface when model is ready
                    ChatPanel(
                        messages = messages,
                        onSendMessage = { messageText ->
                            if (messageText.isNotBlank() && !isLoading) {
                                isLoading = true
                                val userMessage = ChatMessageText(
                                    content = messageText,
                                    side = ChatSide.USER
                                )
                                messages = messages + userMessage
                                
                                // Add loading message
                                val loadingMessage = ChatMessageLoading()
                                messages = messages + loadingMessage
                                
                                // Generate AI response
                                chatViewModel.generateResponse(
                                    model = selectedModel,
                                    input = messageText,
                                    resultListener = { response, isComplete ->
                                        if (isComplete) {
                                            // Remove loading message and add AI response
                                            messages = messages.dropLast(1) + ChatMessageText(
                                                content = response,
                                                side = ChatSide.AGENT
                                            )
                                            isLoading = false
                                        }
                                    },
                                    onError = {
                                        // Remove loading message and show error
                                        messages = messages.dropLast(1) + ChatMessageText(
                                            content = "Sorry, I encountered an error. Please try again.",
                                            side = ChatSide.AGENT
                                        )
                                        isLoading = false
                                    }
                                )
                            }
                        },
                        navigateUp = onNavigateUp,
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Show model download panel when model isn't ready
                    selectedModel?.let { model ->
                        ModelDownloadPanel(
                            model = model,
                            task = task,
                            modelManager = modelManager,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        // No model available
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No models available for this task",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Specialized ChatView for exercise help with context injection.
 */
@Composable
fun ExerciseHelpChatView(
    exerciseNumber: Int,
    questionText: String,
    userAnswer: String,
    correctAnswer: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ElizaChatViewModel = hiltViewModel()
) {
    val title = "Exercise #$exerciseNumber Help"
    val exerciseContext = """
        Question: $questionText
        
        Your answer: $userAnswer
        Correct answer: $correctAnswer
        
        How can I help you understand this better?
    """.trimIndent()
    
    val initialMessages = listOf(
        ChatMessageInfo(content = exerciseContext)
    )
    
    ChatView(
        title = title,
        onNavigateUp = onNavigateUp,
        initialMessages = initialMessages,
        chatViewModel = chatViewModel,
        modifier = modifier
    )
} 