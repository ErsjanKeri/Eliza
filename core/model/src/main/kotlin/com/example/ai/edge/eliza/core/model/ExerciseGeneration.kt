package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Relative difficulty for exercise generation based on existing Difficulty enum.
 * Maps user's difficulty preference to AI generation instructions.
 */
@Serializable
enum class RelativeDifficulty(
    val displayName: String,
    val description: String,
    val promptModifier: String,
    val targetDifficulty: Difficulty
) {
    EASIER(
        displayName = "Easier",
        description = "Simpler numbers and fewer steps",
        promptModifier = "Generate a simpler version with easier numbers and fewer calculation steps",
        targetDifficulty = Difficulty.EASY
    ),
    SAME(
        displayName = "Same Level", 
        description = "Keep the same difficulty level",
        promptModifier = "Generate a similar question with the same difficulty level and complexity",
        targetDifficulty = Difficulty.MEDIUM // Will be dynamically set based on original
    ),
    HARDER(
        displayName = "Harder",
        description = "More complex numbers and additional steps", 
        promptModifier = "Generate a more complex version with harder numbers and additional calculation steps",
        targetDifficulty = Difficulty.HARD
    );
    
    /**
     * Get the target difficulty based on the original exercise's difficulty.
     */
    fun getTargetDifficulty(originalDifficulty: Difficulty): Difficulty {
        return when (this) {
            EASIER -> when (originalDifficulty) {
                Difficulty.HARD -> Difficulty.MEDIUM
                Difficulty.MEDIUM -> Difficulty.EASY
                Difficulty.EASY -> Difficulty.EASY // Already easiest
            }
            SAME -> originalDifficulty
            HARDER -> when (originalDifficulty) {
                Difficulty.EASY -> Difficulty.MEDIUM
                Difficulty.MEDIUM -> Difficulty.HARD
                Difficulty.HARD -> Difficulty.HARD // Already hardest
            }
        }
    }
}

/**
 * Request for generating a new exercise/trial based on an existing exercise.
 * Uses existing infrastructure and data models.
 */
@Serializable
data class ExerciseGenerationRequest(
    val originalExercise: Exercise,
    val selectedDifficulty: RelativeDifficulty,
    val chapterContext: String?, // From RAG
    val conceptFocus: String // Extracted from original question
) {
    val targetDifficulty: Difficulty = selectedDifficulty.getTargetDifficulty(originalExercise.difficulty)
}

/**
 * Structured AI response for parsing generated exercise data.
 */
@Serializable
data class ExerciseGenerationResponse(
    val questionText: String,
    val options: List<String>, 
    val correctAnswerIndex: Int,
    val explanation: String,
    val conceptFocus: String,
    val difficultyAchieved: String
) {
    /**
     * Convert to Trial object using existing data model.
     */
    fun toTrial(originalExerciseId: String): Trial {
        return Trial(
            id = java.util.UUID.randomUUID().toString(),
            originalExerciseId = originalExerciseId,
            questionText = questionText,
            options = options,
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation,
            difficulty = when (difficultyAchieved.lowercase()) {
                "easy", "easier" -> Difficulty.EASY
                "hard", "harder" -> Difficulty.HARD
                else -> Difficulty.MEDIUM
            },
            isCompleted = false,
            userAnswer = null,
            isCorrect = null,
            generatedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Result states for exercise generation process.
 */
sealed class GenerationResult {
    data class Loading(val message: String) : GenerationResult()
    data class Success(val trial: Trial) : GenerationResult()
    data class Error(val message: String) : GenerationResult()
}