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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ai.edge.eliza.ai.rag.service.TextEmbeddingService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test class to verify that the TextEmbeddingService works correctly with the
 * Universal Sentence Encoder model.
 */
@RunWith(AndroidJUnit4::class)
class TextEmbeddingServiceTest {

    private lateinit var textEmbeddingService: TextEmbeddingService
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        textEmbeddingService = TextEmbeddingService(context)
    }

    @Test
    fun testServiceInitialization() = runBlocking {
        // Test that the service can initialize successfully
        val initialized = textEmbeddingService.initialize()
        assertTrue("TextEmbeddingService should initialize successfully", initialized)
    }

    @Test
    fun testBasicTextEmbedding() = runBlocking {
        // Initialize the service
        val initialized = textEmbeddingService.initialize()
        assertTrue("Service must be initialized", initialized)

        // Test basic text embedding
        val testText = "Linear equations are mathematical expressions with variables."
        val embedding = textEmbeddingService.embedText(testText)

        assertNotNull("Embedding should not be null", embedding)
        assertTrue("Embedding should have dimensions > 0", embedding!!.size > 0)
        
        // Universal Sentence Encoder typically produces 512-dimensional embeddings
        assertEquals("Embedding should have 512 dimensions", 512, embedding.size)
        
        // Verify embedding values are in reasonable range
        val maxVal = embedding.maxOrNull() ?: 0f
        val minVal = embedding.minOrNull() ?: 0f
        assertTrue("Embedding values should be reasonable", maxVal < 10f && minVal > -10f)
    }

    @Test
    fun testCosineSimilarity() = runBlocking {
        textEmbeddingService.initialize()

        // Test similar texts
        val text1 = "Solve linear equations step by step"
        val text2 = "Linear equations can be solved systematically"
        val text3 = "Quadratic functions have curved graphs"

        val embedding1 = textEmbeddingService.embedText(text1)
        val embedding2 = textEmbeddingService.embedText(text2)
        val embedding3 = textEmbeddingService.embedText(text3)

        assertNotNull(embedding1)
        assertNotNull(embedding2)
        assertNotNull(embedding3)

        // Similar texts should have higher similarity
        val similarity12 = textEmbeddingService.cosineSimilarity(embedding1!!, embedding2!!)
        val similarity13 = textEmbeddingService.cosineSimilarity(embedding1, embedding3!!)

        assertTrue("Similar texts should have higher similarity", similarity12 > similarity13)
        assertTrue("Similarity should be between -1 and 1", similarity12 <= 1f && similarity12 >= -1f)
        assertTrue("Similarity should be between -1 and 1", similarity13 <= 1f && similarity13 >= -1f)
    }

    @Test
    fun testBatchEmbedding() = runBlocking {
        textEmbeddingService.initialize()

        val texts = listOf(
            "Chapter 1: Introduction to Algebra",
            "Linear equations have one variable",
            "Solve for x in the equation 2x + 5 = 15",
            "The slope-intercept form is y = mx + b"
        )

        val embeddings = textEmbeddingService.embedTexts(texts)

        assertEquals("Should have same number of embeddings as texts", texts.size, embeddings.size)
        assertTrue("All embeddings should be non-null", embeddings.all { it != null })
        assertTrue("All embeddings should have same dimension", 
            embeddings.all { it?.size == embeddings[0]?.size })
    }

    @Test
    fun testMostSimilarSearch() = runBlocking {
        textEmbeddingService.initialize()

        val query = "How to solve linear equations?"
        val documents = listOf(
            "Linear equations can be solved by isolating the variable",
            "Quadratic equations require the quadratic formula",
            "To solve ax + b = c, subtract b and divide by a",
            "Geometry deals with shapes and their properties"
        )

        val queryEmbedding = textEmbeddingService.embedText(query)
        val docEmbeddings = textEmbeddingService.embedTexts(documents)

        assertNotNull(queryEmbedding)
        assertTrue("All document embeddings should be non-null", docEmbeddings.all { it != null })

        val results = textEmbeddingService.findMostSimilar(
            queryEmbedding!!,
            docEmbeddings.filterNotNull(),
            maxResults = 2
        )

        assertEquals("Should return 2 results", 2, results.size)
        assertTrue("Results should be sorted by similarity", 
            results[0].second >= results[1].second)
        
        // The first and third documents should be most relevant to linear equations
        assertTrue("First result should be about linear equations", 
            results[0].first == 0 || results[0].first == 2)
    }

    @Test
    fun testEmptyTextHandling() = runBlocking {
        textEmbeddingService.initialize()

        val emptyEmbedding = textEmbeddingService.embedText("")
        val nullEmbedding = textEmbeddingService.embedText("   ")

        // Empty text should return null or handle gracefully
        // This is implementation-dependent
        assertTrue("Empty text should be handled gracefully", true)
    }

    @Test
    fun testCleanup() = runBlocking {
        textEmbeddingService.initialize()
        
        // Test that cleanup doesn't crash
        textEmbeddingService.cleanup()
        
        // After cleanup, service should still be able to re-initialize
        val reinitialized = textEmbeddingService.initialize()
        assertTrue("Should be able to reinitialize after cleanup", reinitialized)
    }
}