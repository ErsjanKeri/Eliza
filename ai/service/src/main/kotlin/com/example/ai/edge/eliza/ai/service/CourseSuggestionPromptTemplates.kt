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

package com.example.ai.edge.eliza.ai.service

import com.example.ai.edge.eliza.core.model.CourseSuggestionRequest

/**
 * Intelligent prompt templates for course suggestion generation with Gemma 3n.
 * Works with the SystemInstructionProvider for consistent JSON output formatting.
 * Follows the same pattern as ExercisePromptTemplates.
 */
object CourseSuggestionPromptTemplates {
    
    /**
     * Create a user-friendly prompt from the course suggestion request.
     * The SystemInstructionProvider handles the JSON format requirements.
     */
    fun createSuggestionPrompt(request: CourseSuggestionRequest): String {
        return buildString {
            appendLine("I want to learn: ${request.userQuery}")
            
            if (request.userLevel != null) {
                appendLine("My experience level: ${request.userLevel}")
            }
            
            if (request.preferredSubjects.isNotEmpty()) {
                appendLine("I'm particularly interested in: ${request.preferredSubjects.joinToString(", ")}")
            }
            
            if (request.availableTimeHours != null) {
                appendLine("I have about ${request.availableTimeHours} hours to study")
            }
            
            if (request.currentProgress.isNotEmpty()) {
                appendLine("Courses I'm currently taking or have completed: ${request.currentProgress.joinToString(", ")}")
            }
            
            appendLine()
            appendLine("Please recommend the best courses and specific chapters that will help me achieve this learning goal.")
        }
    }
    
    /**
     * Extract learning keywords from the user query for better course matching.
     * Similar to extractConceptFocus in ExercisePromptTemplates.
     */
    fun extractLearningKeywords(userQuery: String): List<String> {
        val keywords = mutableListOf<String>()
        val lowerQuery = userQuery.lowercase()
        
        // Math-related keywords
        val mathKeywords = listOf(
            "algebra", "calculus", "geometry", "trigonometry", "statistics",
            "linear", "quadratic", "polynomial", "function", "equation",
            "derivative", "integral", "matrix", "vector", "probability"
        )
        
        // Physics-related keywords  
        val physicsKeywords = listOf(
            "physics", "mechanics", "thermodynamics", "electromagnetism",
            "quantum", "relativity", "wave", "motion", "force", "energy"
        )
        
        // General learning keywords
        val learningKeywords = listOf(
            "beginner", "advanced", "intermediate", "basic", "fundamental",
            "introduction", "basics", "theory", "practical", "application"
        )
        
        // Extract subject keywords
        mathKeywords.forEach { keyword ->
            if (keyword in lowerQuery) keywords.add("mathematics")
        }
        
        physicsKeywords.forEach { keyword ->
            if (keyword in lowerQuery) keywords.add("physics")
        }
        
        learningKeywords.forEach { keyword ->
            if (keyword in lowerQuery) keywords.add(keyword)
        }
        
        // Extract specific math topics
        when {
            "linear" in lowerQuery || "equation" in lowerQuery -> keywords.add("linear_equations")
            "quadratic" in lowerQuery -> keywords.add("quadratic_equations")  
            "trig" in lowerQuery || "sin" in lowerQuery || "cos" in lowerQuery -> keywords.add("trigonometry")
            "calc" in lowerQuery || "derivative" in lowerQuery -> keywords.add("calculus")
            "geometry" in lowerQuery || "triangle" in lowerQuery -> keywords.add("geometry")
            "stat" in lowerQuery || "probability" in lowerQuery -> keywords.add("statistics")
        }
        
        return keywords.distinct()
    }
    
    /**
     * Create a summary description of the user's learning goal for context.
     */
    fun createLearningGoalSummary(request: CourseSuggestionRequest): String {
        val keywords = extractLearningKeywords(request.userQuery)
        val level = request.userLevel ?: "unspecified level"
        val subjects = if (request.preferredSubjects.isNotEmpty()) {
            request.preferredSubjects.joinToString(", ")
        } else {
            keywords.firstOrNull { it in listOf("mathematics", "physics") } ?: "general"
        }
        
        return "Student wants to learn: ${request.userQuery.take(100)} " +
                "(Level: $level, Subjects: $subjects, Keywords: ${keywords.take(3).joinToString(", ")})"
    }
    
    /**
     * Validate that a course suggestion request has sufficient information.
     */
    fun validateRequest(request: CourseSuggestionRequest): List<String> {
        val issues = mutableListOf<String>()
        
        if (request.userQuery.isBlank()) {
            issues.add("User query cannot be empty")
        }
        
        if (request.userQuery.length < 10) {
            issues.add("User query is too short for meaningful recommendations")
        }
        
        if (request.maxRecommendations < 1) {
            issues.add("Must request at least 1 course recommendation")
        }
        
        return issues
    }
    
    /**
     * Create a fallback prompt when RAG context is limited.
     */
    fun createFallbackPrompt(request: CourseSuggestionRequest): String {
        return """
            Based on the available course information, please suggest learning paths for:
            "${request.userQuery}"
            
            Consider the user's background and provide practical recommendations 
            for courses and chapters that would help them achieve this goal.
            
            Focus on providing a clear learning sequence and realistic time estimates.
        """.trimIndent()
    }
    
    /**
     * Extract difficulty level preference from user query.
     */
    fun extractDifficultyPreference(userQuery: String): String? {
        val lowerQuery = userQuery.lowercase()
        return when {
            "beginner" in lowerQuery || "start" in lowerQuery || "basic" in lowerQuery -> "beginner"
            "advanced" in lowerQuery || "expert" in lowerQuery || "master" in lowerQuery -> "advanced"
            "intermediate" in lowerQuery || "medium" in lowerQuery -> "intermediate"
            else -> null
        }
    }
}