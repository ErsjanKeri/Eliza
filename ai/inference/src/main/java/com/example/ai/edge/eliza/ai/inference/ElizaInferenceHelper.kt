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

 package com.example.ai.edge.eliza.ai.inference

 import android.content.Context
 import android.graphics.Bitmap
 import android.util.Log
 import com.example.ai.edge.eliza.core.model.ChatContext
 import com.example.ai.edge.eliza.core.model.Model
 import com.google.mediapipe.framework.image.BitmapImageBuilder
 import com.google.mediapipe.tasks.genai.llminference.GraphOptions
 import com.google.mediapipe.tasks.genai.llminference.LlmInference
 import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
 import javax.inject.Inject
 import javax.inject.Singleton
 import com.example.ai.edge.eliza.core.model.VariantConfig
 
 private const val TAG = "ElizaInferenceHelperImpl"
 private const val DEFAULT_MAX_TOKEN = 1024
 private const val DEFAULT_TOPK = 40
 private const val DEFAULT_TOPP = 0.95f
 private const val DEFAULT_TEMPERATURE = 0.8f
 private const val MAX_IMAGE_COUNT = 5
 
 /**
  * Type aliases for inference callbacks - exactly like Gallery's pattern.
  */
 typealias ResultListener = (partialResult: String, done: Boolean) -> Unit
 typealias CleanUpListener = () -> Unit
 
 /**
  * Interface for MediaPipe LLM inference operations with intelligent variant switching.
  * 
  * This implements the closest equivalent to MatFormer parameter switching that's
  * possible with MediaPipe's public API. Instead of direct FFN parameter control,
  * we optimize session configuration and memory management for different variants.
  */
 interface ElizaInferenceHelper {
     
     /**
      * Initialize the model for inference with variant-specific optimizations.
      * @param context Android context
      * @param model The model to initialize
      * @param variant The specific variant to optimize for
      * @param onDone Callback when initialization is complete (error message or empty string)
      */
     fun initialize(context: Context, model: Model, variant: String, onDone: (String) -> Unit)
     
     /**
      * Switch to a different variant by recreating the session with optimized parameters.
      * This is our implementation of "MatFormer-style" switching within MediaPipe constraints.
      * @param model The model to switch variants for
      * @param targetVariant The variant to switch to
      * @param onDone Callback when switching is complete
      */
     fun switchVariant(model: Model, targetVariant: String, onDone: (String) -> Unit)
     
     /**
      * Reset the inference session while keeping the model loaded.
      * This clears conversation history for a fresh start.
      * @param model The model to reset
      */
     fun resetSession(model: Model)
     
     /**
      * Clean up the model and release resources.
      * @param model The model to clean up
      */
     fun cleanUp(model: Model)
     
     /**
      * Run inference with variant-optimized parameters.
      * @param model The model to use for inference
      * @param input The complete prompt (should already be enhanced by higher layers)
      * @param chatContext The chat context (for future compatibility)
      * @param resultListener Callback for streaming results
      * @param cleanUpListener Callback for cleanup
      * @param images Optional images for multimodal inference
      */
     fun runInference(
         model: Model,
         input: String,
         chatContext: ChatContext,
         resultListener: ResultListener,
         cleanUpListener: CleanUpListener,
         images: List<Bitmap> = listOf()
     )
     
     /**
      * Get the current variant being used for inference.
      * @param model The model to check
      * @return The current variant, or null if not set
      */
     fun getCurrentVariant(model: Model): String?
     
     /**
      * Extracts the short variant name (e.g., "E2B") from the full variant string.
      * @param model The model to check against
      * @param fullVariantName The full variant name (e.g., "gemma-3n-E4B")
      * @return The short variant name (e.g., "E4B")
      */
     fun getShortVariantName(model: Model, fullVariantName: String): String
 } 
 
 /**
  * Enhanced model instance holder with variant information and optimization state.
  */
 data class ElizaModelInstance(
     val engine: LlmInference,
     var session: LlmInferenceSession,
     var currentVariant: String,
     var config: VariantConfig
 )
 
 /**
  * Intelligent MediaPipe LLM inference implementation with variant switching.
  * 
  * This implements the closest equivalent to MatFormer parameter switching possible
  * with MediaPipe's public API. Instead of direct FFN parameter control, we:
  * 
  * 1. Optimize session configuration for different variants
  * 2. Implement intelligent memory management
  * 3. Provide seamless variant switching
  * 4. Adapt to device capabilities
  */
 @Singleton
 class ElizaInferenceHelperImpl @Inject constructor() : ElizaInferenceHelper {
     
     // Indexed by model name - exactly like Gallery's pattern
     private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()
     
              /**
      * Extracts the short variant name (e.g., "E2B") from the full variant string.
      */
     override fun getShortVariantName(model: Model, fullVariantName: String): String {
         val parts = fullVariantName.split("-")
         val variantPart = parts.getOrNull(2) // e.g., "E4B" from "gemma-3n-E4B"
         
         if (variantPart != null && model.availableVariants.contains(variantPart)) {
             return variantPart
         }
         
         throw IllegalArgumentException("Unknown or invalid variant in '$fullVariantName'")
     }

     /**
      * Get variant-specific configuration from the model definition.
      */
     private fun getVariantConfig(model: Model, variant: String): VariantConfig {
         val shortVariant = getShortVariantName(model, variant)
         return model.variantConfigs[shortVariant]
             ?: throw IllegalArgumentException("No config found for variant '$shortVariant'")
     }
     
     override fun initialize(context: Context, model: Model, variant: String, onDone: (String) -> Unit) {
         Log.d(TAG, "Initializing model '${model.name}' with variant '$variant' optimization...")
         
         val config = getVariantConfig(model, variant)
         
         // Use variant-optimized backend selection
         val preferredBackend = if (config.useGPU) {
             LlmInference.Backend.GPU
         } else {
             LlmInference.Backend.CPU
         }
         
         val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
             .setModelPath(model.getPath(context = context))
             .setMaxTokens(config.maxTokens)
             .setPreferredBackend(preferredBackend)
             .setMaxNumImages(if (model.llmSupportImage) MAX_IMAGE_COUNT else 0)
             
         val options = optionsBuilder.build()
         
         // Create an instance of the LLM Inference task and session with variant optimization
         try {
             val llmInference = LlmInference.createFromOptions(context, options)
             
             val session = createOptimizedSession(llmInference, variant, config, model.llmSupportImage)
             
             model.instance = ElizaModelInstance(
                 engine = llmInference, 
                 session = session,
                 currentVariant = variant,
                 config = config
             )
             
             Log.d(TAG, "Model '${model.name}' initialized successfully with variant '$variant' (topK=${config.topK}, backend=${preferredBackend.name})")
             onDone("")
             
         } catch (e: Exception) {
             Log.e(TAG, "Failed to initialize model '${model.name}' with variant '$variant'", e)
             onDone("Model initialization failed: ${e.message}")
         }
     }
     
     override fun switchVariant(model: Model, targetVariant: String, onDone: (String) -> Unit) {
         val instance = model.instance as ElizaModelInstance? ?: run {
             onDone("Model not initialized")
             return
         }
         
         if (instance.currentVariant == targetVariant) {
             Log.d(TAG, "Already using variant '$targetVariant' for model '${model.name}'")
             onDone("")
             return
         }
         
         Log.d(TAG, "Switching model '${model.name}' from '${instance.currentVariant}' to '$targetVariant'...")
         
         try {
             // Close current session
             instance.session.close()
             
             // Create new session with target variant configuration
             val newConfig = getVariantConfig(model, targetVariant)
             val newSession = createOptimizedSession(instance.engine, targetVariant, newConfig, model.llmSupportImage)
             
             // Update instance
             instance.session = newSession
             instance.currentVariant = targetVariant
             instance.config = newConfig
             
             Log.d(TAG, "Successfully switched model '${model.name}' to variant '$targetVariant' (topK=${newConfig.topK}, memOpt=${newConfig.memoryOptimized})")
             onDone("")
             
         } catch (e: Exception) {
             Log.e(TAG, "Failed to switch model '${model.name}' to variant '$targetVariant'", e)
             onDone("Variant switching failed: ${e.message}")
         }
     }
     
     /**
      * Create an optimized session for the specified variant.
      * This is where we implement our "MatFormer-style" parameter optimization.
      */
     private fun createOptimizedSession(
         inference: LlmInference,
         variant: String,
         config: VariantConfig,
         supportImage: Boolean
     ): LlmInferenceSession {
         
         Log.d(TAG, "Creating optimized session for variant '$variant' with config: $config")
         
         return LlmInferenceSession.createFromOptions(
             inference,
             LlmInferenceSession.LlmInferenceSessionOptions.builder()
                 .setTopK(config.topK)
                 .setTopP(config.topP)
                 .setTemperature(config.temperature)
                 .setGraphOptions(
                     GraphOptions.builder()
                         .setEnableVisionModality(supportImage)
                         // Add any other variant-specific graph options here
                         .build()
                 )
                 .build()
         )
     }
     
     override fun resetSession(model: Model) {
         val instance = model.instance as ElizaModelInstance? ?: return
         
         try {
             Log.d(TAG, "Resetting session for model '${model.name}' with variant '${instance.currentVariant}'")
             
             instance.session.close()
             
             val newSession = createOptimizedSession(
                 instance.engine,
                 instance.currentVariant,
                 instance.config,
                 model.llmSupportImage
             )
             
             instance.session = newSession
             Log.d(TAG, "Session reset complete for model '${model.name}'")
             
         } catch (e: Exception) {
             Log.e(TAG, "Failed to reset session for model '${model.name}'", e)
         }
     }
     
     override fun cleanUp(model: Model) {
         if (model.instance == null) {
             return
         }
         
         Log.d(TAG, "Cleaning up model '${model.name}'")
         val instance = model.instance as ElizaModelInstance
         
         try {
             instance.session.close()
         } catch (e: Exception) {
             Log.e(TAG, "Failed to close the LLM Inference session: ${e.message}")
         }
         
         try {
             instance.engine.close()
         } catch (e: Exception) {
             Log.e(TAG, "Failed to close the LLM Inference engine: ${e.message}")
         }
         
         val onCleanUp = cleanUpListeners.remove(model.name)
         onCleanUp?.invoke()
         
         model.instance = null
         Log.d(TAG, "Model '${model.name}' cleanup completed")
     }
     
     override fun runInference(
         model: Model,
         input: String,
         chatContext: ChatContext,
         resultListener: ResultListener,
         cleanUpListener: CleanUpListener,
         images: List<Bitmap>
     ) {
         val instance = model.instance as ElizaModelInstance
         
         Log.d(TAG, "Running inference for model '${model.name}' with variant '${instance.currentVariant}'")
         
         // Set cleanup listener - exactly like Gallery's pattern
         if (!cleanUpListeners.containsKey(model.name)) {
             cleanUpListeners[model.name] = cleanUpListener
         }
         
         try {
             // Run MediaPipe inference with variant-optimized session
             val session = instance.session
             
             if (input.trim().isNotEmpty()) {
                 session.addQueryChunk(input)
             }
             
             // Add images if provided
             for (image in images) {
                 session.addImage(BitmapImageBuilder(image).build())
             }
             
             // Generate response asynchronously with variant-optimized parameters
             session.generateResponseAsync(resultListener)
             
         } catch (e: Exception) {
             Log.e(TAG, "Error during MediaPipe inference", e)
             resultListener("Error: ${e.message}", true)
         }
     }
     
     override fun getCurrentVariant(model: Model): String? {
         val instance = model.instance as ElizaModelInstance? ?: return null
         return instance.currentVariant
     }
 } 