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
import com.example.ai.edge.eliza.core.data.repository.StudyTimeRecommendation
import com.example.ai.edge.eliza.core.data.repository.SubjectAnswerStats
import com.example.ai.edge.eliza.core.data.repository.SubjectPerformance
import com.example.ai.edge.eliza.core.data.repository.SubjectProgress
import com.example.ai.edge.eliza.core.data.repository.TimeDistribution
import com.example.ai.edge.eliza.core.data.repository.TimeSlot
import com.example.ai.edge.eliza.core.data.repository.VelocityTrend
import com.example.ai.edge.eliza.core.data.repository.WeakAreaRecommendation
import com.example.ai.edge.eliza.core.data.repository.WeeklyReport
import com.example.ai.edge.eliza.core.model.Achievement
import com.example.ai.edge.eliza.core.model.AchievementType
import com.example.ai.edge.eliza.core.model.LearningStats
import com.example.ai.edge.eliza.core.model.LessonProgress
import com.example.ai.edge.eliza.core.model.ProgressStatus
import com.example.ai.edge.eliza.core.model.StudySession
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.UserAnswer
import com.example.ai.edge.eliza.core.model.UserProgress
import com.example.ai.edge.eliza.core.model.WeeklyProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock implementation of ProgressRepository for development and testing.
 * Provides realistic learning analytics and progress tracking data.
 */
@Singleton
class MockProgressRepository @Inject constructor() : ProgressRepository {
    
    private val userProgress = ConcurrentHashMap<String, UserProgress>()
    private val lessonProgress = ConcurrentHashMap<String, LessonProgress>()
    private val userAnswers = ConcurrentHashMap<String, MutableList<UserAnswer>>()
    private val studySessions = ConcurrentHashMap<String, MutableList<StudySession>>()
    private val achievements = ConcurrentHashMap<String, Achievement>()
    private val unlockedAchievements = mutableSetOf<String>()
    private val learningStats = ConcurrentHashMap<String, LearningStats>()
    private val weeklyProgress = ConcurrentHashMap<String, MutableList<WeeklyProgress>>()
    
    init {
        // Initialize with sample data
        createSampleData()
    }
    
    // User Progress operations
    override fun getUserProgressByCourse(courseId: String): Flow<UserProgress?> = flowOf(
        userProgress.values.find { it.courseId == courseId }
    )
    
    override fun getAllUserProgress(): Flow<List<UserProgress>> = flowOf(userProgress.values.toList())
    
    override suspend fun updateUserProgress(progress: UserProgress) {
        userProgress[progress.id] = progress
    }
    
    override suspend fun incrementLessonProgress(courseId: String, lessonId: String) {
        val progress = userProgress.values.find { it.courseId == courseId }
        if (progress != null) {
            userProgress[progress.id] = progress.copy(
                lessonsCompleted = progress.lessonsCompleted + 1,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    override suspend fun incrementExerciseProgress(courseId: String, exerciseId: String, isCorrect: Boolean) {
        val progress = userProgress.values.find { it.courseId == courseId }
        if (progress != null) {
            userProgress[progress.id] = progress.copy(
                exercisesCompleted = progress.exercisesCompleted + 1,
                correctAnswers = if (isCorrect) progress.correctAnswers + 1 else progress.correctAnswers,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    override suspend fun addStudyTime(courseId: String, minutes: Long) {
        val progress = userProgress.values.find { it.courseId == courseId }
        if (progress != null) {
            userProgress[progress.id] = progress.copy(
                totalStudyTime = progress.totalStudyTime + minutes,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    override suspend fun updateStreak(courseId: String, days: Int) {
        val progress = userProgress.values.find { it.courseId == courseId }
        if (progress != null) {
            userProgress[progress.id] = progress.copy(
                currentStreak = days,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    // Lesson Progress operations
    override fun getLessonProgress(lessonId: String, userId: String): Flow<LessonProgress?> = flowOf(
        lessonProgress.values.find { it.lessonId == lessonId && it.userId == userId }
    )
    
    override fun getUserLessonProgress(userId: String): Flow<List<LessonProgress>> = flowOf(
        lessonProgress.values.filter { it.userId == userId }
    )
    
    override fun getCompletedLessons(userId: String): Flow<List<LessonProgress>> = flowOf(
        lessonProgress.values.filter { it.userId == userId && it.status == ProgressStatus.COMPLETED }
    )
    
    override suspend fun startLesson(lessonId: String, userId: String) {
        val progress = LessonProgress(
            id = UUID.randomUUID().toString(),
            lessonId = lessonId,
            userId = userId,
            status = ProgressStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(),
            endTime = null,
            timeSpent = 0L,
            accuracy = 0f,
            hintsUsed = 0,
            mistakesMade = 0,
            completionPercentage = 0f,
            lastCheckpoint = null,
            isBookmarked = false,
            notes = "",
            rating = null,
            feedback = ""
        )
        lessonProgress[progress.id] = progress
    }
    
    override suspend fun completeLesson(lessonId: String, userId: String, timeSpent: Long) {
        val progress = lessonProgress.values.find { it.lessonId == lessonId && it.userId == userId }
        if (progress != null) {
            lessonProgress[progress.id] = progress.copy(
                status = ProgressStatus.COMPLETED,
                endTime = System.currentTimeMillis(),
                timeSpent = timeSpent,
                completionPercentage = 100f,
                accuracy = 0.85f + Random.nextFloat() * 0.15f // Random accuracy between 85-100%
            )
        }
    }
    
    override suspend fun updateLessonProgress(progress: LessonProgress) {
        lessonProgress[progress.id] = progress
    }
    
    // User Answer operations
    override fun getUserAnswersByExercise(exerciseId: String, userId: String): Flow<List<UserAnswer>> = flowOf(
        userAnswers.values.flatten().filter { it.exerciseId == exerciseId && it.userId == userId }
    )
    
    override fun getAllUserAnswers(userId: String): Flow<List<UserAnswer>> = flowOf(
        userAnswers.values.flatten().filter { it.userId == userId }
    )
    
    override fun getCorrectAnswers(userId: String): Flow<List<UserAnswer>> = flowOf(
        userAnswers.values.flatten().filter { it.userId == userId && it.isCorrect }
    )
    
    override suspend fun recordAnswer(answer: UserAnswer) {
        userAnswers.getOrPut(answer.userId) { mutableListOf() }.add(answer)
    }
    
    override suspend fun getAnswerStats(userId: String): AnswerStats {
        val answers = userAnswers[userId] ?: emptyList()
        val totalAnswers = answers.size
        val correctAnswers = answers.count { it.isCorrect }
        val accuracy = if (totalAnswers > 0) correctAnswers.toFloat() / totalAnswers else 0f
        val averageTime = if (totalAnswers > 0) answers.map { it.timeSpent }.average().toLong() else 0L
        
        val subjectBreakdown = Subject.values().associate { subject ->
            val subjectAnswers = answers.filter { it.subject == subject }
            val subjectTotal = subjectAnswers.size
            val subjectCorrect = subjectAnswers.count { it.isCorrect }
            val subjectAccuracy = if (subjectTotal > 0) subjectCorrect.toFloat() / subjectTotal else 0f
            val subjectAvgTime = if (subjectTotal > 0) subjectAnswers.map { it.timeSpent }.average().toLong() else 0L
            
            subject to SubjectAnswerStats(
                subject = subject,
                totalAnswers = subjectTotal,
                correctAnswers = subjectCorrect,
                accuracy = subjectAccuracy,
                averageTime = subjectAvgTime,
                improvementTrend = Random.nextFloat() * 0.2f - 0.1f // Random trend -10% to +10%
            )
        }
        
        return AnswerStats(
            totalAnswers = totalAnswers,
            correctAnswers = correctAnswers,
            accuracy = accuracy,
            averageTimePerAnswer = averageTime,
            subjectBreakdown = subjectBreakdown
        )
    }
    
    // Study Session operations
    override fun getStudySessionsByUser(userId: String): Flow<List<StudySession>> = flowOf(
        studySessions[userId] ?: emptyList()
    )
    
    override fun getStudySessionsByCourse(userId: String, courseId: String): Flow<List<StudySession>> = flowOf(
        studySessions[userId]?.filter { it.courseId == courseId } ?: emptyList()
    )
    
    override fun getActiveStudySession(userId: String): Flow<StudySession?> = flowOf(
        studySessions[userId]?.find { it.endTime == null }
    )
    
    override suspend fun startStudySession(
        userId: String,
        courseId: String?,
        lessonId: String?,
        sessionType: String
    ): StudySession {
        val session = StudySession(
            id = UUID.randomUUID().toString(),
            userId = userId,
            courseId = courseId,
            lessonId = lessonId,
            sessionType = sessionType,
            startTime = System.currentTimeMillis(),
            endTime = null,
            duration = 0L,
            exercisesCompleted = 0,
            correctAnswers = 0,
            hintsUsed = 0,
            mistakesMade = 0,
            topicsStudied = emptyList(),
            difficultyLevel = "MEDIUM",
            focusScore = 0f,
            productivityScore = 0f,
            breaksTaken = 0,
            averageResponseTime = 0L,
            peakPerformanceTime = null,
            notes = "",
            mood = "neutral",
            environment = "home",
            deviceUsed = "mobile"
        )
        studySessions.getOrPut(userId) { mutableListOf() }.add(session)
        return session
    }
    
    override suspend fun endStudySession(sessionId: String, exercisesCompleted: Int, correctAnswers: Int) {
        studySessions.values.forEach { sessions ->
            val session = sessions.find { it.id == sessionId }
            if (session != null) {
                val duration = System.currentTimeMillis() - session.startTime
                val index = sessions.indexOf(session)
                sessions[index] = session.copy(
                    endTime = System.currentTimeMillis(),
                    duration = duration,
                    exercisesCompleted = exercisesCompleted,
                    correctAnswers = correctAnswers,
                    focusScore = 0.7f + Random.nextFloat() * 0.3f, // Random focus score 70-100%
                    productivityScore = 0.6f + Random.nextFloat() * 0.4f, // Random productivity 60-100%
                    averageResponseTime = 3000L + Random.nextLong(7000L) // Random response time 3-10s
                )
            }
        }
    }
    
    override suspend fun updateStudySession(session: StudySession) {
        studySessions[session.userId]?.let { sessions ->
            val index = sessions.indexOfFirst { it.id == session.id }
            if (index != -1) {
                sessions[index] = session
            }
        }
    }
    
    // Achievement operations
    override fun getAllAchievements(): Flow<List<Achievement>> = flowOf(achievements.values.toList())
    
    override fun getUnlockedAchievements(): Flow<List<Achievement>> = flowOf(
        achievements.values.filter { it.id in unlockedAchievements }
    )
    
    override fun getLockedAchievements(): Flow<List<Achievement>> = flowOf(
        achievements.values.filter { it.id !in unlockedAchievements }
    )
    
    override suspend fun checkAndUnlockAchievements(userId: String): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()
        
        // Check for achievements based on user progress
        val userStats = getAnswerStats(userId)
        
        // First correct answer achievement
        if (userStats.correctAnswers >= 1 && !unlockedAchievements.contains("first_correct")) {
            val achievement = achievements["first_correct"]
            if (achievement != null) {
                unlockedAchievements.add("first_correct")
                newAchievements.add(achievement)
            }
        }
        
        // Streak achievements
        val streakAchievements = listOf(
            "streak_3" to 3,
            "streak_7" to 7,
            "streak_30" to 30
        )
        
        val currentStreak = userProgress.values.maxOfOrNull { it.currentStreak } ?: 0
        streakAchievements.forEach { (achievementId, requiredStreak) ->
            if (currentStreak >= requiredStreak && !unlockedAchievements.contains(achievementId)) {
                val achievement = achievements[achievementId]
                if (achievement != null) {
                    unlockedAchievements.add(achievementId)
                    newAchievements.add(achievement)
                }
            }
        }
        
        return newAchievements
    }
    
    override suspend fun unlockAchievement(achievementId: String) {
        unlockedAchievements.add(achievementId)
    }
    
    override suspend fun createCustomAchievement(achievement: Achievement) {
        achievements[achievement.id] = achievement
    }
    
    // Learning Analytics operations
    override fun getLearningStats(userId: String): Flow<LearningStats?> = flowOf(
        learningStats[userId]
    )
    
    override fun getWeeklyProgress(userId: String): Flow<List<WeeklyProgress>> = flowOf(
        weeklyProgress[userId] ?: emptyList()
    )
    
    override fun getWeeklyProgressSince(userId: String, startDate: Long): Flow<List<WeeklyProgress>> = flowOf(
        weeklyProgress[userId]?.filter { it.weekStart >= startDate } ?: emptyList()
    )
    
    override suspend fun updateLearningStats(stats: LearningStats) {
        learningStats[stats.userId] = stats
    }
    
    override suspend fun generateWeeklyReport(userId: String): WeeklyReport {
        val now = System.currentTimeMillis()
        val weekStart = now - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
        
        return WeeklyReport(
            userId = userId,
            weekStartDate = weekStart,
            weekEndDate = now,
            totalStudyTime = 780L, // 13 hours
            lessonsCompleted = 8,
            exercisesCompleted = 45,
            averageAccuracy = 0.78f,
            streakDays = 5,
            topSubjects = listOf(Subject.ALGEBRA, Subject.GEOMETRY),
            achievements = getUnlockedAchievements().value.filter { it.unlockedAt > weekStart },
            improvements = listOf(
                "Improved accuracy in linear equations by 12%",
                "Reduced average time per problem by 15 seconds",
                "Completed first geometry unit"
            ),
            recommendations = listOf(
                "Focus on quadratic equations practice",
                "Try solving problems without hints",
                "Review geometry theorems"
            )
        )
    }
    
    override suspend fun generateMonthlyReport(userId: String): MonthlyReport {
        val now = System.currentTimeMillis()
        val monthStart = now - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        
        return MonthlyReport(
            userId = userId,
            monthStartDate = monthStart,
            monthEndDate = now,
            totalStudyTime = 3600L, // 60 hours
            lessonsCompleted = 25,
            exercisesCompleted = 180,
            averageAccuracy = 0.82f,
            longestStreak = 12,
            coursesCompleted = 2,
            subjectProgress = mapOf(
                Subject.ALGEBRA to SubjectProgress(Subject.ALGEBRA, 0.85f, MasteryLevel.INTERMEDIATE),
                Subject.GEOMETRY to SubjectProgress(Subject.GEOMETRY, 0.65f, MasteryLevel.BEGINNER)
            ),
            achievements = getUnlockedAchievements().value.filter { it.unlockedAt > monthStart },
            learningGoals = listOf(
                LearningGoal(
                    id = "goal-1",
                    description = "Complete Algebra fundamentals",
                    targetDate = now + (7 * 24 * 60 * 60 * 1000L),
                    progress = 0.85f,
                    isCompleted = false
                )
            ),
            nextMonthRecommendations = listOf(
                "Start calculus preparation",
                "Complete remaining geometry units",
                "Practice mixed problem sets"
            )
        )
    }
    
    // Performance Analytics
    override suspend fun getPerformanceMetrics(userId: String): PerformanceMetrics {
        val stats = getAnswerStats(userId)
        
        return PerformanceMetrics(
            userId = userId,
            overallAccuracy = stats.accuracy,
            averageResponseTime = stats.averageTimePerAnswer,
            consistencyScore = 75f + Random.nextFloat() * 20f, // Random 75-95%
            difficultyProgression = 0.65f + Random.nextFloat() * 0.3f, // Random 65-95%
            retentionRate = 0.78f + Random.nextFloat() * 0.2f, // Random 78-98%
            engagementScore = 0.82f + Random.nextFloat() * 0.18f, // Random 82-100%
            improvementRate = 0.05f + Random.nextFloat() * 0.15f, // Random 5-20%
            strengthAreas = listOf("Linear equations", "Basic geometry", "Fractions"),
            weaknessAreas = listOf("Quadratic equations", "Trigonometry", "Word problems")
        )
    }
    
    override suspend fun getSubjectPerformance(userId: String, subject: Subject): SubjectPerformance {
        val stats = getAnswerStats(userId)
        val subjectStats = stats.subjectBreakdown[subject] ?: SubjectAnswerStats(
            subject = subject,
            totalAnswers = 0,
            correctAnswers = 0,
            accuracy = 0f,
            averageTime = 0L,
            improvementTrend = 0f
        )
        
        return SubjectPerformance(
            subject = subject,
            accuracy = subjectStats.accuracy,
            timeSpent = subjectStats.averageTime * subjectStats.totalAnswers,
            masteryLevel = when {
                subjectStats.accuracy >= 0.9f -> MasteryLevel.EXPERT
                subjectStats.accuracy >= 0.8f -> MasteryLevel.ADVANCED
                subjectStats.accuracy >= 0.6f -> MasteryLevel.INTERMEDIATE
                else -> MasteryLevel.BEGINNER
            },
            progressionRate = subjectStats.improvementTrend,
            strongTopics = listOf("Basic operations", "Simple equations"),
            weakTopics = listOf("Complex problems", "Word problems"),
            recommendedActions = listOf("Practice more complex problems", "Review fundamentals")
        )
    }
    
    override suspend fun getStreakAnalysis(userId: String): StreakAnalysis {
        val currentStreak = userProgress.values.maxOfOrNull { it.currentStreak } ?: 0
        
        return StreakAnalysis(
            currentStreak = currentStreak,
            longestStreak = currentStreak + Random.nextInt(10), // Add some historical data
            streakHistory = listOf(
                StreakPeriod(
                    startDate = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L),
                    endDate = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L),
                    length = 7
                ),
                StreakPeriod(
                    startDate = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L),
                    endDate = System.currentTimeMillis(),
                    length = currentStreak
                )
            ),
            averageStreakLength = 4.5f,
            streakMotivation = "You're on fire! Keep up the great work!",
            nextMilestone = if (currentStreak < 7) 7 else if (currentStreak < 30) 30 else 100
        )
    }
    
    override suspend fun getLearningVelocity(userId: String): LearningVelocity {
        return LearningVelocity(
            lessonsPerWeek = 3.5f,
            exercisesPerSession = 8.2f,
            timePerLesson = 25L * 60 * 1000, // 25 minutes
            difficultyProgression = 0.15f, // 15% increase per week
            velocityTrend = VelocityTrend.STABLE,
            optimizedSchedule = listOf(
                StudyTimeRecommendation(Subject.ALGEBRA, 30L, Priority.HIGH),
                StudyTimeRecommendation(Subject.GEOMETRY, 20L, Priority.MEDIUM)
            )
        )
    }
    
    override suspend fun getTimeDistribution(userId: String): TimeDistribution {
        return TimeDistribution(
            totalTime = 3600L, // 60 hours
            bySubject = mapOf(
                Subject.ALGEBRA to 2400L, // 40 hours
                Subject.GEOMETRY to 1200L, // 20 hours
            ),
            byDayOfWeek = mapOf(
                "Monday" to 600L,
                "Tuesday" to 480L,
                "Wednesday" to 540L,
                "Thursday" to 600L,
                "Friday" to 480L,
                "Saturday" to 720L,
                "Sunday" to 180L
            ),
            byTimeOfDay = mapOf(
                "Morning" to 1200L,
                "Afternoon" to 1800L,
                "Evening" to 600L
            ),
            mostProductiveTime = "Afternoon",
            recommendedSchedule = listOf(
                TimeSlot("14:00", "15:00", Subject.ALGEBRA, "Problem solving"),
                TimeSlot("16:00", "16:30", Subject.GEOMETRY, "Concept review")
            )
        )
    }
    
    // Recommendations
    override suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation> {
        return listOf(
            Recommendation(
                type = RecommendationType.PRACTICE,
                title = "Practice Linear Equations",
                description = "You've mastered basic algebra. Try more complex linear equations.",
                priority = Priority.HIGH,
                estimatedTime = 30L,
                subject = Subject.ALGEBRA,
                actionUrl = "lesson://algebra/linear-equations-advanced"
            ),
            Recommendation(
                type = RecommendationType.REVIEW,
                title = "Review Geometry Basics",
                description = "Your geometry accuracy has dropped. Review fundamental concepts.",
                priority = Priority.MEDIUM,
                estimatedTime = 20L,
                subject = Subject.GEOMETRY,
                actionUrl = "lesson://geometry/fundamentals"
            ),
            Recommendation(
                type = RecommendationType.BREAK,
                title = "Take a Break",
                description = "You've been studying for 2 hours. Take a 15-minute break.",
                priority = Priority.LOW,
                estimatedTime = 15L,
                subject = null,
                actionUrl = null
            )
        )
    }
    
    override suspend fun getWeakAreaRecommendations(userId: String): List<WeakAreaRecommendation> {
        return listOf(
            WeakAreaRecommendation(
                subject = Subject.ALGEBRA,
                topic = "Quadratic Equations",
                currentAccuracy = 0.45f,
                targetAccuracy = 0.80f,
                recommendedExercises = listOf(
                    "Quadratic Formula Practice",
                    "Factoring Quadratics",
                    "Completing the Square"
                ),
                estimatedImprovementTime = 120L, // 2 hours
                priority = Priority.HIGH
            ),
            WeakAreaRecommendation(
                subject = Subject.GEOMETRY,
                topic = "Trigonometry",
                currentAccuracy = 0.30f,
                targetAccuracy = 0.70f,
                recommendedExercises = listOf(
                    "Sine and Cosine",
                    "Pythagorean Theorem",
                    "Triangle Properties"
                ),
                estimatedImprovementTime = 180L, // 3 hours
                priority = Priority.MEDIUM
            )
        )
    }
    
    override suspend fun getNextLessonRecommendation(userId: String, courseId: String): LessonRecommendation? {
        return LessonRecommendation(
            lessonId = "lesson-next-1",
            title = "Advanced Linear Equations",
            reason = "Based on your performance in basic equations, you're ready for advanced topics.",
            confidence = 0.85f,
            estimatedDuration = 45L,
            prerequisites = listOf("Basic Linear Equations", "Solving for Variables"),
            difficulty = "Intermediate"
        )
    }
    
    // Private helper methods
    private fun createSampleData() {
        // Create sample user progress
        val progress1 = UserProgress(
            id = "progress-1",
            userId = "user-1",
            courseId = "course-1",
            completionPercentage = 0.65f,
            lessonsCompleted = 8,
            totalLessons = 12,
            exercisesCompleted = 45,
            correctAnswers = 35,
            totalStudyTime = 1200L, // 20 hours
            averageAccuracy = 0.78f,
            currentStreak = 5,
            longestStreak = 12,
            lastStudyDate = System.currentTimeMillis() - 86400000, // 1 day ago
            startDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L), // 30 days ago
            lastUpdated = System.currentTimeMillis() - 86400000,
            achievements = listOf("first_correct", "streak_7"),
            weakAreas = listOf("Quadratic equations", "Word problems"),
            strongAreas = listOf("Linear equations", "Basic algebra")
        )
        
        userProgress[progress1.id] = progress1
        
        // Create sample achievements
        val achievements = listOf(
            Achievement(
                id = "first_correct",
                title = "First Success!",
                description = "Got your first answer correct",
                icon = "star",
                type = AchievementType.MILESTONE,
                requiredValue = 1,
                currentValue = 1,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L),
                points = 10,
                category = "learning",
                rarity = "common",
                prerequisites = emptyList()
            ),
            Achievement(
                id = "streak_7",
                title = "Week Warrior",
                description = "Maintained a 7-day study streak",
                icon = "flame",
                type = AchievementType.STREAK,
                requiredValue = 7,
                currentValue = 7,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L),
                points = 50,
                category = "consistency",
                rarity = "uncommon",
                prerequisites = listOf("streak_3")
            ),
            Achievement(
                id = "streak_30",
                title = "Month Master",
                description = "Maintained a 30-day study streak",
                icon = "trophy",
                type = AchievementType.STREAK,
                requiredValue = 30,
                currentValue = 5,
                isUnlocked = false,
                unlockedAt = null,
                points = 200,
                category = "consistency",
                rarity = "rare",
                prerequisites = listOf("streak_7")
            )
        )
        
        achievements.forEach { achievement ->
            this.achievements[achievement.id] = achievement
            if (achievement.isUnlocked) {
                unlockedAchievements.add(achievement.id)
            }
        }
        
        // Create sample learning stats
        val stats = LearningStats(
            userId = "user-1",
            totalStudyTime = 1200L,
            totalLessons = 8,
            totalExercises = 45,
            averageAccuracy = 0.78f,
            currentStreak = 5,
            longestStreak = 12,
            weeklyGoal = 300L, // 5 hours
            monthlyGoal = 1200L, // 20 hours
            preferredDifficulty = "MEDIUM",
            strongSubjects = listOf(Subject.ALGEBRA),
            weakSubjects = listOf(Subject.GEOMETRY),
            learningVelocity = 0.85f,
            retentionRate = 0.82f,
            engagementScore = 0.75f,
            lastUpdated = System.currentTimeMillis()
        )
        
        learningStats[stats.userId] = stats
        
        // Create sample weekly progress
        val weeklyProgressList = mutableListOf<WeeklyProgress>()
        for (i in 0..4) {
            val weekStart = System.currentTimeMillis() - (i * 7 * 24 * 60 * 60 * 1000L)
            weeklyProgressList.add(
                WeeklyProgress(
                    id = "week-$i",
                    userId = "user-1",
                    weekStart = weekStart,
                    weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L),
                    studyTime = 180L + Random.nextLong(120L), // 3-5 hours
                    lessonsCompleted = 1 + Random.nextInt(3), // 1-3 lessons
                    exercisesCompleted = 8 + Random.nextInt(12), // 8-20 exercises
                    accuracy = 0.65f + Random.nextFloat() * 0.3f, // 65-95%
                    streakDays = Random.nextInt(8), // 0-7 days
                    subjectFocus = Subject.values().random(),
                    improvementAreas = listOf("Problem solving", "Speed"),
                    weeklyGoalMet = Random.nextBoolean()
                )
            )
        }
        
        weeklyProgress["user-1"] = weeklyProgressList
    }
} 