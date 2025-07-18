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

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Gemma model variants based on Google's MatFormer architecture.
 * 
 * MatFormer (Matryoshka Transformer) allows one model file to contain multiple 
 * nested model sizes. The E4B model contains E2B as a subset, using parameter 
 * sharing and nested FFN blocks.
 * 
 * Key insights from Google's implementation:
 * - E4B (4B effective params) = 8B total params with PLE (Per-Layer Embeddings)
 * - E2B (2B effective params) = subset of E4B model, no separate download needed
 * - Mix'n'Match allows custom sizes between E2B and E4B
 * - PLE offloads embeddings to CPU, reducing GPU memory requirements
 */
@Serializable
enum class GemmaVariant(
    val modelId: String,
    val effectiveParams: String,
    val totalParams: String,
    val sizeInBytes: Long,
    val estimatedPeakMemoryInBytes: Long,
    val isNested: Boolean = false,
    val description: String
) {
    /**
     * Gemma 3n E4B - Full model with 4B effective parameters.
     * This is the only model file you need to download.
     * Contains E2B as a nested subset via MatFormer architecture.
     */
    GEMMA_3N_E4B(
        modelId = "gemma-3n-E4B-it-int4",
        effectiveParams = "4B",
        totalParams = "8B",
        sizeInBytes = 4_405_655_031L,
        estimatedPeakMemoryInBytes = 6_979_321_856L,
        isNested = false,
        description = "Full E4B model with nested E2B subset via MatFormer architecture"
    ),
    
    /**
     * Gemma 3n E2B - Nested subset of E4B model.
     * Uses same model file as E4B but activates only subset of parameters.
     * Provides 2x faster inference with reduced memory footprint.
     */
    GEMMA_3N_E2B(
        modelId = "gemma-3n-E2B-it-int4",
        effectiveParams = "2B",
        totalParams = "5B",
        sizeInBytes = 4_405_655_031L, // Same file as E4B
        estimatedPeakMemoryInBytes = 3_500_000_000L, // Reduced due to PLE
        isNested = true,
        description = "Nested E2B subset of E4B model, 2x faster inference"
    );

    /**
     * Gets the display name for UI purposes.
     */
    val displayName: String
        get() = "Gemma 3n ${effectiveParams} (${totalParams} total)"
    
    /**
     * Gets the performance characteristics.
     */
    val performanceLevel: String
        get() = when (this) {
            GEMMA_3N_E4B -> "Highest Quality"
            GEMMA_3N_E2B -> "Balanced Speed"
        }
    
    /**
     * Gets the memory requirements in GB.
     */
    val memoryRequirementGB: Float
        get() = estimatedPeakMemoryInBytes / 1_000_000_000f
    
    /**
     * Checks if this variant requires a separate download.
     */
    val requiresSeparateDownload: Boolean
        get() = !isNested
}

/**
 * Configuration for model switching behavior.
 */
@Serializable
data class ModelSwitchConfig(
    val defaultVariant: GemmaVariant = GemmaVariant.GEMMA_3N_E4B,
    val allowAutomaticSwitching: Boolean = true,
    val memoryThresholdForE2B: Long = 4_000_000_000L, // 4GB
    val preferenceBasedSwitching: Boolean = true
) 