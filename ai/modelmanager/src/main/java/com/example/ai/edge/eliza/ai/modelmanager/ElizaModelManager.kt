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
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.data.repository.ModelInitializationResult
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.core.model.GEMMA_3N_E2B_MODEL
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "ElizaModelManager"

/**
 * Model initialization status types.
 */
enum class ModelInitializationStatusType {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    ERROR,
}

/**
 * Model initialization status with error information.
 */
data class ModelInitializationStatus(
    val status: ModelInitializationStatusType,
    val error: String = "",
)

/**
 * UI state for model management.
 */
data class ModelManagerUiState(
    val model: Model = GEMMA_3N_E2B_MODEL,
    val downloadStatus: ModelDownloadProgress? = null,
    val initializationStatus: ModelInitializationStatus? = null,
    val isReady: Boolean = false,
    val memoryUsage: Long = 0L,
    val errorMessage: String? = null
)

/**
 * Eliza Model Manager - handles Gemma 3N model lifecycle.
 * Adapted from Gallery's ModelManagerViewModel for educational AI use cases.
 */
@HiltViewModel
class ElizaModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: ModelDownloadRepository,
    private val inferenceHelper: ElizaInferenceHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelManagerUiState())
    val uiState = _uiState.asStateFlow()

    private val model = GEMMA_3N_E2B_MODEL
    private val externalFilesDir = context.getExternalFilesDir(null)

    init {
        // Check if model is already downloaded
        checkModelStatus()
    }

    /**
     * Downloads the Gemma 3N model if not already present.
     * Uses Gallery's callback-based pattern for progress updates.
     */
    fun downloadModel() {
        // Update status to DOWNLOADING
        setDownloadStatus(
            model = model,
            status = ModelDownloadProgress(
                progress = 0.0f,
                status = ModelDownloadStatus.DOWNLOADING,
                totalBytes = model.sizeInBytes,
                bytesDownloaded = 0L,
                downloadSpeed = 0L,
                error = null
            )
        )

        // Delete the model files first (like Gallery does)
        deleteModel()

        // Start download using repository (callback-based like Gallery)
        downloadRepository.downloadModel(model, onProgress = ::setDownloadStatusFromProgress)
    }

    /**
     * Cancels the model download.
     */
    fun cancelDownloadModel() {
        downloadRepository.cancelDownloadModel(model)
        deleteModel()
    }

    /**
     * Initializes the Gemma 3N model for inference.
     * Includes proper concurrent access control like Gallery's implementation.
     */
    fun initializeModel(force: Boolean = false): Flow<ModelInitializationResult> = flow {
        if (!isModelDownloaded()) {
            emit(ModelInitializationResult(
                isSuccess = false,
                modelName = model.name,
                initializationTime = 0L,
                memoryUsage = 0L,
                error = "Model not downloaded"
            ))
            return@flow
        }

        // Skip if already initialized (unless forced)
        if (!force && uiState.value.initializationStatus?.status == ModelInitializationStatusType.INITIALIZED) {
            Log.d(TAG, "Model '${model.name}' already initialized. Skipping.")
            emit(ModelInitializationResult(
                isSuccess = true,
                modelName = model.name,
                initializationTime = 0L,
                memoryUsage = model.estimatedPeakMemoryInBytes ?: 0L,
                error = null
            ))
            return@flow
        }

        // Skip if initialization is in progress
        if (model.initializing) {
            model.cleanUpAfterInit = false
            Log.d(TAG, "Model '${model.name}' is being initialized. Skipping.")
            return@flow
        }

        // Clean up any existing instance first
        cleanupModel()

        // Start initialization
        Log.d(TAG, "Initializing model '${model.name}'...")
        model.initializing = true

        // Show initializing status after a delay
        viewModelScope.launch {
            delay(500)
            if (model.instance == null && model.initializing) {
                updateInitializationStatus(ModelInitializationStatus(
                    status = ModelInitializationStatusType.INITIALIZING
                ))
            }
        }

        val startTime = System.currentTimeMillis()
        
        // Initialize the model using MediaPipe (Gallery-style callback)
        val onDone: (error: String) -> Unit = { error ->
            val endTime = System.currentTimeMillis()
            val initTime = endTime - startTime
            model.initializing = false
            
            if (model.instance != null) {
                Log.d(TAG, "Model '${model.name}' initialized successfully")
                updateInitializationStatus(ModelInitializationStatus(
                    status = ModelInitializationStatusType.INITIALIZED
                ))
                
                viewModelScope.launch {
                    emit(ModelInitializationResult(
                        isSuccess = true,
                        modelName = model.name,
                        initializationTime = initTime,
                        memoryUsage = model.estimatedPeakMemoryInBytes ?: 0L,
                        error = null
                    ))
                }
                
                _uiState.update { it.copy(
                    isReady = true,
                    memoryUsage = model.estimatedPeakMemoryInBytes ?: 0L
                )}
                
                // Clean up after init if marked
                if (model.cleanUpAfterInit) {
                    Log.d(TAG, "Model '${model.name}' needs cleaning up after init.")
                    cleanupModel()
                }
            } else {
                Log.d(TAG, "Model '${model.name}' failed to initialize: $error")
                updateInitializationStatus(ModelInitializationStatus(
                    status = ModelInitializationStatusType.ERROR,
                    error = error
                ))
                
                viewModelScope.launch {
                    emit(ModelInitializationResult(
                        isSuccess = false,
                        modelName = model.name,
                        initializationTime = initTime,
                        memoryUsage = 0L,
                        error = error
                    ))
                }
            }
        }

        // Initialize using inference helper
        inferenceHelper.initialize(context, model, onDone)
    }

    /**
     * Cleans up the model and releases resources.
     * Includes proper concurrent access control like Gallery's implementation.
     */
    fun cleanupModel() {
        viewModelScope.launch(Dispatchers.Default) {
            if (model.instance != null) {
                model.cleanUpAfterInit = false
                Log.d(TAG, "Cleaning up model '${model.name}'...")
                
                inferenceHelper.cleanUp(model)
                model.instance = null
                model.initializing = false
                
                updateInitializationStatus(ModelInitializationStatus(
                    status = ModelInitializationStatusType.NOT_INITIALIZED
                ))
                
                _uiState.update { it.copy(
                    isReady = false,
                    memoryUsage = 0L
                )}
            } else {
                // When model is being initialized and we are trying to clean it up at same time,
                // we mark it to clean up and it will be cleaned up after initialization is done.
                if (model.initializing) {
                    model.cleanUpAfterInit = true
                    Log.d(TAG, "Marking model '${model.name}' for cleanup after initialization")
                }
            }
        }
    }

    /**
     * Deletes the downloaded model from storage.
     */
    fun deleteModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cleanupModel()
                val modelDir = File(externalFilesDir, "${model.normalizedName}/${model.version}")
                if (modelDir.exists()) {
                    modelDir.deleteRecursively()
                }
                setDownloadStatus(
                    model = model,
                    status = ModelDownloadProgress(
                        progress = 0.0f,
                        status = ModelDownloadStatus.PENDING,
                        totalBytes = 0L,
                        bytesDownloaded = 0L,
                        downloadSpeed = 0L,
                        error = null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Model deletion failed", e)
            }
        }
    }

    /**
     * Checks if the model is ready for inference.
     */
    fun isModelReady(): Boolean {
        return uiState.value.isReady && 
               uiState.value.initializationStatus?.status == ModelInitializationStatusType.INITIALIZED
    }

    /**
     * Gets the current model instance for inference.
     */
    fun getModelInstance(): Any? {
        return if (isModelReady()) model.instance else null
    }

    /**
     * Sets download status (Gallery-style callback method).
     */
    private fun setDownloadStatus(model: Model, status: ModelDownloadProgress) {
        _uiState.update { it.copy(downloadStatus = status) }
    }

    /**
     * Converts ModelDownloadProgress to ModelDownloadStatus and updates UI.
     */
    private fun setDownloadStatusFromProgress(progress: ModelDownloadProgress) {
        setDownloadStatus(model, progress)
    }

    private fun checkModelStatus() {
        viewModelScope.launch {
            val downloadStatus = if (isModelDownloaded()) {
                ModelDownloadProgress(
                    progress = 1.0f,
                    status = ModelDownloadStatus.COMPLETED,
                    totalBytes = model.sizeInBytes,
                    bytesDownloaded = model.sizeInBytes,
                    downloadSpeed = 0L,
                    error = null
                )
            } else {
                ModelDownloadProgress(
                    progress = 0.0f,
                    status = ModelDownloadStatus.PENDING,
                    totalBytes = 0L,
                    bytesDownloaded = 0L,
                    downloadSpeed = 0L,
                    error = null
                )
            }
            setDownloadStatus(model, downloadStatus)
            
            updateInitializationStatus(ModelInitializationStatus(
                status = ModelInitializationStatusType.NOT_INITIALIZED
            ))
        }
    }

    private fun isModelDownloaded(): Boolean {
        val modelPath = model.getPath(context)
        return File(modelPath).exists()
    }

    private fun updateInitializationStatus(status: ModelInitializationStatus) {
        _uiState.update { it.copy(initializationStatus = status) }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupModel()
    }
}

/**
 * Repository interface for model downloading.
 */
interface ModelDownloadRepository {
    fun downloadModel(
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    )
    
    fun cancelDownloadModel(model: Model)
} 