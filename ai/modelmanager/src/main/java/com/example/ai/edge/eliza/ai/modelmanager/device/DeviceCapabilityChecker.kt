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

package com.example.ai.edge.eliza.ai.modelmanager.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.ai.edge.eliza.core.model.Model
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Device capability checker using Gallery's proven memory detection pattern.
 * Determines if device can handle specific AI models based on memory requirements.
 */
@Singleton
class DeviceCapabilityChecker @Inject constructor() {
    
    companion object {
        private const val TAG = "DeviceCapabilityChecker"
        
        // Gallery's exact system memory reserve - 3GB for Android system
        private const val SYSTEM_RESERVED_MEMORY_IN_BYTES = 3 * (1L shl 30) // 3GB
        
        // Memory safety thresholds
        private const val MEMORY_WARNING_THRESHOLD = 0.9f // Warn if using >90% of available memory
        private const val MEMORY_CRITICAL_THRESHOLD = 1.0f // Critical if using 100%+ of available memory
    }

    /**
     * Device memory information following Gallery's pattern.
     */
    data class DeviceMemoryInfo(
        val totalMemoryBytes: Long,
        val availableMemoryBytes: Long,
        val usableMemoryBytes: Long, // After system reserve
        val isLowMemoryDevice: Boolean,
        val deviceModel: String,
        val androidVersion: String
    ) {
        val totalMemoryGB: Float get() = totalMemoryBytes / (1024f * 1024f * 1024f)
        val availableMemoryGB: Float get() = availableMemoryBytes / (1024f * 1024f * 1024f)
        val usableMemoryGB: Float get() = usableMemoryBytes / (1024f * 1024f * 1024f)
        
        fun canRunModel(modelMemoryRequirement: Long): Boolean {
            return usableMemoryBytes >= modelMemoryRequirement
        }
        
        fun getMemoryUtilization(modelMemoryRequirement: Long): Float {
            return modelMemoryRequirement.toFloat() / usableMemoryBytes.toFloat()
        }
    }

    /**
     * Model compatibility assessment.
     */
    data class ModelCompatibility(
        val isCompatible: Boolean,
        val memoryUtilization: Float, // 0.0 to 2.0+ (>1.0 means exceeds available memory)
        val riskLevel: RiskLevel,
        val recommendation: String,
        val warnings: List<String> = emptyList()
    )

    enum class RiskLevel {
        SAFE,       // <70% memory usage
        WARNING,    // 70-90% memory usage  
        CRITICAL,   // 90-100% memory usage
        DANGEROUS   // >100% memory usage (will likely crash)
    }

    /**
     * Get device memory information using Gallery's exact pattern.
     */
    fun getDeviceMemoryInfo(context: Context): DeviceMemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        Log.d(TAG, "Raw device memory - Available: ${memoryInfo.availMem} bytes, Total: ${memoryInfo.totalMem} bytes")
        
        // Gallery's exact calculation pattern
        val usableMemory = max(memoryInfo.availMem, memoryInfo.totalMem - SYSTEM_RESERVED_MEMORY_IN_BYTES)
        
        val deviceInfo = DeviceMemoryInfo(
            totalMemoryBytes = memoryInfo.totalMem,
            availableMemoryBytes = memoryInfo.availMem,
            usableMemoryBytes = usableMemory,
            isLowMemoryDevice = activityManager.isLowRamDevice,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
        
        Log.d(TAG, "Device capability analysis:")
        Log.d(TAG, "  Device: ${deviceInfo.deviceModel}")
        Log.d(TAG, "  Android: ${deviceInfo.androidVersion}")
        Log.d(TAG, "  Total RAM: ${String.format("%.1f", deviceInfo.totalMemoryGB)} GB")
        Log.d(TAG, "  Available RAM: ${String.format("%.1f", deviceInfo.availableMemoryGB)} GB")
        Log.d(TAG, "  Usable RAM (after system reserve): ${String.format("%.1f", deviceInfo.usableMemoryGB)} GB")
        Log.d(TAG, "  Low RAM device: ${deviceInfo.isLowMemoryDevice}")
        
        return deviceInfo
    }

    /**
     * Assess model compatibility with current device using Gallery's logic.
     */
    fun assessModelCompatibility(
        context: Context, 
        model: Model
    ): ModelCompatibility {
        val deviceInfo = getDeviceMemoryInfo(context)
        val modelMemoryRequirement = model.estimatedPeakMemoryInBytes ?: 0L
        
        if (modelMemoryRequirement == 0L) {
            Log.w(TAG, "Model ${model.name} has no memory requirement specified")
            return ModelCompatibility(
                isCompatible = true,
                memoryUtilization = 0f,
                riskLevel = RiskLevel.SAFE,
                recommendation = "Memory requirement unknown - proceed with caution"
            )
        }
        
        val memoryUtilization = deviceInfo.getMemoryUtilization(modelMemoryRequirement)
        val isCompatible = deviceInfo.canRunModel(modelMemoryRequirement)
        
        val (riskLevel, recommendation, warnings) = when {
            memoryUtilization > 1.0f -> Triple(
                RiskLevel.DANGEROUS,
                "⚠️ DANGEROUS: This model requires ${String.format("%.1f", modelMemoryRequirement / (1024f * 1024f * 1024f))} GB but your device only has ${String.format("%.1f", deviceInfo.usableMemoryGB)} GB usable. Very likely to crash!",
                listOf(
                    "App will likely crash during model loading",
                    "Consider using the smaller 2B model instead",
                    "Loading may cause system instability"
                )
            )
            memoryUtilization > MEMORY_CRITICAL_THRESHOLD -> Triple(
                RiskLevel.CRITICAL,
                "⛔ CRITICAL: This model will use ${String.format("%.0f", memoryUtilization * 100)}% of your available memory. High crash risk!",
                listOf(
                    "Very high memory usage may cause crashes",
                    "Close other apps before proceeding",
                    "Consider using the 2B model for stability"
                )
            )
            memoryUtilization > MEMORY_WARNING_THRESHOLD -> Triple(
                RiskLevel.WARNING,
                "⚠️ WARNING: This model will use ${String.format("%.0f", memoryUtilization * 100)}% of your available memory.",
                listOf(
                    "High memory usage - close other apps first",
                    "Performance may be affected",
                    "Watch for app stability issues"
                )
            )
            else -> Triple(
                RiskLevel.SAFE,
                "✅ SAFE: This model will use ${String.format("%.0f", memoryUtilization * 100)}% of your available memory.",
                emptyList()
            )
        }
        
        Log.d(TAG, "Model compatibility assessment for '${model.name}':")
        Log.d(TAG, "  Memory requirement: ${String.format("%.1f", modelMemoryRequirement / (1024f * 1024f * 1024f))} GB")
        Log.d(TAG, "  Memory utilization: ${String.format("%.1f", memoryUtilization * 100)}%")
        Log.d(TAG, "  Risk level: $riskLevel")
        Log.d(TAG, "  Compatible: $isCompatible")
        Log.d(TAG, "  Recommendation: $recommendation")
        
        return ModelCompatibility(
            isCompatible = isCompatible,
            memoryUtilization = memoryUtilization,
            riskLevel = riskLevel,
            recommendation = recommendation,
            warnings = warnings
        )
    }

    /**
     * Get recommended model based on device capabilities.
     * Follows Gallery's approach but with Eliza-specific model selection.
     */
    fun getRecommendedModel(context: Context, availableModels: List<Model>): Model? {
        val deviceInfo = getDeviceMemoryInfo(context)
        
        Log.d(TAG, "Determining recommended model from ${availableModels.size} available models")
        
        // Sort models by memory requirement (smallest first)
        val sortedModels = availableModels.sortedBy { it.estimatedPeakMemoryInBytes ?: Long.MAX_VALUE }
        
        // Find the largest model that can safely run (under warning threshold)
        var recommendedModel: Model? = null
        
        for (model in sortedModels.reversed()) { // Check largest first
            val compatibility = assessModelCompatibility(context, model)
            
            if (compatibility.riskLevel == RiskLevel.SAFE || compatibility.riskLevel == RiskLevel.WARNING) {
                recommendedModel = model
                Log.d(TAG, "Recommended model: ${model.name} (${compatibility.riskLevel})")
                break
            }
        }
        
        // Fallback: if no model is safe, recommend the smallest one
        if (recommendedModel == null && sortedModels.isNotEmpty()) {
            recommendedModel = sortedModels.first()
            Log.w(TAG, "No safe model found, defaulting to smallest: ${recommendedModel.name}")
        }
        
        return recommendedModel
    }

    /**
     * Check if device is likely to crash with specific model.
     * Used for crash prevention logic.
     */
    fun isLikelyToCrash(context: Context, model: Model): Boolean {
        val compatibility = assessModelCompatibility(context, model)
        return compatibility.riskLevel == RiskLevel.DANGEROUS || 
               compatibility.riskLevel == RiskLevel.CRITICAL
    }
}