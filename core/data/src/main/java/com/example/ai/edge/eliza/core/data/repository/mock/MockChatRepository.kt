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
import com.example.ai.edge.eliza.core.model.ModelState
import com.example.ai.edge.eliza.core.model.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ChatRepository for development and testing.
 * Provides realistic dummy data and simulated AI responses.
 */
@Singleton
class MockChatRepository @Inject constructor() : ChatRepository {
    
    private val chatSessions = ConcurrentHashMap<String, ChatSession>()
    private val chatMessages = ConcurrentHashMap<String, MutableList<ChatMessage>>()
    private val imageMathProblems = ConcurrentHashMap<String, ImageMathProblem>()
    private var modelState = ModelState(
        isInitialized = false,
        isDownloading = false,
        downloadProgress = 0f,
        isModelReady = false,
        modelName = "Gemma-3n-E2B-it-int4",
        modelSize = 3136226711L,
        memoryUsage = 0L,
        lastUsed = 0L,
        errorMessage = null
    )
    
    init {
        // Initialize with sample data
        createSampleChatSessions()
        createSampleImageProblems()
    }
    
    // Chat Session operations
    override fun getAllChatSessions(): Flow<List<ChatSession>> = flowOf(chatSessions.values.toList())
    
    override fun getChatSessionById(sessionId: String): Flow<ChatSession?> = flowOf(chatSessions[sessionId])
    
    override fun getActiveChatSessions(): Flow<List<ChatSession>> = flowOf(
        chatSessions.values.filter { it.isActive }
    )
    
    override fun getChatSessionsBySubject(subject: Subject): Flow<List<ChatSession>> = flowOf(
        chatSessions.values.filter { it.subject == subject }
    )
    
    override fun getChatSessionsByCourse(courseId: String): Flow<List<ChatSession>> = flowOf(
        chatSessions.values.filter { it.courseId == courseId }
    )
    
    override suspend fun createChatSession(
        title: String,
        subject: Subject?,
        courseId: String?
    ): ChatSession {
        val session = ChatSession(
            id = UUID.randomUUID().toString(),
            title = title,
            subject = subject,
            courseId = courseId,
            createdAt = System.currentTimeMillis(),
            lastMessageAt = System.currentTimeMillis(),
            isActive = true,
            messageCount = 0,
            totalTokensUsed = 0,
            averageResponseTime = 0L
        )
        chatSessions[session.id] = session
        chatMessages[session.id] = mutableListOf()
        return session
    }
    
    override suspend fun updateChatSession(session: ChatSession) {
        chatSessions[session.id] = session
    }
    
    override suspend fun deleteChatSession(sessionId: String) {
        chatSessions.remove(sessionId)
        chatMessages.remove(sessionId)
    }
    
    override suspend fun deactivateChatSession(sessionId: String) {
        chatSessions[sessionId]?.let { session ->
            chatSessions[sessionId] = session.copy(isActive = false)
        }
    }
    
    // Chat Message operations
    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = flowOf(
        chatMessages[sessionId] ?: emptyList()
    )
    
    override fun getMessageById(messageId: String): Flow<ChatMessage?> = flowOf(
        chatMessages.values.flatten().find { it.id == messageId }
    )
    
    override fun getRecentMessages(sessionId: String, limit: Int): Flow<List<ChatMessage>> = flowOf(
        chatMessages[sessionId]?.takeLast(limit) ?: emptyList()
    )
    
    override suspend fun sendMessage(
        sessionId: String,
        message: String,
        imageUri: String?
    ): Flow<ChatResponse> = flow {
        // Simulate typing delay
        delay(100)
        
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            content = message,
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            imageUri = imageUri,
            mathSteps = emptyList(),
            isGenerating = false,
            tokensUsed = 0
        )
        
        chatMessages.getOrPut(sessionId) { mutableListOf() }.add(userMessage)
        
        // Simulate AI response generation
        emit(ChatResponse(
            messageId = "",
            sessionId = sessionId,
            content = "",
            mathSteps = emptyList(),
            status = ChatResponseStatus.GENERATING,
            confidence = 0f,
            processingTime = 0L,
            error = null
        ))
        
        delay(1500) // Simulate AI processing time
        
        val aiResponse = generateAIResponse(message, sessionId)
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            content = aiResponse.content,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            imageUri = null,
            mathSteps = aiResponse.mathSteps,
            isGenerating = false,
            tokensUsed = aiResponse.tokensUsed
        )
        
        chatMessages[sessionId]?.add(aiMessage)
        
        emit(ChatResponse(
            messageId = aiMessage.id,
            sessionId = sessionId,
            content = aiResponse.content,
            mathSteps = aiResponse.mathSteps,
            status = ChatResponseStatus.COMPLETE,
            confidence = aiResponse.confidence,
            processingTime = aiResponse.processingTime,
            error = null
        ))
    }
    
    override suspend fun sendImageMessage(
        sessionId: String,
        imageUri: String,
        question: String?
    ): Flow<ChatResponse> = flow {
        // Simulate image processing
        delay(200)
        
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            content = question ?: "Please help me solve this math problem",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            imageUri = imageUri,
            mathSteps = emptyList(),
            isGenerating = false,
            tokensUsed = 0
        )
        
        chatMessages.getOrPut(sessionId) { mutableListOf() }.add(userMessage)
        
        emit(ChatResponse(
            messageId = "",
            sessionId = sessionId,
            content = "",
            mathSteps = emptyList(),
            status = ChatResponseStatus.GENERATING,
            confidence = 0f,
            processingTime = 0L,
            error = null
        ))
        
        delay(2000) // Simulate image processing time
        
        val imageResponse = generateImageResponse(imageUri)
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            content = imageResponse.content,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            imageUri = null,
            mathSteps = imageResponse.mathSteps,
            isGenerating = false,
            tokensUsed = imageResponse.tokensUsed
        )
        
        chatMessages[sessionId]?.add(aiMessage)
        
        emit(ChatResponse(
            messageId = aiMessage.id,
            sessionId = sessionId,
            content = imageResponse.content,
            mathSteps = imageResponse.mathSteps,
            status = ChatResponseStatus.COMPLETE,
            confidence = imageResponse.confidence,
            processingTime = imageResponse.processingTime,
            error = null
        ))
    }
    
    override suspend fun insertMessage(message: ChatMessage) {
        chatMessages.getOrPut(message.sessionId) { mutableListOf() }.add(message)
    }
    
    override suspend fun updateMessage(message: ChatMessage) {
        chatMessages[message.sessionId]?.let { messages ->
            val index = messages.indexOfFirst { it.id == message.id }
            if (index != -1) {
                messages[index] = message
            }
        }
    }
    
    override suspend fun deleteMessage(messageId: String) {
        chatMessages.values.forEach { messages ->
            messages.removeIf { it.id == messageId }
        }
    }
    
    override suspend fun deleteMessagesBySession(sessionId: String) {
        chatMessages.remove(sessionId)
    }
    
    // AI Model operations
    override fun getModelState(): Flow<ModelState> = flowOf(modelState)
    
    override suspend fun initializeModel(): Flow<ModelInitializationResult> = flow {
        if (!modelState.isInitialized) {
            modelState = modelState.copy(isDownloading = true)
            delay(2000) // Simulate initialization time
            
            modelState = modelState.copy(
                isInitialized = true,
                isDownloading = false,
                isModelReady = true,
                memoryUsage = 5905580032L,
                lastUsed = System.currentTimeMillis()
            )
        }
        
        emit(ModelInitializationResult(
            isSuccess = true,
            modelName = "Gemma-3n-E2B-it-int4",
            initializationTime = 2000L,
            memoryUsage = 5905580032L,
            error = null
        ))
    }
    
    override suspend fun downloadModel(): Flow<ModelDownloadProgress> = flow {
        if (modelState.isDownloading) return@flow
        
        modelState = modelState.copy(isDownloading = true)
        
        for (progress in 0..100 step 5) {
            delay(100)
            emit(ModelDownloadProgress(
                progress = progress / 100f,
                status = ModelDownloadStatus.DOWNLOADING,
                bytesDownloaded = (3136226711L * progress / 100),
                totalBytes = 3136226711L,
                downloadSpeed = 31362267L, // ~30MB/s
                error = null
            ))
        }
        
        modelState = modelState.copy(
            isDownloading = false,
            downloadProgress = 1.0f
        )
        
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
        delay(800) // Simulate processing time
        val response = generateAIResponse(prompt, sessionId)
        emit(response)
    }
    
    override suspend fun generateMathSolution(problem: String, steps: Boolean): Flow<AIResponse> = flow {
        delay(1200) // Simulate processing time
        val response = generateMathSolutionResponse(problem, steps)
        emit(response)
    }
    
    override suspend fun explainConcept(concept: String, subject: Subject): Flow<AIResponse> = flow {
        delay(1000) // Simulate processing time
        val response = generateConceptExplanation(concept, subject)
        emit(response)
    }
    
    // Image Processing operations
    override suspend fun processImage(imageUri: String): Flow<ImageProcessingResult> = flow {
        delay(1500) // Simulate image processing time
        
        val mathProblem = ImageMathProblem(
            id = UUID.randomUUID().toString(),
            imageUri = imageUri,
            extractedText = "2x + 5 = 13",
            problemType = "linear_equation",
            difficulty = "EASY",
            subject = Subject.ALGEBRA,
            confidence = 0.92f,
            boundingBoxes = emptyList(),
            processingTime = 1500L,
            timestamp = System.currentTimeMillis()
        )
        
        imageMathProblems[mathProblem.id] = mathProblem
        
        emit(ImageProcessingResult(
            imageUri = imageUri,
            extractedText = mathProblem.extractedText,
            mathProblem = mathProblem,
            confidence = mathProblem.confidence,
            processingTime = mathProblem.processingTime,
            error = null
        ))
    }
    
    override suspend fun extractMathProblem(imageUri: String): ImageMathProblem? {
        return imageMathProblems.values.find { it.imageUri == imageUri }
    }
    
    override fun getImageMathProblems(): Flow<List<ImageMathProblem>> = flowOf(
        imageMathProblems.values.toList()
    )
    
    override suspend fun solveImageProblem(problemId: String): Flow<AIResponse> = flow {
        delay(1000)
        val problem = imageMathProblems[problemId]
        if (problem != null) {
            emit(generateImageProblemSolution(problem))
        }
    }
    
    // Conversation management
    override suspend fun clearConversation(sessionId: String) {
        chatMessages[sessionId]?.clear()
    }
    
    override suspend fun exportConversation(sessionId: String, format: ExportFormat): String {
        val messages = chatMessages[sessionId] ?: return ""
        return when (format) {
            ExportFormat.TEXT -> messages.joinToString("\n") { "${if (it.isFromUser) "User" else "AI"}: ${it.content}" }
            ExportFormat.JSON -> "{ \"messages\": [...] }" // Simplified
            ExportFormat.PDF -> "PDF export not implemented in mock"
            ExportFormat.MARKDOWN -> messages.joinToString("\n\n") { 
                "**${if (it.isFromUser) "User" else "AI"}**: ${it.content}"
            }
        }
    }
    
    override suspend fun getConversationSummary(sessionId: String): ConversationSummary {
        val messages = chatMessages[sessionId] ?: emptyList()
        return ConversationSummary(
            sessionId = sessionId,
            messageCount = messages.size,
            topicsDiscussed = listOf("Algebra", "Linear Equations"),
            problemsSolved = 3,
            averageResponseTime = 1500L,
            totalTokensUsed = 450,
            difficulty = "Intermediate",
            learningObjectives = listOf("Solve linear equations", "Understand variables")
        )
    }
    
    // Private helper methods
    private fun createSampleChatSessions() {
        val session1 = ChatSession(
            id = "session-1",
            title = "Algebra Help",
            subject = Subject.ALGEBRA,
            courseId = "course-1",
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            lastMessageAt = System.currentTimeMillis() - 3600000, // 1 hour ago
            isActive = true,
            messageCount = 8,
            totalTokensUsed = 234,
            averageResponseTime = 1800L
        )
        
        val session2 = ChatSession(
            id = "session-2",
            title = "Geometry Problems",
            subject = Subject.GEOMETRY,
            courseId = "course-2",
            createdAt = System.currentTimeMillis() - 172800000, // 2 days ago
            lastMessageAt = System.currentTimeMillis() - 7200000, // 2 hours ago
            isActive = false,
            messageCount = 12,
            totalTokensUsed = 456,
            averageResponseTime = 2100L
        )
        
        chatSessions[session1.id] = session1
        chatSessions[session2.id] = session2
        
        // Add sample messages
        chatMessages[session1.id] = mutableListOf(
            ChatMessage(
                id = "msg-1",
                sessionId = session1.id,
                content = "How do I solve 2x + 5 = 13?",
                isFromUser = true,
                timestamp = System.currentTimeMillis() - 7200000,
                imageUri = null,
                mathSteps = emptyList(),
                isGenerating = false,
                tokensUsed = 0
            ),
            ChatMessage(
                id = "msg-2",
                sessionId = session1.id,
                content = "I'll help you solve 2x + 5 = 13 step by step!",
                isFromUser = false,
                timestamp = System.currentTimeMillis() - 7198000,
                imageUri = null,
                mathSteps = listOf(
                    MathStep(
                        stepNumber = 1,
                        description = "Subtract 5 from both sides",
                        equation = "2x + 5 - 5 = 13 - 5",
                        result = "2x = 8",
                        explanation = "To isolate the term with x, we subtract 5 from both sides."
                    ),
                    MathStep(
                        stepNumber = 2,
                        description = "Divide both sides by 2",
                        equation = "2x ÷ 2 = 8 ÷ 2",
                        result = "x = 4",
                        explanation = "To solve for x, we divide both sides by the coefficient of x."
                    )
                ),
                isGenerating = false,
                tokensUsed = 87
            )
        )
    }
    
    private fun createSampleImageProblems() {
        val problem1 = ImageMathProblem(
            id = "img-problem-1",
            imageUri = "sample://quadratic.jpg",
            extractedText = "x² + 2x - 8 = 0",
            problemType = "quadratic_equation",
            difficulty = "MEDIUM",
            subject = Subject.ALGEBRA,
            confidence = 0.95f,
            boundingBoxes = emptyList(),
            processingTime = 1200L,
            timestamp = System.currentTimeMillis() - 3600000
        )
        
        imageMathProblems[problem1.id] = problem1
    }
    
    private fun generateAIResponse(prompt: String, sessionId: String): AIResponse {
        val responses = listOf(
            "I'd be happy to help you with that! Let me break it down step by step.",
            "That's a great question! Here's how we can approach this problem:",
            "Let's work through this together. First, let me explain the concept:",
            "I can help you understand this better. Here's the key insight:",
            "Perfect! This is a common type of problem. Let me show you the method:"
        )
        
        return AIResponse(
            content = responses.random(),
            mathSteps = emptyList(),
            confidence = 0.85f,
            processingTime = 1500L,
            tokensUsed = 45,
            model = "Gemma-3n-E2B-it-int4"
        )
    }
    
    private fun generateMathSolutionResponse(problem: String, steps: Boolean): AIResponse {
        val mathSteps = if (steps) {
            listOf(
                MathStep(
                    stepNumber = 1,
                    description = "Identify the equation type",
                    equation = problem,
                    result = "Linear equation",
                    explanation = "This is a linear equation in one variable."
                ),
                MathStep(
                    stepNumber = 2,
                    description = "Solve for the variable",
                    equation = "Isolate x",
                    result = "x = solution",
                    explanation = "Apply inverse operations to solve."
                )
            )
        } else {
            emptyList()
        }
        
        return AIResponse(
            content = "Here's the solution to your math problem:",
            mathSteps = mathSteps,
            confidence = 0.92f,
            processingTime = 1200L,
            tokensUsed = 67,
            model = "Gemma-3n-E2B-it-int4"
        )
    }
    
    private fun generateImageResponse(imageUri: String): AIResponse {
        return AIResponse(
            content = "I can see the math problem in your image. Let me solve it for you:",
            mathSteps = listOf(
                MathStep(
                    stepNumber = 1,
                    description = "Extract the problem from image",
                    equation = "2x + 5 = 13",
                    result = "Linear equation identified",
                    explanation = "From the image, I can see this is a linear equation."
                ),
                MathStep(
                    stepNumber = 2,
                    description = "Solve the equation",
                    equation = "2x = 13 - 5",
                    result = "2x = 8",
                    explanation = "Subtract 5 from both sides."
                ),
                MathStep(
                    stepNumber = 3,
                    description = "Final solution",
                    equation = "x = 8/2",
                    result = "x = 4",
                    explanation = "Divide both sides by 2."
                )
            ),
            confidence = 0.88f,
            processingTime = 2000L,
            tokensUsed = 89,
            model = "Gemma-3n-E2B-it-int4"
        )
    }
    
    private fun generateConceptExplanation(concept: String, subject: Subject): AIResponse {
        return AIResponse(
            content = "Let me explain the concept of '$concept' in $subject:\n\nThis is a fundamental concept that helps you understand...",
            mathSteps = emptyList(),
            confidence = 0.90f,
            processingTime = 1000L,
            tokensUsed = 125,
            model = "Gemma-3n-E2B-it-int4"
        )
    }
    
    private fun generateImageProblemSolution(problem: ImageMathProblem): AIResponse {
        return AIResponse(
            content = "Based on the extracted problem '${problem.extractedText}', here's the solution:",
            mathSteps = listOf(
                MathStep(
                    stepNumber = 1,
                    description = "Problem analysis",
                    equation = problem.extractedText,
                    result = "Equation type: ${problem.problemType}",
                    explanation = "This is a ${problem.difficulty.lowercase()} ${problem.problemType.replace("_", " ")}."
                )
            ),
            confidence = problem.confidence,
            processingTime = 1000L,
            tokensUsed = 78,
            model = "Gemma-3n-E2B-it-int4"
        )
    }
} 