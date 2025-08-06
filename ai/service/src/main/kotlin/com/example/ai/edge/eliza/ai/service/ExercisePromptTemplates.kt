package com.example.ai.edge.eliza.ai.service

import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.ExerciseGenerationRequest
import com.example.ai.edge.eliza.core.model.RelativeDifficulty
import com.example.ai.edge.eliza.core.model.SupportedLanguage

/**
 * Intelligent prompt templates for exercise generation with Gemma 3n.
 * Reuses existing data models and focuses on reliable JSON output.
 */
object ExercisePromptTemplates {
    
    /**
     * Create a simple, effective generation prompt for the given request.
     * Uses line-by-line format to avoid JSON parsing issues with commas and quotes.
     */
    fun createGenerationPrompt(request: ExerciseGenerationRequest, language: SupportedLanguage): String {
        val original = request.originalExercise
        val difficulty = request.selectedDifficulty
        
        return """
            Create a new math question similar to this one:
            
            ORIGINAL QUESTION: "${original.questionText.get(language)}"
            CORRECT ANSWER: ${original.options[original.correctAnswerIndex].get(language)}
            
            TASK: ${difficulty.promptModifier}
            
            You must respond in EXACTLY this format with no extra text:
            
            questionText: [your new question here]
            option1: [first answer choice]
            option2: [second answer choice] 
            option3: [third answer choice]
            option4: [fourth answer choice]
            correctAnswerIndex: [0, 1, 2, or 3]
            explanation: [brief explanation of the solution]
            conceptFocus: ${request.conceptFocus}
            difficultyAchieved: ${difficulty.name.lowercase()}
            
            EXAMPLE FORMAT:
            questionText: Find x when 3x + 5 = 14
            option1: x = 3
            option2: x = 4  
            option3: x = 2
            option4: x = 5
            correctAnswerIndex: 0
            explanation: Subtract 5 from both sides to get 3x = 9, then divide by 3
            conceptFocus: ${request.conceptFocus}
            difficultyAchieved: ${difficulty.name.lowercase()}
            
            IMPORTANT RULES:
            - Make sure your answer is mathematically correct
            - Use different numbers than the original
            - Keep the same mathematical concept
            - Do not use colons in your answer choices
            - Follow the exact format above

        """.trimIndent()
    }
    
    /**
     * Create an example question showing the desired format.
     */
    private fun createExampleQuestion(original: Exercise, difficulty: RelativeDifficulty, language: SupportedLanguage): String {
        val baseQuestion = original.questionText.get(language)
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
    private fun getExampleOption(original: Exercise, index: Int, language: SupportedLanguage): String {
        val questionText = original.questionText.get(language)
        val baseOptions = when {
            questionText.contains("slope") -> listOf("2", "3", "-2", "-3")
            questionText.contains("vertex") -> listOf("(2, -1)", "(2, 1)", "(-2, -1)", "(-2, 1)")
            questionText.contains("factor") -> listOf("(x + 2)(x + 3)", "(x + 1)(x + 6)", "(x - 2)(x - 3)", "(x - 1)(x - 6)")
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
    fun extractConceptFocus(exercise: Exercise, language: SupportedLanguage): String {
        val questionText = exercise.questionText.get(language).lowercase()
        
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