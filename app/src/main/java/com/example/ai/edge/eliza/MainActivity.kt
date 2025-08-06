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

package com.example.ai.edge.eliza

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.ai.edge.eliza.core.data.util.LocaleHelper
import com.example.ai.edge.eliza.core.data.util.LocaleManager
import com.example.ai.edge.eliza.core.data.util.NetworkMonitor
import com.example.ai.edge.eliza.core.designsystem.theme.ElizaTheme
import com.example.ai.edge.eliza.ui.ElizaApp
import com.example.ai.edge.eliza.ui.rememberElizaAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    @Inject
    lateinit var localeManager: LocaleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appState = rememberElizaAppState(
                networkMonitor = networkMonitor,
            )

            ElizaTheme {
                ElizaApp(appState)
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        // Apply user's preferred locale to activity context SYNCHRONOUSLY
        // This must happen before super.attachBaseContext() is called
        // 
        // NOTE: We cannot use localeManager here because Hilt injection hasn't 
        // happened yet (attachBaseContext runs BEFORE onCreate where injection occurs).
        // Instead, we use LocaleHelper which doesn't require Hilt dependencies.
        val localizedContext = newBase?.let { context ->
            try {
                LocaleHelper.getLocalizedContext(context)
            } catch (e: Exception) {
                // Continue with default context if locale setup fails
                // This ensures the app doesn't crash on locale errors
                context
            }
        } ?: newBase
        
        super.attachBaseContext(localizedContext)
    }
}