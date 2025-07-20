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

package com.example.ai.edge.eliza.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.ai.edge.eliza.feature.home.HomeScreen

const val HOME_ROUTE = "home_route"
const val HOME_BASE_ROUTE = "home_base_route"

fun NavController.navigateToHome(navOptions: NavOptions) = navigate(HOME_ROUTE, navOptions)

/**
 * The Home section of the app. This is the main landing screen where users can
 * continue learning or start new courses.
 *
 * @param onCourseClick - Called when a course is clicked, contains the ID of the course
 */
fun NavGraphBuilder.homeSection(
    onCourseClick: (String) -> Unit,
) {
    navigation(
        startDestination = HOME_ROUTE,
        route = HOME_BASE_ROUTE,
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(onCourseClick = onCourseClick)
        }
    }
} 