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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.example.ai.edge.eliza.feature.home.navigation.HOME_BASE_ROUTE
import com.example.ai.edge.eliza.feature.home.navigation.homeSection
import com.example.ai.edge.eliza.feature.courseprogress.navigation.courseProgressScreen
import com.example.ai.edge.eliza.feature.courseprogress.navigation.navigateToCourseProgress
import com.example.ai.edge.eliza.ui.ElizaAppState

/**
 * Top-level navigation graph for the Eliza application. 
 * Navigation is organized following NowInAndroid patterns.
 *
 * The navigation graph defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun ElizaNavHost(
    appState: ElizaAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HOME_BASE_ROUTE,
        modifier = modifier,
    ) {
        homeSection(
            onCourseClick = { courseId ->
                // Navigate to course progress screen
                navController.navigateToCourseProgress(courseId)
            }
        )
        
        courseProgressScreen(
            onBackClick = {
                navController.popBackStack()
            }
        )
        
        // TODO: Add other sections as they're implemented
        // settingsSection()
        // chatSection()
        // coursesSection()
    }
} 