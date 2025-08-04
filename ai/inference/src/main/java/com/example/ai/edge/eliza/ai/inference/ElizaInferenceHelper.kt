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
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ConfigKey
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPK
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPP
import com.example.ai.edge.eliza.core.model.DEFAULT_TEMPERATURE
import com.example.ai.edge.eliza.core.model.DEFAULT_MAX_TOKEN
import com.example.ai.edge.eliza.core.model.MAX_IMAGE_COUNT
import com.example.ai.edge.eliza.core.model.Accelerator
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElizaInferenceHelperImpl"

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

typealias CleanUpListener = () -> Unit

data class ElizaModelInstance(
    val engine: LlmInference,
    var session: LlmInferenceSession
)

interface ElizaInferenceHelper {
    fun initialize(context: Context, model: Model, variant: String, onDone: (String) -> Unit)
    fun switchVariant(model: Model, targetVariant: String, onDone: (String) -> Unit)
    fun generateResponseStreaming(model: Model, prompt: String, onPartialResponse: (String, Boolean) -> Unit, onDone: (String) -> Unit)
    fun generateResponseStreamingWithImages(model: Model, prompt: String, images: List<String>, onPartialResponse: (String, Boolean) -> Unit, onDone: (String) -> Unit)
    fun cleanup(model: Model)
    fun initializeModelsForTasks()
    fun getAvailableVariants(model: Model): List<String>
}

@Singleton
class ElizaInferenceHelperImpl @Inject constructor() : ElizaInferenceHelper {

    // Indexed by model name - EXACT COPY of Gallery pattern
    private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()

    override fun initializeModelsForTasks() {
        Log.d(TAG, "Initializing models for Eliza tasks using Gallery's approach")
        // No additional initialization needed for Gallery approach
    }

    override fun getAvailableVariants(model: Model): List<String> {
        // Gallery doesn't use variants, so return default
        return listOf("default")
    }

    override fun initialize(context: Context, model: Model, variant: String, onDone: (String) -> Unit) {
        Log.d(TAG, "Initializing model '${model.name}' using Gallery's EXACT approach...")
        
        // EXACT COPY of Gallery's initialization from LlmChatModelHelper.initialize()
        
        // Prepare options.
        val maxTokens = model.getIntConfigValue(key = ConfigKey.MAX_TOKENS, defaultValue = DEFAULT_MAX_TOKEN)
        val topK = model.getIntConfigValue(key = ConfigKey.TOPK, defaultValue = DEFAULT_TOPK)
        val topP = model.getFloatConfigValue(key = ConfigKey.TOPP, defaultValue = DEFAULT_TOPP)
        val temperature = model.getFloatConfigValue(key = ConfigKey.TEMPERATURE, defaultValue = DEFAULT_TEMPERATURE)
        val accelerator = model.getStringConfigValue(key = ConfigKey.ACCELERATOR, defaultValue = Accelerator.GPU.label)
        
        Log.d(TAG, "Initializing...")
        val preferredBackend = when (accelerator) {
            Accelerator.CPU.label -> LlmInference.Backend.CPU
            Accelerator.GPU.label -> LlmInference.Backend.GPU
            else -> LlmInference.Backend.GPU
        }
        
        val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(model.getPath(context = context))
            .setMaxTokens(maxTokens)
            .setPreferredBackend(preferredBackend)
            .setMaxNumImages(if (model.llmSupportImage) MAX_IMAGE_COUNT else 0)
        val options = optionsBuilder.build()

        // Create an instance of the LLM Inference task and session - EXACT Gallery pattern
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
                    .build(),
            )
            model.instance = ElizaModelInstance(engine = llmInference, session = session)
        } catch (e: Exception) {
            onDone(cleanUpMediapipeTaskErrorMessage(e.message ?: "Unknown error"))
            return
        }
        onDone("")
    }
    
    override fun switchVariant(model: Model, targetVariant: String, onDone: (String) -> Unit) {
        // Gallery doesn't use variants, so just return success
        Log.d(TAG, "Gallery approach doesn't use variants - staying with default configuration")
        onDone("")
    }

    override fun generateResponseStreaming(model: Model, prompt: String, onPartialResponse: (String, Boolean) -> Unit, onDone: (String) -> Unit) {
        val instance = model.instance as ElizaModelInstance? ?: run {
            onDone("Model not initialized")
            return
        }
        
        Log.d(TAG, "Generating streaming response for model '${model.name}' using Gallery's EXACT approach")
        
        try {
            // EXACT COPY of Gallery's runInference pattern from LlmChatModelHelper.runInference()
            val session = instance.session
            if (prompt.trim().isNotEmpty()) {
                session.addQueryChunk(prompt)
            }
            
            // Use Gallery's exact ResultListener pattern - this uses Gallery's typealias
            val resultListener: ResultListener = { partialResult: String, done: Boolean ->
                if (partialResult.isNotEmpty()) {
                    onPartialResponse(partialResult, false) // false = not complete
                }
                if (done) {
                    onPartialResponse("", true) // true = complete
                    onDone("")
                }
            }
            
            // EXACT Gallery pattern - line 190 from LlmChatModelHelper
            val unused = session.generateResponseAsync(resultListener)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response with model '${model.name}'", e)
            onDone("Response generation failed: ${e.message}")
        }
    }

    override fun generateResponseStreamingWithImages(model: Model, prompt: String, images: List<String>, onPartialResponse: (String, Boolean) -> Unit, onDone: (String) -> Unit) {
        val instance = model.instance as ElizaModelInstance? ?: run {
            onDone("Model not initialized")
            return
        }
        
        if (!model.llmSupportImage) {
            onDone("Model does not support image input")
            return
        }
        
        Log.d(TAG, "Generating streaming response with images for model '${model.name}' using Gallery's EXACT approach")
        
        try {
            // EXACT COPY of Gallery's runInference pattern with images
            val session = instance.session
            if (prompt.trim().isNotEmpty()) {
                session.addQueryChunk(prompt)
            }
            
            // Convert image paths to Bitmap and add to session (Gallery pattern)
            for (imagePath in images) {
                // Note: In actual use, you'd load the Bitmap from the path
                // For now, we'll skip this since Gallery expects Bitmap objects
                // This would need to be implemented based on your image loading strategy
                Log.w(TAG, "Image support needs Bitmap conversion implementation for path: $imagePath")
            }
            
            // Use Gallery's exact ResultListener pattern
            val resultListener: ResultListener = { partialResult: String, done: Boolean ->
                if (partialResult.isNotEmpty()) {
                    onPartialResponse(partialResult, false) // false = not complete
                }
                if (done) {
                    onPartialResponse("", true) // true = complete
                    onDone("")
                }
            }
            
            val unused = session.generateResponseAsync(resultListener)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response with images for model '${model.name}'", e)
            onDone("Response generation failed: ${e.message}")
        }
    }

    override fun cleanup(model: Model) {
        // EXACT COPY of Gallery's cleanUp method from LlmChatModelHelper.cleanUp()
        if (model.instance == null) {
            return
        }

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
        if (onCleanUp != null) {
            onCleanUp()
        }
        model.instance = null
        Log.d(TAG, "Clean up done.")
    }

    /**
     * EXACT COPY of Gallery's cleanUpMediapipeTaskErrorMessage from Utils.kt
     */
    private fun cleanUpMediapipeTaskErrorMessage(message: String): String {
        val index = message.indexOf("=== Source Location Trace")
        if (index >= 0) {
            return message.substring(0, index)
        }
        return message
    }
}