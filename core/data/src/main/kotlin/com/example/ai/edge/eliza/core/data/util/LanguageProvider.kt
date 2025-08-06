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

package com.example.ai.edge.eliza.core.data.util

import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.model.LocalizedContent
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central service for managing user's language preference throughout the app.
 * 
 * This ensures that:
 * - All content is displayed in the user's preferred language
 * - RAG processing uses the user's language for better context
 * - AI prompts and responses are in the user's language
 * - Fallback to English only when content is not available in user's language
 * 
 * Key principle: User's language is ALWAYS preferred, English is only fallback.
 */
@Singleton
class LanguageProvider @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Get the current user's language as a Flow.
     * This is the PRIMARY source of truth for language throughout the app.
     */
    val currentLanguage: Flow<SupportedLanguage> = 
        userPreferencesRepository.getUserPreferences()
            .map { it.language }
    
    /**
     * Get the current user's language synchronously.
     * Use this for immediate language resolution in non-reactive contexts.
     */
    suspend fun getCurrentLanguage(): SupportedLanguage {
        return userPreferencesRepository.getCurrentLanguage()
    }
    
    /**
     * Localize content to the user's current language.
     * Returns a Flow that updates when user changes language.
     */
    fun localizeContent(content: LocalizedContent): Flow<String> {
        return currentLanguage.map { language -> 
            content.get(language) 
        }
    }
    
    /**
     * Localize content to the user's current language synchronously.
     * Use this for immediate content resolution.
     */
    suspend fun localizeContentSync(content: LocalizedContent): String {
        val language = getCurrentLanguage()
        return content.get(language)
    }
    
    /**
     * Get content in user's language for RAG processing.
     * This ensures RAG works with content in the user's preferred language.
     */
    suspend fun getContentForRAG(content: LocalizedContent): String {
        return localizeContentSync(content)
    }
    
    /**
     * Get list content in user's language for RAG processing.
     * Maps all LocalizedContent items to user's language.
     */
    suspend fun getContentListForRAG(contentList: List<LocalizedContent>): List<String> {
        val language = getCurrentLanguage()
        return contentList.map { it.get(language) }
    }
    
    /**
     * Check if content is available in user's language.
     * Returns true if content exists, false if it would fallback to English.
     */
    suspend fun isContentAvailableInUserLanguage(content: LocalizedContent): Boolean {
        val language = getCurrentLanguage()
        return content.hasLanguage(language)
    }
    
    /**
     * Get language code for external APIs (e.g., translation services).
     */
    suspend fun getLanguageCode(): String {
        return getCurrentLanguage().code
    }
    
    /**
     * Get language display name for UI.
     */
    suspend fun getLanguageDisplayName(): String {
        return getCurrentLanguage().displayName
    }
    
    /**
     * Get native language name for language selection UI.
     */
    suspend fun getLanguageNativeName(): String {
        return getCurrentLanguage().nativeName
    }
}