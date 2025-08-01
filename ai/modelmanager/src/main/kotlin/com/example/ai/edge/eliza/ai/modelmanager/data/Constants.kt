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

package com.example.ai.edge.eliza.ai.modelmanager.data

/**
 * Gallery constants for model configuration
 * Copied exactly from Gallery's Consts.kt
 */

// Default values for LLM models (Gallery's exact values)
const val DEFAULT_MAX_TOKEN = 1024
const val DEFAULT_TEMPERATURE = 1.0f
const val DEFAULT_TOPK = 40
const val DEFAULT_TOPP = 0.9f

// Default accelerators (Gallery's exact default)
val DEFAULT_ACCELERATORS = listOf(Accelerator.GPU)

// Max number of images allowed in a "ask image" session
const val MAX_IMAGE_COUNT = 10

// Max number of audio clip in an "ask audio" session  
const val MAX_AUDIO_CLIP_COUNT = 1

// Audio recording sample rate
const val SAMPLE_RATE = 16000

// Config keys (Gallery's exact implementation) 
val CONFIG_MAX_TOKENS = ConfigKey("Max tokens", ValueType.INT)
val CONFIG_TOPK = ConfigKey("TopK", ValueType.INT)
val CONFIG_TOPP = ConfigKey("TopP", ValueType.FLOAT) 
val CONFIG_TEMPERATURE = ConfigKey("Temperature", ValueType.FLOAT)
val CONFIG_ACCELERATOR = ConfigKey("Choose accelerator", ValueType.STRING)

/**
 * Gallery's exact convertValueToTargetType function
 * Copied exactly from Gallery's Config.kt
 */
fun convertValueToTargetType(value: Any, valueType: ValueType): Any {
    return when (valueType) {
        ValueType.INT ->
            when (value) {
                is Int -> value
                is Float -> value.toInt()
                is Double -> value.toInt()
                is String -> value.toIntOrNull() ?: 0
                is Boolean -> if (value) 1 else 0
                else -> 0
            }

        ValueType.FLOAT ->
            when (value) {
                is Int -> value.toFloat()
                is Float -> value
                is Double -> value.toFloat()
                is String -> value.toFloatOrNull() ?: 0f
                is Boolean -> if (value) 1f else 0f
                else -> 0f
            }

        ValueType.BOOLEAN ->
            when (value) {
                is Int -> value != 0
                is Boolean -> value
                is Float -> kotlin.math.abs(value) > 1e-6
                is Double -> kotlin.math.abs(value) > 1e-6
                is String -> value.isNotEmpty()
                else -> false
            }

        ValueType.STRING -> value.toString()
    }
}

/**
 * Gallery's exact MediaPipe error message cleanup function
 * Copied exactly from Gallery's Utils.kt
 */
fun cleanUpMediapipeTaskErrorMessage(message: String): String {
    val index = message.indexOf("=== Source Location Trace")
    if (index >= 0) {
        return message.substring(0, index)
    }
    return message
} 