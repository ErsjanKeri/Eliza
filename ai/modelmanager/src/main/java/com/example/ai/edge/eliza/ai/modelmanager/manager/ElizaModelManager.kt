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

package com.example.ai.edge.eliza.ai.modelmanager.manager

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.ai.modelmanager.LlmChatModelHelper
import com.example.ai.edge.eliza.ai.modelmanager.data.ELIZA_TASKS
import com.example.ai.edge.eliza.ai.modelmanager.data.Model
import com.example.ai.edge.eliza.ai.modelmanager.data.ModelDownloadStatus
import com.example.ai.edge.eliza.ai.modelmanager.data.ModelDownloadStatusType
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.data.TaskType
import com.example.ai.edge.eliza.ai.modelmanager.data.processTasks
import com.example.ai.edge.eliza.ai.modelmanager.data.createLlmChatConfigs
import com.example.ai.edge.eliza.ai.modelmanager.data.Accelerator
import com.example.ai.edge.eliza.ai.modelmanager.download.ModelDownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ElizaModelManager"

/** Model initialization status types - copied from Gallery. */
enum class ModelInitializationStatusType {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    ERROR,
}

/** Model initialization status with error information - copied from Gallery. */
data class ModelInitializationStatus(
    val status: ModelInitializationStatusType,
    val error: String = "",
)

// ModelDownloadStatus and ModelDownloadStatusType are now imported from data package

/** UI state for the model manager - adapted from Gallery. */
data class ModelManagerUiState(
    val tasks: List<Task> = emptyList(),
    val selectedModel: Model? = null,
    val modelDownloadStatus: Map<String, ModelDownloadStatus> = emptyMap(),
    val modelInitializationStatus: Map<String, ModelInitializationStatus> = emptyMap(),
    val textInputHistory: List<String> = emptyList(),
)

/**
 * Eliza Model Manager - Copied from Gallery's ModelManagerViewModel
 * Simplified for Eliza's needs but following Gallery's proven patterns
 */
@HiltViewModel
class ElizaModelManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadRepository: ModelDownloadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(createUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Process tasks and models like Gallery does
        processTasks()
        
        // Add Gemma model to tasks (simplified version)
        addGemmaModel()
    }

    private fun addGemmaModel() {
        // Create EXACT Gemma-3n-E4B-it-int4 model from Gallery's allowlist
        // This matches Gallery's model configuration exactly
        val modelId = "google/gemma-3n-E4B-it-litert-preview"
        val modelFile = "gemma-3n-E4B-it-int4.task"
        val downloadUrl = "https://huggingface.co/$modelId/resolve/main/$modelFile?download=true"
        
        // Gallery's exact configuration for this model
        val configs = createLlmChatConfigs(
            defaultMaxToken = 4096, // From Gallery allowlist
            defaultTopK = 64,       // From Gallery allowlist
            defaultTopP = 0.95f,    // From Gallery allowlist
            defaultTemperature = 1.0f, // From Gallery allowlist
            accelerators = listOf(Accelerator.CPU, Accelerator.GPU) // "cpu,gpu" from allowlist
        )
        
        val gemmaModel = Model(
            name = "Gemma-3n-E4B-it-int4", // Exact name from Gallery
            version = "20250520", // Exact version from Gallery
            downloadFileName = modelFile,
            downloadUrl = downloadUrl,
            sizeInBytes = 4405655031L, // Exact size from Gallery
            estimatedPeakMemoryInBytes = 6979321856L, // Exact memory from Gallery
            description = "Preview version of [Gemma 3n E4B](https://ai.google.dev/gemma/docs/gemma-3n) ready for deployment on Android using the [MediaPipe LLM Inference API](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference). The current checkpoint only supports text and vision input, with 4096 context length.",
            configs = configs,
            llmSupportImage = true, // From Gallery allowlist
            llmSupportAudio = false, // Not specified in Gallery allowlist
            showRunAgainButton = false, // Gallery sets this to false for LLM models
            showBenchmarkButton = false, // Gallery sets this to false for LLM models
        )
        
        // Add to both ELIZA tasks (both use the same model)
        ELIZA_TASKS.forEach { task ->
            task.models.add(gemmaModel)
        }
        
        // Pre-process the model (Gallery's pattern)
        gemmaModel.preProcess()
        
        // Set as selected model
        _uiState.update { currentState ->
            currentState.copy(selectedModel = gemmaModel)
        }
        
        Log.d(TAG, "Added exact Gallery Gemma-3n-E4B-it-int4 model to tasks")
    }

    fun initializeModel(context: Context, task: Task, model: Model, force: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            // Skip if initialized already.
            if (
                !force &&
                uiState.value.modelInitializationStatus[model.name]?.status ==
                ModelInitializationStatusType.INITIALIZED
            ) {
                Log.d(TAG, "Model '${model.name}' has been initialized. Skipping.")
                return@launch
            }

            // Skip if initialization is in progress.
            if (model.initializing) {
                model.cleanUpAfterInit = false
                Log.d(TAG, "Model '${model.name}' is being initialized. Skipping.")
                return@launch
            }

            // Clean up.
            cleanupModel(task = task, model = model)

            // Start initialization.
            Log.d(TAG, "Initializing model '${model.name}'...")
            model.initializing = true

            // Show initializing status after a delay.
            launch {
                delay(500)
                if (model.instance == null && model.initializing) {
                    updateModelInitializationStatus(
                        model = model,
                        status = ModelInitializationStatusType.INITIALIZING,
                    )
                }
            }

            val onDone: (error: String) -> Unit = { error ->
                model.initializing = false
                if (model.instance != null) {
                    Log.d(TAG, "Model '${model.name}' initialized successfully")
                    updateModelInitializationStatus(
                        model = model,
                        status = ModelInitializationStatusType.INITIALIZED,
                    )
                    if (model.cleanUpAfterInit) {
                        Log.d(TAG, "Model '${model.name}' needs cleaning up after init.")
                        cleanupModel(task = task, model = model)
                    }
                } else if (error.isNotEmpty()) {
                    Log.d(TAG, "Model '${model.name}' failed to initialize")
                    updateModelInitializationStatus(
                        model = model,
                        status = ModelInitializationStatusType.ERROR,
                        error = error,
                    )
                }
            }
            
            // Use LlmChatModelHelper for all Eliza tasks
            when (task.type) {
                TaskType.ELIZA_CHAT,
                TaskType.ELIZA_EXERCISE_HELP ->
                    LlmChatModelHelper.initialize(context = context, model = model, onDone = onDone)
            }
        }
    }

    fun cleanupModel(task: Task, model: Model) {
        try {
            Log.d(TAG, "Cleaning up model '${model.name}'")
            LlmChatModelHelper.cleanUp(model)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clean up model '${model.name}'", e)
        }
    }
    
    /**
     * Download a model - Gallery's exact WorkManager approach
     */
    fun downloadModel(context: Context, task: Task, model: Model) {
        Log.d(TAG, "Starting download for model '${model.name}' using Gallery WorkManager pattern")
        
        // Use real ModelDownloadRepository like Gallery
        modelDownloadRepository.downloadModel(
            model = model,
            onStatusUpdated = { downloadModel, status ->
                Log.d(TAG, "Download progress for '${downloadModel.name}': ${status.status}")
                setDownloadStatus(downloadModel, status)
            }
        )
    }
    
    /**
     * Set download status - Gallery's exact pattern
     */
    private fun setDownloadStatus(curModel: Model, status: ModelDownloadStatus) {
        // Update model download progress - Gallery's exact implementation
        val curModelDownloadStatus = _uiState.value.modelDownloadStatus.toMutableMap()
        curModelDownloadStatus[curModel.name] = status
        val newUiState = _uiState.value.copy(modelDownloadStatus = curModelDownloadStatus)
        
        // Delete downloaded file if status is failed or not_downloaded (Gallery pattern)
        if (status.status == ModelDownloadStatusType.FAILED ||
            status.status == ModelDownloadStatusType.NOT_DOWNLOADED) {
            // TODO: Implement file deletion like Gallery
            Log.d(TAG, "Would delete file for ${curModel.downloadFileName}")
        }
        
        _uiState.update { newUiState }
    }

    private fun updateModelInitializationStatus(
        model: Model,
        status: ModelInitializationStatusType,
        error: String = "",
    ) {
        val newUiState = _uiState.value.copy()
        val newStatus = newUiState.modelInitializationStatus.toMutableMap()
        newStatus[model.name] = ModelInitializationStatus(status = status, error = error)
        _uiState.update { newUiState.copy(modelInitializationStatus = newStatus) }
    }

    private fun getModelDownloadStatus(model: Model): ModelDownloadStatus {
        // Check if model file exists
        val modelPath = model.getPath(context)
        val modelFile = File(modelPath)
        
        return if (modelFile.exists() && modelFile.length() > 0) {
            ModelDownloadStatus(
                status = ModelDownloadStatusType.SUCCEEDED,
                receivedBytes = modelFile.length(),
                totalBytes = model.sizeInBytes,
            )
        } else {
            ModelDownloadStatus(status = ModelDownloadStatusType.NOT_DOWNLOADED)
        }
    }

    private fun createUiState(): ModelManagerUiState {
        val modelDownloadStatus: MutableMap<String, ModelDownloadStatus> = mutableMapOf()
        val modelInstances: MutableMap<String, ModelInitializationStatus> = mutableMapOf()
        
        for (task in ELIZA_TASKS) {
            for (model in task.models) {
                modelDownloadStatus[model.name] = getModelDownloadStatus(model = model)
                modelInstances[model.name] =
                    ModelInitializationStatus(status = ModelInitializationStatusType.NOT_INITIALIZED)
            }
        }

        return ModelManagerUiState(
            tasks = ELIZA_TASKS.toList(),
            modelDownloadStatus = modelDownloadStatus,
            modelInitializationStatus = modelInstances,
            textInputHistory = emptyList(),
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up all models
        for (task in ELIZA_TASKS) {
            for (model in task.models) {
                cleanupModel(task, model)
            }
        }
    }
} 