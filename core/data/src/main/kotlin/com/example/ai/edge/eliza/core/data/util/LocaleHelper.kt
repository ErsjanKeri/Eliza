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

package com.example.ai.edge.eliza.core.data.util

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import java.util.Locale

/**
 * Lightweight locale helper that works WITHOUT Hilt dependencies.
 * 
 * This is used in attachBaseContext() where Hilt injection hasn't happened yet.
 * For full locale management with reactive updates, use LocaleManager instead.
 */
object LocaleHelper {
    
    private const val TAG = "LocaleHelper"
    private const val PREFS_NAME = "user_preferences"
    private const val LANGUAGE_KEY = "language"
    
    /**
     * Get the user's preferred locale synchronously.
     * Falls back to English if no preference is stored or on any error.
     * 
     * This method is safe to call from attachBaseContext().
     */
    fun getUserLocale(context: Context): Locale {
        return try {
            // Read directly from SharedPreferences (synchronous)
            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val languageCode = sharedPrefs.getString(LANGUAGE_KEY, null)
            
            val supportedLanguage = when {
                languageCode.isNullOrEmpty() -> {
                    Log.d(TAG, "No language preference found, defaulting to English")
                    SupportedLanguage.DEFAULT
                }
                else -> {
                    val lang = SupportedLanguage.fromLegacyString(languageCode)
                    Log.d(TAG, "Found language preference: $languageCode -> ${lang.code}")
                    lang
                }
            }
            
            Locale(supportedLanguage.code)
        } catch (e: Exception) {
            Log.w(TAG, "Error reading language preference, defaulting to English", e)
            Locale(SupportedLanguage.DEFAULT.code)
        }
    }
    
    /**
     * Update context with the given locale.
     * 
     * This creates a new context with the locale applied.
     */
    fun updateContextLocale(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
    
    /**
     * Get a localized context based on user preferences.
     * 
     * This is the main method to use in attachBaseContext().
     */
    fun getLocalizedContext(baseContext: Context): Context {
        val userLocale = getUserLocale(baseContext)
        return updateContextLocale(baseContext, userLocale)
    }
}