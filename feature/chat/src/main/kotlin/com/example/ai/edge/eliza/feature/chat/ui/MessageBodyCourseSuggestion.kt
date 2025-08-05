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

package com.example.ai.edge.eliza.feature.chat.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.component.ElizaMarkdownRenderer
import com.example.ai.edge.eliza.core.designsystem.icon.ElizaIcons
import com.example.ai.edge.eliza.feature.chat.model.CourseNavigationAction
import com.example.ai.edge.eliza.feature.chat.model.CourseNavigationType

/**
 * Composable function to display AI course suggestions with navigation buttons.
 * Renders the AI text response plus interactive course/chapter navigation actions.
 */
@Composable
fun MessageBodyCourseSuggestion(
    message: ChatMessageCourseSuggestion,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToChapter: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Render the AI's text response using markdown
        ElizaMarkdownRenderer(
            content = message.content.trimEnd(),
            smallFontSize = false,
            useDefaultPadding = false, // We handle padding ourselves
            modifier = Modifier.fillMaxWidth()
        )
        
        // 2. Render course navigation actions if available
        if (message.navigationData.suggestions.isNotEmpty()) {
            CourseNavigationSection(
                navigationData = message.navigationData,
                onNavigateToCourse = onNavigateToCourse,
                onNavigateToChapter = onNavigateToChapter
            )
        }
    }
}

@Composable
private fun CourseNavigationSection(
    navigationData: com.example.ai.edge.eliza.feature.chat.model.CourseNavigationData,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToChapter: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape, // Square corners instead of rounded
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Clean, no alpha
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) // Clean border
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = ElizaIcons.Play,
                    contentDescription = "Course Suggestions",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Recommended Courses",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Navigation actions
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp), // Limit height to prevent huge cards
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(navigationData.suggestions) { action ->
                    CourseNavigationButton(
                        action = action,
                        onNavigateToCourse = onNavigateToCourse,
                        onNavigateToChapter = onNavigateToChapter
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseNavigationButton(
    action: CourseNavigationAction,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToChapter: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (buttonText, buttonIcon, onClick) = when (action.type) {
        CourseNavigationType.COURSE_OVERVIEW -> Triple(
            action.displayText,
            ElizaIcons.Play,
            { onNavigateToCourse(action.courseId) }
        )
        CourseNavigationType.CHAPTER_DIRECT -> Triple(
            action.displayText,
            ElizaIcons.Play,
            { 
                action.chapterId?.let { chapterId ->
                    onNavigateToChapter(action.courseId, chapterId)
                } ?: Unit
            }
        )
        CourseNavigationType.START_COURSE -> Triple(
            action.displayText,
            ElizaIcons.Play,
            { onNavigateToCourse(action.courseId) }
        )
    }
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape, // Square corners
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp), // No shadow
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                action.relevanceExplanation?.let { explanation ->
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Icon(
                imageVector = buttonIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}