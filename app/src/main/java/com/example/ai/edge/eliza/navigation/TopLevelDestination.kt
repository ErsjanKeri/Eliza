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

package com.example.ai.edge.eliza.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ai.edge.eliza.R
import com.example.ai.edge.eliza.core.designsystem.icon.ElizaIcons
import com.example.ai.edge.eliza.feature.home.navigation.HOME_BASE_ROUTE
import com.example.ai.edge.eliza.feature.home.navigation.HOME_ROUTE
import com.example.ai.edge.eliza.feature.chat.navigation.COURSE_SUGGESTIONS_CHAT_ROUTE
import com.example.ai.edge.eliza.feature.settings.navigation.SETTINGS_ROUTE

/**
 * Type for the top level destinations in the Eliza application. 
 * Contains metadata about the destination that is used in the bottom navigation and top app bar.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is not selected.
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 * @param route The route to use when navigating to this destination.
 * @param baseRoute The highest ancestor of this destination. Defaults to [route], meaning that
 * there is a single destination in that section of the app (no nested destinations).
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: String,
    val baseRoute: String = route,
) {
    HOME(
        selectedIcon = ElizaIcons.Home,
        unselectedIcon = ElizaIcons.HomeBorder,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
        route = HOME_ROUTE,
        baseRoute = HOME_BASE_ROUTE,
    ),
    COURSE_SUGGESTIONS(
        selectedIcon = ElizaIcons.Chat,
        unselectedIcon = ElizaIcons.ChatBorder,
        iconTextId = R.string.course_suggestions,
        titleTextId = R.string.course_suggestions,
        route = COURSE_SUGGESTIONS_CHAT_ROUTE,
    ),
    SETTINGS(
        selectedIcon = ElizaIcons.Settings,
        unselectedIcon = ElizaIcons.SettingsBorder,
        iconTextId = R.string.settings,
        titleTextId = R.string.settings,
        route = SETTINGS_ROUTE,
    ),
} 