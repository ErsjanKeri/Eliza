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
 * Room entity for courses.
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
    val totalLessons: Int,
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
 * Room entity for lessons.
 */
@Entity(
    tableName = "lessons",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class LessonEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val lessonNumber: Int,
    val title: String,
    val markdownContent: String,
    val imageReferences: List<String> = emptyList(),
    val estimatedReadingTime: Int,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for exercises.
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val lessonId: String,
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