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

package com.example.ai.edge.eliza

import android.app.Application
import android.util.Log
import com.example.ai.edge.eliza.ai.rag.service.RagInitializationService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Eliza AI Tutor app.
 * Required for Hilt dependency injection.
 * Initializes the RAG system on app startup.
 */
@HiltAndroidApp
class ElizaApplication : Application() {
    
    @Inject
    lateinit var ragInitializationService: RagInitializationService
    
    companion object {
        private const val TAG = "ElizaApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Eliza app starting...")
        
        // Initialize the RAG system in background
        // This will index mock data and prepare embeddings for enhanced chat
        ragInitializationService.initializeAsync()
        
        Log.d(TAG, "RAG initialization triggered")
    }
} 