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

package com.example.ai.edge.eliza.ai.service

import com.example.ai.edge.eliza.core.model.ChatContext

/**
 * Video explanation prompt templates that reuse existing ChatContext structures.
 * Follows the same pattern as ExercisePromptTemplates for consistency.
 * 
 * Transforms Eliza's rich educational context into natural language prompts
 * suitable for ElizaServer's simple prompt-based API.
 */
object VideoPromptTemplates {
    
    /**
     * Create a video explanation prompt for chapter reading context.
     * Transforms rich chapter context into a natural language prompt for ElizaServer.
     * Optimized to stay within 1000 character server limit.
     */
    fun createChapterVideoPrompt(
        userQuestion: String,
        context: ChatContext.ChapterReading
    ): String {
        return """
            Create a ${context.courseGrade} educational video about "${context.chapterTitle}" for this question:
            
            QUESTION: "$userQuestion"
            
            CONTEXT: ${context.courseTitle} - Chapter ${context.chapterNumber}/${context.totalChapters}
            
            REQUIREMENTS:
            - Clear step-by-step visual explanation
            - Focus on the specific concept asked
            - Use animations for mathematical concepts
            - Include concrete examples
            - 30-60 seconds duration
            
            Make it engaging and easy to understand for ${context.courseGrade} students.
        """.trimIndent()
    }
    
    /**
     * Create a video explanation prompt for exercise solving context.
     * Transforms rich exercise context into a natural language prompt for ElizaServer.
     * Optimized to stay within 1000 character server limit.
     */
    fun createExerciseVideoPrompt(
        userQuestion: String,
        context: ChatContext.ExerciseSolving
    ): String {
        return """
            Create an educational video for Exercise #${context.exerciseNumber} from ${context.chapterTitle}:
            
            QUESTION: "$userQuestion"
            
            PROBLEM: "${context.questionText}"
            Options: A) ${context.options.getOrNull(0)} B) ${context.options.getOrNull(1)} C) ${context.options.getOrNull(2)} D) ${context.options.getOrNull(3)}
            
            ${if (context.userAnswer != null && context.correctAnswer != null) {
                "Student chose ${getOptionLetter(context.userAnswerIndex)} but correct is ${getOptionLetter(context.correctAnswerIndex)}."
            } else {
                "Student needs help solving this."
            }}
            
            REQUIREMENTS:
            - Step-by-step visual solution
            - Explain why correct answer is right
            ${if (context.userAnswer != context.correctAnswer && context.userAnswer != null) "- Show why ${getOptionLetter(context.userAnswerIndex)} is wrong" else ""}
            - Use clear animations
            - 45-90 seconds
            
            Make it engaging and educational.
        """.trimIndent()
    }
    
    /**
     * Create a general video prompt when no specific context is available.
     * Fallback for direct video requests without chapter/exercise context.
     * Optimized to stay within 1000 character server limit.
     */
    fun createGeneralVideoPrompt(userQuestion: String): String {
        return """
            Create an educational video for this question:
            QUESTION: "$userQuestion"
        """.trimIndent()
    }
    
    /**
     * Extract a summary of chapter content for context inclusion.
     * Limits content length to avoid overwhelming the video prompt.
     */
    private fun getChapterContentSummary(markdownContent: String?): String {
        if (markdownContent.isNullOrBlank()) {
            return "Chapter content available for reference during video creation."
        }
        
        // Extract key sections and limit to manageable size for video prompt
        val lines = markdownContent.lines()
        val keyPoints = lines
            .filter { line -> 
                line.startsWith("#") || // Headers
                line.startsWith("**") || // Bold text (key concepts)
                line.contains("example", ignoreCase = true) || // Examples
                line.contains("formula", ignoreCase = true) // Formulas
            }
            .take(5) // Limit to avoid prompt overflow
            .joinToString("\n")
            
        return if (keyPoints.isNotBlank()) {
            "Key concepts to reference:\n$keyPoints"
        } else {
            "Full chapter content available for reference during video creation."
        }
    }
    
    /**
     * Convert option index to letter (0 -> A, 1 -> B, etc.).
     */
    private fun getOptionLetter(index: Int?): String {
        return when (index) {
            0 -> "A)"
            1 -> "B)"
            2 -> "C)"
            3 -> "D)"
            else -> "?"
        }
    }
}