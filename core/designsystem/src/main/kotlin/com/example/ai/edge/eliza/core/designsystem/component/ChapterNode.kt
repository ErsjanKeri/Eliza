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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme

/**
 * Duolingo-style chapter node component.
 * 
 * - Completed chapters: Blue background with white checkmark
 * - Available chapters: White background with blue circle outline
 */
@Composable
fun ChapterNode(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Main node circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary // Blue background for completed
                    } else {
                        Color.White // White background for available
                    }
                )
                .border(
                    width = if (isCompleted) 0.dp else 3.dp,
                    color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                // White checkmark for completed chapters
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Blue circle dot for available chapters
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        // Chapter title
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.size(width = 80.dp, height = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterNodePreview() {
    ElizaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Completed chapter
            ChapterNode(
                title = "Linear Equations",
                isCompleted = true,
                onClick = {}
            )

            // Available chapter
            ChapterNode(
                title = "Quadratic Functions",
                isCompleted = false,
                onClick = {}
            )
        }
    }
} 