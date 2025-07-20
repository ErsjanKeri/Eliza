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

package com.example.ai.edge.eliza.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a complete course in the AI tutoring system.
 * Similar to how Gallery manages model allowlists, courses will be downloaded
 * and stored locally with this structure.
 */
@Serializable
data class Course(
    val id: String,
    val title: String,
    val subject: Subject,
    val grade: String,
    val description: String,
    val lessons: List<Lesson>,
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
 * Represents a lesson/chapter within a course.
 */
@Serializable
data class Lesson(
    val id: String,
    val courseId: String,
    val lessonNumber: Int,
    val title: String,
    val markdownContent: String,
    val imageReferences: List<String> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val estimatedReadingTime: Int, // in minutes
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents an exercise within a lesson.
 */
@Serializable
data class Exercise(
    val id: String,
    val lessonId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isCompleted: Boolean = false,
    val userAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents AI-generated practice questions based on original exercises.
 */
@Serializable
data class Trial(
    val id: String,
    val originalExerciseId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isCompleted: Boolean = false,
    val userAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents different subjects available in the tutoring system.
 */
@Serializable
enum class Subject(val displayName: String) {
    ALGEBRA("Algebra"),
    GEOMETRY("Geometry"),
    CALCULUS("Calculus"),
    STATISTICS("Statistics"),
    ARITHMETIC("Arithmetic"),
    TRIGONOMETRY("Trigonometry"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry")
}

/**
 * Represents difficulty levels for exercises and trials.
 */
@Serializable
enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
} 