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
import com.example.ai.edge.eliza.core.model.VideoRequestResult
import com.example.ai.edge.eliza.core.model.VideoExplanation
import com.example.ai.edge.eliza.core.model.VideoRequestType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock implementation of ChatRepository for development and testing.
 * Provides realistic fake data to support development without requiring actual AI backend.
 * UPDATED: Now supports chapter-based chat sessions with video explanations.
 * 
 * Mock Behavior:
 * - Data re-initializes on app start (when repository is created)
 * - Changes persist during runtime for development (mutable collections)
 * - Perfect for demo development without real backend
 */
@Singleton
class MockChatRepository @Inject constructor() : ChatRepository {
    
    // Mock data storage - mutable for runtime changes, re-initialized on app start
    private val chatSessions = mutableListOf<ChatSession>()
    private val chatMessages = mutableListOf<ChatMessage>()
    private val imageMathProblems = mutableListOf<ImageMathProblem>()
    private val videoExplanations = mutableListOf<VideoExplanation>()
    
    // Model state management
    private var currentModelState = ModelState(
        isInitialized = true,
        isDownloading = false,
        downloadProgress = 1.0f,
        modelName = "Gemma-3n-E4B-it-int4"
    )
    
    init {
        // Initialize mock data on app start
        initializeMockData()
    }
    
    // Chat Session operations (UPDATED for chapter-based organization)
    override fun getAllChatSessions(): Flow<List<ChatSession>> = 
        flowOf(chatSessions.sortedByDescending { it.lastMessageAt })
    
    override fun getChatSessionById(sessionId: String): Flow<ChatSession?> = 
        flowOf(chatSessions.find { it.id == sessionId })
    
    override fun getActiveChatSessions(): Flow<List<ChatSession>> = 
        flowOf(chatSessions.filter { it.isActive }.sortedByDescending { it.lastMessageAt })
    
    override fun getChatSessionsByCourse(courseId: String): Flow<List<ChatSession>> = 
        flowOf(chatSessions.filter { it.courseId == courseId }.sortedByDescending { it.lastMessageAt })
    
    // NEW: Chapter-specific session operations
    override fun getChatSessionsByChapter(chapterId: String): Flow<List<ChatSession>> = 
        flowOf(chatSessions.filter { it.chapterId == chapterId }.sortedByDescending { it.lastMessageAt })
    
    override fun getChatSessionsByChapterAndUser(chapterId: String, userId: String): Flow<List<ChatSession>> = 
        flowOf(chatSessions.filter { it.chapterId == chapterId && it.userId == userId }.sortedByDescending { it.lastMessageAt })
    
    override fun getChatSessionsByUser(userId: String): Flow<List<ChatSession>> = 
        flowOf(chatSessions.filter { it.userId == userId }.sortedByDescending { it.lastMessageAt })
    
    override fun getActiveSessionForChapterAndUser(chapterId: String, userId: String): Flow<ChatSession?> = 
        flowOf(chatSessions.find { it.chapterId == chapterId && it.userId == userId && it.isActive })
    
    // UPDATED: Chapter-based session creation
    override suspend fun createChatSession(
        title: String,
        chapterId: String,
        courseId: String,
        userId: String
    ): ChatSession {
        val session = ChatSession(
            id = "session_${UUID.randomUUID()}",
            title = title,
            chapterId = chapterId,
            courseId = courseId,
            userId = userId,
            createdAt = System.currentTimeMillis(),
            lastMessageAt = System.currentTimeMillis(),
            isActive = true,
            messageCount = 0,
            videoCount = 0
        )
        
        chatSessions.add(session)
        return session
    }
    
    override suspend fun updateChatSession(session: ChatSession) {
        val index = chatSessions.indexOfFirst { it.id == session.id }
        if (index != -1) {
            chatSessions[index] = session
        }
    }
    
    override suspend fun deleteChatSession(sessionId: String) {
        chatSessions.removeIf { it.id == sessionId }
        chatMessages.removeIf { it.sessionId == sessionId }
    }
    
    override suspend fun deactivateChatSession(sessionId: String) {
        chatSessions.find { it.id == sessionId }?.let { session ->
            updateChatSession(session.copy(isActive = false))
        }
    }
    
    // Chat Message operations (UPDATED with video support)
    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = 
        flowOf(chatMessages.filter { it.sessionId == sessionId }.sortedBy { it.timestamp })
    
    override fun getMessageById(messageId: String): Flow<ChatMessage?> = 
        flowOf(chatMessages.find { it.id == messageId })
    
    override fun getRecentMessages(sessionId: String, limit: Int): Flow<List<ChatMessage>> = 
        flowOf(chatMessages.filter { it.sessionId == sessionId }.sortedByDescending { it.timestamp }.take(limit))
    
    override fun getMessagesWithVideos(): Flow<List<ChatMessage>> = 
        flowOf(chatMessages.filter { it.videoExplanation != null }.sortedByDescending { it.timestamp })
    
    override fun getVideoMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = 
        flowOf(chatMessages.filter { it.sessionId == sessionId && it.videoExplanation != null }.sortedByDescending { it.timestamp })
    
    override suspend fun sendMessage(sessionId: String, message: String, imageUri: String?): Flow<ChatResponse> = flow {
        // Create user message
        val userMessage = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            sessionId = sessionId,
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis(),
            messageType = if (imageUri != null) MessageType.IMAGE else MessageType.TEXT,
            videoExplanation = null,
            imageUri = imageUri
        )
        
        chatMessages.add(userMessage)
        
        // Update session message count
        chatSessions.find { it.id == sessionId }?.let { session ->
            updateChatSession(session.copy(
                messageCount = session.messageCount + 1,
                lastMessageAt = System.currentTimeMillis()
            ))
        }
        
        emit(ChatResponse(
            messageId = userMessage.id,
            sessionId = sessionId,
            content = "Message received",
            status = ChatResponseStatus.COMPLETE,
            processingTime = 100L
        ))
        
        // Simulate AI response after delay
        delay(1500)
        
        val aiResponse = generateMockAIResponse(message)
        val aiMessage = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            sessionId = sessionId,
            content = aiResponse.content,
            isUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT,
            mathSteps = aiResponse.mathSteps,
            processingTimeMs = aiResponse.processingTime
        )
        
        chatMessages.add(aiMessage)
        
        // Update session message count
        chatSessions.find { it.id == sessionId }?.let { session ->
            updateChatSession(session.copy(
                messageCount = session.messageCount + 1,
                lastMessageAt = System.currentTimeMillis()
            ))
        }
        
        emit(ChatResponse(
            messageId = aiMessage.id,
            sessionId = sessionId,
            content = aiResponse.content,
            mathSteps = aiResponse.mathSteps,
            status = ChatResponseStatus.COMPLETE,
            confidence = aiResponse.confidence,
            processingTime = aiResponse.processingTime,
            tokensGenerated = aiResponse.tokensUsed
        ))
    }
    
    override suspend fun sendImageMessage(sessionId: String, imageUri: String, question: String?): Flow<ChatResponse> = 
        sendMessage(sessionId, question ?: "What do you see in this image?", imageUri)
    
    // NEW: Video explanation request
    override suspend fun requestVideoExplanation(
        sessionId: String,
        chapterId: String,
        userQuestion: String
    ): Flow<VideoRequestResult> = flow {
        emit(VideoRequestResult.Loading)
        delay(2000) // Simulate API call
        
        // Create mock video explanation
        val videoExplanation = VideoExplanation(
            id = "video_${UUID.randomUUID()}",
            userId = "user_demo", // In real app, this would come from auth
            chapterId = chapterId,
            exerciseId = null,
            requestType = VideoRequestType.CHAPTER_EXPLANATION,
            userQuestion = userQuestion,
            contextData = "Mock chapter content for $chapterId",
            videoUrl = "https://example.com/video_explanation.mp4",
            localFilePath = "/storage/videos/explanation_${System.currentTimeMillis()}.mp4",
            fileSizeBytes = 1800000L, // 1.8MB
            durationSeconds = 210 // 3.5 minutes
        )
        
        videoExplanations.add(videoExplanation)
        
        // Create video message in chat
        val videoMessage = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            sessionId = sessionId,
            content = "Here's a video explanation for your question: \"$userQuestion\"",
            isUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.VIDEO,
            videoExplanation = videoExplanation,
            processingTimeMs = 2000L
        )
        
        chatMessages.add(videoMessage)
        
        // Update session counts
        chatSessions.find { it.id == sessionId }?.let { session ->
            updateChatSession(session.copy(
                messageCount = session.messageCount + 1,
                videoCount = session.videoCount + 1,
                lastMessageAt = System.currentTimeMillis()
            ))
        }
        
        emit(VideoRequestResult.Success(videoExplanation))
    }
    
    override suspend fun insertMessage(message: ChatMessage) {
        chatMessages.removeIf { it.id == message.id }
        chatMessages.add(message)
    }
    
    override suspend fun updateMessage(message: ChatMessage) {
        val index = chatMessages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            chatMessages[index] = message
        }
    }
    
    override suspend fun deleteMessage(messageId: String) {
        chatMessages.removeIf { it.id == messageId }
    }
    
    override suspend fun deleteMessagesBySession(sessionId: String) {
        chatMessages.removeIf { it.sessionId == sessionId }
    }
    
    // AI Model operations
    override fun getModelState(): Flow<ModelState> = flowOf(currentModelState)
    
    override suspend fun initializeModel(): Flow<ModelInitializationResult> = flow {
        delay(1000)
        currentModelState = currentModelState.copy(isInitialized = true)
        emit(ModelInitializationResult(
            isSuccess = true,
            modelName = "Gemma-3n-E4B-it-int4",
            initializationTime = 1000L,
            memoryUsage = 4500000000L // 4.5GB
        ))
    }
    
    override suspend fun downloadModel(): Flow<ModelDownloadProgress> = flow {
        val totalBytes = 4405655031L
        for (progress in 0..100 step 10) {
            delay(200)
            emit(ModelDownloadProgress(
                progress = progress / 100f,
                status = if (progress == 100) ModelDownloadStatus.COMPLETED else ModelDownloadStatus.DOWNLOADING,
                bytesDownloaded = (totalBytes * progress / 100),
                totalBytes = totalBytes,
                downloadSpeed = 10_000_000L // 10 MB/s
            ))
        }
        currentModelState = currentModelState.copy(downloadProgress = 1.0f)
    }
    
    override suspend fun generateResponse(sessionId: String, prompt: String): Flow<AIResponse> = flow {
        delay(1000)
        emit(generateMockAIResponse(prompt))
    }
    
    override suspend fun generateMathSolution(problem: String, steps: Boolean): Flow<AIResponse> = flow {
        delay(1500)
        val mathSteps = if (steps) {
            listOf(
                MathStep(1, "Identify the equation", "2x + 5 = 15", "This is a linear equation"),
                MathStep(2, "Subtract 5 from both sides", "2x = 10", "Isolate the term with x"),
                MathStep(3, "Divide by 2", "x = 5", "Solve for x")
            )
        } else emptyList()
        
        emit(AIResponse(
            content = "To solve this problem, we need to isolate x. The solution is x = 5.",
            mathSteps = mathSteps,
            confidence = 0.95f,
            processingTime = 1500L,
            tokensUsed = 75
        ))
    }
    
    override suspend fun explainConcept(concept: String, subject: Subject): Flow<AIResponse> = flow {
        delay(1200)
        emit(AIResponse(
            content = "Let me explain $concept in ${subject.displayName}. This is a fundamental concept that helps you understand...",
            confidence = 0.88f,
            processingTime = 1200L,
            tokensUsed = 120
        ))
    }
    
    // Image Processing operations
    override suspend fun processImage(imageUri: String): Flow<ImageProcessingResult> = flow {
        delay(2000)
        val mathProblem = ImageMathProblem(
            id = "img_${UUID.randomUUID()}",
            imageUri = imageUri,
            extractedText = "Solve: 3x + 7 = 22",
            problemType = ProblemType.EQUATION,
            confidence = 0.92f
        )
        imageMathProblems.add(mathProblem)
        
        emit(ImageProcessingResult(
            imageUri = imageUri,
            extractedText = mathProblem.extractedText,
            mathProblem = mathProblem,
            confidence = mathProblem.confidence,
            processingTime = 2000L
        ))
    }
    
    override suspend fun extractMathProblem(imageUri: String): ImageMathProblem? {
        return imageMathProblems.find { it.imageUri == imageUri }
    }
    
    override fun getImageMathProblems(): Flow<List<ImageMathProblem>> = 
        flowOf(imageMathProblems.sortedByDescending { it.processedAt })
    
    override suspend fun solveImageProblem(problemId: String): Flow<AIResponse> = flow {
        delay(1800)
        emit(AIResponse(
            content = "Looking at this math problem, I can help you solve it step by step...",
            confidence = 0.91f,
            processingTime = 1800L,
            tokensUsed = 95
        ))
    }
    
    // Conversation management
    override suspend fun clearConversation(sessionId: String) {
        chatMessages.removeIf { it.sessionId == sessionId }
        chatSessions.find { it.id == sessionId }?.let { session ->
            updateChatSession(session.copy(messageCount = 0, videoCount = 0))
        }
    }
    
    override suspend fun exportConversation(sessionId: String, format: ExportFormat): String {
        val messages = chatMessages.filter { it.sessionId == sessionId }
        return when (format) {
            ExportFormat.TEXT -> messages.joinToString("\n") { "${if (it.isUser) "User" else "AI"}: ${it.content}" }
            ExportFormat.JSON -> "{\"messages\": [${messages.size} messages]}"
            ExportFormat.PDF -> "PDF export not implemented"
            ExportFormat.MARKDOWN -> messages.joinToString("\n\n") { "**${if (it.isUser) "User" else "AI"}**: ${it.content}" }
        }
    }
    
    override suspend fun getConversationSummary(sessionId: String): ConversationSummary {
        val session = chatSessions.find { it.id == sessionId }
        val messages = chatMessages.filter { it.sessionId == sessionId }
        
        return ConversationSummary(
            sessionId = sessionId,
            messageCount = messages.size,
            videoCount = messages.count { it.videoExplanation != null },
            topicsDiscussed = listOf("Linear Equations", "Problem Solving"),
            problemsSolved = 2,
            averageResponseTime = 1400L,
            totalTokensUsed = 450,
            difficulty = "Medium",
            learningObjectives = listOf("Understand linear equations", "Practice problem solving"),
            chapterId = session?.chapterId,
            courseId = session?.courseId
        )
    }
    
    // Helper function to generate realistic AI responses
    private fun generateMockAIResponse(userMessage: String): AIResponse {
        val responses = listOf(
            "I understand you're asking about ${userMessage.take(20)}... Let me help you with that.",
            "That's a great question! Here's how I would approach this problem...",
            "I can see you're working on this concept. Let me break it down step by step...",
            "This is a common question in mathematics. Here's the explanation you need..."
        )
        
        return AIResponse(
            content = responses.random(),
            confidence = Random.nextFloat() * (0.98f - 0.85f) + 0.85f,
            processingTime = Random.nextLong(800L, 2001L),
            tokensUsed = Random.nextInt(30, 151)
        )
    }
    
    // Initialize mock data on app start
    private fun initializeMockData() {
        // Clear existing data
        chatSessions.clear()
        chatMessages.clear()
        imageMathProblems.clear()
        videoExplanations.clear()
        
        // Create mock chat sessions for demo
        val demoSession = ChatSession(
            id = "session_demo_linear",
            title = "Understanding Linear Equations",
            chapterId = "chapter_linear_eq",
            courseId = "course_algebra_1",
            userId = "user_demo",
            createdAt = System.currentTimeMillis() - 3600000L, // 1 hour ago
            lastMessageAt = System.currentTimeMillis() - 600000L, // 10 minutes ago
            isActive = true,
            messageCount = 4,
            videoCount = 1
        )
        
        chatSessions.add(demoSession)
        
        // Create mock messages
        val demoMessages = listOf(
            ChatMessage(
                id = "msg_demo_1",
                sessionId = "session_demo_linear",
                content = "I'm having trouble understanding how to solve linear equations. Can you help?",
                isUser = true,
                timestamp = System.currentTimeMillis() - 3600000L,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "msg_demo_2", 
                sessionId = "session_demo_linear",
                content = "I'd be happy to help you with linear equations! A linear equation is an equation where the variable has a power of 1. The basic goal is to isolate the variable on one side of the equation.",
                isUser = false,
                timestamp = System.currentTimeMillis() - 3550000L,
                messageType = MessageType.TEXT,
                processingTimeMs = 1200L
            ),
            ChatMessage(
                id = "msg_demo_3",
                sessionId = "session_demo_linear",
                content = "Can you show me a visual example?",
                isUser = true,
                timestamp = System.currentTimeMillis() - 1800000L,
                messageType = MessageType.TEXT
            )
        )
        
        chatMessages.addAll(demoMessages)
        
        // Create mock video explanation
        val mockVideo = VideoExplanation(
            id = "video_demo_linear",
            userId = "user_demo",
            chapterId = "chapter_linear_eq",
            exerciseId = null,
            requestType = VideoRequestType.CHAPTER_EXPLANATION,
            userQuestion = "Can you show me a visual example?",
            contextData = "Linear equations chapter content",
            videoUrl = "https://example.com/linear_equations_demo.mp4",
            localFilePath = "/storage/videos/linear_demo.mp4",
            fileSizeBytes = 1750000L,
            durationSeconds = 195,
            createdAt = System.currentTimeMillis() - 1200000L,
            lastAccessedAt = System.currentTimeMillis() - 600000L
        )
        
        videoExplanations.add(mockVideo)
        
        // Add video message
        val videoMessage = ChatMessage(
            id = "msg_demo_4",
            sessionId = "session_demo_linear",
            content = "Here's a visual explanation of linear equations that should help clarify the concept!",
            isUser = false,
            timestamp = System.currentTimeMillis() - 1200000L,
            messageType = MessageType.VIDEO,
            videoExplanation = mockVideo,
            processingTimeMs = 2100L
        )
        
        chatMessages.add(videoMessage)
    }
} 