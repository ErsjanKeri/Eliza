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

package com.example.ai.edge.eliza.feature.courseprogress.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ai.edge.eliza.feature.courseprogress.CourseProgressScreen

const val COURSE_PROGRESS_ROUTE = "course_progress_route"
const val COURSE_ID_ARG = "courseId"

fun NavController.navigateToCourseProgress(
    courseId: String,
    navOptions: NavOptions? = null
) = navigate("$COURSE_PROGRESS_ROUTE/$courseId", navOptions)

/**
 * Course Progress screen that shows Duolingo-style chapter progression for a specific course.
 *
 * @param onBackClick - Called when the back button is clicked
 */
fun NavGraphBuilder.courseProgressScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$COURSE_PROGRESS_ROUTE/{$COURSE_ID_ARG}",
        arguments = listOf(
            navArgument(COURSE_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString(COURSE_ID_ARG) ?: ""
        CourseProgressScreen(
            courseId = courseId,
            onBackClick = onBackClick
        )
    }
} 