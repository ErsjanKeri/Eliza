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

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai.edge.eliza.core.common.R
import com.example.ai.edge.eliza.core.model.SupportedLanguage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if there's an error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetPreferences() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to defaults"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Language Section
                SettingsSection(title = stringResource(R.string.language_section)) {
                    LanguageSelector(
                        selectedLanguage = uiState.userPreferences.language,
                        availableLanguages = uiState.availableLanguages,
                        onLanguageSelected = viewModel::updateLanguage
                    )
                }

                // Learning Profile Section
                SettingsSection(title = stringResource(R.string.learning_profile_section)) {
                    // Experience Level
                    Text(
                        text = stringResource(R.string.experience_level),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.availableExperienceLevels.forEach { level ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.userPreferences.experienceLevel == level,
                                        onClick = { viewModel.updateExperienceLevel(level) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.userPreferences.experienceLevel == level,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = level.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferred Subjects
                    Text(
                        text = stringResource(R.string.preferred_subjects),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.availableSubjects.forEach { subject ->
                            FilterChip(
                                selected = uiState.userPreferences.preferredSubjects.contains(subject),
                                onClick = {
                                    val currentSubjects = uiState.userPreferences.preferredSubjects
                                    val newSubjects = if (currentSubjects.contains(subject)) {
                                        currentSubjects - subject
                                    } else {
                                        currentSubjects + subject
                                    }
                                    viewModel.updatePreferredSubjects(newSubjects)
                                },
                                label = { Text(subject.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }

                // Study Preferences Section
                SettingsSection(title = stringResource(R.string.study_preferences_section)) {
                    // Available Time
                    Text(
                        text = stringResource(R.string.available_time_hours_per_week),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var timeText by remember { 
                        mutableStateOf(uiState.userPreferences.availableTimeHours?.toString() ?: "") 
                    }
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { newValue ->
                            timeText = newValue
                            val hours = newValue.toIntOrNull()
                            viewModel.updateAvailableTimeHours(hours)
                        },
                        label = { Text(stringResource(R.string.hours_per_week)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferred Difficulty
                    Text(
                        text = stringResource(R.string.preferred_difficulty),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.availableDifficultyLevels.forEach { difficulty ->
                            FilterChip(
                                selected = uiState.userPreferences.preferredDifficulty == difficulty,
                                onClick = { viewModel.updatePreferredDifficulty(difficulty) },
                                label = { Text(difficulty.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Study Schedule
                    Text(
                        text = stringResource(R.string.preferred_study_time),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.availableStudySchedules.forEach { schedule ->
                            FilterChip(
                                selected = uiState.userPreferences.studySchedule == schedule,
                                onClick = { viewModel.updateStudySchedule(schedule) },
                                label = { Text(schedule.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }

                // Learning Goals Section
                SettingsSection(title = stringResource(R.string.learning_goals_section)) {
                    val commonGoals = listOf(
                        "Improve problem-solving skills",
                        "Prepare for exams",
                        "Learn new concepts",
                        "Review fundamentals",
                        "Advanced understanding"
                    )
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        commonGoals.forEach { goal ->
                            FilterChip(
                                selected = uiState.userPreferences.learningGoals.contains(goal),
                                onClick = {
                                    val currentGoals = uiState.userPreferences.learningGoals
                                    val newGoals = if (currentGoals.contains(goal)) {
                                        currentGoals - goal
                                    } else {
                                        currentGoals + goal
                                    }
                                    viewModel.updateLearningGoals(newGoals)
                                },
                                label = { Text(goal) }
                            )
                        }
                    }
                }

                // Reset Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reset_settings),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reset_settings_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { viewModel.resetPreferences() }
                        ) {
                            Text("Reset to Defaults")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selectedLanguage: SupportedLanguage,
    availableLanguages: List<SupportedLanguage>,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLanguage.nativeName,
            onValueChange = {},
            readOnly = true,
                                    label = { Text(stringResource(R.string.app_language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.nativeName) },
                    onClick = {
                        // Save the language preference
                        onLanguageSelected(language)
                        expanded = false
                        
                        // Recreate activity to apply new locale immediately
                        // This is necessary for Android locale changes to take effect
                        (context as? Activity)?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}