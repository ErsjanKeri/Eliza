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

import com.example.ai.edge.eliza.core.model.GemmaVariant

/**
 * Configuration for Gemma model variants and switching behavior.
 * 
 * This configuration makes it easy to:
 * 1. Change the default model variant (just change DEFAULT_VARIANT)
 * 2. Enable/disable automatic switching based on device capabilities
 * 3. Adjust memory thresholds and performance settings
 * 4. Configure MatFormer behavior for nested model switching
 */
object ModelConfig {
    
    
    /**
     * Default model variant to use.
     * 
     * Change this to switch between variants:
     * - GemmaVariant.GEMMA_3N_E4B: Highest quality (4B effective params, ~7GB memory)
     * - GemmaVariant.GEMMA_3N_E2B: Faster speed (2B effective params, ~3.5GB memory)
     */
    val DEFAULT_VARIANT = GemmaVariant.GEMMA_3N_E4B
    
    /**
     * Enable automatic variant switching based on device capabilities.
     * 
     * When true:
     * - Automatically switches to E2B on low-memory devices
     * - Switches to E4B on high-memory devices
     * - Switches during runtime if memory pressure is detected
     */
    const val ENABLE_AUTO_SWITCHING = true
    
    /**
     * Memory threshold for automatic switching (0.0 to 1.0).
     * 
     * When memory usage exceeds this threshold:
     * - Automatically switches from E4B to E2B
     * - Helps prevent out-of-memory errors
     */
    const val AUTO_SWITCH_MEMORY_THRESHOLD = 0.8f
    
    /**
     * Enable performance monitoring for optimal variant selection.
     * 
     * When true:
     * - Monitors inference speed and memory usage
     * - Suggests optimal variant based on performance
     * - Logs performance metrics for debugging
     */
    const val ENABLE_PERFORMANCE_MONITORING = true
    
    /**
     * Preload nested variants for faster switching.
     * 
     * When true:
     * - Preloads E2B configuration when E4B is loaded
     * - Enables near-instant switching between variants
     * - Uses slightly more memory but much faster switching
     */
    const val PRELOAD_NESTED_VARIANTS = true
    
    // ===========================================
    // DEVICE CAPABILITY THRESHOLDS
    // ===========================================
    
    /**
     * Memory thresholds for automatic variant recommendation.
     */
    object MemoryThresholds {
        /** Minimum memory (GB) to recommend E4B variant */
        const val E4B_MIN_MEMORY_GB = 6.0f
        
        /** Minimum memory (GB) to recommend E2B variant */
        const val E2B_MIN_MEMORY_GB = 3.0f
        
        /** Fallback to E2B if memory is below this threshold */
        const val FALLBACK_THRESHOLD_GB = 2.5f
    }
    
    /**
     * Performance thresholds for variant switching.
     */
    object PerformanceThresholds {
        /** Maximum acceptable inference time (ms) for E4B */
        const val E4B_MAX_INFERENCE_TIME_MS = 5000L
        
        /** Maximum acceptable inference time (ms) for E2B */
        const val E2B_MAX_INFERENCE_TIME_MS = 3000L
        
        /** Switch to faster variant if inference time exceeds threshold */
        const val ENABLE_PERFORMANCE_BASED_SWITCHING = true
    }
    
    // ===========================================
    //  MATFORMER ARCHITECTURE SETTINGS
    // ===========================================
    
    /**
     * MatFormer-specific configuration for nested model switching.
     */
    object MatFormerConfig {
        /** Enable Mix'n'Match custom model sizing */
        const val ENABLE_MIX_N_MATCH = false // Disabled for now, may be enabled in future
        
        /** Enable elastic execution (dynamic switching during inference) */
        const val ENABLE_ELASTIC_EXECUTION = false // Future feature
        
        /** Enable Per-Layer Embeddings (PLE) for memory optimization */
        const val ENABLE_PLE = true
        
        /** Enable KV Cache sharing between variants */
        const val ENABLE_KV_CACHE_SHARING = true
    }
    
    // ===========================================
    // EASY VARIANT SWITCHING METHODS
    // ===========================================
    
    /**
     * Quick switch to high-quality variant.
     * Use this method in your code to easily switch to E4B.
     */
    fun useHighQualityVariant() = GemmaVariant.GEMMA_3N_E4B
    
    /**
     * Quick switch to fast variant.
     * Use this method in your code to easily switch to E2B.
     */
    fun useFastVariant() = GemmaVariant.GEMMA_3N_E2B
    
    /**
     * Get variant based on use case.
     */
    fun getVariantForUseCase(useCase: UseCase): GemmaVariant {
        return when (useCase) {
            UseCase.EDUCATIONAL_TUTORING -> GemmaVariant.GEMMA_3N_E4B // High quality for education
            UseCase.QUICK_RESPONSES -> GemmaVariant.GEMMA_3N_E2B // Fast for quick responses
            UseCase.RESOURCE_CONSTRAINED -> GemmaVariant.GEMMA_3N_E2B // E2B for low resources
            UseCase.DEMO_SHOWCASE -> GemmaVariant.GEMMA_3N_E4B // E4B for demos
        }
    }
    
    /**
     * Use cases for different model variants.
     */
    enum class UseCase {
        EDUCATIONAL_TUTORING,
        QUICK_RESPONSES,
        RESOURCE_CONSTRAINED,
        DEMO_SHOWCASE
    }
    
    // ===========================================
    // DEBUGGING AND LOGGING
    // ===========================================
    
    /**
     * Enable detailed logging for model switching.
     */
    const val ENABLE_DEBUG_LOGGING = true
    
    /**
     * Enable performance benchmarking.
     */
    const val ENABLE_BENCHMARKING = false
    
    /**
     * Log memory usage during switching.
     */
    const val LOG_MEMORY_USAGE = true
} 