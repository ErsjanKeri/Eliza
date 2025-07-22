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

package com.example.ai.edge.eliza.feature.chapter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.ai.edge.eliza.core.designsystem.icon.ElizaIcons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.feature.chapter.component.ElizaMarkdownRenderer

/**
 * Chapter Screen showing markdown content with optional split-screen chat.
 * Supports full-screen reading and split-screen chat functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(
    chapterId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load chapter data when the screen is first shown
    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ChapterTopAppBar(
                title = uiState.chapter?.title ?: "Chapter",
                onBackClick = onBackClick,
                onChatClick = { viewModel.toggleChatLayout() },
                isChatVisible = uiState.isChatVisible
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    LoadingState()
                }
                
                uiState.hasError -> {
                    // Error state
                    val error = (uiState.contentState as? ChapterContentUiState.LoadFailed)?.exception
                    ErrorState(
                        error = error?.message ?: "Unknown error",
                        onRetry = { viewModel.loadChapter(chapterId) }
                    )
                }
                
                uiState.hasContent -> {
                    // Success state - show content based on layout
                    when (uiState.layoutState) {
                        ChapterLayoutState.FullScreen -> {
                            FullScreenContent(
                                chapter = uiState.chapter!!,
                                onImageClick = viewModel::onImageClick
                            )
                        }
                        ChapterLayoutState.SplitScreen -> {
                            SplitScreenContent(
                                chapter = uiState.chapter!!,
                                onImageClick = viewModel::onImageClick,
                                onCloseChatClick = { viewModel.hideChat() }
                            )
                        }
                    }
                }
                
                else -> {
                    // Empty state
                    EmptyState()
                }
            }
        }
    }
}

/**
 * Top app bar for the chapter screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    isChatVisible: Boolean,
    modifier: Modifier = Modifier
) {
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
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        actions = {
            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = if (isChatVisible) Icons.Filled.Close else ElizaIcons.Chat,
                    contentDescription = if (isChatVisible) "Close chat" else "Open chat",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    )
}

/**
 * Full screen content layout.
 */
@Composable
private fun FullScreenContent(
    chapter: com.example.ai.edge.eliza.core.model.Chapter,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ElizaMarkdownRenderer(
            content = chapter.markdownContent,
            onImageClick = onImageClick,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Add some bottom padding for better scrolling experience
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Split screen content layout with chat.
 */
@Composable
private fun SplitScreenContent(
    chapter: com.example.ai.edge.eliza.core.model.Chapter,
    onImageClick: (String) -> Unit,
    onCloseChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Chapter content (left half)
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ElizaMarkdownRenderer(
                content = chapter.markdownContent,
                onImageClick = onImageClick,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Chat interface (right half)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for chat interface
            Text(
                text = "Chat Interface",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Ask questions about this chapter",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ElizaButton(
                onClick = onCloseChatClick,
                text = { Text("Close Chat") }
            )
        }
    }
}

/**
 * Loading state display.
 */
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading chapter...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state display.
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error loading chapter",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ElizaButton(
                onClick = onRetry,
                text = { Text("Retry") }
            )
        }
    }
}

/**
 * Empty state display.
 */
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No chapter content available",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 