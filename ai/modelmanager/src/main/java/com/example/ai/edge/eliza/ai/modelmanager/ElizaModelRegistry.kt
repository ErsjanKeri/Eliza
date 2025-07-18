package com.example.ai.edge.eliza.ai.modelmanager

import android.content.Context
import android.util.Log
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelSwitchResult
import com.example.ai.edge.eliza.core.model.ModelPerformance
import com.example.ai.edge.eliza.core.model.DeviceCapabilities
import com.example.ai.edge.eliza.core.model.VariantConfig
import com.example.ai.edge.eliza.core.model.createLlmChatConfigs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElizaModelRegistry"



/**
 * Registry for managing Gemma model variants with MatFormer architecture support.
 * 
 * This registry implements the MatFormer concept where E2B is a nested subset of E4B,
 * allowing for efficient variant switching without requiring separate model downloads.
 * 
 * Migrated from enum-based to String-based variant approach while preserving
 * all sophisticated device capability analysis and performance optimization features.
 */
@Singleton
class ElizaModelRegistry @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    // Single model with variants instead of mapping
    private var gemmaModel: Model? = null
    
    // Configuration for model switching
    private lateinit var config: ModelSwitchConfig
    
    init {
        initializeDefaultModels()
    }
    
    /**
     * Initializes the registry with default models.
     */
    private fun initializeDefaultModels() {
        // Create Gemma-3n model with E4B/E2B variants (MatFormer architecture)
        gemmaModel = Model(
            name = "Gemma-3n-it-int4",
            downloadFileName = "gemma-3n-E4B-it-int4.task",
            url = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview/resolve/main/gemma-3n-E4B-it-int4.task?download=true",
            sizeInBytes = 4_405_655_031L, // 4.4GB
            estimatedPeakMemoryInBytes = 6_979_321_856L, // ~7GB for E4B
            llmSupportImage = true,
            version = "20250520",
            
            // MatFormer variants - same file, different parameter usage
            availableVariants = listOf("E4B", "E2B"),
            currentVariant = "E4B", // Default to E4B
            variantConfigs = mapOf(
                "E4B" to VariantConfig(
                    topK = 64,
                    topP = 0.95f,
                    temperature = 0.8f,
                    maxTokens = 1024,
                    memoryOptimized = false,
                    useGPU = true,
                    estimatedMemoryBytes = 6_979_321_856L, // ~7GB full model
                    inferenceSpeedTokensPerSecond = 4.0f,
                    qualityScore = 0.95f,
                    isOptimizedForDevice = false
                ),
                "E2B" to VariantConfig(
                    topK = 32,
                    topP = 0.9f,
                    temperature = 0.7f,
                    maxTokens = 512,
                    memoryOptimized = true,
                    useGPU = false,
                    estimatedMemoryBytes = 3_500_000_000L, // ~3.5GB with PLE optimization
                    inferenceSpeedTokensPerSecond = 8.0f,
                    qualityScore = 0.8f,
                    isOptimizedForDevice = true
                )
            )
        )
        
        Log.d(TAG, "Initialized ModelRegistry with Gemma model containing ${gemmaModel?.availableVariants?.size} variants")
    }
    
    /**
     * Configures the registry with default MatFormer settings.
     */
    fun configureWithDefaults() {
        config = ModelSwitchConfig(
            defaultVariant = "E4B",
            allowAutomaticSwitching = true,
            memoryThresholdForE2B = 4_000_000_000L,
            preferenceBasedSwitching = true
        )
        
        Log.d(TAG, "Configured ModelRegistry with default variant: ${config.defaultVariant}")
    }
    
    /**
     * Gets the current active model.
     */
    fun getCurrentModel(): Model? = gemmaModel
    
    /**
     * Gets the model for a specific variant (updates current variant).
     */
    fun getModelForVariant(variant: String): Model? {
        return gemmaModel?.takeIf { it.switchToVariant(variant) }
    }
    
    /**
     * Gets all available variants.
     */
    fun getAvailableVariants(): List<String> {
        return gemmaModel?.availableVariants ?: emptyList()
    }
    
    /**
     * Switches to a specified variant.
     * This is a high-level operation that coordinates the variant change.
     */
    fun switchToVariant(targetVariant: String): Flow<ModelSwitchResult> = flow {
        try {
            Log.d(TAG, "Switching to variant: $targetVariant")
            
            val model = gemmaModel
            if (model == null) {
                emit(ModelSwitchResult.Error("No model available"))
                return@flow
            }
            
            // Validate variant availability
            if (!isVariantAvailable(targetVariant)) {
                emit(ModelSwitchResult.Error("Variant $targetVariant not available"))
                return@flow
            }
            
            // Emit loading state
            emit(ModelSwitchResult.Loading("Switching to $targetVariant..."))
            
            // Perform the switch
            if (!model.switchToVariant(targetVariant)) {
                emit(ModelSwitchResult.Error("Failed to switch to variant $targetVariant"))
                return@flow
            }
            
            emit(ModelSwitchResult.Success("Switched to $targetVariant"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to variant: $targetVariant", e)
            emit(ModelSwitchResult.Error("Switch failed: ${e.message}"))
        }
    }
    
    /**
     * Checks if a variant is available (i.e., model is downloaded and variant exists).
     */
    fun isVariantAvailable(variant: String): Boolean {
        val model = gemmaModel ?: return false
        return variant in model.availableVariants && model.isDownloaded(context)
    }
    
    /**
     * Get performance characteristics for a specific variant.
     */
    fun getVariantPerformance(variant: String): ModelPerformance? {
        val model = gemmaModel ?: return null
        val config = model.variantConfigs[variant] ?: return null
        
        return ModelPerformance(
            variant = variant,
            estimatedMemoryMB = config.estimatedMemoryBytes / (1024 * 1024),
            inferenceSpeedTokensPerSecond = config.inferenceSpeedTokensPerSecond,
            qualityScore = config.qualityScore,
            isOptimizedForDevice = config.isOptimizedForDevice
        )
    }
    
    /**
     * Get recommended variant based on device capabilities.
     * This implements intelligent variant selection using MatFormer principles.
     */
    fun getRecommendedVariant(deviceCapabilities: DeviceCapabilities): String {
        val model = gemmaModel ?: return "E4B"
        
        return when {
            // Low memory devices should use E2B
            deviceCapabilities.isLowMemory -> "E2B"
            
            // Devices with less than 6GB available should use E2B
            deviceCapabilities.availableMemoryGB < 6 -> "E2B"
            
            // High-performance devices can use E4B
            deviceCapabilities.preferPerformance -> "E4B"
            
            // Default to E2B for better compatibility
            else -> "E2B"
        }
    }
    
    /**
     * Get recommended variant based on current device state.
     * This is a convenience overload that automatically detects device capabilities.
     */
    fun getRecommendedVariant(): String {
        // Analyze current device capabilities
        val deviceCapabilities = getCurrentDeviceCapabilities()
        return getRecommendedVariant(deviceCapabilities)
    }
    
    /**
     * Check if a variant can be used efficiently on the current device.
     */
    fun isVariantOptimalForDevice(variant: String, deviceCapabilities: DeviceCapabilities): Boolean {
        val performance = getVariantPerformance(variant) ?: return false
        val requiredMemoryGB = performance.estimatedMemoryMB / 1024
        
        return when {
            // E2B is optimal for low-memory devices
            variant == "E2B" && deviceCapabilities.isLowMemory -> true
            
            // E4B requires sufficient memory and performance preference
            variant == "E4B" && 
            deviceCapabilities.availableMemoryGB >= requiredMemoryGB && 
            deviceCapabilities.preferPerformance -> true
            
            // E2B is safe for most devices
            variant == "E2B" -> true
            
            else -> false
        }
    }
    
    /**
     * Get all available variants with their performance characteristics.
     */
    fun getAvailableVariantsWithPerformance(): List<ModelPerformance> {
        return getAvailableVariants().mapNotNull { variant ->
            getVariantPerformance(variant)
        }
    }
    
    /**
     * Get optimal variant based on current system state and performance requirements.
     */
    fun getOptimalVariant(
    preferQuality: Boolean = false,
    maxMemoryGB: Long? = null,
    requireGPU: Boolean = false
): String {
    val availableVariants = getAvailableVariants()
    val deviceCapabilities = getCurrentDeviceCapabilities()
    
    return availableVariants
        .mapNotNull { variant -> 
            val performance = getVariantPerformance(variant)
            if (performance != null) {
                variant to performance
            } else {
                null
            }
        }
        .filter { (variant, performance) ->
            // Filter by memory constraint
            val perf = performance ?: return@filter false
            maxMemoryGB?.let { maxMem ->
                perf.estimatedMemoryMB / 1024 <= maxMem
            } ?: true
        }
        .filter { (variant, performance) ->
            // Filter by GPU requirement
            val perf = performance ?: return@filter false
            if (requireGPU) {
                gemmaModel?.variantConfigs?.get(variant)?.useGPU == true
            } else {
                true
            }
        }
        .maxByOrNull { (variant, performance) ->
            if (preferQuality) {
                // Prioritize quality score
                performance.qualityScore
            } else {
                // Prioritize inference speed
                performance.inferenceSpeedTokensPerSecond
            }
        }?.first ?: getRecommendedVariant(deviceCapabilities)
    }

    
    /**
     * Get current device capabilities for intelligent variant selection.
     */
    private fun getCurrentDeviceCapabilities(): DeviceCapabilities {
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)

        val availableMemoryGB = memoryInfo.availMem / (1024 * 1024 * 1024)
        val isLowMemory = memoryInfo.lowMemory
        
        return DeviceCapabilities(
            availableMemoryGB = availableMemoryGB,
            isLowMemory = isLowMemory,
            preferPerformance = availableMemoryGB > 6 && !isLowMemory
        )
    }
    
    /**
     * Get variant recommendation with detailed reasoning.
     */
    fun getVariantRecommendationWithReason(): VariantRecommendation {
        val deviceCapabilities = getCurrentDeviceCapabilities()
        val recommendedVariant = getRecommendedVariant(deviceCapabilities)
        val performance = getVariantPerformance(recommendedVariant)
        
        val reason = when {
            deviceCapabilities.isLowMemory -> "Device is low on memory, using memory-optimized variant"
            deviceCapabilities.availableMemoryGB < 6 -> "Limited memory available (${deviceCapabilities.availableMemoryGB}GB), using efficient variant"
            deviceCapabilities.preferPerformance -> "High-performance device detected, but recommending balanced variant for stability"
            else -> "Balanced variant recommended for optimal performance/memory ratio"
        }
        
        return VariantRecommendation(
            variant = recommendedVariant,
            reason = reason,
            confidence = if (deviceCapabilities.isLowMemory) 0.95f else 0.8f,
            performance = performance
        )
    }
}

/**
 * Updated model switch configuration for string-based variants.
 */
data class ModelSwitchConfig(
    val defaultVariant: String = "E4B",
    val allowAutomaticSwitching: Boolean = true,
    val memoryThresholdForE2B: Long = 4_000_000_000L,
    val preferenceBasedSwitching: Boolean = true
)

/**
 * Updated model performance with string-based variant.
 */
data class ModelPerformance(
    val variant: String,
    val estimatedMemoryMB: Long,
    val inferenceSpeedTokensPerSecond: Float,
    val qualityScore: Float,
    val isOptimizedForDevice: Boolean
)

/**
 * Variant recommendation with reasoning.
 */
data class VariantRecommendation(
    val variant: String,
    val reason: String,
    val confidence: Float,
    val performance: ModelPerformance?
)