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
// Import Gallery-compatible Model class from core.model
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Model

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ElizaChatViewModel"

/**
 * Gallery-style chat ViewModel for Eliza with RAG enhancement
 * Copied exactly from Gallery's LlmChatViewModelBase pattern, enhanced with RAG
 */
@HiltViewModel
open class ElizaChatViewModel @Inject constructor(
    private val ragEnhancedChatService: RagEnhancedChatService
) : ViewModel() {
    
    /**
     * Generate response using Gallery's exact pattern with RAG enhancement
     * Enhanced version that uses RagEnhancedChatService when context is available
     */
    fun generateResponse(
        model: Model,
        input: String,
        context: ChatContext? = null,
        images: List<Bitmap> = listOf(),
        resultListener: (String, Boolean) -> Unit,
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Starting inference for: ${input.take(50)}...")
            Log.d(TAG, "Model name: ${model.name}, instance: ${model.instance}")
            
            try {
                // Wait for instance to be initialized (Gallery pattern)
                var waitTime = 0
                while (model.instance == null) {
                    if (waitTime > 30000) { // 30 second timeout
                        Log.e(TAG, "Model instance is null after 30 seconds - initialization failed")
                        resultListener("Model initialization failed - instance is null", true)
                        onError()
                        return@launch
                    }
                    Log.d(TAG, "Waiting for model instance... (${waitTime}ms)")
                    delay(100)
                    waitTime += 100
                }
                delay(500) // Gallery's initialization delay
                
                Log.d(TAG, "Model instance available, starting inference...")
                
                // Use RAG-enhanced chat service for intelligent response generation
                ragEnhancedChatService.generateEnhancedResponse(
                    model = model,
                    input = input,
                    context = context,
                    images = images,
                    resultListener = { partialResult, done ->
                        Log.d(TAG, "Enhanced inference callback - done: $done, length: ${partialResult.length}")
                        resultListener(partialResult, done)
                    },
                    onError = {
                        Log.e(TAG, "Enhanced inference failed")
                        onError()
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
     * Enhanced message generation with RAG context awareness.
     * Automatically uses RAG when context is provided and enhanced RAG is enabled.
     */
    suspend fun sendMessageWithChatContext(
        model: Model,
        message: String,
        chatContext: ChatContext?,
        images: List<Bitmap> = emptyList(),
        resultListener: (String, Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            ragEnhancedChatService.generateEnhancedResponse(
                model = model,
                input = message,
                context = chatContext,
                images = images,
                resultListener = resultListener,
                onError = {
                    Log.w(TAG, "RAG-enhanced generation failed, falling back to basic")
                    // Fallback handled by the service
                }
            )
        }
    }
    
    /**
     * Simple RAG wrapper - for now just passes context as part of prompt
     * DEPRECATED: Use sendMessageWithChatContext for better RAG integration
     */
    @Deprecated("Use sendMessageWithChatContext for enhanced RAG integration")
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
    
    /**
     * Stop ongoing response generation
     */
    fun stopResponse(model: Model) {
        Log.d(TAG, "Stopping response for model ${model.name}...")
        viewModelScope.launch(Dispatchers.Default) {
            LlmChatModelHelper.stopResponse(model)
        }
    }
    
    /**
     * Check if enhanced RAG is available for the current session.
     */
    fun isEnhancedRagAvailable(): Boolean {
        return ragEnhancedChatService.isEnhancedRagAvailable()
    }
    
    /**
     * Get contextual suggestions for the user based on their current context.
     */
    suspend fun getContextualSuggestions(
        query: String,
        chatContext: ChatContext
    ): List<String> {
        return ragEnhancedChatService.getContextSuggestions(query, chatContext)
    }
}

/**
 * Specialized ViewModels for different tasks - Gallery pattern
 */
@HiltViewModel 
class ElizaChatChatViewModel @Inject constructor(
    ragEnhancedChatService: RagEnhancedChatService
) : ElizaChatViewModel(ragEnhancedChatService)

@HiltViewModel
class ElizaExerciseHelpViewModel @Inject constructor(
    ragEnhancedChatService: RagEnhancedChatService
) : ElizaChatViewModel(ragEnhancedChatService) 