/*
 * Copyright 2025 AI Edge Eliza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.feature.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ai.edge.eliza.feature.chat.ui.ChatView
import com.example.ai.edge.eliza.feature.chat.ui.ExerciseHelpChatView

const val CHAT_ROUTE = "chat_route"
const val EXERCISE_HELP_CHAT_ROUTE = "exercise_help_chat_route"

/**
 * Navigation to chat screen
 */
fun NavController.navigateToChat(title: String) {
    this.navigate("$CHAT_ROUTE/$title")
}

/**
 * Navigation to exercise help chat  
 */
fun NavController.navigateToExerciseHelpChat(
    exerciseNumber: Int,
    questionText: String,
    userAnswer: String,
    correctAnswer: String
) {
    // URL encode the parameters to handle special characters
    val encodedQuestion = java.net.URLEncoder.encode(questionText, "UTF-8")
    val encodedUserAnswer = java.net.URLEncoder.encode(userAnswer, "UTF-8") 
    val encodedCorrectAnswer = java.net.URLEncoder.encode(correctAnswer, "UTF-8")
    
    this.navigate("$EXERCISE_HELP_CHAT_ROUTE/$exerciseNumber/$encodedQuestion/$encodedUserAnswer/$encodedCorrectAnswer")
}

/**
 * Chat section for the navigation graph
 */
fun NavGraphBuilder.chatSection(
    onNavigateUp: () -> Unit
) {
    // General chat screen
    composable(
        route = "$CHAT_ROUTE/{title}",
        arguments = listOf(
            navArgument("title") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val title = backStackEntry.arguments?.getString("title") ?: "Chat"
        
        ChatView(
            title = title,
            onNavigateUp = onNavigateUp
        )
    }
    
    // Exercise help chat screen  
    composable(
        route = "$EXERCISE_HELP_CHAT_ROUTE/{exerciseNumber}/{questionText}/{userAnswer}/{correctAnswer}",
        arguments = listOf(
            navArgument("exerciseNumber") { type = NavType.IntType },
            navArgument("questionText") { type = NavType.StringType },
            navArgument("userAnswer") { type = NavType.StringType },
            navArgument("correctAnswer") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val exerciseNumber = backStackEntry.arguments?.getInt("exerciseNumber") ?: 1
        val questionText = backStackEntry.arguments?.getString("questionText") ?: ""
        val userAnswer = backStackEntry.arguments?.getString("userAnswer") ?: ""
        val correctAnswer = backStackEntry.arguments?.getString("correctAnswer") ?: ""
        
        // URL decode the parameters
        val decodedQuestion = java.net.URLDecoder.decode(questionText, "UTF-8")
        val decodedUserAnswer = java.net.URLDecoder.decode(userAnswer, "UTF-8")
        val decodedCorrectAnswer = java.net.URLDecoder.decode(correctAnswer, "UTF-8")
        
        ExerciseHelpChatView(
            exerciseNumber = exerciseNumber,
            questionText = decodedQuestion,
            userAnswer = decodedUserAnswer,
            correctAnswer = decodedCorrectAnswer,
            onNavigateUp = onNavigateUp
        )
    }
} 