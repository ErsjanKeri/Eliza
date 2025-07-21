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

package com.example.ai.edge.eliza.core.data.repository.mock

import com.example.ai.edge.eliza.core.data.repository.AnswerStats
import com.example.ai.edge.eliza.core.data.repository.LearningGoal
import com.example.ai.edge.eliza.core.data.repository.LearningVelocity
import com.example.ai.edge.eliza.core.data.repository.LessonRecommendation
import com.example.ai.edge.eliza.core.data.repository.MasteryLevel
import com.example.ai.edge.eliza.core.data.repository.MonthlyReport
import com.example.ai.edge.eliza.core.data.repository.PerformanceMetrics
import com.example.ai.edge.eliza.core.data.repository.Priority
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import com.example.ai.edge.eliza.core.data.repository.Recommendation
import com.example.ai.edge.eliza.core.data.repository.RecommendationType
import com.example.ai.edge.eliza.core.data.repository.StreakAnalysis
import com.example.ai.edge.eliza.core.data.repository.StreakPeriod
import com.example.ai.edge.eliza.core.data.repository.SubjectAnswerStats
import com.example.ai.edge.eliza.core.data.repository.SubjectPerformance
import com.example.ai.edge.eliza.core.data.repository.SubjectProgress
import com.example.ai.edge.eliza.core.data.repository.TimeDistribution
import com.example.ai.edge.eliza.core.data.repository.VelocityTrend
import com.example.ai.edge.eliza.core.data.repository.WeakAreaRecommendation
import com.example.ai.edge.eliza.core.data.repository.WeeklyReport
import com.example.ai.edge.eliza.core.model.Achievement
import com.example.ai.edge.eliza.core.model.AchievementRequirement
import com.example.ai.edge.eliza.core.model.AchievementType
import com.example.ai.edge.eliza.core.model.LearningStats
import com.example.ai.edge.eliza.core.model.LessonProgress
import com.example.ai.edge.eliza.core.model.SessionType
import com.example.ai.edge.eliza.core.model.StudySession
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.UserAnswer
import com.example.ai.edge.eliza.core.model.UserProgress
import com.example.ai.edge.eliza.core.model.WeeklyProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ProgressRepository for development and testing.
 * Provides realistic fake data to support development without requiring actual backend.
 */
@Singleton
class MockProgressRepository @Inject constructor() : ProgressRepository {

    private val mockUserProgress = mutableMapOf<String, UserProgress>()
    private val mockLessonProgress = mutableMapOf<String, LessonProgress>()
    private val mockUserAnswers = mutableMapOf<String, MutableList<UserAnswer>>()
    private val mockStudySessions = mutableMapOf<String, MutableList<StudySession>>()
    private val mockAchievements = mutableMapOf<String, Achievement>()

    init {
        // Create mock progress data matching the course IDs from MockCourseRepository
        mockUserProgress["course_algebra_101"] = UserProgress(
            id = "progress_algebra_101",
            courseId = "course_algebra_101",
            completedLessons = 3,
            totalLessons = 8,
            completedExercises = 8,
            totalExercises = 25,
            correctAnswers = 15,
            totalAnswers = 20,
            timeSpentMinutes = 180,
            streakDays = 5,
            lastStudiedAt = System.currentTimeMillis() - 86400000,
            createdAt = System.currentTimeMillis() - 604800000,
            updatedAt = System.currentTimeMillis() - 86400000
        )

        mockUserProgress["course_geometry_101"] = UserProgress(
            id = "progress_geometry_101",
            courseId = "course_geometry_101",
            completedLessons = 1,
            totalLessons = 10,
            completedExercises = 3,
            totalExercises = 30,
            correctAnswers = 2,
            totalAnswers = 3,
            timeSpentMinutes = 60,
            streakDays = 2,
            lastStudiedAt = System.currentTimeMillis() - 172800000, // 2 days ago
            createdAt = System.currentTimeMillis() - 1209600000, // 2 weeks ago
            updatedAt = System.currentTimeMillis() - 172800000
        )

        mockLessonProgress["lesson_1"] = LessonProgress(
            id = "lessonProgress_1",
            lessonId = "lesson_1",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 3,
            totalExercises = 3,
            timeSpentMinutes = 45,
            firstAccessAt = System.currentTimeMillis() - 86400000,
            lastAccessAt = System.currentTimeMillis() - 3600000,
            completedAt = System.currentTimeMillis() - 86400000
        )

        mockLessonProgress["lesson_2"] = LessonProgress(
            id = "lessonProgress_2",
            lessonId = "lesson_2",
            userId = "user_default",
            isCompleted = false,
            completedExercises = 1,
            totalExercises = 5,
            timeSpentMinutes = 20,
            firstAccessAt = System.currentTimeMillis() - 3600000,
            lastAccessAt = System.currentTimeMillis() - 1800000,
            completedAt = null
        )

        // Create mock achievements
        mockAchievements["achievement1"] = Achievement(
            id = "achievement1",
            title = "First Steps",
            description = "Complete your first lesson",
            iconUrl = "icon_first_steps",
            requirement = AchievementRequirement(
                type = AchievementType.LESSONS_COMPLETED,
                threshold = 1
            ),
            rewardPoints = 100,
            unlockedAt = System.currentTimeMillis() - 86400000
        )
    }

    override fun getUserProgressByCourse(courseId: String): Flow<UserProgress?> = 
        flowOf(mockUserProgress[courseId])

    override fun getAllUserProgress(): Flow<List<UserProgress>> = 
        flowOf(mockUserProgress.values.toList())

    override suspend fun updateUserProgress(progress: UserProgress) {
        mockUserProgress[progress.courseId] = progress
    }

    override suspend fun incrementLessonProgress(courseId: String, lessonId: String) {
        val progress = mockUserProgress[courseId] ?: return
        mockUserProgress[courseId] = progress.copy(
            completedLessons = progress.completedLessons + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun incrementExerciseProgress(courseId: String, exerciseId: String, isCorrect: Boolean) {
        val progress = mockUserProgress[courseId] ?: return
        mockUserProgress[courseId] = progress.copy(
            completedExercises = progress.completedExercises + 1,
            correctAnswers = if (isCorrect) progress.correctAnswers + 1 else progress.correctAnswers,
            totalAnswers = progress.totalAnswers + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun addStudyTime(courseId: String, minutes: Long) {
        val progress = mockUserProgress[courseId] ?: return
        mockUserProgress[courseId] = progress.copy(
            timeSpentMinutes = progress.timeSpentMinutes + minutes,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun updateStreak(courseId: String, days: Int) {
        val progress = mockUserProgress[courseId] ?: return
        mockUserProgress[courseId] = progress.copy(
            streakDays = days,
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun getLessonProgress(lessonId: String, userId: String): Flow<LessonProgress?> = 
        flowOf(mockLessonProgress[lessonId])

    override fun getUserLessonProgress(userId: String): Flow<List<LessonProgress>> = 
        flowOf(mockLessonProgress.values.toList())

    override fun getCompletedLessons(userId: String): Flow<List<LessonProgress>> = 
        flowOf(mockLessonProgress.values.filter { it.isCompleted })

    override suspend fun startLesson(lessonId: String, userId: String) {
        mockLessonProgress[lessonId] = LessonProgress(
            id = "progress_$lessonId",
            lessonId = lessonId,
            userId = userId,
            isCompleted = false,
            completedExercises = 0,
            totalExercises = 5,
            timeSpentMinutes = 0,
            firstAccessAt = System.currentTimeMillis(),
            lastAccessAt = System.currentTimeMillis(),
            completedAt = null
        )
    }

    override suspend fun completeLesson(lessonId: String, userId: String, timeSpent: Long) {
        mockLessonProgress[lessonId]?.let { progress ->
            mockLessonProgress[lessonId] = progress.copy(
                isCompleted = true,
                timeSpentMinutes = timeSpent,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateLessonProgress(progress: LessonProgress) {
        mockLessonProgress[progress.lessonId] = progress
    }

    override fun getUserAnswersByExercise(exerciseId: String, userId: String): Flow<List<UserAnswer>> = 
        flowOf(mockUserAnswers[userId]?.filter { it.exerciseId == exerciseId } ?: emptyList())

    override fun getAllUserAnswers(userId: String): Flow<List<UserAnswer>> = 
        flowOf(mockUserAnswers[userId] ?: emptyList())

    override fun getCorrectAnswers(userId: String): Flow<List<UserAnswer>> = 
        flowOf(mockUserAnswers[userId]?.filter { it.isCorrect } ?: emptyList())

    override suspend fun recordAnswer(answer: UserAnswer) {
        mockUserAnswers.getOrPut(answer.userId) { mutableListOf() }.add(answer)
    }

    override suspend fun getAnswerStats(userId: String): AnswerStats {
        val answers = mockUserAnswers[userId] ?: emptyList()
        val correct = answers.count { it.isCorrect }
        return AnswerStats(
            totalAnswers = answers.size,
            correctAnswers = correct,
            accuracy = if (answers.isNotEmpty()) correct.toFloat() / answers.size else 0f,
            averageTimePerAnswer = 30000L, // 30 seconds
            subjectBreakdown = mapOf(
                            Subject.ALGEBRA to SubjectAnswerStats(
                subject = Subject.ALGEBRA,
                    totalAnswers = answers.size,
                    correctAnswers = correct,
                    accuracy = if (answers.isNotEmpty()) correct.toFloat() / answers.size else 0f,
                    averageTime = 30000L,
                    improvementTrend = 0.1f
                )
            )
        )
    }

    override fun getStudySessionsByUser(userId: String): Flow<List<StudySession>> = 
        flowOf(mockStudySessions[userId] ?: emptyList())

    override fun getStudySessionsByCourse(userId: String, courseId: String): Flow<List<StudySession>> = 
        flowOf(mockStudySessions[userId]?.filter { it.courseId == courseId } ?: emptyList())

    override fun getActiveStudySession(userId: String): Flow<StudySession?> = 
        flowOf(mockStudySessions[userId]?.firstOrNull { it.endedAt == null })

    override suspend fun startStudySession(userId: String, courseId: String?, lessonId: String?, sessionType: String): StudySession {
        val session = StudySession(
            id = "session_${System.currentTimeMillis()}",
            userId = userId,
            courseId = courseId,
            lessonId = lessonId,
            sessionType = SessionType.AI_CHAT_TUTORING,
            durationMinutes = 0,
            exercisesCompleted = 0,
            correctAnswers = 0,
            totalAnswers = 0,
            startedAt = System.currentTimeMillis(),
            endedAt = null
        )
        mockStudySessions.getOrPut(userId) { mutableListOf() }.add(session)
        return session
    }

    override suspend fun endStudySession(sessionId: String, exercisesCompleted: Int, correctAnswers: Int) {
        mockStudySessions.values.forEach { sessions ->
            sessions.find { it.id == sessionId }?.let { session ->
                val index = sessions.indexOf(session)
                sessions[index] = session.copy(
                    endedAt = System.currentTimeMillis(),
                    durationMinutes = (System.currentTimeMillis() - session.startedAt) / 60000,
                    exercisesCompleted = exercisesCompleted,
                    correctAnswers = correctAnswers
                )
            }
        }
    }

    override suspend fun updateStudySession(session: StudySession) {
        mockStudySessions[session.userId]?.let { sessions ->
            val index = sessions.indexOfFirst { it.id == session.id }
            if (index != -1) {
                sessions[index] = session
            }
        }
    }

    override fun getAllAchievements(): Flow<List<Achievement>> = 
        flowOf(mockAchievements.values.toList())

    override fun getUnlockedAchievements(): Flow<List<Achievement>> = 
        flowOf(mockAchievements.values.filter { it.unlockedAt != null })

    override fun getLockedAchievements(): Flow<List<Achievement>> = 
        flowOf(mockAchievements.values.filter { it.unlockedAt == null })

    override suspend fun checkAndUnlockAchievements(userId: String): List<Achievement> {
        return emptyList() // Mock implementation
    }

    override suspend fun unlockAchievement(achievementId: String) {
        mockAchievements[achievementId]?.let { achievement ->
            mockAchievements[achievementId] = achievement.copy(
                unlockedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun createCustomAchievement(achievement: Achievement) {
        mockAchievements[achievement.id] = achievement
    }

    override fun getLearningStats(userId: String): Flow<LearningStats?> = 
        flowOf(null) // Simplified mock implementation

    override fun getWeeklyProgress(userId: String): Flow<List<WeeklyProgress>> = 
        flowOf(listOf(
            WeeklyProgress(
                weekStartDate = System.currentTimeMillis() - 604800000,
                weekEndDate = System.currentTimeMillis(),
                minutesStudied = 300,
                lessonsCompleted = 2,
                exercisesCompleted = 8,
                daysActive = 5,
                averageAccuracy = 0.8f
            )
        ))

    override fun getWeeklyProgressSince(userId: String, startDate: Long): Flow<List<WeeklyProgress>> = 
        getWeeklyProgress(userId)

    override suspend fun updateLearningStats(stats: LearningStats) {
        // Mock implementation - no actual storage
    }

    override suspend fun generateWeeklyReport(userId: String): WeeklyReport {
        return WeeklyReport(
            userId = userId,
            weekStartDate = System.currentTimeMillis() - 604800000,
            weekEndDate = System.currentTimeMillis(),
            totalStudyTime = 300,
            lessonsCompleted = 2,
            exercisesCompleted = 8,
            averageAccuracy = 0.8f,
            streakDays = 5,
            topSubjects = listOf(Subject.ALGEBRA),
            achievements = mockAchievements.values.filter { it.unlockedAt != null }.toList(),
            improvements = listOf("Improved algebra skills"),
            recommendations = listOf("Focus on geometry next week")
        )
    }

    override suspend fun generateMonthlyReport(userId: String): MonthlyReport {
        return MonthlyReport(
            userId = userId,
            monthStartDate = System.currentTimeMillis() - 2592000000,
            monthEndDate = System.currentTimeMillis(),
            totalStudyTime = 1200,
            lessonsCompleted = 8,
            exercisesCompleted = 32,
            averageAccuracy = 0.75f,
            longestStreak = 10,
            coursesCompleted = 1,
            subjectProgress = emptyMap(),
            achievements = mockAchievements.values.filter { it.unlockedAt != null }.toList(),
            learningGoals = emptyList(),
            nextMonthRecommendations = listOf("Focus on advanced topics")
        )
    }

    override suspend fun getPerformanceMetrics(userId: String): PerformanceMetrics {
        return PerformanceMetrics(
            userId = userId,
            overallAccuracy = 0.8f,
            averageResponseTime = 30000L,
            consistencyScore = 85f,
            difficultyProgression = 0.7f,
            retentionRate = 0.9f,
            engagementScore = 0.8f,
            improvementRate = 0.1f,
            strengthAreas = listOf("Algebra", "Basic Math"),
            weaknessAreas = listOf("Geometry", "Word Problems")
        )
    }

    override suspend fun getSubjectPerformance(userId: String, subject: Subject): SubjectPerformance {
        return SubjectPerformance(
            subject = subject,
            accuracy = 0.8f,
            timeSpent = 300000L,
            masteryLevel = MasteryLevel.INTERMEDIATE,
            progressionRate = 0.1f,
            strongTopics = listOf("Linear Equations", "Basic Operations"),
            weakTopics = listOf("Quadratic Equations", "Factoring"),
            recommendedActions = listOf("Practice more quadratic problems")
        )
    }

    override suspend fun getStreakAnalysis(userId: String): StreakAnalysis {
        return StreakAnalysis(
            currentStreak = 5,
            longestStreak = 15,
            streakHistory = listOf(
                StreakPeriod(
                    startDate = System.currentTimeMillis() - 432000000,
                    endDate = System.currentTimeMillis(),
                    length = 5
                )
            ),
            averageStreakLength = 8.5f,
            streakMotivation = "You're on a great streak! Keep it up!",
            nextMilestone = 10
        )
    }

    override suspend fun getLearningVelocity(userId: String): LearningVelocity {
        // Simplified mock - return a basic velocity object
        return LearningVelocity(
            lessonsPerWeek = 2.5f,
            exercisesPerSession = 8.0f,
            timePerLesson = 1500000L, // 25 minutes in milliseconds
            difficultyProgression = 0.1f,
            velocityTrend = VelocityTrend.STABLE,
            optimizedSchedule = emptyList()
        )
    }

    override suspend fun getTimeDistribution(userId: String): TimeDistribution {
        // Simplified mock - return a basic distribution object
        return TimeDistribution(
            totalTime = 3600L,
            bySubject = mapOf(Subject.ALGEBRA to 3600L),
            byDayOfWeek = mapOf("Monday" to 3600L),
            byTimeOfDay = mapOf("Morning" to 3600L),
            mostProductiveTime = "Morning",
            recommendedSchedule = emptyList()
        )
    }

    override suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation> {
        return listOf(
            Recommendation(
                type = RecommendationType.PRACTICE,
                title = "Practice Algebra",
                description = "Focus on linear equations to improve your skills",
                priority = Priority.HIGH,
                estimatedTime = 30L,
                subject = Subject.ALGEBRA,
                actionUrl = null
            )
        )
    }

    override suspend fun getWeakAreaRecommendations(userId: String): List<WeakAreaRecommendation> {
        return listOf(
            WeakAreaRecommendation(
                subject = Subject.GEOMETRY,
                topic = "Basic Shapes",
                currentAccuracy = 0.6f,
                targetAccuracy = 0.8f,
                recommendedExercises = listOf("geometry_basic_1"),
                estimatedImprovementTime = 60L,
                priority = Priority.MEDIUM
            )
        )
    }

    override suspend fun getNextLessonRecommendation(userId: String, courseId: String): LessonRecommendation? {
        return LessonRecommendation(
            lessonId = "lesson_next",
            title = "Advanced Algebra",
            reason = "Based on your progress in basic algebra",
            confidence = 0.8f,
            estimatedDuration = 45L,
            prerequisites = listOf("lesson_basic_algebra"),
            difficulty = "Intermediate"
        )
    }
} 