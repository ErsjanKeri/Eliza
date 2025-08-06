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

package com.example.ai.edge.eliza.ai.rag.util

import android.util.Log
import com.example.ai.edge.eliza.ai.rag.service.RagIndexingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility to ensure all course content is properly indexed for RAG system.
 * CRITICAL FIX: Addresses issue where trigonometry and other courses weren't indexed,
 * causing AI to recommend external courses instead of internal ones.
 */
@Singleton
class RagContentIndexer @Inject constructor(
    private val ragIndexingService: RagIndexingService
) {
    
    companion object {
        private const val TAG = "RagContentIndexer"
        
        // Critical courses that MUST be indexed for course suggestions
        private val CRITICAL_COURSE_IDS = listOf(
            "course_trigonometry_1",  // Contains cos/sin identities that user needs
            "course_algebra_1",       // Algebra fundamentals
            "course_geometry_1",      // Geometry basics
            "course_calculus_1",      // Calculus introduction
            "course_statistics_1",    // Statistics
            "course_algebra_2"        // Advanced algebra
        )
    }
    
    /**
     * Force index all critical courses for course suggestions.
     * This ensures trigonometry, calculus, and other courses are available for RAG.
     */
    suspend fun ensureCriticalCoursesIndexed(): Boolean {
        Log.d(TAG, "🔍 CRITICAL FIX: Ensuring all courses are indexed for proper course suggestions")
        
        var successCount = 0
        
        for (courseId in CRITICAL_COURSE_IDS) {
            try {
                Log.d(TAG, "Checking/indexing critical course: $courseId")
                
                // Check if already indexed
                val isIndexed = ragIndexingService.isContentIndexed(courseId, "course")
                
                if (!isIndexed) {
                    Log.w(TAG, "⚠️ Course $courseId NOT indexed! Indexing now...")
                    val indexed = ragIndexingService.indexCourse(courseId)
                    if (indexed) {
                        Log.d(TAG, "✅ Successfully indexed $courseId")
                        successCount++
                    } else {
                        Log.e(TAG, "❌ Failed to index $courseId")
                    }
                } else {
                    Log.d(TAG, "✅ Course $courseId already indexed")
                    successCount++
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error indexing course $courseId", e)
            }
        }
        
        val stats = ragIndexingService.getIndexingStats()
        Log.d(TAG, "📊 Final indexing stats: ${stats.totalCourses} courses, ${stats.totalChapters} chapters, ${stats.totalChunks} chunks")
        
        if (successCount == CRITICAL_COURSE_IDS.size) {
            Log.d(TAG, "🎉 All critical courses successfully indexed!")
            return true
        } else {
            Log.w(TAG, "⚠️ Only $successCount out of ${CRITICAL_COURSE_IDS.size} critical courses indexed")
            return false
        }
    }
    
    /**
     * Force complete re-indexing of all content.
     * Use this if there are persistent indexing issues.
     */
    suspend fun forceReindexAll(): Boolean {
        Log.d(TAG, "🔄 FORCE RE-INDEX: Clearing and re-indexing ALL content")
        return ragIndexingService.reindexAll()
    }
    
    /**
     * Convenient method to run indexing in background scope.
     */
    fun ensureCriticalCoursesIndexedAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            ensureCriticalCoursesIndexed()
        }
    }
    
    /**
     * Check if trigonometry course specifically is indexed.
     * This is the main culprit causing external course recommendations.
     */
    suspend fun isTrigonometryIndexed(): Boolean {
        return ragIndexingService.isContentIndexed("course_trigonometry_1", "course")
    }
    
    /**
     * Index only the trigonometry course (quick fix for the immediate issue).
     */
    suspend fun indexTrigonometryOnly(): Boolean {
        Log.d(TAG, "🎯 QUICK FIX: Indexing trigonometry course only")
        return ragIndexingService.indexCourse("course_trigonometry_1")
    }
}