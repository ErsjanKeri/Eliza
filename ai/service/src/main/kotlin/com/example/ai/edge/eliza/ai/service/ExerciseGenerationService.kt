package com.example.ai.edge.eliza.ai.service

import android.util.Log
import com.example.ai.edge.eliza.ai.modelmanager.LlmChatModelHelper
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.ExerciseGenerationRequest
import com.example.ai.edge.eliza.core.model.GenerationResult
import com.example.ai.edge.eliza.core.model.Model
import com.example.ai.edge.eliza.core.model.RelativeDifficulty
import com.example.ai.edge.eliza.core.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Service for AI-powered exercise generation using existing infrastructure.
 * Integrates with RagProviderFactory, LlmChatModelHelper, and CourseRepository following proper architecture.
 * Uses Singleton scope with proper RAG enhancement via RagProviderFactory.
 */
@Singleton
class ExerciseGenerationService @Inject constructor(
    private val courseRepository: CourseRepository, // Reuse existing repository
    private val userPreferencesRepository: UserPreferencesRepository, // For user language
    private val responseParser: ExerciseResponseParser,
    private val ragProviderFactory: RagProviderFactory // Proper Singleton-scoped RAG integration
) {
    
    companion object {
        private const val TAG = "ExerciseGenerationService"
    }
    
    /**
     * Generate a new trial question based on an existing exercise.
     * Uses existing CourseRepository infrastructure for persistence.
     */
    suspend fun generateTrialQuestion(
        originalExercise: Exercise,
        difficulty: RelativeDifficulty,
        model: Model
    ): Flow<GenerationResult> = flow {
        
        Log.d(TAG, "Starting generation for exercise: ${originalExercise.id}")
        emit(GenerationResult.Loading("Eliza is getting ready..."))
        
        try {
            // Step 1: Extract concept and get RAG context
            val userLanguage = userPreferencesRepository.getCurrentLanguage()
            val conceptFocus = ExercisePromptTemplates.extractConceptFocus(originalExercise, userLanguage)
            
            // Step 2: Get chapter and course data for proper ChatContext
            val chapter = courseRepository.getChapterById(originalExercise.chapterId).firstOrNull() 
                ?: throw Exception("Chapter not found: ${originalExercise.chapterId}")
            val course = courseRepository.getCourseById(chapter.courseId).firstOrNull() 
                ?: throw Exception("Course not found: ${chapter.courseId}")
            
            // Step 3: Create proper ChatContext for RAG enhancement
            val chatContext = ChatContext.createExerciseSolving(
                course = course,
                chapter = chapter,
                exercise = originalExercise,
                language = userLanguage,
                userAnswer = "Generating practice question",
                isTestQuestion = false
            )
            
            // Step 4: Create generation request
            val request = ExerciseGenerationRequest(
                originalExercise = originalExercise,
                selectedDifficulty = difficulty,
                chapterContext = null, // Will be provided via RAG enhancement
                conceptFocus = conceptFocus
            )
            
            // Step 5: Generate prompt
            val prompt = ExercisePromptTemplates.createGenerationPrompt(request, userLanguage)
            
            Log.d(TAG, "Generated prompt length: ${prompt.length}")
            
            // Step 6: Use RagProviderFactory + LlmChatModelHelper for proper Singleton-scoped RAG enhancement
            
            // Get appropriate RAG provider based on context
            val ragProvider = ragProviderFactory.createProvider(chatContext)
            
            // Enhance prompt with relevant content
            val relevantContent = ragProvider.getRelevantContent(
                query = prompt,
                context = chatContext,
                maxChunks = 5
            )
            
            // Build enhanced prompt with RAG context
            val enhancedPrompt = if (relevantContent.isNotEmpty()) {
                val ragContext = relevantContent.joinToString("\n") { 
                    "Context: ${it.title}\n${it.content}" 
                }
                "$ragContext\n\nGeneration Request:\n$prompt"
            } else {
                prompt
            }
            
            Log.d(TAG, "Enhanced prompt length: ${enhancedPrompt.length}, RAG chunks: ${relevantContent.size}")
            
            // Step 7: Use LlmChatModelHelper for direct model inference  
            emit(GenerationResult.Loading("Exercise is being prepared..."))
            
            val aiResponse = suspendCancellableCoroutine<String> { continuation ->
                var fullResponse = ""
                var completed = false
                
                LlmChatModelHelper.runInference(
                    model = model,
                    input = enhancedPrompt,
                    resultListener = { partial, done ->
                        fullResponse += partial
                        if (done && !completed) {
                            completed = true
                            continuation.resume(fullResponse)
                        }
                    },
                    cleanUpListener = {
                        Log.d(TAG, "AI inference cleanup completed")
                    }
                )
                
                continuation.invokeOnCancellation {
                    Log.d(TAG, "AI generation was cancelled")
                }
            }
            
            Log.d(TAG, "RAG-enhanced AI response received, length: ${aiResponse.length}")
            
            // Step 8: Process the AI response
            val result = processGenerationResponse(aiResponse, request, userLanguage)
            when (result) {
                is GenerationResult.Success -> {
                    // Save to database using existing repository
                    try {
                        courseRepository.insertTrial(result.trial)
                        Log.d(TAG, "Trial saved successfully: ${result.trial.id}")
                        emit(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save trial", e)
                        emit(GenerationResult.Error("Failed to save generated question"))
                    }
                }
                is GenerationResult.Error -> {
                    emit(result)
                }
                else -> {
                    // Shouldn't happen in this flow
                    emit(GenerationResult.Error("Unexpected generation state"))
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Generation process failed", e)
            emit(GenerationResult.Error("Generation failed: ${e.message}"))
        }
    }
    
    /**
     * Process the AI response and create a Trial object.
     */
    private suspend fun processGenerationResponse(
        aiResponse: String,
        request: ExerciseGenerationRequest,
        userLanguage: SupportedLanguage
    ): GenerationResult {
        
        return try {
            Log.d(TAG, "Processing AI response length: ${aiResponse.length}")
            
            // Parse the AI response
            val parsedResponse = responseParser.parseGeneratedExercise(aiResponse)
                ?: return GenerationResult.Error("Failed to parse AI response - invalid JSON format")
            
            // Additional quality validation
            if (!isQualityQuestion(parsedResponse.questionText, request.originalExercise.questionText.get(userLanguage))) {
                return GenerationResult.Error("Generated question quality too low")
            }
            
            // Convert to Trial using existing data model
            val trial = parsedResponse.toTrial(request.originalExercise.id)
            
            Log.d(TAG, "Successfully created trial: ${trial.questionText.get(userLanguage).take(50)}...")
            GenerationResult.Success(trial)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process generation response", e)
            GenerationResult.Error("Failed to create question: ${e.message}")
        }
    }
    
    /**
     * Validate if the generated question meets simple quality standards.
     */
    private fun isQualityQuestion(generated: String, original: String): Boolean {
        // Basic quality checks
        if (generated.equals(original, ignoreCase = true)) return false
        
        // Check for common AI hallucination patterns
        val lowQualityPatterns = listOf(
            "i cannot", "i can't", "sorry", "as an ai",
            "generated question", "example question",
            "[placeholder]", "xxx", "..."
        )
        
        val generatedLower = generated.lowercase()
        if (lowQualityPatterns.any { generatedLower.contains(it) }) {
            return false
        }
        
        return true
    }
    
    /**
     * Get existing trials for an exercise using CourseRepository.
     */
    suspend fun getExistingTrials(exerciseId: String) = 
        courseRepository.getTrialsByExercise(exerciseId)
    
    /**
     * Submit an answer for a trial question using existing infrastructure.
     */
    suspend fun submitTrialAnswer(trialId: String, answerIndex: Int) = 
        courseRepository.submitTrialAnswer(trialId, answerIndex)
}