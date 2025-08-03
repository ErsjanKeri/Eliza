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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import com.example.ai.edge.eliza.ai.service.ElizaChatViewModel
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.feature.chat.ui.sidebar.ChatSidebar

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
    chatContext: ChatContext? = null,
    chatViewModel: ElizaChatViewModel = hiltViewModel(),
    modelManager: ElizaModelManager = hiltViewModel(),
    enhancedChatViewModel: EnhancedChatViewModel = hiltViewModel(),
    showSidebarToggle: Boolean = false
) {
    val uiState by modelManager.uiState.collectAsState()
    val sidebarState by enhancedChatViewModel.sidebarState.collectAsState()
    val context = LocalContext.current
    var messages by remember { mutableStateOf(initialMessages.toMutableList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentStreamingMessage by remember { mutableStateOf<ChatMessageText?>(null) }
    
    // Get the selected model for the chat task
    val task = TASK_ELIZA_CHAT
    val taskData = uiState.tasks.find { it.type.id == task.type.id }
    val modelsForTask = taskData?.models ?: emptyList()
    val selectedModel = uiState.selectedModel ?: modelsForTask.firstOrNull()
    val downloadStatus = selectedModel?.let { uiState.modelDownloadStatus[it.name] }
    val initStatus = selectedModel?.let { uiState.modelInitializationStatus[it.name] }
    val isModelReady = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED && 
                      initStatus?.status == com.example.ai.edge.eliza.ai.modelmanager.manager.ModelInitializationStatusType.INITIALIZED &&
                      selectedModel?.instance != null
    
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
                actions = {
                    // Sidebar toggle button (≡)
                    if (showSidebarToggle) {
                        IconButton(
                            onClick = { enhancedChatViewModel.toggleSidebar() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Toggle chat sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Main chat content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
            // Model selector chip - Gallery's exact pattern
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
            
            // RAG Enhancement Toggle
            RagToggleComponent(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Main content area - Gallery's exact pattern
            if (isModelReady && selectedModel != null) {
                // Show chat interface when model is ready - Gallery's exact pattern
                ChatPanel(
                    modifier = Modifier.weight(1f),
                    messages = messages,
                    onSendMessage = { messageText ->
                        if (messageText.isNotBlank() && !isLoading && selectedModel != null) {
                            isLoading = true
                            
                            // Add user message
                            val userMessage = ChatMessageText(
                                content = messageText,
                                side = ChatSide.USER
                            )
                            messages = (messages + userMessage).toMutableList()
                            
                            // Add loading message (Gallery pattern)
                            val loadingMessage = ChatMessageLoading()
                            messages = (messages + loadingMessage).toMutableList()
                            
                            // Use provided chatContext or create basic one for RAG enhancement
                            val contextToUse = chatContext ?: ChatContext.GeneralTutoring(
                                subject = "General",
                                grade = null,
                                previousTopics = messages.map { msg ->
                                    when (msg) {
                                        is ChatMessageText -> msg.content.take(50) // Extract topics from previous messages
                                        else -> ""
                                    }
                                }.filter { it.isNotBlank() }
                            )
                            
                            // Generate AI response with streaming and RAG enhancement
                            chatViewModel.generateResponse(
                                model = selectedModel,
                                input = messageText,
                                context = contextToUse,
                                resultListener = { partialResponse, isComplete ->
                                    if (currentStreamingMessage == null) {
                                        // First token - replace loading with streaming message (Gallery pattern)
                                        val streamingMessage = ChatMessageText(
                                            content = partialResponse, // First token
                                            side = ChatSide.AGENT
                                        )
                                        currentStreamingMessage = streamingMessage
                                        messages = (messages.dropLast(1) + streamingMessage).toMutableList()
                                    } else {
                                        // Append new token to existing content (Gallery pattern)
                                        val accumulatedContent = currentStreamingMessage!!.content + partialResponse
                                        val updatedMessage = ChatMessageText(
                                            content = accumulatedContent, // Accumulate tokens
                                            side = ChatSide.AGENT
                                        )
                                        currentStreamingMessage = updatedMessage
                                        messages = (messages.dropLast(1) + updatedMessage).toMutableList()
                                    }
                                    
                                    if (isComplete) {
                                        // Reset state
                                        currentStreamingMessage = null
                                        isLoading = false
                                    }
                                },
                                onError = {
                                    // Remove loading/streaming message and show error
                                    val errorMessage = ChatMessageText(
                                        content = "Sorry, I encountered an error. Please try again.",
                                        side = ChatSide.AGENT
                                    )
                                    messages = (messages.dropLast(1) + errorMessage).toMutableList()
                                    currentStreamingMessage = null
                                    isLoading = false
                                }
                            )
                        }
                    },
                    navigateUp = onNavigateUp,
                    isLoading = isLoading
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
            
            // Chat Sidebar
            ChatSidebar(
                sidebarState = sidebarState,
                onToggleSidebar = { enhancedChatViewModel.toggleSidebar() },
                onExpandCourse = { courseId -> 
                    enhancedChatViewModel.toggleCourseExpansion(courseId)
                },
                onExpandChapter = { chapterId -> 
                    enhancedChatViewModel.toggleChapterExpansion(chapterId)
                },
                onSelectChatSession = { session -> 
                    enhancedChatViewModel.selectChatSession(session)
                },
                onCreateNewChat = { chatType -> 
                    enhancedChatViewModel.createNewChatSession(chatType)
                }
            )
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

/**
 * Enhanced ChatView for chapter reading with full RAG context.
 */
@Composable
fun EnhancedChapterChatView(
    courseId: String,
    chapterId: String,
    readingProgress: Float = 0f,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ElizaChatViewModel = hiltViewModel(),
    enhancedViewModel: EnhancedChatViewModel = hiltViewModel()
) {
    val uiState by enhancedViewModel.uiState.collectAsState()
    
    // Load real course and chapter data from repository
    LaunchedEffect(courseId, chapterId, readingProgress) {
        enhancedViewModel.loadChapterContext(courseId, chapterId, readingProgress)
        // Initialize sidebar context for auto-expansion
        enhancedViewModel.initializeSidebarContext(courseId, chapterId)
    }
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading chapter",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        uiState.chatContext != null -> {
            EnhancedChatInterface(
                title = uiState.title,
                chatContext = uiState.chatContext,
                onNavigateUp = onNavigateUp,
                modifier = modifier,
                chatViewModel = chatViewModel
            )
        }
    }
}

/**
 * Enhanced ChatView for exercise help with comprehensive context.
 */
@Composable
fun EnhancedExerciseHelpChatView(
    courseId: String,
    chapterId: String,
    exerciseId: String,
    userAnswer: String? = null,
    isTestQuestion: Boolean = false,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ElizaChatViewModel = hiltViewModel(),
    enhancedViewModel: EnhancedChatViewModel = hiltViewModel()
) {
    val uiState by enhancedViewModel.uiState.collectAsState()
    
    // Load real exercise context data from repository
    LaunchedEffect(courseId, chapterId, exerciseId, userAnswer, isTestQuestion) {
        enhancedViewModel.loadExerciseContext(courseId, chapterId, exerciseId, userAnswer, isTestQuestion)
        // Initialize sidebar context for auto-expansion (exercise help context)
        enhancedViewModel.initializeSidebarContext(courseId, chapterId)
    }
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading exercise",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        uiState.chatContext != null -> {
            val initialMessages = uiState.initialMessage?.let { message ->
                listOf(ChatMessageInfo(content = message))
            } ?: emptyList()
            
            EnhancedChatInterface(
                title = uiState.title,
                chatContext = uiState.chatContext,
                initialMessages = initialMessages,
                onNavigateUp = onNavigateUp,
                modifier = modifier,
                chatViewModel = chatViewModel
            )
        }
    }
}

/**
 * Shared enhanced chat interface that integrates RAG context with Gallery chat UI.
 */
@Composable
private fun EnhancedChatInterface(
    title: String,
    chatContext: ChatContext?,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    initialMessages: List<ChatMessage> = emptyList(),
    chatViewModel: ElizaChatViewModel = hiltViewModel()
) {
    // Use the existing ChatView but with enhanced context awareness
    // The ChatViewModel will automatically use RAG when chatContext is provided
    ChatView(
        title = title,
        onNavigateUp = onNavigateUp,
        initialMessages = initialMessages,
        chatContext = chatContext,
        chatViewModel = chatViewModel,
        modifier = modifier
    )
    
    // TODO: In future iterations, this can be enhanced to:
    // 1. Display RAG toggle status
    // 2. Show context suggestions
    // 3. Provide context-aware input hints
    // 4. Display relevance scores for responses
} 