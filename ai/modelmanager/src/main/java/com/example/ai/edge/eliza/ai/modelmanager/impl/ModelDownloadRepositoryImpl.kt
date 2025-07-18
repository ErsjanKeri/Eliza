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

package com.example.ai.edge.eliza.ai.modelmanager.impl

import android.util.Log
import com.example.ai.edge.eliza.ai.modelmanager.ModelDownloadRepository
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ModelDownloadRepositoryImpl"

/**
 * Implementation of ModelDownloadRepository.
 * Handles model downloading with progress tracking like Gallery's DownloadRepository.
 */
@Singleton
class ModelDownloadRepositoryImpl @Inject constructor() : ModelDownloadRepository {
    
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloads = mutableMapOf<String, Job>()
    
    override fun downloadModel(
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    ) {
        Log.d(TAG, "Starting download for model: ${model.name}")
        
        // Cancel any existing download for this model
        cancelDownloadModel(model)
        
        // Start new download job
        val downloadJob = downloadScope.launch {
            try {
                // TODO: Implement actual model download logic
                // For now, this is a mock implementation for development
                
                // Simulate download progress like Gallery does
                onProgress(ModelDownloadProgress(
                    progress = 0.0f,
                    status = ModelDownloadStatus.DOWNLOADING,
                    bytesDownloaded = 0L,
                    totalBytes = model.sizeInBytes,
                    downloadSpeed = 0L,
                    error = null
                ))
                
                // Simulate progressive download
                for (progress in 10..100 step 10) {
                    delay(200) // Simulate network delay
                    
                    val bytesDownloaded = (model.sizeInBytes * progress / 100)
                    onProgress(ModelDownloadProgress(
                        progress = progress / 100f,
                        status = ModelDownloadStatus.DOWNLOADING,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = model.sizeInBytes,
                        downloadSpeed = 1024L * 1024L, // 1MB/s
                        error = null
                    ))
                }
                
                // Complete download
                onProgress(ModelDownloadProgress(
                    progress = 1.0f,
                    status = ModelDownloadStatus.COMPLETED,
                    bytesDownloaded = model.sizeInBytes,
                    totalBytes = model.sizeInBytes,
                    downloadSpeed = 0L,
                    error = null
                ))
                
                Log.d(TAG, "Download completed for model: ${model.name}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for model: ${model.name}", e)
                onProgress(ModelDownloadProgress(
                    progress = 0f,
                    status = ModelDownloadStatus.FAILED,
                    bytesDownloaded = 0L,
                    totalBytes = model.sizeInBytes,
                    downloadSpeed = 0L,
                    error = e.message
                ))
            } finally {
                // Clean up job from active downloads
                activeDownloads.remove(model.name)
            }
        }
        
        // Track the download job
        activeDownloads[model.name] = downloadJob
    }
    
    override fun cancelDownloadModel(model: Model) {
        Log.d(TAG, "Cancelling download for model: ${model.name}")
        
        activeDownloads[model.name]?.let { job ->
            job.cancel()
            activeDownloads.remove(model.name)
            Log.d(TAG, "Download cancelled for model: ${model.name}")
        }
    }
} 