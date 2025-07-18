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

import com.example.ai.edge.eliza.core.model.ChatContext
import com.example.ai.edge.eliza.core.model.ContentChunk
import com.example.ai.edge.eliza.core.model.EnhancedPrompt
import com.example.ai.edge.eliza.core.model.PromptEnhancementResult

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