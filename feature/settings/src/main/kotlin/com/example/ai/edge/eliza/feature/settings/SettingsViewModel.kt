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

package com.example.ai.edge.eliza.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.model.UserPreferences
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences.createDefault(),
    val availableLanguages: List<SupportedLanguage> = UserPreferences.SUPPORTED_LANGUAGES,
    val availableExperienceLevels: List<String> = UserPreferences.EXPERIENCE_LEVELS,
    val availableSubjects: List<String> = UserPreferences.COMMON_SUBJECTS,
    val availableDifficultyLevels: List<String> = UserPreferences.DIFFICULTY_LEVELS,
    val availableStudySchedules: List<String> = UserPreferences.STUDY_SCHEDULES,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserPreferences()
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collectLatest { preferences ->
                _uiState.value = _uiState.value.copy(
                    userPreferences = preferences,
                    isLoading = false
                )
            }
        }
    }

    fun updateLanguage(language: SupportedLanguage) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                userPreferencesRepository.updateLanguage(language)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update language: ${e.message}"
                )
            }
        }
    }

    fun updateExperienceLevel(level: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    experienceLevel = level,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update experience level: ${e.message}"
                )
            }
        }
    }

    fun updatePreferredSubjects(subjects: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    preferredSubjects = subjects,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update subjects: ${e.message}"
                )
            }
        }
    }

    fun updateAvailableTimeHours(hours: Int?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    availableTimeHours = hours,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update available time: ${e.message}"
                )
            }
        }
    }

    fun updateLearningGoals(goals: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    learningGoals = goals,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update learning goals: ${e.message}"
                )
            }
        }
    }

    fun updatePreferredDifficulty(difficulty: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    preferredDifficulty = difficulty,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update difficulty: ${e.message}"
                )
            }
        }
    }

    fun updateStudySchedule(schedule: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val updatedPreferences = _uiState.value.userPreferences.copy(
                    studySchedule = schedule,
                    lastUpdated = System.currentTimeMillis()
                )
                userPreferencesRepository.saveUserPreferences(updatedPreferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update study schedule: ${e.message}"
                )
            }
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                userPreferencesRepository.resetToDefaults()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to reset preferences: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}