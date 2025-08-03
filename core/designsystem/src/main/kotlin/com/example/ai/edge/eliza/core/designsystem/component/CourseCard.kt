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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme

/**
 * Beautiful CourseCard component with modern design, circular progress, and expandable details.
 * Follows NowInAndroid design patterns with Eliza's educational theme.
 */
@Composable
fun CourseCard(
    title: String,
    description: String,
    subject: String,
    grade: String,
    totalChapters: Int,
    completedChapters: Int,
    estimatedHours: Int,
    progressPercentage: Float,
    isDownloaded: Boolean,
    isStarted: Boolean,
    timeSpent: String = "",
    onCardClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(0.dp), // Square corners
        colors = CardDefaults.cardColors(
            // make the color slight gray
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp, // SAME elevation for all cards
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // UPDATED: More compact padding (was 20.dp)
        ) {
            // Header Row with Progress and Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Course info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Subject badge
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(0.dp), // Square design consistency
                        modifier = Modifier.padding(bottom = 4.dp) // UPDATED: Reduced spacing (was 8.dp)
                    ) {
                        Text(
                            text = "$subject • $grade",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Course title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize * 0.8f // Example: 90% of original
                            // Or set a specific size like: fontSize = 22.sp (if headlineSmall is, for example, 24.sp)
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp)) // UPDATED: Reduced spacing (was 4.dp)

                    // Short description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // UPDATED: Reduced spacing (was 16.dp)

                // Right side: Progress indicator
                ProgressCircle(
                    progress = animatedProgress,
                    completedChapters = completedChapters,
                    totalChapters = totalChapters,
                    isStarted = isStarted
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // UPDATED: Reduced spacing (was 16.dp)

            // Course stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // UPDATED: Reduced spacing (was 16.dp)
                ) {
                    CourseStatItem(
                        icon = Icons.Outlined.Schedule,
                        text = "${estimatedHours}h",
                        label = "Est. Time"
                    )

                    if (isStarted && timeSpent.isNotEmpty()) {
                        CourseStatItem(
                            icon = Icons.Outlined.CheckCircle,
                            text = timeSpent,
                            label = "Studied"
                        )
                    }

                    CourseStatItem(
                        icon = Icons.Filled.School,
                        text = "$totalChapters",
                        label = "Chapters"
                    )
                }

                // IDENTICAL action button layout for ALL cards
                UniformActionButton(
                    isStarted = isStarted,
                    onPrimaryAction = if (isStarted) onContinueClick else onCardClick,
                    onExpandClick = { isExpanded = !isExpanded },
                    isExpanded = isExpanded
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp) // UPDATED: Reduced spacing (was 16.dp)
                ) {
                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.height(12.dp)) // UPDATED: Reduced spacing (was 16.dp)

                    // Additional course details
                    Text(
                        text = "Course Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp)) // UPDATED: Reduced spacing (was 8.dp)

                    if (isStarted) {
                        Text(
                            text = "Progress: $completedChapters of $totalChapters chapters completed (${(progressPercentage).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp)) // UPDATED: Reduced spacing (was 4.dp)
                    }

                    Text(
                        text = "📚 Grade Level: $grade",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(3.dp)) // UPDATED: Reduced spacing (was 4.dp)

                    Text(
                        text = if (isDownloaded) "💾 Downloaded • Available offline" else "🌐 Online content • Download to access offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCircle(
    progress: Float,
    completedChapters: Int,
    totalChapters: Int,
    isStarted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(56.dp), // UPDATED: Smaller size (was 64.dp)
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(56.dp), // UPDATED: Smaller size (was 64.dp)
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeWidth = 5.dp, // UPDATED: Thinner stroke (was 6.dp)
            strokeCap = StrokeCap.Round,
        )

        // Progress circle
        if (isStarted) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(56.dp), // UPDATED: Smaller size (was 64.dp)
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp, // UPDATED: Thinner stroke (was 6.dp)
                strokeCap = StrokeCap.Round,
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isStarted) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start course",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp) // UPDATED: Smaller icon (was 24.dp)
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

@Composable
private fun UniformActionButton(
    isStarted: Boolean,
    onPrimaryAction: () -> Unit,
    onExpandClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(0.dp), // UPDATED: Square corners using 0dp radius
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Continue button section
            Row(
                modifier = Modifier
                    .clickable { onPrimaryAction() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isStarted) "Continue" else "Start",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Vertical separator line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
            )

            // Expand button section
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Show less" else "Show more",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseCardPreview() {
    ElizaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Continuing course
            CourseCard(
                title = "Introduction to Calculus",
                description = "Explore the fundamentals of differential and integral calculus with practical applications.",
                subject = "Calculus",
                grade = "11th Grade",
                totalChapters = 5,
                completedChapters = 3,
                estimatedHours = 30,
                progressPercentage = 60f,
                isDownloaded = true,
                isStarted = true,
                timeSpent = "4h",
                onCardClick = {},
                onContinueClick = {}
            )

            // New course
            CourseCard(
                title = "Trigonometry Essentials",
                description = "Master sine, cosine, tangent and their applications in solving triangles and modeling periodic phenomena.",
                subject = "Trigonometry",
                grade = "10th Grade",
                totalChapters = 4,
                completedChapters = 0,
                estimatedHours = 18,
                progressPercentage = 0f,
                isDownloaded = false,
                isStarted = false,
                onCardClick = {},
                onContinueClick = {}
            )
        }
    }
} 