package com.example.ai.edge.eliza.ai.service

import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.ExerciseGenerationRequest
import com.example.ai.edge.eliza.core.model.RelativeDifficulty

/**
 * Intelligent prompt templates for exercise generation with Gemma 3n.
 * Reuses existing data models and focuses on reliable JSON output.
 */
object ExercisePromptTemplates {
    
    /**
     * Create a simple, effective generation prompt for the given request.
     * Uses line-by-line format to avoid JSON parsing issues with commas and quotes.
     */
    fun createGenerationPrompt(request: ExerciseGenerationRequest): String {
        val original = request.originalExercise
        val difficulty = request.selectedDifficulty
        
        return """
            Create a new math question similar to this one:
            
            ORIGINAL: "${original.questionText}"
            CORRECT ANSWER: ${original.options[original.correctAnswerIndex]}
            
            TASK: ${difficulty.promptModifier}
            CONCEPT: ${request.conceptFocus}
            
            Generate your response in EXACTLY this format (no extra text):
            
            questionText: ${createExampleQuestion(original, difficulty)}
            option1: ${getExampleOption(original, 0)}
            option2: ${getExampleOption(original, 1)}
            option3: ${getExampleOption(original, 2)}
            option4: ${getExampleOption(original, 3)}
            correctAnswerIndex: ${original.correctAnswerIndex}
            explanation: ${original.explanation.replace(":", " -").take(50)}
            conceptFocus: ${request.conceptFocus}
            difficultyAchieved: ${difficulty.name.lowercase()}
            
            RULES:
            - Change numbers but keep same concept
            - One answer per line
            - No colons in answers (use = instead)
            - Follow format exactly
        """.trimIndent()
    }
    
    /**
     * Create an example question showing the desired format.
     */
    private fun createExampleQuestion(original: Exercise, difficulty: RelativeDifficulty): String {
        val baseQuestion = original.questionText
        return when {
            baseQuestion.contains("slope") -> "What is the slope of y = 2x + 3?"
            baseQuestion.contains("solve") && baseQuestion.contains("equation") -> "Solve: 3x + 2 = 11"
            baseQuestion.contains("vertex") -> "What is the vertex of y = x² - 4x + 3?"
            baseQuestion.contains("factor") -> "Factor: x² + 5x + 6"
            else -> "Find x when: 2x + 1 = 9"
        }.replace("\"", "\\\"") // Escape quotes for JSON
    }
    
    /**
     * Get a specific example option for line-by-line format.
     */
    private fun getExampleOption(original: Exercise, index: Int): String {
        val baseOptions = when {
            original.questionText.contains("slope") -> listOf("2", "3", "-2", "-3")
            original.questionText.contains("vertex") -> listOf("(2, -1)", "(2, 1)", "(-2, -1)", "(-2, 1)")
            original.questionText.contains("factor") -> listOf("(x + 2)(x + 3)", "(x + 1)(x + 6)", "(x - 2)(x - 3)", "(x - 1)(x - 6)")
            else -> listOf("x = 4", "x = 3", "x = 5", "x = 2")
        }
        return if (index < baseOptions.size) baseOptions[index] else "option ${index + 1}"
    }
    
    /**
     * Get difficulty-specific requirements for prompt generation.
     */
    private fun getDifficultySpecificRequirements(difficulty: RelativeDifficulty): String {
        return when (difficulty) {
            RelativeDifficulty.EASIER -> """
                - Use smaller, simpler numbers
                - Reduce calculation steps
                - Make the concept more straightforward
                - Avoid fractions or decimals if possible
            """.trimIndent()
            
            RelativeDifficulty.SAME -> """
                - Maintain similar complexity level
                - Use comparable number ranges
                - Keep similar number of calculation steps
                - Preserve the original challenge level
            """.trimIndent()
            
            RelativeDifficulty.HARDER -> """
                - Use larger or more complex numbers
                - Add additional calculation steps
                - Include more challenging mathematical operations
                - Consider introducing fractions, decimals, or multi-step problems
            """.trimIndent()
        }
    }
    
    /**
     * Extract the main mathematical concept from an exercise.
     */
    fun extractConceptFocus(exercise: Exercise): String {
        val questionText = exercise.questionText.lowercase()
        
        return when {
            questionText.contains("slope") || questionText.contains("gradient") -> 
                "Linear Functions and Slope"
            questionText.contains("solve") && questionText.contains("equation") -> 
                "Solving Linear Equations"
            questionText.contains("factor") || questionText.contains("factoring") -> 
                "Factoring Expressions"
            questionText.contains("quadratic") -> 
                "Quadratic Equations"
            questionText.contains("system") && questionText.contains("equation") -> 
                "Systems of Equations"
            questionText.contains("graph") || questionText.contains("coordinate") -> 
                "Graphing and Coordinates"
            questionText.contains("percent") || questionText.contains("%") -> 
                "Percentage and Proportion"
            questionText.contains("area") || questionText.contains("perimeter") -> 
                "Geometry and Measurement"
            questionText.contains("probability") -> 
                "Probability and Statistics"
            questionText.contains("fraction") || questionText.contains("decimal") -> 
                "Fractions and Decimals"
            else -> "Mathematical Problem Solving"
        }
    }
    
    /**
     * Create a validation prompt to check if generated question is good quality.
     */
    fun createValidationPrompt(original: Exercise, generated: String): String {
        return """
            You are a math education expert. Review this generated practice question:
            
            ORIGINAL: "${original.questionText}"
            GENERATED: "$generated"
            
            Respond with ONLY "VALID" or "INVALID" based on whether the generated question:
            1. Tests the same mathematical concept
            2. Has a clear, unambiguous correct answer
            3. Has exactly 4 distinct options
            4. Is grammatically correct and clear
            
            RESPONSE:
        """.trimIndent()
    }
}