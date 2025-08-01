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

import android.content.Context
import java.io.File
import java.util.regex.Pattern

// Copied exactly from Gallery's Model.kt
private val NORMALIZE_NAME_REGEX = Pattern.compile("[^a-zA-Z0-9_]")

/** Different types of accelerators. */
enum class Accelerator(val label: String) {
  CPU("CPU"),
  GPU("GPU"),
}

/** Configuration value types. */
enum class ValueType {
  INT,
  FLOAT,
  BOOLEAN,
  STRING,
}

/** Config key for model configuration. */
data class ConfigKey(val label: String, val valueType: ValueType)

/** Config for a model. */
data class Config(
  val key: ConfigKey,
  val defaultValue: Any,
  val description: String = "",
  val min: Float? = null,
  val max: Float? = null,
  val options: List<String>? = null, // For string configs with predefined options
)

/** Additional data file for a model. */
data class DataFile(
  val fileName: String,
  val downloadUrl: String,
  val sizeInBytes: Long,
)

/** Model configuration. Copied exactly from Gallery. */
data class Model(
  /** Model name as shown to the user. */
  val name: String,

  /** Brief description. */
  val description: String = "",

  /** Model variant to download. */
  val downloadFileName: String = "",

  /** URL to download the model. */
  val downloadUrl: String = "",

  /** Size of the file in bytes. */
  val sizeInBytes: Long = 0L,

  /** Whether the download file is a zip. */
  val isZip: Boolean = false,

  /** Directory to unzip the files to. Only applicable for zip files. */
  val unzipDir: String = "",

  /** Model version. */
  val version: String = "1.0",

  /** Configuration values for this model. */
  val configs: List<Config> = listOf(),

  /** Whether this model supports image input. */
  val llmSupportImage: Boolean = false,

  /** Whether this model supports audio input. */
  val llmSupportAudio: Boolean = false,

  /** Whether this model supports prompts/templates. */
  val llmPromptTemplates: List<String> = listOf(),

  /** Whether to show run again button for this model. */
  val showRunAgainButton: Boolean = true,

  /** Whether to show benchmark button for this model. */
  val showBenchmarkButton: Boolean = false,

  /** Additional data files. */
  val extraDataFiles: List<DataFile> = listOf(),

  /** Whether this model is imported from user device. */
  val imported: Boolean = false,

  // Runtime fields (copied from Gallery)
  var normalizedName: String = "",
  var instance: Any? = null,
  var initializing: Boolean = false,
  var cleanUpAfterInit: Boolean = false,
  var configValues: Map<String, Any> = mapOf(),
  var totalBytes: Long = 0L,
  var accessToken: String? = null,

  /** The estimated peak memory in byte to run the model. */
  val estimatedPeakMemoryInBytes: Long? = null,
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
    
    /**
     * Gallery's exact configuration value getters
     * Copied exactly from Gallery's Model.kt
     */
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
} 