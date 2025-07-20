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

package com.example.ai.edge.eliza.core.data.repository.mock

import com.example.ai.edge.eliza.core.data.repository.AIResponse
import com.example.ai.edge.eliza.core.data.repository.ChatRepository
import com.example.ai.edge.eliza.core.data.repository.ChatResponse
import com.example.ai.edge.eliza.core.data.repository.ChatResponseStatus
import com.example.ai.edge.eliza.core.data.repository.ConversationSummary
import com.example.ai.edge.eliza.core.data.repository.ExportFormat
import com.example.ai.edge.eliza.core.data.repository.ImageProcessingResult
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadProgress
import com.example.ai.edge.eliza.core.data.repository.ModelDownloadStatus
import com.example.ai.edge.eliza.core.data.repository.ModelInitializationResult
import com.example.ai.edge.eliza.core.model.ChatMessage
import com.example.ai.edge.eliza.core.model.ChatSession
import com.example.ai.edge.eliza.core.model.ImageMathProblem
import com.example.ai.edge.eliza.core.model.MathStep
import com.example.ai.edge.eliza.core.model.MessageStatus
import com.example.ai.edge.eliza.core.model.MessageType
import com.example.ai.edge.eliza.core.model.ModelState
import com.example.ai.edge.eliza.core.model.ProblemType
import com.example.ai.edge.eliza.core.model.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ChatRepository for development and testing.
 * Provides realistic fake data to support development without requiring actual AI backend.
 */
@Singleton
class MockChatRepository @Inject constructor() : ChatRepository {

    private val mockSessions = mutableMapOf<String, ChatSession>()
    private val mockMessages = mutableMapOf<String, MutableList<ChatMessage>>()
    private val mockModelState = ModelState(
        isInitialized = true,
        isDownloading = false,
        downloadProgress = 1.0f,
                    modelName = "Gemma-3n-E4B-it-int4",
                  sizeInBytes = 4405655031L,
          estimatedPeakMemoryInBytes = 6979321856L,
        errorMessage = null
    )

    init {
        // Create some mock sessions
        val session1 = ChatSession(
            id = "session1",
            title = "Algebra Basics",
            subject = Subject.ALGEBRA,
            courseId = "course1",
            lessonId = "lesson1",
            createdAt = System.currentTimeMillis() - 86400000,
            lastMessageAt = System.currentTimeMillis() - 3600000,
            isActive = true
        )
        
        val session2 = ChatSession(
            id = "session2",
            title = "Geometry Help",
            subject = Subject.GEOMETRY,
            courseId = "course2",
            lessonId = "lesson2",
            createdAt = System.currentTimeMillis() - 172800000,
            lastMessageAt = System.currentTimeMillis() - 7200000,
            isActive = true
        )
        
        mockSessions["session1"] = session1
        mockSessions["session2"] = session2
        
        // Create some mock messages
        mockMessages["session1"] = mutableListOf(
            ChatMessage(
                id = "msg1",
                sessionId = "session1",
                content = "Hello! I need help with algebra.",
                isUser = true,
                timestamp = System.currentTimeMillis() - 3600000,
                imageUri = null,
                mathSteps = emptyList(),
                messageType = MessageType.TEXT,
                status = MessageStatus.SENT
            ),
            ChatMessage(
                id = "msg2",
                sessionId = "session1",
                content = "Hello! I'd be happy to help you with algebra. What specific topic would you like to work on?",
                isUser = false,
                timestamp = System.currentTimeMillis() - 3599000,
                imageUri = null,
                mathSteps = emptyList(),
                messageType = MessageType.TEXT,
                status = MessageStatus.SENT
            )
        )
        
        mockMessages["session2"] = mutableListOf(
            ChatMessage(
                id = "msg3",
                sessionId = "session2",
                content = "Can you help me with triangles?",
                isUser = true,
                timestamp = System.currentTimeMillis() - 7200000,
                imageUri = null,
                mathSteps = emptyList(),
                messageType = MessageType.TEXT,
                status = MessageStatus.SENT
            )
        )
    }

    override fun getAllChatSessions(): Flow<List<ChatSession>> = 
        flowOf(mockSessions.values.toList())

    override fun getChatSessionById(sessionId: String): Flow<ChatSession?> = 
        flowOf(mockSessions[sessionId])

    override fun getActiveChatSessions(): Flow<List<ChatSession>> = 
        flowOf(mockSessions.values.filter { it.isActive })

    override fun getChatSessionsBySubject(subject: Subject): Flow<List<ChatSession>> = 
        flowOf(mockSessions.values.filter { it.subject == subject })

    override fun getChatSessionsByCourse(courseId: String): Flow<List<ChatSession>> = 
        flowOf(mockSessions.values.filter { it.courseId == courseId })

    override suspend fun createChatSession(title: String, subject: Subject?, courseId: String?): ChatSession {
        val session = ChatSession(
            id = "session_${System.currentTimeMillis()}",
            title = title,
            subject = subject,
            courseId = courseId,
            lessonId = null,
            createdAt = System.currentTimeMillis(),
            lastMessageAt = System.currentTimeMillis(),
            isActive = true
        )
        mockSessions[session.id] = session
        mockMessages[session.id] = mutableListOf()
        return session
    }

    override suspend fun updateChatSession(session: ChatSession) {
        mockSessions[session.id] = session
    }

    override suspend fun deleteChatSession(sessionId: String) {
        mockSessions.remove(sessionId)
        mockMessages.remove(sessionId)
    }

    override suspend fun deactivateChatSession(sessionId: String) {
        mockSessions[sessionId]?.let { session ->
            mockSessions[sessionId] = session.copy(isActive = false)
        }
    }

    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = 
        flowOf(mockMessages[sessionId] ?: emptyList())

    override fun getMessageById(messageId: String): Flow<ChatMessage?> = 
        flowOf(mockMessages.values.flatten().find { it.id == messageId })

    override fun getRecentMessages(sessionId: String, limit: Int): Flow<List<ChatMessage>> = 
        flowOf(mockMessages[sessionId]?.takeLast(limit) ?: emptyList())

    override suspend fun sendMessage(sessionId: String, message: String, imageUri: String?): Flow<ChatResponse> = flow {
        // Add user message
        val userMessage = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sessionId = sessionId,
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis(),
            imageUri = imageUri,
            mathSteps = emptyList(),
            messageType = if (imageUri != null) MessageType.IMAGE else MessageType.TEXT,
            status = MessageStatus.SENT
        )
        
        mockMessages[sessionId]?.add(userMessage)
        
        // Simulate AI processing
        emit(ChatResponse(
            messageId = "response_${System.currentTimeMillis()}",
            sessionId = sessionId,
            content = "",
            mathSteps = emptyList(),
            status = ChatResponseStatus.GENERATING,
            confidence = 0.9f,
            processingTime = 0L,
            error = null
        ))
        
        delay(1000) // Simulate processing time
        
        // Generate mock response
        val responseContent = generateMockResponse(message)
        val aiMessage = ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sessionId = sessionId,
            content = responseContent,
            isUser = false,
            timestamp = System.currentTimeMillis(),
            imageUri = null,
            mathSteps = emptyList(),
            messageType = MessageType.TEXT,
            status = MessageStatus.SENT
        )
        
        mockMessages[sessionId]?.add(aiMessage)
        
        emit(ChatResponse(
            messageId = aiMessage.id,
            sessionId = sessionId,
            content = responseContent,
            mathSteps = emptyList(),
            status = ChatResponseStatus.COMPLETE,
            confidence = 0.9f,
            processingTime = 1000L,
            error = null
        ))
    }

    override suspend fun sendImageMessage(sessionId: String, imageUri: String, question: String?): Flow<ChatResponse> = 
        sendMessage(sessionId, question ?: "Please solve this math problem", imageUri)

    override suspend fun insertMessage(message: ChatMessage) {
        mockMessages[message.sessionId]?.add(message)
    }

    override suspend fun updateMessage(message: ChatMessage) {
        mockMessages[message.sessionId]?.let { messages ->
            val index = messages.indexOfFirst { it.id == message.id }
            if (index != -1) {
                messages[index] = message
            }
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        mockMessages.values.forEach { messages ->
            messages.removeIf { it.id == messageId }
        }
    }

    override suspend fun deleteMessagesBySession(sessionId: String) {
        mockMessages[sessionId]?.clear()
    }

    override fun getModelState(): Flow<ModelState> = flowOf(mockModelState)

    override suspend fun initializeModel(): Flow<ModelInitializationResult> = flow {
        emit(ModelInitializationResult(
            isSuccess = true,
            modelName = "Gemma-3n-E4B-it-int4",
            initializationTime = 5000L,
                          memoryUsage = 4405655031L,
            error = null
        ))
    }

    override suspend fun downloadModel(): Flow<ModelDownloadProgress> = flow {
        emit(ModelDownloadProgress(
            progress = 1.0f,
            status = ModelDownloadStatus.COMPLETED,
            bytesDownloaded = 3136226711L,
            totalBytes = 3136226711L,
            downloadSpeed = 0L,
            error = null
        ))
    }

    override suspend fun generateResponse(sessionId: String, prompt: String): Flow<AIResponse> = flow {
        delay(500) // Simulate processing
        emit(AIResponse(
            content = generateMockResponse(prompt),
            mathSteps = emptyList(),
            confidence = 0.9f,
            processingTime = 500L,
            tokensUsed = 50,
            model = "Gemma-3n-E4B-it-int4"
        ))
    }

    override suspend fun generateMathSolution(problem: String, steps: Boolean): Flow<AIResponse> = flow {
        delay(1000) // Simulate processing
        val mockSteps = if (steps) {
            listOf(
                MathStep(
                    stepNumber = 1,
                    description = "Identify the problem type",
                    equation = problem,
                    explanation = "This is a basic algebra problem"
                ),
                MathStep(
                    stepNumber = 2,
                    description = "Solve step by step",
                    equation = "x = 5",
                    explanation = "Final answer"
                )
            )
        } else {
            emptyList()
        }
        
        emit(AIResponse(
            content = "Here's the solution to your math problem:\n\nStep 1: $problem\nStep 2: Solution: x = 5",
            mathSteps = mockSteps,
            confidence = 0.95f,
            processingTime = 1000L,
            tokensUsed = 75,
            model = "Gemma-3n-E4B-it-int4"
        ))
    }

    override suspend fun explainConcept(concept: String, subject: Subject): Flow<AIResponse> = flow {
        delay(800) // Simulate processing
        emit(AIResponse(
            content = "Let me explain $concept in ${subject.name.lowercase()}:\n\n$concept is a fundamental concept that...",
            mathSteps = emptyList(),
            confidence = 0.85f,
            processingTime = 800L,
            tokensUsed = 60,
            model = "Gemma-3n-E4B-it-int4"
        ))
    }

    override suspend fun processImage(imageUri: String): Flow<ImageProcessingResult> = flow {
        delay(1500) // Simulate image processing
        emit(ImageProcessingResult(
            imageUri = imageUri,
            extractedText = "2x + 3 = 7",
            mathProblem = ImageMathProblem(
                id = "img_${System.currentTimeMillis()}",
                imageUri = imageUri,
                extractedText = "2x + 3 = 7",
                problemType = ProblemType.EQUATION,
                confidence = 0.9f
            ),
            confidence = 0.9f,
            processingTime = 1500L,
            error = null
        ))
    }

    override suspend fun extractMathProblem(imageUri: String): ImageMathProblem? {
        return ImageMathProblem(
            id = "img_${System.currentTimeMillis()}",
            imageUri = imageUri,
            extractedText = "2x + 3 = 7",
            problemType = ProblemType.EQUATION,
            confidence = 0.9f
        )
    }

    override fun getImageMathProblems(): Flow<List<ImageMathProblem>> = flowOf(emptyList())

    override suspend fun solveImageProblem(problemId: String): Flow<AIResponse> = flow {
        delay(1200) // Simulate processing
        emit(AIResponse(
            content = "To solve this problem:\n\n2x + 3 = 7\n2x = 4\nx = 2",
            mathSteps = listOf(
                MathStep(
                    stepNumber = 1,
                    description = "Subtract 3 from both sides",
                    equation = "2x = 4",
                    explanation = "We need to isolate the variable term"
                ),
                MathStep(
                    stepNumber = 2,
                    description = "Divide both sides by 2",
                    equation = "x = 2",
                    explanation = "This gives us the final answer"
                )
            ),
            confidence = 0.95f,
            processingTime = 1200L,
            tokensUsed = 80,
            model = "Gemma-3n-E4B-it-int4"
        ))
    }

    override suspend fun clearConversation(sessionId: String) {
        mockMessages[sessionId]?.clear()
    }

    override suspend fun exportConversation(sessionId: String, format: ExportFormat): String {
        val messages = mockMessages[sessionId] ?: emptyList()
        return when (format) {
            ExportFormat.TEXT -> messages.joinToString("\n") { "${if (it.isUser) "User" else "AI"}: ${it.content}" }
            ExportFormat.JSON -> """{"messages": ${messages.size}}"""
            ExportFormat.PDF -> "PDF export not implemented in mock"
            ExportFormat.MARKDOWN -> messages.joinToString("\n") { 
                if (it.isUser) "**User**: ${it.content}" else "**AI**: ${it.content}"
            }
        }
    }

    override suspend fun getConversationSummary(sessionId: String): ConversationSummary {
        val messages = mockMessages[sessionId] ?: emptyList()
        return ConversationSummary(
            sessionId = sessionId,
            messageCount = messages.size,
            topicsDiscussed = listOf("algebra", "equations"),
            problemsSolved = messages.count { !it.isUser },
            averageResponseTime = 1000L,
            totalTokensUsed = messages.size * 50,
            difficulty = "intermediate",
            learningObjectives = listOf("solve linear equations", "understand algebraic concepts")
        )
    }

    private fun generateMockResponse(prompt: String): String {
        return when {
            prompt.contains("algebra", ignoreCase = true) -> 
                "Great! I can help you with algebra. What specific algebraic concept would you like to explore?"
            prompt.contains("equation", ignoreCase = true) -> 
                "Let's work on solving equations step by step. Can you share the equation you'd like to solve?"
            prompt.contains("graph", ignoreCase = true) -> 
                "Graphing is a powerful tool in mathematics. Would you like to learn about graphing functions or data?"
            prompt.contains("geometry", ignoreCase = true) -> 
                "Geometry is fascinating! Are you working with shapes, angles, or geometric proofs?"
            else -> 
                "I'm here to help with your math questions. Can you provide more details about what you'd like to learn?"
        }
    }
} 