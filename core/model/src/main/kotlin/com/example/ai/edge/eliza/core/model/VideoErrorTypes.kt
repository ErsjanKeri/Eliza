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

package com.example.ai.edge.eliza.core.model

/**
 * Comprehensive error types for video explanation system.
 * 
 * These error types provide detailed classification of video generation failures
 * to enable intelligent error handling and user-friendly error messages.
 */
enum class VideoErrorType {
    // Network-related errors
    NETWORK_UNAVAILABLE,        // No internet connection
    NETWORK_TIMEOUT,           // Request timeout
    NETWORK_INTERRUPTED,       // Connection lost during operation
    
    // Server-related errors  
    SERVER_UNAVAILABLE,        // ElizaServer is down/unreachable
    SERVER_OVERLOADED,         // Server too busy (503 errors)
    SERVER_ERROR,              // Internal server error (500)
    AUTHENTICATION_ERROR,      // Auth/permission issues
    
    // Request-related errors
    INVALID_PROMPT,            // Prompt validation failed
    PROMPT_TOO_LONG,          // Prompt exceeds length limits
    UNSUPPORTED_CONTENT,       // Content type not supported
    RATE_LIMITED,              // Too many requests
    
    // Generation-related errors
    GENERATION_FAILED,         // Video generation failed on server
    GENERATION_TIMEOUT,        // Generation took too long
    CONTENT_REJECTED,          // Content failed moderation
    INSUFFICIENT_RESOURCES,    // Server lacks resources
    
    // Download-related errors
    DOWNLOAD_FAILED,           // Failed to download completed video
    DOWNLOAD_CORRUPTED,        // Downloaded file is corrupted
    DOWNLOAD_INTERRUPTED,      // Download was interrupted
    STORAGE_FULL,              // Device storage full
    
    // File system errors
    FILE_SYSTEM_ERROR,         // General file I/O error
    PERMISSION_DENIED,         // No permission to write files
    DISK_SPACE_LOW,           // Low disk space warning
    
    // Video playback errors
    VIDEO_CORRUPTED,           // Video file is corrupted
    CODEC_UNSUPPORTED,         // Video codec not supported
    PLAYBACK_ERROR,            // ExoPlayer playback error
    
    // Thumbnail generation errors
    THUMBNAIL_FAILED,          // Thumbnail extraction failed
    METADATA_UNAVAILABLE,      // Video metadata unavailable
    
    // General errors
    UNKNOWN_ERROR              // Unclassified error
}

/**
 * Enhanced error information for video explanation failures.
 * 
 * Provides detailed error context to enable intelligent error handling,
 * retry logic, and user-friendly error messages.
 */
data class VideoErrorInfo(
    val type: VideoErrorType,
    val message: String,
    val technicalDetails: String? = null,
    val isRetryable: Boolean = false,
    val suggestedAction: String? = null,
    val retryDelayMs: Long = 0L,
    val maxRetries: Int = 0
) {
    
    companion object {
        
        /**
         * Create error info from a network exception.
         */
        fun fromNetworkError(exception: Throwable): VideoErrorInfo {
            return when {
                exception.message?.contains("timeout", ignoreCase = true) == true -> {
                    VideoErrorInfo(
                        type = VideoErrorType.NETWORK_TIMEOUT,
                        message = "Request timed out. Please check your internet connection and try again.",
                        technicalDetails = exception.message,
                        isRetryable = true,
                        suggestedAction = "Check your internet connection",
                        retryDelayMs = 5000L,
                        maxRetries = 3
                    )
                }
                exception.message?.contains("network", ignoreCase = true) == true -> {
                    VideoErrorInfo(
                        type = VideoErrorType.NETWORK_UNAVAILABLE,
                        message = "No internet connection available. Please connect to the internet and try again.",
                        technicalDetails = exception.message,
                        isRetryable = true,
                        suggestedAction = "Connect to the internet",
                        retryDelayMs = 3000L,
                        maxRetries = 5
                    )
                }
                else -> {
                    VideoErrorInfo(
                        type = VideoErrorType.NETWORK_INTERRUPTED,
                        message = "Network connection was interrupted. Please try again.",
                        technicalDetails = exception.message,
                        isRetryable = true,
                        suggestedAction = "Try again",
                        retryDelayMs = 2000L,
                        maxRetries = 3
                    )
                }
            }
        }
        
        /**
         * Create error info from an HTTP response code.
         */
        fun fromHttpError(code: Int, responseBody: String?): VideoErrorInfo {
            return when (code) {
                503 -> VideoErrorInfo(
                    type = VideoErrorType.SERVER_OVERLOADED,
                    message = "The video service is currently busy. Please try again in a few moments.",
                    technicalDetails = "HTTP $code: $responseBody",
                    isRetryable = true,
                    suggestedAction = "Wait and try again",
                    retryDelayMs = 10000L,
                    maxRetries = 2
                )
                500, 502, 504 -> VideoErrorInfo(
                    type = VideoErrorType.SERVER_ERROR,
                    message = "The video service is temporarily unavailable. Please try again later.",
                    technicalDetails = "HTTP $code: $responseBody",
                    isRetryable = true,
                    suggestedAction = "Try again later",
                    retryDelayMs = 15000L,
                    maxRetries = 2
                )
                429 -> VideoErrorInfo(
                    type = VideoErrorType.RATE_LIMITED,
                    message = "Too many video requests. Please wait a moment before requesting another video.",
                    technicalDetails = "HTTP $code: Rate limited",
                    isRetryable = true,
                    suggestedAction = "Wait before retrying",
                    retryDelayMs = 30000L,
                    maxRetries = 1
                )
                400 -> VideoErrorInfo(
                    type = VideoErrorType.INVALID_PROMPT,
                    message = "Your request couldn't be processed. Please try rephrasing your question.",
                    technicalDetails = "HTTP $code: $responseBody",
                    isRetryable = false,
                    suggestedAction = "Rephrase your question"
                )
                401, 403 -> VideoErrorInfo(
                    type = VideoErrorType.AUTHENTICATION_ERROR,
                    message = "Authentication error. Please restart the app and try again.",
                    technicalDetails = "HTTP $code: Authentication failed",
                    isRetryable = false,
                    suggestedAction = "Restart the app"
                )
                else -> VideoErrorInfo(
                    type = VideoErrorType.SERVER_ERROR,
                    message = "An unexpected error occurred. Please try again.",
                    technicalDetails = "HTTP $code: $responseBody",
                    isRetryable = true,
                    suggestedAction = "Try again",
                    retryDelayMs = 5000L,
                    maxRetries = 2
                )
            }
        }
        
        /**
         * Create error info for storage-related failures.
         */
        fun fromStorageError(exception: Throwable): VideoErrorInfo {
            return when {
                exception.message?.contains("space", ignoreCase = true) == true -> {
                    VideoErrorInfo(
                        type = VideoErrorType.STORAGE_FULL,
                        message = "Not enough storage space to download the video. Please free up some space and try again.",
                        technicalDetails = exception.message,
                        isRetryable = true,
                        suggestedAction = "Free up storage space",
                        retryDelayMs = 0L,
                        maxRetries = 1
                    )
                }
                exception.message?.contains("permission", ignoreCase = true) == true -> {
                    VideoErrorInfo(
                        type = VideoErrorType.PERMISSION_DENIED,
                        message = "Unable to save video due to permission restrictions. Please check app permissions.",
                        technicalDetails = exception.message,
                        isRetryable = false,
                        suggestedAction = "Check app permissions"
                    )
                }
                else -> {
                    VideoErrorInfo(
                        type = VideoErrorType.FILE_SYSTEM_ERROR,
                        message = "Failed to save video file. Please try again.",
                        technicalDetails = exception.message,
                        isRetryable = true,
                        suggestedAction = "Try again",
                        retryDelayMs = 2000L,
                        maxRetries = 2
                    )
                }
            }
        }
        
        /**
         * Create error info for video generation failures on server.
         */
        fun fromGenerationFailure(serverMessage: String): VideoErrorInfo {
            return when {
                serverMessage.contains("timeout", ignoreCase = true) -> {
                    VideoErrorInfo(
                        type = VideoErrorType.GENERATION_TIMEOUT,
                        message = "Video generation is taking longer than expected. Please try with a shorter or simpler request.",
                        technicalDetails = serverMessage,
                        isRetryable = true,
                        suggestedAction = "Try a simpler request",
                        retryDelayMs = 5000L,
                        maxRetries = 1
                    )
                }
                serverMessage.contains("content", ignoreCase = true) -> {
                    VideoErrorInfo(
                        type = VideoErrorType.CONTENT_REJECTED,
                        message = "Your request couldn't be processed. Please try rephrasing your question.",
                        technicalDetails = serverMessage,
                        isRetryable = false,
                        suggestedAction = "Rephrase your question"
                    )
                }
                serverMessage.contains("resource", ignoreCase = true) -> {
                    VideoErrorInfo(
                        type = VideoErrorType.INSUFFICIENT_RESOURCES,
                        message = "The video service is currently at capacity. Please try again in a few minutes.",
                        technicalDetails = serverMessage,
                        isRetryable = true,
                        suggestedAction = "Try again later",
                        retryDelayMs = 60000L,
                        maxRetries = 1
                    )
                }
                else -> {
                    VideoErrorInfo(
                        type = VideoErrorType.GENERATION_FAILED,
                        message = "Video generation failed. Please try again with a different request.",
                        technicalDetails = serverMessage,
                        isRetryable = true,
                        suggestedAction = "Try a different request",
                        retryDelayMs = 3000L,
                        maxRetries = 2
                    )
                }
            }
        }
    }
}