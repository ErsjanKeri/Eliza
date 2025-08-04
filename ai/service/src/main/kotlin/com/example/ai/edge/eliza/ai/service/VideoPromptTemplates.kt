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
     */
    fun createChapterVideoPrompt(
        userQuestion: String,
        context: ChatContext.ChapterReading
    ): String {
        return """
            Create an educational video explanation for this question about "${context.chapterTitle}":
            
            STUDENT QUESTION: "$userQuestion"
            
            EDUCATIONAL CONTEXT:
            - Course: ${context.courseTitle} (${context.courseSubject}, Grade ${context.courseGrade})
            - Chapter: ${context.chapterTitle} (Chapter ${context.chapterNumber} of ${context.totalChapters})
            - Progress: ${context.completedChapters}/${context.totalChapters} chapters completed
            ${if (context.currentSection != null) "- Current Section: ${context.currentSection}" else ""}
            
            CHAPTER CONTENT OVERVIEW:
            ${getChapterContentSummary(context.markdownContent)}
            
            VIDEO REQUIREMENTS:
            - Create a clear, step-by-step visual explanation
            - Focus on the specific concept the student asked about
            - Connect to the broader chapter themes
            - Use visual animations to illustrate mathematical concepts
            - Keep explanations at ${context.courseGrade} grade level
            - Include concrete examples and practice problems
            - Duration: 30-60 seconds for focused explanation
            
            Create an engaging educational video that helps the student understand this concept within the context of their current chapter study.
        """.trimIndent()
    }
    
    /**
     * Create a video explanation prompt for exercise solving context.
     * Transforms rich exercise context into a natural language prompt for ElizaServer.
     */
    fun createExerciseVideoPrompt(
        userQuestion: String,
        context: ChatContext.ExerciseSolving
    ): String {
        return """
            Create an educational video explanation for this exercise problem:
            
            STUDENT QUESTION: "$userQuestion"
            
            EXERCISE CONTEXT:
            - Course: ${context.courseTitle} (${context.courseSubject})
            - Chapter: ${context.chapterTitle}
            - Exercise #${context.exerciseNumber}
            
            PROBLEM DETAILS:
            Question: "${context.questionText}"
            
            Multiple Choice Options:
            A) ${context.options.getOrNull(0) ?: "Option A"}
            B) ${context.options.getOrNull(1) ?: "Option B"}
            C) ${context.options.getOrNull(2) ?: "Option C"}
            D) ${context.options.getOrNull(3) ?: "Option D"}
            
            ${if (context.userAnswer != null && context.correctAnswer != null) {
                """
                STUDENT'S PERFORMANCE:
                Student Selected: ${getOptionLetter(context.userAnswerIndex)} ${context.userAnswer}
                Correct Answer: ${getOptionLetter(context.correctAnswerIndex)} ${context.correctAnswer}
                Result: ${if (context.userAnswer == context.correctAnswer) "CORRECT" else "INCORRECT"}
                Attempts: ${context.attempts}
                """.trimIndent()
            } else {
                "CONTEXT: Student is asking for help with this problem"
            }}
            
            VIDEO REQUIREMENTS:
            - Create a step-by-step visual solution
            ${if (context.userAnswer != context.correctAnswer && context.userAnswer != null) {
                "- Explain why option ${getOptionLetter(context.userAnswerIndex)} (${context.userAnswer}) is incorrect"
            } else ""}
            - Show the complete solution process leading to option ${getOptionLetter(context.correctAnswerIndex)} (${context.correctAnswer})
            - Use visual animations to demonstrate mathematical steps
            - Highlight key concepts from the chapter content
            - Include similar practice examples if helpful
            - Duration: 45-90 seconds for complete explanation
            
            Create an engaging educational video that helps the student understand both the specific problem and the underlying mathematical concepts.
        """.trimIndent()
    }
    
    /**
     * Create a general video prompt when no specific context is available.
     * Fallback for direct video requests without chapter/exercise context.
     */
    fun createGeneralVideoPrompt(userQuestion: String): String {
        return """
            Create an educational video explanation for this mathematical question:
            
            STUDENT QUESTION: "$userQuestion"
            
            VIDEO REQUIREMENTS:
            - Provide a clear, step-by-step explanation
            - Use visual animations to illustrate concepts
            - Include concrete examples and demonstrations
            - Make the explanation accessible and engaging
            - Duration: 30-60 seconds for focused explanation
            
            Create an educational video that helps the student understand this mathematical concept clearly.
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