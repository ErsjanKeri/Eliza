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

/**
 * Room entity for user progress per course.
 * UPDATED: Renamed lesson references to chapter references.
 */
@Entity(
    tableName = "user_progress",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class UserProgressEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val completedChapters: Int = 0, // RENAMED from completedLessons
    val totalChapters: Int = 0, // RENAMED from totalLessons
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val timeSpentMinutes: Long = 0,
    val streakDays: Int = 0,
    val lastStudiedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for chapter progress.
 * UPDATED: Renamed from LessonProgressEntity to ChapterProgressEntity.
 */
@Entity(
    tableName = "chapter_progress", // RENAMED from "lesson_progress"
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChapterEntity::class, // UPDATED from LessonEntity
            parentColumns = ["id"],
            childColumns = ["chapterId"], // UPDATED from lessonId
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ChapterProgressEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String, // RENAMED from lessonId
    val userId: String,
    val isCompleted: Boolean = false,
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val timeSpentMinutes: Long = 0,
    val firstAccessAt: Long = System.currentTimeMillis(),
    val lastAccessAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * Room entity for user answers.
 */
@Entity(
    tableName = "user_answers",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class UserAnswerEntity(
    @PrimaryKey
    val id: String,
    val exerciseId: String,
    val trialId: String? = null,
    val userId: String,
    val selectedAnswer: Int,
    val isCorrect: Boolean,
    val timeSpentSeconds: Long = 0,
    val hintsUsed: Int = 0,
    val answeredAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for study sessions.
 * UPDATED: Renamed lesson references to chapter references.
 */
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val courseId: String? = null,
    val chapterId: String? = null, // RENAMED from lessonId
    val sessionType: String,
    val durationMinutes: Long = 0,
    val exercisesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

/**
 * Room entity for achievements.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String? = null,
    val requirementType: String,
    val requirementThreshold: Int,
    val requirementSubject: String? = null,
    val requirementDifficulty: String? = null,
    val rewardPoints: Int = 0,
    val unlockedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for learning statistics.
 * UPDATED: Renamed lesson references to chapter references and added video tracking.
 */
@Entity(tableName = "learning_stats")
data class LearningStatsEntity(
    @PrimaryKey
    val userId: String,
    val totalTimeMinutes: Long = 0,
    val totalChaptersCompleted: Int = 0, // RENAMED from totalLessonsCompleted
    val totalExercisesCompleted: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val coursesCompleted: Int = 0,
    val totalCourses: Int = 0,
    val chatSessionsCount: Int = 0,
    val imageProblemsCount: Int = 0,
    val videoExplanationsCount: Int = 0, // NEW: Track video explanation usage
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for weekly progress.
 * UPDATED: Renamed lesson references to chapter references.
 */
@Entity(
    tableName = "weekly_progress",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = LearningStatsEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class WeeklyProgressEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val weekStartDate: Long,
    val weekEndDate: Long,
    val minutesStudied: Long = 0,
    val chaptersCompleted: Int = 0, // RENAMED from lessonsCompleted
    val exercisesCompleted: Int = 0,
    val daysActive: Int = 0,
    val averageAccuracy: Float = 0f
) 