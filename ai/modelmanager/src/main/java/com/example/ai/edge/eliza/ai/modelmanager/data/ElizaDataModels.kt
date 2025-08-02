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
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import java.util.regex.Pattern

// Import Gallery-compatible classes from core.model
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelDataFile
import com.example.ai.edge.eliza.core.model.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.core.model.Config
import com.example.ai.edge.eliza.core.model.ConfigKey
import com.example.ai.edge.eliza.core.model.ValueType
import com.example.ai.edge.eliza.core.model.Accelerator
import com.example.ai.edge.eliza.core.model.createLlmChatConfigs
import com.example.ai.edge.eliza.core.model.convertValueToTargetType
import com.example.ai.edge.eliza.core.model.DEFAULT_MAX_TOKEN
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPK
import com.example.ai.edge.eliza.core.model.DEFAULT_TOPP
import com.example.ai.edge.eliza.core.model.DEFAULT_TEMPERATURE
import com.example.ai.edge.eliza.core.model.DEFAULT_ACCELERATORS
import com.example.ai.edge.eliza.core.model.MAX_IMAGE_COUNT
import com.example.ai.edge.eliza.core.model.MAX_AUDIO_CLIP_COUNT
import com.example.ai.edge.eliza.core.model.SAMPLE_RATE

/** Type of task. Simplified for Eliza. */
enum class TaskType(val label: String, val id: String) {
  ELIZA_CHAT(label = "Eliza", id = "eliza_chat"),
  ELIZA_EXERCISE_HELP(label = "Exercise Help", id = "eliza_exercise_help"),
}

/** Data class for a task. Copied from Gallery. */
data class Task(
  /** Type of the task. */
  val type: TaskType,

  /** Icon to be shown in the task tile. */
  val icon: ImageVector? = null,

  /** Vector resource id for the icon. This precedes the icon if both are set. */
  val iconVectorResourceId: Int? = null,

  /** List of models for the task. */
  val models: MutableList<Model>,

  /** Description of the task. */
  val description: String,

  /** Documentation url for the task. */
  val docUrl: String = "",

  /** Source code url for the model-related functions. */
  val sourceCodeUrl: String = "",

  /** Placeholder text for the name of the agent shown above chat messages. */
  @StringRes val agentNameRes: Int = android.R.string.unknownName, // Default fallback

  /** Placeholder text for the text input field. */
  @StringRes val textInputPlaceHolderRes: Int = android.R.string.search_go, // Default fallback

  // The following fields are managed by the app. Don't need to set manually.
  var index: Int = -1,
  val updateTrigger: MutableState<Long> = mutableLongStateOf(0),
)

// All constants and functions are now imported from core.model to ensure Gallery compatibility

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

/** Eliza's main chat task. */
val TASK_ELIZA_CHAT =
  Task(
    type = TaskType.ELIZA_CHAT,
    icon = Icons.Outlined.Star,
    models = mutableListOf(),
    description = "Chat with Eliza for personalized learning assistance",
    docUrl = "https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android",
  )

/** Eliza's exercise help task. */
val TASK_ELIZA_EXERCISE_HELP =
  Task(
    type = TaskType.ELIZA_EXERCISE_HELP,
    icon = Icons.Outlined.Star,
    models = mutableListOf(),
    description = "Get help with specific exercises and questions",
    docUrl = "https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android",
  )

/** All tasks for Eliza. */
val ELIZA_TASKS: List<Task> = listOf(TASK_ELIZA_CHAT, TASK_ELIZA_EXERCISE_HELP)

fun getModelByName(name: String): Model? {
  for (task in ELIZA_TASKS) {
    for (model in task.models) {
      if (model.name == name) {
        return model
      }
    }
  }
  return null
}

fun processTasks() {
  for ((index, task) in ELIZA_TASKS.withIndex()) {
    task.index = index
    for (model in task.models) {
      model.preProcess()
    }
  }
}