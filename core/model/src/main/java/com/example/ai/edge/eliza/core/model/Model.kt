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

package com.example.ai.edge.eliza.core.model

import android.content.Context
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File
import java.util.regex.Pattern

private val NORMALIZE_NAME_REGEX = Pattern.compile("[^a-zA-Z0-9_]")

/**
 * Represents an AI model with its configuration and metadata.
 * Adapted from Gallery's Model class for educational AI use cases.
 */
@Serializable
data class Model(
    /** The name (for display purpose) of the model. */
    val name: String,

    /** The version of the model. */
    val version: String = "20250520",

    /**
     * The name of the downloaded model file.
     * The final file path of the downloaded model will be:
     * {context.getExternalFilesDir}/{normalizedName}/{version}/{downloadFileName}
     */
    val downloadFileName: String,

    /** The URL to download the model from. */
    val url: String,

    /** The size of the model file in bytes. */
    val sizeInBytes: Long,

    /** A list of additional data files required by the model. */
    val extraDataFiles: List<ModelDataFile> = listOf(),

    /**
     * A description or information about the model.
     * Will be shown at the start of the chat session and in the expanded model item.
     */
    val info: String = "",

    /** The url to jump to when clicking "learn more" in expanded model item. */
    val learnMoreUrl: String = "",

    /** A list of configurable parameters for the model. */
    val configs: List<Config> = listOf(),

    /** Whether the LLM model supports image input. */
    val llmSupportImage: Boolean = false,

    /** Whether the LLM model supports audio input. */
    val llmSupportAudio: Boolean = false,

    /** Whether the model is imported or not. */
    val imported: Boolean = false,

    /** SHA-256 checksum for model file integrity verification (optional). */
    val sha256Checksum: String? = null,

    /** The estimated peak memory in byte to run the model. */
    val estimatedPeakMemoryInBytes: Long? = null,

    // The following fields are managed by the app. Don't need to set manually.
    var normalizedName: String = "",
    @Contextual var instance: Any? = null,
    var initializing: Boolean = false,
    var cleanUpAfterInit: Boolean = false,
    var configValues: Map<String, @Contextual Any> = mapOf(),
    var totalBytes: Long = 0L,
    var accessToken: String? = null,
) {
    init {
        normalizedName = NORMALIZE_NAME_REGEX.matcher(name).replaceAll("_")
    }

    fun preProcess() {
        val configValues: MutableMap<String, Any> = mutableMapOf()
        for (config in this.configs) {
            configValues[config.key.label] = config.defaultValue
        }
        this.configValues = configValues
        this.totalBytes = this.sizeInBytes + this.extraDataFiles.sumOf { it.sizeInBytes }
    }

    fun getPath(context: Context, fileName: String = downloadFileName): String {
        if (imported) {
            return listOf(context.getExternalFilesDir(null)?.absolutePath ?: "", fileName)
                .joinToString(File.separator)
        }

        val baseDir = listOf(
            context.getExternalFilesDir(null)?.absolutePath ?: "",
            normalizedName,
            version
        ).joinToString(File.separator)
        
        return "$baseDir/$fileName"
    }

    fun getIntConfigValue(key: ConfigKey, defaultValue: Int = 0): Int {
        return getTypedConfigValue(key = key, valueType = ValueType.INT, defaultValue = defaultValue) as Int
    }

    fun getFloatConfigValue(key: ConfigKey, defaultValue: Float = 0.0f): Float {
        return getTypedConfigValue(key = key, valueType = ValueType.FLOAT, defaultValue = defaultValue) as Float
    }

    fun getBooleanConfigValue(key: ConfigKey, defaultValue: Boolean = false): Boolean {
        return getTypedConfigValue(key = key, valueType = ValueType.BOOLEAN, defaultValue = defaultValue) as Boolean
    }

    fun getStringConfigValue(key: ConfigKey, defaultValue: String = ""): String {
        return getTypedConfigValue(key = key, valueType = ValueType.STRING, defaultValue = defaultValue) as String
    }

    private fun getTypedConfigValue(key: ConfigKey, valueType: ValueType, defaultValue: Any): Any {
        return convertValueToTargetType(
            value = configValues.getOrDefault(key.label, defaultValue),
            valueType = valueType,
        )
    }
}

/**
 * Represents additional data files required by a model.
 */
@Serializable
data class ModelDataFile(
    val name: String,
    val url: String,
    val downloadFileName: String,
    val sizeInBytes: Long,
)

/**
 * Enum representing the download status of a model.
 */
enum class ModelDownloadStatusType {
    NOT_DOWNLOADED,
    PARTIALLY_DOWNLOADED,
    IN_PROGRESS,
    UNZIPPING,
    SUCCEEDED,
    FAILED,
}

/**
 * Represents the download status of a model.
 */
@Serializable
data class ModelDownloadStatus(
    val status: ModelDownloadStatusType,
    val totalBytes: Long = 0,
    val receivedBytes: Long = 0,
    val errorMessage: String = "",
    val bytesPerSecond: Long = 0,
    val remainingMs: Long = 0,
)

/**
 * Configuration for model parameters.
 */
@Serializable
data class Config(
    val key: ConfigKey,
    @Contextual val defaultValue: Any,
    val needReinitialization: Boolean = true
)

/**
 * Configuration keys for model parameters.
 */
@Serializable
enum class ConfigKey(val label: String) {
    MAX_TOKENS("Max Tokens"),
    TOPK("Top K"),
    TOPP("Top P"),
    TEMPERATURE("Temperature"),
    ACCELERATOR("Accelerator"),
    USE_GPU("Use GPU"),
    MAX_RESULT_COUNT("Max Result Count"),
    ITERATIONS("Iterations")
}

/**
 * Value types for configurations.
 */
@Serializable
enum class ValueType {
    INT,
    FLOAT,
    BOOLEAN,
    STRING
}

/**
 * Accelerator types for model execution.
 */
@Serializable
enum class Accelerator(val label: String) {
    CPU("CPU"),
    GPU("GPU")
}

/**
 * Converts a value to the target type.
 */
private fun convertValueToTargetType(value: Any, valueType: ValueType): Any {
    return when (valueType) {
        ValueType.INT -> when (value) {
            is Int -> value
            is Float -> value.toInt()
            is Double -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
        ValueType.FLOAT -> when (value) {
            is Float -> value
            is Int -> value.toFloat()
            is Double -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0.0f
            else -> 0.0f
        }
        ValueType.BOOLEAN -> when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> false
        }
        ValueType.STRING -> value.toString()
    }
}

/**
 * Creates LLM chat configurations with default values.
 */
fun createLlmChatConfigs(
    defaultMaxToken: Int = 4096,
    defaultTopK: Int = 64,
    defaultTopP: Float = 0.95f,
    defaultTemperature: Float = 1.0f,
    accelerators: List<Accelerator> = listOf(Accelerator.GPU, Accelerator.CPU)
): List<Config> {
    return listOf(
        Config(
            key = ConfigKey.MAX_TOKENS,
            defaultValue = defaultMaxToken
        ),
        Config(
            key = ConfigKey.TOPK,
            defaultValue = defaultTopK
        ),
        Config(
            key = ConfigKey.TOPP,
            defaultValue = defaultTopP
        ),
        Config(
            key = ConfigKey.TEMPERATURE,
            defaultValue = defaultTemperature
        ),
        Config(
            key = ConfigKey.ACCELERATOR,
            defaultValue = accelerators.first().label
        )
    )
}

/**
 * Pre-configured Gemma 3N model for educational AI.
 */
val GEMMA_3N_E4B_MODEL = Model(
    name = "Gemma-3n-E4B-it-int4",
    version = "20250520",
    downloadFileName = "gemma-3n-E4B-it-int4.task",
    url = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview/resolve/main/gemma-3n-E4B-it-int4.task?download=true",
    sizeInBytes = 4405655031L,
    estimatedPeakMemoryInBytes = 6979321856L,
    llmSupportImage = true,
    llmSupportAudio = false,
    info = "Gemma 3n E4B optimized for educational AI tutoring with text and vision capabilities",
    learnMoreUrl = "https://ai.google.dev/gemma/docs/gemma-3n",
    configs = createLlmChatConfigs(
        defaultMaxToken = 4096,
        defaultTopK = 64,
        defaultTopP = 0.95f,
        defaultTemperature = 1.0f,
        accelerators = listOf(Accelerator.GPU, Accelerator.CPU)
    )
).apply {
    preProcess()
} 