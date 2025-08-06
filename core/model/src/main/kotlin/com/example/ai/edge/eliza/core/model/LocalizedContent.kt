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
 * Container for content that supports multiple languages.
 * 
 * Design principles:
 * - English (en) is always required and serves as the fallback
 * - Other languages are optional
 * - If requested language is not available, falls back to English
 * - Supports future RTL languages without breaking changes
 * 
 * Usage:
 * ```
 * val title = LocalizedContent(
 *     en = "Linear Equations",
 *     sq = "Ekuacionet Lineare", 
 *     de = "Lineare Gleichungen"
 * )
 * 
 * val userLanguage = SupportedLanguage.ALBANIAN
 * val displayText = title.get(userLanguage) // Returns "Ekuacionet Lineare"
 * ```
 */
@Serializable
data class LocalizedContent(
    val en: String,           // English - required, serves as fallback
    val sq: String? = null,   // Albanian - optional
    val de: String? = null    // German - optional
) {
    /**
     * Get content in the specified language.
     * Falls back to English if the requested language is not available.
     */
    fun get(language: SupportedLanguage): String {
        return when (language) {
            SupportedLanguage.ENGLISH -> en
            SupportedLanguage.ALBANIAN -> sq ?: en  // Fallback to English
            SupportedLanguage.GERMAN -> de ?: en    // Fallback to English
        }
    }
    
    /**
     * Check if content is available in the specified language.
     */
    fun hasLanguage(language: SupportedLanguage): Boolean {
        return when (language) {
            SupportedLanguage.ENGLISH -> en.isNotBlank()
            SupportedLanguage.ALBANIAN -> !sq.isNullOrBlank()
            SupportedLanguage.GERMAN -> !de.isNullOrBlank()
        }
    }
    
    /**
     * Get all available languages for this content.
     */
    fun getAvailableLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().filter { hasLanguage(it) }
    }
    
    /**
     * Create a copy with updated content for a specific language.
     */
    fun withLanguage(language: SupportedLanguage, content: String): LocalizedContent {
        return when (language) {
            SupportedLanguage.ENGLISH -> copy(en = content)
            SupportedLanguage.ALBANIAN -> copy(sq = content)
            SupportedLanguage.GERMAN -> copy(de = content)
        }
    }
    
    companion object {
        /**
         * Create LocalizedContent with only English text.
         * Useful for migration from existing string-based content.
         */
        fun englishOnly(content: String): LocalizedContent {
            return LocalizedContent(en = content)
        }
        
        /**
         * Create empty LocalizedContent (for testing or placeholders).
         */
        fun empty(): LocalizedContent {
            return LocalizedContent(en = "")
        }
        
        /**
         * Create LocalizedContent with all three supported languages.
         * Validates that English content is not blank.
         */
        fun create(
            english: String,
            albanian: String? = null,
            german: String? = null
        ): LocalizedContent {
            require(english.isNotBlank()) { "English content cannot be blank" }
            return LocalizedContent(
                en = english,
                sq = albanian?.takeIf { it.isNotBlank() },
                de = german?.takeIf { it.isNotBlank() }
            )
        }
    }
}

/**
 * Extension functions for working with lists of LocalizedContent.
 */

/**
 * Get all content in the specified language.
 * Useful for converting lists of LocalizedContent to lists of strings.
 */
fun List<LocalizedContent>.getAll(language: SupportedLanguage): List<String> {
    return map { it.get(language) }
}

/**
 * Check if all content in the list is available in the specified language.
 */
fun List<LocalizedContent>.allAvailableIn(language: SupportedLanguage): Boolean {
    return all { it.hasLanguage(language) }
}

/**
 * Get content that is available in the specified language.
 * Returns pairs of (index, content) for items that have the language.
 */
fun List<LocalizedContent>.getAvailableIn(language: SupportedLanguage): List<Pair<Int, String>> {
    return mapIndexedNotNull { index, content ->
        if (content.hasLanguage(language)) {
            index to content.get(language)
        } else null
    }
}