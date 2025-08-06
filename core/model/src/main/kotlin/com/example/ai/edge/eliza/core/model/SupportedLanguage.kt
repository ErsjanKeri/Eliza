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

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Supported languages in the ELIZA learning platform.
 * 
 * Each language includes:
 * - code: ISO 639-1 language code
 * - displayName: English name for the language (for settings)
 * - nativeName: Native name for the language (for user selection)
 */
@Serializable
enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    ALBANIAN("sq", "Albanian", "Shqip"),
    GERMAN("de", "German", "Deutsch");
    
    companion object {
        /**
         * Default language for fallback scenarios.
         * User can change their preferred language in settings, but this serves as the system default.
         */
        val DEFAULT = ENGLISH
        
        /**
         * All supported language codes as a list for validation.
         */
        val SUPPORTED_CODES = values().map { it.code }
        
        /**
         * Get SupportedLanguage from language code.
         * Returns DEFAULT if code is not recognized.
         */
        fun fromCode(code: String): SupportedLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
        
        /**
         * Get SupportedLanguage from old string format used in UserPreferences.
         * Provides backward compatibility.
         */
        fun fromLegacyString(legacyString: String): SupportedLanguage {
            return when (legacyString.lowercase()) {
                "english" -> ENGLISH
                "albanian" -> ALBANIAN  
                "german" -> GERMAN
                else -> DEFAULT
            }
        }
        
    }
    
    /**
     * Convert to legacy string format for backward compatibility.
     */
    fun toLegacyString(): String {
        return when (this) {
            ENGLISH -> "english"
            ALBANIAN -> "albanian"
            GERMAN -> "german"
        }
    }
}