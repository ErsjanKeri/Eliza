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

package com.example.ai.edge.eliza.ai.modelmanager.download

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.Model
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ModelDownloadRepositoryImpl"

/**
 * Repository interface for model downloading.
 */
interface ModelDownloadRepository {
    fun downloadModel(
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    )

    fun cancelDownloadModel(model: Model)
    
    fun observeWorkerProgress(
        workerId: UUID,
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    )
}

/**
 * Implementation of ModelDownloadRepository using WorkManager.
 * Based exactly on Gallery's DefaultDownloadRepository implementation.
 */
@Singleton
class ModelDownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ModelDownloadRepository {
    
    private val workManager = WorkManager.getInstance(context)
    
    override fun downloadModel(
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    ) {
        Log.d(TAG, "Starting WorkManager download for model: ${model.name}")
        
        // Create input data exactly like Gallery's pattern
        val inputDataBuilder = Data.Builder()
            .putString(KEY_MODEL_NAME, model.name)
            .putString(KEY_MODEL_URL, model.url)
            .putString(KEY_MODEL_VERSION, model.version)
            .putString(KEY_MODEL_DOWNLOAD_MODEL_DIR, model.normalizedName)
            .putString(KEY_MODEL_DOWNLOAD_FILE_NAME, model.downloadFileName)
            .putLong(KEY_MODEL_TOTAL_BYTES, model.sizeInBytes)
            
        // Add checksum if available
        if (model.sha256Checksum != null) {
            inputDataBuilder.putString(KEY_MODEL_SHA256_CHECKSUM, model.sha256Checksum)
        }
        
        val inputData = inputDataBuilder.build()
        
        // Create worker request exactly like Gallery's pattern
        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(inputData)
            .addTag("$MODEL_NAME_TAG:${model.name}")
            .build()
        
        val workerId = downloadWorkRequest.id
        
        // Start WorkManager job - replace any existing download for this model
        workManager.enqueueUniqueWork(
            model.name,
            ExistingWorkPolicy.REPLACE,
            downloadWorkRequest
        )
        
        // Observe progress exactly like Gallery's pattern
        observeWorkerProgress(workerId = workerId, model = model, onProgress = onProgress)
    }
    
    override fun cancelDownloadModel(model: Model) {
        Log.d(TAG, "Cancelling WorkManager download for model: ${model.name}")
        workManager.cancelAllWorkByTag("$MODEL_NAME_TAG:${model.name}")
    }
    
    override fun observeWorkerProgress(
        workerId: UUID,
        model: Model,
        onProgress: (ModelDownloadProgress) -> Unit
    ) {
        workManager.getWorkInfoByIdLiveData(workerId).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        Log.d(TAG, "Model '${model.name}' download enqueued")
                        onProgress(ModelDownloadProgress(
                            progress = 0.0f,
                            status = ModelDownloadStatus.DOWNLOADING,
                            bytesDownloaded = 0L,
                            totalBytes = model.sizeInBytes,
                            downloadSpeed = 0L,
                            error = null
                        ))
                    }
                    
                    WorkInfo.State.RUNNING -> {
                        val receivedBytes = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RECEIVED_BYTES, 0L)
                        val downloadRate = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RATE, 0L)
                        val remainingMs = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_REMAINING_MS, 0L)
                        
                        if (receivedBytes > 0L) {
                            val progress = if (model.sizeInBytes > 0L) {
                                (receivedBytes.toFloat() / model.sizeInBytes.toFloat())
                            } else {
                                0f
                            }
                            
                            onProgress(ModelDownloadProgress(
                                progress = progress,
                                status = ModelDownloadStatus.DOWNLOADING,
                                bytesDownloaded = receivedBytes,
                                totalBytes = model.sizeInBytes,
                                downloadSpeed = downloadRate,
                                error = null
                            ))
                        }
                    }
                    
                    WorkInfo.State.SUCCEEDED -> {
                        Log.d(TAG, "Model '${model.name}' download succeeded")
                        onProgress(ModelDownloadProgress(
                            progress = 1.0f,
                            status = ModelDownloadStatus.COMPLETED,
                            bytesDownloaded = model.sizeInBytes,
                            totalBytes = model.sizeInBytes,
                            downloadSpeed = 0L,
                            error = null
                        ))
                    }
                    
                    WorkInfo.State.FAILED -> {
                        val errorMessage = workInfo.outputData.getString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE)
                            ?: "Download failed"
                        Log.e(TAG, "Model '${model.name}' download failed: $errorMessage")
                        onProgress(ModelDownloadProgress(
                            progress = 0f,
                            status = ModelDownloadStatus.FAILED,
                            bytesDownloaded = 0L,
                            totalBytes = model.sizeInBytes,
                            downloadSpeed = 0L,
                            error = errorMessage
                        ))
                    }
                    
                    WorkInfo.State.CANCELLED -> {
                        Log.d(TAG, "Model '${model.name}' download cancelled")
                        onProgress(ModelDownloadProgress(
                            progress = 0f,
                            status = ModelDownloadStatus.PENDING,
                            bytesDownloaded = 0L,
                            totalBytes = model.sizeInBytes,
                            downloadSpeed = 0L,
                            error = null
                        ))
                    }
                    
                    else -> {
                        // Do nothing for BLOCKED state
                    }
                }
            }
        }
    }
} 