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
// Import Gallery-compatible classes from core.model
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.core.model.createLlmChatConfigs
import com.example.ai.edge.eliza.core.model.Accelerator
// Import Eliza-specific classes from ai.modelmanager.data
import com.example.ai.edge.eliza.ai.modelmanager.data.ELIZA_TASKS
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.data.TaskType
import com.example.ai.edge.eliza.ai.modelmanager.data.processTasks
import com.example.ai.edge.eliza.ai.modelmanager.download.ModelDownloadRepository
// OAuth imports now from core data layer and app level
import com.example.ai.edge.eliza.core.data.repository.DataStoreRepository
import com.example.ai.edge.eliza.core.data.repository.AccessTokenData
import com.example.ai.edge.eliza.core.data.repository.TokenStatus
import com.example.ai.edge.eliza.core.data.repository.TokenStatusAndData
import com.example.ai.edge.eliza.core.data.repository.TokenRequestResult
import com.example.ai.edge.eliza.core.data.repository.TokenRequestResultType
import com.example.ai.edge.eliza.core.data.auth.AuthConfig
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import androidx.core.net.toUri
import java.net.HttpURLConnection
import java.net.URL
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
    private val dataStoreRepository: DataStoreRepository, // Fixed: Use app-level DataStore
) : ViewModel() {
    
    // OAuth components (copied from Gallery)
    private val authService = AuthorizationService(context)
    private var curAccessToken: String? = null

    private val _uiState = MutableStateFlow(createUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load models using Gallery's exact pattern
        loadElizaModels()
    }

    /**
     * Public function to refresh models - useful for fixing state corruption
     * Follows Gallery's model reloading pattern
     */
    fun refreshModels() {
        Log.d(TAG, "Refreshing models to fix any state corruption...")
        loadElizaModels()
    }

    /**
     * Select a model and update UI state - copied EXACTLY from Gallery
     * This is crucial for model selection to work properly
     */
    fun selectModel(model: Model) {
        Log.d(TAG, "Selecting model: ${model.name}")
        _uiState.update { 
            it.copy(selectedModel = model)
        }
    }

    /**
     * Load Eliza models following Gallery's exact pattern
     * This prevents duplicates by clearing existing models first
     */
    private fun loadElizaModels() {
        Log.d(TAG, "Loading Eliza models using Gallery's proven pattern...")
        
        // STEP 1: Clear all existing models (Gallery's pattern)
        ELIZA_TASKS.forEach { task ->
            task.models.clear()
            Log.d(TAG, "Cleared existing models from task: ${task.type}")
        }
        
        // STEP 2: Add models from Eliza's predefined list
        addGemmaModels()
        
        // STEP 3: Process all tasks (Gallery's pattern)
        processTasks()
        
        // STEP 4: Update UI state
        _uiState.update { createUiState() }
        
        Log.d(TAG, "Successfully loaded Eliza models. Total tasks: ${ELIZA_TASKS.size}")
        ELIZA_TASKS.forEach { task ->
            Log.d(TAG, "Task ${task.type} has ${task.models.size} models")
        }
    }

    private fun addGemmaModels() {
        // Create BOTH Gemma models from Gallery's allowlist as requested
        addGemmaE4BModel()
        addGemmaE2BModel()
    }
    
    private fun addGemmaE4BModel() {
        // EXACT COPY of Gallery's model creation pattern from ModelAllowlist.kt toModel() method
        val modelId = "google/gemma-3n-E4B-it-litert-preview"
        val modelFile = "gemma-3n-E4B-it-int4.task"
        
        // Construct HF download url - EXACT Gallery pattern
        val downloadUrl = "https://huggingface.co/$modelId/resolve/main/$modelFile?download=true"
        
        // Config - EXACT Gallery pattern
        val defaultTopK: Int = 64       // From Gallery allowlist
        val defaultTopP: Float = 0.95f    // From Gallery allowlist  
        val defaultTemperature: Float = 1.0f // From Gallery allowlist
        val defaultMaxToken = 4096 // From Gallery allowlist
        var accelerators: List<Accelerator> = listOf(Accelerator.CPU, Accelerator.GPU) // "cpu,gpu" from allowlist
        
        val configs = createLlmChatConfigs(
            defaultTopK = defaultTopK,
            defaultTopP = defaultTopP,
            defaultTemperature = defaultTemperature,
            defaultMaxToken = defaultMaxToken,
            accelerators = accelerators,
        )
        
        // Misc - EXACT Gallery pattern
        var showBenchmarkButton = true
        var showRunAgainButton = true
        // isLlmModel = true for our case
        showBenchmarkButton = false
        showRunAgainButton = false
        
        val gemmaE4BModel = Model(
            name = "Gemma-3n-E4B-it-int4", // Exact name from Gallery
            version = "20250520", // Exact version from Gallery
            info = "Preview version of [Gemma 3n E4B](https://ai.google.dev/gemma/docs/gemma-3n) ready for deployment on Android using the [MediaPipe LLM Inference API](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference). The current checkpoint only supports text and vision input, with 4096 context length.",
            url = downloadUrl,
            sizeInBytes = 4405655031L, // Exact size from Gallery
            estimatedPeakMemoryInBytes = 6979321856L, // Exact memory from Gallery
            configs = configs,
            downloadFileName = modelFile,
            showBenchmarkButton = showBenchmarkButton,
            showRunAgainButton = showRunAgainButton,
            learnMoreUrl = "https://huggingface.co/${modelId}",
            llmSupportImage = true, // From Gallery allowlist
            llmSupportAudio = false, // Not specified in Gallery allowlist
        )
        
        // Add to both ELIZA tasks with duplicate prevention
        ELIZA_TASKS.forEach { task ->
            // Check for duplicates before adding (additional safety)
            val existingModel = task.models.find { it.name == gemmaE4BModel.name }
            if (existingModel == null) {
                task.models.add(gemmaE4BModel)
                Log.d(TAG, "Added ${gemmaE4BModel.name} to task ${task.type}")
            } else {
                Log.w(TAG, "Model ${gemmaE4BModel.name} already exists in task ${task.type}, skipping")
            }
        }
        
        // Pre-process the model (Gallery's pattern)
        gemmaE4BModel.preProcess()
        
        // Set as default selected model
        _uiState.update { currentState ->
            currentState.copy(selectedModel = gemmaE4BModel)
        }
        
        Log.d(TAG, "Added exact Gallery Gemma-3n-E4B-it-int4 model to tasks")
    }
    
    private fun addGemmaE2BModel() {
        // EXACT COPY of Gallery's model creation pattern from ModelAllowlist.kt toModel() method
        val modelId = "google/gemma-3n-E2B-it-litert-preview"
        val modelFile = "gemma-3n-E2B-it-int4.task"
        
        // Construct HF download url - EXACT Gallery pattern
        val downloadUrl = "https://huggingface.co/$modelId/resolve/main/$modelFile?download=true"
        
        // Config - EXACT Gallery pattern
        val defaultTopK: Int = 64       // From Gallery allowlist
        val defaultTopP: Float = 0.95f    // From Gallery allowlist
        val defaultTemperature: Float = 1.0f // From Gallery allowlist
        val defaultMaxToken = 4096 // From Gallery allowlist
        var accelerators: List<Accelerator> = listOf(Accelerator.CPU, Accelerator.GPU) // "cpu,gpu" from allowlist
        
        val configs = createLlmChatConfigs(
            defaultTopK = defaultTopK,
            defaultTopP = defaultTopP,
            defaultTemperature = defaultTemperature,
            defaultMaxToken = defaultMaxToken,
            accelerators = accelerators,
        )
        
        // Misc - EXACT Gallery pattern
        var showBenchmarkButton = true
        var showRunAgainButton = true
        // isLlmModel = true for our case
        showBenchmarkButton = false
        showRunAgainButton = false
        
        val gemmaE2BModel = Model(
            name = "Gemma-3n-E2B-it-int4", // Exact name from Gallery
            version = "20250520", // Exact version from Gallery
            info = "Preview version of [Gemma 3n E2B](https://ai.google.dev/gemma/docs/gemma-3n) ready for deployment on Android using the [MediaPipe LLM Inference API](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference). The current checkpoint only supports text and vision input, with 4096 context length.",
            url = downloadUrl,
            sizeInBytes = 3136226711L, // Exact size from Gallery
            estimatedPeakMemoryInBytes = 5905580032L, // Exact memory from Gallery
            configs = configs,
            downloadFileName = modelFile,
            showBenchmarkButton = showBenchmarkButton,
            showRunAgainButton = showRunAgainButton,
            learnMoreUrl = "https://huggingface.co/${modelId}",
            llmSupportImage = true, // From Gallery allowlist
            llmSupportAudio = false, // Not specified in Gallery allowlist
        )
        
        // Add to both ELIZA tasks with duplicate prevention
        ELIZA_TASKS.forEach { task ->
            // Check for duplicates before adding (additional safety)
            val existingModel = task.models.find { it.name == gemmaE2BModel.name }
            if (existingModel == null) {
                task.models.add(gemmaE2BModel)
                Log.d(TAG, "Added ${gemmaE2BModel.name} to task ${task.type}")
            } else {
                Log.w(TAG, "Model ${gemmaE2BModel.name} already exists in task ${task.type}, skipping")
            }
        }
        
        // Pre-process the model (Gallery's pattern)
        gemmaE2BModel.preProcess()
        
        Log.d(TAG, "Added exact Gallery Gemma-3n-E2B-it-int4 model to tasks")
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
     * Clear error states for a model to prevent persistent error displays
     * Following Gallery's state management pattern
     */
    private fun clearModelErrorStates(model: Model) {
        Log.d(TAG, "Clearing error states for model: ${model.name}")
        
        // Update download status to clear any previous errors
        setDownloadStatus(
            curModel = model,
            status = ModelDownloadStatus(
                status = ModelDownloadStatusType.NOT_DOWNLOADED,
                errorMessage = "" // Clear error message
            )
        )
        
        // Update initialization status to clear any previous errors
        updateModelInitializationStatus(
            model = model,
            status = ModelInitializationStatusType.NOT_INITIALIZED,
            error = "" // Clear error message
        )
    }
    
    /**
     * Download a model with OAuth authentication - Gallery's exact authentication pattern
     * Enhanced with proper error state clearing
     */
    fun downloadModel(context: Context, task: Task, model: Model) {
        Log.d(TAG, "Starting download for model '${model.name}' using Gallery OAuth + WorkManager pattern")
        
        // Clear any previous error states first
        clearModelErrorStates(model)
        
        // Gallery's exact pattern: Update status to IN_PROGRESS first
        setDownloadStatus(
            curModel = model,
            status = ModelDownloadStatus(status = ModelDownloadStatusType.IN_PROGRESS)
        )
        
        // Gallery's exact pattern: Delete existing model files first
        deleteModel(task = task, model = model)
        
        // 🔐 NEW: Gallery's OAuth Authentication Flow
        if (model.url.startsWith("https://huggingface.co")) {
            Log.d(TAG, "Model '${model.name}' is from HuggingFace. Checking if auth is needed...")
            
            // Step 1: Check if the URL needs authentication
            val firstResponseCode = getModelUrlResponse(model = model)
            if (firstResponseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Model '${model.name}' doesn't need auth. Start downloading...")
                startDownloadWithToken(model, null)
                return
            } else if (firstResponseCode < 0) {
                Log.e(TAG, "Network error checking model URL")
                setDownloadStatus(
                    curModel = model,
                    status = ModelDownloadStatus(
                        status = ModelDownloadStatusType.FAILED,
                        errorMessage = "Network error checking model URL"
                    )
                )
                return
            }
            
            Log.d(TAG, "Model '${model.name}' needs auth. Checking token status...")
            
            // Step 2: Check current token status
            val tokenStatusAndData = getTokenStatusAndData()
            
            when (tokenStatusAndData.status) {
                TokenStatus.NOT_STORED, TokenStatus.EXPIRED -> {
                    Log.d(TAG, "Token not available or expired. User needs to authenticate.")
                    setDownloadStatus(
                        curModel = model,
                        status = ModelDownloadStatus(
                            status = ModelDownloadStatusType.FAILED,
                            errorMessage = "Authentication required. Please sign in to HuggingFace."
                        )
                    )
                    // TODO: Trigger OAuth flow in UI
                }
                
                TokenStatus.NOT_EXPIRED -> {
                    // Use current token to check download URL
                    Log.d(TAG, "Checking download URL with current token...")
                    val responseCode = getModelUrlResponse(
                        model = model,
                        accessToken = tokenStatusAndData.data!!.accessToken
                    )
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Download URL accessible with current token.")
                        startDownloadWithToken(model, tokenStatusAndData.data!!.accessToken)
                    } else {
                        Log.d(TAG, "Download URL not accessible. Response code: $responseCode. Need new token.")
                        setDownloadStatus(
                            curModel = model,
                            status = ModelDownloadStatus(
                                status = ModelDownloadStatusType.FAILED,
                                errorMessage = "Authentication expired. Please sign in to HuggingFace again."
                            )
                        )
                        // TODO: Trigger OAuth flow in UI
                    }
                }
            }
        } else {
            // For non-HuggingFace URLs, download directly
            Log.d(TAG, "Model '${model.name}' is not from HuggingFace. Start downloading...")
            startDownloadWithToken(model, null)
        }
    }
    
    /**
     * Start actual download with optional access token
     * Copied from Gallery's download pattern
     */
    private fun startDownloadWithToken(model: Model, accessToken: String?) {
        Log.d(TAG, "Starting download for '${model.name}' with token: ${if (accessToken != null) "provided" else "none"}")
        
        modelDownloadRepository.downloadModel(
            model = model,
            accessToken = accessToken, // Pass token to download repository
            onStatusUpdated = { downloadModel, status ->
                Log.d(TAG, "Download progress for '${downloadModel.name}': ${status.status}")
                setDownloadStatus(downloadModel, status)
            }
        )
    }
    
    /**
     * Delete model files - Gallery's exact pattern
     */
    fun deleteModel(task: Task, model: Model) {
        Log.d(TAG, "Deleting model files for '${model.name}'")
        try {
            val modelPath = model.getPath(context)
            val modelFile = File(modelPath)
            if (modelFile.exists()) {
                modelFile.delete()
                Log.d(TAG, "Deleted model file: $modelPath")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model file for '${model.name}'", e)
        }
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

    // ✨ OAuth Methods (copied from Gallery) ✨

    /**
     * EXACT COPY of Gallery's getModelUrlResponse method
     * No modifications - copied exactly from Gallery's ModelManagerViewModel.kt line 396-411
     */
    fun getModelUrlResponse(model: Model, accessToken: String? = null): Int {
        try {
            val url = URL(model.url)
            val connection = url.openConnection() as HttpURLConnection
            if (accessToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            connection.connect()

            // Report the result.
            return connection.responseCode
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            return -1
        }
    }

    /**
     * Get current token status and data
     * Copied from Gallery's getTokenStatusAndData
     */
    fun getTokenStatusAndData(): TokenStatusAndData {
        var tokenStatus = TokenStatus.NOT_STORED
        Log.d(TAG, "Reading token data from data store...")
        val tokenData = dataStoreRepository.readAccessTokenData()

        if (tokenData != null) {
            Log.d(TAG, "Token exists and loaded.")

            // Check expiration (with 5-minute buffer)
            val curTs = System.currentTimeMillis()
            val expirationTs = tokenData.expiresAtMs - 5 * 60 * 1000 // 5 minute buffer
            Log.d(TAG, "Checking token expiration. Current: $curTs, expires: $expirationTs")
            
            if (curTs >= expirationTs) {
                Log.d(TAG, "Token expired!")
                tokenStatus = TokenStatus.EXPIRED
            } else {
                Log.d(TAG, "Token not expired.")
                tokenStatus = TokenStatus.NOT_EXPIRED
                curAccessToken = tokenData.accessToken
            }
        } else {
            Log.d(TAG, "Token doesn't exist.")
        }

        return TokenStatusAndData(status = tokenStatus, data = tokenData)
    }

    /**
     * Create authorization request for HuggingFace OAuth
     * Copied from Gallery's getAuthorizationRequest
     */
    fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            AuthConfig.authServiceConfig,
            AuthConfig.clientId,
            ResponseTypeValues.CODE,
            AuthConfig.redirectUri.toUri(),
        )
        .setScope("read-repos")
        .build()
    }

    /**
     * Save access token data securely
     * Copied from Gallery's saveAccessToken pattern
     */
    private fun saveAccessToken(accessToken: String, refreshToken: String, expiresAt: Long) {
        dataStoreRepository.saveAccessTokenData(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
    }

    /**
     * Handle OAuth authorization result and exchange code for tokens
     * Copied from Gallery's handleAuthResult method
     */
    fun handleAuthResult(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?,
        onTokenRequested: (TokenRequestResult) -> Unit
    ) {
        when {
            authResponse?.authorizationCode != null -> {
                // Authorization successful, exchange the code for tokens
                var errorMessage: String? = null
                authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { 
                    tokenResponse, tokenEx ->
                    
                    if (tokenResponse != null) {
                        if (tokenResponse.accessToken == null) {
                            errorMessage = "Empty access token"
                        } else if (tokenResponse.refreshToken == null) {
                            errorMessage = "Empty refresh token"
                        } else if (tokenResponse.accessTokenExpirationTime == null) {
                            errorMessage = "Empty expiration time"
                        } else {
                            // Token exchange successful. Store the tokens securely
                            Log.d(TAG, "Token exchange successful. Storing tokens...")
                            saveAccessToken(
                                accessToken = tokenResponse.accessToken!!,
                                refreshToken = tokenResponse.refreshToken!!,
                                expiresAt = tokenResponse.accessTokenExpirationTime!!,
                            )
                            curAccessToken = tokenResponse.accessToken!!
                            Log.d(TAG, "Token successfully saved.")
                        }
                    } else if (tokenEx != null) {
                        errorMessage = "Token exchange failed: ${tokenEx.message}"
                    } else {
                        errorMessage = "Token exchange failed"
                    }
                    
                    if (errorMessage == null) {
                        onTokenRequested(TokenRequestResult(status = TokenRequestResultType.SUCCEEDED))
                    } else {
                        onTokenRequested(
                            TokenRequestResult(
                                status = TokenRequestResultType.FAILED,
                                errorMessage = errorMessage,
                            )
                        )
                    }
                }
            }

            authException != null -> {
                onTokenRequested(
                    TokenRequestResult(
                        status = if (authException.message == "User cancelled flow") 
                            TokenRequestResultType.USER_CANCELLED
                        else TokenRequestResultType.FAILED,
                        errorMessage = authException.message,
                    )
                )
            }

            else -> {
                onTokenRequested(TokenRequestResult(status = TokenRequestResultType.USER_CANCELLED))
            }
        }
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