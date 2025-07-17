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
 * Represents user progress for a specific course.
 */
@Serializable
data class UserProgress(
    val id: String,
    val courseId: String,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val timeSpentMinutes: Long = 0,
    val streakDays: Int = 0,
    val lastStudiedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val completionPercentage: Float
        get() = if (totalLessons > 0) (completedLessons.toFloat() / totalLessons.toFloat()) * 100f else 0f
    
    val accuracyPercentage: Float
        get() = if (totalAnswers > 0) (correctAnswers.toFloat() / totalAnswers.toFloat()) * 100f else 0f
}

/**
 * Represents progress for a specific lesson.
 */
@Serializable
data class LessonProgress(
    val id: String,
    val lessonId: String,
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
 * Represents user's answer to an exercise.
 */
@Serializable
data class UserAnswer(
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
 * Represents a study session.
 */
@Serializable
data class StudySession(
    val id: String,
    val userId: String,
    val courseId: String? = null,
    val lessonId: String? = null,
    val sessionType: SessionType,
    val durationMinutes: Long = 0,
    val exercisesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

/**
 * Types of study sessions.
 */
@Serializable
enum class SessionType {
    LESSON_STUDY,
    PRACTICE_EXERCISES,
    AI_CHAT_TUTORING,
    IMAGE_PROBLEM_SOLVING,
    REVIEW_SESSION
}

/**
 * Represents user achievements.
 */
@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String? = null,
    val requirement: AchievementRequirement,
    val rewardPoints: Int = 0,
    val unlockedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Requirements for unlocking achievements.
 */
@Serializable
data class AchievementRequirement(
    val type: AchievementType,
    val threshold: Int,
    val subject: Subject? = null,
    val difficulty: Difficulty? = null
)

/**
 * Types of achievements.
 */
@Serializable
enum class AchievementType {
    LESSONS_COMPLETED,
    EXERCISES_COMPLETED,
    CORRECT_ANSWERS,
    STREAK_DAYS,
    TIME_SPENT,
    COURSE_COMPLETED,
    PERFECT_SCORE,
    CHAT_SESSIONS,
    IMAGE_PROBLEMS_SOLVED
}

/**
 * Represents user's overall learning statistics.
 */
@Serializable
data class LearningStats(
    val userId: String,
    val totalTimeMinutes: Long = 0,
    val totalLessonsCompleted: Int = 0,
    val totalExercisesCompleted: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val coursesCompleted: Int = 0,
    val totalCourses: Int = 0,
    val chatSessionsCount: Int = 0,
    val imageProblemsCount: Int = 0,
    val subjectStats: Map<Subject, SubjectStats> = emptyMap(),
    val weeklyProgress: List<WeeklyProgress> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Statistics for a specific subject.
 */
@Serializable
data class SubjectStats(
    val subject: Subject,
    val timeSpentMinutes: Long = 0,
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 0,
    val exercisesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val averageAccuracy: Float = 0f,
    val strongestTopics: List<String> = emptyList(),
    val weakestTopics: List<String> = emptyList()
)

/**
 * Progress for a specific week.
 */
@Serializable
data class WeeklyProgress(
    val weekStartDate: Long,
    val weekEndDate: Long,
    val minutesStudied: Long = 0,
    val lessonsCompleted: Int = 0,
    val exercisesCompleted: Int = 0,
    val daysActive: Int = 0,
    val averageAccuracy: Float = 0f
) 