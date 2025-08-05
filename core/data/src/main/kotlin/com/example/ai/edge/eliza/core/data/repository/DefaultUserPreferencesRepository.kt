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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of UserPreferencesRepository.
 * Uses DataStoreRepository for persistence and provides reactive access via Flow.
 * 
 * This implementation maintains a local cache for performance and provides
 * real-time updates to consumers via StateFlow.
 */
@Singleton
class DefaultUserPreferencesRepository @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : UserPreferencesRepository {
    
    private val mutex = Mutex()
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    private val userPreferencesFlow: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()
    
    override fun getUserPreferences(): Flow<UserPreferences> {
        return kotlinx.coroutines.flow.flow {
            // Load preferences if not already loaded
            if (_userPreferences.value == null) {
                loadPreferences()
            }
            
            // Emit current preferences and listen for updates
            userPreferencesFlow.collect { preferences ->
                emit(preferences ?: UserPreferences.createDefault())
            }
        }
    }
    
    override suspend fun getCurrentUserPreferences(): UserPreferences {
        return mutex.withLock {
            _userPreferences.value ?: run {
                loadPreferences()
                _userPreferences.value ?: UserPreferences.createDefault()
            }
        }
    }
    
    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        mutex.withLock {
            val preferencesString = formatPreferencesForStorage(preferences)
            dataStoreRepository.saveUserPreferences(preferencesString)
            _userPreferences.value = preferences
        }
    }
    
    override suspend fun updateUserPreferences(
        experienceLevel: String?,
        preferredSubjects: List<String>?,
        availableTimeHours: Int?,
        learningGoals: List<String>?,
        preferredDifficulty: String?,
        studySchedule: String?,
        language: String?
    ) {
        val currentPreferences = getCurrentUserPreferences()
        val updatedPreferences = currentPreferences.update(
            experienceLevel = experienceLevel ?: currentPreferences.experienceLevel,
            preferredSubjects = preferredSubjects ?: currentPreferences.preferredSubjects,
            availableTimeHours = availableTimeHours ?: currentPreferences.availableTimeHours,
            learningGoals = learningGoals ?: currentPreferences.learningGoals,
            preferredDifficulty = preferredDifficulty ?: currentPreferences.preferredDifficulty,
            studySchedule = studySchedule ?: currentPreferences.studySchedule,
            language = language ?: currentPreferences.language
        )
        saveUserPreferences(updatedPreferences)
    }
    
    override suspend fun getCurrentLanguage(): String {
        return getCurrentUserPreferences().language
    }
    
    override suspend fun updateLanguage(language: String) {
        updateUserPreferences(language = language)
    }
    
    override suspend fun updateLearningPreferences(
        experienceLevel: String?,
        preferredSubjects: List<String>?,
        availableTimeHours: Int?,
        learningGoals: List<String>?,
        preferredDifficulty: String?,
        studySchedule: String?
    ) {
        updateUserPreferences(
            experienceLevel = experienceLevel,
            preferredSubjects = preferredSubjects,
            availableTimeHours = availableTimeHours,
            learningGoals = learningGoals,
            preferredDifficulty = preferredDifficulty,
            studySchedule = studySchedule
        )
    }
    
    override suspend fun resetToDefaults() {
        saveUserPreferences(UserPreferences.createDefault())
    }
    
    override suspend fun hasUserSetPreferences(): Boolean {
        val preferences = getCurrentUserPreferences()
        return preferences.hasPreferences && !preferences.isFirstTime
    }
    
    override suspend fun getPreferencesForAI(): String {
        val preferences = getCurrentUserPreferences()
        return buildString {
            append("User Preferences:")
            append("\nLanguage: ${preferences.language}")
            
            preferences.experienceLevel?.let { 
                append("\nExperience Level: $it") 
            }
            
            if (preferences.preferredSubjects.isNotEmpty()) {
                append("\nPreferred Subjects: ${preferences.preferredSubjects.joinToString(", ")}")
            }
            
            preferences.availableTimeHours?.let { 
                append("\nAvailable Study Time: ${it} hours per week") 
            }
            
            if (preferences.learningGoals.isNotEmpty()) {
                append("\nLearning Goals: ${preferences.learningGoals.joinToString(", ")}")
            }
            
            preferences.preferredDifficulty?.let { 
                append("\nPreferred Difficulty: $it") 
            }
            
            preferences.studySchedule?.let { 
                append("\nPreferred Study Schedule: $it") 
            }
            
            if (!preferences.hasPreferences) {
                append("\n(User has not set detailed learning preferences yet)")
            }
        }
    }
    
    /**
     * Load preferences from DataStore.
     */
    private suspend fun loadPreferences() {
        val preferencesString = dataStoreRepository.readUserPreferences()
        val preferences = if (preferencesString != null) {
            parsePreferencesFromStorage(preferencesString)
        } else {
            UserPreferences.createDefault()
        }
        _userPreferences.value = preferences
    }
    
    /**
     * Convert UserPreferences to storage format.
     * Format: experienceLevel|subjects|timeHours|goals|language
     */
    private fun formatPreferencesForStorage(preferences: UserPreferences): String {
        val experienceLevel = preferences.experienceLevel ?: ""
        val subjects = preferences.preferredSubjects.joinToString(",")
        val timeHours = preferences.availableTimeHours?.toString() ?: ""
        val goals = preferences.learningGoals.joinToString(",")
        val language = preferences.language
        
        return "$experienceLevel|$subjects|$timeHours|$goals|$language"
    }
    
    /**
     * Parse UserPreferences from storage format.
     * Format: experienceLevel|subjects|timeHours|goals|language
     */
    private fun parsePreferencesFromStorage(preferencesString: String): UserPreferences {
        return try {
            val parts = preferencesString.split("|")
            if (parts.size >= 5) {
                UserPreferences.create(
                    experienceLevel = parts[0].takeIf { it.isNotEmpty() },
                    preferredSubjects = parts[1].split(",").filter { it.isNotEmpty() },
                    availableTimeHours = parts[2].takeIf { it.isNotEmpty() }?.toIntOrNull(),
                    learningGoals = parts[3].split(",").filter { it.isNotEmpty() },
                    language = parts[4].takeIf { it.isNotEmpty() } ?: UserPreferences.DEFAULT_LANGUAGE
                )
            } else if (parts.size >= 4) {
                // Backwards compatibility for old format without language
                UserPreferences.create(
                    experienceLevel = parts[0].takeIf { it.isNotEmpty() },
                    preferredSubjects = parts[1].split(",").filter { it.isNotEmpty() },
                    availableTimeHours = parts[2].takeIf { it.isNotEmpty() }?.toIntOrNull(),
                    learningGoals = parts[3].split(",").filter { it.isNotEmpty() },
                    language = UserPreferences.DEFAULT_LANGUAGE
                )
            } else {
                UserPreferences.createDefault()
            }
        } catch (e: Exception) {
            // If parsing fails, return default preferences
            UserPreferences.createDefault()
        }
    }
}