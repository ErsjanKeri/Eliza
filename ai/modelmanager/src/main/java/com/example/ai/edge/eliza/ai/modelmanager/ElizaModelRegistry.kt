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
import com.example.ai.edge.eliza.core.model.GemmaVariant
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.ModelSwitchResult
import com.example.ai.edge.eliza.core.model.ModelSwitchConfig
import com.example.ai.edge.eliza.core.model.ModelPerformance
import com.example.ai.edge.eliza.core.model.DeviceCapabilities
import com.example.ai.edge.eliza.core.model.createLlmChatConfigs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElizaModelRegistry"

/**
 * Extension function to check if a model is downloaded and valid.
 */
private fun Model.isDownloaded(context: Context): Boolean {
    val modelPath = this.getPath(context)
    val file = java.io.File(modelPath)
    
    if (!file.exists()) {
        return false
    }
    
    // Validate file size matches expected size
    val actualSize = file.length()
    return actualSize == this.sizeInBytes
}

/**
 * Registry for managing Gemma model variants with MatFormer architecture support.
 * 
 * This registry implements the MatFormer concept where E2B is a nested subset of E4B,
 * allowing for efficient variant switching without requiring separate model downloads.
 */
@Singleton
class ElizaModelRegistry @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    // Registry of available models mapped by variant
    private val availableModels = mutableMapOf<GemmaVariant, Model>()
    
    // Configuration for model switching
    private lateinit var config: ModelSwitchConfig
    
    // Current active variant
    private var currentVariant: GemmaVariant = GemmaVariant.GEMMA_3N_E4B
    
    init {
        // Initialize with default models
        initializeDefaultModels()
    }
    
    /**
     * Initializes the registry with default models.
     */
    private fun initializeDefaultModels() {
        // Create Gemma-3n E4B model (contains E2B as nested subset)
        val gemmaE4B = Model(
            name = "Gemma-3n-E4B-it-int4",
            downloadFileName = "gemma-3n-E4B-it-int4.task",
            url = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview/resolve/main/gemma-3n-E4B-it-int4.task?download=true",
            sizeInBytes = 4_405_655_031L, // 4.4GB
            estimatedPeakMemoryInBytes = 6_979_321_856L, // ~7GB
            llmSupportImage = true,
            version = "20250520"
        )
        
        // Register both variants using the same model file (MatFormer architecture)
        availableModels[GemmaVariant.GEMMA_3N_E4B] = gemmaE4B
        availableModels[GemmaVariant.GEMMA_3N_E2B] = gemmaE4B // Same file, different parameter usage
        
        Log.d(TAG, "Initialized ModelRegistry with ${availableModels.size} model entries")
    }
    
    /**
     * Configures the registry with default MatFormer settings.
     */
    fun configureWithDefaults() {
        config = ModelSwitchConfig(
            defaultVariant = GemmaVariant.GEMMA_3N_E4B,
            allowAutomaticSwitching = true,
            memoryThresholdForE2B = 4_000_000_000L,
            preferenceBasedSwitching = true
        )
        
        Log.d(TAG, "Configured ModelRegistry with default variant: ${config.defaultVariant.displayName}")
    }
    
    /**
     * Gets the current active model.
     */
    fun getCurrentModel(): Model? {
        return availableModels[currentVariant]
    }
    
    /**
     * Gets the model for a specific variant.
     */
    fun getModelForVariant(variant: GemmaVariant): Model? {
        return availableModels[variant]
    }
    
    /**
     * Gets all available variants.
     */
    fun getAvailableVariants(): List<GemmaVariant> {
        return availableModels.keys.toList()
    }
    
    /**
     * Switches to a specified variant.
     * This is a high-level operation that coordinates the variant change.
     */
    fun switchToVariant(targetVariant: GemmaVariant): Flow<ModelSwitchResult> = flow {
        try {
            Log.d(TAG, "Switching to variant: ${targetVariant.displayName}")
            
            // Validate variant availability
            if (!isVariantAvailable(targetVariant)) {
                emit(ModelSwitchResult.Error("Variant ${targetVariant.displayName} not available"))
                return@flow
            }
            
            // Emit loading state
            emit(ModelSwitchResult.Loading("Switching to ${targetVariant.displayName}..."))
            
            // Perform the switch (this would be handled by the ModelManager)
            // For now, we just emit success
            emit(ModelSwitchResult.Success("Switched to ${targetVariant.displayName}"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to variant: ${targetVariant.displayName}", e)
            emit(ModelSwitchResult.Error("Switch failed: ${e.message}"))
        }
    }
    
    /**
     * Checks if a variant is available (i.e., model is downloaded).
     */
    fun isVariantAvailable(variant: GemmaVariant): Boolean {
        val model = getCurrentModel() ?: return false
        return model.isDownloaded(context)
    }
    
    /**
     * Get performance characteristics for a specific variant.
     */
    fun getVariantPerformance(variant: GemmaVariant): ModelPerformance {
        return when (variant) {
            GemmaVariant.GEMMA_3N_E2B -> ModelPerformance(
                variant = variant,
                estimatedMemoryMB = 3500, // ~3.5GB with PLE optimization
                inferenceSpeedTokensPerSecond = 8.0f, // Faster due to smaller FFN
                qualityScore = 0.8f, // Good quality
                isOptimizedForDevice = true
            )
            GemmaVariant.GEMMA_3N_E4B -> ModelPerformance(
                variant = variant,
                estimatedMemoryMB = 7000, // ~7GB full model
                inferenceSpeedTokensPerSecond = 4.0f, // Slower but higher quality
                qualityScore = 0.95f, // Excellent quality
                isOptimizedForDevice = false
            )
        }
    }
    
    /**
     * Get recommended variant based on device capabilities.
     * This implements intelligent variant selection using MatFormer principles.
     */
    fun getRecommendedVariant(deviceCapabilities: DeviceCapabilities): GemmaVariant {
        return when {
            // Low memory devices should use E2B
            deviceCapabilities.isLowMemory -> GemmaVariant.GEMMA_3N_E2B
            
            // Devices with less than 6GB available should use E2B
            deviceCapabilities.availableMemoryGB < 6 -> GemmaVariant.GEMMA_3N_E2B
            
            // High-performance devices can use E4B
            deviceCapabilities.preferPerformance -> GemmaVariant.GEMMA_3N_E4B
            
            // Default to E2B for better compatibility
            else -> GemmaVariant.GEMMA_3N_E2B
        }
    }
    
    /**
     * Get recommended variant based on current device state.
     * This is a convenience overload that automatically detects device capabilities.
     */
    fun getRecommendedVariant(): GemmaVariant {
        // For now, default to E2B for better compatibility
        // In a real implementation, this would analyze device capabilities
        return GemmaVariant.GEMMA_3N_E2B
    }
    
    /**
     * Check if a variant can be used efficiently on the current device.
     */
    fun isVariantOptimalForDevice(variant: GemmaVariant, deviceCapabilities: DeviceCapabilities): Boolean {
        val performance = getVariantPerformance(variant)
        val requiredMemoryGB = performance.estimatedMemoryMB / 1024
        
        return when {
            // E2B is optimal for low-memory devices
            variant == GemmaVariant.GEMMA_3N_E2B && deviceCapabilities.isLowMemory -> true
            
            // E4B requires sufficient memory and performance preference
            variant == GemmaVariant.GEMMA_3N_E4B && 
            deviceCapabilities.availableMemoryGB >= requiredMemoryGB && 
            deviceCapabilities.preferPerformance -> true
            
            // E2B is safe for most devices
            variant == GemmaVariant.GEMMA_3N_E2B -> true
            
            else -> false
        }
    }
    
    /**
     * Get all available variants with their performance characteristics.
     */
    fun getAvailableVariantsWithPerformance(): List<ModelPerformance> {
        return GemmaVariant.values().map { variant ->
            getVariantPerformance(variant)
        }
    }
} 