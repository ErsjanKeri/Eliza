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

package com.example.ai.edge.eliza.data

import androidx.datastore.core.DataStore
import com.example.ai.edge.eliza.proto.ImportedModel
import com.example.ai.edge.eliza.proto.ElizaSettings
import com.example.ai.edge.eliza.proto.Theme
import com.example.ai.edge.eliza.core.data.repository.DataStoreRepository
import com.example.ai.edge.eliza.core.data.repository.AccessTokenData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Repository for managing data using Proto DataStore . */
class DefaultDataStoreRepository(private val dataStore: DataStore<ElizaSettings>) : DataStoreRepository {
  override fun saveTextInputHistory(history: List<String>) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().clearTextInputHistory().addAllTextInputHistory(history).build()
      }
    }
  }

  override fun readTextInputHistory(): List<String> {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.textInputHistoryList
    }
  }

  override fun saveTheme(theme: String) {
    val protoTheme = when (theme) {
      "LIGHT" -> Theme.THEME_LIGHT
      "DARK" -> Theme.THEME_DARK
      "AUTO" -> Theme.THEME_AUTO
      else -> Theme.THEME_AUTO
    }
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setTheme(protoTheme).build() }
    }
  }

  override fun readTheme(): String {
    return runBlocking {
      val settings = dataStore.data.first()
      val curTheme = settings.theme
      // Use "auto" as the default theme.
      val theme = if (curTheme == Theme.THEME_UNSPECIFIED) Theme.THEME_AUTO else curTheme
      when (theme) {
        Theme.THEME_LIGHT -> "LIGHT"
        Theme.THEME_DARK -> "DARK"
        Theme.THEME_AUTO -> "AUTO"
        else -> "AUTO"
      }
    }
  }

  override fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long) {
    runBlocking {
      dataStore.updateData { settings ->
        settings
          .toBuilder()
          .setAccessTokenData(
            com.example.ai.edge.eliza.proto.AccessTokenData.newBuilder()
              .setAccessToken(accessToken)
              .setRefreshToken(refreshToken)
              .setExpiresAtMs(expiresAt)
              .build()
          )
          .build()
      }
    }
  }

  override fun clearAccessTokenData() {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().clearAccessTokenData().build() }
    }
  }

  override fun readAccessTokenData(): AccessTokenData? {
    return runBlocking {
      val settings = dataStore.data.first()
      if (settings.hasAccessTokenData()) {
        val protoData = settings.accessTokenData
        AccessTokenData(
          accessToken = protoData.accessToken,
          refreshToken = protoData.refreshToken,
          expiresAtMs = protoData.expiresAtMs
        )
      } else null
    }
  }

  override fun saveImportedModels(importedModels: List<String>) {
    // Note: This interface uses List<String> for cross-module compatibility
    // but we'll implement proper protobuf storage when imported models are actually used
    runBlocking {
      dataStore.updateData { settings ->
        // For now, we don't store imported models as Eliza focuses on predefined models
        // This can be implemented when user model import feature is added
        settings
      }
    }
  }

  override fun readImportedModels(): List<String> {
    return runBlocking {
      // Return empty list as Eliza currently doesn't support user imported models
      // This maintains interface compatibility while avoiding unnecessary storage
      emptyList()
    }
  }

  override fun saveUserPreferences(preferences: String) {
    runBlocking {
      dataStore.updateData { settings ->
        val preferencesProto = com.example.ai.edge.eliza.proto.UserLearningPreferences.newBuilder()
        
        // Parse JSON string and convert to proto
        try {
          // Simple delimiter-based approach: experienceLevel|subjects|timeHours|goals|language
          val parts = preferences.split("|")
          if (parts.size >= 5) {
            if (parts[0].isNotEmpty()) preferencesProto.experienceLevel = parts[0]
            if (parts[1].isNotEmpty()) preferencesProto.addAllPreferredSubjects(parts[1].split(",").filter { it.isNotEmpty() })
            if (parts[2].isNotEmpty()) preferencesProto.availableTimeHours = parts[2].toIntOrNull() ?: 0
            if (parts[3].isNotEmpty()) preferencesProto.addAllLearningGoals(parts[3].split(",").filter { it.isNotEmpty() })
            if (parts[4].isNotEmpty()) preferencesProto.language = parts[4]
          } else if (parts.size >= 4) {
            // Backwards compatibility for old format without language
            if (parts[0].isNotEmpty()) preferencesProto.experienceLevel = parts[0]
            if (parts[1].isNotEmpty()) preferencesProto.addAllPreferredSubjects(parts[1].split(",").filter { it.isNotEmpty() })
            if (parts[2].isNotEmpty()) preferencesProto.availableTimeHours = parts[2].toIntOrNull() ?: 0
            if (parts[3].isNotEmpty()) preferencesProto.addAllLearningGoals(parts[3].split(",").filter { it.isNotEmpty() })
            preferencesProto.language = "english" // Default to English for backwards compatibility
          }
          preferencesProto.lastUpdated = System.currentTimeMillis()
        } catch (e: Exception) {
          // If parsing fails, create empty preferences with default language
          preferencesProto.isFirstTime = true
          preferencesProto.language = "english"
          preferencesProto.lastUpdated = System.currentTimeMillis()
        }
        
        settings.toBuilder()
          .setUserLearningPreferences(preferencesProto.build())
          .build()
      }
    }
  }

  override fun readUserPreferences(): String? {
    return runBlocking {
      val settings = dataStore.data.first()
      if (settings.hasUserLearningPreferences()) {
        val prefs = settings.userLearningPreferences
        // Convert proto to simple string format
        // Format: experienceLevel|subjects|timeHours|goals|language
        val subjects = prefs.preferredSubjectsList.joinToString(",")
        val goals = prefs.learningGoalsList.joinToString(",")
        val language = if (prefs.language.isNotEmpty()) prefs.language else "english"
        "${prefs.experienceLevel}|$subjects|${prefs.availableTimeHours}|$goals|$language"
      } else {
        null
      }
    }
  }
}