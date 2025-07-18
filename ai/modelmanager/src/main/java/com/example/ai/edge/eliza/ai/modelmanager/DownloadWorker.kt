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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ElizaDownloadWorker"
private const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "eliza_model_download_channel_foreground"
private var channelCreated = false

/**
 * WorkManager worker for downloading AI models.
 * Based exactly on Gallery's DownloadWorker implementation.
 */
@HiltWorker
@RequiresApi(Build.VERSION_CODES.O)
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val externalFilesDir = context.getExternalFilesDir(null)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId: Int = params.id.hashCode()

    init {
        if (!channelCreated) {
            // Create notification channel for model downloading progress
            val channel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "Model Downloading",
                NotificationManager.IMPORTANCE_LOW // Make it silent
            ).apply {
                description = "Notifications for AI model downloading"
            }
            notificationManager.createNotificationChannel(channel)
            channelCreated = true
        }
    }

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_MODEL_URL)
        val modelName = inputData.getString(KEY_MODEL_NAME) ?: "AI Model"
        val version = inputData.getString(KEY_MODEL_VERSION) ?: "latest"
        val fileName = inputData.getString(KEY_MODEL_DOWNLOAD_FILE_NAME)
        val modelDir = inputData.getString(KEY_MODEL_DOWNLOAD_MODEL_DIR) ?: "models"
        val totalBytes = inputData.getLong(KEY_MODEL_TOTAL_BYTES, 0L)
        val accessToken = inputData.getString(KEY_MODEL_DOWNLOAD_ACCESS_TOKEN)

        return withContext(Dispatchers.IO) {
            if (fileUrl == null || fileName == null) {
                Result.failure(
                    Data.Builder()
                        .putString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE, "Invalid download parameters")
                        .build()
                )
            } else {
                try {
                    // Set worker as foreground service immediately
                    setForeground(createForegroundInfo(progress = 0, modelName = modelName))

                    val url = URL(fileUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    
                    // Add access token if provided
                    if (accessToken != null) {
                        Log.d(TAG, "Using access token: ${accessToken.subSequence(0, 10)}...")
                        connection.setRequestProperty("Authorization", "Bearer $accessToken")
                    }

                    // Prepare output directory
                    val outputDir = File(
                        externalFilesDir,
                        listOf(modelDir, version).joinToString(separator = File.separator)
                    )
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    // Prepare output file and check for partial download
                    val outputFile = File(
                        outputDir,
                        fileName
                    )
                    val outputFileBytes = outputFile.length()
                    var downloadedBytes = 0L
                    
                    if (outputFileBytes > 0) {
                        Log.d(TAG, "File '$fileName' partial size: $outputFileBytes. Trying to resume download")
                        connection.setRequestProperty("Range", "bytes=$outputFileBytes-")
                        downloadedBytes = outputFileBytes
                    }

                    connection.connect()
                    Log.d(TAG, "Response code: ${connection.responseCode}")

                    if (connection.responseCode == HttpURLConnection.HTTP_OK ||
                        connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        
                        val contentRange = connection.getHeaderField("Content-Range")
                        if (contentRange != null) {
                            // Parse Content-Range header for resume support
                            val rangeParts = contentRange.substringAfter("bytes ").split("/")
                            val byteRange = rangeParts[0].split("-")
                            val startByte = byteRange[0].toLong()
                            Log.d(TAG, "Content-Range: $contentRange. Start bytes: $startByte")
                            downloadedBytes = startByte
                        } else {
                            Log.d(TAG, "Download starts from beginning")
                            downloadedBytes = 0L
                        }
                    } else {
                        throw IOException("HTTP error code: ${connection.responseCode}")
                    }

                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(outputFile, true /* append */)

                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    var lastSetProgressTs: Long = 0
                    var deltaBytes = 0L
                    val bytesReadSizeBuffer: MutableList<Long> = mutableListOf()
                    val bytesReadLatencyBuffer: MutableList<Long> = mutableListOf()

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        deltaBytes += bytesRead

                        // Report progress every 200ms
                        val curTs = System.currentTimeMillis()
                        if (curTs - lastSetProgressTs > 200) {
                            // Calculate download rate
                            var bytesPerMs = 0f
                            if (lastSetProgressTs != 0L) {
                                if (bytesReadSizeBuffer.size == 5) {
                                    bytesReadSizeBuffer.removeAt(0)
                                }
                                bytesReadSizeBuffer.add(deltaBytes)
                                if (bytesReadLatencyBuffer.size == 5) {
                                    bytesReadLatencyBuffer.removeAt(0)
                                }
                                bytesReadLatencyBuffer.add(curTs - lastSetProgressTs)
                                deltaBytes = 0L
                                bytesPerMs = bytesReadSizeBuffer.sum().toFloat() / bytesReadLatencyBuffer.sum()
                            }

                            // Calculate remaining time
                            var remainingMs = 0f
                            if (bytesPerMs > 0f && totalBytes > 0L) {
                                remainingMs = (totalBytes - downloadedBytes) / bytesPerMs
                            }

                            setProgress(
                                Data.Builder()
                                    .putLong(KEY_MODEL_DOWNLOAD_RECEIVED_BYTES, downloadedBytes)
                                    .putLong(KEY_MODEL_DOWNLOAD_RATE, (bytesPerMs * 1000).toLong())
                                    .putLong(KEY_MODEL_DOWNLOAD_REMAINING_MS, remainingMs.toLong())
                                    .build()
                            )
                            
                            val progressPercent = if (totalBytes > 0L) {
                                (downloadedBytes * 100 / totalBytes).toInt()
                            } else {
                                0
                            }
                            
                            setForeground(
                                createForegroundInfo(
                                    progress = progressPercent,
                                    modelName = modelName
                                )
                            )
                            
                            Log.d(TAG, "Downloaded bytes: $downloadedBytes/$totalBytes ($progressPercent%)")
                            lastSetProgressTs = curTs
                        }
                    }

                    outputStream.close()
                    inputStream.close()

                    Log.d(TAG, "Download completed successfully")
                    Result.success()
                    
                } catch (e: IOException) {
                    Log.e(TAG, "Download failed", e)
                    Result.failure(
                        Data.Builder()
                            .putString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE, e.message)
                            .build()
                    )
                }
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0)
    }

    /**
     * Creates a ForegroundInfo object for the download worker's ongoing notification.
     * Based on Gallery's implementation.
     */
    private fun createForegroundInfo(progress: Int, modelName: String? = null): ForegroundInfo {
        val title = if (modelName != null) {
            "Downloading \"$modelName\""
        } else {
            "Downloading AI model"
        }
        val content = "Download in progress: $progress%"

        val notification = NotificationCompat.Builder(applicationContext, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true) // Makes the notification non-dismissible
            .setProgress(100, progress, false)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
} 