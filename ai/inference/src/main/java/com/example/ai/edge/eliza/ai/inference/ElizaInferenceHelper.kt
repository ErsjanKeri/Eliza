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
  * Interface for pure MediaPipe LLM inference operations.
  * Based exactly on Gallery's LlmChatModelHelper pattern.
  * 
  * This is a low-level interface for MediaPipe operations only.
  * For RAG-enhanced educational responses, use ElizaChatService instead.
  */
 interface ElizaInferenceHelper {
     
     /**
      * Initialize the model for inference.
      * @param context Android context
      * @param model The model to initialize
      * @param onDone Callback when initialization is complete (error message or empty string)
      */
     fun initialize(context: Context, model: Model, onDone: (String) -> Unit)
     
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
      * Run pure MediaPipe inference.
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
 } 
 
 
 /**
  * Model instance holder - exactly like Gallery's LlmModelInstance.
  */
 data class ElizaModelInstance(
     val engine: LlmInference,
     var session: LlmInferenceSession
 )
 
 /**
  * Pure MediaPipe LLM inference implementation.
  * Based exactly on Gallery's LlmChatModelHelper pattern.
  * 
  * This class handles only MediaPipe operations. For educational AI responses
  * with RAG enhancement, use ElizaChatService which combines this with RAG.
  */
 @Singleton
 class ElizaInferenceHelperImpl @Inject constructor() : ElizaInferenceHelper {
     
     // Indexed by model name - exactly like Gallery's pattern
     private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()
     
     override fun initialize(context: Context, model: Model, onDone: (String) -> Unit) {
         Log.d(TAG, "Initializing model '${model.name}' with MediaPipe...")
         
         // Prepare options - exactly like Gallery's pattern
         val maxTokens = DEFAULT_MAX_TOKEN
         val topK = DEFAULT_TOPK
         val topP = DEFAULT_TOPP
         val temperature = DEFAULT_TEMPERATURE
         
         // Use GPU backend by default (like Gallery)
         val preferredBackend = LlmInference.Backend.CPU // TODO: Gallery uses GPU sometimes but that lead to app crash 
         
         val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
             .setModelPath(model.getPath(context = context))
             .setMaxTokens(maxTokens)
             .setPreferredBackend(preferredBackend)
             .setMaxNumImages(if (model.llmSupportImage) MAX_IMAGE_COUNT else 0)
             
         val options = optionsBuilder.build()
         
         // Create an instance of the LLM Inference task and session - exactly like Gallery
         try {
             val llmInference = LlmInference.createFromOptions(context, options)
             
             val session = LlmInferenceSession.createFromOptions(
                 llmInference,
                 LlmInferenceSession.LlmInferenceSessionOptions.builder()
                     .setTopK(topK)
                     .setTopP(topP)
                     .setTemperature(temperature)
                     .setGraphOptions(
                         GraphOptions.builder()
                             .setEnableVisionModality(model.llmSupportImage)
                             .build()
                     )
                     .build()
             )
             
             model.instance = ElizaModelInstance(engine = llmInference, session = session)
             Log.d(TAG, "Model '${model.name}' initialized successfully")
             onDone("")
             
         } catch (e: Exception) {
             Log.e(TAG, "Failed to initialize model '${model.name}'", e)
             onDone("Model initialization failed: ${e.message}")
         }
     }
     
     override fun resetSession(model: Model) {
         try {
             Log.d(TAG, "Resetting session for model '${model.name}'")
             
             val instance = model.instance as ElizaModelInstance? ?: return
             val session = instance.session
             session.close()
             
             val inference = instance.engine
             val topK = DEFAULT_TOPK
             val topP = DEFAULT_TOPP
             val temperature = DEFAULT_TEMPERATURE
             
             val newSession = LlmInferenceSession.createFromOptions(
                 inference,
                 LlmInferenceSession.LlmInferenceSessionOptions.builder()
                     .setTopK(topK)
                     .setTopP(topP)
                     .setTemperature(temperature)
                     .setGraphOptions(
                         GraphOptions.builder()
                             .setEnableVisionModality(model.llmSupportImage)
                             .build()
                     )
                     .build()
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
         Log.d(TAG, "Running inference for model '${model.name}'")
         
         val instance = model.instance as ElizaModelInstance
         
         // Set cleanup listener - exactly like Gallery's pattern
         if (!cleanUpListeners.containsKey(model.name)) {
             cleanUpListeners[model.name] = cleanUpListener
         }
         
         try {
             Log.d(TAG, "Running pure MediaPipe inference...")
             
             // Run MediaPipe inference - exactly like Gallery's pattern
             // The input should already be a complete, enhanced prompt from higher layers
             val session = instance.session
             
             if (input.trim().isNotEmpty()) {
                 session.addQueryChunk(input)
             }
             
             // Add images if provided
             for (image in images) {
                 session.addImage(BitmapImageBuilder(image).build())
             }
             
             // Generate response asynchronously - exactly like Gallery's pattern
             session.generateResponseAsync(resultListener)
             
         } catch (e: Exception) {
             Log.e(TAG, "Error during MediaPipe inference", e)
             resultListener("Error: ${e.message}", true)
         }
     }
 } 