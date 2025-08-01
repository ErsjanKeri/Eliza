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

package com.example.ai.edge.eliza.ai.service

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.ai.modelmanager.LlmChatModelHelper
import com.example.ai.edge.eliza.ai.modelmanager.data.TASK_ELIZA_CHAT
import com.example.ai.edge.eliza.ai.modelmanager.data.TASK_ELIZA_EXERCISE_HELP
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.data.Model
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ElizaChatViewModel"

/**
 * Gallery-style chat ViewModel for Eliza
 * Copied exactly from Gallery's LlmChatViewModelBase pattern
 */
@HiltViewModel
open class ElizaChatViewModel @Inject constructor() : ViewModel() {
    
    /**
     * Generate response using Gallery's exact pattern
     * Direct copy from Gallery's LlmChatViewModelBase.generateResponse
     */
    fun generateResponse(
        model: Model,
        input: String,
        images: List<Bitmap> = listOf(),
        resultListener: (String, Boolean) -> Unit,
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Starting inference for: ${input.take(50)}...")
            
            try {
                // Wait for instance to be initialized (Gallery pattern)
                while (model.instance == null) {
                    delay(100)
                }
                delay(500) // Gallery's initialization delay
                
                // Run inference using Gallery's exact pattern
                LlmChatModelHelper.runInference(
                    model = model,
                    input = input,
                    images = images,
                    audioClips = listOf(), // No audio support for now
                    resultListener = resultListener,
                    cleanUpListener = {
                        Log.d(TAG, "Model cleanup triggered during inference")
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during inference", e)
                resultListener("Error: ${e.message}", true)
                onError()
            }
        }
    }
    
    /**
     * Send a message and get AI response
     * Gallery-style interface for simple usage
     */
    suspend fun sendMessage(
        model: Model,
        message: String,
        images: List<Bitmap> = emptyList(),
        resultListener: (String, Boolean) -> Unit
    ) {
        generateResponse(
            model = model,
            input = message,
            images = images,
            resultListener = resultListener
        )
    }
    
    /**
     * Simple RAG wrapper - for now just passes context as part of prompt
     * TODO: Implement proper RAG context injection
     */
    suspend fun sendMessageWithContext(
        model: Model,
        message: String,
        context: String,
        images: List<Bitmap> = emptyList(),
        resultListener: (String, Boolean) -> Unit
    ) {
        val enhancedPrompt = """
            Context: $context
            
            Question: $message
            
            Please answer based on the provided context.
        """.trimIndent()
        
        sendMessage(model, enhancedPrompt, images, resultListener)
    }
}

/**
 * Specialized ViewModels for different tasks - Gallery pattern
 */
@HiltViewModel 
class ElizaChatChatViewModel @Inject constructor() : ElizaChatViewModel()

@HiltViewModel
class ElizaExerciseHelpViewModel @Inject constructor() : ElizaChatViewModel() 