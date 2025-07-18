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

package com.example.ai.edge.eliza.ai.modelmanager

import android.graphics.Bitmap
import android.util.Log
import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper
import com.example.ai.edge.eliza.ai.inference.ResultListener
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElizaChatService"

/**
 * High-level chat service that combines RAG with MediaPipe inference.
 * This service provides the complete educational AI experience by:
 * 1. Using RAG to enhance prompts with relevant educational content
 * 2. Using MediaPipe for actual inference
 * 3. Providing streaming responses with educational context
 */
@Singleton
class ElizaChatService @Inject constructor(
    private val inferenceHelper: ElizaInferenceHelper,
    private val ragProviderFactory: RagProviderFactory
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Generate an AI response with RAG enhancement.
     * This is the main method that combines RAG + MediaPipe inference.
     */
    fun generateResponse(
        model: Model,
        input: String,
        chatContext: ChatContext,
        resultListener: ResultListener,
        cleanUpListener: () -> Unit,
        images: List<Bitmap> = listOf()
    ) {
        Log.d(TAG, "Generating RAG-enhanced response for context: ${chatContext.javaClass.simpleName}")
        
        // Use coroutines for RAG processing while keeping MediaPipe callback-based
        serviceScope.launch {
            try {
                // Step 1: Get the appropriate RAG provider for this context
                val ragProvider = ragProviderFactory.createProvider(chatContext)
                
                // Step 2: Enhance the prompt with RAG
                val enhancementResult = ragProvider.enhancePrompt(input, chatContext)
                val enhancedPrompt = enhancementResult.enhancedPrompt
                
                // Step 3: Get system instructions
                val systemInstructions = ragProvider.getSystemInstructions(chatContext)
                
                // Step 4: Build the final educational prompt
                val finalPrompt = buildFinalPrompt(
                    systemInstructions = systemInstructions,
                    enhancedPrompt = enhancedPrompt,
                    chatContext = chatContext
                )
                
                Log.d(TAG, "RAG enhancement complete. Running MediaPipe inference...")
                Log.d(TAG, "Enhanced prompt length: ${finalPrompt.length} chars")
                Log.d(TAG, "Retrieved chunks: ${enhancementResult.chunksUsed}")
                
                // Step 5: Run the enhanced prompt through MediaPipe inference
                runMediaPipeInference(
                    model = model,
                    finalPrompt = finalPrompt,
                    resultListener = resultListener,
                    cleanUpListener = cleanUpListener,
                    images = images
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during RAG-enhanced inference", e)
                resultListener("I apologize, but I encountered an error while processing your question. Please try again.", true)
            }
        }
    }
    
    /**
     * Reset the inference session.
     */
    fun resetSession(model: Model) {
        Log.d(TAG, "Resetting chat session for model: ${model.name}")
        inferenceHelper.resetSession(model)
    }
    
    /**
     * Build the final prompt that combines system instructions with RAG-enhanced content.
     */
    private fun buildFinalPrompt(
        systemInstructions: String,
        enhancedPrompt: com.example.ai.edge.eliza.core.model.EnhancedPrompt,
        chatContext: ChatContext
    ): String {
        return buildString {
            // System instructions first
            append(systemInstructions)
            append("\n\n")
            
            // Add context-specific information
            when (chatContext) {
                is ChatContext.ChapterReading -> {
                    append("CURRENT CHAPTER: ${chatContext.chapterTitle}\n")
                    append("LESSON ID: ${chatContext.lessonId}\n")
                }
                is ChatContext.Revision -> {
                    append("REVISION MODE: Reviewing ${chatContext.completedLessonIds.size} completed lessons\n")
                }
                is ChatContext.ExerciseSolving -> {
                    append("EXERCISE MODE: Problem ID ${chatContext.exerciseId}\n")
                    append("ATTEMPTS: ${chatContext.attempts}\n")
                    append("HINTS USED: ${chatContext.hintsUsed}\n")
                }
                is ChatContext.GeneralTutoring -> {
                    append("GENERAL TUTORING MODE\n")
                }
            }
            
            append("\n")
            
            // RAG-enhanced content
            append(enhancedPrompt.enhancedPrompt)
        }
    }
    
    /**
     * Run MediaPipe inference with the enhanced prompt.
     */
    private fun runMediaPipeInference(
        model: Model,
        finalPrompt: String,
        resultListener: ResultListener,
        cleanUpListener: () -> Unit,
        images: List<Bitmap>
    ) {
        // Create a simple ChatContext for the inference layer (it doesn't need RAG context)
        val inferenceContext = ChatContext.GeneralTutoring()
        
        // Run inference using the pure MediaPipe helper
        inferenceHelper.runInference(
            model = model,
            input = finalPrompt,
            chatContext = inferenceContext,
            resultListener = resultListener,
            cleanUpListener = cleanUpListener,
            images = images
        )
    }
} 