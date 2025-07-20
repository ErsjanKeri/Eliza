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

package com.example.ai.edge.eliza.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.edge.eliza.core.designsystem.component.ElizaBackground
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme
import com.example.ai.edge.eliza.core.model.Course

/**
 * Home screen entry point following NowInAndroid patterns.
 * Collects state from the ViewModel and passes it to the stateless UI.
 */
@Composable
internal fun HomeScreen(
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onTabChange = viewModel::selectTab,
        onCourseClick = { courseId ->
            viewModel.onCourseClick(courseId)
            onCourseClick(courseId)
        },
        onStartNewCourse = { courseId ->
            viewModel.onStartNewCourse(courseId)
            onCourseClick(courseId)
        },
        modifier = modifier,
    )
}

/**
 * Stateless Home screen UI following NowInAndroid patterns.
 * Displays course feed with tab-based interface for continuing vs starting courses.
 */
@Composable
internal fun HomeScreen(
    uiState: HomeScreenUiState,
    onTabChange: (HomeTab) -> Unit,
    onCourseClick: (String) -> Unit,
    onStartNewCourse: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyStaggeredGridState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 24.dp,
            modifier = Modifier.testTag("home:feed"),
            state = state,
        ) {
            // Welcome header
            item(span = StaggeredGridItemSpan.FullLine, contentType = "header") {
                WelcomeHeader(uiState = uiState)
            }

            // Tab selector
            item(span = StaggeredGridItemSpan.FullLine, contentType = "tabs") {
                TabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabChange = onTabChange,
                )
            }

            // Course content based on selected tab
            when (uiState.selectedTab) {
                HomeTab.CONTINUE_LEARNING -> {
                    continueLearningContent(
                        courseFeedState = uiState.courseFeedState,
                        onCourseClick = onCourseClick,
                    )
                }
                HomeTab.START_NEW_COURSE -> {
                    startNewCourseContent(
                        courseFeedState = uiState.courseFeedState,
                        onStartNewCourse = onStartNewCourse,
                    )
                }
            }

            // Bottom spacing
            item(span = StaggeredGridItemSpan.FullLine, contentType = "bottomSpacing") {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Loading overlay following NowInAndroid patterns
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
            ) + fadeOut(),
        ) {
            LoadingIndicator()
        }
    }
}

/**
 * Welcome header showing user progress and greeting.
 */
@Composable
private fun WelcomeHeader(
    uiState: HomeScreenUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        
        when (val progressState = uiState.progressState) {
            is ProgressUiState.Success -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        val overview = progressState.overallProgress
                        if (overview.totalLessonsCompleted > 0) {
                            append("${overview.totalLessonsCompleted} lessons completed")
                            if (overview.formattedTimeSpent.isNotEmpty()) {
                                append(" • ${overview.formattedTimeSpent} studied")
                            }
                        } else {
                            append("Ready to start learning?")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            else -> {
                // Show placeholder or nothing during loading
            }
        }
    }
}

/**
 * Tab selector for switching between Continue Learning and Start New Course.
 */
@Composable
private fun TabSelector(
    selectedTab: HomeTab,
    onTabChange: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        HomeTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabChange(tab) },
                label = {
                    Text(
                        text = tab.displayName,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.testTag("home:tab:${tab.name}"),
            )
            
            if (tab != HomeTab.entries.last()) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

/**
 * Extension function for Continue Learning content in the staggered grid.
 */
private fun LazyStaggeredGridScope.continueLearningContent(
    courseFeedState: CourseFeedUiState,
    onCourseClick: (String) -> Unit,
) {
    when (courseFeedState) {
        is CourseFeedUiState.Loading -> {
            // Loading is handled by the overlay
        }
        is CourseFeedUiState.LoadFailed -> {
            item(span = StaggeredGridItemSpan.FullLine) {
                ErrorMessage(
                    message = "Failed to load courses",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        is CourseFeedUiState.Success -> {
            if (courseFeedState.continuingCourses.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(
                        message = "No courses in progress.\nStart a new course to begin learning!",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(
                    items = courseFeedState.continuingCourses,
                    key = { it.course.id },
                ) { courseWithProgress ->
                    ContinueCourseCard(
                        courseWithProgress = courseWithProgress,
                        onClick = { onCourseClick(courseWithProgress.course.id) },
                    )
                }
            }
        }
    }
}

/**
 * Extension function for Start New Course content in the staggered grid.
 */
private fun LazyStaggeredGridScope.startNewCourseContent(
    courseFeedState: CourseFeedUiState,
    onStartNewCourse: (String) -> Unit,
) {
    when (courseFeedState) {
        is CourseFeedUiState.Loading -> {
            // Loading is handled by the overlay
        }
        is CourseFeedUiState.LoadFailed -> {
            item(span = StaggeredGridItemSpan.FullLine) {
                ErrorMessage(
                    message = "Failed to load courses",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        is CourseFeedUiState.Success -> {
            if (courseFeedState.newCourses.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(
                        message = "All courses have been started!\nCheck the Continue Learning tab.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(
                    items = courseFeedState.newCourses,
                    key = { it.id },
                ) { course ->
                    NewCourseCard(
                        course = course,
                        onClick = { onStartNewCourse(course.id) },
                    )
                }
            }
        }
    }
}

/**
 * Placeholder loading indicator.
 * TODO: Replace with proper loading component from design system.
 */
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Loading courses...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Placeholder error message component.
 * TODO: Replace with proper error component from design system.
 */
@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Placeholder empty state component.
 * TODO: Replace with proper empty state component from design system.
 */
@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Placeholder course card for continuing courses.
 * TODO: Create proper course card components.
 */
@Composable
private fun ContinueCourseCard(
    courseWithProgress: CourseWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement with design system components
    Box(modifier = modifier.padding(8.dp)) {
        Text("Continue: ${courseWithProgress.course.title}")
    }
}

/**
 * Placeholder course card for new courses.
 * TODO: Create proper course card components.
 */
@Composable
private fun NewCourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement with design system components
    Box(modifier = modifier.padding(8.dp)) {
        Text("Start: ${course.title}")
    }
} 