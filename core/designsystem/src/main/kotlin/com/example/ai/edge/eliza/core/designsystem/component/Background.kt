/*
 * Copyright 2024 The Eliza Project
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.theme.GradientColors
import com.example.ai.edge.eliza.core.designsystem.theme.LocalBackgroundTheme
import com.example.ai.edge.eliza.core.designsystem.theme.LocalGradientColors
import kotlin.math.tan

/**
 * The main background for the Eliza educational app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Surface].
 * Provides a consistent foundation for all learning interfaces.
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The background content.
 */
@Composable
fun ElizaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}

/**
 * A gradient background for special educational screens and content areas.
 * Uses [LocalGradientColors] to set the gradient colors of a [Box] within a [Surface].
 * Perfect for lesson introductions, achievement screens, and special learning content.
 *
 * @param modifier Modifier to be applied to the background.
 * @param gradientColors The gradient colors to be rendered.
 * @param content The background content.
 */
@Composable
fun ElizaGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
    
    Surface(
        color = if (gradientColors.container == Color.Unspecified) {
            Color.Transparent
        } else {
            gradientColors.container
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    // Create a subtle diagonal gradient for visual interest
                    val gradientWidth = size.width * 0.5f
                    val gradientHeight = size.height * 0.3f
                    
                    onDrawBehind {
                        if (currentTopColor != Color.Unspecified && 
                            currentBottomColor != Color.Unspecified) {
                            
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        currentTopColor,
                                        currentBottomColor,
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(gradientWidth, gradientHeight),
                                ),
                                size = size,
                            )
                        }
                    }
                },
        ) {
            content()
        }
    }
}

/**
 * A card-style background for educational content sections.
 * Perfect for lesson cards, course cards, and content containers.
 *
 * @param modifier Modifier to be applied to the card background.
 * @param tonalElevation The tonal elevation for the card surface.
 * @param content The card content.
 */
@Composable
fun ElizaCardBackground(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        tonalElevation = tonalElevation,
        modifier = modifier,
        content = content,
    )
}

/**
 * A lesson content background optimized for reading and learning.
 * Provides the ideal surface for educational text and media content.
 *
 * @param modifier Modifier to be applied to the lesson background.
 * @param content The lesson content.
 */
@Composable
fun ElizaLessonBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        color = Color.White, // Always white for optimal reading
        tonalElevation = 1.dp,
        modifier = modifier,
        content = content,
    )
} 