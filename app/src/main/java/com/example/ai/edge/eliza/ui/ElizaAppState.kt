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

package com.example.ai.edge.eliza.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.ai.edge.eliza.core.data.util.NetworkMonitor
import com.example.ai.edge.eliza.feature.home.navigation.navigateToHome
import com.example.ai.edge.eliza.feature.chat.navigation.navigateToCourseSuggestionsChat
import com.example.ai.edge.eliza.feature.settings.navigation.navigateToSettings
import com.example.ai.edge.eliza.navigation.TopLevelDestination
import com.example.ai.edge.eliza.navigation.TopLevelDestination.HOME
import com.example.ai.edge.eliza.navigation.TopLevelDestination.COURSE_SUGGESTIONS
import com.example.ai.edge.eliza.navigation.TopLevelDestination.SETTINGS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun rememberElizaAppState(
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): ElizaAppState {
    return remember(
        navController,
        coroutineScope,
        networkMonitor,
    ) {
        ElizaAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
        )
    }
}

@Stable
class ElizaAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentDestination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
                    return when (currentDestination?.route) {
            HOME.route -> HOME
            COURSE_SUGGESTIONS.route -> COURSE_SUGGESTIONS
            SETTINGS.route -> SETTINGS
            else -> HOME // Default to HOME
        }
        }

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Map of top level destinations to be used in the Bottom Navigation.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one level of navigation above them.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination) {
            HOME -> navController.navigateToHome(topLevelNavOptions)
            COURSE_SUGGESTIONS -> {
                // Navigate to course suggestions chat interface
                navController.navigateToCourseSuggestionsChat()
            }
            SETTINGS -> {
                // Navigate to settings screen
                navController.navigateToSettings(topLevelNavOptions)
            }
        }
    }
} 