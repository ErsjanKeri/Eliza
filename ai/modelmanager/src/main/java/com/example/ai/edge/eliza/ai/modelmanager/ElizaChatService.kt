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
import com.example.ai.edge.eliza.core.model.GemmaVariant
import com.example.ai.edge.eliza.core.model.ModelInitializationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElizaChatService"

/**
 * High-level chat service that combines RAG with MediaPipe inference using MatFormer variant switching.
 * This service provides the complete educational AI experience by:
 * 1. Using RAG to enhance prompts with relevant educational content
 * 2. Selecting optimal variants for different educational contexts
 * 3. Using ModelManager for variant-aware inference
 * 4. Providing streaming responses with educational context
 */
@Singleton
class ElizaChatService @Inject constructor(
    private val modelManager: ElizaModelManager,
    private val inferenceHelper: ElizaInferenceHelper,
    private val ragProviderFactory: RagProviderFactory
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Generate an AI response with RAG enhancement and intelligent variant selection.
     * This is the main method that combines RAG + MatFormer variant switching + MediaPipe inference.
     */
    fun generateResponse(
        input: String,
        chatContext: ChatContext,
        resultListener: ResultListener,
        cleanUpListener: () -> Unit,
        images: List<Bitmap> = listOf()
    ) {
        Log.d(TAG, "Generating RAG-enhanced response for context: ${chatContext.javaClass.simpleName}")
        
        // Use coroutines for RAG processing and variant management
        serviceScope.launch {
            try {
                // Step 1: Select optimal variant for this educational context
                val optimalVariant = selectOptimalVariantForContext(chatContext)
                Log.d(TAG, "Selected optimal variant: ${optimalVariant.displayName} for context: ${chatContext.javaClass.simpleName}")
                
                // Step 2: Ensure model is initialized with the optimal variant
                ensureModelInitialized(optimalVariant) { initResult ->
                    when (initResult) {
                        is ModelInitializationResult.Success -> {
                            // Step 3: Proceed with RAG enhancement and inference
                            serviceScope.launch {
                                performRagEnhancedInference(
                                    input = input,
                                    chatContext = chatContext,
                                    variant = optimalVariant,
                                    resultListener = resultListener,
                                    cleanUpListener = cleanUpListener,
                                    images = images
                                )
                            }
                        }
                        is ModelInitializationResult.Error -> {
                            Log.e(TAG, "Failed to initialize model with variant $optimalVariant: ${initResult.message}")
                            resultListener("I apologize, but I'm having trouble initializing the AI model. Please try again in a moment.", true)
                        }
                        is ModelInitializationResult.Loading -> {
                            Log.d(TAG, "Model initialization in progress: ${initResult.message}")
                            // Continue waiting - this shouldn't happen in our callback but handle gracefully
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during RAG-enhanced inference setup", e)
                resultListener("I apologize, but I encountered an error while processing your question. Please try again.", true)
            }
        }
    }
    
    /**
     * Select the optimal variant for the given educational context.
     * This implements intelligent variant selection based on educational use cases.
     */
    private fun selectOptimalVariantForContext(context: ChatContext): GemmaVariant {
        return when (context) {
            is ChatContext.ChapterReading -> {
                // E2B is optimal for reading comprehension - faster, more focused
                GemmaVariant.GEMMA_3N_E2B
            }
            is ChatContext.ExerciseSolving -> {
                // E4B is optimal for problem solving - higher quality, more thorough
                GemmaVariant.GEMMA_3N_E4B
            }
            is ChatContext.Revision -> {
                // E2B is optimal for revision - efficient, quick responses
                GemmaVariant.GEMMA_3N_E2B
            }
            is ChatContext.GeneralTutoring -> {
                // Use device-adaptive recommendation for general tutoring
                modelManager.getRecommendedVariant()
            }
        }
    }
    
    /**
     * Ensure the model is initialized with the specified variant.
     * This handles variant switching if needed.
     */
    private suspend fun ensureModelInitialized(
        targetVariant: GemmaVariant,
        onComplete: (ModelInitializationResult) -> Unit
    ) {
        val currentVariant = modelManager.getCurrentVariant()
        
        when {
            // Model not initialized at all
            currentVariant == null -> {
                Log.d(TAG, "Model not initialized. Initializing with variant: ${targetVariant.displayName}")
                modelManager.initializeModel(targetVariant).collect { result ->
                    when (result) {
                        is ModelInitializationResult.Success -> {
                            Log.d(TAG, "Model initialized successfully with ${targetVariant.displayName}")
                            onComplete(ModelInitializationResult.Success(result.message))
                        }
                        is ModelInitializationResult.Error -> {
                            Log.e(TAG, "Model initialization failed: ${result.message}")
                            onComplete(ModelInitializationResult.Error(result.message))
                        }
                        is ModelInitializationResult.Loading -> {
                            Log.d(TAG, "Model initialization in progress: ${result.message}")
                            // Continue waiting
                        }
                    }
                }
            }
            
            // Model initialized with different variant - switch
            currentVariant != targetVariant -> {
                Log.d(TAG, "Switching from ${currentVariant.displayName} to ${targetVariant.displayName}")
                modelManager.switchToVariant(targetVariant).collect { result ->
                    when (result) {
                        is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Success -> {
                            Log.d(TAG, "Successfully switched to ${targetVariant.displayName}")
                            onComplete(ModelInitializationResult.Success("Switched to ${targetVariant.displayName}"))
                        }
                        is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Error -> {
                            Log.e(TAG, "Variant switching failed: ${result.message}")
                            onComplete(ModelInitializationResult.Error(result.message))
                        }
                        is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Loading -> {
                            Log.d(TAG, "Variant switching in progress: ${result.message}")
                            // Continue waiting
                        }
                    }
                }
            }
            
            // Model already initialized with correct variant
            else -> {
                Log.d(TAG, "Model already initialized with correct variant: ${targetVariant.displayName}")
                onComplete(ModelInitializationResult.Success("Model ready"))
            }
        }
    }
    
    /**
     * Perform RAG enhancement and inference with the initialized model.
     */
    private suspend fun performRagEnhancedInference(
        input: String,
        chatContext: ChatContext,
        variant: GemmaVariant,
        resultListener: ResultListener,
        cleanUpListener: () -> Unit,
        images: List<Bitmap>
    ) {
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
                chatContext = chatContext,
                variant = variant
            )
            
            Log.d(TAG, "RAG enhancement complete. Running MediaPipe inference with ${variant.displayName}")
            Log.d(TAG, "Enhanced prompt length: ${finalPrompt.length} chars")
            Log.d(TAG, "Retrieved chunks: ${enhancementResult.chunksUsed}")
            
            // Step 5: Get the current model and run inference
            val model = modelManager.getCurrentModel()
            if (model != null) {
                runVariantOptimizedInference(
                    model = model,
                    finalPrompt = finalPrompt,
                    chatContext = chatContext,
                    resultListener = resultListener,
                    cleanUpListener = cleanUpListener,
                    images = images
                )
            } else {
                Log.e(TAG, "No model available after initialization")
                resultListener("I apologize, but the AI model is not available. Please try again.", true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during RAG-enhanced inference", e)
            resultListener("I apologize, but I encountered an error while processing your question. Please try again.", true)
        }
    }
    
    /**
     * Build the final prompt that combines system instructions with RAG-enhanced content.
     * Now includes variant-specific optimization hints.
     */
    private fun buildFinalPrompt(
        systemInstructions: String,
        enhancedPrompt: com.example.ai.edge.eliza.core.model.EnhancedPrompt,
        chatContext: ChatContext,
        variant: GemmaVariant
    ): String {
        return buildString {
            // System instructions first
            append(systemInstructions)
            append("\n\n")
            
            // Add variant-specific optimization hints
            when (variant) {
                GemmaVariant.GEMMA_3N_E2B -> {
                    append("OPTIMIZATION MODE: Efficient response mode. Provide concise, focused answers.\n")
                }
                GemmaVariant.GEMMA_3N_E4B -> {
                    append("OPTIMIZATION MODE: High-quality response mode. Provide detailed, comprehensive answers.\n")
                }
            }
            
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
     * Run MediaPipe inference with variant-optimized parameters.
     */
    private fun runVariantOptimizedInference(
        model: Model,
        finalPrompt: String,
        chatContext: ChatContext,
        resultListener: ResultListener,
        cleanUpListener: () -> Unit,
        images: List<Bitmap>
    ) {
        // Create a simple ChatContext for the inference layer
        val inferenceContext = ChatContext.GeneralTutoring()
        
        // Run inference using the variant-aware inference helper
        inferenceHelper.runInference(
            model = model,
            input = finalPrompt,
            chatContext = inferenceContext,
            resultListener = resultListener,
            cleanUpListener = cleanUpListener,
            images = images
        )
    }
    
    /**
     * Reset the inference session for the current variant.
     */
    fun resetSession() {
        val model = modelManager.getCurrentModel()
        if (model != null) {
            Log.d(TAG, "Resetting chat session for model: ${model.name}")
            inferenceHelper.resetSession(model)
        } else {
            Log.w(TAG, "No model available to reset session")
        }
    }
    
    /**
     * Switch to optimal variant for the given context.
     * This can be called proactively when context changes.
     */
    suspend fun switchToOptimalVariant(chatContext: ChatContext) {
        val optimalVariant = selectOptimalVariantForContext(chatContext)
        val currentVariant = modelManager.getCurrentVariant()
        
        if (currentVariant != optimalVariant) {
            Log.d(TAG, "Proactively switching to optimal variant: ${optimalVariant.displayName} for context: ${chatContext.javaClass.simpleName}")
            modelManager.switchToVariant(optimalVariant).collect { result ->
                when (result) {
                    is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Success -> {
                        Log.d(TAG, "Successfully switched to optimal variant: ${optimalVariant.displayName}")
                    }
                    is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Error -> {
                        Log.e(TAG, "Failed to switch to optimal variant: ${result.message}")
                    }
                    is com.example.ai.edge.eliza.core.model.ModelSwitchResult.Loading -> {
                        Log.d(TAG, "Switching to optimal variant: ${result.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Get the current variant being used.
     */
    fun getCurrentVariant(): GemmaVariant? {
        return modelManager.getCurrentVariant()
    }
    
    /**
     * Get performance information for the current variant.
     */
    fun getCurrentVariantPerformance(): com.example.ai.edge.eliza.core.model.ModelPerformance? {
        return modelManager.getCurrentVariantPerformance()
    }
} 