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
    val chapters: List<Chapter>, // RENAMED from lessons
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
 * Represents a chapter within a course.
 * RENAMED from Lesson to Chapter to better reflect content organization.
 */
@Serializable
data class Chapter(
    val id: String,
    val courseId: String,
    val chapterNumber: Int, // RENAMED from lessonNumber
    val title: String,
    val markdownContent: String,
    val imageReferences: List<String> = emptyList(),
    val exercises: List<Exercise> = emptyList(), // Exactly 5 exercises = test questions
    val estimatedReadingTime: Int, // in minutes
    val isCompleted: Boolean = false, // TRUE only when test score = 100%
    val testScore: Int? = null, // Latest test score (0-100)
    val testAttempts: Int = 0, // Number of test attempts
    val lastTestAttempt: Long? = null, // Timestamp of last test
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents an exercise within a chapter.
 */
@Serializable
data class Exercise(
    val id: String,
    val chapterId: String, // UPDATED from lessonId to chapterId
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
 * Represents a video explanation requested by a user.
 * NEW: Core model for the video explanation system.
 */
@Serializable
data class VideoExplanation(
    val id: String,
    val userId: String, // User-specific, no sharing between users
    val chapterId: String? = null, // For chapter video explanations
    val exerciseId: String? = null, // For exercise video explanations
    val requestType: VideoRequestType,
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
 * Types of video explanation requests.
 * NEW: Distinguishes between chapter and exercise video requests.
 */
@Serializable
enum class VideoRequestType {
    CHAPTER_EXPLANATION, // Videos for general chapter understanding
    EXERCISE_HELP        // Videos for specific exercise wrong answers
}

/**
 * Represents help provided for wrong exercise answers.
 * NEW: Core model for the exercise help system.
 */
@Serializable
data class ExerciseHelp(
    val id: String,
    val exerciseId: String,
    val userId: String,
    val incorrectAnswer: Int,
    val correctAnswer: Int,
    val userQuestion: String? = null,
    val helpType: HelpType,
    val explanation: String? = null, // For local AI explanations
    val videoExplanation: VideoExplanation? = null, // For video explanations
    val createdAt: Long = System.currentTimeMillis(),
    val wasHelpful: Boolean? = null // User feedback
)

/**
 * Types of help provided for exercises.
 * NEW: Distinguishes between local AI and video explanations.
 */
@Serializable
enum class HelpType {
    LOCAL_AI,           // AI-generated explanation using local model
    VIDEO_EXPLANATION,  // Video explanation from API
    NEW_TRIAL          // Generated new trial question
}

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