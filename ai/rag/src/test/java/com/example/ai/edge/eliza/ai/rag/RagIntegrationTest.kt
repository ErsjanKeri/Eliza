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
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkEntity
import com.example.ai.edge.eliza.ai.rag.data.VectorStorageDatabase
import com.example.ai.edge.eliza.ai.rag.service.ContentChunkingService
import com.example.ai.edge.eliza.ai.rag.service.TextEmbeddingService
import com.example.ai.edge.eliza.core.model.ContentChunkType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Integration test to verify the complete RAG pipeline works correctly.
 * Tests chunking, embedding, storage, and retrieval.
 */
@RunWith(AndroidJUnit4::class)
class RagIntegrationTest {

    private lateinit var database: VectorStorageDatabase
    private lateinit var textEmbeddingService: TextEmbeddingService
    private lateinit var contentChunkingService: ContentChunkingService
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            VectorStorageDatabase::class.java
        ).build()
        
        textEmbeddingService = TextEmbeddingService(context)
        contentChunkingService = ContentChunkingService()
    }

    @After
    @Throws(IOException::class)
    fun cleanup() {
        database.close()
        textEmbeddingService.cleanup()
    }

    @Test
    fun testCompleteRagPipeline() = runBlocking {
        // Sample chapter content similar to our mock data
        val chapterContent = """
        # Linear Equations
        
        Linear equations are mathematical expressions that contain variables raised to the first power. 
        They form straight lines when graphed on a coordinate plane.
        
        ## Basic Form
        
        The general form of a linear equation is: ax + b = c
        
        Where:
        - a is the coefficient of x
        - b is a constant term  
        - c is the result
        
        ## Solving Linear Equations
        
        To solve a linear equation, follow these steps:
        
        1. Isolate the variable term on one side
        2. Move constants to the other side
        3. Divide both sides by the coefficient
        
        ### Example 1
        
        Solve: 2x + 5 = 15
        
        Step 1: Subtract 5 from both sides
        2x = 10
        
        Step 2: Divide both sides by 2
        x = 5
        
        ## Practice Problems
        
        Try solving these equations:
        1. 3x + 7 = 22
        2. -2x + 8 = 4
        3. 5x - 3 = 17
        """.trimIndent()

        // Step 1: Test chunking
        val chunks = contentChunkingService.chunkChapterContent(
            chapterId = "test_chapter_1",
            courseId = "test_course_1", 
            chapterTitle = "Linear Equations",
            markdownContent = chapterContent,
            chapterNumber = 1
        )

        assertTrue("Should create multiple chunks", chunks.size > 1)
        assertTrue("All chunks should have reasonable size", 
            chunks.all { it.content.length >= 100 && it.content.length <= 2000 })

        // Step 2: Test embedding generation
        assertTrue("TextEmbeddingService should initialize", textEmbeddingService.initialize())

        val chunksWithEmbeddings = mutableListOf<ContentChunkEntity>()
        for (chunk in chunks) {
            val embedding = textEmbeddingService.embedText("${chunk.title}\n\n${chunk.content}")
            if (embedding != null) {
                chunksWithEmbeddings.add(chunk.copy(embedding = embedding))
            }
        }

        assertTrue("Should have embedded chunks", chunksWithEmbeddings.isNotEmpty())
        assertEquals("All chunks should have embeddings", chunks.size, chunksWithEmbeddings.size)

        // Step 3: Test database storage
        val contentChunkDao = database.contentChunkDao()
        contentChunkDao.insertChunks(chunksWithEmbeddings)

        val storedChunks = contentChunkDao.getChunksByChapter("test_chapter_1")
        assertEquals("Stored chunks should match inserted chunks", 
            chunksWithEmbeddings.size, storedChunks.size)

        // Step 4: Test vector similarity search
        val queryText = "How do you solve equations with variables?"
        val queryEmbedding = textEmbeddingService.embedText(queryText)
        assertNotNull("Query embedding should not be null", queryEmbedding)

        // Find most similar chunks
        val similarities = storedChunks.map { chunk ->
            chunk to textEmbeddingService.cosineSimilarity(queryEmbedding!!, chunk.embedding)
        }.sortedByDescending { it.second }

        assertTrue("Should have similarity results", similarities.isNotEmpty())
        assertTrue("Best match should have reasonable similarity", similarities.first().second > 0.3f)

        // The best match should be about solving equations
        val bestMatch = similarities.first().first
        assertTrue("Best match should contain solving content",
            bestMatch.content.lowercase().contains("solve") || 
            bestMatch.content.lowercase().contains("equation"))
    }

    @Test
    fun testDifferentChunkTypes() = runBlocking {
        val codeContent = """
        # Programming Example
        
        Here's how to solve linear equations in code:
        
        ```python
        def solve_linear(a, b, c):
            # Solve ax + b = c for x
            return (c - b) / a
        ```
        
        The function takes coefficients and returns the solution.
        """.trimIndent()

        val chunks = contentChunkingService.chunkChapterContent(
            chapterId = "test_chapter_code",
            courseId = "test_course_1",
            chapterTitle = "Programming Examples", 
            markdownContent = codeContent,
            chapterNumber = 2
        )

        // Should detect different chunk types
        val chunkTypes = chunks.map { ContentChunkType.valueOf(it.chunkType) }.toSet()
        assertTrue("Should have multiple chunk types", chunkTypes.size > 1)
        
        // Should have at least one code example
        assertTrue("Should have code examples", 
            chunks.any { ContentChunkType.valueOf(it.chunkType) == ContentChunkType.EXAMPLE })
    }

    @Test
    fun testPerformance() = runBlocking {
        val startTime = System.currentTimeMillis()
        
        // Initialize embedding service
        textEmbeddingService.initialize()
        
        val initTime = System.currentTimeMillis()
        
        // Create embeddings for several texts
        val texts = listOf(
            "Linear equations are fundamental in algebra",
            "Quadratic equations have degree 2",
            "Systems of equations can have multiple variables",
            "Graphing helps visualize mathematical relationships"
        )
        
        val embeddings = textEmbeddingService.embedTexts(texts)
        val embeddingTime = System.currentTimeMillis()
        
        // Perform similarity search
        val queryEmbedding = textEmbeddingService.embedText("What are linear equations?")
        val results = textEmbeddingService.findMostSimilar(
            queryEmbedding!!,
            embeddings.filterNotNull(),
            maxResults = 2
        )
        
        val searchTime = System.currentTimeMillis()
        
        // Performance assertions
        assertTrue("Initialization should be under 5 seconds", initTime - startTime < 5000)
        assertTrue("Embedding generation should be under 2 seconds", embeddingTime - initTime < 2000)
        assertTrue("Search should be under 100ms", searchTime - embeddingTime < 100)
        
        // Quality assertions
        assertTrue("Should find relevant results", results.isNotEmpty())
        assertTrue("Best result should be about linear equations", results[0].first == 0)
    }

    @Test
    fun testMockDataCompatibility() = runBlocking {
        // Test with content structure similar to our mock data
        val mockChapterContent = """
        ## Course Overview
        Master the fundamentals of algebra including linear equations, polynomials, and factoring.
        
        ### Chapter Learning Objectives
        - Understand linear equations
        - Learn solving techniques
        - Apply to real-world problems
        
        ### Key Concepts
        - Variables and coefficients
        - Equation balancing
        - Solution verification
        """.trimIndent()

        val chunks = contentChunkingService.chunkChapterContent(
            chapterId = "algebra_chapter_1",
            courseId = "course_algebra_1",
            chapterTitle = "Algebra I Fundamentals", 
            markdownContent = mockChapterContent,
            chapterNumber = 1
        )

        assertTrue("Should create chunks from mock-style content", chunks.isNotEmpty())
        
        // Test that embeddings work with this content
        textEmbeddingService.initialize()
        
        val firstChunk = chunks.first()
        val embedding = textEmbeddingService.embedText(firstChunk.content)
        assertNotNull("Should create embedding for mock content", embedding)
        
        // Test search with typical student questions
        val studentQuestions = listOf(
            "What are linear equations?",
            "How do I solve for x?",
            "What is a variable in algebra?"
        )
        
        for (question in studentQuestions) {
            val questionEmbedding = textEmbeddingService.embedText(question)
            assertNotNull("Should embed student question: $question", questionEmbedding)
            
            val similarity = textEmbeddingService.cosineSimilarity(embedding!!, questionEmbedding!!)
            assertTrue("Should have some similarity with chapter content", similarity > 0.1f)
        }
    }
}