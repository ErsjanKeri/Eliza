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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages locale changes based on user preferences.
 * 
 * This service bridges our custom SupportedLanguage enum with Android's Locale system,
 * ensuring that string resources are displayed in the user's preferred language.
 * 
 * Key Features:
 * - Converts SupportedLanguage to Android Locale
 * - Updates app configuration for immediate locale changes
 * - Provides reactive streams for locale updates
 * - Handles fallbacks for unsupported locales
 */
@Singleton
class LocaleManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    companion object {
        private const val TAG = "LocaleManager"
    }
    
    /**
     * Flow that emits the current locale based on user preferences.
     * This can be observed to react to language changes.
     */
    val currentLocale: Flow<Locale> = userPreferencesRepository.getUserPreferences()
        .map { preferences -> supportedLanguageToLocale(preferences.language) }
    
    /**
     * Get the current locale synchronously.
     * Note: This is a suspend function as it needs to access UserPreferences.
     */
    suspend fun getCurrentLocale(): Locale {
        val currentLanguage = userPreferencesRepository.getCurrentLanguage()
        return supportedLanguageToLocale(currentLanguage)
    }
    
    /**
     * Convert our SupportedLanguage enum to Android Locale.
     * Uses the language codes defined in SupportedLanguage.
     */
    fun supportedLanguageToLocale(language: SupportedLanguage): Locale {
        return when (language) {
            SupportedLanguage.ENGLISH -> Locale.ENGLISH
            SupportedLanguage.ALBANIAN -> Locale("sq", "AL") // Albanian (Albania)
            SupportedLanguage.GERMAN -> Locale.GERMAN
        }
    }
    
    /**
     * Convert Android Locale back to our SupportedLanguage enum.
     * Useful for detecting system locale changes.
     */
    fun localeToSupportedLanguage(locale: Locale): SupportedLanguage {
        return when (locale.language) {
            "en" -> SupportedLanguage.ENGLISH
            "sq" -> SupportedLanguage.ALBANIAN
            "de" -> SupportedLanguage.GERMAN
            else -> SupportedLanguage.DEFAULT
        }
    }
    
    /**
     * Update the locale for a given context.
     * This creates a new context with the updated configuration.
     * 
     * @param context The base context to update
     * @param locale The target locale
     * @return Updated context with new locale configuration
     */
    @SuppressLint("ObsoleteSdkInt") // We handle API level differences properly
    fun updateContextLocale(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            // For older Android versions, update resources directly
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Get a localized context based on user preferences.
     * This is the main method that components should use to get a properly localized context.
     */
    suspend fun getLocalizedContext(baseContext: Context): Context {
        val locale = getCurrentLocale()
        return updateContextLocale(baseContext, locale)
    }
    
    /**
     * Check if the current system locale matches user preferences.
     * Useful for determining if a locale update is needed.
     */
    suspend fun isSystemLocaleMatchingPreferences(context: Context): Boolean {
        val currentLocale = getCurrentLocale()
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return currentLocale.language == systemLocale.language
    }
}