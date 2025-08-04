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

package com.example.ai.edge.eliza.feature.chat.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ai.edge.eliza.core.common.VideoThumbnailGenerator

/**
 * Compose-specific extensions for video thumbnail generation.
 * 
 * These extensions provide Jetpack Compose specific functionality
 * that builds on the core VideoThumbnailGenerator utility.
 */

/**
 * Generate a thumbnail ImageBitmap from a video file for Compose.
 * 
 * @param videoFilePath The path to the video file (local file path)
 * @param timeUs The time position in the video to extract the frame from (in microseconds).
 *               Default is 1000000 (1 second) to avoid potential black frames at the start.
 * @return ImageBitmap for Compose or null if generation fails
 */
suspend fun generateVideoThumbnail(
    videoFilePath: String,
    timeUs: Long = 1000000L // 1 second
): ImageBitmap? {
    val bitmap = VideoThumbnailGenerator.generateThumbnailBitmap(videoFilePath, timeUs)
    return bitmap?.asImageBitmap()
}