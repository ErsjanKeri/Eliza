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

package com.example.ai.edge.eliza.core.data.repository

// Interface only - no protobuf dependencies here
// Implementation will be in app module with proper protobuf access

interface DataStoreRepository {
  fun saveTextInputHistory(history: List<String>)

  fun readTextInputHistory(): List<String>

  fun saveTheme(theme: String) // Using String instead of Theme enum to avoid protobuf dependency

  fun readTheme(): String

  fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long)

  fun clearAccessTokenData()

  fun readAccessTokenData(): AccessTokenData?

  fun saveImportedModels(importedModels: List<String>) // Using String (JSON) to avoid protobuf dependency

  fun readImportedModels(): List<String>

  // User Learning Preferences
  fun saveUserPreferences(preferences: String) // Using JSON string to avoid protobuf dependency

  fun readUserPreferences(): String? // Returns JSON string or null if not set
}

// Authentication token types (OAuth removed - using direct API tokens)
enum class TokenStatus {
  NOT_STORED,
  EXPIRED,
  NOT_EXPIRED,
}

enum class TokenRequestResultType {
  FAILED,
  SUCCEEDED,
  USER_CANCELLED,
}

data class AccessTokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMs: Long
)

data class TokenStatusAndData(val status: TokenStatus, val data: AccessTokenData?)

data class TokenRequestResult(val status: TokenRequestResultType, val errorMessage: String? = null)