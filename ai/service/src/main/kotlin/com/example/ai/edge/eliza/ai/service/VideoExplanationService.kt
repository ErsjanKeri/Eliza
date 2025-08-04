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

package com.example.ai.edge.eliza.ai.service

import android.content.Context
import android.util.Log
import com.example.ai.edge.eliza.core.common.VideoThumbnailGenerator
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.VideoErrorInfo
import com.example.ai.edge.eliza.core.model.VideoErrorType
import com.example.ai.edge.eliza.core.model.VideoExplanationStatus
import com.example.ai.edge.eliza.core.model.VideoExplanationStatusType
import com.example.ai.edge.eliza.core.network.VideoService
import com.example.ai.edge.eliza.core.network.model.VideoRequest
import com.example.ai.edge.eliza.core.network.model.VideoResponse
import com.example.ai.edge.eliza.core.network.model.VideoStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VideoExplanationService"

/**
 * Service interface for managing video explanation requests.
 * Follows the same pattern as ModelDownloadRepository for consistency.
 */
interface VideoExplanationService {
    
    /**
     * Request a video explanation for the given user question and context.
     * 
     * @param userQuestion The user's question/prompt
     * @param context The educational context (chapter or exercise)
     * @param onStatusUpdated Callback for status updates during the lifecycle
     */
    fun requestVideoExplanation(
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit
    )
    
    /**
     * Cancel an ongoing video request.
     * 
     * @param videoId The ID of the video request to cancel
     */
    fun cancelVideoRequest(videoId: String)
    
    /**
     * Get the local file path for a completed video.
     * 
     * @param videoId The ID of the video
     * @return Local file path if video exists, null otherwise
     */
    fun getLocalVideoPath(videoId: String): String?
}

/**
 * Implementation of VideoExplanationService.
 * Follows the same pattern as ModelDownloadRepositoryImpl for consistency.
 */
@Singleton
class VideoExplanationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoService: VideoService
) : VideoExplanationService {
    
    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Storage for active video requests
    private val activeRequests = mutableMapOf<String, VideoExplanationStatus>()
    
    // Videos directory for local storage
    private val videosDir: File by lazy {
        File(context.filesDir, "videos").apply {
            if (!exists()) mkdirs()
        }
    }
    
    override fun requestVideoExplanation(
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit
    ) {
        Log.d(TAG, "Starting video explanation request for: $userQuestion")
        
        serviceScope.launch {
            requestVideoWithRetry(userQuestion, context, onStatusUpdated, retryCount = 0)
        }
    }
    
    /**
     * Internal method that handles video requests with intelligent retry logic.
     * Preserves all functionality while adding comprehensive error handling.
     */
    private suspend fun requestVideoWithRetry(
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int = 0
    ) {
        try {
            // Step 1: Create initial status
            val initialStatus = VideoExplanationStatus(
                status = VideoExplanationStatusType.QUEUED,
                currentMessage = if (retryCount > 0) "Retrying video request..." else "Preparing video request...",
                progress = 0,
                retryCount = retryCount
            )
            onStatusUpdated(initialStatus)
            
            // Step 2: Validate input and generate prompt
            if (userQuestion.trim().isEmpty()) {
                val errorInfo = VideoErrorInfo(
                    type = VideoErrorType.INVALID_PROMPT,
                    message = "Please provide a question for the video explanation.",
                    isRetryable = false,
                    suggestedAction = "Enter a question"
                )
                onStatusUpdated(initialStatus.withError(errorInfo))
                return
            }
            
            val prompt = generatePromptFromContext(userQuestion, context)
            Log.d(TAG, "Generated prompt: ${prompt.take(100)}...")
            
            // Step 3: Validate prompt length
            if (prompt.length > 4000) { // Reasonable limit for most APIs
                val errorInfo = VideoErrorInfo(
                    type = VideoErrorType.PROMPT_TOO_LONG,
                    message = "Your request is too long. Please try a shorter question.",
                    isRetryable = false,
                    suggestedAction = "Use a shorter question"
                )
                onStatusUpdated(initialStatus.withError(errorInfo))
                return
            }
            
            // Step 4: Send request to ElizaServer with error handling
            val videoRequest = VideoRequest(
                prompt = prompt,
                durationLimit = 60 // Default 60 seconds
            )
            
            val response = try {
                videoService.createVideo(videoRequest)
            } catch (e: HttpException) {
                handleHttpError(e, initialStatus, userQuestion, context, onStatusUpdated, retryCount)
                return
            } catch (e: java.net.SocketTimeoutException) {
                handleNetworkError(e, initialStatus, userQuestion, context, onStatusUpdated, retryCount)
                return
            } catch (e: java.net.UnknownHostException) {
                handleNetworkError(e, initialStatus, userQuestion, context, onStatusUpdated, retryCount)
                return
            } catch (e: java.io.IOException) {
                handleNetworkError(e, initialStatus, userQuestion, context, onStatusUpdated, retryCount)
                return
            }
            
            val videoId = response.videoId
            Log.d(TAG, "Video request created with ID: $videoId")
            
            // Step 5: Update status with video ID
            val queuedStatus = initialStatus.copy(
                status = VideoExplanationStatusType.QUEUED,
                videoId = videoId,
                currentMessage = "Video queued for generation...",
                progress = 10
            )
            activeRequests[videoId] = queuedStatus
            onStatusUpdated(queuedStatus)
            
            // Step 6: Start polling for status updates
            pollVideoStatusWithRetry(videoId, onStatusUpdated)
            
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error requesting video explanation", e)
            val errorInfo = VideoErrorInfo(
                type = VideoErrorType.UNKNOWN_ERROR,
                message = "An unexpected error occurred. Please try again.",
                technicalDetails = e.message,
                isRetryable = true,
                suggestedAction = "Try again",
                retryDelayMs = 3000L,
                maxRetries = 2
            )
            
            val failedStatus = VideoExplanationStatus(
                status = VideoExplanationStatusType.FAILED,
                retryCount = retryCount
            ).withError(errorInfo)
            
            handleRetryOrFail(failedStatus, userQuestion, context, onStatusUpdated)
        }
    }
    
    /**
     * Handle HTTP errors with intelligent classification and retry logic.
     */
    private suspend fun handleHttpError(
        exception: HttpException,
        currentStatus: VideoExplanationStatus,
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int
    ) {
        val responseBody = try {
            exception.response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        
        val errorInfo = VideoErrorInfo.fromHttpError(exception.code(), responseBody)
        val failedStatus = currentStatus.withError(errorInfo)
        
        Log.w(TAG, "HTTP error ${exception.code()}: ${errorInfo.message}")
        handleRetryOrFail(failedStatus, userQuestion, context, onStatusUpdated)
    }
    
    /**
     * Handle network errors with intelligent classification and retry logic.
     */
    private suspend fun handleNetworkError(
        exception: Exception,
        currentStatus: VideoExplanationStatus,
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int
    ) {
        val errorInfo = VideoErrorInfo.fromNetworkError(exception)
        val failedStatus = currentStatus.withError(errorInfo)
        
        Log.w(TAG, "Network error: ${errorInfo.message}")
        handleRetryOrFail(failedStatus, userQuestion, context, onStatusUpdated)
    }
    
    /**
     * Decide whether to retry or fail based on error info and retry count.
     */
    private suspend fun handleRetryOrFail(
        failedStatus: VideoExplanationStatus,
        userQuestion: String,
        context: ChatContext?,
        onStatusUpdated: (VideoExplanationStatus) -> Unit
    ) {
        val errorInfo = failedStatus.errorInfo
        if (failedStatus.canRetry && errorInfo != null) {
            onStatusUpdated(failedStatus.copy(
                currentMessage = "Retrying in ${errorInfo.retryDelayMs / 1000} seconds..."
            ))
            
            delay(errorInfo.retryDelayMs)
            
            requestVideoWithRetry(
                userQuestion = userQuestion,
                context = context,
                onStatusUpdated = onStatusUpdated,
                retryCount = failedStatus.retryCount + 1
            )
        } else {
            // Final failure - no more retries
            onStatusUpdated(failedStatus)
        }
    }
    
    override fun cancelVideoRequest(videoId: String) {
        Log.d(TAG, "Canceling video request: $videoId")
        activeRequests.remove(videoId)
        // Note: ElizaServer doesn't have a cancel endpoint, so we just stop polling
    }
    
    override fun getLocalVideoPath(videoId: String): String? {
        val videoFile = File(videosDir, "$videoId.mp4")
        return if (videoFile.exists()) videoFile.absolutePath else null
    }
    
    /**
     * Generate appropriate prompt based on context type.
     * Reuses existing VideoPromptTemplates for consistency.
     */
    private fun generatePromptFromContext(userQuestion: String, context: ChatContext?): String {
        return when (context) {
            is ChatContext.ChapterReading -> {
                VideoPromptTemplates.createChapterVideoPrompt(userQuestion, context)
            }
            is ChatContext.ExerciseSolving -> {
                VideoPromptTemplates.createExerciseVideoPrompt(userQuestion, context)
            }
            else -> {
                VideoPromptTemplates.createGeneralVideoPrompt(userQuestion)
            }
        }
    }
    
    /**
     * Poll video status every 3 seconds until completion with retry logic.
     * Follows the same pattern as ModelDownloadRepository progress updates.
     */
    private suspend fun pollVideoStatusWithRetry(
        videoId: String,
        onStatusUpdated: (VideoExplanationStatus) -> Unit
    ) {
        Log.d(TAG, "Starting status polling for video: $videoId")
        
        var consecutiveErrors = 0
        val maxConsecutiveErrors = 3
        
        try {
            while (activeRequests.containsKey(videoId)) {
                delay(3000) // Poll every 3 seconds
                
                try {
                    val statusResponse = videoService.getVideoStatus(videoId)
                    Log.d(TAG, "Video $videoId status: ${statusResponse.status}")
                    
                    // Reset error count on successful request
                    consecutiveErrors = 0
                    
                    val currentStatus = activeRequests[videoId] ?: return
                    val updatedStatus = mapServerStatusToLocal(statusResponse, currentStatus)
                    
                    activeRequests[videoId] = updatedStatus
                    onStatusUpdated(updatedStatus)
                    
                    // Check if completed or failed
                    when (statusResponse.status) {
                        VideoStatus.COMPLETED -> {
                            downloadCompletedVideoWithRetry(videoId, onStatusUpdated)
                            return
                        }
                        VideoStatus.FAILED -> {
                            val errorInfo = VideoErrorInfo.fromGenerationFailure(statusResponse.message)
                            val failedStatus = currentStatus.withError(errorInfo)
                            onStatusUpdated(failedStatus)
                            activeRequests.remove(videoId)
                            return
                        }
                        else -> {
                            // Continue polling
                        }
                    }
                    
                } catch (e: Exception) {
                    consecutiveErrors++
                    Log.w(TAG, "Error polling video status (attempt $consecutiveErrors/$maxConsecutiveErrors): ${e.message}")
                    
                    if (consecutiveErrors >= maxConsecutiveErrors) {
                        // Too many consecutive errors - fail permanently
                        val errorInfo = when (e) {
                            is HttpException -> VideoErrorInfo.fromHttpError(e.code(), null)
                            is java.net.SocketTimeoutException, 
                            is java.net.UnknownHostException,
                            is java.io.IOException -> VideoErrorInfo.fromNetworkError(e)
                            else -> VideoErrorInfo(
                                type = VideoErrorType.UNKNOWN_ERROR,
                                message = "Lost connection to video service. Please try again.",
                                technicalDetails = e.message,
                                isRetryable = true,
                                suggestedAction = "Try again",
                                retryDelayMs = 5000L,
                                maxRetries = 1
                            )
                        }
                        
                        val currentStatus = activeRequests[videoId] ?: VideoExplanationStatus(
                            status = VideoExplanationStatusType.FAILED,
                            videoId = videoId
                        )
                        
                        onStatusUpdated(currentStatus.withError(errorInfo))
                        activeRequests.remove(videoId)
                        return
                    }
                    
                    // For temporary errors, continue polling but with backoff
                    delay(5000) // Longer delay after error
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in video polling for $videoId", e)
            val currentStatus = activeRequests[videoId] ?: VideoExplanationStatus(
                status = VideoExplanationStatusType.FAILED,
                videoId = videoId
            )
            
            val errorInfo = VideoErrorInfo(
                type = VideoErrorType.UNKNOWN_ERROR,
                message = "Video monitoring failed. Please try again.",
                technicalDetails = e.message,
                isRetryable = true,
                suggestedAction = "Try again",
                retryDelayMs = 3000L,
                maxRetries = 1
            )
            
            onStatusUpdated(currentStatus.withError(errorInfo))
            activeRequests.remove(videoId)
        }
    }
    
    /**
     * Map server video status to local status.
     */
    private fun mapServerStatusToLocal(
        serverResponse: VideoResponse,
        currentStatus: VideoExplanationStatus
    ): VideoExplanationStatus {
        return when (serverResponse.status) {
            VideoStatus.QUEUED -> currentStatus.copy(
                status = VideoExplanationStatusType.QUEUED,
                currentMessage = serverResponse.message,
                progress = 15
            )
            VideoStatus.GENERATING_SCRIPT -> currentStatus.copy(
                status = VideoExplanationStatusType.GENERATING_SCRIPT,
                currentMessage = serverResponse.message,
                progress = serverResponse.progress ?: 30
            )
            VideoStatus.RENDERING_VIDEO -> currentStatus.copy(
                status = VideoExplanationStatusType.RENDERING_VIDEO,
                currentMessage = serverResponse.message,
                progress = serverResponse.progress ?: 70
            )
            VideoStatus.COMPLETED -> currentStatus.copy(
                status = VideoExplanationStatusType.DOWNLOADING,
                currentMessage = "Video ready, downloading...",
                progress = 90
            )
            VideoStatus.FAILED -> currentStatus.copy(
                status = VideoExplanationStatusType.FAILED,
                currentMessage = serverResponse.message,
                errorMessage = serverResponse.message
            )
        }
    }
    
    /**
     * Download completed video to local storage with comprehensive error handling and retry logic.
     * Follows the same pattern as ModelDownloadRepository file handling.
     */
    private suspend fun downloadCompletedVideoWithRetry(
        videoId: String,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int = 0
    ) {
        Log.d(TAG, "Downloading completed video: $videoId (attempt ${retryCount + 1})")
        
        val currentStatus = activeRequests[videoId] ?: return
        
        try {
            // Update status to downloading
            val downloadingStatus = currentStatus.copy(
                status = VideoExplanationStatusType.DOWNLOADING,
                currentMessage = if (retryCount > 0) "Retrying video download..." else "Downloading video file...",
                progress = 95,
                retryCount = retryCount
            )
            activeRequests[videoId] = downloadingStatus
            onStatusUpdated(downloadingStatus)
            
            // Check available storage space
            val availableSpace = videosDir.freeSpace
            if (availableSpace < 10_000_000) { // 10MB minimum
                val errorInfo = VideoErrorInfo(
                    type = VideoErrorType.STORAGE_FULL,
                    message = "Not enough storage space to download the video. Please free up some space and try again.",
                    isRetryable = true,
                    suggestedAction = "Free up storage space",
                    maxRetries = 1
                )
                onStatusUpdated(currentStatus.withError(errorInfo))
                activeRequests.remove(videoId)
                return
            }
            
            // Download video file with error handling
            val response: Response<ResponseBody> = try {
                videoService.downloadVideo(videoId)
            } catch (e: HttpException) {
                handleDownloadHttpError(e, currentStatus, videoId, onStatusUpdated, retryCount)
                return
            } catch (e: java.net.SocketTimeoutException) {
                handleDownloadNetworkError(e, currentStatus, videoId, onStatusUpdated, retryCount)
                return
            } catch (e: java.io.IOException) {
                handleDownloadNetworkError(e, currentStatus, videoId, onStatusUpdated, retryCount)
                return
            }
            
            if (!response.isSuccessful) {
                val errorInfo = VideoErrorInfo.fromHttpError(response.code(), null)
                val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
                handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
                return
            }
            
            val responseBody = response.body()
            if (responseBody == null) {
                val errorInfo = VideoErrorInfo(
                    type = VideoErrorType.DOWNLOAD_CORRUPTED,
                    message = "Download failed: no video data received. Please try again.",
                    isRetryable = true,
                    suggestedAction = "Try again",
                    retryDelayMs = 3000L,
                    maxRetries = 2
                )
                val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
                handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
                return
            }
            
            val videoFile = File(videosDir, "$videoId.mp4")
            
            // Save to local file with error handling
            try {
                saveVideoToFile(responseBody.byteStream(), videoFile)
            } catch (e: java.io.IOException) {
                val errorInfo = VideoErrorInfo.fromStorageError(e)
                val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
                handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
                return
            }
            
            // Validate downloaded file
            if (!videoFile.exists() || videoFile.length() == 0L) {
                val errorInfo = VideoErrorInfo(
                    type = VideoErrorType.DOWNLOAD_CORRUPTED,
                    message = "Downloaded video file is corrupted. Please try again.",
                    isRetryable = true,
                    suggestedAction = "Try again",
                    retryDelayMs = 2000L,
                    maxRetries = 2
                )
                val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
                handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
                return
            }
            
            Log.d(TAG, "Video downloaded successfully: ${videoFile.absolutePath} (${videoFile.length()} bytes)")
            
            // Generate thumbnail and get actual duration (non-blocking for main flow)
            val actualDuration = try {
                val durationMs = VideoThumbnailGenerator.getVideoDuration(videoFile.absolutePath)
                (durationMs / 1000).toInt() // Convert to seconds
            } catch (e: Exception) {
                Log.w(TAG, "Could not extract video duration: ${e.message}")
                60 // Default fallback - don't fail download for this
            }
            
            // Generate and cache thumbnail (non-blocking for main flow)
            try {
                VideoThumbnailGenerator.generateAndCacheThumbnail(
                    context = context,
                    videoFilePath = videoFile.absolutePath,
                    videoId = videoId
                )
                Log.d(TAG, "Video thumbnail generated for: $videoId")
            } catch (e: Exception) {
                Log.w(TAG, "Could not generate video thumbnail: ${e.message}")
                // Don't fail download for thumbnail generation failure
            }
            
            // Update final status
            val completedStatus = currentStatus.copy(
                status = VideoExplanationStatusType.COMPLETED,
                currentMessage = "Video ready for playback",
                progress = 100,
                localFilePath = videoFile.absolutePath,
                fileSizeBytes = videoFile.length(),
                durationSeconds = actualDuration
            )
            
            activeRequests.remove(videoId) // Remove from active requests
            onStatusUpdated(completedStatus)
            
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error downloading video $videoId", e)
            val errorInfo = VideoErrorInfo(
                type = VideoErrorType.DOWNLOAD_FAILED,
                message = "An unexpected error occurred during download. Please try again.",
                technicalDetails = e.message,
                isRetryable = true,
                suggestedAction = "Try again",
                retryDelayMs = 3000L,
                maxRetries = 2
            )
            
            val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
            handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
        }
    }
    
    /**
     * Handle HTTP errors during video download.
     */
    private suspend fun handleDownloadHttpError(
        exception: HttpException,
        currentStatus: VideoExplanationStatus,
        videoId: String,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int
    ) {
        val errorInfo = VideoErrorInfo.fromHttpError(exception.code(), null)
        val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
        Log.w(TAG, "Download HTTP error ${exception.code()}: ${errorInfo.message}")
        handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
    }
    
    /**
     * Handle network errors during video download.
     */
    private suspend fun handleDownloadNetworkError(
        exception: Exception,
        currentStatus: VideoExplanationStatus,
        videoId: String,
        onStatusUpdated: (VideoExplanationStatus) -> Unit,
        retryCount: Int
    ) {
        val errorInfo = VideoErrorInfo.fromNetworkError(exception)
        val failedStatus = currentStatus.copy(retryCount = retryCount).withError(errorInfo)
        Log.w(TAG, "Download network error: ${errorInfo.message}")
        handleDownloadRetryOrFail(failedStatus, videoId, onStatusUpdated)
    }
    
    /**
     * Decide whether to retry download or fail based on error info and retry count.
     */
    private suspend fun handleDownloadRetryOrFail(
        failedStatus: VideoExplanationStatus,
        videoId: String,
        onStatusUpdated: (VideoExplanationStatus) -> Unit
    ) {
        val errorInfo = failedStatus.errorInfo
        if (failedStatus.canRetry && errorInfo != null) {
            onStatusUpdated(failedStatus.copy(
                currentMessage = "Download failed. Retrying in ${errorInfo.retryDelayMs / 1000} seconds..."
            ))
            
            delay(errorInfo.retryDelayMs)
            
            downloadCompletedVideoWithRetry(
                videoId = videoId,
                onStatusUpdated = onStatusUpdated,
                retryCount = failedStatus.retryCount + 1
            )
        } else {
            // Final failure - no more retries
            onStatusUpdated(failedStatus)
            activeRequests.remove(videoId)
        }
    }
    
    /**
     * Save video data to local file.
     */
    private fun saveVideoToFile(inputStream: InputStream, file: File) {
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    
    /**
     * Clean up resources when service is destroyed.
     */
    fun cleanup() {
        serviceScope.cancel()
        activeRequests.clear()
    }
}