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

package com.example.ai.edge.eliza.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A class to model test option colors for Eliza's educational test interface.
 * Provides up to 20 distinct, accessible colors for test alternatives.
 */
@Immutable
data class TestColors(
    val options: List<Color> = emptyList()
) {
    /**
     * Get a color for a specific option index.
     * Safely handles indices beyond available colors.
     */
    fun getColorForIndex(index: Int): Color {
        return if (index < options.size) {
            options[index]
        } else {
            // Fallback to cycling through available colors
            options.getOrElse(index % options.size) { Color.Gray }
        }
    }
    
    /**
     * Get colors for a specific number of options.
     */
    fun getColorsForCount(count: Int): List<Color> {
        return (0 until count).map { getColorForIndex(it) }
    }
}

/**
 * Default test colors using the educational color palette.
 */
internal val ElizaTestColors = TestColors(
    options = listOf(
        TestOption01, // Red (Classic Kahoot)
        TestOption02, // Blue (Classic Kahoot)
        TestOption03, // Orange (Classic Kahoot)
        TestOption04, // Green (Classic Kahoot)
        TestOption05, // Purple
        TestOption06, // Pink
        TestOption07, // Cyan
        TestOption08, // Brown
        TestOption09, // Blue Gray
        TestOption10, // Deep Purple
        TestOption11, // Indigo
        TestOption12, // Teal
        TestOption13, // Light Green
        TestOption14, // Lime
        TestOption15, // Amber
        TestOption16, // Deep Orange
        TestOption17, // Red Orange
        TestOption18, // Gray
        TestOption19, // Deep Purple Alt
        TestOption20  // Blue Alt
    )
)

/**
 * A composition local for [TestColors].
 * Allows consistent test option coloring throughout the test interface.
 */
val LocalTestColors = staticCompositionLocalOf { ElizaTestColors } 