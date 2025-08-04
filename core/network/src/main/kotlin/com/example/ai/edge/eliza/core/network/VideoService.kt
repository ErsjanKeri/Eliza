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

package com.example.ai.edge.eliza.core.network

import com.example.ai.edge.eliza.core.network.model.VideoRequest
import com.example.ai.edge.eliza.core.network.model.VideoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for ElizaServer video generation API.
 * 
 * This interface matches the exact endpoints provided by ElizaServer:
 * - POST /api/v1/videos - Create video generation request
 * - GET /api/v1/videos/{video_id}/status - Check generation status
 * - GET /api/v1/videos/{video_id}/download - Download completed video
 */
interface VideoService {
    
    /**
     * Create a new video generation request.
     * 
     * @param request Video generation request containing prompt and duration_limit
     * @return VideoResponse with video_id and initial status
     */
    @POST("api/v1/videos")
    suspend fun createVideo(@Body request: VideoRequest): VideoResponse
    
    /**
     * Check the status of a video generation request.
     * 
     * @param videoId The unique video ID returned from createVideo
     * @return VideoResponse with current status and progress
     */
    @GET("api/v1/videos/{video_id}/status")
    suspend fun getVideoStatus(@Path("video_id") videoId: String): VideoResponse
    
    /**
     * Download a completed video file.
     * 
     * @param videoId The unique video ID of the completed video
     * @return Response containing the MP4 video file as bytes
     */
    @GET("api/v1/videos/{video_id}/download")
    suspend fun downloadVideo(@Path("video_id") videoId: String): Response<ResponseBody>
}