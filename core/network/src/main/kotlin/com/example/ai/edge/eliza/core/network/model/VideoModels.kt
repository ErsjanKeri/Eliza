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

package com.example.ai.edge.eliza.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Video generation status enum matching ElizaServer's VideoStatus.
 * 
 * These values must exactly match the server-side enum values.
 */
@Serializable
enum class VideoStatus {
    @SerialName("queued")
    QUEUED,
    
    @SerialName("generating_script")
    GENERATING_SCRIPT,
    
    @SerialName("rendering_video")
    RENDERING_VIDEO,
    
    @SerialName("completed")
    COMPLETED,
    
    @SerialName("failed")
    FAILED
}

/**
 * Request model for creating a new video generation.
 * Matches ElizaServer's VideoRequest model exactly.
 * 
 * @param prompt Description of the educational video to generate (10-1000 characters)
 * @param durationLimit Maximum duration in seconds (5-120, default 30)
 */
@Serializable
data class VideoRequest(
    @SerialName("prompt")
    val prompt: String,
    
    @SerialName("duration_limit")
    val durationLimit: Int? = null
)

/**
 * Response model for video generation requests.
 * Matches ElizaServer's VideoResponse model exactly.
 * 
 * @param videoId Unique identifier for the video generation request
 * @param status Current status of the video generation
 * @param message Human-readable status message
 * @param videoUrl URL for downloading the completed video (null until completed)
 * @param createdAt ISO timestamp when the request was created
 * @param progress Generation progress percentage (0-100)
 */
@Serializable
data class VideoResponse(
    @SerialName("video_id")
    val videoId: String,
    
    @SerialName("status")
    val status: VideoStatus,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("video_url")
    val videoUrl: String? = null,
    
    @SerialName("created_at")
    val createdAt: String, // ISO datetime string from server
    
    @SerialName("progress")
    val progress: Int? = null
)