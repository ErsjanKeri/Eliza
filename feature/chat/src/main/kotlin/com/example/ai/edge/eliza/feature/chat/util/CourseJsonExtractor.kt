/*
 * Copyright 2025 The Eliza Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.feature.chat.util

import android.util.Log

/**
 * Utility for extracting JSON from AI responses that may be wrapped in markdown or contain extra text.
 * Based on the robust patterns from ExerciseResponseParser.
 */
object CourseJsonExtractor {
    
    private const val TAG = "CourseJsonExtractor"
    
    /**
     * Extract clean JSON string from AI response, handling various formats:
     * 1. Direct JSON: {...}
     * 2. Markdown wrapped: ```json {...} ```
     * 3. Mixed content: text {...} text
     * 4. Multiline JSON with extra whitespace
     */
    fun extractJsonFromResponse(response: String): String {
        val cleanResponse = response.trim()
        
        Log.d(TAG, "Extracting JSON from response (${cleanResponse.length} chars)")
        Log.d(TAG, "Response preview: ${cleanResponse.take(100)}...")
        
        // Case 1: Direct JSON response
        if (cleanResponse.startsWith("{") && cleanResponse.endsWith("}")) {
            Log.d(TAG, "Found direct JSON response")
            return cleanResponse
        }
        
        // Case 2: JSON wrapped in markdown code blocks
        // Pattern matches: ```json\n{...}\n``` or ```\n{...}\n```
        val jsonBlockRegex = "```(?:json)?\n?(.+?)\n?```".toRegex(RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonBlockRegex.find(cleanResponse)
        if (jsonMatch != null) {
            val extractedJson = jsonMatch.groupValues[1].trim()
            Log.d(TAG, "Found JSON in markdown block: ${extractedJson.take(50)}...")
            return extractedJson
        }
        
        // Case 3: Find JSON object within text (first { to last })
        val jsonStart = cleanResponse.indexOf("{")
        val jsonEnd = cleanResponse.lastIndexOf("}") + 1
        
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            val extractedJson = cleanResponse.substring(jsonStart, jsonEnd)
            Log.d(TAG, "Found JSON within text: ${extractedJson.take(50)}...")
            return extractedJson
        }
        
        // Case 4: Try to find JSON-like structure with line breaks
        val multilineJsonRegex = "\\{[^{}]*(?:\"[^\"]*\"\\s*:[^,}]*,?\\s*)+[^{}]*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
        val multilineMatch = multilineJsonRegex.find(cleanResponse)
        if (multilineMatch != null) {
            val extractedJson = multilineMatch.value
            Log.d(TAG, "Found multiline JSON: ${extractedJson.take(50)}...")
            return extractedJson
        }
        
        Log.e(TAG, "No valid JSON found in response")
        Log.e(TAG, "Full response: $cleanResponse")
        throw IllegalArgumentException("No valid JSON found in AI response")
    }
    
    /**
     * Safe extraction that returns null instead of throwing exception.
     * Useful for graceful fallback to regular text messages.
     */
    fun tryExtractJsonFromResponse(response: String): String? {
        return try {
            extractJsonFromResponse(response)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract JSON: ${e.message}")
            null
        }
    }
}