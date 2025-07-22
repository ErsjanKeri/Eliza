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

package com.example.ai.edge.eliza.feature.courseprogress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.edge.eliza.core.designsystem.component.ChapterPath
import com.example.ai.edge.eliza.core.designsystem.component.CourseHeader

/**
 * Course Progress Screen showing Duolingo-style chapter progression.
 * Displays chapters as connected nodes in a winding path layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseProgressScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onChapterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load course data when the screen is first shown
    LaunchedEffect(courseId) {
        viewModel.loadCourseProgress(courseId)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(uiState.course?.title ?: "Course Progress") 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading course",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                uiState.chapterNodes.isNotEmpty() -> {
                    // Success state - show the beautiful course header and Duolingo-style chapter path
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Course header
                        uiState.course?.let { course ->
                            CourseHeader(
                                title = course.title,
                                description = course.description,
                                subject = course.subject.displayName,
                                grade = course.grade,
                                estimatedHours = course.estimatedHours,
                                isDownloaded = course.isDownloaded,
                                completedChapters = uiState.chapterNodes.count { it.isCompleted },
                                totalChapters = uiState.chapterNodes.size,
                                isExpanded = uiState.isHeaderExpanded,
                                onToggleExpanded = {
                                    viewModel.toggleHeaderExpanded()
                                }
                            )
                        }
                        
                        // Chapter path
                        ChapterPath(
                            chapters = uiState.chapterNodes,
                            onChapterClick = onChapterClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                else -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No chapters available for this course",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
} 