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

package com.example.ai.edge.eliza.core.data.repository

import com.example.ai.edge.eliza.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user learning preferences and application settings.
 * This provides a clean abstraction layer over DataStore for user preferences management.
 * 
 * Handles both learning preferences (experience level, subjects, time, goals) and 
 * application settings (language selection for AI responses and UI).
 */
interface UserPreferencesRepository {
    
    // User Preferences Access
    /**
     * Get user preferences as a reactive Flow.
     * Returns default preferences if none are set.
     */
    fun getUserPreferences(): Flow<UserPreferences>
    
    /**
     * Get current user preferences synchronously.
     * Returns default preferences if none are set.
     */
    suspend fun getCurrentUserPreferences(): UserPreferences
    
    // User Preferences Updates
    /**
     * Save complete user preferences.
     */
    suspend fun saveUserPreferences(preferences: UserPreferences)
    
    /**
     * Update user preferences using builder pattern.
     */
    suspend fun updateUserPreferences(
        experienceLevel: String? = null,
        preferredSubjects: List<String>? = null,
        availableTimeHours: Int? = null,
        learningGoals: List<String>? = null,
        preferredDifficulty: String? = null,
        studySchedule: String? = null,
        language: String? = null
    )
    
    // Language-specific Operations
    /**
     * Get current app language setting.
     * Returns "english" if not set.
     */
    suspend fun getCurrentLanguage(): String
    
    /**
     * Update only the language preference.
     */
    suspend fun updateLanguage(language: String)
    
    // Learning Preferences Operations
    /**
     * Update only learning-related preferences.
     */
    suspend fun updateLearningPreferences(
        experienceLevel: String? = null,
        preferredSubjects: List<String>? = null,
        availableTimeHours: Int? = null,
        learningGoals: List<String>? = null,
        preferredDifficulty: String? = null,
        studySchedule: String? = null
    )
    
    // Utility Operations
    /**
     * Reset preferences to default values.
     */
    suspend fun resetToDefaults()
    
    /**
     * Check if user has set any meaningful preferences.
     */
    suspend fun hasUserSetPreferences(): Boolean
    
    /**
     * Get preferences formatted for AI prompt context.
     * Returns a structured string suitable for AI model consumption.
     */
    suspend fun getPreferencesForAI(): String
}