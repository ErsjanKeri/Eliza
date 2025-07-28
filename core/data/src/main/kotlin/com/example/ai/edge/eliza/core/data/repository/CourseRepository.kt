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

package com.example.ai.edge.eliza.core.data.repository

import com.example.ai.edge.eliza.core.model.Course
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.Chapter
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.Trial
import com.example.ai.edge.eliza.core.model.VideoExplanation
import com.example.ai.edge.eliza.core.model.ExerciseHelp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for course-related operations.
 * This interface defines all operations for managing courses, chapters, exercises, and trials.
 * UPDATED: Renamed lesson references to chapter references and added video explanation support.
 * Similar to how Gallery manages model allowlists, this handles course content management.
 */
interface CourseRepository {
    
    // Course operations
    fun getAllCourses(): Flow<List<Course>>
    fun getCourseById(courseId: String): Flow<Course?>
    fun getCoursesBySubject(subject: Subject): Flow<List<Course>>
    fun getDownloadedCourses(): Flow<List<Course>>
    suspend fun insertCourse(course: Course)
    suspend fun updateCourse(course: Course)
    suspend fun deleteCourse(courseId: String)
    suspend fun downloadCourse(courseId: String): Flow<DownloadProgress>
    
    // Chapter operations (RENAMED from lesson operations)
    fun getChaptersByCourse(courseId: String): Flow<List<Chapter>> // RENAMED
    fun getChapterById(chapterId: String): Flow<Chapter?> // RENAMED
    fun getChapterByNumber(courseId: String, chapterNumber: Int): Flow<Chapter?> // RENAMED
    suspend fun insertChapter(chapter: Chapter) // RENAMED
    suspend fun updateChapter(chapter: Chapter) // RENAMED
    suspend fun deleteChapter(chapterId: String) // RENAMED
    suspend fun markChapterCompleted(chapterId: String) // RENAMED
    
    // Exercise operations
    fun getExercisesByChapter(chapterId: String): Flow<List<Exercise>> // UPDATED from lessonId
    fun getExerciseById(exerciseId: String): Flow<Exercise?>
    fun getIncompleteExercises(chapterId: String): Flow<List<Exercise>> // UPDATED from lessonId
    suspend fun insertExercise(exercise: Exercise)
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exerciseId: String)
    suspend fun submitExerciseAnswer(exerciseId: String, answerIndex: Int): ExerciseResult
    suspend fun resetChapterProgress(chapterId: String) // NEW: Reset all exercises for retake
    
    // Trial operations (AI-generated practice questions)
    fun getTrialsByExercise(exerciseId: String): Flow<List<Trial>>
    fun getTrialById(trialId: String): Flow<Trial?>
    suspend fun generateTrialsForExercise(exerciseId: String, count: Int): List<Trial>
    suspend fun insertTrial(trial: Trial)
    suspend fun updateTrial(trial: Trial)
    suspend fun deleteTrial(trialId: String)
    suspend fun submitTrialAnswer(trialId: String, answerIndex: Int): TrialResult
    
    // NEW: Video explanation operations
    fun getVideoExplanationsByUser(userId: String): Flow<List<VideoExplanation>>
    fun getVideoExplanationsByChapter(chapterId: String, userId: String): Flow<List<VideoExplanation>>
    fun getVideoExplanationsByExercise(exerciseId: String, userId: String): Flow<List<VideoExplanation>>
    suspend fun insertVideoExplanation(videoExplanation: VideoExplanation)
    suspend fun deleteVideoExplanation(videoId: String)
    suspend fun updateVideoLastAccessed(videoId: String)
    
    // NEW: Exercise help operations
    fun getExerciseHelpByExercise(exerciseId: String, userId: String): Flow<List<ExerciseHelp>>
    suspend fun insertExerciseHelp(exerciseHelp: ExerciseHelp)
    suspend fun updateExerciseHelpFeedback(helpId: String, wasHelpful: Boolean)
    
    // Aggregate operations
    suspend fun getCourseProgress(courseId: String): CourseProgress
    suspend fun refreshCourseContent(courseId: String)
    suspend fun syncCourseProgress(courseId: String)
}

/**
 * Represents the download progress of a course.
 */
data class DownloadProgress(
    val courseId: String,
    val progress: Float, // 0.0 to 1.0
    val status: DownloadStatus,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null
)

/**
 * Download status states.
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Result of submitting an exercise answer.
 */
data class ExerciseResult(
    val exerciseId: String,
    val isCorrect: Boolean,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val explanation: String,
    val timeSpent: Long = 0L,
    val hintsUsed: Int = 0
)

/**
 * Result of submitting a trial answer.
 */
data class TrialResult(
    val trialId: String,
    val isCorrect: Boolean,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val explanation: String,
    val timeSpent: Long = 0L,
    val hintsUsed: Int = 0
)

/**
 * Course progress summary.
 * UPDATED: Renamed lesson references to chapter references.
 */
data class CourseProgress(
    val courseId: String,
    val completedChapters: Int, // RENAMED from completedLessons
    val totalChapters: Int, // RENAMED from totalLessons
    val completedExercises: Int,
    val totalExercises: Int,
    val averageScore: Float,
    val timeSpent: Long,
    val lastStudied: Long
) 