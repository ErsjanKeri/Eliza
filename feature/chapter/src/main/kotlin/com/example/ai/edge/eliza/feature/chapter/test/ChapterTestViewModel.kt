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

package com.example.ai.edge.eliza.feature.chapter.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import com.example.ai.edge.eliza.core.model.ChapterTest
import com.example.ai.edge.eliza.core.model.TestResult
import com.example.ai.edge.eliza.core.model.TestState
import com.example.ai.edge.eliza.core.model.Exercise
import javax.inject.Inject

/**
 * ViewModel for managing chapter test state and operations.
 * Handles test progression, scoring, and result submission.
 */
@HiltViewModel
class ChapterTestViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<TestState>(TestState.NotStarted)
    val testState: StateFlow<TestState> = _testState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Constants
    private val defaultUserId = "default_user" // TODO: Get from user session
    private var testStartTime: Long = 0L

    /**
     * Start a test for the given chapter.
     */
    fun startTest(chapterId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Get chapter from repository
                val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
                if (chapter == null) {
                    _errorMessage.value = "Chapter not found"
                    _testState.value = TestState.Error("Chapter not found")
                    return@launch
                }

                // Validate that chapter has at least 1 exercise for testing
                if (chapter.exercises.isEmpty()) {
                    _errorMessage.value = "Chapter must have at least one exercise for testing"
                    _testState.value = TestState.Error("No exercises available for this chapter")
                    return@launch
                }

                // Get all exercises for this chapter from repository (in case chapter.exercises is empty)
                val allExercises = courseRepository.getExercisesByChapter(chapterId).firstOrNull() ?: emptyList()
                val exercisesToUse = if (allExercises.isNotEmpty()) allExercises else chapter.exercises

                if (exercisesToUse.isEmpty()) {
                    _errorMessage.value = "No exercises found for this chapter"
                    _testState.value = TestState.Error("No exercises available for testing")
                    return@launch
                }

                // Create test state with variable number of exercises
                val chapterTest = ChapterTest(
                    chapterId = chapterId,
                    chapterTitle = chapter.title,
                    exercises = exercisesToUse,
                    currentQuestionIndex = 0,
                    userAnswers = List(exercisesToUse.size) { null }
                )

                testStartTime = System.currentTimeMillis()
                _testState.value = TestState.InProgress(chapterTest)

            } catch (e: Exception) {
                _errorMessage.value = e.message
                _testState.value = TestState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select an answer for the current question.
     */
    fun selectAnswer(answerIndex: Int) {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test
            val updatedAnswers = currentTest.userAnswers.toMutableList()
            updatedAnswers[currentTest.currentQuestionIndex] = answerIndex

            val updatedTest = currentTest.copy(userAnswers = updatedAnswers)
            _testState.value = TestState.InProgress(updatedTest)
        }
    }

    /**
     * Navigate to a specific question.
     */
    fun navigateToQuestion(questionIndex: Int) {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test
            if (questionIndex in 0 until currentTest.exercises.size) {
                val updatedTest = currentTest.copy(currentQuestionIndex = questionIndex)
                _testState.value = TestState.InProgress(updatedTest)
            }
        }
    }

    /**
     * Go to the previous question.
     */
    fun previousQuestion() {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test
            if (currentTest.canGoPrevious) {
                val updatedTest = currentTest.copy(
                    currentQuestionIndex = currentTest.currentQuestionIndex - 1
                )
                _testState.value = TestState.InProgress(updatedTest)
            }
        }
    }

    /**
     * Go to the next question.
     */
    fun nextQuestion() {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test
            if (currentTest.canGoNext) {
                val updatedTest = currentTest.copy(
                    currentQuestionIndex = currentTest.currentQuestionIndex + 1
                )
                _testState.value = TestState.InProgress(updatedTest)
            }
        }
    }

    /**
     * Submit the test and calculate results.
     */
    fun submitTest() {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test

            // Validate that all questions are answered
            if (!currentTest.isAllAnswered) {
                _errorMessage.value = "Please answer all questions before submitting"
                return
            }

            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    // Calculate score and results
                    val testResult = calculateTestResult(currentTest)

                    // Submit results to repository (simplified for reliability)
                    try {
                        submitTestResults(testResult)
                    } catch (e: Exception) {
                        // Don't fail the entire test submission if repository update fails
                        _errorMessage.value = "Test completed but some data may not be saved"
                    }

                    // Update test state - this should trigger navigation
                    _testState.value = TestState.Completed(testResult)

                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Failed to submit test"
                    _testState.value = TestState.Error(e.message ?: "Failed to submit test")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Calculate test results from user answers.
     */
    private fun calculateTestResult(chapterTest: ChapterTest): TestResult {
        var correctAnswers = 0
        val wrongExercises = mutableListOf<Exercise>()
        val userAnswers = chapterTest.userAnswers.map { it ?: -1 }
        val allExercises = chapterTest.exercises // Store reference to all exercises

        chapterTest.exercises.forEachIndexed { index, exercise ->
            val userAnswer = userAnswers[index]
            if (userAnswer == exercise.correctAnswerIndex) {
                correctAnswers++
            } else {
                wrongExercises.add(exercise)
            }
        }

        val score = if (chapterTest.exercises.isNotEmpty()) {
            (correctAnswers * 100) / chapterTest.exercises.size
        } else {
            0
        }
        val timeSpentSeconds = (System.currentTimeMillis() - testStartTime) / 1000

        return TestResult(
            chapterId = chapterTest.chapterId,
            chapterTitle = chapterTest.chapterTitle,
            score = score,
            correctAnswers = correctAnswers,
            totalQuestions = chapterTest.exercises.size,
            wrongExercises = wrongExercises,
            userAnswers = userAnswers,
            timeSpentSeconds = timeSpentSeconds,
            exercises = allExercises // Add all exercises for proper result processing
        )
    }

    /**
     * Submit test results to the repository and update chapter completion.
     */
    private suspend fun submitTestResults(testResult: TestResult) {
        try {
            // Update chapter with test results first (simplified approach)
            val chapter = courseRepository.getChapterById(testResult.chapterId).firstOrNull()
            if (chapter != null) {
                val updatedChapter = chapter.copy(
                    isCompleted = testResult.isPassing,
                    testScore = testResult.score,
                    testAttempts = chapter.testAttempts + 1,
                    lastTestAttempt = System.currentTimeMillis()
                )
                courseRepository.updateChapter(updatedChapter)
            }

            // Update exercise results for wrong exercises only (simplified)
            testResult.wrongExercises.forEach { exercise ->
                val userAnswerIndex = testResult.userAnswers.getOrElse(
                    testResult.exercises.indexOf(exercise)
                ) { -1 }
                
                val updatedExercise = exercise.copy(
                    userAnswer = userAnswerIndex,
                    isCorrect = false,
                    isCompleted = true
                )
                courseRepository.updateExercise(updatedExercise)
            }

        } catch (e: Exception) {
            // Log error but don't fail the entire submission
            _errorMessage.value = "Warning: Some progress data may not have been saved"
            // Continue with test completion
        }
    }

    /**
     * Reset test state to start a new test.
     */
    fun resetTest() {
        _testState.value = TestState.NotStarted
        _errorMessage.value = null
        _isLoading.value = false
        testStartTime = 0L
    }

    /**
     * Request help for a specific exercise (integrate with existing ExerciseHelp system).
     */
    fun requestExerciseHelp(exercise: Exercise, incorrectAnswer: Int) {
        // TODO: Integrate with existing ExerciseHelp system
        // This would typically navigate to the help screen or show a dialog
        // For now, this is a placeholder
        viewModelScope.launch {
            try {
                // Create exercise help request
                // exerciseHelpRepository.createHelpRequest(exercise.id, incorrectAnswer, defaultUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to request help: ${e.message}"
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
} 