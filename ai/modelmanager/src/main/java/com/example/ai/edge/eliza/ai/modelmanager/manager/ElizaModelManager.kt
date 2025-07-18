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

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper
import com.example.ai.edge.eliza.ai.modelmanager.download.ModelDownloadRepository
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.DeviceCapabilities
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelInitializationResult
import com.example.ai.edge.eliza.core.model.ModelPerformance
import com.example.ai.edge.eliza.core.model.ModelSwitchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

private const val TAG = "ElizaModelManager"

/** Model initialization status types. */
enum class ModelInitializationStatusType {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    ERROR,
}

/** Model initialization status with error information. */
data class ModelInitializationStatus(
    val status: ModelInitializationStatusType,
    val error: String = "",
)

/** UI state for the model manager. */
data class ModelManagerUiState(
    val isReady: Boolean = false,
    val memoryUsage: Long = 0L,
    val currentVariant: String? = null,
    val downloadProgress: ModelDownloadProgress? = null
)

/**
 * Eliza Model Manager - handles Gemma 3N model lifecycle with variant support. Adapted from
 * Gallery's ModelManagerViewModel for educational AI use cases. Now supports MatFormer architecture
 * with E4B/E2B variant switching.
 */
@HiltViewModel
class ElizaModelManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: ModelDownloadRepository,
    private val inferenceHelper: ElizaInferenceHelper,
    private val modelRegistry: ElizaModelRegistry
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelManagerUiState())
    val uiState = _uiState.asStateFlow()

    private val externalFilesDir = context.getExternalFilesDir(null)

    init {
        // Configure model registry with defaults
        modelRegistry.configureWithDefaults()

        // Initialize UI state with registry information
        _uiState.value = _uiState.value.copy(currentVariant = modelRegistry.getRecommendedVariant())

        // Check if model is already downloaded
        checkModelStatus()
    }

    /** Gets the current active model from the registry. */
    private val activeModel: Model
        get() =
            modelRegistry.getCurrentModel()
                ?: throw IllegalStateException("No model available in registry")

    /**
     * Initialize the model with variant-specific optimizations. This uses our intelligent variant
     * switching system that optimizes MediaPipe session configuration for different MatFormer-style
     * variants.
     */
    suspend fun initializeModel(variant: String? = null): Flow<ModelInitializationResult> = flow {
        val targetVariant = variant ?: modelRegistry.getRecommendedVariant()

        try {
            emit(ModelInitializationResult.Loading("Initializing $targetVariant variant..."))

            val model = modelRegistry.getCurrentModel()
            if (model == null) {
                emit(ModelInitializationResult.Error("No model available in registry"))
                return@flow
            }

            // Check if model file exists
            if (!model.isDownloaded(context)) {
                emit(ModelInitializationResult.Error("Model not downloaded. Please download first."))
                return@flow
            }

            // Initialize inference helper with variant-specific optimizations
            val initResult = CompletableDeferred<String>()
            inferenceHelper.initialize(context, model, targetVariant) { error ->
                initResult.complete(error)
            }

            val error = initResult.await()
            if (error.isNotEmpty()) {
                emit(ModelInitializationResult.Error(error))
                return@flow
            }

            // Update current variant
            _uiState.update { it.copy(currentVariant = targetVariant) }

            Log.d(TAG, "Model initialized successfully with variant: $targetVariant")
            emit(ModelInitializationResult.Success("Model ready with $targetVariant optimization"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model with variant: $targetVariant", e)
            emit(ModelInitializationResult.Error("Initialization failed: ${e.message}"))
        }
    }

    /**
     * Switch to a different variant with optimized parameters. This is our implementation of
     * "MatFormer-style" switching within MediaPipe constraints.
     */
    suspend fun switchToVariant(targetVariant: String): Flow<ModelSwitchResult> = flow {
        try {
            emit(ModelSwitchResult.Loading("Switching to $targetVariant..."))

            val currentVariant = getCurrentVariant()
            if (currentVariant == targetVariant) {
                emit(ModelSwitchResult.Success("Already using $targetVariant"))
                return@flow
            }

            val model = getCurrentModel()
            if (model == null) {
                emit(ModelSwitchResult.Error("No model available to switch"))
                return@flow
            }

            // Use the inference helper to perform the actual variant switching
            val switchResult = CompletableDeferred<String>()
            inferenceHelper.switchVariant(model, targetVariant) { error ->
                switchResult.complete(error)
            }

            val error = switchResult.await()
            if (error.isNotEmpty()) {
                emit(ModelSwitchResult.Error(error))
                return@flow
            }

            // Update UI state
            _uiState.update { it.copy(currentVariant = targetVariant) }

            Log.d(TAG, "Successfully switched to variant: $targetVariant")
            emit(ModelSwitchResult.Success("Switched to $targetVariant"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to variant: $targetVariant", e)
            emit(ModelSwitchResult.Error("Variant switching failed: ${e.message}"))
        }
    }

    /** Get the current variant being used. */
    fun getCurrentVariant(): String? {
        return inferenceHelper.getCurrentVariant(activeModel)
    }

    /** Get the current active model. */
    fun getCurrentModel(): Model? {
        return try {
            activeModel
        } catch (e: IllegalStateException) {
            null
        }
    }

    /** Get performance characteristics for the current variant. */
    fun getCurrentVariantPerformance(): ModelPerformance? {
        val currentVariant = getCurrentVariant()
        return if (currentVariant != null) {
            modelRegistry.getVariantPerformance(currentVariant)
        } else {
            null
        }
    }

    /** Get the recommended variant for the current device. */
    fun getRecommendedVariant(): String {
        return modelRegistry.getRecommendedVariant()
    }

    /** Check if a variant is available for switching. */
    fun isVariantAvailable(variant: String): Boolean {
        val model = modelRegistry.getCurrentModel() ?: return false
        return model.isDownloaded(context)
    }

    /**
     * Switch to optimal variant based on current device state. This implements automatic variant
     * switching based on memory pressure, battery level, and performance requirements.
     */
    suspend fun switchToOptimalVariant(): Flow<ModelSwitchResult> = flow {
        try {
            emit(ModelSwitchResult.Loading("Analyzing device state..."))

            val deviceCapabilities = getDeviceCapabilities()
            val recommendedVariant = modelRegistry.getRecommendedVariant(deviceCapabilities)

            if (recommendedVariant == _uiState.value.currentVariant) {
                emit(ModelSwitchResult.Success("Already using optimal variant"))
                return@flow
            }

            // Switch to recommended variant
            switchToVariant(recommendedVariant).collect { result -> emit(result) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to optimal variant", e)
            emit(ModelSwitchResult.Error("Optimal variant switching failed: ${e.message}"))
        }
    }

    /** Get current device capabilities for variant selection. */
    private fun getDeviceCapabilities(): DeviceCapabilities {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMemoryGB = memoryInfo.availMem / (1024 * 1024 * 1024)
        val isLowMemory = memoryInfo.lowMemory

        return DeviceCapabilities(
            availableMemoryGB = availableMemoryGB,
            isLowMemory = isLowMemory,
            preferPerformance = availableMemoryGB > 6 && !isLowMemory
        )
    }

    /** Downloads the model if not already present. */
    fun downloadModel() {
        val model = modelRegistry.getCurrentModel() ?: return

        if (model.isDownloaded(context)) {
            Log.d(TAG, "Model '${model.name}' already downloaded")
            return
        }

        Log.d(TAG, "Starting download for model '${model.name}'")

        downloadRepository.downloadModel(model) { progress ->
            _uiState.update { it.copy(downloadProgress = progress) }

            if (progress.status == ModelDownloadStatus.COMPLETED) {
                Log.d(TAG, "Model '${model.name}' download completed")
                checkModelStatus()
            } else if (progress.status == ModelDownloadStatus.FAILED) {
                Log.e(TAG, "Model '${model.name}' download failed: ${progress.error}")
            }
        }
    }

    /** Checks if the model is downloaded and updates UI state. */
    private fun checkModelStatus() {
        val model = modelRegistry.getCurrentModel() ?: return
        val isDownloaded = model.isDownloaded(context)

        _uiState.update {
            it.copy(
                isReady = isDownloaded,
                memoryUsage = if (isDownloaded) model.estimatedPeakMemoryInBytes ?: 0L else 0L
            )
        }

        Log.d(
            TAG,
            "Model '${model.name}' status: ${if (isDownloaded) "Downloaded" else "Not downloaded"}"
        )
    }

    /** Cancels the current model download. */
    fun cancelDownload() {
        val model = modelRegistry.getCurrentModel() ?: return
        downloadRepository.cancelDownloadModel(model)
        Log.d(TAG, "Cancelled download for model '${model.name}'")
    }

    /** Checks if the model is downloaded. */
    fun isModelDownloaded(): Boolean {
        val model = modelRegistry.getCurrentModel() ?: return false
        return model.isDownloaded(context)
    }

    /** Cleans up model resources. */
    fun cleanupModel() {
        val model = modelRegistry.getCurrentModel() ?: return
        inferenceHelper.cleanUp(model)
        Log.d(TAG, "Cleaned up model '${model.name}'")
    }

    override fun onCleared() {
        super.onCleared()
        cleanupModel()
    }
} 