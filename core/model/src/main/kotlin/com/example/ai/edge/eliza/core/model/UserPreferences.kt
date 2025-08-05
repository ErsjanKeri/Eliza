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

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * User learning preferences for personalized course recommendations.
 * These preferences help the AI provide better-targeted suggestions.
 */
@Serializable
data class UserPreferences(
    val experienceLevel: String? = null, // "beginner", "intermediate", "advanced"
    val preferredSubjects: List<String> = emptyList(), // e.g., ["mathematics", "physics"]
    val availableTimeHours: Int? = null, // Hours per week available for study
    val learningGoals: List<String> = emptyList(), // User-defined learning objectives
    val preferredDifficulty: String? = null, // "easy", "moderate", "challenging"
    val studySchedule: String? = null, // "morning", "afternoon", "evening", "flexible"
    val language: String = "english", // "albanian", "german", "english" - default to English
    val isFirstTime: Boolean = true, // Whether user has set preferences before
    val lastUpdated: Long = System.currentTimeMillis() // When preferences were last modified
) {
    companion object {
        val EXPERIENCE_LEVELS = listOf("beginner", "intermediate", "advanced")
        val COMMON_SUBJECTS = listOf(
            "mathematics", 
            "physics", 
            "chemistry", 
            "biology", 
            "computer science", 
            "engineering",
            "statistics",
            "calculus",
            "algebra",
            "geometry"
        )
        val DIFFICULTY_LEVELS = listOf("easy", "moderate", "challenging")
        val STUDY_SCHEDULES = listOf("morning", "afternoon", "evening", "flexible")
        val SUPPORTED_LANGUAGES = listOf("albanian", "german", "english")
        val DEFAULT_LANGUAGE = "english"
        
        /**
         * Create default preferences for first-time users.
         */
        fun createDefault(): UserPreferences {
            return UserPreferences(
                language = DEFAULT_LANGUAGE,
                isFirstTime = true,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        /**
         * Create preferences from user input.
         */
        fun create(
            experienceLevel: String? = null,
            preferredSubjects: List<String> = emptyList(),
            availableTimeHours: Int? = null,
            learningGoals: List<String> = emptyList(),
            preferredDifficulty: String? = null,
            studySchedule: String? = null,
            language: String = DEFAULT_LANGUAGE
        ): UserPreferences {
            return UserPreferences(
                experienceLevel = experienceLevel,
                preferredSubjects = preferredSubjects,
                availableTimeHours = availableTimeHours,
                learningGoals = learningGoals,
                preferredDifficulty = preferredDifficulty,
                studySchedule = studySchedule,
                language = language,
                isFirstTime = false,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Check if user has set any meaningful preferences.
     */
    val hasPreferences: Boolean
        get() = experienceLevel != null || 
                preferredSubjects.isNotEmpty() || 
                availableTimeHours != null ||
                learningGoals.isNotEmpty()
    
    /**
     * Get a summary string of current preferences for display.
     */
    val summary: String
        get() = buildString {
            append("Language: ${language.replaceFirstChar { it.uppercase() }}")
            experienceLevel?.let { 
                append(" • Level: $it") 
            }
            if (preferredSubjects.isNotEmpty()) {
                append(" • Subjects: ${preferredSubjects.take(2).joinToString(", ")}")
                if (preferredSubjects.size > 2) append(" +${preferredSubjects.size - 2} more")
            }
            availableTimeHours?.let { 
                append(" • Time: ${it}h/week") 
            }
            if (language == DEFAULT_LANGUAGE && !hasPreferences) {
                append(" • No learning preferences set")
            }
        }
    
    /**
     * Create an updated copy of preferences.
     */
    fun update(
        experienceLevel: String? = this.experienceLevel,
        preferredSubjects: List<String> = this.preferredSubjects,
        availableTimeHours: Int? = this.availableTimeHours,
        learningGoals: List<String> = this.learningGoals,
        preferredDifficulty: String? = this.preferredDifficulty,
        studySchedule: String? = this.studySchedule,
        language: String = this.language
    ): UserPreferences {
        return copy(
            experienceLevel = experienceLevel,
            preferredSubjects = preferredSubjects,
            availableTimeHours = availableTimeHours,
            learningGoals = learningGoals,
            preferredDifficulty = preferredDifficulty,
            studySchedule = studySchedule,
            language = language,
            isFirstTime = false,
            lastUpdated = System.currentTimeMillis()
        )
    }
}