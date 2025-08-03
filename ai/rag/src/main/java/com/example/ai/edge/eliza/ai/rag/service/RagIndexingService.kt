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
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkDao
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkEntity
import com.example.ai.edge.eliza.ai.rag.data.VectorIndexMetadata
import com.example.ai.edge.eliza.ai.rag.data.VectorIndexMetadataDao
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for indexing content and creating vector embeddings for RAG.
 * Handles automatic indexing of mock data and chapter content.
 */
@Singleton
class RagIndexingService @Inject constructor(
    private val courseRepository: CourseRepository,
    private val textEmbeddingService: TextEmbeddingService,
    private val contentChunkingService: ContentChunkingService,
    private val contentChunkDao: ContentChunkDao,
    private val vectorIndexMetadataDao: VectorIndexMetadataDao
) {
    
    companion object {
        private const val TAG = "RagIndexingService"
        private const val BATCH_SIZE = 10 // Process embeddings in batches
        private const val INDEX_VERSION = 1
    }
    
    /**
     * Index a single chapter's content.
     */
    suspend fun indexChapter(chapterId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting indexing for chapter: $chapterId")
            
            // Check if already indexed
            val existingMetadata = vectorIndexMetadataDao.getMetadata(chapterId, "chapter")
            if (existingMetadata != null && existingMetadata.indexVersion >= INDEX_VERSION) {
                Log.d(TAG, "Chapter $chapterId already indexed with current version")
                return@withContext true
            }
            
            // Get chapter from repository
            val chapter = courseRepository.getChapterById(chapterId).firstOrNull()
            if (chapter == null) {
                Log.w(TAG, "Chapter not found: $chapterId")
                return@withContext false
            }
            
            // Initialize text embedding service
            if (!textEmbeddingService.initialize()) {
                Log.e(TAG, "Failed to initialize text embedding service")
                return@withContext false
            }
            
            Log.d(TAG, "Chunking chapter content: ${chapter.title}")
            
            // Chunk the chapter content
            val chunks = contentChunkingService.chunkChapterContent(
                chapterId = chapterId,
                courseId = chapter.courseId,
                chapterTitle = chapter.title,
                markdownContent = chapter.markdownContent,
                chapterNumber = chapter.chapterNumber
            )
            
            if (chunks.isEmpty()) {
                Log.w(TAG, "No chunks created for chapter: $chapterId")
                return@withContext false
            }
            
            Log.d(TAG, "Created ${chunks.size} chunks, generating embeddings...")
            
            // Generate embeddings for chunks in batches
            val chunksWithEmbeddings = generateEmbeddingsForChunks(chunks)
            
            // Save chunks to database
            contentChunkDao.deleteChunksByChapter(chapterId) // Remove old chunks
            contentChunkDao.insertChunks(chunksWithEmbeddings)
            
            // Update metadata
            val metadata = VectorIndexMetadata(
                id = "chapter_$chapterId",
                indexType = "chapter",
                targetId = chapterId,
                chunkCount = chunksWithEmbeddings.size,
                embeddingDimension = chunksWithEmbeddings.firstOrNull()?.embedding?.size ?: 0,
                lastIndexedAt = System.currentTimeMillis(),
                indexVersion = INDEX_VERSION
            )
            vectorIndexMetadataDao.insertMetadata(metadata)
            
            Log.d(TAG, "Successfully indexed chapter $chapterId with ${chunksWithEmbeddings.size} chunks")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error indexing chapter $chapterId", e)
            false
        }
    }
    
    /**
     * Index all chapters in a course.
     */
    suspend fun indexCourse(courseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting indexing for course: $courseId")
            
            val course = courseRepository.getCourseById(courseId).firstOrNull()
            if (course == null) {
                Log.w(TAG, "Course not found: $courseId")
                return@withContext false
            }
            
            var successCount = 0
            val totalChapters = course.chapters.size
            
            for (chapter in course.chapters) {
                if (indexChapter(chapter.id)) {
                    successCount++
                }
            }
            
            Log.d(TAG, "Indexed $successCount out of $totalChapters chapters for course $courseId")
            
            // Update course-level metadata
            if (successCount > 0) {
                val metadata = VectorIndexMetadata(
                    id = "course_$courseId",
                    indexType = "course",
                    targetId = courseId,
                    chunkCount = contentChunkDao.getChunksByCourse(courseId).size,
                    embeddingDimension = 0, // Will be set from first chunk
                    lastIndexedAt = System.currentTimeMillis(),
                    indexVersion = INDEX_VERSION
                )
                vectorIndexMetadataDao.insertMetadata(metadata)
            }
            
            successCount == totalChapters
            
        } catch (e: Exception) {
            Log.e(TAG, "Error indexing course $courseId", e)
            false
        }
    }
    
    /**
     * Index all available content from mock data.
     * This should be called when the app starts to ensure mock data is indexed.
     */
    suspend fun indexAllMockData(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to index all mock data...")
            
            val courses = courseRepository.getAllCourses().firstOrNull() ?: emptyList()
            if (courses.isEmpty()) {
                Log.w(TAG, "No courses found in mock data")
                return@withContext false
            }
            
            var successCount = 0
            for (course in courses) {
                if (indexCourse(course.id)) {
                    successCount++
                }
            }
            
            Log.d(TAG, "Successfully indexed $successCount out of ${courses.size} courses")
            successCount == courses.size
            
        } catch (e: Exception) {
            Log.e(TAG, "Error indexing all mock data", e)
            false
        }
    }
    
    /**
     * Check if content is already indexed.
     */
    suspend fun isContentIndexed(targetId: String, indexType: String): Boolean {
        return try {
            val metadata = vectorIndexMetadataDao.getMetadata(targetId, indexType)
            metadata != null && metadata.indexVersion >= INDEX_VERSION
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if content is indexed", e)
            false
        }
    }
    
    /**
     * Get indexing statistics.
     */
    suspend fun getIndexingStats(): IndexingStats = withContext(Dispatchers.IO) {
        try {
            val allMetadata = vectorIndexMetadataDao.getAllMetadata()
            val totalChunks = contentChunkDao.getRecentChunks(Int.MAX_VALUE).size
            
            IndexingStats(
                totalCourses = allMetadata.count { it.indexType == "course" },
                totalChapters = allMetadata.count { it.indexType == "chapter" },
                totalChunks = totalChunks,
                lastIndexedAt = allMetadata.maxOfOrNull { it.lastIndexedAt } ?: 0L,
                embeddingDimension = allMetadata.firstOrNull()?.embeddingDimension ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting indexing stats", e)
            IndexingStats()
        }
    }
    
    /**
     * Re-index content (useful for updates or schema changes).
     */
    suspend fun reindexAll(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting complete re-indexing...")
            
            // Clear existing data
            val allMetadata = vectorIndexMetadataDao.getAllMetadata()
            for (metadata in allMetadata) {
                when (metadata.indexType) {
                    "chapter" -> contentChunkDao.deleteChunksByChapter(metadata.targetId)
                    "course" -> contentChunkDao.deleteChunksByCourse(metadata.targetId)
                }
                vectorIndexMetadataDao.deleteMetadata(metadata.targetId, metadata.indexType)
            }
            
            // Re-index everything
            indexAllMockData()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during re-indexing", e)
            false
        }
    }
    
    /**
     * Generate embeddings for a list of chunks in batches.
     */
    private suspend fun generateEmbeddingsForChunks(chunks: List<ContentChunkEntity>): List<ContentChunkEntity> {
        val chunksWithEmbeddings = mutableListOf<ContentChunkEntity>()
        
        for (i in chunks.indices step BATCH_SIZE) {
            val batch = chunks.subList(i, minOf(i + BATCH_SIZE, chunks.size))
            val batchTexts = batch.map { "${it.title}\n\n${it.content}" }
            
            Log.d(TAG, "Processing embedding batch ${i / BATCH_SIZE + 1} (${batch.size} chunks)")
            
            val embeddings = textEmbeddingService.embedTexts(batchTexts)
            
            for (j in batch.indices) {
                val embedding = embeddings[j]
                if (embedding != null) {
                    chunksWithEmbeddings.add(batch[j].copy(embedding = embedding))
                } else {
                    Log.w(TAG, "Failed to create embedding for chunk: ${batch[j].id}")
                    // Add chunk with empty embedding as fallback
                    chunksWithEmbeddings.add(batch[j])
                }
            }
        }
        
        return chunksWithEmbeddings
    }
    
    /**
     * Data class for indexing statistics.
     */
    data class IndexingStats(
        val totalCourses: Int = 0,
        val totalChapters: Int = 0,
        val totalChunks: Int = 0,
        val lastIndexedAt: Long = 0L,
        val embeddingDimension: Int = 0
    )
}