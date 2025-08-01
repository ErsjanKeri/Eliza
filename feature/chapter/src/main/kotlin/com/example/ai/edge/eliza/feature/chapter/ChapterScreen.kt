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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
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
 * Chapter screen that displays markdown content and provides test functionality.
 * Now with full-screen chat navigation instead of split view.
 */
@Composable
fun ChapterScreen(
    chapterId: String,
    onBackClick: () -> Unit,
    onNavigateToTest: () -> Unit,
    onNavigateToResults: () -> Unit = onNavigateToTest, // Default to same as test for backward compatibility
    onRetakeTest: () -> Unit = onNavigateToTest, // Default to same as test for backward compatibility  
    onNavigateToChat: (String) -> Unit, // Navigate to full-screen chat with chapter title
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
                onChatClick = { 
                    // Navigate to full-screen chat instead of split view
                    val chapterTitle = uiState.chapter?.title ?: "Chapter"
                    onNavigateToChat(chapterTitle)
                }
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
                    // Success state - show full-screen content only (no more split view)
                    FullScreenContent(
                        chapter = uiState.chapter!!,
                        onImageClick = viewModel::onImageClick,
                        onTakeTestClick = { action ->
                            when (action) {
                                TestButtonAction.TAKE_TEST -> onNavigateToTest()
                                TestButtonAction.SHOW_RESULTS -> onNavigateToResults()
                                TestButtonAction.RETAKE_TEST -> onRetakeTest()
                            }
                        }
                    )
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
                    imageVector = ElizaIcons.Chat,
                    contentDescription = "Open chat",
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
    onTakeTestClick: (TestButtonAction) -> Unit,
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
        
        // Test button - shown at the bottom of chapter content
        ChapterTestButton(
            chapter = chapter,
            onTakeTestClick = onTakeTestClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
        
        // Add some bottom padding for better scrolling experience
        Spacer(modifier = Modifier.height(32.dp))
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

/**
 * Test button component that shows different states based on chapter completion.
 */
enum class TestButtonAction {
    TAKE_TEST,
    SHOW_RESULTS, 
    RETAKE_TEST
}

@Composable
private fun ChapterTestButton(
    chapter: com.example.ai.edge.eliza.core.model.Chapter,
    onTakeTestClick: (TestButtonAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Enhanced logic: Check for test attempts using database state
    val hasTestAttempts = chapter.testAttempts > 0
    
    val buttonText = when {
        chapter.isCompleted -> "Retake Test (Score: ${chapter.testScore ?: 0}%)"
        hasTestAttempts -> "Show Results (Score: ${chapter.testScore ?: 0}%)"
        else -> "Take Test (${chapter.exercises.size} Questions)"
    }
    
    val buttonIcon = when {
        chapter.isCompleted -> Icons.Default.Refresh // Retake icon
        hasTestAttempts -> Icons.Default.Info // Show results icon
        else -> Icons.Default.Edit // Take test icon
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Completion status indicator
        if (chapter.isCompleted) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Chapter Complete",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Chapter Complete",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Main test button
        ElizaButton(
            onClick = { 
                val action = when {
                    chapter.isCompleted -> TestButtonAction.RETAKE_TEST
                    hasTestAttempts -> TestButtonAction.SHOW_RESULTS
                    else -> TestButtonAction.TAKE_TEST
                }
                onTakeTestClick(action)
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = when {
                    chapter.isCompleted -> "Retake Test"
                    hasTestAttempts -> "Show Results"
                    else -> "Take Test"
                },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Column {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = when {
                                chapter.isCompleted -> "Try again for 100% completion"
                                hasTestAttempts -> "View your test results"
                                else -> "Test your understanding!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Additional info for chapters with test attempts
        if (hasTestAttempts) {
            Text(
                text = "Attempts: ${chapter.testAttempts}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 