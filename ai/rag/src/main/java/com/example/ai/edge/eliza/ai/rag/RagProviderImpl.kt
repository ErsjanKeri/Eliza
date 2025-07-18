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
        maxChunks: Int = 3
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
} 

/**
 * RAG provider for chapter reading context.
 */
class ChapterRagProvider @Inject constructor(
    private val courseRepository: CourseRepository
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.ChapterReading) {
            val lesson = courseRepository.getLessonById(context.lessonId).firstOrNull()
            lesson?.let { 
                listOf(
                    ContentChunk(
                        id = "chapter_${context.lessonId}",
                        title = context.chapterTitle,
                        content = lesson.markdownContent,
                        source = "Chapter ${lesson.lessonNumber}",
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
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = 0.8f,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return """
            You are an AI tutor helping a student with their current chapter reading.
            The student is reading a specific lesson and may ask questions about the content.
            Provide clear, educational explanations that build on the chapter content.
            Use examples and step-by-step explanations when helpful.
        """.trimIndent()
    }
}

/**
 * RAG provider for revision context.
 */
class RevisionRagProvider @Inject constructor(
    private val courseRepository: CourseRepository
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.Revision) {
            // Get content from completed lessons for revision
            val chunks = mutableListOf<ContentChunk>()
            context.completedLessonIds.take(maxChunks).forEach { lessonId ->
                val lesson = courseRepository.getLessonById(lessonId).firstOrNull()
                lesson?.let {
                    chunks.add(
                        ContentChunk(
                            id = "revision_$lessonId",
                            title = lesson.title,
                            content = lesson.markdownContent.take(300),
                            source = "Lesson ${lesson.lessonNumber}",
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
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = 0.7f,
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
class GeneralRagProvider @Inject constructor() : RagProvider {
    
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
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = 0.6f,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return """
            You are an AI tutor providing general math tutoring.
            The student may ask questions about various math topics.
            Provide clear, step-by-step explanations and examples.
            Be patient and encouraging, adapting to the student's level.
        """.trimIndent()
    }
}

/**
 * RAG provider for exercise solving context.
 */
class ExerciseRagProvider @Inject constructor(
    private val courseRepository: CourseRepository
) : RagProvider {
    
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return if (context is ChatContext.ExerciseSolving) {
            val lesson = courseRepository.getLessonById(context.lessonId).firstOrNull()
            lesson?.let {
                listOf(
                    ContentChunk(
                        id = "exercise_${context.exerciseId}",
                        title = "Exercise Context",
                        content = lesson.markdownContent,
                        source = "Exercise from ${lesson.title}",
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
                append("Exercise solving context:\n")
                if (context is ChatContext.ExerciseSolving) {
                    append("Exercise ID: ${context.exerciseId}\n")
                    append("Attempts: ${context.attempts}\n")
                    append("Hints used: ${context.hintsUsed}\n")
                }
                append("User question: $prompt")
            },
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = 0.8f,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return """
            You are an AI tutor helping a student solve an exercise.
            The student is working on a specific problem and may need guidance.
            Provide hints rather than direct answers when possible.
            Encourage problem-solving thinking and step-by-step approaches.
        """.trimIndent()
    }
}

/**
 * Factory implementation for creating appropriate RAG providers.
 */
@Singleton
class RagProviderFactoryImpl @Inject constructor(
    private val chapterRagProvider: ChapterRagProvider,
    private val revisionRagProvider: RevisionRagProvider,
    private val generalRagProvider: GeneralRagProvider,
    private val exerciseRagProvider: ExerciseRagProvider
) : RagProviderFactory {
    
    override fun createProvider(context: ChatContext): RagProvider {
        return when (context) {
            is ChatContext.ChapterReading -> chapterRagProvider
            is ChatContext.Revision -> revisionRagProvider
            is ChatContext.GeneralTutoring -> generalRagProvider
            is ChatContext.ExerciseSolving -> exerciseRagProvider
        }
    }
} 