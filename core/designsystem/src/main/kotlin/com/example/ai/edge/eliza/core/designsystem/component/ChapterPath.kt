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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme

/**
 * Data class representing a chapter in the learning path
 */
data class ChapterNodeData(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

/**
 * Duolingo-style winding chapter path layout.
 * Displays chapters in a serpentine pattern with connecting lines.
 */
@Composable
fun ChapterPath(
    chapters: List<ChapterNodeData>,
    onChapterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pathColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    
    // Calculate total height needed
    val totalHeight = if (chapters.isNotEmpty()) {
        (chapters.size * 120).dp // 120dp spacing between nodes
    } else {
        200.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        // Background connecting lines
        if (chapters.size > 1) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
            ) {
                val nodeSpacing = 120.dp.toPx()
                val centerX = size.width / 2f
                val leftX = size.width * 0.25f
                val rightX = size.width * 0.75f
                
                // Draw connecting lines between nodes
                for (i in 0 until chapters.size - 1) {
                    val currentY = i * nodeSpacing + 32.dp.toPx() // Offset for node center
                    val nextY = (i + 1) * nodeSpacing + 32.dp.toPx()
                    
                    val currentX = when (i % 3) {
                        0 -> centerX  // Center
                        1 -> rightX   // Right
                        else -> leftX // Left
                    }
                    
                    val nextX = when ((i + 1) % 3) {
                        0 -> centerX  // Center
                        1 -> rightX   // Right
                        else -> leftX // Left
                    }
                    
                    // Draw curved line between nodes
                    drawLine(
                        color = pathColor,
                        start = Offset(currentX, currentY),
                        end = Offset(nextX, nextY),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                }
            }
        }

        // Chapter nodes positioned in winding pattern
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            chapters.forEachIndexed { index, chapter ->
                val nodeAlignment = when (index % 3) {
                    0 -> Alignment.CenterHorizontally  // Center
                    1 -> Alignment.End                 // Right
                    else -> Alignment.Start            // Left
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // Fixed spacing between nodes
                    contentAlignment = Alignment.TopCenter
                ) {
                    ChapterNode(
                        title = chapter.title,
                        isCompleted = chapter.isCompleted,
                        onClick = { onChapterClick(chapter.id) },
                        modifier = Modifier.align(
                            when (nodeAlignment) {
                                Alignment.Start -> Alignment.CenterStart
                                Alignment.End -> Alignment.CenterEnd
                                else -> Alignment.Center
                            }
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterPathPreview() {
    ElizaTheme {
        val sampleChapters = listOf(
            ChapterNodeData("1", "Linear Equations", true),
            ChapterNodeData("2", "Quadratic Functions", true),
            ChapterNodeData("3", "Polynomials", false),
            ChapterNodeData("4", "Factoring", false),
            ChapterNodeData("5", "Complex Numbers", false),
            ChapterNodeData("6", "Systems of Equations", false)
        )
        
        ChapterPath(
            chapters = sampleChapters,
            onChapterClick = { chapterId ->
                println("Chapter clicked: $chapterId")
            }
        )
    }
} 