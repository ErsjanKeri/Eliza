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

package com.example.ai.edge.eliza.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.ai.edge.eliza.core.database.converter.Converters

/**
 * Room entity for chat sessions.
 * UPDATED: Now chapter-centric with user-specific sessions.
 */
@Entity(
    tableName = "chat_sessions",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val chapterId: String, // REQUIRED: Always linked to a chapter
    val courseId: String,
    val userId: String, // NEW: User-specific sessions
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val messageCount: Int = 0,
    val videoCount: Int = 0 // NEW: Track videos in session
)

/**
 * Room entity for chat messages.
 * UPDATED: Enhanced for video content support.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = VideoExplanationEntity::class,
            parentColumns = ["id"],
            childColumns = ["videoExplanationId"],
            onDelete = androidx.room.ForeignKey.SET_NULL
        )
    ]
)
@TypeConverters(Converters::class)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val content: String,
    val isUser: Boolean, // Following Gallery's pattern (true = user, false = agent)
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String, // MessageType enum as string
    val videoExplanationId: String? = null, // NEW: FK to video explanations
    val imageUri: String? = null,
    val mathSteps: List<MathStepEntity> = emptyList(),
    val status: String = "SENT",
    val relatedExerciseId: String? = null,
    val processingTimeMs: Long = 0L // NEW: Track AI response time
)

/**
 * Room entity for math steps in solutions.
 */
@Entity(
    tableName = "math_steps",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChatMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class MathStepEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val stepNumber: Int,
    val description: String,
    val equation: String? = null,
    val explanation: String? = null
)

/**
 * Room entity for image math problems.
 */
@Entity(tableName = "image_math_problems")
@TypeConverters(Converters::class)
data class ImageMathProblemEntity(
    @PrimaryKey
    val id: String,
    val imageUri: String,
    val extractedText: String,
    val problemType: String,
    val confidence: Float,
    val boundingBoxes: List<BoundingBoxEntity> = emptyList(),
    val processedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for bounding boxes.
 */
@Entity(
    tableName = "bounding_boxes",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ImageMathProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class BoundingBoxEntity(
    @PrimaryKey
    val id: String,
    val problemId: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float
) 