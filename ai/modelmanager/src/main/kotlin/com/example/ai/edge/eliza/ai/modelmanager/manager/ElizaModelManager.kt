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
import com.example.ai.edge.eliza.ai.modelmanager.device.DeviceCapabilityChecker
// DataStore for token management (OAuth removed - using direct API tokens)
import com.example.ai.edge.eliza.core.data.repository.DataStoreRepository
import com.example.ai.edge.eliza.core.data.repository.AccessTokenData
import com.example.ai.edge.eliza.core.data.repository.TokenStatus
import com.example.ai.edge.eliza.core.data.repository.TokenStatusAndData
import com.example.ai.edge.eliza.core.data.repository.TokenRequestResult
import com.example.ai.edge.eliza.core.data.repository.TokenRequestResultType
import java.net.HttpURLConnection
import java.net.URL
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import com.example.ai.edge.eliza.ai.modelmanager.BuildConfig

private const val TAG = "ElizaModelManager"

enum class ModelInitializationStatusType {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    ERROR,
    CANCELLED_DUE_TO_MEMORY, // NEW: User cancelled due to memory warning
}

data class ModelInitializationStatus(
    val status: ModelInitializationStatusType,
    val error: String = "",
)

data class ModelManagerUiState(
    val tasks: List<Task> = emptyList(),
    val selectedModel: Model? = null,
    val modelDownloadStatus: Map<String, ModelDownloadStatus> = emptyMap(),
    val modelInitializationStatus: Map<String, ModelInitializationStatus> = emptyMap(),
    val textInputHistory: List<String> = emptyList(),
    // NEW: Memory warning dialog state
    val showMemoryWarning: Boolean = false,
    val memoryWarningModel: Model? = null,
    val memoryWarningCompatibility: DeviceCapabilityChecker.ModelCompatibility? = null,
    val memoryWarningDeviceInfo: DeviceCapabilityChecker.DeviceMemoryInfo? = null,
)

@HiltViewModel
class ElizaModelManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadRepository: ModelDownloadRepository,
    private val dataStoreRepository: DataStoreRepository, // Fixed: Use app-level DataStore
    private val deviceCapabilityChecker: DeviceCapabilityChecker, // NEW: Device-aware model management
) : ViewModel() {
    
    // Direct API token management (OAuth removed for simplicity)
    private var curAccessToken: String? = null
    
    // Crash detection state
    private var hasPreviouslyCrashed: Boolean = false
    private var crashedModelName: String? = null
    
    // User preference overrides
    private var userOverridesDeviceRecommendations: Boolean = false
    private var skipMemoryWarnings: Boolean = false

    private val _uiState = MutableStateFlow(createUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // STEP 1: Load user preferences first
        loadUserPreferences()
        
        // STEP 2: Check for previous crashes and adjust strategy
        checkForPreviousCrashes()
        
        // STEP 3: Load models using Gallery's pattern (now crash-aware and user-preference-aware)
        loadElizaModels()
        
        // STEP 4: Initialize HuggingFace API token for easy authentication
        initializeDirectApiToken()
    }

    /**
     * Public function to refresh models - useful for fixing state corruption
     */
    fun refreshModels() {
        Log.d(TAG, "Refreshing models to fix any state corruption...")
        loadElizaModels()
    }

    /**
     * Public accessor for device capability checker.
     * Allows UI components to assess model compatibility.
     */
    fun getDeviceCapabilityChecker(): DeviceCapabilityChecker = deviceCapabilityChecker

    /**
     * Advanced user preference: Override device recommendations.
     * When enabled, users can manually select any model regardless of device capabilities.
     */
    fun setUserOverridesDeviceRecommendations(override: Boolean) {
        Log.d(TAG, "User device override setting changed: $override")
        userOverridesDeviceRecommendations = override
        
        // Save preference to SharedPreferences
        try {
            val prefs = context.getSharedPreferences("eliza_user_preferences", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("override_device_recommendations", override).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user override preference", e)
        }
    }

    /**
     * Advanced user preference: Skip memory warnings.
     * When enabled, users won't see memory warning dialogs.
     */
    fun setSkipMemoryWarnings(skip: Boolean) {
        Log.d(TAG, "Skip memory warnings setting changed: $skip")
        skipMemoryWarnings = skip
        
        // Save preference to SharedPreferences
        try {
            val prefs = context.getSharedPreferences("eliza_user_preferences", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("skip_memory_warnings", skip).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save skip warnings preference", e)
        }
    }

    /**
     * Get current user override settings.
     */
    fun getUserOverridesDeviceRecommendations(): Boolean = userOverridesDeviceRecommendations
    fun getSkipMemoryWarnings(): Boolean = skipMemoryWarnings

    /**
     * Load user preferences from SharedPreferences.
     */
    private fun loadUserPreferences() {
        try {
            val prefs = context.getSharedPreferences("eliza_user_preferences", Context.MODE_PRIVATE)
            userOverridesDeviceRecommendations = prefs.getBoolean("override_device_recommendations", false)
            skipMemoryWarnings = prefs.getBoolean("skip_memory_warnings", false)
            
            Log.d(TAG, "Loaded user preferences:")
            Log.d(TAG, "  Override device recommendations: $userOverridesDeviceRecommendations")
            Log.d(TAG, "  Skip memory warnings: $skipMemoryWarnings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load user preferences", e)
            userOverridesDeviceRecommendations = false
            skipMemoryWarnings = false
        }
    }

    /**
     * Select a model and update UI state 
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
        
        // STEP 4: Intelligent device-aware model selection 
        selectOptimalModelForDevice()
        
        // STEP 5: Update UI state
        _uiState.update { createUiState() }
        
        Log.d(TAG, "Successfully loaded Eliza models. Total tasks: ${ELIZA_TASKS.size}")
        ELIZA_TASKS.forEach { task ->
            Log.d(TAG, "Task ${task.type} has ${task.models.size} models")
        }
    }

    /**
     * Check for previous app crashes and adjust model selection strategy.
     * Critical for preventing repeated crashes on app restart.
     */
    private fun checkForPreviousCrashes() {
        try {
            val prefs = context.getSharedPreferences("eliza_crash_detection", Context.MODE_PRIVATE)
            hasPreviouslyCrashed = prefs.getBoolean("has_crashed", false)
            crashedModelName = prefs.getString("last_crashed_model", null)
            val crashTimestamp = prefs.getLong("crash_timestamp", 0L)
            val crashError = prefs.getString("crash_error", null)
            
            if (hasPreviouslyCrashed && crashedModelName != null) {
                val crashAge = System.currentTimeMillis() - crashTimestamp
                val crashAgeHours = crashAge / (1000 * 60 * 60)
                
                Log.w(TAG, "🚨 CRASH DETECTION: Previous crash detected!")
                Log.w(TAG, "  Crashed model: $crashedModelName")
                Log.w(TAG, "  Crash age: ${crashAgeHours}h ago")
                Log.w(TAG, "  Error: $crashError")
                
                // Clear old crash info (older than 24 hours) to allow retry
                if (crashAgeHours > 24) {
                    Log.d(TAG, "🔄 Crash info is >24h old, clearing to allow retry")
                    clearCrashInfo()
                    hasPreviouslyCrashed = false
                    crashedModelName = null
                } else {
                    Log.w(TAG, "🚫 Recent crash detected - will avoid problematic model")
                }
            } else {
                Log.d(TAG, "✅ No previous crashes detected")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check crash info", e)
            hasPreviouslyCrashed = false
            crashedModelName = null
        }
    }

    /**
     * Clear crash information (used for 24h reset logic).
     */
    private fun clearCrashInfo() {
        try {
            val prefs = context.getSharedPreferences("eliza_crash_detection", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d(TAG, "🧹 Cleared crash detection info")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear crash info", e)
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

    /**
     * Device-aware model selection using Gallery's memory detection pattern.
     * Automatically selects the optimal model based on device capabilities.
     * Respects user preference overrides for advanced users.
     */
    private fun selectOptimalModelForDevice() {
        Log.d(TAG, "Performing device-aware model selection...")
        
        // Check if user has overridden device recommendations
        if (userOverridesDeviceRecommendations) {
            Log.d(TAG, "⚠️ User has overridden device recommendations - skipping automatic selection")
            Log.d(TAG, "User will manually select models via UI")
            
            // Still select a default model, but don't apply device filtering
            val availableModels = ELIZA_TASKS.firstOrNull()?.models ?: emptyList()
            if (availableModels.isNotEmpty()) {
                // Select the first available model (typically the 4B model)
                val defaultModel = availableModels.first()
                _uiState.update { currentState ->
                    currentState.copy(selectedModel = defaultModel)
                }
                Log.d(TAG, "Selected default model for user override: ${defaultModel.name}")
            }
            return
        }
        
        // Get all available models from the first task (both tasks have same models)
        val availableModels = ELIZA_TASKS.firstOrNull()?.models ?: emptyList()
        if (availableModels.isEmpty()) {
            Log.w(TAG, "No models available for device-aware selection")
            return
        }
        
        Log.d(TAG, "Available models: ${availableModels.map { "${it.name} (${String.format("%.1f", (it.estimatedPeakMemoryInBytes ?: 0L) / (1024f * 1024f * 1024f))} GB)" }}")
        
        // 🚨 CRASH-AWARE FILTERING: Remove previously crashed models from consideration
        val safeModels = if (hasPreviouslyCrashed && crashedModelName != null) {
            Log.w(TAG, "🚫 Filtering out previously crashed model: $crashedModelName")
            availableModels.filter { it.name != crashedModelName }
        } else {
            availableModels
        }
        
        if (safeModels.isEmpty()) {
            Log.e(TAG, "❌ No safe models available after crash filtering!")
            return
        }
        
        if (safeModels.size != availableModels.size) {
            Log.w(TAG, "⚠️ Crash detection filtered models: ${availableModels.size} → ${safeModels.size}")
            Log.w(TAG, "Safe models: ${safeModels.map { it.name }}")
        }
        
        // Get device memory info
        val deviceMemoryInfo = deviceCapabilityChecker.getDeviceMemoryInfo(context)
        Log.d(TAG, "Device memory analysis:")
        Log.d(TAG, "  Device: ${deviceMemoryInfo.deviceModel}")
        Log.d(TAG, "  Total RAM: ${String.format("%.1f", deviceMemoryInfo.totalMemoryGB)} GB")
        Log.d(TAG, "  Usable RAM: ${String.format("%.1f", deviceMemoryInfo.usableMemoryGB)} GB")
        Log.d(TAG, "  Low RAM device: ${deviceMemoryInfo.isLowMemoryDevice}")
        
        // Get recommended model from SAFE models only
        val recommendedModel = deviceCapabilityChecker.getRecommendedModel(context, safeModels)
        
        if (recommendedModel != null) {
            val compatibility = deviceCapabilityChecker.assessModelCompatibility(context, recommendedModel)
            
            Log.d(TAG, "Device-recommended model: ${recommendedModel.name}")
            Log.d(TAG, "  Memory requirement: ${String.format("%.1f", (recommendedModel.estimatedPeakMemoryInBytes ?: 0L) / (1024f * 1024f * 1024f))} GB")
            Log.d(TAG, "  Memory utilization: ${String.format("%.1f", compatibility.memoryUtilization * 100)}%")
            Log.d(TAG, "  Risk level: ${compatibility.riskLevel}")
            Log.d(TAG, "  Recommendation: ${compatibility.recommendation}")
            
            // Set as selected model
            _uiState.update { currentState ->
                currentState.copy(selectedModel = recommendedModel)
            }
            
            Log.d(TAG, "✅ Selected optimal model for device: ${recommendedModel.name}")
        } else {
            Log.w(TAG, "Could not determine optimal model - no model selection made")
            
            // Fallback: Select smallest SAFE model by memory requirement
            val fallbackModel = safeModels.minByOrNull { it.estimatedPeakMemoryInBytes ?: Long.MAX_VALUE }
            if (fallbackModel != null) {
                _uiState.update { currentState ->
                    currentState.copy(selectedModel = fallbackModel)
                }
                Log.w(TAG, "⚠️ Fallback to smallest model: ${fallbackModel.name}")
            }
        }
    }

    /**
     * Check if model needs memory warning and show dialog if necessary.
     * Returns true if warning should be shown, false if safe to proceed.
     * Respects user preference to skip warnings.
     */
    fun checkAndShowMemoryWarning(context: Context, task: Task, model: Model): Boolean {
        // Check if user has disabled memory warnings
        if (skipMemoryWarnings) {
            Log.d(TAG, "⚠️ User has disabled memory warnings - skipping warning for ${model.name}")
            return false
        }
        
        // Skip warning for models that are already safe
        val compatibility = deviceCapabilityChecker.assessModelCompatibility(context, model)
        
        // Show warning for WARNING, CRITICAL, or DANGEROUS risk levels
        val shouldWarn = when (compatibility.riskLevel) {
            DeviceCapabilityChecker.RiskLevel.WARNING,
            DeviceCapabilityChecker.RiskLevel.CRITICAL,
            DeviceCapabilityChecker.RiskLevel.DANGEROUS -> true
            DeviceCapabilityChecker.RiskLevel.SAFE -> false
        }
        
        if (shouldWarn) {
            Log.w(TAG, "🚨 Memory warning required for ${model.name}: ${compatibility.riskLevel}")
            val deviceInfo = deviceCapabilityChecker.getDeviceMemoryInfo(context)
            
            _uiState.update { currentState ->
                currentState.copy(
                    showMemoryWarning = true,
                    memoryWarningModel = model,
                    memoryWarningCompatibility = compatibility,
                    memoryWarningDeviceInfo = deviceInfo
                )
            }
            return true
        } else {
            Log.d(TAG, "✅ No memory warning needed for ${model.name}: ${compatibility.riskLevel}")
            return false
        }
    }

    /**
     * Hide memory warning dialog.
     */
    fun hideMemoryWarning() {
        _uiState.update { currentState ->
            currentState.copy(
                showMemoryWarning = false,
                memoryWarningModel = null,
                memoryWarningCompatibility = null,
                memoryWarningDeviceInfo = null
            )
        }
    }

    /**
     * Cancel model loading due to memory warning.
     */
    fun cancelDueToMemoryWarning() {
        val model = uiState.value.memoryWarningModel
        if (model != null) {
            Log.d(TAG, "❌ User cancelled model loading due to memory warning: ${model.name}")
            updateModelInitializationStatus(
                model = model,
                status = ModelInitializationStatusType.CANCELLED_DUE_TO_MEMORY,
                error = "Model loading cancelled due to device memory constraints"
            )
            hideMemoryWarning()
        }
    }

    /**
     * Proceed with model initialization despite memory warning.
     */
    fun proceedWithMemoryWarning(context: Context, task: Task) {
        val model = uiState.value.memoryWarningModel
        if (model != null) {
            Log.w(TAG, "⚠️ User chose to proceed with risky model: ${model.name}")
            hideMemoryWarning()
            initializeModelUnsafe(context, task, model, force = true)
        }
    }

    /**
     * Switch to safer model from memory warning dialog.
     */
    fun switchToSaferModel(context: Context, task: Task) {
        val dangerousModel = uiState.value.memoryWarningModel
        if (dangerousModel != null) {
            Log.d(TAG, "🔄 User chose safer model instead of: ${dangerousModel.name}")
            
            // Find the 2B model as safer alternative
            val availableModels = task.models
            val saferModel = availableModels.find { 
                it.name.contains("E2B", ignoreCase = true) && it != dangerousModel 
            }
            
            if (saferModel != null) {
                Log.d(TAG, "✅ Switching to safer model: ${saferModel.name}")
                
                // Update selected model
                _uiState.update { currentState ->
                    currentState.copy(selectedModel = saferModel)
                }
                
                hideMemoryWarning()
                
                // Initialize the safer model (which should be safe)
                initializeModel(context, task, saferModel, force = true)
            } else {
                Log.e(TAG, "❌ No safer model found!")
                hideMemoryWarning()
            }
        }
    }

    /**
     * Safe model initialization with memory warning check.
     * This is the main entry point that should be used by UI components.
     */
    fun initializeModel(context: Context, task: Task, model: Model, force: Boolean = false) {
        // Check if we need to show memory warning first
        if (!force && checkAndShowMemoryWarning(context, task, model)) {
            Log.d(TAG, "⏸️ Model initialization paused for memory warning")
            return // Wait for user decision via memory warning dialog
        }
        
        // Proceed with unsafe initialization
        initializeModelUnsafe(context, task, model, force)
    }

    /**
     * Unsafe model initialization - bypasses memory warning.
     * Only call this after memory warning has been handled.
     */
    private fun initializeModelUnsafe(context: Context, task: Task, model: Model, force: Boolean = false) {
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
                    Log.e(TAG, "Model '${model.name}' failed to initialize: $error")
                    updateModelInitializationStatus(
                        model = model,
                        status = ModelInitializationStatusType.ERROR,
                        error = error,
                    )
                    
                    // 🚨 CRITICAL: Auto-switch to smaller model on 4B failure (Gallery-inspired pattern)
                    handleModelInitializationFailure(context, task, model, error)
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

    /**
     * Handle model initialization failure with intelligent fallback.
     * Critical feature: Auto-switch to 2B model when 4B model crashes.
     */
    private fun handleModelInitializationFailure(
        context: Context, 
        task: Task, 
        failedModel: Model, 
        error: String
    ) {
        Log.w(TAG, "🚨 Handling model initialization failure for: ${failedModel.name}")
        Log.w(TAG, "Error details: $error")
        
        // Check if this was a memory-related crash (common patterns from TensorFlow Lite)
        val isMemoryRelatedCrash = error.contains("Cannot reserve space", ignoreCase = true) ||
                                  error.contains("OutOfMemoryError", ignoreCase = true) ||
                                  error.contains("memory", ignoreCase = true) ||
                                  error.contains("allocation", ignoreCase = true)
        
        // Check if the failed model is the 4B model
        val is4BModel = failedModel.name.contains("E4B", ignoreCase = true)
        
        if (is4BModel && isMemoryRelatedCrash) {
            Log.w(TAG, "🔄 Detected 4B model memory crash - attempting auto-switch to 2B model")
            
            // Find the 2B model as fallback
            val availableModels = task.models
            val fallback2BModel = availableModels.find { 
                it.name.contains("E2B", ignoreCase = true) && it != failedModel 
            }
            
            if (fallback2BModel != null) {
                Log.w(TAG, "✅ Found 2B fallback model: ${fallback2BModel.name}")
                
                // Check if the 2B model is compatible with this device
                val compatibility = deviceCapabilityChecker.assessModelCompatibility(context, fallback2BModel)
                
                if (compatibility.riskLevel != DeviceCapabilityChecker.RiskLevel.DANGEROUS) {
                    Log.w(TAG, "🔄 Auto-switching to 2B model due to 4B model crash...")
                    Log.w(TAG, "2B Model compatibility: ${compatibility.riskLevel} (${String.format("%.1f", compatibility.memoryUtilization * 100)}% memory usage)")
                    
                    // Update selected model to 2B
                    _uiState.update { currentState ->
                        currentState.copy(selectedModel = fallback2BModel)
                    }
                    
                    // Save crash information for future prevention
                    saveCrashInfo(failedModel, error)
                    
                    // Auto-initialize the 2B model
                    viewModelScope.launch(Dispatchers.Default) {
                        delay(1000) // Brief delay to let cleanup complete
                        Log.w(TAG, "🚀 Auto-initializing 2B fallback model...")
                        initializeModel(context, task, fallback2BModel, force = true)
                    }
                } else {
                    Log.e(TAG, "❌ 2B model is also too large for this device - no automatic fallback possible")
                    Log.e(TAG, "Device needs manual intervention or app may not be compatible")
                }
            } else {
                Log.e(TAG, "❌ No 2B fallback model found - cannot auto-recover from 4B crash")
            }
        } else {
            Log.w(TAG, "⚠️ Model failure not suitable for auto-fallback:")
            Log.w(TAG, "  Is 4B model: $is4BModel")
            Log.w(TAG, "  Is memory-related: $isMemoryRelatedCrash")
            Log.w(TAG, "  Manual intervention may be required")
        }
    }

    /**
     * Save crash information to SharedPreferences for crash detection on app restart.
     */
    private fun saveCrashInfo(crashedModel: Model, error: String) {
        try {
            val prefs = context.getSharedPreferences("eliza_crash_detection", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("last_crashed_model", crashedModel.name)
                .putString("crash_error", error)
                .putLong("crash_timestamp", System.currentTimeMillis())
                .putBoolean("has_crashed", true)
                .apply()
            
            Log.d(TAG, "💾 Saved crash info for future crash detection")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash info", e)
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
     * Download a model with direct API token authentication
     * Simplified approach without OAuth complexity
     */
    fun downloadModel(context: Context, task: Task, model: Model) {
        Log.d(TAG, "Starting download for model '${model.name}' using direct API token authentication")
        
        // Clear any previous error states first
        clearModelErrorStates(model)
        
        // Gallery's exact pattern: Update status to IN_PROGRESS first
        setDownloadStatus(
            curModel = model,
            status = ModelDownloadStatus(status = ModelDownloadStatusType.IN_PROGRESS)
        )
        
        // Gallery's exact pattern: Delete existing model files first
        deleteModel(task = task, model = model)
        
        // 🔑 Simplified Direct API Token Authentication (OAuth removed)
        if (model.url.startsWith("https://huggingface.co")) {
            Log.d(TAG, "Model '${model.name}' is from HuggingFace. Using direct API token...")
            
            // Use Dispatchers.IO for network calls
            viewModelScope.launch(Dispatchers.IO) {
                // Check current token status (should be populated by initializeDirectApiToken)
                val tokenStatusAndData = getTokenStatusAndData()
                
                when (tokenStatusAndData.status) {
                    TokenStatus.NOT_STORED, TokenStatus.EXPIRED -> {
                        Log.e(TAG, "No valid API token found. Please check HUGGINGFACE_API_TOKEN in local.properties")
                        withContext(Dispatchers.Main) {
                            setDownloadStatus(
                                curModel = model,
                                status = ModelDownloadStatus(
                                    status = ModelDownloadStatusType.FAILED,
                                    errorMessage = "API token not found. Please check HUGGINGFACE_API_TOKEN in local.properties"
                                )
                            )
                        }
                    }
                    
                    TokenStatus.NOT_EXPIRED -> {
                        // Use stored API token for download
                        Log.d(TAG, "Using stored API token for download...")
                        withContext(Dispatchers.Main) {
                            startDownloadWithToken(model, tokenStatusAndData.data!!.accessToken)
                        }
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
            // Memory warning dialog defaults
            showMemoryWarning = false,
            memoryWarningModel = null,
            memoryWarningCompatibility = null,
            memoryWarningDeviceInfo = null,
        )
    }

    // ✨ Direct API Token Management (OAuth removed) ✨
    
    /**
     * 🔑 Direct API Token Initialization 
     * Uses HuggingFace API token from environment variable for authentication
     * This is much simpler than OAuth for development and personal use
     */
    private fun initializeDirectApiToken() {
        // Read HuggingFace API token from environment variable
        val directApiToken = BuildConfig.HUGGINGFACE_API_TOKEN
        
        if (directApiToken.isEmpty()) {
            Log.e(TAG, "HUGGINGFACE_API_TOKEN not found in local.properties! Please add your token.")
            return
        }
        
        Log.d(TAG, "Initializing direct API token for HuggingFace authentication...")
        
        // Check if we already have a valid token
        val tokenStatusAndData = getTokenStatusAndData()
        
        when (tokenStatusAndData.status) {
            TokenStatus.NOT_STORED, TokenStatus.EXPIRED -> {
                Log.d(TAG, "No valid token found. Saving direct API token to DataStore...")
                
                // Save the API token with far future expiration (1 year from now)
                val oneYearFromNow = System.currentTimeMillis() + (365L * 24L * 60L * 60L * 1000L)
                
                dataStoreRepository.saveAccessTokenData(
                    accessToken = directApiToken,
                    refreshToken = directApiToken, // API tokens don't need refresh, so reuse the same token
                    expiresAt = oneYearFromNow
                )
                
                curAccessToken = directApiToken
                Log.d(TAG, "✅ Direct API token saved successfully. Authentication ready!")
            }
            
            TokenStatus.NOT_EXPIRED -> {
                Log.d(TAG, "✅ Valid token already exists. Using stored token for authentication.")
                curAccessToken = tokenStatusAndData.data?.accessToken
            }
        }
    }

    /**
     * EXACT COPY of Gallery's getModelUrlResponse method
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
     * Save access token data securely
     * Simplified version for direct API token usage
     */
    private fun saveAccessToken(accessToken: String, refreshToken: String, expiresAt: Long) {
        dataStoreRepository.saveAccessTokenData(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
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
