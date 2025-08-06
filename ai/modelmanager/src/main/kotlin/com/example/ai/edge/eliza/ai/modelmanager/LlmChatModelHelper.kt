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

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
// Import Gallery-compatible classes from core.model
import com.example.ai.edge.eliza.core.model.Accelerator
import com.example.ai.edge.eliza.core.model.ConfigKey
import com.example.ai.edge.eliza.core.model.DEFAULT_MAX_TOKEN
import com.example.ai.edge.eliza.core.model.DEFAULT_TEMPERATURE
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPK
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPP
import com.example.ai.edge.eliza.core.model.MAX_IMAGE_COUNT
import com.example.ai.edge.eliza.core.model.Model
// cleanUpMediapipeTaskErrorMessage function now defined locally in Gallery style
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession

private const val TAG = "ElizaLlmChatModelHelper"

/**
 * Gallery's exact MediaPipe LLM inference helper
 * Copied exactly from Gallery's LlmChatModelHelper.kt
 */

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit
typealias CleanUpListener = () -> Unit

data class LlmModelInstance(val engine: LlmInference, var session: LlmInferenceSession)

object LlmChatModelHelper {
    // Indexed by model name.
    private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()

    fun initialize(context: Context, model: Model, onDone: (String) -> Unit) {
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

        // Create an instance of the LLM Inference task and session.
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
            model.instance = LlmModelInstance(engine = llmInference, session = session)
        } catch (e: Exception) {
            onDone(cleanUpMediapipeTaskErrorMessage(e.message ?: "Unknown error"))
            return
        }
        onDone("")
    }

    fun resetSession(model: Model) {
        try {
            Log.d(TAG, "Resetting session for model '${model.name}'")

            val instance = model.instance as LlmModelInstance? ?: return
            val session = instance.session
            session.close()

            val inference = instance.engine
            val topK = model.getIntConfigValue(key = ConfigKey.TOPK, defaultValue = DEFAULT_TOPK)
            val topP = model.getFloatConfigValue(key = ConfigKey.TOPP, defaultValue = DEFAULT_TOPP)
            val temperature = model.getFloatConfigValue(key = ConfigKey.TEMPERATURE, defaultValue = DEFAULT_TEMPERATURE)
            
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
                    .build(),
            )
            instance.session = newSession
            Log.d(TAG, "Resetting done")
        } catch (e: Exception) {
            Log.d(TAG, "Failed to reset session", e)
        }
    }

    fun stopResponse(model: Model) {
        Log.d(TAG, "Stopping response for model ${model.name}...")
        try {
            val instance = model.instance as LlmModelInstance?
            if (instance != null) {
                instance.session.cancelGenerateResponseAsync()
                Log.d(TAG, "Successfully cancelled response for model ${model.name}")
            } else {
                Log.w(TAG, "Model instance is null, cannot cancel response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping response for model ${model.name}", e)
        }
    }

    fun cleanUp(model: Model) {
        if (model.instance == null) {
            return
        }

        val instance = model.instance as LlmModelInstance

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

    fun runInference(
        model: Model,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        images: List<Bitmap> = listOf(),
        audioClips: List<ByteArray> = listOf(),
    ) {
        Log.d(TAG, "runInference called for model: ${model.name}")
        Log.d(TAG, "Input text: ${input.take(100)}...")
        
        if (model.instance == null) {
            Log.e(TAG, "Model instance is null! Cannot run inference.")
            resultListener("Model instance is null", true)
            return
        }
        
        val instance = model.instance as LlmModelInstance
        Log.d(TAG, "Model instance available: ${instance}")

        // Set listener.
        if (!cleanUpListeners.containsKey(model.name)) {
            cleanUpListeners[model.name] = cleanUpListener
        }

        // Start async inference.
        //
        // For a model that supports image modality, we need to add the text query chunk before adding
        // image.
        val session = instance.session
        Log.d(TAG, "Adding query chunk to session...")
        
        if (input.trim().isNotEmpty()) {
            session.addQueryChunk(input)
        }
        for (image in images) {
            session.addImage(BitmapImageBuilder(image).build())
        }
        for (audioClip in audioClips) {
            // Uncomment when audio is supported.
            // session.addAudio(audioClip)
        }
        
        Log.d(TAG, "Starting async response generation...")
        val unused = session.generateResponseAsync { partialResult, done ->
            Log.d(TAG, "Response callback - done: $done, partial: ${partialResult.take(50)}...")
            resultListener(partialResult, done)
        }
        Log.d(TAG, "generateResponseAsync call completed")
    }
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