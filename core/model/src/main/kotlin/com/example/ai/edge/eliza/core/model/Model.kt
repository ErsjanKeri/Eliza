/*
 * Copyright 2025 AI Edge Eliza - Based on Google Gallery
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
import kotlin.math.abs
import java.io.File

/**
 * EXACT COPY of Gallery's Model implementation
 * This ensures 100% compatibility with Gallery's model management system
 */

data class ModelDataFile(
  val name: String,
  val url: String,
  val downloadFileName: String,
  val sizeInBytes: Long,
)

const val IMPORTS_DIR = "__imports"
private val NORMALIZE_NAME_REGEX = Regex("[^a-zA-Z0-9]")

data class PromptTemplate(val title: String, val description: String, val prompt: String)

/** A model for a task - EXACT COPY from Gallery */
data class Model(
  /** The name (for display purpose) of the model. */
  val name: String,

  /** The version of the model. */
  val version: String = "_",

  /**
   * The name of the downloaded model file.
   *
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
   *
   * Will be shown at the start of the chat session and in the expanded model item.
   */
  val info: String = "",

  /** The url to jump to when clicking "learn more" in expanded model item. */
  val learnMoreUrl: String = "",

  /** A list of configurable parameters for the model. */
  val configs: List<Config> = listOf(),

  /** Whether to show the "run again" button in the UI. */
  val showRunAgainButton: Boolean = true,

  /** Whether to show the "benchmark" button in the UI. */
  val showBenchmarkButton: Boolean = true,

  /** Indicates whether the model is a zip file. */
  val isZip: Boolean = false,

  /** The name of the directory to unzip the model to (if it's a zip file). */
  val unzipDir: String = "",

  /** The prompt templates for the model (only for LLM). */
  val llmPromptTemplates: List<PromptTemplate> = listOf(),

  /** Whether the LLM model supports image input. */
  val llmSupportImage: Boolean = false,

  /** Whether the LLM model supports audio input. */
  val llmSupportAudio: Boolean = false,

  /** Whether the model is imported or not. */
  val imported: Boolean = false,

  // The following fields are managed by the app. Don't need to set manually.
  var normalizedName: String = "",
  var instance: Any? = null,
  var initializing: Boolean = false,
  // TODO(jingjin): use a "queue" system to manage model init and cleanup.
  var cleanUpAfterInit: Boolean = false,
  var configValues: Map<String, Any> = mapOf(),
  var totalBytes: Long = 0L,
  var accessToken: String? = null,

  /** The estimated peak memory in byte to run the model. */
  val estimatedPeakMemoryInBytes: Long? = null,
) {
  init {
    normalizedName = NORMALIZE_NAME_REGEX.replace(name, "_")
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

    val baseDir =
      listOf(context.getExternalFilesDir(null)?.absolutePath ?: "", normalizedName, version)
        .joinToString(File.separator)
    return if (this.isZip && this.unzipDir.isNotEmpty()) {
      "$baseDir/${this.unzipDir}"
    } else {
      "$baseDir/$fileName"
    }
  }

  fun getIntConfigValue(key: ConfigKey, defaultValue: Int = 0): Int {
    return getTypedConfigValue(key = key, valueType = ValueType.INT, defaultValue = defaultValue)
      as Int
  }

  fun getFloatConfigValue(key: ConfigKey, defaultValue: Float = 0.0f): Float {
    return getTypedConfigValue(key = key, valueType = ValueType.FLOAT, defaultValue = defaultValue)
      as Float
  }

  fun getBooleanConfigValue(key: ConfigKey, defaultValue: Boolean = false): Boolean {
    return getTypedConfigValue(
      key = key,
      valueType = ValueType.BOOLEAN,
      defaultValue = defaultValue,
    )
      as Boolean
  }

  fun getStringConfigValue(key: ConfigKey, defaultValue: String = ""): String {
    return getTypedConfigValue(key = key, valueType = ValueType.STRING, defaultValue = defaultValue)
      as String
  }

  fun getExtraDataFile(name: String): ModelDataFile? {
    return extraDataFiles.find { it.name == name }
  }

  private fun getTypedConfigValue(key: ConfigKey, valueType: ValueType, defaultValue: Any): Any {
    return convertValueToTargetType(
      value = configValues.getOrDefault(key.label, defaultValue),
      valueType = valueType,
    )
  }
}

enum class ModelDownloadStatusType {
  NOT_DOWNLOADED,
  PARTIALLY_DOWNLOADED,
  IN_PROGRESS,
  UNZIPPING,
  SUCCEEDED,
  FAILED,
}

data class ModelDownloadStatus(
  val status: ModelDownloadStatusType,
  val totalBytes: Long = 0,
  val receivedBytes: Long = 0,
  val errorMessage: String = "",
  val bytesPerSecond: Long = 0,
  val remainingMs: Long = 0,
)

/** Video explanation status types - follows ModelDownloadStatusType pattern */
enum class VideoExplanationStatusType {
  NOT_STARTED,
  QUEUED,
  GENERATING_SCRIPT,
  RENDERING_VIDEO,
  DOWNLOADING,
  COMPLETED,
  FAILED,
}

/** Video explanation status - follows ModelDownloadStatus pattern */
data class VideoExplanationStatus(
  val status: VideoExplanationStatusType,
  val progress: Int = 0, // 0-100 percentage
  val currentMessage: String = "",
  val videoId: String = "",
  val localFilePath: String? = null,
  val durationSeconds: Int = 0,
  val fileSizeBytes: Long = 0,
  val errorMessage: String = "",
  val startedAt: Long = System.currentTimeMillis(),
  val errorInfo: VideoErrorInfo? = null, // Enhanced error information
  val retryCount: Int = 0, // Number of retry attempts
  val canRetry: Boolean = false, // Whether retry is possible
  val lastRetryAt: Long = 0L // Timestamp of last retry attempt
) {
  
  /**
   * Create a new status with updated retry information.
   */
  fun withRetry(): VideoExplanationStatus {
    return copy(
      retryCount = retryCount + 1,
      lastRetryAt = System.currentTimeMillis()
    )
  }
  
  /**
   * Create a failed status with error information.
   */
  fun withError(errorInfo: VideoErrorInfo): VideoExplanationStatus {
    return copy(
      status = VideoExplanationStatusType.FAILED,
      errorMessage = errorInfo.message,
      errorInfo = errorInfo,
      canRetry = errorInfo.isRetryable && retryCount < errorInfo.maxRetries,
      currentMessage = errorInfo.message
    )
  }
  
  /**
   * Check if enough time has passed since last retry to attempt again.
   */
  fun canRetryNow(): Boolean {
    if (!canRetry || errorInfo == null) return false
    val timeSinceLastRetry = System.currentTimeMillis() - lastRetryAt
    return timeSinceLastRetry >= errorInfo.retryDelayMs
  }
}

/**
 * The types of configuration editors available.
 *
 * This enum defines the different UI components used to edit configuration values. Each type
 * corresponds to a specific editor widget, such as a slider or a switch.
 */
enum class ConfigEditorType {
  LABEL,
  NUMBER_SLIDER,
  BOOLEAN_SWITCH,
  DROPDOWN,
}

/** The data types of configuration values. */
enum class ValueType {
  INT,
  FLOAT,
  DOUBLE,
  STRING,
  BOOLEAN,
}

enum class ConfigKey(val label: String) {
  MAX_TOKENS("Max tokens"),
  TOPK("TopK"),
  TOPP("TopP"),
  TEMPERATURE("Temperature"),
  DEFAULT_MAX_TOKENS("Default max tokens"),
  DEFAULT_TOPK("Default TopK"),
  DEFAULT_TOPP("Default TopP"),
  DEFAULT_TEMPERATURE("Default temperature"),
  SUPPORT_IMAGE("Support image"),
  SUPPORT_AUDIO("Support audio"),
  MAX_RESULT_COUNT("Max result count"),
  USE_GPU("Use GPU"),
  ACCELERATOR("Choose accelerator"),
  COMPATIBLE_ACCELERATORS("Compatible accelerators"),
  WARM_UP_ITERATIONS("Warm up iterations"),
  BENCHMARK_ITERATIONS("Benchmark iterations"),
  ITERATIONS("Iterations"),
  THEME("Theme"),
  NAME("Name"),
  MODEL_TYPE("Model type"),
}

/**
 * Base class for configuration settings.
 *
 * @param type The type of configuration editor.
 * @param key The unique key for the configuration setting.
 * @param defaultValue The default value for the configuration setting.
 * @param valueType The data type of the configuration value.
 * @param needReinitialization Indicates whether the model needs to be reinitialized after changing
 *   this config.
 */
open class Config(
  val type: ConfigEditorType,
  open val key: ConfigKey,
  open val defaultValue: Any,
  open val valueType: ValueType,
  open val needReinitialization: Boolean = true,
)

/** Configuration setting for a label. */
class LabelConfig(override val key: ConfigKey, override val defaultValue: String = "") :
  Config(
    type = ConfigEditorType.LABEL,
    key = key,
    defaultValue = defaultValue,
    valueType = ValueType.STRING,
  )

/**
 * Configuration setting for a number slider.
 *
 * @param sliderMin The minimum value of the slider.
 * @param sliderMax The maximum value of the slider.
 */
class NumberSliderConfig(
  override val key: ConfigKey,
  val sliderMin: Float,
  val sliderMax: Float,
  override val defaultValue: Float,
  override val valueType: ValueType,
  override val needReinitialization: Boolean = true,
) :
  Config(
    type = ConfigEditorType.NUMBER_SLIDER,
    key = key,
    defaultValue = defaultValue,
    valueType = valueType,
  )

/** Configuration setting for a boolean switch. */
class BooleanSwitchConfig(
  override val key: ConfigKey,
  override val defaultValue: Boolean,
  override val needReinitialization: Boolean = true,
) :
  Config(
    type = ConfigEditorType.BOOLEAN_SWITCH,
    key = key,
    defaultValue = defaultValue,
    valueType = ValueType.BOOLEAN,
  )

/** Configuration setting for a dropdown. */
class SegmentedButtonConfig(
  override val key: ConfigKey,
  override val defaultValue: String,
  val options: List<String>,
  val allowMultiple: Boolean = false,
) :
  Config(
    type = ConfigEditorType.DROPDOWN,
    key = key,
    defaultValue = defaultValue,
    // The emitted value will be comma-separated labels when allowMultiple=true.
    valueType = ValueType.STRING,
  )

fun convertValueToTargetType(value: Any, valueType: ValueType): Any {
  return when (valueType) {
    ValueType.INT ->
      when (value) {
        is Int -> value
        is Float -> value.toInt()
        is Double -> value.toInt()
        is String -> value.toIntOrNull() ?: ""
        is Boolean -> if (value) 1 else 0
        else -> ""
      }

    ValueType.FLOAT ->
      when (value) {
        is Int -> value.toFloat()
        is Float -> value
        is Double -> value.toFloat()
        is String -> value.toFloatOrNull() ?: ""
        is Boolean -> if (value) 1f else 0f
        else -> ""
      }

    ValueType.DOUBLE ->
      when (value) {
        is Int -> value.toDouble()
        is Float -> value.toDouble()
        is Double -> value
        is String -> value.toDoubleOrNull() ?: ""
        is Boolean -> if (value) 1.0 else 0.0
        else -> ""
      }

    ValueType.BOOLEAN ->
      when (value) {
        is Int -> value == 0
        is Boolean -> value
        is Float -> abs(value) > 1e-6
        is Double -> abs(value) > 1e-6
        is String -> value.isNotEmpty()
        else -> false
      }

    ValueType.STRING -> value.toString()
  }
}

fun createLlmChatConfigs(
  defaultMaxToken: Int = DEFAULT_MAX_TOKEN,
  defaultTopK: Int = DEFAULT_TOPK,
  defaultTopP: Float = DEFAULT_TOPP,
  defaultTemperature: Float = DEFAULT_TEMPERATURE,
  accelerators: List<Accelerator> = DEFAULT_ACCELERATORS,
): List<Config> {
  return listOf(
    LabelConfig(key = ConfigKey.MAX_TOKENS, defaultValue = "$defaultMaxToken"),
    NumberSliderConfig(
      key = ConfigKey.TOPK,
      sliderMin = 5f,
      sliderMax = 100f,
      defaultValue = defaultTopK.toFloat(),
      valueType = ValueType.INT,
    ),
    NumberSliderConfig(
      key = ConfigKey.TOPP,
      sliderMin = 0.0f,
      sliderMax = 1.0f,
      defaultValue = defaultTopP,
      valueType = ValueType.FLOAT,
    ),
    NumberSliderConfig(
      key = ConfigKey.TEMPERATURE,
      sliderMin = 0.0f,
      sliderMax = 2.0f,
      defaultValue = defaultTemperature,
      valueType = ValueType.FLOAT,
    ),
    SegmentedButtonConfig(
      key = ConfigKey.ACCELERATOR,
      defaultValue = accelerators[0].label,
      options = accelerators.map { it.label },
    ),
  )
}

fun getConfigValueString(value: Any, config: Config): String {
  var strNewValue = "$value"
  if (config.valueType == ValueType.FLOAT) {
    strNewValue = "%.2f".format(value)
  }
  return strNewValue
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Types and Constants - EXACT COPY from Gallery

enum class Accelerator(val label: String) {
  CPU(label = "CPU"),
  GPU(label = "GPU"),
}

// Default values for LLM models - EXACT COPY from Gallery
const val DEFAULT_MAX_TOKEN = 1024
const val DEFAULT_TOPK = 40
const val DEFAULT_TOPP = 0.9f
const val DEFAULT_TEMPERATURE = 1.0f
val DEFAULT_ACCELERATORS = listOf(Accelerator.GPU)

// Max number of images allowed in a "ask image" session.
const val MAX_IMAGE_COUNT = 10

// Max number of audio clip in an "ask audio" session.
const val MAX_AUDIO_CLIP_COUNT = 1

// Max audio clip duration in seconds.
const val MAX_AUDIO_CLIP_DURATION_SEC = 30

// Audio-recording related consts.
const val SAMPLE_RATE = 16000