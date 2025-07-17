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
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.data.repository.ModelInitializationResult
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
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

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
    val downloadStatus: ModelDownloadStatus? = null,
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
     */
    fun downloadModel(): Flow<ModelDownloadProgress> = flow {
        if (isModelDownloaded()) {
            emit(ModelDownloadProgress(
                progress = 1.0f,
                status = ModelDownloadStatus.COMPLETED,
                bytesDownloaded = model.sizeInBytes,
                totalBytes = model.sizeInBytes,
                downloadSpeed = 0L,
                error = null
            ))
            return@flow
        }

        updateDownloadStatus(ModelDownloadStatus(
            status = ModelDownloadStatusType.IN_PROGRESS,
            totalBytes = model.sizeInBytes
        ))

        try {
            downloadRepository.downloadModel(model) { progress ->
                emit(progress)
                updateDownloadStatus(ModelDownloadStatus(
                    status = when (progress.status) {
                        ModelDownloadStatus.DOWNLOADING -> ModelDownloadStatusType.IN_PROGRESS
                        ModelDownloadStatus.COMPLETED -> ModelDownloadStatusType.SUCCEEDED
                        ModelDownloadStatus.FAILED -> ModelDownloadStatusType.FAILED
                        else -> ModelDownloadStatusType.IN_PROGRESS
                    },
                    totalBytes = model.sizeInBytes,
                    receivedBytes = progress.bytesDownloaded,
                    bytesPerSecond = progress.downloadSpeed,
                    errorMessage = progress.error ?: ""
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Model download failed", e)
            emit(ModelDownloadProgress(
                progress = 0f,
                status = ModelDownloadStatus.FAILED,
                bytesDownloaded = 0L,
                totalBytes = model.sizeInBytes,
                downloadSpeed = 0L,
                error = e.message
            ))
        }
    }

    /**
     * Initializes the Gemma 3N model for inference.
     */
    fun initializeModel(): Flow<ModelInitializationResult> = flow {
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

        updateInitializationStatus(ModelInitializationStatus(
            status = ModelInitializationStatusType.INITIALIZING
        ))

        try {
            val startTime = System.currentTimeMillis()
            
            // Initialize the model using MediaPipe
            inferenceHelper.initialize(context, model) { error ->
                viewModelScope.launch {
                    val endTime = System.currentTimeMillis()
                    val initTime = endTime - startTime
                    
                    if (error.isEmpty()) {
                        updateInitializationStatus(ModelInitializationStatus(
                            status = ModelInitializationStatusType.INITIALIZED
                        ))
                        
                        emit(ModelInitializationResult(
                            isSuccess = true,
                            modelName = model.name,
                            initializationTime = initTime,
                            memoryUsage = model.estimatedPeakMemoryInBytes ?: 0L,
                            error = null
                        ))
                        
                        _uiState.update { it.copy(
                            isReady = true,
                            memoryUsage = model.estimatedPeakMemoryInBytes ?: 0L
                        )}
                    } else {
                        updateInitializationStatus(ModelInitializationStatus(
                            status = ModelInitializationStatusType.ERROR,
                            error = error
                        ))
                        
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
        } catch (e: Exception) {
            Log.e(TAG, "Model initialization failed", e)
            updateInitializationStatus(ModelInitializationStatus(
                status = ModelInitializationStatusType.ERROR,
                error = e.message ?: "Unknown error"
            ))
            
            emit(ModelInitializationResult(
                isSuccess = false,
                modelName = model.name,
                initializationTime = 0L,
                memoryUsage = 0L,
                error = e.message
            ))
        }
    }

    /**
     * Cleans up the model and releases resources.
     */
    fun cleanupModel() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                inferenceHelper.cleanUp(model)
                _uiState.update { it.copy(
                    isReady = false,
                    memoryUsage = 0L
                )}
                updateInitializationStatus(ModelInitializationStatus(
                    status = ModelInitializationStatusType.NOT_INITIALIZED
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Model cleanup failed", e)
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
                updateDownloadStatus(ModelDownloadStatus(
                    status = ModelDownloadStatusType.NOT_DOWNLOADED
                ))
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

    private fun checkModelStatus() {
        viewModelScope.launch {
            val downloadStatus = if (isModelDownloaded()) {
                ModelDownloadStatus(status = ModelDownloadStatusType.SUCCEEDED)
            } else {
                ModelDownloadStatus(status = ModelDownloadStatusType.NOT_DOWNLOADED)
            }
            updateDownloadStatus(downloadStatus)
            
            updateInitializationStatus(ModelInitializationStatus(
                status = ModelInitializationStatusType.NOT_INITIALIZED
            ))
        }
    }

    private fun isModelDownloaded(): Boolean {
        val modelPath = model.getPath(context)
        return File(modelPath).exists()
    }

    private fun updateDownloadStatus(status: ModelDownloadStatus) {
        _uiState.update { it.copy(downloadStatus = status) }
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
    suspend fun downloadModel(
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    )
}

/**
 * Helper interface for model inference operations.
 */
interface ElizaInferenceHelper {
    suspend fun initialize(
        context: Context,
        model: Model,
        onComplete: (error: String) -> Unit
    )
    
    suspend fun cleanUp(model: Model)
    
    suspend fun generateResponse(
        model: Model,
        prompt: String,
        images: List<android.graphics.Bitmap> = emptyList()
    ): Flow<String>
} 