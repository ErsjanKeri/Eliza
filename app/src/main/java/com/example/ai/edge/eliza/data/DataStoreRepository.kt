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

/** Repository for managing data using Proto DataStore (copied exactly from Gallery). */
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
}