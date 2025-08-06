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

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Text embedding service using Google AI Edge MediaPipe for on-device text embeddings.
 * This service uses the Universal Sentence Encoder model for high-quality semantic embeddings.
 */
@Singleton
class TextEmbeddingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TextEmbeddingService"
        private const val MODEL_PATH = "universal_sentence_encoder.tflite"
    }

    private var textEmbedder: TextEmbedder? = null
    private var isInitialized = false

    /**
     * Initialize the MediaPipe TextEmbedder.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing MediaPipe TextEmbedder")
            
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .build()

            val options = TextEmbedder.TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .setL2Normalize(true)
                .setQuantize(false)
                .build()

            textEmbedder = TextEmbedder.createFromOptions(context, options)
            isInitialized = true
            
            Log.d(TAG, "TextEmbedder initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextEmbedder", e)
            isInitialized = false
            false
        }
    }

    /**
     * Generate embedding for a single text.
     */
    suspend fun embedText(text: String): FloatArray? = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            Log.w(TAG, "TextEmbedder not initialized, initializing now...")
            if (!initialize()) {
                throw IllegalStateException("Failed to initialize TextEmbedder")
            }
        }

        try {
            val embedder = textEmbedder
            if (embedder != null) {
                Log.d(TAG, "Generating embedding for text: ${text.take(50)}...")
                
                // Use the MediaPipe API as shown in the working Embedder project
                val embeddingResult = embedder.embed(text)
                val embeddings = embeddingResult.embeddingResult().embeddings()
                
                if (embeddings.isNotEmpty()) {
                    val embedding = embeddings.first()
                    Log.d(TAG, "Successfully generated embedding with ${embedding.floatEmbedding().size} dimensions")
                    return@withContext embedding.floatEmbedding()
                } else {
                    Log.w(TAG, "No embeddings returned from MediaPipe")
                    return@withContext null
                }
            } else {
                Log.e(TAG, "TextEmbedder is null")
                return@withContext null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate embedding for text: ${text.take(50)}...", e)
            return@withContext null
        }
    }

    /**
     * Generate embeddings for multiple texts.
     */
    suspend fun embedTexts(texts: List<String>): List<FloatArray?> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            Log.w(TAG, "TextEmbedder not initialized, initializing now...")
            if (!initialize()) {
                throw IllegalStateException("Failed to initialize TextEmbedder")
            }
        }

        try {
            Log.d(TAG, "Generating embeddings for ${texts.size} texts")
            val results = mutableListOf<FloatArray?>()
            
            for (text in texts) {
                val embedding = embedText(text)
                results.add(embedding)
            }
            
            Log.d(TAG, "Generated ${results.count { it != null }} successful embeddings out of ${texts.size}")
            return@withContext results
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate embeddings for texts", e)
            return@withContext texts.map { null }
        }
    }

    /**
     * Generate a mock embedding for development/testing purposes.
     * Creates a deterministic 384-dimensional vector based on text hash.
     */
    private fun generateMockEmbedding(text: String): FloatArray {
        val dimension = 384 // Standard embedding dimension
        val hash = text.hashCode()
        val random = kotlin.random.Random(hash)
        
        return FloatArray(dimension) { 
            (random.nextFloat() - 0.5f) * 0.2f  // Approximate Gaussian distribution
        }.also { embedding ->
            // Normalize the vector
            val norm = sqrt(embedding.map { it * it }.sum())
            if (norm > 0) {
                for (i in embedding.indices) {
                    embedding[i] = embedding[i] / norm
                }
            }
        }
    }

    /**
     * Calculate cosine similarity between two embeddings.
     */
    fun cosineSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        if (embedding1.size != embedding2.size) {
            Log.w(TAG, "Embedding dimensions don't match: ${embedding1.size} vs ${embedding2.size}")
            return 0f
        }

        val dotProduct = embedding1.zip(embedding2) { a, b -> a * b }.sum()
        val norm1 = sqrt(embedding1.map { it * it }.sum())
        val norm2 = sqrt(embedding2.map { it * it }.sum())

        return if (norm1 > 0 && norm2 > 0) {
            dotProduct / (norm1 * norm2)
        } else {
            0f
        }
    }

    /**
     * Find the most similar embeddings to a query embedding.
     * Returns pairs of (index, similarity_score) sorted by similarity descending.
     */
    fun findMostSimilar(
        queryEmbedding: FloatArray,
        candidateEmbeddings: List<FloatArray>,
        maxResults: Int = 5
    ): List<Pair<Int, Float>> {
        if (candidateEmbeddings.isEmpty()) return emptyList()
        
        val similarities = candidateEmbeddings.mapIndexed { index, embedding ->
            val similarity = cosineSimilarity(queryEmbedding, embedding)
            index to similarity
        }
        
        return similarities
            .sortedByDescending { it.second }
            .take(maxResults)
    }

    /**
     * Check if the service is properly initialized.
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Clean up resources.
     */
    fun cleanup() {
        try {
            textEmbedder?.close()
            textEmbedder = null
            isInitialized = false
            Log.d(TAG, "TextEmbeddingService cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}