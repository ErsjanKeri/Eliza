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

package com.example.ai.edge.eliza.feature.chapter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ai.edge.eliza.feature.chapter.ChapterScreen

const val CHAPTER_ROUTE = "chapter_route"
const val CHAPTER_ID_ARG = "chapterId"

/**
 * Navigate to the chapter screen.
 */
fun NavController.navigateToChapter(
    chapterId: String,
    navOptions: NavOptions? = null
) = navigate("$CHAPTER_ROUTE/$chapterId", navOptions)

/**
 * Chapter screen composable for the navigation graph.
 * 
 * Shows markdown content with full-screen and split-screen chat functionality.
 * 
 * @param onBackClick - Called when the back button is clicked
 */
fun NavGraphBuilder.chapterScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$CHAPTER_ROUTE/{$CHAPTER_ID_ARG}",
        arguments = listOf(
            navArgument(CHAPTER_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getString(CHAPTER_ID_ARG) ?: ""
        ChapterScreen(
            chapterId = chapterId,
            onBackClick = onBackClick
        )
    }
} 