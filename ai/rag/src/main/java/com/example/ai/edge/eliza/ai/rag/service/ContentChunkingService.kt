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
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkEntity
import com.example.ai.edge.eliza.core.model.ContentChunkType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for chunking markdown content into semantic segments suitable for RAG.
 * Uses markdown structure and semantic boundaries to create optimal chunks.
 */
@Singleton
class ContentChunkingService @Inject constructor() {
    
    companion object {
        private const val TAG = "ContentChunkingService"
        
        // Target chunk sizes (in characters, roughly 200-400 tokens)
        private const val MIN_CHUNK_SIZE = 800  // ~200 tokens
        private const val MAX_CHUNK_SIZE = 1600 // ~400 tokens
        private const val OVERLAP_SIZE = 100    // Overlap between chunks
        
        // Markdown patterns for section detection
        private val HEADER_REGEX = Regex("^#{1,6}\\s+(.+)$", RegexOption.MULTILINE)
        private val CODE_BLOCK_REGEX = Regex("```[\\s\\S]*?```")
        private val MATH_BLOCK_REGEX = Regex("\\$\\$[\\s\\S]*?\\$\\$")
        private val LIST_ITEM_REGEX = Regex("^[\\s]*[-*+]\\s+", RegexOption.MULTILINE)
        private val NUMBERED_LIST_REGEX = Regex("^[\\s]*\\d+\\.\\s+", RegexOption.MULTILINE)
    }
    
    /**
     * Chunk a chapter's markdown content into semantic segments with multi-vector retrieval.
     * Creates both summary chunks and detail chunks for enhanced context understanding.
     */
    suspend fun chunkChapterContent(
        chapterId: String,
        courseId: String,
        chapterTitle: String,
        markdownContent: String,
        chapterNumber: Int
    ): List<ContentChunkEntity> {
        Log.d(TAG, "Chunking chapter content with multi-vector retrieval: $chapterTitle (${markdownContent.length} chars)")
        
        val chunks = mutableListOf<ContentChunkEntity>()
        
        try {
            // Step 1: Create chapter summary chunk (for high-level queries)
            val summaryChunk = createChapterSummary(
                chapterId, courseId, chapterTitle, markdownContent, chapterNumber
            )
            chunks.add(summaryChunk)
            
            // Step 2: Extract special content types (code blocks, math, etc.)
            val specialChunks = extractSpecialContent(
                chapterId, courseId, markdownContent, chapterNumber
            )
            chunks.addAll(specialChunks)
            
            // Step 3: Remove special content for main text processing
            val mainContent = removeSpecialContent(markdownContent)
            
            // Step 4: Split by headers to respect document structure
            val sections = splitByHeaders(mainContent)
            
            // Step 5: Process each section with both summary and detail chunks
            sections.forEachIndexed { sectionIndex, section ->
                // Create detail chunks for specific queries
                val detailChunks = chunkSection(
                    section, chapterId, courseId, chapterNumber, sectionIndex
                )
                chunks.addAll(detailChunks)
                
                // Create section summary if section is large enough
                if (section.content.length > MIN_CHUNK_SIZE) {
                    val sectionSummary = createSectionSummary(
                        section, chapterId, courseId, chapterNumber, sectionIndex
                    )
                    chunks.add(sectionSummary)
                }
            }
            
            Log.d(TAG, "Created ${chunks.size} chunks (with multi-vector retrieval) for chapter $chapterTitle")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error chunking chapter content", e)
            // Fallback: create a single chunk with the entire content
            chunks.add(createFallbackChunk(chapterId, courseId, chapterTitle, markdownContent))
        }
        
        return chunks
    }
    
    /**
     * Extract special content types (code blocks, formulas, etc.) as separate chunks.
     */
    private fun extractSpecialContent(
        chapterId: String,
        courseId: String,
        content: String,
        chapterNumber: Int
    ): List<ContentChunkEntity> {
        val chunks = mutableListOf<ContentChunkEntity>()
        
        // Extract code blocks
        CODE_BLOCK_REGEX.findAll(content).forEach { match ->
            chunks.add(
                ContentChunkEntity(
                    id = "${chapterId}_code_${chunks.size}",
                    courseId = courseId,
                    chapterId = chapterId,
                    title = "Code Example",
                    content = match.value.trim(),
                    chunkType = ContentChunkType.EXAMPLE.name,
                    source = "Chapter $chapterNumber",
                    startPosition = match.range.first,
                    endPosition = match.range.last,
                    tokenCount = estimateTokenCount(match.value),
                    embedding = FloatArray(0), // Will be filled later
                    metadata = mapOf(
                        "type" to "code_block",
                        "language" to extractCodeLanguage(match.value)
                    )
                )
            )
        }
        
        // Extract math blocks
        MATH_BLOCK_REGEX.findAll(content).forEach { match ->
            chunks.add(
                ContentChunkEntity(
                    id = "${chapterId}_math_${chunks.size}",
                    courseId = courseId,
                    chapterId = chapterId,
                    title = "Mathematical Formula",
                    content = match.value.trim(),
                    chunkType = ContentChunkType.FORMULA.name,
                    source = "Chapter $chapterNumber",
                    startPosition = match.range.first,
                    endPosition = match.range.last,
                    tokenCount = estimateTokenCount(match.value),
                    embedding = FloatArray(0), // Will be filled later
                    metadata = mapOf("type" to "math_block")
                )
            )
        }
        
        return chunks
    }
    
    /**
     * Remove special content to process main text separately.
     */
    private fun removeSpecialContent(content: String): String {
        return content
            .replace(CODE_BLOCK_REGEX, "[CODE_BLOCK]")
            .replace(MATH_BLOCK_REGEX, "[MATH_BLOCK]")
    }
    
    /**
     * Split content by markdown headers to respect document structure.
     */
    private fun splitByHeaders(content: String): List<MarkdownSection> {
        val sections = mutableListOf<MarkdownSection>()
        val lines = content.split("\n")
        
        var currentSection = MarkdownSection("", "", 0, mutableListOf())
        var currentStart = 0
        
        lines.forEachIndexed { index, line ->
            val headerMatch = HEADER_REGEX.find(line)
            
            if (headerMatch != null) {
                // Save previous section if it has content
                if (currentSection.content.isNotEmpty()) {
                    currentSection.endLine = index - 1
                    sections.add(currentSection.copy(lines = currentSection.lines.toMutableList()))
                }
                
                // Start new section
                val headerLevel = line.takeWhile { it == '#' }.length
                val headerText = headerMatch.groupValues[1].trim()
                
                currentSection = MarkdownSection(
                    headerText, 
                    line, 
                    index, 
                    mutableListOf(line),
                    headerLevel
                )
                currentStart = index
            } else {
                currentSection.lines.add(line)
                currentSection.content += if (currentSection.content.isEmpty()) line else "\n$line"
            }
        }
        
        // Add final section
        if (currentSection.content.isNotEmpty()) {
            currentSection.endLine = lines.size - 1
            sections.add(currentSection.copy(lines = currentSection.lines.toMutableList()))
        }
        
        return sections
    }
    
    /**
     * Chunk a section respecting semantic boundaries.
     */
    private fun chunkSection(
        section: MarkdownSection,
        chapterId: String,
        courseId: String,
        chapterNumber: Int,
        sectionIndex: Int
    ): List<ContentChunkEntity> {
        val chunks = mutableListOf<ContentChunkEntity>()
        val content = section.content
        
        if (content.length <= MAX_CHUNK_SIZE) {
            // Section fits in one chunk
            chunks.add(
                createChunk(
                    chapterId, courseId, section.header, content, 
                    chapterNumber, sectionIndex, 0, section.startLine, section.endLine
                )
            )
        } else {
            // Split section into multiple chunks
            val subChunks = splitLongSection(content)
            subChunks.forEachIndexed { chunkIndex, chunkContent ->
                chunks.add(
                    createChunk(
                        chapterId, courseId, section.header, chunkContent,
                        chapterNumber, sectionIndex, chunkIndex, section.startLine, section.endLine
                    )
                )
            }
        }
        
        return chunks
    }
    
    /**
     * Split a long section into smaller chunks respecting paragraph boundaries.
     */
    private fun splitLongSection(content: String): List<String> {
        val chunks = mutableListOf<String>()
        val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
        
        var currentChunk = StringBuilder()
        
        for (paragraph in paragraphs) {
            val paragraphWithNewlines = paragraph.trim()
            
            // If adding this paragraph would exceed max size, finalize current chunk
            if (currentChunk.isNotEmpty() && 
                currentChunk.length + paragraphWithNewlines.length > MAX_CHUNK_SIZE) {
                
                chunks.add(currentChunk.toString().trim())
                
                // Start new chunk with overlap if previous chunk is substantial
                if (currentChunk.length > MIN_CHUNK_SIZE) {
                    val overlapText = getLastSentences(currentChunk.toString(), OVERLAP_SIZE)
                    currentChunk = StringBuilder(overlapText)
                } else {
                    currentChunk = StringBuilder()
                }
            }
            
            if (currentChunk.isNotEmpty()) {
                currentChunk.append("\n\n")
            }
            currentChunk.append(paragraphWithNewlines)
        }
        
        // Add final chunk
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }
        
        return chunks.filter { it.length >= MIN_CHUNK_SIZE || chunks.size == 1 }
    }
    
    /**
     * Create a content chunk entity.
     */
    private fun createChunk(
        chapterId: String,
        courseId: String,
        title: String,
        content: String,
        chapterNumber: Int,
        sectionIndex: Int,
        chunkIndex: Int,
        startLine: Int,
        endLine: Int
    ): ContentChunkEntity {
        return ContentChunkEntity(
            id = "${chapterId}_section_${sectionIndex}_chunk_${chunkIndex}",
            courseId = courseId,
            chapterId = chapterId,
            title = title.ifEmpty() { "Chapter $chapterNumber Section" },
            content = content.trim(),
            chunkType = ContentChunkType.CHAPTER_SECTION.name,
            source = "Chapter $chapterNumber",
            startPosition = startLine,
            endPosition = endLine,
            tokenCount = estimateTokenCount(content),
            embedding = FloatArray(0), // Will be filled later
            metadata = mapOf(
                "section_index" to sectionIndex.toString(),
                "chunk_index" to chunkIndex.toString()
            )
        )
    }
    
    /**
     * Create a fallback chunk when chunking fails.
     */
    private fun createFallbackChunk(
        chapterId: String,
        courseId: String,
        title: String,
        content: String
    ): ContentChunkEntity {
        return ContentChunkEntity(
            id = "${chapterId}_fallback",
            courseId = courseId,
            chapterId = chapterId,
            title = title,
            content = content.take(MAX_CHUNK_SIZE),
            chunkType = ContentChunkType.CHAPTER_SECTION.name,
            source = "Full Chapter",
            startPosition = 0,
            endPosition = content.length,
            tokenCount = estimateTokenCount(content),
            embedding = FloatArray(0),
            metadata = mapOf("type" to "fallback")
        )
    }
    
    /**
     * Get the last few sentences for chunk overlap.
     */
    private fun getLastSentences(text: String, maxLength: Int): String {
        val sentences = text.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        if (sentences.isEmpty()) return ""
        
        var result = ""
        for (i in sentences.size - 1 downTo 0) {
            val sentence = sentences[i].trim()
            if (result.length + sentence.length <= maxLength) {
                result = if (result.isEmpty()) sentence else "$sentence. $result"
            } else {
                break
            }
        }
        
        return result
    }
    
    /**
     * Extract programming language from code block.
     */
    private fun extractCodeLanguage(codeBlock: String): String {
        val firstLine = codeBlock.lines().firstOrNull() ?: return "unknown"
        return firstLine.removePrefix("```").trim().takeWhile { !it.isWhitespace() }
    }
    
    /**
     * Estimate token count (roughly 4 characters per token).
     */
    private fun estimateTokenCount(text: String): Int {
        return (text.length / 4).coerceAtLeast(1)
    }
    
    /**
     * Create a high-level summary chunk for the entire chapter.
     * Used for answering general questions about the chapter.
     */
    private fun createChapterSummary(
        chapterId: String,
        courseId: String,
        chapterTitle: String,
        markdownContent: String,
        chapterNumber: Int
    ): ContentChunkEntity {
        // Extract key concepts and create concise summary
        val headers = HEADER_REGEX.findAll(markdownContent)
            .map { it.groupValues[1] }
            .take(5) // Top 5 headers
            .joinToString(", ")
        
        val summary = buildString {
            append("Chapter $chapterNumber: $chapterTitle\n\n")
            append("Key Topics: $headers\n\n")
            
            // Extract first paragraph as overview
            val firstParagraph = markdownContent
                .lines()
                .dropWhile { it.startsWith("#") || it.isBlank() }
                .takeWhile { it.isNotBlank() }
                .joinToString(" ")
                .take(300)
            
            if (firstParagraph.isNotEmpty()) {
                append("Overview: $firstParagraph...\n\n")
            }
            
            append("This chapter covers fundamental concepts in $chapterTitle with practical examples and exercises.")
        }
        
        return ContentChunkEntity(
            id = "${chapterId}_summary",
            chapterId = chapterId,
            courseId = courseId,
            title = "Chapter $chapterNumber: $chapterTitle Summary",
            content = summary,
            chunkType = ContentChunkType.SUMMARY.name,
            source = "chapter_$chapterNumber",
            startPosition = 0,
            endPosition = summary.length,
            tokenCount = estimateTokenCount(summary),
            embedding = floatArrayOf(), // Will be populated during indexing
            metadata = mapOf(
                "chapter_title" to chapterTitle,
                "chapter_number" to chapterNumber.toString(),
                "is_summary" to "true",
                "summary_type" to "chapter"
            )
        )
    }
    
    /**
     * Create a summary chunk for a large section.
     * Provides mid-level context for section-specific queries.
     */
    private fun createSectionSummary(
        section: MarkdownSection,
        chapterId: String,
        courseId: String,
        chapterNumber: Int,
        sectionIndex: Int
    ): ContentChunkEntity {
        val summary = buildString {
            append("Section: ${section.header}\n\n")
            
            // Extract key points from the section
            val keyPoints = section.content
                .lines()
                .filter { line ->
                    line.trim().startsWith("-") || 
                    line.trim().startsWith("*") || 
                    line.trim().matches(Regex("^\\d+\\..*"))
                }
                .take(3)
                .joinToString("\n")
            
            if (keyPoints.isNotEmpty()) {
                append("Key Points:\n$keyPoints\n\n")
            }
            
            // Add first sentence or two as context
            val firstSentences = section.content
                .replace(Regex("[#*`]+"), "")
                .split(Regex("[.!?]+"))
                .take(2)
                .joinToString(". ")
                .trim()
                .take(200)
            
            if (firstSentences.isNotEmpty()) {
                append("Summary: $firstSentences")
                if (!firstSentences.endsWith(".")) append(".")
            }
        }
        
        return ContentChunkEntity(
            id = "${chapterId}_section_${sectionIndex}_summary",
            chapterId = chapterId,
            courseId = courseId,
            title = "Section: ${section.header}",
            content = summary,
            chunkType = ContentChunkType.SUMMARY.name,
            source = "section_$sectionIndex",
            startPosition = section.startLine,
            endPosition = section.endLine,
            tokenCount = estimateTokenCount(summary),
            embedding = floatArrayOf(), // Will be populated during indexing
            metadata = mapOf(
                "section_header" to section.header,
                "section_index" to sectionIndex.toString(),
                "is_summary" to "true",
                "summary_type" to "section"
            )
        )
    }
    
    /**
     * Data class for markdown sections.
     */
    private data class MarkdownSection(
        val header: String,
        var content: String,
        val startLine: Int,
        val lines: MutableList<String>,
        val headerLevel: Int = 1,
        var endLine: Int = startLine
    )
}