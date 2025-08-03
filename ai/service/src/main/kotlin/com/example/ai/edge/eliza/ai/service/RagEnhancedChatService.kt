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

import android.graphics.Bitmap
import android.util.Log
import com.example.ai.edge.eliza.ai.modelmanager.LlmChatModelHelper
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Model
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

private const val TAG = "RagEnhancedChatService"

/**
 * RAG-enhanced chat service that provides context-aware AI responses.
 * Integrates with the existing ChatViewModel but adds RAG-powered context retrieval.
 */
@ViewModelScoped
class RagEnhancedChatService @Inject constructor(
    private val ragProviderFactory: RagProviderFactory
) {
    
    /**
     * Generate response with RAG enhancement when enabled.
     * Falls back to basic generation when RAG is disabled or fails.
     */
    suspend fun generateEnhancedResponse(
        model: Model,
        input: String,
        context: ChatContext?,
        images: List<Bitmap> = emptyList(),
        resultListener: (String, Boolean) -> Unit,
        onError: () -> Unit = {}
    ) {
        Log.d(TAG, "Generating enhanced response with RAG")
        Log.d(TAG, "Context provided: ${context != null}, Context type: ${context?.javaClass?.simpleName}")
        Log.d(TAG, "Enhanced RAG enabled: ${ragProviderFactory.isEnhancedRagEnabled()}")
        Log.d(TAG, "Input length: ${input.length}, Images: ${images.size}")
        
        try {
            // Check if enhanced RAG is enabled and context is available
            if (context != null && ragProviderFactory.isEnhancedRagEnabled()) {
                Log.d(TAG, "Enhanced RAG is enabled, using RAG provider for context-aware response")
                val ragProvider = ragProviderFactory.createEnhancedProvider(context)
                
                // Get enhanced prompt with RAG context
                val enhancementResult = ragProvider.enhancePrompt(input, context)
                
                if (enhancementResult.confidence >= 0.6) {
                    Log.d(TAG, "RAG enhancement successful (confidence: ${enhancementResult.confidence})")
                    
                    // Use enhanced prompt for generation
                    generateWithPrompt(
                        model = model,
                        prompt = enhancementResult.enhancedPrompt.enhancedPrompt,
                        images = images,
                        resultListener = { response, isComplete ->
                            if (isComplete) {
                                Log.d(TAG, "Enhanced RAG response completed")
                            }
                            resultListener(response, isComplete)
                        },
                        onError = {
                            Log.w(TAG, "Enhanced RAG generation failed, falling back to basic")
                            generateBasicResponse(model, input, images, resultListener, onError)
                        }
                    )
                    return
                } else {
                    Log.w(TAG, "RAG enhancement confidence too low (${enhancementResult.confidence}), falling back to basic")
                }
            } else {
                if (context == null) {
                    Log.d(TAG, "No context provided, using basic generation")
                } else {
                    Log.d(TAG, "Enhanced RAG disabled, using basic generation")
                }
            }
            
            // Fall back to basic generation
            generateBasicResponse(model, input, images, resultListener, onError)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in enhanced response generation", e)
            generateBasicResponse(model, input, images, resultListener, onError)
        }
    }
    
    /**
     * Generate response using direct model inference (no RAG).
     */
    private fun generateBasicResponse(
        model: Model,
        input: String,
        images: List<Bitmap>,
        resultListener: (String, Boolean) -> Unit,
        onError: () -> Unit
    ) {
        Log.d(TAG, "Using basic response generation via LlmChatModelHelper")
        Log.d(TAG, "Model: ${model.name}, Input length: ${input.length}, Images: ${images.size}")
        
        try {
            // Call LlmChatModelHelper directly to avoid infinite loop
            LlmChatModelHelper.runInference(
                model = model,
                input = input,
                resultListener = { response, isComplete ->
                    Log.d(TAG, "Basic inference callback - complete: $isComplete, response length: ${response.length}")
                    resultListener(response, isComplete)
                },
                cleanUpListener = {
                    Log.d(TAG, "Basic inference cleanup completed")
                },
                images = images
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in basic response generation", e)
            resultListener("Error generating response: ${e.message}", true)
            onError()
        }
    }
    
    /**
     * Generate response with a specific prompt (used for RAG-enhanced prompts).
     */
    private fun generateWithPrompt(
        model: Model,
        prompt: String,
        images: List<Bitmap>,
        resultListener: (String, Boolean) -> Unit,
        onError: () -> Unit
    ) {
        Log.d(TAG, "Generating with enhanced prompt (${prompt.length} chars)")
        Log.d(TAG, "RAG Enhanced - Model: ${model.name}, Images: ${images.size}")
        Log.d(TAG, "Enhanced prompt preview: ${prompt.take(200)}...")
        
        try {
            // Call LlmChatModelHelper directly to avoid infinite loop
            LlmChatModelHelper.runInference(
                model = model,
                input = prompt,
                resultListener = { response, isComplete ->
                    Log.d(TAG, "Enhanced inference callback - complete: $isComplete, response length: ${response.length}")
                    resultListener(response, isComplete)
                },
                cleanUpListener = {
                    Log.d(TAG, "Enhanced inference cleanup completed")
                },
                images = images
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in enhanced response generation", e)
            resultListener("Error generating enhanced response: ${e.message}", true)
            onError()
        }
    }
    
    /**
     * Check if enhanced RAG is available and enabled.
     */
    fun isEnhancedRagAvailable(): Boolean {
        return ragProviderFactory.isEnhancedRagEnabled()
    }
    
    /**
     * Get context-specific suggestions for the user's query.
     */
    suspend fun getContextSuggestions(
        query: String,
        context: ChatContext
    ): List<String> {
        return try {
            val ragProvider = ragProviderFactory.createProvider(context)
            val relevantContent = ragProvider.getRelevantContent(query, context, maxChunks = 2)
            
            relevantContent.map { chunk ->
                "Ask about: ${chunk.title}"
            }.take(3)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting context suggestions", e)
            emptyList()
        }
    }
}