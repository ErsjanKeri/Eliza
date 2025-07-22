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

package com.example.ai.edge.eliza.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule

/**
 * Collapsible course header component that displays course information and progress.
 * REDESIGNED: Shows only progress bar when collapsed, course details when expanded.
 */
@Composable
fun CourseHeader(
    title: String,
    description: String,
    subject: String,
    grade: String,
    estimatedHours: Int,
    isDownloaded: Boolean,
    completedChapters: Int,
    totalChapters: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressPercentage = if (totalChapters > 0) {
        completedChapters.toFloat() / totalChapters.toFloat()
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpanded() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Always visible: Progress bar with expand button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress bar section
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = progressPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${(progressPercentage * 100).toInt()}% completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Expand/collapse button
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Show less" else "Show more",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Expandable content - Course Card Information
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Course title and details
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subject and grade badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$subject • $grade",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Course stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CourseStatItem(
                        icon = Icons.Outlined.Schedule,
                        text = "${estimatedHours}h",
                        label = "Est. Time"
                    )
                    
                    CourseStatItem(
                        icon = Icons.Filled.School,
                        text = "$totalChapters",
                        label = "Chapters"
                    )
                    
                    CourseStatItem(
                        icon = if (isDownloaded) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                        text = if (isDownloaded) "Downloaded" else "Online",
                        label = if (isDownloaded) "Available offline" else "Requires internet"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Course description
                Text(
                    text = "About this course",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun CourseStatItem(
    icon: ImageVector,
    text: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseHeaderPreview() {
    ElizaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Collapsed state (default)
            CourseHeader(
                title = "Algebra I Fundamentals",
                description = "Master the fundamentals of algebra including linear equations, quadratic functions, and polynomial operations. Perfect for high school students or anyone looking to strengthen their mathematical foundation.",
                subject = "Algebra",
                grade = "9th Grade",
                estimatedHours = 20,
                isDownloaded = true,
                completedChapters = 2,
                totalChapters = 3,
                isExpanded = false,
                onToggleExpanded = {}
            )
            
            // Expanded state
            CourseHeader(
                title = "Geometry: Shapes & Proofs",
                description = "Explore the world of geometric shapes, theorems, and proofs. Learn about angles, triangles, circles, and spatial reasoning through interactive lessons and practical applications.",
                subject = "Geometry",
                grade = "10th Grade",
                estimatedHours = 15,
                isDownloaded = false,
                completedChapters = 5,
                totalChapters = 10,
                isExpanded = true,
                onToggleExpanded = {}
            )
        }
    }
} 