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

package com.example.ai.edge.eliza.ai.rag

import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.UserPreferencesRepository
import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.ContentChunk
import com.example.ai.edge.eliza.core.model.ContentChunkType
import com.example.ai.edge.eliza.core.model.EnhancedPrompt
import com.example.ai.edge.eliza.core.model.PromptEnhancementResult
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Interface for providing context-aware content retrieval and prompt enhancement.
 * This is the main interface for RAG (Retrieval Augmented Generation) functionality.
 */
interface RagProvider {
    /**
     * Retrieves relevant content chunks based on the user's query and current context.
     */
    suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int = 5
    ): List<ContentChunk>
    
    /**
     * Enhances a user prompt with relevant context information.
     */
    suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult
    
    /**
     * Gets contextual system instructions for the AI based on the current context.
     */
    suspend fun getSystemInstructions(context: ChatContext): String
}

/**
 * Factory for creating appropriate RagProvider instances based on context.
 */
interface RagProviderFactory {
    fun createProvider(context: ChatContext): RagProvider
    fun createEnhancedProvider(context: ChatContext): RagProvider
    fun createBasicProvider(context: ChatContext): RagProvider
    fun setUseEnhancedRag(enabled: Boolean)
    fun isEnhancedRagEnabled(): Boolean
} 

/**
 * RAG provider for chapter reading context.
 */
class ChapterRagProvider @Inject constructor(
    private val courseRepository: CourseRepository,
    private val systemInstructionProvider: SystemInstructionProvider
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.ChapterReading) {
            // Use the localized content from context instead of going back to repository
            context.markdownContent?.let { content ->
                listOf(
                    ContentChunk(
                        id = "chapter_${context.chapterId}",
                        title = context.chapterTitle,
                        content = content,
                        source = "Chapter ${context.chapterNumber}",
                        relevanceScore = 0.9f,
                        chunkType = ContentChunkType.CHAPTER_SECTION
                    )
                )
            } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        val startTime = System.currentTimeMillis()
        val chunks = getRelevantContent(prompt, context)
        val systemInstructions = getSystemInstructions(context)
        
        val enhancedPrompt = EnhancedPrompt(
            originalPrompt = prompt,
            enhancedPrompt = buildString {
                append("Based on the current chapter content:\n")
                chunks.forEach { chunk ->
                    append("${chunk.title}: ${chunk.content.take(200)}...\n")
                }
                append("\nUser question: $prompt")
            },
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        // Calculate confidence based on chapter content availability
        val confidence = if (chunks.isNotEmpty()) {
            if (context is ChatContext.ChapterReading && context.chapterTitle.isNotBlank()) 0.8f else 0.7f
        } else 0.5f
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = false)
    }
}

/**
 * RAG provider for revision context.
 */
class RevisionRagProvider @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.Revision) {
            // Get content from completed chapters for revision
            val chunks = mutableListOf<ContentChunk>()
            val userLanguage = userPreferencesRepository.getCurrentLanguage()
            context.completedChapterIds.take(maxChunks).forEach { chapterId ->
                val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
                chapter?.let {
                    chunks.add(
                        ContentChunk(
                            id = "revision_$chapterId",
                            title = chapter.title.get(userLanguage),
                            content = chapter.markdownContent.get(userLanguage).take(300),
                            source = "Chapter ${chapter.chapterNumber}",
                            relevanceScore = 0.7f,
                            chunkType = ContentChunkType.CHAPTER_SECTION
                        )
                    )
                }
            }
            chunks
        } else {
            emptyList()
        }
    }
    
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        val startTime = System.currentTimeMillis()
        val chunks = getRelevantContent(prompt, context)
        val systemInstructions = getSystemInstructions(context)
        
        val enhancedPrompt = EnhancedPrompt(
            originalPrompt = prompt,
            enhancedPrompt = buildString {
                append("Revision context - Previously studied topics:\n")
                chunks.forEach { chunk ->
                    append("- ${chunk.title}\n")
                }
                append("\nUser question: $prompt")
            },
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        // Calculate confidence based on available revision content
        val confidence = when {
            chunks.isEmpty() -> 0.4f
            chunks.size >= 3 -> 0.8f // Good coverage across multiple chapters
            chunks.size >= 2 -> 0.7f // Moderate coverage
            else -> 0.6f // Single chapter coverage
        }
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return """
            You are an AI tutor helping a student with revision.
            The student is reviewing previously learned content and may have questions.
            Focus on reinforcing understanding and addressing any weak areas.
            Provide practice problems and explanations to strengthen comprehension.
        """.trimIndent()
    }
}

/**
 * RAG provider for general tutoring context.
 */
class GeneralRagProvider @Inject constructor(
    private val systemInstructionProvider: SystemInstructionProvider
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.GeneralTutoring) {
            // For general tutoring, provide basic educational content
            listOf(
                ContentChunk(
                    id = "general_math",
                    title = "General Math Concepts",
                    content = "Basic mathematical principles and problem-solving strategies",
                    source = "General Knowledge",
                    relevanceScore = 0.5f,
                    chunkType = ContentChunkType.CONCEPT_OVERVIEW
                )
            )
        } else {
            emptyList()
        }
    }
    
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        val startTime = System.currentTimeMillis()
        val chunks = getRelevantContent(prompt, context)
        val systemInstructions = getSystemInstructions(context)
        
        val enhancedPrompt = EnhancedPrompt(
            originalPrompt = prompt,
            enhancedPrompt = "General tutoring question: $prompt",
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        // Calculate confidence based on content availability
        val confidence = if (chunks.isNotEmpty()) 0.6f else 0.4f // Lower confidence for general tutoring
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = false)
    }
}

/**
 * RAG provider for exercise solving context.
 */
class ExerciseRagProvider @Inject constructor(
    private val courseRepository: CourseRepository,
    private val systemInstructionProvider: SystemInstructionProvider
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.ExerciseSolving) {
            // Use the localized content from context instead of going back to repository
            context.chapterContent?.let { content ->
                listOf(
                    ContentChunk(
                        id = "exercise_${context.exerciseId}",
                        title = "Exercise Context",
                        content = content,
                        source = "Exercise from ${context.chapterTitle}",
                        relevanceScore = 0.9f,
                        chunkType = ContentChunkType.PRACTICE_PROBLEM
                    )
                )
            } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        val startTime = System.currentTimeMillis()
        val chunks = getRelevantContent(prompt, context)
        val systemInstructions = getSystemInstructions(context)
        
        val enhancedPrompt = EnhancedPrompt(
            originalPrompt = prompt,
            enhancedPrompt = buildString {
                append("EXERCISE CONTEXT:\n")
                if (context is ChatContext.ExerciseSolving) {
                    append("Course: ${context.courseTitle} (${context.courseSubject})\n")
                    append("Chapter: ${context.chapterTitle}\n")
                    append("Exercise #${context.exerciseNumber}\n\n")
                    
                    append("QUESTION: ${context.questionText}\n\n")
                    
                    if (context.options.isNotEmpty()) {
                        append("MULTIPLE CHOICE OPTIONS:\n")
                        context.options.forEachIndexed { index, option ->
                            val label = when(index) {
                                0 -> "A"
                                1 -> "B" 
                                2 -> "C"
                                3 -> "D"
                                else -> "${index + 1}"
                            }
                            append("$label) $option\n")
                        }
                        append("\n")
                    }
                    
                    if (context.correctAnswerIndex != null && context.correctAnswer != null) {
                        val correctLabel = when(context.correctAnswerIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C" 
                            3 -> "D"
                            else -> "${context.correctAnswerIndex!! + 1}"
                        }
                        append("CORRECT ANSWER: $correctLabel) ${context.correctAnswer}\n\n")
                    }
                    
                    if (context.userAnswerIndex != null && context.userAnswer != null) {
                        val userLabel = when(context.userAnswerIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D" 
                            else -> "${context.userAnswerIndex!! + 1}"
                        }
                        append("USER'S SELECTED ANSWER: $userLabel) ${context.userAnswer}\n")
                        
                        val isCorrect = context.userAnswerIndex == context.correctAnswerIndex
                        append("USER'S ANSWER STATUS: ${if (isCorrect) "CORRECT" else "INCORRECT"}\n\n")
                    }
                    
                    append("Exercise Context:\n")
                    append("- Exercise ID: ${context.exerciseId}\n")
                    append("- Difficulty: ${context.difficulty}\n")
                    append("- Attempts: ${context.attempts}\n")
                    append("- Hints used: ${context.hintsUsed}\n")
                    if (context.isTestQuestion) {
                        append("- Type: Test Question\n")
                    }
                    append("\n")
                }
                append("USER QUESTION: $prompt")
            },
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        // Calculate dynamic confidence based on context completeness
        val confidence = calculateExerciseConfidence(context)
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    /**
     * Calculate confidence score based on exercise context completeness.
     * Higher confidence for exercises with complete context information.
     */
    private fun calculateExerciseConfidence(context: ChatContext): Float {
        if (context !is ChatContext.ExerciseSolving) return 0.5f
        
        var confidence = 0.5f // Base confidence
        
        // Add confidence for available context components
        if (context.questionText.isNotBlank()) confidence += 0.1f
        if (context.options.isNotEmpty()) confidence += 0.1f
        if (context.correctAnswerIndex != null && context.correctAnswer != null) confidence += 0.15f
        if (context.userAnswerIndex != null && context.userAnswer != null) confidence += 0.1f
        if (context.chapterTitle.isNotBlank()) confidence += 0.05f
        
        // Bonus for complete exercise context (all fields present)
        val hasCompleteContext = context.questionText.isNotBlank() &&
                context.options.isNotEmpty() &&
                context.correctAnswerIndex != null &&
                context.userAnswerIndex != null
        
        if (hasCompleteContext) confidence += 0.1f
        
        return confidence.coerceIn(0.5f, 0.9f) // Range: 0.5-0.9 for basic RAG
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = false)
    }
}

/**
 * RAG provider for course suggestion context.
 * Provides comprehensive course and chapter information for AI-powered course recommendations.
 */
class CourseSuggestionRagProvider @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val systemInstructionProvider: SystemInstructionProvider
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.CourseSuggestion) {
            try {
                // Get all available courses for comprehensive course suggestions
                val allCourses = courseRepository.getAllCourses().firstOrNull() ?: emptyList()
                val userLanguage = userPreferencesRepository.getCurrentLanguage()
                
                // Create content chunks for each course with key information
                allCourses.take(maxChunks).map { course ->
                    val courseOverview = buildString {
                        appendLine("Course: ${course.title.get(userLanguage)}")
                        appendLine("Subject: ${course.subject.name}")
                        appendLine("Grade: ${course.grade}")
                        appendLine("Description: ${course.description.get(userLanguage)}")
                        appendLine("Chapters (${course.totalChapters} total):")
                        course.chapters.forEach { chapter ->
                            appendLine("- Chapter ${chapter.chapterNumber}: ${chapter.title.get(userLanguage)}")
                        }
                        appendLine("Estimated completion: ${course.estimatedHours} hours")
                    }
                    
                    ContentChunk(
                        id = "course_${course.id}",
                        title = course.title.get(userLanguage),
                        content = courseOverview,
                        source = "Course Overview",
                        relevanceScore = 0.8f, // High relevance for course discovery
                        chunkType = ContentChunkType.SUMMARY,
                        metadata = mapOf(
                            "courseId" to course.id,
                            "subject" to course.subject.name,
                            "grade" to course.grade,
                            "totalChapters" to course.totalChapters.toString(),
                            "estimatedHours" to course.estimatedHours.toString()
                        )
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        
        val startTime = System.currentTimeMillis()
        
        if (context !is ChatContext.CourseSuggestion) {
            return PromptEnhancementResult(
                enhancedPrompt = com.example.ai.edge.eliza.core.model.EnhancedPrompt(
                    originalPrompt = prompt,
                    enhancedPrompt = prompt,
                    context = context,
                    retrievedChunks = emptyList(),
                    systemInstructions = ""
                ),
                confidence = 0.3f,
                processingTime = System.currentTimeMillis() - startTime,
                chunksUsed = 0
            )
        }
        
        // Get comprehensive course content for recommendations
        val chunks = getRelevantContent(prompt, context, maxChunks = 10) // More courses for better suggestions
        
        // Build context with all available courses and chapters
        val contextText = if (chunks.isNotEmpty()) {
            buildString {
                appendLine("AVAILABLE COURSES AND CHAPTERS:")
                appendLine()
                chunks.forEach { chunk ->
                    appendLine(chunk.content)
                    appendLine()
                }
                appendLine("USER QUERY: ${context.userQuery}")
                if (context.userLevel != null) {
                    appendLine("USER LEVEL: ${context.userLevel}")
                }
                if (context.availableTimeHours != null) {
                    appendLine("AVAILABLE TIME: ${context.availableTimeHours} hours")
                }
                if (context.preferredSubjects.isNotEmpty()) {
                    appendLine("PREFERRED SUBJECTS: ${context.preferredSubjects.joinToString(", ")}")
                }
                if (context.completedCourses.isNotEmpty()) {
                    appendLine("COMPLETED COURSES: ${context.completedCourses.joinToString(", ")}")
                }
            }
        } else {
            "No course content available for recommendations."
        }
        
        val systemInstructions = systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = false)
        
        val enhancedPrompt = buildString {
            appendLine(systemInstructions)
            appendLine()
            appendLine(contextText)
            appendLine()
            appendLine("USER REQUEST: $prompt")
        }
        
        // Calculate confidence based on available content
        val confidence = when {
            chunks.isEmpty() -> 0.3f
            chunks.size >= 3 -> 0.8f // Good course coverage
            chunks.size >= 2 -> 0.7f // Moderate coverage  
            else -> 0.6f // Limited coverage
        }
        
        return PromptEnhancementResult(
            enhancedPrompt = com.example.ai.edge.eliza.core.model.EnhancedPrompt(
                originalPrompt = prompt,
                enhancedPrompt = enhancedPrompt,
                context = context,
                retrievedChunks = chunks,
                systemInstructions = systemInstructions
            ),
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = false)
    }
}

/**
 * Factory implementation for creating appropriate RAG providers.
 * Supports both basic and enhanced (vector-based) RAG providers with UI toggle.
 */
@Singleton
class RagProviderFactoryImpl @Inject constructor(
    private val chapterRagProvider: ChapterRagProvider,
    private val revisionRagProvider: RevisionRagProvider,
    private val generalRagProvider: GeneralRagProvider,
    private val exerciseRagProvider: ExerciseRagProvider,
    private val courseSuggestionRagProvider: CourseSuggestionRagProvider,
    private val enhancedRagProvider: com.example.ai.edge.eliza.ai.rag.service.EnhancedRagProvider
) : RagProviderFactory {
    
    private var useEnhancedRag = false // UI toggle state
    
    override fun createProvider(context: ChatContext): RagProvider {
        return if (useEnhancedRag) {
            createEnhancedProvider(context)
        } else {
            createBasicProvider(context)
        }
    }
    
    override fun createEnhancedProvider(context: ChatContext): RagProvider {
        // Enhanced provider works for all context types
        return enhancedRagProvider
    }
    
    override fun setUseEnhancedRag(enabled: Boolean) {
        useEnhancedRag = enabled
    }
    
    override fun isEnhancedRagEnabled(): Boolean {
        return useEnhancedRag
    }
    
    /**
     * Create basic (non-vector) RAG provider.
     */
    override fun createBasicProvider(context: ChatContext): RagProvider {
        return when (context) {
            is ChatContext.ChapterReading -> chapterRagProvider
            is ChatContext.Revision -> revisionRagProvider
            is ChatContext.GeneralTutoring -> generalRagProvider
            is ChatContext.ExerciseSolving -> exerciseRagProvider
            is ChatContext.CourseSuggestion -> courseSuggestionRagProvider
        }
    }
} 