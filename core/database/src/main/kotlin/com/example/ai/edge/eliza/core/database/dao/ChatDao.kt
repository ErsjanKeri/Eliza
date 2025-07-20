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

package com.example.ai.edge.eliza.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ai.edge.eliza.core.database.entity.ChatMessageEntity
import com.example.ai.edge.eliza.core.database.entity.ChatSessionEntity
import com.example.ai.edge.eliza.core.database.entity.ImageMathProblemEntity
import com.example.ai.edge.eliza.core.database.entity.MathStepEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat-related operations.
 */
@Dao
interface ChatDao {
    
    // Chat Session operations
    @Query("SELECT * FROM chat_sessions ORDER BY lastMessageAt DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    fun getChatSessionById(sessionId: String): Flow<ChatSessionEntity?>
    
    @Query("SELECT * FROM chat_sessions WHERE isActive = 1 ORDER BY lastMessageAt DESC")
    fun getActiveChatSessions(): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE subject = :subject ORDER BY lastMessageAt DESC")
    fun getChatSessionsBySubject(subject: String): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE courseId = :courseId ORDER BY lastMessageAt DESC")
    fun getChatSessionsByCourse(courseId: String): Flow<List<ChatSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSessionEntity)
    
    @Update
    suspend fun updateChatSession(session: ChatSessionEntity)
    
    @Delete
    suspend fun deleteChatSession(session: ChatSessionEntity)
    
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteChatSessionById(sessionId: String)
    
    @Query("UPDATE chat_sessions SET isActive = 0 WHERE id = :sessionId")
    suspend fun deactivateChatSession(sessionId: String)
    
    // Chat Message operations
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    fun getMessageById(messageId: String): Flow<ChatMessageEntity?>
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(sessionId: String, limit: Int): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId AND isUser = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLastAIMessage(sessionId: String): Flow<ChatMessageEntity?>
    
    @Query("SELECT * FROM chat_messages WHERE imageUri IS NOT NULL ORDER BY timestamp DESC")
    fun getMessagesWithImages(): Flow<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)
    
    // Math Step operations
    @Query("SELECT * FROM math_steps WHERE messageId = :messageId ORDER BY stepNumber ASC")
    fun getMathStepsByMessage(messageId: String): Flow<List<MathStepEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMathStep(step: MathStepEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMathSteps(steps: List<MathStepEntity>)
    
    @Delete
    suspend fun deleteMathStep(step: MathStepEntity)
    
    @Query("DELETE FROM math_steps WHERE messageId = :messageId")
    suspend fun deleteMathStepsByMessage(messageId: String)
    
    // Image Math Problem operations
    @Query("SELECT * FROM image_math_problems ORDER BY processedAt DESC")
    fun getAllImageMathProblems(): Flow<List<ImageMathProblemEntity>>
    
    @Query("SELECT * FROM image_math_problems WHERE id = :problemId")
    fun getImageMathProblemById(problemId: String): Flow<ImageMathProblemEntity?>
    
    @Query("SELECT * FROM image_math_problems WHERE problemType = :type ORDER BY processedAt DESC")
    fun getImageMathProblemsByType(type: String): Flow<List<ImageMathProblemEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageMathProblem(problem: ImageMathProblemEntity)
    
    @Update
    suspend fun updateImageMathProblem(problem: ImageMathProblemEntity)
    
    @Delete
    suspend fun deleteImageMathProblem(problem: ImageMathProblemEntity)
    
    @Query("DELETE FROM image_math_problems WHERE id = :problemId")
    suspend fun deleteImageMathProblemById(problemId: String)
    
    // Aggregate queries
    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun getMessageCountBySession(sessionId: String): Int
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId AND isUser = 0")
    suspend fun getAIMessageCount(sessionId: String): Int
    
    @Query("SELECT COUNT(*) FROM chat_sessions WHERE isActive = 1")
    suspend fun getActiveSessionCount(): Int
    
    @Query("SELECT COUNT(*) FROM image_math_problems WHERE processedAt > :timestamp")
    suspend fun getImageProblemsCountSince(timestamp: Long): Int
} 