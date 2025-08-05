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
import com.example.ai.edge.eliza.ai.rag.util.RagContentIndexer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for initializing the RAG system on app startup.
 * Ensures mock data is indexed and ready for enhanced RAG functionality.
 */
@Singleton
class RagInitializationService @Inject constructor(
    private val textEmbeddingService: TextEmbeddingService,
    private val ragIndexingService: RagIndexingService,
    private val ragContentIndexer: RagContentIndexer
) {
    
    companion object {
        private const val TAG = "RagInitializationService"
    }
    
    private var isInitialized = false
    private var isInitializing = false
    
    /**
     * Initialize the RAG system asynchronously.
     * This should be called when the app starts.
     */
    fun initializeAsync() {
        if (isInitialized || isInitializing) {
            Log.d(TAG, "RAG system already initialized or initializing")
            return
        }
        
        isInitializing = true
        Log.d(TAG, "Starting RAG system initialization...")
        
        // Initialize in background to avoid blocking app startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initializeRagSystem()
            } catch (e: Exception) {
                Log.e(TAG, "Error during RAG initialization", e)
                isInitializing = false
            }
        }
    }
    
    /**
     * Initialize RAG system synchronously (for testing or specific cases).
     */
    suspend fun initializeSync(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "RAG system already initialized")
            return true
        }
        
        return try {
            initializeRagSystem()
        } catch (e: Exception) {
            Log.e(TAG, "Error during synchronous RAG initialization", e)
            false
        }
    }
    
    /**
     * Check if the RAG system is ready to use.
     */
    fun isRagReady(): Boolean = isInitialized
    
    /**
     * Check if the RAG system is currently initializing.
     */
    fun isRagInitializing(): Boolean = isInitializing
    
    /**
     * Get initialization status for UI display.
     */
    fun getInitializationStatus(): RagInitializationStatus {
        return when {
            isInitialized -> RagInitializationStatus.READY
            isInitializing -> RagInitializationStatus.INITIALIZING
            else -> RagInitializationStatus.NOT_STARTED
        }
    }
    
    /**
     * Force re-initialization (useful for development or after updates).
     */
    suspend fun forceReinitialize(): Boolean {
        Log.d(TAG, "Forcing RAG re-initialization...")
        isInitialized = false
        isInitializing = false
        return initializeSync()
    }
    
    /**
     * Perform the actual RAG system initialization.
     */
    private suspend fun initializeRagSystem(): Boolean {
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Step 1: Initializing text embedding service...")
            val embeddingInitialized = textEmbeddingService.initialize()
            if (!embeddingInitialized) {
                Log.w(TAG, "Text embedding service failed to initialize")
                // Continue anyway - system can work in basic mode
            }
            
            Log.d(TAG, "Step 2: Checking if mock data needs indexing...")
            val stats = ragIndexingService.getIndexingStats()
            
            if (stats.totalChunks == 0) {
                Log.d(TAG, "Step 3: Indexing mock data (first time)...")
                val indexingResult = ragIndexingService.indexAllMockData()
                if (indexingResult) {
                    Log.d(TAG, "Mock data indexing completed successfully")
                } else {
                    Log.w(TAG, "Mock data indexing had some failures")
                }
            } else {
                Log.d(TAG, "Step 3: Mock data already indexed (${stats.totalChunks} chunks)")
            }
            
            // CRITICAL FIX: Ensure all critical courses (especially trigonometry) are indexed
            Log.d(TAG, "Step 4: 🎯 CRITICAL FIX - Ensuring trigonometry and other critical courses are indexed...")
            val criticalCoursesIndexed = ragContentIndexer.ensureCriticalCoursesIndexed()
            if (criticalCoursesIndexed) {
                Log.d(TAG, "✅ All critical courses successfully indexed - Course suggestions will now work properly!")
            } else {
                Log.w(TAG, "⚠️ Some critical courses failed to index - Course suggestions may recommend external courses")
            }
            
            val finalStats = ragIndexingService.getIndexingStats()
            val endTime = System.currentTimeMillis()
            
            Log.d(TAG, "RAG initialization completed in ${endTime - startTime}ms")
            Log.d(TAG, "Final stats: ${finalStats.totalCourses} courses, ${finalStats.totalChapters} chapters, ${finalStats.totalChunks} chunks")
            
            isInitialized = true
            isInitializing = false
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "RAG initialization failed", e)
            isInitializing = false
            return false
        }
    }
    
    /**
     * Get detailed initialization information for debugging.
     */
    suspend fun getInitializationInfo(): RagInitializationInfo {
        val stats = ragIndexingService.getIndexingStats()
        val embeddingReady = try {
            textEmbeddingService.embedText("test") != null
        } catch (e: Exception) {
            false
        }
        
        return RagInitializationInfo(
            isInitialized = isInitialized,
            isInitializing = isInitializing,
            embeddingServiceReady = embeddingReady,
            totalCourses = stats.totalCourses,
            totalChapters = stats.totalChapters,
            totalChunks = stats.totalChunks,
            lastIndexedAt = stats.lastIndexedAt,
            embeddingDimension = stats.embeddingDimension
        )
    }
}

/**
 * Enumeration of RAG initialization states.
 */
enum class RagInitializationStatus {
    NOT_STARTED,
    INITIALIZING, 
    READY,
    FAILED
}

/**
 * Detailed information about RAG initialization state.
 */
data class RagInitializationInfo(
    val isInitialized: Boolean,
    val isInitializing: Boolean,
    val embeddingServiceReady: Boolean,
    val totalCourses: Int,
    val totalChapters: Int,
    val totalChunks: Int,
    val lastIndexedAt: Long,
    val embeddingDimension: Int
)