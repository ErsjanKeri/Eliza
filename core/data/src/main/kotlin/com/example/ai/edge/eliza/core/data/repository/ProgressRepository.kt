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

import com.example.ai.edge.eliza.core.model.Achievement
import com.example.ai.edge.eliza.core.model.LearningStats
import com.example.ai.edge.eliza.core.model.ChapterProgress
import com.example.ai.edge.eliza.core.model.StudySession
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.UserAnswer
import com.example.ai.edge.eliza.core.model.UserProgress
import com.example.ai.edge.eliza.core.model.WeeklyProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for progress-related operations.
 * This interface defines all operations for managing user progress,
 * learning analytics, achievements, and study sessions.
 * UPDATED: Now uses chapter-based terminology throughout.
 */
interface ProgressRepository {
    
    // User Progress operations
    fun getUserProgressByCourse(courseId: String): Flow<UserProgress?>
    fun getAllUserProgress(): Flow<List<UserProgress>>
    suspend fun updateUserProgress(progress: UserProgress)
    suspend fun incrementChapterProgress(courseId: String, chapterId: String) // UPDATED: lesson → chapter
    suspend fun incrementExerciseProgress(courseId: String, exerciseId: String, isCorrect: Boolean)
    suspend fun addStudyTime(courseId: String, minutes: Long)
    suspend fun updateStreak(courseId: String, days: Int)
    
    // Chapter Progress operations (UPDATED: lesson → chapter)
    fun getChapterProgress(chapterId: String, userId: String): Flow<ChapterProgress?>
    fun getUserChapterProgress(userId: String): Flow<List<ChapterProgress>>
    fun getCompletedChapters(userId: String): Flow<List<ChapterProgress>>
    suspend fun startChapter(chapterId: String, userId: String)
    suspend fun completeChapter(chapterId: String, userId: String, timeSpent: Long)
    suspend fun updateChapterProgress(progress: ChapterProgress)
    
    // User Answer operations
    fun getUserAnswersByExercise(exerciseId: String, userId: String): Flow<List<UserAnswer>>
    fun getAllUserAnswers(userId: String): Flow<List<UserAnswer>>
    fun getCorrectAnswers(userId: String): Flow<List<UserAnswer>>
    suspend fun recordAnswer(answer: UserAnswer)
    suspend fun getAnswerStats(userId: String): AnswerStats
    
    // Study Session operations (UPDATED: lessonId → chapterId)
    fun getStudySessionsByUser(userId: String): Flow<List<StudySession>>
    fun getStudySessionsByCourse(userId: String, courseId: String): Flow<List<StudySession>>
    fun getActiveStudySession(userId: String): Flow<StudySession?>
    suspend fun startStudySession(userId: String, courseId: String?, chapterId: String?, sessionType: String): StudySession
    suspend fun endStudySession(sessionId: String, exercisesCompleted: Int, correctAnswers: Int)
    suspend fun updateStudySession(session: StudySession)
    
    // Achievement operations
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    fun getLockedAchievements(): Flow<List<Achievement>>
    suspend fun checkAndUnlockAchievements(userId: String): List<Achievement>
    suspend fun unlockAchievement(achievementId: String)
    suspend fun createCustomAchievement(achievement: Achievement)
    
    // Learning Analytics operations
    fun getLearningStats(userId: String): Flow<LearningStats?>
    fun getWeeklyProgress(userId: String): Flow<List<WeeklyProgress>>
    fun getWeeklyProgressSince(userId: String, startDate: Long): Flow<List<WeeklyProgress>>
    suspend fun updateLearningStats(stats: LearningStats)
    suspend fun generateWeeklyReport(userId: String): WeeklyReport
    suspend fun generateMonthlyReport(userId: String): MonthlyReport
    
    // Performance Analytics
    suspend fun getPerformanceMetrics(userId: String): PerformanceMetrics
    suspend fun getSubjectPerformance(userId: String, subject: Subject): SubjectPerformance
    suspend fun getStreakAnalysis(userId: String): StreakAnalysis
    suspend fun getLearningVelocity(userId: String): LearningVelocity
    suspend fun getTimeDistribution(userId: String): TimeDistribution
    
    // Recommendations (UPDATED: lesson → chapter)
    suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation>
    suspend fun getWeakAreaRecommendations(userId: String): List<WeakAreaRecommendation>
    suspend fun getNextChapterRecommendation(userId: String, courseId: String): ChapterRecommendation?
}

/**
 * Answer statistics for a user.
 */
data class AnswerStats(
    val totalAnswers: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val averageTimePerAnswer: Long,
    val subjectBreakdown: Map<Subject, SubjectAnswerStats>
)

/**
 * Answer statistics for a specific subject.
 */
data class SubjectAnswerStats(
    val subject: Subject,
    val totalAnswers: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val averageTime: Long,
    val improvementTrend: Float // positive = improving, negative = declining
)

/**
 * Weekly learning report.
 * UPDATED: lesson → chapter terminology.
 */
data class WeeklyReport(
    val userId: String,
    val weekStartDate: Long,
    val weekEndDate: Long,
    val totalStudyTime: Long,
    val chaptersCompleted: Int, // UPDATED: lessonsCompleted → chaptersCompleted
    val exercisesCompleted: Int,
    val averageAccuracy: Float,
    val streakDays: Int,
    val topSubjects: List<Subject>,
    val achievements: List<Achievement>,
    val improvements: List<String>,
    val recommendations: List<String>
)

/**
 * Monthly learning report.
 * UPDATED: lesson → chapter terminology.
 */
data class MonthlyReport(
    val userId: String,
    val monthStartDate: Long,
    val monthEndDate: Long,
    val totalStudyTime: Long,
    val chaptersCompleted: Int, // UPDATED: lessonsCompleted → chaptersCompleted
    val exercisesCompleted: Int,
    val averageAccuracy: Float,
    val longestStreak: Int,
    val coursesCompleted: Int,
    val subjectProgress: Map<Subject, SubjectProgress>,
    val achievements: List<Achievement>,
    val learningGoals: List<LearningGoal>,
    val nextMonthRecommendations: List<String>
)

/**
 * Performance metrics for a user.
 */
data class PerformanceMetrics(
    val userId: String,
    val overallAccuracy: Float,
    val averageResponseTime: Long,
    val consistencyScore: Float, // 0-100
    val difficultyProgression: Float, // how well user handles increasing difficulty
    val retentionRate: Float, // how well user retains knowledge over time
    val engagementScore: Float, // based on study frequency and duration
    val improvementRate: Float, // rate of improvement over time
    val strengthAreas: List<String>,
    val weaknessAreas: List<String>
)

/**
 * Subject-specific performance.
 */
data class SubjectPerformance(
    val subject: Subject,
    val accuracy: Float,
    val timeSpent: Long,
    val masteryLevel: MasteryLevel,
    val progressionRate: Float,
    val strongTopics: List<String>,
    val weakTopics: List<String>,
    val recommendedActions: List<String>
)

/**
 * Streak analysis.
 */
data class StreakAnalysis(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakHistory: List<StreakPeriod>,
    val averageStreakLength: Float,
    val streakMotivation: String,
    val nextMilestone: Int
)

/**
 * Learning velocity metrics.
 * UPDATED: lesson → chapter terminology.
 */
data class LearningVelocity(
    val chaptersPerWeek: Float, // UPDATED: lessonsPerWeek → chaptersPerWeek
    val exercisesPerSession: Float,
    val timePerChapter: Long, // UPDATED: timePerLesson → timePerChapter
    val difficultyProgression: Float,
    val velocityTrend: VelocityTrend, // speeding up, slowing down, stable
    val optimizedSchedule: List<StudyTimeRecommendation>
)

/**
 * Time distribution analysis.
 */
data class TimeDistribution(
    val totalTime: Long,
    val bySubject: Map<Subject, Long>,
    val byDayOfWeek: Map<String, Long>,
    val byTimeOfDay: Map<String, Long>,
    val mostProductiveTime: String,
    val recommendedSchedule: List<TimeSlot>
)

/**
 * Personalized recommendation.
 */
data class Recommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority,
    val estimatedTime: Long,
    val subject: Subject?,
    val actionUrl: String? = null
)

/**
 * Weak area recommendation.
 */
data class WeakAreaRecommendation(
    val subject: Subject,
    val topic: String,
    val currentAccuracy: Float,
    val targetAccuracy: Float,
    val recommendedExercises: List<String>,
    val estimatedImprovementTime: Long,
    val priority: Priority
)

/**
 * Chapter recommendation.
 * UPDATED: LessonRecommendation → ChapterRecommendation, lessonId → chapterId.
 */
data class ChapterRecommendation(
    val chapterId: String, // UPDATED: lessonId → chapterId
    val title: String,
    val reason: String,
    val confidence: Float,
    val estimatedDuration: Long,
    val prerequisites: List<String>,
    val difficulty: String
)

// Supporting enums and data classes
enum class MasteryLevel { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }
enum class VelocityTrend { ACCELERATING, STABLE, DECELERATING }
enum class Priority { LOW, MEDIUM, HIGH, URGENT }
enum class RecommendationType { PRACTICE, REVIEW, NEW_TOPIC, BREAK, ACHIEVEMENT }

data class SubjectProgress(
    val subject: Subject,
    val completionPercentage: Float,
    val masteryLevel: MasteryLevel
)

data class LearningGoal(
    val id: String,
    val description: String,
    val targetDate: Long,
    val progress: Float,
    val isCompleted: Boolean
)

data class StreakPeriod(
    val startDate: Long,
    val endDate: Long,
    val length: Int
)

data class StudyTimeRecommendation(
    val subject: Subject,
    val recommendedMinutes: Long,
    val priority: Priority
)

data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val subject: Subject,
    val activity: String
) 