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

package com.example.ai.edge.eliza.ai.rag.service

import android.util.Log
import com.example.ai.edge.eliza.ai.rag.RagProvider
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkDao
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkEntity
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
 * Enhanced RAG provider using vector-based semantic similarity search.
 * Replaces the basic string-based RAG with intelligent context retrieval.
 */
@Singleton
class EnhancedRagProvider @Inject constructor(
    private val textEmbeddingService: TextEmbeddingService,
    private val contentChunkDao: ContentChunkDao,
    private val ragIndexingService: RagIndexingService,
    private val courseRepository: CourseRepository,
    private val systemInstructionProvider: com.example.ai.edge.eliza.ai.rag.SystemInstructionProvider
) : RagProvider {
    
    companion object {
        private const val TAG = "EnhancedRagProvider"
        private const val SIMILARITY_THRESHOLD = 0.7f
        private const val DEFAULT_MAX_CHUNKS = 3
        private const val MAX_CONTEXT_LENGTH = 2000 // Characters for context
    }
    
    /**
     * Retrieve relevant content using vector similarity search.
     */
    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Getting relevant content for query: ${query.take(50)}...")
            
            // Ensure text embedding service is initialized
            if (!textEmbeddingService.initialize()) {
                Log.w(TAG, "Text embedding service not available, falling back to basic RAG")
                return getBasicContent(context, maxChunks)
            }
            
            // Create embedding for the query
            val queryEmbedding = textEmbeddingService.embedText(query)
            if (queryEmbedding == null) {
                Log.w(TAG, "Failed to create query embedding, falling back to basic RAG")
                return getBasicContent(context, maxChunks)
            }
            
            // Get candidate chunks based on context
            val candidateChunks = getCandidateChunks(context)
            if (candidateChunks.isEmpty()) {
                Log.w(TAG, "No indexed content found for context, attempting to index")
                indexContextContent(context)
                return getBasicContent(context, maxChunks)
            }
            
            // Find most similar chunks
            val similarChunks = findSimilarChunks(queryEmbedding, candidateChunks, maxChunks)
            
            Log.d(TAG, "Found ${similarChunks.size} relevant chunks in ${System.currentTimeMillis() - startTime}ms")
            
            return similarChunks
            
        } catch (e: Exception) {
            Log.e(TAG, "Enhanced RAG vector search failed - falling back to basic content", e)
            // Return basic content but with metadata indicating fallback occurred
            val basicContent = getBasicContent(context, maxChunks)
            return basicContent.map { chunk ->
                chunk.copy(
                    metadata = chunk.metadata + mapOf(
                        "fallback_reason" to "vector_search_failed",
                        "original_error" to e.message.orEmpty(),
                        "enhanced_rag_status" to "fallback_to_basic"
                    )
                )
            }
        }
    }
    
    /**
     * Enhanced prompt creation with vector-retrieved context.
     */
    override suspend fun enhancePrompt(
        prompt: String,
        context: ChatContext
    ): PromptEnhancementResult {
        val startTime = System.currentTimeMillis()
        val chunks = getRelevantContent(prompt, context, DEFAULT_MAX_CHUNKS)
        val systemInstructions = getSystemInstructions(context)
        
        val enhancedPrompt = EnhancedPrompt(
            originalPrompt = prompt,
            enhancedPrompt = buildVectorEnhancedPrompt(prompt, chunks, context),
            context = context,
            retrievedChunks = chunks,
            systemInstructions = systemInstructions
        )
        
        // Calculate confidence based on chunk relevance and quantity
        val confidence = calculateConfidence(chunks)
        
        return PromptEnhancementResult(
            enhancedPrompt = enhancedPrompt,
            confidence = confidence,
            processingTime = System.currentTimeMillis() - startTime,
            chunksUsed = chunks.size
        )
    }
    
    /**
     * Get context-aware system instructions.
     */
    override suspend fun getSystemInstructions(context: ChatContext): String {
        return systemInstructionProvider.getSystemInstructions(context, isEnhancedRag = true)
    }
    
    /**
     * Get candidate chunks based on chat context.
     * Enhanced RAG searches ALL indexed content, not just current chapter.
     */
    private suspend fun getCandidateChunks(context: ChatContext): List<ContentChunkEntity> {
        return when (context) {
            is ChatContext.ChapterReading -> {
                // 🔍 ENHANCED RAG: Search ALL chunks across ALL courses/chapters
                Log.d(TAG, "Enhanced RAG: Searching ALL indexed content for relevant chunks")
                contentChunkDao.getAllChunks()
            }
            is ChatContext.ExerciseSolving -> {
                // 🔍 ENHANCED RAG: Search ALL chunks across ALL courses/chapters
                Log.d(TAG, "Enhanced RAG: Searching ALL indexed content for exercise help")
                contentChunkDao.getAllChunks()
            }
            is ChatContext.Revision -> {
                // For revision, search completed chapters + broader context
                val revisionChunks = mutableListOf<ContentChunkEntity>()
                context.completedChapterIds.forEach { chapterId ->
                    revisionChunks.addAll(contentChunkDao.getChunksByChapter(chapterId))
                }
                
                // Add broader context from all courses for better revision support
                val allChunks = contentChunkDao.getAllChunks()
                revisionChunks.addAll(allChunks.filter { chunk ->
                    !context.completedChapterIds.contains(chunk.chapterId)
                })
                
                revisionChunks
            }
            is ChatContext.GeneralTutoring -> {
                // 🔍 ENHANCED RAG: For general tutoring, search entire knowledge base
                Log.d(TAG, "Enhanced RAG: Searching ALL indexed content for general tutoring")
                contentChunkDao.getAllChunks()
            }
            is ChatContext.CourseSuggestion -> {
                // 🔍 ENHANCED RAG: For course suggestions, search ALL indexed content for best recommendations
                Log.d(TAG, "Enhanced RAG: Searching ALL indexed content for course suggestions")
                contentChunkDao.getAllChunks()
            }
        }
    }
    
    /**
     * Find chunks most similar to the query embedding using multi-vector retrieval strategy.
     * Prioritizes summaries for general queries and details for specific queries.
     */
    private suspend fun findSimilarChunks(
        queryEmbedding: FloatArray,
        candidateChunks: List<ContentChunkEntity>,
        maxChunks: Int
    ): List<ContentChunk> {
        // Separate summary and detail chunks
        val summaryChunks = candidateChunks.filter { 
            it.chunkType == ContentChunkType.SUMMARY.name 
        }
        val detailChunks = candidateChunks.filter { 
            it.chunkType != ContentChunkType.SUMMARY.name 
        }
        
        // Calculate similarities for all chunks
        val summarySimilarities = summaryChunks
            .filter { it.embedding.isNotEmpty() }
            .map { chunk ->
                val similarity = textEmbeddingService.cosineSimilarity(queryEmbedding, chunk.embedding)
                chunk to similarity
            }
            .filter { it.second >= SIMILARITY_THRESHOLD }
        
        val detailSimilarities = detailChunks
            .filter { it.embedding.isNotEmpty() }
            .map { chunk ->
                val similarity = textEmbeddingService.cosineSimilarity(queryEmbedding, chunk.embedding)
                chunk to similarity
            }
            .filter { it.second >= SIMILARITY_THRESHOLD }
        
        // Multi-vector strategy: combine summaries and details intelligently
        val selectedChunks = mutableListOf<Pair<ContentChunkEntity, Float>>()
        
        // Always include the best summary for context
        summarySimilarities
            .sortedByDescending { it.second }
            .take(1)
            .let { selectedChunks.addAll(it) }
        
        // Fill remaining slots with best detail chunks
        val remainingSlots = maxChunks - selectedChunks.size
        if (remainingSlots > 0) {
            detailSimilarities
                .sortedByDescending { it.second }
                .take(remainingSlots)
                .let { selectedChunks.addAll(it) }
        }
        
        // If no good detail chunks, add more summaries
        if (selectedChunks.size < maxChunks) {
            val additionalSummaries = maxChunks - selectedChunks.size
            summarySimilarities
                .sortedByDescending { it.second }
                .drop(1) // Skip the first one we already added
                .take(additionalSummaries)
                .let { selectedChunks.addAll(it) }
        }
        
        Log.d(TAG, "Multi-vector retrieval: ${selectedChunks.count { it.first.chunkType == ContentChunkType.SUMMARY.name }} summaries, ${selectedChunks.count { it.first.chunkType != ContentChunkType.SUMMARY.name }} details")
        
        return selectedChunks
            .sortedByDescending { it.second } // Sort by similarity
            .map { (chunkEntity, similarity) ->
                ContentChunk(
                    id = chunkEntity.id,
                    title = chunkEntity.title,
                    content = chunkEntity.content,
                    source = chunkEntity.source,
                    relevanceScore = similarity,
                    chunkType = ContentChunkType.valueOf(chunkEntity.chunkType),
                    metadata = chunkEntity.metadata + mapOf(
                        "similarity" to similarity.toString(),
                        "retrieval_strategy" to "multi_vector"
                    )
                )
            }
    }
    
    /**
     * Build an enhanced prompt with vector-retrieved context.
     */
    private fun buildVectorEnhancedPrompt(
        originalPrompt: String,
        chunks: List<ContentChunk>,
        context: ChatContext
    ): String {
        val contextBuilder = StringBuilder()
        
        // Add exercise-specific context first if this is an exercise
        if (context is ChatContext.ExerciseSolving) {
            contextBuilder.append("# EXERCISE CONTEXT\n\n")
            contextBuilder.append("**Course**: ${context.courseTitle} (${context.courseSubject})\n")
            contextBuilder.append("**Chapter**: ${context.chapterTitle}\n")
            contextBuilder.append("**Exercise**: #${context.exerciseNumber}\n\n")
            
            contextBuilder.append("## QUESTION\n")
            contextBuilder.append("${context.questionText}\n\n")
            
            if (context.options.isNotEmpty()) {
                contextBuilder.append("## MULTIPLE CHOICE OPTIONS\n")
                context.options.forEachIndexed { index, option ->
                    val label = when(index) {
                        0 -> "A"
                        1 -> "B" 
                        2 -> "C"
                        3 -> "D"
                        else -> "${index + 1}"
                    }
                    contextBuilder.append("$label) $option\n")
                }
                contextBuilder.append("\n")
            }
            
            if (context.correctAnswerIndex != null && context.options.isNotEmpty()) {
                val correctLabel = when(context.correctAnswerIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C" 
                    3 -> "D"
                    else -> "${context.correctAnswerIndex!! + 1}"
                }
                val correctAnswer = context.options.getOrNull(context.correctAnswerIndex!!) ?: ""
                contextBuilder.append("## CORRECT ANSWER\n")
                contextBuilder.append("$correctLabel) $correctAnswer\n\n")
            }
            
            if (context.userAnswerIndex != null && context.options.isNotEmpty()) {
                val userLabel = when(context.userAnswerIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> "${context.userAnswerIndex!! + 1}"
                }
                val userAnswer = context.options.getOrNull(context.userAnswerIndex!!) ?: ""
                contextBuilder.append("## STUDENT'S ANSWER\n")
                contextBuilder.append("$userLabel) $userAnswer\n\n")
                
                // Add performance context
                contextBuilder.append("**Attempts made**: ${context.attempts}\n")
                contextBuilder.append("**Hints used**: ${context.hintsUsed}\n\n")
            }
        }
        
        // Add vector-retrieved context from chapter content
        contextBuilder.append("# RELEVANT CHAPTER CONTENT\n\n")
        
        if (chunks.isEmpty()) {
            contextBuilder.append("No additional context found from chapter content.\n\n")
        } else {
            chunks.forEachIndexed { index, chunk ->
                contextBuilder.append("## Context ${index + 1}: ${chunk.title}\n")
                contextBuilder.append("**Source**: ${chunk.source}\n")
                contextBuilder.append("**Relevance**: ${(chunk.relevanceScore * 100).toInt()}%\n\n")
                
                // Truncate content if too long
                val content = if (chunk.content.length > MAX_CONTEXT_LENGTH / chunks.size) {
                    chunk.content.take(MAX_CONTEXT_LENGTH / chunks.size) + "..."
                } else {
                    chunk.content
                }
                contextBuilder.append("$content\n\n")
            }
        }
        
        // Add the original question
        contextBuilder.append("# STUDENT QUESTION\n")
        contextBuilder.append(originalPrompt)
        
        return contextBuilder.toString()
    }
    
    /**
     * Calculate confidence score based on retrieved chunks.
     * Considers whether content was retrieved via vector search or fallback.
     */
    private fun calculateConfidence(chunks: List<ContentChunk>): Float {
        if (chunks.isEmpty()) return 0.1f // Very low confidence for no content
        
        // Check if any chunks indicate a fallback occurred
        val hasFallback = chunks.any { chunk ->
            chunk.metadata["enhanced_rag_status"] == "fallback_to_basic"
        }
        
        if (hasFallback) {
            Log.w(TAG, "Enhanced RAG fallback detected - lowering confidence score")
            return 0.3f // Lower confidence to indicate fallback occurred
        }
        
        // Normal confidence calculation for successful vector search
        val avgRelevance = chunks.map { it.relevanceScore }.average().toFloat()
        val chunkCountFactor = (chunks.size.toFloat() / DEFAULT_MAX_CHUNKS).coerceAtMost(1f)
        
        return (avgRelevance * 0.7f + chunkCountFactor * 0.3f).coerceIn(0.4f, 1f) // Minimum 0.4f for successful retrieval
    }
    
    /**
     * Fallback to basic content when vector search is not available.
     */
    private suspend fun getBasicContent(context: ChatContext, maxChunks: Int): List<ContentChunk> {
        return when (context) {
            is ChatContext.ChapterReading -> {
                val chapter = courseRepository.getChapterById(context.chapterId).firstOrNull()
                chapter?.let { 
                    listOf(
                        ContentChunk(
                            id = "basic_${context.chapterId}",
                            title = context.chapterTitle,
                            content = chapter.markdownContent.take(MAX_CONTEXT_LENGTH),
                            source = "Chapter ${chapter.chapterNumber}",
                            relevanceScore = 0.8f,
                            chunkType = ContentChunkType.CHAPTER_SECTION
                        )
                    )
                } ?: emptyList()
            }
            else -> emptyList()
        }
    }
    
    /**
     * Trigger indexing for context content if not already indexed.
     */
    private suspend fun indexContextContent(context: ChatContext) {
        try {
            when (context) {
                is ChatContext.ChapterReading -> {
                    Log.d(TAG, "Triggering indexing for chapter: ${context.chapterId}")
                    ragIndexingService.indexChapter(context.chapterId)
                }
                is ChatContext.ExerciseSolving -> {
                    Log.d(TAG, "Triggering indexing for exercise chapter: ${context.chapterId}")
                    ragIndexingService.indexChapter(context.chapterId)
                }
                else -> {
                    Log.d(TAG, "Context type doesn't support automatic indexing")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering content indexing", e)
        }
    }
}