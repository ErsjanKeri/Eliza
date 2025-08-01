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
import androidx.core.content.edit
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.example.ai.edge.eliza.ai.modelmanager.data.Model
import com.example.ai.edge.eliza.ai.modelmanager.data.ModelDownloadStatus
import com.example.ai.edge.eliza.ai.modelmanager.data.ModelDownloadStatusType
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_ERROR_MESSAGE
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_FILE_NAME
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_MODEL_DIR
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_RATE
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_RECEIVED_BYTES
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_DOWNLOAD_REMAINING_MS
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_NAME
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_SHA256_CHECKSUM
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_START_UNZIPPING
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_TOTAL_BYTES
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_URL
import com.example.ai.edge.eliza.ai.modelmanager.download.KEY_MODEL_VERSION
import com.example.ai.edge.eliza.ai.modelmanager.download.MODEL_NAME_TAG
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ModelDownloadRepositoryImpl"

/** Gallery's exact work info data structure */
data class ElizaWorkInfo(val modelName: String, val workId: String)

/**
 * Repository interface for model downloading.
 * Matches Gallery's DownloadRepository interface exactly.
 */
interface ModelDownloadRepository {
    fun downloadModel(
        model: Model,
        onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit,
    )

    fun cancelDownloadModel(model: Model)
    
    fun cancelAll(models: List<Model>, onComplete: () -> Unit)
    
    fun observeWorkerProgress(
        workerId: UUID,
        model: Model,
        onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit,
    )
    
    fun getEnqueuedOrRunningWorkInfos(): List<ElizaWorkInfo>
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
    
    /**
     * Gallery's exact SharedPreferences pattern for tracking download start times
     */
    private val downloadStartTimeSharedPreferences =
        context.getSharedPreferences("download_start_time_ms", Context.MODE_PRIVATE)
    
    override fun downloadModel(
        model: Model,
        onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit,
    ) {
        Log.d(TAG, "Starting WorkManager download for model: ${model.name}")
        
        // Create input data exactly like Gallery's pattern
        val inputDataBuilder = Data.Builder()
            .putString(KEY_MODEL_NAME, model.name)
            .putString(KEY_MODEL_URL, model.downloadUrl)
            .putString(KEY_MODEL_VERSION, model.version)
            .putString(KEY_MODEL_DOWNLOAD_MODEL_DIR, model.normalizedName)
            .putString(KEY_MODEL_DOWNLOAD_FILE_NAME, model.downloadFileName)
            .putLong(KEY_MODEL_TOTAL_BYTES, model.sizeInBytes)
            
        // Note: sha256Checksum not available in Gallery Model, but we keep the logic for future use
        
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
        observeWorkerProgress(workerId = workerId, model = model, onStatusUpdated = onStatusUpdated)
    }
    
    override fun cancelDownloadModel(model: Model) {
        Log.d(TAG, "Cancelling WorkManager download for model: ${model.name}")
        workManager.cancelAllWorkByTag("$MODEL_NAME_TAG:${model.name}")
    }
    
    override fun cancelAll(models: List<Model>, onComplete: () -> Unit) {
        if (models.isEmpty()) {
            onComplete()
            return
        }

        val futures = mutableListOf<ListenableFuture<Operation.State.SUCCESS>>()
        for (tag in models.map { "$MODEL_NAME_TAG:${it.name}" }) {
            futures.add(workManager.cancelAllWorkByTag(tag).result)
        }
        val combinedFuture: ListenableFuture<List<Operation.State.SUCCESS>> = Futures.allAsList(futures)
        Futures.addCallback(
            combinedFuture,
            object : FutureCallback<List<Operation.State.SUCCESS>> {
                override fun onSuccess(result: List<Operation.State.SUCCESS>?) {
                    // All cancellations are complete
                    onComplete()
                }

                override fun onFailure(t: Throwable) {
                    // At least one cancellation failed
                    t.printStackTrace()
                    onComplete()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }
    
    override fun observeWorkerProgress(
        workerId: UUID,
        model: Model,
        onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit,
    ) {
        workManager.getWorkInfoByIdLiveData(workerId).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        // Gallery's exact pattern: save download start time
                        downloadStartTimeSharedPreferences.edit {
                            putLong(model.name, System.currentTimeMillis())
                        }
                        Log.d(TAG, "Model '${model.name}' download enqueued")
                        onStatusUpdated(
                            model,
                            ModelDownloadStatus(
                                status = ModelDownloadStatusType.IN_PROGRESS,
                                totalBytes = model.sizeInBytes,
                                receivedBytes = 0L,
                                bytesPerSecond = 0L,
                                remainingMs = 0L
                            )
                        )
                    }
                    
                    WorkInfo.State.RUNNING -> {
                        val receivedBytes = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RECEIVED_BYTES, 0L)
                        val downloadRate = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RATE, 0L)
                        val remainingMs = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_REMAINING_MS, 0L)
                        val startUnzipping = workInfo.progress.getBoolean(KEY_MODEL_START_UNZIPPING, false)
                        
                        if (!startUnzipping) {
                            if (receivedBytes > 0L) {
                                onStatusUpdated(
                                    model,
                                    ModelDownloadStatus(
                                        status = ModelDownloadStatusType.IN_PROGRESS,
                                        totalBytes = model.sizeInBytes,
                                        receivedBytes = receivedBytes,
                                        bytesPerSecond = downloadRate,
                                        remainingMs = remainingMs
                                    )
                                )
                            }
                        } else {
                            onStatusUpdated(
                                model,
                                ModelDownloadStatus(status = ModelDownloadStatusType.UNZIPPING)
                            )
                        }
                    }
                    
                    WorkInfo.State.SUCCEEDED -> {
                        Log.d(TAG, "Model '${model.name}' download succeeded")
                        onStatusUpdated(
                            model,
                            ModelDownloadStatus(
                                status = ModelDownloadStatusType.SUCCEEDED,
                                totalBytes = model.sizeInBytes,
                                receivedBytes = model.sizeInBytes,
                                bytesPerSecond = 0L,
                                remainingMs = 0L
                            )
                        )
                        
                        // Gallery's exact pattern: log duration and cleanup start time
                        val startTime = downloadStartTimeSharedPreferences.getLong(model.name, 0L)
                        val duration = System.currentTimeMillis() - startTime
                        Log.d(TAG, "Download completed in ${duration}ms")
                        downloadStartTimeSharedPreferences.edit { remove(model.name) }
                    }
                    
                    WorkInfo.State.FAILED -> {
                        val errorMessage = workInfo.outputData.getString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE)
                            ?: "Download failed"
                        Log.e(TAG, "Model '${model.name}' download failed: $errorMessage")
                        onStatusUpdated(
                            model,
                            ModelDownloadStatus(
                                status = ModelDownloadStatusType.FAILED,
                                totalBytes = model.sizeInBytes,
                                receivedBytes = 0L,
                                errorMessage = errorMessage,
                                bytesPerSecond = 0L,
                                remainingMs = 0L
                            )
                        )
                        
                        // Gallery's exact pattern: cleanup start time
                        downloadStartTimeSharedPreferences.edit { remove(model.name) }
                    }
                    
                    WorkInfo.State.CANCELLED -> {
                        Log.d(TAG, "Model '${model.name}' download cancelled")
                        // Gallery's exact pattern: cancelled becomes NOT_DOWNLOADED, not FAILED
                        onStatusUpdated(
                            model,
                            ModelDownloadStatus(
                                status = ModelDownloadStatusType.NOT_DOWNLOADED,
                                totalBytes = model.sizeInBytes,
                                receivedBytes = 0L,
                                errorMessage = "",
                                bytesPerSecond = 0L,
                                remainingMs = 0L
                            )
                        )
                        
                        // Gallery's exact pattern: cleanup start time
                        downloadStartTimeSharedPreferences.edit { remove(model.name) }
                    }
                    
                    else -> {
                        // Do nothing for BLOCKED state
                    }
                }
            }
        }
    }
    
    /**
     * Gallery's exact method for getting enqueued or running work infos
     */
    override fun getEnqueuedOrRunningWorkInfos(): List<ElizaWorkInfo> {
        val workQuery =
            WorkQuery.Builder.fromStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)).build()

        return workManager.getWorkInfos(workQuery).get().map { info ->
            val tags = info.tags
            val modelName = tags.find { it.startsWith("$MODEL_NAME_TAG:") }
                ?.substring("$MODEL_NAME_TAG:".length) ?: "Unknown"
            ElizaWorkInfo(modelName = modelName, workId = info.id.toString())
        }
    }
} 