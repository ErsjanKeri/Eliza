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
import com.example.ai.edge.eliza.core.model.UserAnswer
import com.example.ai.edge.eliza.core.model.TestResult
import com.example.ai.edge.eliza.core.model.TestState
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.GenerationResult // NEW: For exercise generation
import com.example.ai.edge.eliza.core.model.Model // NEW: For AI model handling
import com.example.ai.edge.eliza.core.model.RelativeDifficulty // NEW: For difficulty selection
import com.example.ai.edge.eliza.core.model.Trial // NEW: For generated trials
import com.example.ai.edge.eliza.ai.service.ExerciseGenerationService // NEW: Generation service
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager // NEW: Model management
import com.example.ai.edge.eliza.ai.modelmanager.manager.ModelInitializationStatusType // NEW: Model status
import com.example.ai.edge.eliza.ai.modelmanager.data.ELIZA_TASKS // NEW: Task definitions
import com.example.ai.edge.eliza.ai.modelmanager.data.TaskType // NEW: Task types
import android.content.Context // NEW: For model initialization
import android.util.Log // NEW: For logging
import dagger.hilt.android.qualifiers.ApplicationContext // NEW: Context injection
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing chapter test state and operations.
 * Handles test progression, scoring, and result submission.
 */
@HiltViewModel
class ChapterTestViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // NEW: For model initialization
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository,
    private val exerciseGenerationService: ExerciseGenerationService // NEW: For AI generation
) : ViewModel() {

    // ElizaModelManager will be provided from the UI layer since it's a @HiltViewModel
    private var elizaModelManager: ElizaModelManager? = null
    
    /**
     * Set the model manager from the UI layer.
     * This is required because ElizaModelManager is a @HiltViewModel and can't be directly injected.
     */
    fun setModelManager(modelManager: ElizaModelManager) {
        this.elizaModelManager = modelManager
    }

    private val _testState = MutableStateFlow<TestState>(TestState.NotStarted)
    val testState: StateFlow<TestState> = _testState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // NEW: Exercise generation state management
    private val _generationState = MutableStateFlow<GenerationResult?>(null)
    val generationState: StateFlow<GenerationResult?> = _generationState.asStateFlow()

    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    private val _showGenerationDialog = MutableStateFlow(false)
    val showGenerationDialog: StateFlow<Boolean> = _showGenerationDialog.asStateFlow()

    private val _selectedExerciseForGeneration = MutableStateFlow<Exercise?>(null)
    val selectedExerciseForGeneration: StateFlow<Exercise?> = _selectedExerciseForGeneration.asStateFlow()

    private val _showTrialPractice = MutableStateFlow(false)
    val showTrialPractice: StateFlow<Boolean> = _showTrialPractice.asStateFlow()

    private val _currentTrial = MutableStateFlow<Trial?>(null)
    val currentTrial: StateFlow<Trial?> = _currentTrial.asStateFlow()

    // Constants
    private val defaultUserId = "user_default" // TODO: Get from user session
    private var testStartTime: Long = 0L

    /**
     * Show test results for the given chapter (load from saved data).
     */
    fun showResults(chapterId: String) {
        viewModelScope.launch {
            loadChapterForResults(chapterId)
        }
    }

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

                // ALWAYS start a fresh test when startTest() is called
                // The UI button determines whether to start test or show results
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
     * During test taking, answers are only stored in UI state until test submission.
     */
    fun selectAnswer(answerIndex: Int) {
        val currentState = _testState.value
        if (currentState is TestState.InProgress) {
            val currentTest = currentState.test
            
            // Update UI state only - don't save to database until test is submitted
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
     * Implements comprehensive progress tracking with "best attempt" logic.
     */
    private suspend fun submitTestResults(testResult: TestResult) {
        try {
            // Step 1: Save individual UserAnswer records for detailed tracking
            testResult.exercises.forEachIndexed { index, exercise ->
                val userAnswer = testResult.userAnswers.getOrElse(index) { -1 }
                val isCorrect = userAnswer == exercise.correctAnswerIndex
                
                // Create UserAnswer record for this test attempt
                val answerRecord = UserAnswer(
                    id = UUID.randomUUID().toString(),
                    exerciseId = exercise.id,
                    trialId = null, // This is a test attempt, not a trial
                    userId = defaultUserId,
                    selectedAnswer = userAnswer,
                    isCorrect = isCorrect,
                    timeSpentSeconds = testResult.timeSpentSeconds / testResult.exercises.size, // Distribute time evenly
                    hintsUsed = 0,
                    answeredAt = testResult.completedAt
                )
                
                // Save UserAnswer record
                progressRepository.recordAnswer(answerRecord)
            }
            
            // Step 2: Update Exercise entities with "best attempt" logic
            testResult.exercises.forEachIndexed { index, exercise ->
                val userAnswer = testResult.userAnswers.getOrElse(index) { -1 }
                val isCorrect = userAnswer == exercise.correctAnswerIndex
                
                // KEY: "Best attempt" logic - once correct, stays correct!
                val updatedExercise = exercise.copy(
                    userAnswer = userAnswer,
                    isCorrect = isCorrect,
                    isCompleted = isCorrect || exercise.isCompleted // Once true, stays true!
                )
                
                courseRepository.updateExercise(updatedExercise)
            }
            
            // Step 3: Update Chapter with test results and permanent progress
            val chapter = courseRepository.getChapterById(testResult.chapterId).firstOrNull()
            if (chapter != null) {
                // Calculate permanent progress based on updated Exercise.isCompleted status
                val permanentlyCompletedCount = testResult.exercises.count { exercise ->
                    val userAnswer = testResult.userAnswers.getOrElse(testResult.exercises.indexOf(exercise)) { -1 }
                    val isCorrect = userAnswer == exercise.correctAnswerIndex
                    isCorrect || exercise.isCompleted // Either just solved OR was already completed
                }
                val isPermanentlyComplete = permanentlyCompletedCount == testResult.exercises.size
                
                val updatedChapter = chapter.copy(
                    isCompleted = isPermanentlyComplete, // Based on permanent progress
                    testScore = testResult.score, // Current test attempt score
                    testAttempts = chapter.testAttempts + 1,
                    lastTestAttempt = System.currentTimeMillis()
                )
                
                courseRepository.updateChapter(updatedChapter)
            }
            
        } catch (e: Exception) {
            // Don't fail the entire test submission if some saves fail
            _errorMessage.value = "Test completed but some progress may not have been saved"
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
    
    /**
     * Load chapter data for results screen when coming from navigation.
     * This recreates the test result with full exercise data and real user answers.
     */
    suspend fun loadChapterForResults(chapterId: String) {
        try {
            _isLoading.value = true
            val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
            if (chapter != null) {
                val exercises = chapter.exercises
                
                // Load real user answers from saved UserAnswer records
                val realUserAnswers = mutableListOf<Int>()
                val wrongExercises = mutableListOf<Exercise>()
                var correctAnswers = 0
                
                // For each exercise, get the latest TEST answer (not trial)
                exercises.forEach { exercise ->
                    val userAnswerRecords = progressRepository.getUserAnswersByExercise(exercise.id, defaultUserId).firstOrNull()
                    // Filter for test attempts only (trialId == null) and get the latest one
                    val testAnswers = userAnswerRecords?.filter { it.trialId == null }
                    val latestTestAnswer = testAnswers?.maxByOrNull { it.answeredAt }
                    
                    val userAnswer = latestTestAnswer?.selectedAnswer ?: -1
                    realUserAnswers.add(userAnswer)
                    
                    // Calculate correct vs wrong based on actual saved answers
                    if (userAnswer >= 0 && userAnswer == exercise.correctAnswerIndex) {
                        correctAnswers++
                    } else if (userAnswer >= 0) {
                        wrongExercises.add(exercise)
                    }
                }
                
                // Calculate score based on real answers
                val score = if (exercises.isNotEmpty()) {
                    (correctAnswers * 100) / exercises.size
                } else {
                    0
                }
                
                val testResult = TestResult(
                    chapterId = chapterId,
                    chapterTitle = chapter.title,
                    score = score, // Calculated from real answers
                    correctAnswers = correctAnswers, // Calculated from real answers
                    totalQuestions = exercises.size,
                    wrongExercises = wrongExercises, // Calculated from real answers
                    userAnswers = realUserAnswers, // Real user answers from database!
                    timeSpentSeconds = 0L, // Historical time not tracked
                    exercises = exercises // All exercises for reference
                )
                
                _testState.value = TestState.Completed(testResult)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load chapter data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Calculate test result from current exercise progress.
     * Used when user has existing progress and we go directly to results.
     */
    private fun calculateCurrentTestResult(chapterId: String, chapterTitle: String, exercises: List<Exercise>): TestResult {
        val solvedCount = exercises.count { it.isCompleted }  // Count "permanently solved" exercises
        val score = if (exercises.isNotEmpty()) (solvedCount * 100) / exercises.size else 0
        val wrongExercises = exercises.filter { it.userAnswer != null && !it.isCompleted }
        val userAnswers = exercises.map { it.userAnswer ?: -1 }
        
        return TestResult(
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            score = score,
            correctAnswers = solvedCount,
            totalQuestions = exercises.size,
            wrongExercises = wrongExercises,
            userAnswers = userAnswers,
            timeSpentSeconds = 0L, // We don't track historical time
            exercises = exercises  // All exercises with current state
        )
    }
    
    /**
     * Reset all progress for this chapter and start fresh test.
     * Called when user clicks "Retake Test" button.
     */
    fun retakeTest(chapterId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Reset all exercises for this chapter
                courseRepository.resetChapterProgress(chapterId)
                
                // Start fresh test
                startTest(chapterId)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset test: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // NEW: Exercise generation functionality

    /**
     * Start the exercise generation process for a given exercise.
     * Shows difficulty selection dialog.
     */
    fun requestExerciseGeneration(exercise: Exercise) {
        _selectedExerciseForGeneration.value = exercise
        _showDifficultyDialog.value = true
    }

    /**
     * Generate new question with selected difficulty and provided model.
     * Model is already initialized by DifficultySelectionDialog.
     */
    fun generateNewQuestion(exercise: Exercise, difficulty: RelativeDifficulty, model: Model) {
        viewModelScope.launch {
            _showDifficultyDialog.value = false
            _showGenerationDialog.value = true
            
            try {
                Log.d("ChapterTestViewModel", "Starting exercise generation for exercise: ${exercise.id}")
                Log.d("ChapterTestViewModel", "Using model: ${model.name}")
                
                // Start the actual generation process (model is already ready)
                exerciseGenerationService.generateTrialQuestion(
                    originalExercise = exercise,
                    difficulty = difficulty,
                    model = model
                ).collect { result ->
                    _generationState.value = result
                }
                
            } catch (e: Exception) {
                Log.e("ChapterTestViewModel", "Exercise generation failed", e)
                _generationState.value = GenerationResult.Error("Generation failed: ${e.message}")
            }
        }
    }

    /**
     * Start practice with generated trial (modal approach).
     */
    fun startTrialPractice(trial: Trial) {
        _showGenerationDialog.value = false
        _generationState.value = null
        _currentTrial.value = trial
        _showTrialPractice.value = true
    }

    /**
     * Generate another question with same settings.
     */
    fun generateAnother() {
        val selectedExercise = _selectedExerciseForGeneration.value
        if (selectedExercise != null) {
            // Reopen difficulty dialog for new generation
            _showGenerationDialog.value = false
            _showDifficultyDialog.value = true
        }
    }

    /**
     * Dismiss all generation dialogs.
     */
    fun dismissGenerationDialogs() {
        _showDifficultyDialog.value = false
        _showGenerationDialog.value = false
        _generationState.value = null
        _selectedExerciseForGeneration.value = null
    }

    /**
     * Handle trial practice answer submission.
     */
    fun submitTrialAnswer(answerIndex: Int, isCorrect: Boolean) {
        val trial = _currentTrial.value ?: return
        
        viewModelScope.launch {
            try {
                // Use existing CourseRepository method to submit trial answer
                courseRepository.submitTrialAnswer(trial.id, answerIndex)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save trial answer: ${e.message}"
            }
        }
    }

    /**
     * Dismiss trial practice screen.
     */
    fun dismissTrialPractice() {
        _showTrialPractice.value = false
        _currentTrial.value = null
    }

    /**
     * Generate another question from trial practice.
     */
    fun generateAnotherFromPractice() {
        val selectedExercise = _selectedExerciseForGeneration.value
        if (selectedExercise != null) {
            _showTrialPractice.value = false
            _currentTrial.value = null
            _showDifficultyDialog.value = true
        }
    }

    /**
     * Get current selected model and ensure it's properly initialized.
     * Returns null if no model is selected or if initialization fails.
     */
    private suspend fun getCurrentInitializedModel(): com.example.ai.edge.eliza.core.model.Model? {
        try {
            // Check if model manager is available
            val modelManager = elizaModelManager
            if (modelManager == null) {
                Log.e("ChapterTestViewModel", "ElizaModelManager not set - call setModelManager() first")
                return null
            }
            
            // Get current UI state from ElizaModelManager
            val uiState = modelManager.uiState.value
            val selectedModel = uiState.selectedModel
            
            if (selectedModel == null) {
                Log.d("ChapterTestViewModel", "No model selected in ElizaModelManager")
                return null
            }
            
            Log.d("ChapterTestViewModel", "Selected model: ${selectedModel.name}")
            
            // Check if model is already initialized
            val initStatus = uiState.modelInitializationStatus[selectedModel.name]
            
            when (initStatus?.status) {
                ModelInitializationStatusType.INITIALIZED -> {
                    Log.d("ChapterTestViewModel", "Model ${selectedModel.name} already initialized")
                    return selectedModel
                }
                ModelInitializationStatusType.INITIALIZING -> {
                    Log.d("ChapterTestViewModel", "Model ${selectedModel.name} is currently initializing, waiting...")
                    // Wait for initialization to complete
                    return waitForModelInitialization(selectedModel)
                }
                else -> {
                    Log.d("ChapterTestViewModel", "Model ${selectedModel.name} not initialized, starting initialization...")
                    // Initialize the model
                    return initializeModel(selectedModel)
                }
            }
        } catch (e: Exception) {
            Log.e("ChapterTestViewModel", "Failed to get initialized model", e)
            return null
        }
    }
    
    /**
     * Initialize a model and wait for it to complete.
     */
    private suspend fun initializeModel(model: com.example.ai.edge.eliza.core.model.Model): com.example.ai.edge.eliza.core.model.Model? {
        try {
            val modelManager = elizaModelManager ?: return null
            
            // Find the appropriate task for this model
            val task = ELIZA_TASKS.find { task -> 
                task.models.any { it.name == model.name }
            } ?: ELIZA_TASKS.first() // Fallback to first task
            
            Log.d("ChapterTestViewModel", "Initializing model ${model.name} with task ${task.type}")
            
            // Start model initialization
            modelManager.initializeModel(context, task, model)
            
            // Wait for initialization to complete
            return waitForModelInitialization(model)
            
        } catch (e: Exception) {
            Log.e("ChapterTestViewModel", "Failed to initialize model ${model.name}", e)
            return null
        }
    }
    
    /**
     * Wait for model initialization to complete and return the model if successful.
     */
    private suspend fun waitForModelInitialization(model: com.example.ai.edge.eliza.core.model.Model): com.example.ai.edge.eliza.core.model.Model? {
        return try {
            val modelManager = elizaModelManager ?: return null
            
            // Use kotlinx.coroutines delay in a loop to wait for initialization
            var attempts = 0
            val maxAttempts = 30 // 30 seconds max wait time
            
            while (attempts < maxAttempts) {
                kotlinx.coroutines.delay(1000) // Wait 1 second
                attempts++
                
                val currentStatus = modelManager.uiState.value.modelInitializationStatus[model.name]
                
                when (currentStatus?.status) {
                    ModelInitializationStatusType.INITIALIZED -> {
                        Log.d("ChapterTestViewModel", "Model ${model.name} initialized successfully after ${attempts} seconds")
                        return model
                    }
                    ModelInitializationStatusType.ERROR -> {
                        Log.e("ChapterTestViewModel", "Model ${model.name} initialization failed: ${currentStatus.error}")
                        return null
                    }
                    ModelInitializationStatusType.INITIALIZING -> {
                        Log.d("ChapterTestViewModel", "Model ${model.name} still initializing... (${attempts}/${maxAttempts})")
                        continue
                    }
                    else -> {
                        Log.w("ChapterTestViewModel", "Model ${model.name} status unknown: ${currentStatus?.status}")
                        continue
                    }
                }
            }
            
            Log.e("ChapterTestViewModel", "Model ${model.name} initialization timed out after ${maxAttempts} seconds")
            null
            
        } catch (e: Exception) {
            Log.e("ChapterTestViewModel", "Error waiting for model initialization", e)
            null
        }
    }
} 