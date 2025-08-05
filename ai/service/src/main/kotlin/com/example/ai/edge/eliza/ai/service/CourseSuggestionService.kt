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

package com.example.ai.edge.eliza.ai.service

import android.util.Log
import com.example.ai.edge.eliza.ai.modelmanager.LlmChatModelHelper
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Course
import com.example.ai.edge.eliza.core.model.CourseSuggestionRequest
import com.example.ai.edge.eliza.core.model.CourseSuggestionResponse
import com.example.ai.edge.eliza.core.model.CourseSuggestionResult
import com.example.ai.edge.eliza.core.model.CourseSuggestionState
import com.example.ai.edge.eliza.core.model.Model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Service for AI-powered course suggestion generation using existing infrastructure.
 * Integrates with RagProviderFactory, LlmChatModelHelper, and CourseRepository following proper architecture.
 * Uses Singleton scope with proper RAG enhancement via RagProviderFactory.
 */
@Singleton
class CourseSuggestionService @Inject constructor(
    private val courseRepository: CourseRepository, // Reuse existing repository
    private val ragProviderFactory: RagProviderFactory // Proper Singleton-scoped RAG integration
) {
    
    companion object {
        private const val TAG = "CourseSuggestionService"
        
        // JSON parser with lenient settings for LLM output
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }
    
    /**
     * Generate course suggestions based on user's learning goals.
     * Uses existing CourseRepository infrastructure and RAG enhancement.
     */
    suspend fun generateCourseSuggestions(
        request: CourseSuggestionRequest,
        model: Model
    ): Flow<CourseSuggestionState> = flow {
        
        Log.d(TAG, "Starting course suggestions for query: ${request.userQuery}")
        emit(CourseSuggestionState.Loading("Eliza is analyzing your learning goals..."))
        
        try {
            // Step 1: Validate request
            val validationIssues = CourseSuggestionPromptTemplates.validateRequest(request)
            if (validationIssues.isNotEmpty()) {
                emit(CourseSuggestionState.Error("Invalid request: ${validationIssues.joinToString(", ")}"))
                return@flow
            }
            
            // Step 2: Get all available courses for context
            val allCourses = courseRepository.getAllCourses().firstOrNull() ?: emptyList()
            if (allCourses.isEmpty()) {
                emit(CourseSuggestionState.Error("No courses available for recommendations"))
                return@flow
            }
            
            Log.d(TAG, "Found ${allCourses.size} available courses")
            
            // Step 3: Create ChatContext for RAG enhancement
            val chatContext = ChatContext.createCourseSuggestion(
                userQuery = request.userQuery,
                userLevel = request.userLevel,
                preferredSubjects = request.preferredSubjects,
                availableTimeHours = request.availableTimeHours,
                allUserProgress = emptyList(), // Could be enhanced with actual user progress
                conversationHistory = emptyList()
            )
            
            // Step 4: Generate user-friendly prompt
            val userPrompt = CourseSuggestionPromptTemplates.createSuggestionPrompt(request)
            
            Log.d(TAG, "Generated prompt length: ${userPrompt.length}")
            
            // Step 5: Use RagProviderFactory for proper RAG enhancement
            emit(CourseSuggestionState.Loading("Finding the best courses for you..."))
            
            // Get appropriate RAG provider based on context
            val ragProvider = ragProviderFactory.createProvider(chatContext)
            
            // Enhance prompt with relevant course content
            val enhancementResult = ragProvider.enhancePrompt(
                prompt = userPrompt,
                context = chatContext
            )
            
            Log.d(TAG, "RAG enhancement completed (confidence: ${enhancementResult.confidence})")
            Log.d(TAG, "Enhanced prompt length: ${enhancementResult.enhancedPrompt.enhancedPrompt.length}")
            
            // Step 6: Use LlmChatModelHelper for direct model inference
            emit(CourseSuggestionState.Loading("Generating personalized recommendations..."))
            
            val aiResponse = suspendCancellableCoroutine<String> { continuation ->
                LlmChatModelHelper.runInference(
                    model = model,
                    input = enhancementResult.enhancedPrompt.enhancedPrompt,
                    resultListener = { response, isComplete ->
                        Log.d(TAG, "Course suggestion callback - complete: $isComplete, response length: ${response.length}")
                        if (isComplete) {
                            continuation.resume(response)
                        }
                    },
                    cleanUpListener = {
                        Log.d(TAG, "Course suggestion inference cleanup completed")
                    },
                    images = emptyList()
                )
            }
            
            Log.d(TAG, "Received AI response length: ${aiResponse.length}")
            
            // Step 7: Parse AI response and enrich with course metadata
            emit(CourseSuggestionState.Loading("Processing recommendations..."))
            
            val suggestions = parseAndEnrichSuggestions(aiResponse, allCourses, request)
            
            Log.d(TAG, "Successfully generated ${suggestions.recommendations.size} course recommendations")
            emit(CourseSuggestionState.Success(suggestions))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating course suggestions", e)
            emit(CourseSuggestionState.Error("Failed to generate suggestions: ${e.message}"))
        }
    }
    
    /**
     * Parse AI response and enrich with actual course metadata.
     * Follows the same pattern as ExerciseResponseParser.
     */
    private suspend fun parseAndEnrichSuggestions(
        aiResponse: String,
        availableCourses: List<Course>,
        request: CourseSuggestionRequest
    ): CourseSuggestionResult {
        
        try {
            // Extract JSON from AI response (may contain extra text)
            val jsonStartIndex = aiResponse.indexOf('{')
            val jsonEndIndex = aiResponse.lastIndexOf('}')
            
            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonStartIndex >= jsonEndIndex) {
                throw Exception("No valid JSON found in AI response")
            }
            
            val jsonString = aiResponse.substring(jsonStartIndex, jsonEndIndex + 1)
            Log.d(TAG, "Extracted JSON length: ${jsonString.length}")
            
            // Parse the JSON response
            val aiSuggestionResponse = json.decodeFromString<CourseSuggestionResponse>(jsonString)
            
            // Convert to domain objects with actual course metadata
            val courseRecommendations = aiSuggestionResponse.toCourseRecommendations(availableCourses)
            
            // Calculate total estimated hours
            val totalEstimatedHours = courseRecommendations.sumOf { it.estimatedCompletionHours }
            
            // Calculate confidence based on AI response quality and available data
            val confidence = calculateSuggestionConfidence(
                aiResponse = aiSuggestionResponse,
                foundCourses = courseRecommendations.size,
                requestedCourses = request.maxRecommendations
            )
            
            return CourseSuggestionResult(
                query = request.userQuery,
                recommendations = courseRecommendations,
                reasoning = aiSuggestionResponse.reasoning,
                studyPlan = aiSuggestionResponse.studyPlan ?: "Start with the recommended courses and progress through the suggested chapters.",
                alternativeTopics = aiSuggestionResponse.alternativeTopics,
                totalEstimatedHours = totalEstimatedHours,
                confidence = confidence
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI response", e)
            
            // Fallback: create basic suggestions from available courses
            return createFallbackSuggestions(request, availableCourses)
        }
    }
    
    /**
     * Calculate confidence score based on AI response quality.
     */
    private fun calculateSuggestionConfidence(
        aiResponse: CourseSuggestionResponse,
        foundCourses: Int,
        requestedCourses: Int
    ): Float {
        var confidence = 0.5f // Base confidence
        
        // Increase confidence based on course matches
        if (foundCourses > 0) confidence += 0.2f
        if (foundCourses >= requestedCourses) confidence += 0.1f
        
        // Increase confidence for detailed reasoning
        if (aiResponse.reasoning.length > 100) confidence += 0.1f
        
        // Increase confidence for detailed study plan
        if ((aiResponse.studyPlan?.length ?: 0) > 50) confidence += 0.1f
        
        // Decrease confidence if courses have poor relevance scores
        val avgRelevanceScore = aiResponse.recommendedCourses
            .map { it.relevanceScore }
            .average()
        
        if (avgRelevanceScore >= 8) confidence += 0.1f
        else if (avgRelevanceScore < 6) confidence -= 0.1f
        
        return confidence.coerceIn(0.1f, 1.0f)
    }
    
    /**
     * Create fallback suggestions when AI parsing fails.
     */
    private fun createFallbackSuggestions(
        request: CourseSuggestionRequest,
        availableCourses: List<Course>
    ): CourseSuggestionResult {
        
        // Simple keyword matching for fallback
        val keywords = CourseSuggestionPromptTemplates.extractLearningKeywords(request.userQuery)
        
        val matchingCourses = availableCourses.filter { course ->
            keywords.any { keyword ->
                course.title.contains(keyword, ignoreCase = true) ||
                course.description.contains(keyword, ignoreCase = true) ||
                course.subject.name.contains(keyword, ignoreCase = true)
            }
        }.take(request.maxRecommendations)
        
        val fallbackRecommendations = matchingCourses.map { course ->
            com.example.ai.edge.eliza.core.model.CourseRecommendation(
                courseId = course.id,
                courseTitle = course.title,
                subject = course.subject.name,
                grade = course.grade,
                description = course.description,
                relevanceReason = "Matches keywords from your query: ${keywords.joinToString(", ")}",
                recommendedChapters = course.chapters.take(3).map { chapter ->
                    com.example.ai.edge.eliza.core.model.ChapterRecommendation(
                        chapterId = chapter.id,
                        chapterTitle = chapter.title,
                        chapterNumber = chapter.chapterNumber,
                        relevanceReason = "Foundation chapter for this subject",
                        keyTopics = listOf("Basic concepts", "Fundamentals"),
                        estimatedReadingTime = chapter.estimatedReadingTime,
                        isCompleted = chapter.isCompleted
                    )
                },
                totalChapters = course.totalChapters,
                estimatedCompletionHours = course.estimatedHours,
                difficultyLevel = "Suitable for your level",
                prerequisites = emptyList(),
                learningOutcomes = listOf("Build strong foundation", "Practical understanding")
            )
        }
        
        return CourseSuggestionResult(
            query = request.userQuery,
            recommendations = fallbackRecommendations,
            reasoning = "Based on keyword matching from available courses. For more personalized recommendations, try describing your goals in more detail.",
            studyPlan = "Start with the first recommended course and progress through the chapters sequentially.",
            alternativeTopics = keywords,
            totalEstimatedHours = fallbackRecommendations.sumOf { it.estimatedCompletionHours },
            confidence = 0.4f // Lower confidence for fallback
        )
    }
}