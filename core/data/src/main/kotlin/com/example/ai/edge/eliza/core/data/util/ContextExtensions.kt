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

import android.content.Context

/**
 * Context extensions for locale management.
 * 
 * Provides convenient ways to work with localized contexts throughout the app.
 */

/**
 * Extension function to get a localized string from resources.
 * 
 * This is useful when you need to get strings outside of Composable context,
 * such as in ViewModels or repositories.
 * 
 * @param resId String resource ID
 * @param formatArgs Optional format arguments for string formatting
 * @return Localized string based on context's current locale
 */
fun Context.getLocalizedString(resId: Int, vararg formatArgs: Any): String {
    return if (formatArgs.isNotEmpty()) {
        getString(resId, *formatArgs)
    } else {
        getString(resId)
    }
}

/**
 * Extension function to check if a context has a specific locale set.
 * 
 * @param languageCode The language code to check (e.g., "en", "sq", "de")
 * @return True if the context's locale matches the given language code
 */
fun Context.hasLocale(languageCode: String): Boolean {
    val currentLocale = resources.configuration.locales[0]
    return currentLocale.language == languageCode
}