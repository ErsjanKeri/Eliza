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

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a chapter test session for UI state management.
 * Wraps the chapter's exercises (exactly 5) with test-taking state.
 */
@Serializable
data class ChapterTest(
    val chapterId: String,
    val chapterTitle: String,
    val exercises: List<Exercise>, // All exercises from the chapter
    val currentQuestionIndex: Int = 0,
    val userAnswers: List<Int?> = List(exercises.size) { null }, // Track all answers (0-n or null)
    val isCompleted: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate progress as a percentage (0.0 to 1.0)
     */
    val progress: Float
        get() = currentQuestionIndex.toFloat() / exercises.size.toFloat()
    
    /**
     * Check if all questions have been answered
     */
    val isAllAnswered: Boolean
        get() = userAnswers.all { it != null }
    
    /**
     * Get the number of answered questions
     */
    val answeredCount: Int
        get() = userAnswers.count { it != null }
    
    /**
     * Check if user can navigate to next question
     */
    val canGoNext: Boolean
        get() = currentQuestionIndex < exercises.size - 1
    
    /**
     * Check if user can navigate to previous question
     */
    val canGoPrevious: Boolean
        get() = currentQuestionIndex > 0
}

/**
 * Represents the result of a completed chapter test.
 * Used for displaying results and determining next actions.
 */
@Serializable
data class TestResult(
    val chapterId: String,
    val chapterTitle: String,
    val score: Int, // 0-100 percentage
    val correctAnswers: Int,
    val totalQuestions: Int,
    val wrongExercises: List<Exercise>, // Exercises the user got wrong
    val userAnswers: List<Int>, // All user answers (0-3)
    val timeSpentSeconds: Long,
    val exercises: List<Exercise> = emptyList(), // All exercises for reference
    val completedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if the user passed the test (100% required)
     */
    val isPassing: Boolean
        get() = score == 100
    
    /**
     * Get the grade as a letter (A, B, C, D, F)
     */
    val letterGrade: String
        get() = when {
            score >= 100 -> "A+"
            score >= 90 -> "A"
            score >= 80 -> "B"
            score >= 70 -> "C"
            score >= 60 -> "D"
            else -> "F"
        }
    
    /**
     * Get a motivational message based on the score
     */
    val message: String
        get() = when {
            score == 100 -> "Perfect! Chapter complete!"
            score >= 80 -> "Great work! Try again for 100% to complete the chapter."
            score >= 60 -> "Good effort! Review the material and try again."
            else -> "Keep studying! You can do this!"
        }
}

/**
 * Represents the state of test taking for UI management.
 * Used by ViewModels to track test progress and navigation.
 */
@Serializable
sealed class TestState {
    /**
     * Test is not started yet
     */
    @Serializable
    data object NotStarted : TestState()
    
    /**
     * Test is currently being taken
     */
    @Serializable
    data class InProgress(
        val test: ChapterTest
    ) : TestState()
    
    /**
     * Test has been completed and results are available
     */
    @Serializable
    data class Completed(
        val result: TestResult
    ) : TestState()
    
    /**
     * Test failed to load or submit
     */
    @Serializable
    data class Error(
        val message: String
    ) : TestState()
} 