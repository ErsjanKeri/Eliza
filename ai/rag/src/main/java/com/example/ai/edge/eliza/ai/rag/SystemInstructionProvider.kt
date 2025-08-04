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

package com.example.ai.edge.eliza.ai.rag

import com.example.ai.edge.eliza.core.model.ChatContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized provider for system instructions to eliminate duplication.
 * Single source of truth for all RAG provider system instructions.
 */
@Singleton
class SystemInstructionProvider @Inject constructor() {
    
    /**
     * Get system instructions for any chat context.
     * Eliminates duplication between basic and enhanced RAG providers.
     */
    fun getSystemInstructions(context: ChatContext, isEnhancedRag: Boolean = false): String {
        return when (context) {
            is ChatContext.ExerciseSolving -> getExerciseSystemInstructions(isEnhancedRag)
            is ChatContext.ChapterReading -> getChapterReadingSystemInstructions(isEnhancedRag)
            is ChatContext.Revision -> getRevisionSystemInstructions(isEnhancedRag)
            is ChatContext.GeneralTutoring -> getGeneralTutoringSystemInstructions(isEnhancedRag)
        }
    }
    
    /**
     * System instructions for exercise solving context.
     * Shared between ExerciseRagProvider and EnhancedRagProvider.
     */
    private fun getExerciseSystemInstructions(isEnhancedRag: Boolean): String {
        val baseInstructions = """
            You are Eliza, an AI tutor helping a student with a specific exercise problem.
            
            IMPORTANT CONTEXT: You have complete information about:
            - The exact question the student is working on
            - All multiple choice options (A, B, C, D)
            - Which option the student selected (if any)
            - The correct answer
            - Whether the student's answer was right or wrong
            - The number of attempts and hints used
            
            INSTRUCTION GUIDELINES:
            - If the student got the answer wrong, explain what went wrong with their reasoning
            - If the student got it right, reinforce their understanding and explain why it's correct
            - Reference the specific options by letter (A, B, C, D) when explaining
            - Connect your explanation to the chapter content and course material
            - Provide step-by-step reasoning that leads to the correct answer
            - Encourage critical thinking rather than just giving direct answers
            - If the student asks follow-up questions, use the exercise context to provide relevant examples
        """.trimIndent()
        
        return if (isEnhancedRag) {
            baseInstructions + "\n- Use the vector-retrieved chapter content to provide deeper understanding\n\nBe encouraging, clear, and educational in your responses."
        } else {
            baseInstructions + "\n\nBe encouraging, clear, and educational in your responses."
        }
    }
    
    /**
     * System instructions for chapter reading context.
     */
    private fun getChapterReadingSystemInstructions(isEnhancedRag: Boolean): String {
        val baseInstructions = """
            You are Eliza, an AI tutor helping a student with their current chapter reading.
            The student may ask questions about the content they're studying.
            Provide clear, educational explanations that build on the chapter content.
            Use examples and step-by-step explanations when helpful.
        """.trimIndent()
        
        return if (isEnhancedRag) {
            baseInstructions + "\nUse the provided context from the chapter to give accurate, educational explanations.\nBreak down complex concepts into simple steps.\nIf the context doesn't contain enough information, acknowledge the limitation."
        } else {
            baseInstructions
        }
    }
    
    /**
     * System instructions for revision context.
     */
    private fun getRevisionSystemInstructions(isEnhancedRag: Boolean): String {
        return """
            You are Eliza, an AI tutor helping a student review previously learned material.
            Focus on connecting concepts across different chapters and identifying patterns.
            Help the student identify and strengthen their understanding of weak areas.
        """.trimIndent()
    }
    
    /**
     * System instructions for general tutoring context.
     */
    private fun getGeneralTutoringSystemInstructions(isEnhancedRag: Boolean): String {
        return """
            You are Eliza, an AI tutor providing general math tutoring.
            The student may ask questions about various math topics.
            Provide clear, step-by-step explanations and examples.
            Be patient and encouraging, adapting to the student's level.
        """.trimIndent()
    }
}