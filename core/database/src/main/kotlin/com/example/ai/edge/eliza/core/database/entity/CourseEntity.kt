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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.ai.edge.eliza.core.database.converter.Converters

/**
 * Room entity for courses.
 * UPDATED: Renamed lesson references to chapter references.
 */
@Entity(tableName = "courses")
@TypeConverters(Converters::class)
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val grade: String,
    val description: String,
    val totalChapters: Int, // RENAMED from totalLessons
    val estimatedHours: Int,
    val imageUrl: String? = null,
    val isDownloaded: Boolean = false,
    val downloadUrl: String? = null,
    val sizeInBytes: Long = 0L,
    val version: String = "1.0.0",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for chapters.
 * RENAMED from LessonEntity to ChapterEntity.
 */
@Entity(
    tableName = "chapters", // RENAMED from "lessons"
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val chapterNumber: Int, // RENAMED from lessonNumber
    val title: String,
    val markdownContent: String,
    val imageReferences: List<String> = emptyList(),
    val estimatedReadingTime: Int,
    val isCompleted: Boolean = false, // TRUE only when test score = 100%
    val testScore: Int? = null, // Latest test score (0-100)
    @ColumnInfo(defaultValue = "0")
    val testAttempts: Int = 0, // Number of test attempts
    val lastTestAttempt: Long? = null, // Timestamp of last test
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for exercises.
 * UPDATED: Changed foreign key reference from lessons to chapters.
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChapterEntity::class, // UPDATED from LessonEntity
            parentColumns = ["id"],
            childColumns = ["chapterId"], // UPDATED from lessonId
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String, // RENAMED from lessonId
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val userAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for AI-generated trials.
 */
@Entity(
    tableName = "trials",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalExerciseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class TrialEntity(
    @PrimaryKey
    val id: String,
    val originalExerciseId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val userAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for video explanations.
 * NEW: Core entity for the video explanation system.
 */
@Entity(
    tableName = "video_explanations",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class VideoExplanationEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // User-specific, no sharing between users
    val chapterId: String? = null, // For chapter video explanations
    val exerciseId: String? = null, // For exercise video explanations
    val requestType: String, // VideoRequestType enum as string
    val userQuestion: String,
    val contextData: String, // JSON of chapter markdown or exercise data
    val videoUrl: String, // Original API URL
    val localFilePath: String, // Local storage path
    val fileSizeBytes: Long,
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for exercise help.
 * NEW: Core entity for the exercise help system.
 */
@Entity(
    tableName = "exercise_help",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
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
data class ExerciseHelpEntity(
    @PrimaryKey
    val id: String,
    val exerciseId: String,
    val userId: String,
    val incorrectAnswer: Int,
    val correctAnswer: Int,
    val userQuestion: String? = null,
    val helpType: String, // HelpType enum as string
    val explanation: String? = null, // For local AI explanations
    val videoExplanationId: String? = null, // FK to video_explanations
    val createdAt: Long = System.currentTimeMillis(),
    val wasHelpful: Boolean? = null // User feedback
) 