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

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.ai.edge.eliza.core.designsystem.icon.ElizaIcons

/**
 * Eliza top app bar with educational styling.
 * Provides consistent navigation headers throughout the learning app.
 *
 * @param titleRes Resource ID for the title text.
 * @param navigationIcon Icon for the navigation action (usually back button).
 * @param navigationIconContentDescription Content description for the navigation icon.
 * @param actionIcon Icon for the primary action.
 * @param actionIconContentDescription Content description for the action icon.
 * @param modifier Modifier to be applied to the top app bar.
 * @param colors Colors to use for the top app bar.
 * @param onNavigationClick Called when the navigation icon is clicked.
 * @param onActionClick Called when the action icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElizaTopAppBar(
    @StringRes titleRes: Int,
    navigationIcon: ImageVector,
    navigationIconContentDescription: String,
    actionIcon: ImageVector,
    actionIconContentDescription: String,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = navigationIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = colors,
        modifier = modifier.testTag("ElizaTopAppBar"),
    )
}

/**
 * Eliza top app bar with string title for dynamic content.
 * Perfect for lesson titles, course names, etc.
 *
 * @param title The title text to display.
 * @param navigationIcon Icon for the navigation action.
 * @param navigationIconContentDescription Content description for the navigation icon.
 * @param actionIcon Icon for the primary action.
 * @param actionIconContentDescription Content description for the action icon.
 * @param modifier Modifier to be applied to the top app bar.
 * @param colors Colors to use for the top app bar.
 * @param onNavigationClick Called when the navigation icon is clicked.
 * @param onActionClick Called when the action icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElizaTopAppBar(
    title: String,
    navigationIcon: ImageVector,
    navigationIconContentDescription: String,
    actionIcon: ImageVector,
    actionIconContentDescription: String,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = navigationIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = colors,
        modifier = modifier.testTag("ElizaTopAppBar"),
    )
}

/**
 * Eliza top app bar with scroll behavior for long content.
 * Great for lesson content that scrolls.
 *
 * @param title The title text to display.
 * @param scrollBehavior Scroll behavior for the app bar.
 * @param modifier Modifier to be applied to the top app bar.
 * @param navigationIcon Optional navigation icon.
 * @param navigationIconContentDescription Content description for navigation icon.
 * @param actionIcon Optional action icon.
 * @param actionIconContentDescription Content description for action icon.
 * @param onNavigationClick Called when the navigation icon is clicked.
 * @param onActionClick Called when the action icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElizaTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
        },
        navigationIcon = {
            navigationIcon?.let { icon ->
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = icon,
                        contentDescription = navigationIconContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            actionIcon?.let { icon ->
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = icon,
                        contentDescription = actionIconContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier.testTag("ElizaTopAppBar"),
    )
}

/**
 * Simple Eliza top app bar with just a title.
 * Perfect for main screens like Home, Courses, etc.
 *
 * @param title The title text to display.
 * @param modifier Modifier to be applied to the top app bar.
 * @param colors Colors to use for the top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElizaSimpleTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
    ),
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        colors = colors,
        modifier = modifier.testTag("ElizaSimpleTopAppBar"),
    )
} 