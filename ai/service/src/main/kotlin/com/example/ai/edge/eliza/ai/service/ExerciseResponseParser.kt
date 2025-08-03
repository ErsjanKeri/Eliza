package com.example.ai.edge.eliza.ai.service

import android.util.Log
import com.example.ai.edge.eliza.core.model.ExerciseGenerationResponse
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/**
 * Parser for AI-generated exercise responses.
 * Handles JSON extraction and validation for reliable exercise generation.
 */
class ExerciseResponseParser @Inject constructor() {
    
    companion object {
        private const val TAG = "ExerciseResponseParser"
    }
    
    /**
     * Parse AI response using line-by-line format.
     * Much more robust than JSON for small models with commas and quotes in options.
     */
    fun parseGeneratedExercise(aiResponse: String): ExerciseGenerationResponse? {
        return try {
            Log.d(TAG, "Parsing AI response: ${aiResponse.take(200)}...")
            
            // Parse line-by-line format
            val response = parseLineByLineFormat(aiResponse)
            
            if (response != null) {
                // Validate the parsed response
                validateGeneratedExercise(response)
                Log.d(TAG, "Successfully parsed exercise: ${response.questionText.take(50)}...")
                return response
            }
            
            // Fallback to JSON parsing if line format fails
            Log.d(TAG, "Line format failed, trying JSON fallback...")
            tryJsonParsing(aiResponse)
            
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Validation failed", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing exercise", e)
            null
        }
    }
    
    /**
     * Parse the new line-by-line format that avoids JSON comma/quote issues.
     * Format: "key: value" on each line.
     */
    private fun parseLineByLineFormat(aiResponse: String): ExerciseGenerationResponse? {
        return try {
            Log.d(TAG, "Attempting line-by-line parsing...")
            
            val lines = aiResponse.trim().lines()
            val data = mutableMapOf<String, String>()
            
            // Extract key-value pairs from lines
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.contains(":") && !trimmedLine.startsWith("//") && !trimmedLine.startsWith("#")) {
                    val parts = trimmedLine.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim().lowercase()
                        val value = parts[1].trim()
                        data[key] = value
                    }
                }
            }
            
            Log.d(TAG, "Extracted data: $data")
            
            // Extract required fields
            val questionText = data["questiontext"] ?: return null
            val option1 = data["option1"] ?: return null
            val option2 = data["option2"] ?: return null
            val option3 = data["option3"] ?: return null
            val option4 = data["option4"] ?: return null
            val correctAnswerIndex = data["correctanswerindex"]?.toIntOrNull() ?: return null
            
            val options = listOf(option1, option2, option3, option4)
            
            // Optional fields with defaults
            val explanation = data["explanation"] ?: "Solution steps provided"
            val conceptFocus = data["conceptfocus"] ?: "Mathematical Problem Solving"
            val difficultyAchieved = data["difficultyachieved"] ?: "medium"
            
            Log.d(TAG, "Line parsing successful - Question: ${questionText.take(50)}...")
            
            ExerciseGenerationResponse(
                questionText = questionText,
                options = options,
                correctAnswerIndex = correctAnswerIndex,
                explanation = explanation,
                conceptFocus = conceptFocus,
                difficultyAchieved = difficultyAchieved
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Line-by-line parsing failed", e)
            null
        }
    }
    
    /**
     * Fallback JSON parsing for compatibility with old responses.
     */
    private fun tryJsonParsing(aiResponse: String): ExerciseGenerationResponse? {
        return try {
            Log.d(TAG, "Attempting JSON parsing as fallback...")
            
            // Extract JSON from AI response (handle markdown code blocks)
            val jsonString = extractJsonFromResponse(aiResponse)
            Log.d(TAG, "Extracted JSON: $jsonString")
            
            val jsonObject = JSONObject(jsonString)
            
            ExerciseGenerationResponse(
                questionText = jsonObject.getString("questionText").trim(),
                options = parseOptionsArray(jsonObject.getJSONArray("options")),
                correctAnswerIndex = jsonObject.getInt("correctAnswerIndex"),
                explanation = jsonObject.optString("explanation", "Solution steps provided").trim(),
                conceptFocus = jsonObject.optString("conceptFocus", "Mathematical Problem Solving").trim(),
                difficultyAchieved = jsonObject.optString("difficultyAchieved", "medium").trim()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "JSON fallback parsing failed", e)
            // Try the old fallback parsing method
            tryFallbackParsing(aiResponse)
        }
    }
    
    /**
     * Last resort parsing for when both line and JSON parsing fail.
     * Extracts what we can to maximize success rate.
     */
    private fun tryFallbackParsing(aiResponse: String): ExerciseGenerationResponse? {
        return try {
            Log.d(TAG, "Attempting fallback parsing...")
            
            // Try to extract key fields using regex patterns
            val questionText = extractFieldByPattern(aiResponse, "questionText")
            val explanation = extractFieldByPattern(aiResponse, "explanation")
            val conceptFocus = extractFieldByPattern(aiResponse, "conceptFocus")
            val difficultyAchieved = extractFieldByPattern(aiResponse, "difficultyAchieved")
            
            // Extract options from pipe-separated format directly
            val optionsText = extractFieldByPattern(aiResponse, "options")
            val options = if (optionsText.contains("|")) {
                optionsText.split("|").map { it.trim() }.filter { it.isNotBlank() }.take(4)
            } else {
                // Try to extract from array format
                val arrayMatch = "\\[([^\\]]+)\\]".toRegex().find(optionsText)
                arrayMatch?.groupValues?.get(1)?.split(",")?.map { 
                    it.trim().removePrefix("\"").removeSuffix("\"") 
                }?.take(4) ?: emptyList()
            }
            
            // Extract correct answer index
            val correctAnswerIndex = extractFieldByPattern(aiResponse, "correctAnswerIndex").toIntOrNull() ?: 0
            
            if (questionText.isNotBlank() && options.size == 4 && explanation.isNotBlank()) {
                val response = ExerciseGenerationResponse(
                    questionText = questionText,
                    options = options,
                    correctAnswerIndex = correctAnswerIndex.coerceIn(0, 3),
                    explanation = explanation,
                    conceptFocus = conceptFocus.takeIf { it.isNotBlank() } ?: "Mathematical Problem Solving",
                    difficultyAchieved = difficultyAchieved.takeIf { it.isNotBlank() } ?: "medium"
                )
                
                Log.d(TAG, "Fallback parsing successful!")
                return response
            }
            
            Log.d(TAG, "Fallback parsing failed - insufficient data")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Fallback parsing failed", e)
            null
        }
    }
    
    /**
     * Extract field value using regex pattern.
     */
    private fun extractFieldByPattern(text: String, fieldName: String): String {
        val pattern = "\"$fieldName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(text)?.groupValues?.get(1) ?: ""
    }
    
    /**
     * Extract JSON content from AI response, handling various formats.
     */
    private fun extractJsonFromResponse(response: String): String {
        val cleanResponse = response.trim()
        
        // Case 1: Direct JSON response
        if (cleanResponse.startsWith("{") && cleanResponse.endsWith("}")) {
            return attemptJsonRepair(cleanResponse)
        }
        
        // Case 2: JSON wrapped in markdown code blocks
        val jsonBlockRegex = "```(?:json)?\n?(.+?)\n?```".toRegex(RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonBlockRegex.find(cleanResponse)
        if (jsonMatch != null) {
            return attemptJsonRepair(jsonMatch.groupValues[1].trim())
        }
        
        // Case 3: Find JSON object within text
        val jsonStart = cleanResponse.indexOf("{")
        val jsonEnd = cleanResponse.lastIndexOf("}") + 1
        
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            return attemptJsonRepair(cleanResponse.substring(jsonStart, jsonEnd))
        }
        
        // Case 4: Try to find JSON-like structure with line breaks
        val multilineJsonRegex = "\\{[^{}]*(?:\"[^\"]*\"\\s*:[^,}]*,?\\s*)+[^{}]*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
        val multilineMatch = multilineJsonRegex.find(cleanResponse)
        if (multilineMatch != null) {
            return attemptJsonRepair(multilineMatch.value)
        }
        
        throw IllegalArgumentException("No valid JSON found in response")
    }
    
    /**
     * Attempt to repair common JSON formatting issues, especially with options arrays.
     * Optimized for Gemma 3n's tendency to create pipe-separated options.
     */
    private fun attemptJsonRepair(jsonString: String): String {
        var repairedJson = jsonString
        
        Log.d(TAG, "Attempting JSON repair on: ${jsonString.take(200)}...")
        
        // Fix 1: Remove common markdown artifacts
        repairedJson = repairedJson
            .replace("```json", "")
            .replace("```", "")
            .replace("\\\"", "\"")
            .trim()
        
        // Fix 2: THE MAIN ISSUE - Repair pipe-separated options in single string
        // Pattern: "options": ["item1 | item2 | item3 | item4"]
        val pipeOptionsRegex = "\"options\"\\s*:\\s*\\[\\s*\"([^\"]*\\|[^\"]*?)\"\\s*\\]".toRegex()
        val pipeMatch = pipeOptionsRegex.find(repairedJson)
        
        if (pipeMatch != null) {
            val pipeContent = pipeMatch.groupValues[1]
            Log.d(TAG, "Found pipe-separated options: $pipeContent")
            
            // Split by pipes and create proper array
            val options = pipeContent.split("|")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(4) // Ensure exactly 4 options
                .map { "\"$it\"" }
            
            if (options.size == 4) {
                val repairedOptionsString = "\"options\": [${options.joinToString(", ")}]"
                Log.d(TAG, "Repaired pipe-separated options: $repairedOptionsString")
                repairedJson = repairedJson.replace(pipeMatch.value, repairedOptionsString)
            }
        } else {
            // Fix 3: Repair missing quotes in options array (fallback)
            val optionsRegex = "\"options\"\\s*:\\s*\\[([^\\]]+)\\]".toRegex()
            val optionsMatch = optionsRegex.find(repairedJson)
            
            if (optionsMatch != null) {
                val optionsContent = optionsMatch.groupValues[1]
                Log.d(TAG, "Found options array content: $optionsContent")
                
                // Check if it might be comma-separated but improperly quoted
                val items = if (optionsContent.contains(",")) {
                    optionsContent.split(",")
                } else {
                    // Try pipe separation as fallback
                    optionsContent.split("|")
                }
                
                val repairedItems = items.map { item ->
                    val trimmedItem = item.trim()
                    
                    // If item doesn't start and end with quotes, add them
                    if (!trimmedItem.startsWith("\"") || !trimmedItem.endsWith("\"")) {
                        // Remove any existing quotes and re-add them properly
                        val cleanItem = trimmedItem.removePrefix("\"").removeSuffix("\"")
                        "\"$cleanItem\""
                    } else {
                        trimmedItem
                    }
                }.take(4) // Ensure exactly 4 options
                
                if (repairedItems.size == 4) {
                    val repairedOptionsArray = repairedItems.joinToString(", ")
                    val repairedOptionsString = "\"options\": [$repairedOptionsArray]"
                    
                    Log.d(TAG, "Repaired options array: $repairedOptionsString")
                    repairedJson = repairedJson.replace(optionsMatch.value, repairedOptionsString)
                }
            }
        }
        
        // Fix 4: Remove trailing commas
        repairedJson = repairedJson.replace(",\\s*}".toRegex(), "}")
        repairedJson = repairedJson.replace(",\\s*]".toRegex(), "]")
        
        // Fix 5: Normalize whitespace
        repairedJson = repairedJson.replace("\\s+".toRegex(), " ")
        
        Log.d(TAG, "JSON repair completed: ${repairedJson.take(200)}...")
        return repairedJson
    }
    
    /**
     * Parse the options array from JSON, ensuring exactly 4 options.
     */
    private fun parseOptionsArray(jsonArray: JSONArray): List<String> {
        val options = mutableListOf<String>()
        
        for (i in 0 until jsonArray.length()) {
            val option = jsonArray.getString(i).trim()
            if (option.isNotBlank()) {
                options.add(option)
            }
        }
        
        if (options.size != 4) {
            throw IllegalArgumentException("Must have exactly 4 options, found ${options.size}")
        }
        
        return options
    }
    
        /**
     * Validate the generated exercise for essential requirements only.
     * Made much more lenient to maximize success rate.
     */
    private fun validateGeneratedExercise(response: ExerciseGenerationResponse) {
        // Only validate the absolute essentials
        require(response.questionText.isNotBlank()) { 
            "Question text cannot be blank" 
        }
        require(response.options.size == 4) { 
            "Must have exactly 4 options, found ${response.options.size}" 
        }
        require(response.correctAnswerIndex in 0..3) { 
            "Correct answer index must be 0-3, found ${response.correctAnswerIndex}" 
        }
        
        // Just check that options aren't blank - that's it!
        response.options.forEach { option ->
            require(option.isNotBlank()) { "Option cannot be blank: '$option'" }
        }
        
        Log.d(TAG, "Exercise validation passed successfully")
    }
    

    
    /**
     * Check if the response looks like valid JSON structure.
     */
    fun isValidJsonStructure(response: String): Boolean {
        return try {
            extractJsonFromResponse(response)
            true
        } catch (e: Exception) {
            false
        }
    }
}