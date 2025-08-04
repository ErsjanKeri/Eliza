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

package com.example.ai.edge.eliza.core.common

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for generating video thumbnails from MP4 files.
 * 
 * This class provides functionality to extract the first frame from a video file
 * and create a thumbnail image, following the existing patterns in the app.
 */
object VideoThumbnailGenerator {

    /**
     * Generate a thumbnail bitmap from a video file.
     * 
     * @param videoFilePath The path to the video file (local file path)
     * @param timeUs The time position in the video to extract the frame from (in microseconds).
     *               Default is 1000000 (1 second) to avoid potential black frames at the start.
     * @return Bitmap or null if generation fails
     */
    suspend fun generateThumbnailBitmap(
        videoFilePath: String,
        timeUs: Long = 1000000L // 1 second
    ): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            // Set the video data source
            if (videoFilePath.startsWith("http")) {
                retriever.setDataSource(videoFilePath, HashMap<String, String>())
            } else {
                retriever.setDataSource(videoFilePath)
            }
            
            // Get the frame at the specified time
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: IOException) {
                // Ignore release errors
            }
        }
    }

    /**
     * Generate and cache a thumbnail for a video file.
     * 
     * @param context Android context for file operations
     * @param videoFilePath The path to the video file
     * @param videoId Unique identifier for the video (used for cache file naming)
     * @return The file path to the cached thumbnail or null if generation fails
     */
    suspend fun generateAndCacheThumbnail(
        context: Context,
        videoFilePath: String,
        videoId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Generate thumbnail bitmap
            val bitmap = generateThumbnailBitmap(videoFilePath) ?: return@withContext null
            
            // Create thumbnails directory
            val thumbnailsDir = File(context.filesDir, "video_thumbnails")
            if (!thumbnailsDir.exists()) {
                thumbnailsDir.mkdirs()
            }
            
            // Create thumbnail file
            val thumbnailFile = File(thumbnailsDir, "${videoId}_thumbnail.jpg")
            
            // Save bitmap to file
            FileOutputStream(thumbnailFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            thumbnailFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get cached thumbnail path if it exists.
     * 
     * @param context Android context for file operations
     * @param videoId Unique identifier for the video
     * @return The file path to the cached thumbnail or null if it doesn't exist
     */
    fun getCachedThumbnailPath(context: Context, videoId: String): String? {
        val thumbnailsDir = File(context.filesDir, "video_thumbnails")
        val thumbnailFile = File(thumbnailsDir, "${videoId}_thumbnail.jpg")
        return if (thumbnailFile.exists()) thumbnailFile.absolutePath else null
    }
    
    /**
     * Get video duration in milliseconds.
     * 
     * @param videoFilePath The path to the video file
     * @return Video duration in milliseconds or 0 if unable to retrieve
     */
    suspend fun getVideoDuration(videoFilePath: String): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            if (videoFilePath.startsWith("http")) {
                retriever.setDataSource(videoFilePath, HashMap<String, String>())
            } else {
                retriever.setDataSource(videoFilePath)
            }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: IOException) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Clean up old thumbnail files to save storage space.
     * 
     * @param context Android context for file operations
     * @param maxAgeMillis Maximum age of thumbnail files in milliseconds
     * @return Number of files cleaned up
     */
    suspend fun cleanupOldThumbnails(
        context: Context,
        maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L // 7 days
    ): Int = withContext(Dispatchers.IO) {
        val thumbnailsDir = File(context.filesDir, "video_thumbnails")
        if (!thumbnailsDir.exists()) return@withContext 0
        
        val currentTime = System.currentTimeMillis()
        var cleanedCount = 0
        
        thumbnailsDir.listFiles()?.forEach { file ->
            if (currentTime - file.lastModified() > maxAgeMillis) {
                if (file.delete()) {
                    cleanedCount++
                }
            }
        }
        
        cleanedCount
    }
}