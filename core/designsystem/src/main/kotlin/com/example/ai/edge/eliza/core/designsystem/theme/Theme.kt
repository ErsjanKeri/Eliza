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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Eliza's educational color scheme - Light blue, blue, and white theme
 * Designed for clarity, friendliness, and optimal learning experience
 */
private val ElizaColorScheme = lightColorScheme(
    // Primary colors - Core blue family
    primary = Blue50,                    // Main interactive elements
    onPrimary = White,                   // Text/icons on primary
    primaryContainer = Blue90,           // Primary containers and backgrounds
    onPrimaryContainer = Blue10,         // Text/icons on primary containers
    
    // Secondary colors - Light blue accent
    secondary = LightBlue50,             // Secondary actions and accents
    onSecondary = White,                 // Text/icons on secondary
    secondaryContainer = LightBlue90,    // Secondary containers
    onSecondaryContainer = LightBlue10,  // Text/icons on secondary containers
    
    // Tertiary colors - Additional blue variation
    tertiary = Blue60,                   // Additional interactive elements
    onTertiary = White,                  // Text/icons on tertiary
    tertiaryContainer = Blue95,          // Tertiary containers
    onTertiaryContainer = Blue20,        // Text/icons on tertiary containers
    
    // Error colors - For incorrect answers and alerts
    error = Red40,                       // Error states
    onError = White,                     // Text/icons on error
    errorContainer = Red90,              // Error backgrounds
    onErrorContainer = Red40,            // Text/icons on error containers
    
    // Background colors - Main app surfaces
    background = Blue99,                 // Main app background
    onBackground = BlueGray10,           // Text/icons on background
    surface = White,                     // Card and sheet surfaces
    onSurface = BlueGray10,              // Text/icons on surfaces
    
    // Surface variations
    surfaceVariant = Blue95,             // Alternate surface color
    onSurfaceVariant = BlueGray30,       // Text/icons on surface variants
    inverseSurface = BlueGray20,         // High contrast surfaces
    inverseOnSurface = BlueGray95,       // Text/icons on inverse surfaces
    
    // Outline colors - Borders and dividers
    outline = BlueGray50,                // Standard borders
    outlineVariant = BlueGray80,         // Subtle borders
    
    // Additional colors
    surfaceTint = Blue50,                // Surface elevation tinting
    inversePrimary = Blue80,             // Inverse primary color
    scrim = BlueGray10.copy(alpha = 0.32f), // Modal overlays
)

/**
 * Eliza gradient colors for visual interest and depth
 */
private val ElizaGradientColors = GradientColors(
    top = Blue95,                        // Soft blue gradient top
    bottom = LightBlue95,                // Light blue gradient bottom
    container = Blue99,                  // Container background
)

/**
 * Eliza background theme for consistent surface treatment
 */
private val ElizaBackgroundTheme = BackgroundTheme(
    color = Blue99,                      // Main background color
    tonalElevation = 0.dp,              // Base elevation
)

/**
 * Eliza tint theme for icons and accents
 */
private val ElizaTintTheme = TintTheme(
    iconTint = Blue50,                   // Default icon tinting
)

/**
 * Eliza theme - The main theme composable for the educational app.
 * 
 * Features:
 * - Light blue, blue, and white color palette
 * - Professional typography optimized for learning
 * - Consistent elevation and surface treatment
 * - No dark mode - single beautiful theme
 *
 * @param content The content to apply the theme to.
 */
@Composable
fun ElizaTheme(
    content: @Composable () -> Unit,
) {
    // Color scheme with elevation-based surface colors
    val colorScheme = ElizaColorScheme
    
    // Gradient colors for special backgrounds
    val gradientColors = ElizaGradientColors
    
    // Background theme for consistent surfaces
    val backgroundTheme = ElizaBackgroundTheme
    
    // Tint theme for icons and accents
    val tintTheme = ElizaTintTheme
    
    // Test colors for educational interfaces
    val testColors = ElizaTestColors
    
    // Provide all theme values through composition locals
    CompositionLocalProvider(
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides tintTheme,
        LocalTestColors provides testColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ElizaTypography,
            content = content,
        )
    }
} 