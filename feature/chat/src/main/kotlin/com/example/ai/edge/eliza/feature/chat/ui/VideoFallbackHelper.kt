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

import com.example.ai.edge.eliza.core.model.VideoErrorType

/**
 * Helper utility for handling video generation failures and providing intelligent fallbacks.
 * 
 * This utility helps determine when to offer text-based alternatives and provides
 * user-friendly messaging for different failure scenarios.
 */
object VideoFallbackHelper {
    
    /**
     * Determine if a text chat fallback should be offered based on the error type.
     */
    fun shouldOfferTextFallback(errorType: VideoErrorType, retryCount: Int): Boolean {
        return when (errorType) {
            // Network issues - offer fallback for offline scenarios
            VideoErrorType.NETWORK_UNAVAILABLE,
            VideoErrorType.NETWORK_TIMEOUT,
            VideoErrorType.NETWORK_INTERRUPTED -> true
            
            // Server issues - offer fallback when server is down
            VideoErrorType.SERVER_UNAVAILABLE,
            VideoErrorType.SERVER_OVERLOADED,
            VideoErrorType.SERVER_ERROR -> true
            
            // Content issues - offer fallback for content that can't be processed
            VideoErrorType.CONTENT_REJECTED,
            VideoErrorType.UNSUPPORTED_CONTENT,
            VideoErrorType.PROMPT_TOO_LONG -> true
            
            // Generation failures - offer fallback after retries fail
            VideoErrorType.GENERATION_FAILED,
            VideoErrorType.GENERATION_TIMEOUT,
            VideoErrorType.INSUFFICIENT_RESOURCES -> retryCount > 0
            
            // Storage issues - don't offer fallback, user needs to fix storage
            VideoErrorType.STORAGE_FULL,
            VideoErrorType.PERMISSION_DENIED,
            VideoErrorType.FILE_SYSTEM_ERROR -> false
            
            // Rate limiting - don't offer fallback, user should wait
            VideoErrorType.RATE_LIMITED -> false
            
            // Auth issues - don't offer fallback, app issue
            VideoErrorType.AUTHENTICATION_ERROR -> false
            
            // Download issues - don't offer fallback, video was ready but download failed
            VideoErrorType.DOWNLOAD_FAILED,
            VideoErrorType.DOWNLOAD_CORRUPTED,
            VideoErrorType.DOWNLOAD_INTERRUPTED -> false
            
            // For other errors, offer fallback after some attempts
            else -> retryCount > 1
        }
    }
    
    /**
     * Get a user-friendly fallback message based on the error type.
     */
    fun getFallbackMessage(errorType: VideoErrorType): String {
        return when (errorType) {
            VideoErrorType.NETWORK_UNAVAILABLE -> 
                "Since you're offline, I'll answer your question with text instead."
            
            VideoErrorType.SERVER_UNAVAILABLE,
            VideoErrorType.SERVER_OVERLOADED,
            VideoErrorType.SERVER_ERROR -> 
                "The video service is temporarily unavailable. Let me answer your question with text instead."
            
            VideoErrorType.CONTENT_REJECTED,
            VideoErrorType.UNSUPPORTED_CONTENT -> 
                "I can't create a video for this type of content, but I can explain it with text."
            
            VideoErrorType.PROMPT_TOO_LONG -> 
                "Your question is quite detailed. Let me provide a comprehensive text explanation instead."
            
            VideoErrorType.GENERATION_FAILED,
            VideoErrorType.GENERATION_TIMEOUT -> 
                "Video generation didn't work this time. Let me give you a detailed text explanation."
            
            VideoErrorType.INSUFFICIENT_RESOURCES -> 
                "The video service is busy right now. I'll provide a thorough text explanation instead."
            
            else -> 
                "I'll answer your question with text since the video couldn't be generated."
        }
    }
    
    /**
     * Create a fallback prompt that includes the original user question and explains
     * why we're falling back to text.
     */
    fun createFallbackPrompt(originalQuestion: String, errorType: VideoErrorType): String {
        val fallbackReason = when (errorType) {
            VideoErrorType.NETWORK_UNAVAILABLE -> "since you're currently offline"
            VideoErrorType.SERVER_UNAVAILABLE -> "due to video service maintenance"
            VideoErrorType.CONTENT_REJECTED -> "as this content is better explained in text"
            VideoErrorType.PROMPT_TOO_LONG -> "given the detailed nature of your question"
            else -> "to ensure you get a comprehensive answer"
        }
        
        return "I understand you asked: \"$originalQuestion\"\n\n" +
               "I'll provide a detailed text explanation $fallbackReason. " +
               "Please let me give you a thorough answer to help you understand this topic."
    }
    
    /**
     * Determine the priority of offering fallback (higher number = higher priority).
     */
    fun getFallbackPriority(errorType: VideoErrorType): Int {
        return when (errorType) {
            // High priority - obvious cases where text is better alternative
            VideoErrorType.NETWORK_UNAVAILABLE -> 10
            VideoErrorType.CONTENT_REJECTED -> 9
            VideoErrorType.UNSUPPORTED_CONTENT -> 9
            VideoErrorType.PROMPT_TOO_LONG -> 8
            
            // Medium priority - service issues where text is good alternative  
            VideoErrorType.SERVER_UNAVAILABLE -> 7
            VideoErrorType.SERVER_OVERLOADED -> 6
            VideoErrorType.GENERATION_FAILED -> 5
            VideoErrorType.GENERATION_TIMEOUT -> 5
            
            // Lower priority - temporary issues that might resolve
            VideoErrorType.NETWORK_TIMEOUT -> 4
            VideoErrorType.NETWORK_INTERRUPTED -> 4
            VideoErrorType.INSUFFICIENT_RESOURCES -> 3
            VideoErrorType.SERVER_ERROR -> 3
            
            // Very low priority - user action required
            VideoErrorType.STORAGE_FULL -> 1
            VideoErrorType.PERMISSION_DENIED -> 1
            VideoErrorType.RATE_LIMITED -> 0
            
            else -> 2
        }
    }
}