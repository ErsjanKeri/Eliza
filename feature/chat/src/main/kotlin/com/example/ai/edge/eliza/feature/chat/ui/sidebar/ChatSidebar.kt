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

package com.example.ai.edge.eliza.feature.chat.ui.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.model.ChatSession
import com.example.ai.edge.eliza.core.model.ChatType

/**
 * Toggleable chat sidebar that displays hierarchical chat organization.
 * Shows Course > Chapter > Chat Types structure with smooth animations.
 */
@Composable
fun ChatSidebar(
    sidebarState: SidebarUiState,
    onToggleSidebar: () -> Unit,
    onExpandCourse: (String) -> Unit,
    onExpandChapter: (String) -> Unit,
    onSelectChatSession: (ChatSession) -> Unit,
    onCreateNewChat: (ChatType) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = sidebarState.isVisible,
        enter = slideInHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetX = { fullWidth -> fullWidth }
        ),
        exit = slideOutHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            targetOffsetX = { fullWidth -> fullWidth }
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                ),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Sidebar Header
                SidebarHeader(
                    currentChapterId = sidebarState.currentChapterId,
                    onCreateNewChat = { onCreateNewChat(ChatType.GENERAL_CHAPTER) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                if (sidebarState.isLoading) {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (sidebarState.error != null) {
                    // Error state
                    ErrorMessage(
                        error = sidebarState.error,
                        onRetry = { /* TODO: Implement retry */ }
                    )
                } else {
                    // Chat organization content
                    ChatHierarchyContent(
                        sidebarState = sidebarState,
                        onExpandCourse = onExpandCourse,
                        onExpandChapter = onExpandChapter,
                        onSelectChatSession = onSelectChatSession,
                        onCreateNewChat = onCreateNewChat
                    )
                }
            }
        }
    }
}

/**
 * Header section of the sidebar with title and new chat button.
 */
@Composable
private fun SidebarHeader(
    currentChapterId: String?,
    onCreateNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "💬 Chat Sessions",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onCreateNewChat,
            modifier = Modifier.width(240.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("New Chat")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

/**
 * Main content area showing the hierarchical chat organization.
 */
@Composable
private fun ChatHierarchyContent(
    sidebarState: SidebarUiState,
    onExpandCourse: (String) -> Unit,
    onExpandChapter: (String) -> Unit,
    onSelectChatSession: (ChatSession) -> Unit,
    onCreateNewChat: (ChatType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        // For demo, we'll show the current course/chapter structure
        // TODO: In full implementation, this would iterate through all courses
        
        if (sidebarState.currentCourseId != null && sidebarState.currentChapterId != null) {
            item {
                // Course level (auto-expanded for current course)
                CourseItem(
                    courseId = sidebarState.currentCourseId,
                    courseName = "📚 Algebra Basics", // TODO: Get from repository
                    isExpanded = true, // Auto-expanded for current course
                    onToggleExpanded = { onExpandCourse(sidebarState.currentCourseId) }
                )
            }
            
            item {
                // Chapter level (auto-expanded for current chapter)
                ChapterItem(
                    chapterId = sidebarState.currentChapterId,
                    chapterName = "📖 Linear Equations", // TODO: Get from repository
                    isExpanded = true, // Auto-expanded for current chapter
                    onToggleExpanded = { onExpandChapter(sidebarState.currentChapterId) },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Chat type sections
            item {
                ChatTypesList(
                    sidebarState = sidebarState,
                    onSelectChatSession = onSelectChatSession,
                    onCreateNewChat = onCreateNewChat,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        } else {
            item {
                // No chapter context - show message
                Text(
                    text = "🔍 Open a chapter to view related chat sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Course-level item in the hierarchy.
 */
@Composable
private fun CourseItem(
    courseId: String,
    courseName: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onToggleExpanded,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand"
        )
        Text(
            text = courseName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Chapter-level item in the hierarchy.
 */
@Composable
private fun ChapterItem(
    chapterId: String,
    chapterName: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onToggleExpanded,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand"
        )
        Text(
            text = chapterName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * List of chat types and their sessions.
 */
@Composable
private fun ChatTypesList(
    sidebarState: SidebarUiState,
    onSelectChatSession: (ChatSession) -> Unit,
    onCreateNewChat: (ChatType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // General Chapter Discussion
        ChatTypeSection(
            title = "💬 General Chapter Discussion",
            chatType = ChatType.GENERAL_CHAPTER,
            sessions = sidebarState.chatSessionsByType[ChatType.GENERAL_CHAPTER] ?: emptyList(),
            activeSessionId = sidebarState.activeSessionId,
            onSelectSession = onSelectChatSession,
            onCreateNewChat = { onCreateNewChat(ChatType.GENERAL_CHAPTER) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Exercise Help
        ChatTypeSection(
            title = "❓ Exercise Help",
            chatType = ChatType.EXERCISE_HELP,
            sessions = sidebarState.chatSessionsByType[ChatType.EXERCISE_HELP] ?: emptyList(),
            activeSessionId = sidebarState.activeSessionId,
            onSelectSession = onSelectChatSession,
            onCreateNewChat = { onCreateNewChat(ChatType.EXERCISE_HELP) },
            showSessionNumbers = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Text Questions
        ChatTypeSection(
            title = "📝 Text Questions",
            chatType = ChatType.TEXT_SELECTION,
            sessions = sidebarState.chatSessionsByType[ChatType.TEXT_SELECTION] ?: emptyList(),
            activeSessionId = sidebarState.activeSessionId,
            onSelectSession = onSelectChatSession,
            onCreateNewChat = { onCreateNewChat(ChatType.TEXT_SELECTION) }
        )
    }
}

/**
 * Error message display.
 */
@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "❌ Error loading chats",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}