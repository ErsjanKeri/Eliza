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
import com.example.ai.edge.eliza.core.data.repository.ChapterRecommendation
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
import com.example.ai.edge.eliza.core.model.ChapterProgress
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
 * UPDATED: Now uses chapter-based terminology throughout.
 */
@Singleton
class MockProgressRepository @Inject constructor() : ProgressRepository {

    private val mockUserProgress = mutableMapOf<String, UserProgress>()
    private val mockChapterProgress = mutableMapOf<String, ChapterProgress>()
    private val mockUserAnswers = mutableMapOf<String, MutableList<UserAnswer>>()
    private val mockStudySessions = mutableMapOf<String, MutableList<StudySession>>()
    private val mockAchievements = mutableMapOf<String, Achievement>()

    init {
        // Create mock progress data matching the course IDs from MockCourseRepository
        // FIXED: Updated course IDs to match MockCourseRepository
        mockUserProgress["course_algebra_1"] = UserProgress(
            id = "progress_algebra_1",
            courseId = "course_algebra_1", // FIXED: was course_algebra_101
            completedChapters = 2, // UPDATED: 2 out of 3 chapters (in progress)
            totalChapters = 3,
            completedExercises = 5,
            totalExercises = 8,
            correctAnswers = 4,
            totalAnswers = 5,
            timeSpentMinutes = 120, // 2 hours
            streakDays = 3,
            lastStudiedAt = System.currentTimeMillis() - 3600000, // 1 hour ago
            createdAt = System.currentTimeMillis() - 604800000, // 1 week ago
            updatedAt = System.currentTimeMillis() - 3600000
        )

        mockUserProgress["course_geometry_1"] = UserProgress(
            id = "progress_geometry_1",
            courseId = "course_geometry_1", // FIXED: was course_geometry_101
            completedChapters = 1, // UPDATED: 1 out of 2 chapters (in progress)
            totalChapters = 2,
            completedExercises = 2,
            totalExercises = 6,
            correctAnswers = 2,
            totalAnswers = 2,
            timeSpentMinutes = 60, // 1 hour
            streakDays = 1,
            lastStudiedAt = System.currentTimeMillis() - 172800000, // 2 days ago
            createdAt = System.currentTimeMillis() - 1209600000, // 2 weeks ago
            updatedAt = System.currentTimeMillis() - 172800000
        )

        // NEW: Add a third course that's more advanced and clearly in progress
        mockUserProgress["course_calculus_1"] = UserProgress(
            id = "progress_calculus_1",
            courseId = "course_calculus_1",
            completedChapters = 3, // 3 out of 5 chapters (well into the course)
            totalChapters = 5,
            completedExercises = 12,
            totalExercises = 20,
            correctAnswers = 10,
            totalAnswers = 12,
            timeSpentMinutes = 240, // 4 hours
            streakDays = 7,
            lastStudiedAt = System.currentTimeMillis() - 7200000, // 2 hours ago
            createdAt = System.currentTimeMillis() - 1814400000, // 3 weeks ago
            updatedAt = System.currentTimeMillis() - 7200000
        )

        // UPDATED: Create mock chapter progress (was lesson progress)
        mockChapterProgress["chapter_1"] = ChapterProgress(
            id = "chapterProgress_1",
            chapterId = "chapter_1", // UPDATED: lessonId → chapterId
            userId = "user_default",
            isCompleted = true,
            completedExercises = 3,
            totalExercises = 3,
            timeSpentMinutes = 45,
            firstAccessAt = System.currentTimeMillis() - 86400000,
            lastAccessAt = System.currentTimeMillis() - 3600000,
            completedAt = System.currentTimeMillis() - 86400000
        )

        mockChapterProgress["chapter_2"] = ChapterProgress(
            id = "chapterProgress_2",
            chapterId = "chapter_2", // UPDATED: lessonId → chapterId
            userId = "user_default",
            isCompleted = false,
            completedExercises = 1,
            totalExercises = 5,
            timeSpentMinutes = 20,
            firstAccessAt = System.currentTimeMillis() - 3600000,
            lastAccessAt = System.currentTimeMillis() - 1800000,
            completedAt = null
        )

        // NEW: Add chapter progress for the algebra course (showing 2 completed chapters)
        mockChapterProgress["chapter_linear_eq"] = ChapterProgress(
            id = "chapterProgress_linear_eq",
            chapterId = "chapter_linear_eq",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 2,
            totalExercises = 2,
            timeSpentMinutes = 60,
            firstAccessAt = System.currentTimeMillis() - 604800000, // 1 week ago
            lastAccessAt = System.currentTimeMillis() - 86400000, // 1 day ago
            completedAt = System.currentTimeMillis() - 86400000
        )

        mockChapterProgress["chapter_quadratics"] = ChapterProgress(
            id = "chapterProgress_quadratics",
            chapterId = "chapter_quadratics",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 3,
            totalExercises = 3,
            timeSpentMinutes = 80,
            firstAccessAt = System.currentTimeMillis() - 432000000, // 5 days ago
            lastAccessAt = System.currentTimeMillis() - 3600000, // 1 hour ago
            completedAt = System.currentTimeMillis() - 259200000 // 3 days ago
        )

        // NEW: Add chapter progress for calculus course (showing 3 completed out of 5)
        mockChapterProgress["chapter_calc_limits"] = ChapterProgress(
            id = "chapterProgress_calc_limits",
            chapterId = "chapter_calc_limits",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 4,
            totalExercises = 4,
            timeSpentMinutes = 90,
            firstAccessAt = System.currentTimeMillis() - 1814400000, // 3 weeks ago
            lastAccessAt = System.currentTimeMillis() - 1209600000, // 2 weeks ago
            completedAt = System.currentTimeMillis() - 1209600000
        )

        mockChapterProgress["chapter_calc_derivatives"] = ChapterProgress(
            id = "chapterProgress_calc_derivatives",
            chapterId = "chapter_calc_derivatives",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 5,
            totalExercises = 5,
            timeSpentMinutes = 100,
            firstAccessAt = System.currentTimeMillis() - 1209600000, // 2 weeks ago
            lastAccessAt = System.currentTimeMillis() - 604800000, // 1 week ago
            completedAt = System.currentTimeMillis() - 604800000
        )

        mockChapterProgress["chapter_calc_applications"] = ChapterProgress(
            id = "chapterProgress_calc_applications",
            chapterId = "chapter_calc_applications",
            userId = "user_default",
            isCompleted = true,
            completedExercises = 3,
            totalExercises = 4,
            timeSpentMinutes = 75,
            firstAccessAt = System.currentTimeMillis() - 604800000, // 1 week ago
            lastAccessAt = System.currentTimeMillis() - 7200000, // 2 hours ago
            completedAt = System.currentTimeMillis() - 259200000 // 3 days ago
        )

        // Create mock achievements
        mockAchievements["achievement1"] = Achievement(
            id = "achievement1",
            title = "First Steps",
            description = "Complete your first chapter",
            iconUrl = "icon_first_steps",
            requirement = AchievementRequirement(
                type = AchievementType.CHAPTERS_COMPLETED,
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

    // UPDATED: incrementLessonProgress → incrementChapterProgress
    override suspend fun incrementChapterProgress(courseId: String, chapterId: String) {
        val progress = mockUserProgress[courseId] ?: return
        mockUserProgress[courseId] = progress.copy(
            completedChapters = progress.completedChapters + 1,
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

    // UPDATED: getLessonProgress → getChapterProgress
    override fun getChapterProgress(chapterId: String, userId: String): Flow<ChapterProgress?> = 
        flowOf(mockChapterProgress[chapterId])

    // UPDATED: getUserLessonProgress → getUserChapterProgress
    override fun getUserChapterProgress(userId: String): Flow<List<ChapterProgress>> = 
        flowOf(mockChapterProgress.values.toList())

    // UPDATED: getCompletedLessons → getCompletedChapters
    override fun getCompletedChapters(userId: String): Flow<List<ChapterProgress>> = 
        flowOf(mockChapterProgress.values.filter { it.isCompleted })

    // UPDATED: startLesson → startChapter
    override suspend fun startChapter(chapterId: String, userId: String) {
        mockChapterProgress[chapterId] = ChapterProgress(
            id = "progress_$chapterId",
            chapterId = chapterId,
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

    // UPDATED: completeLesson → completeChapter
    override suspend fun completeChapter(chapterId: String, userId: String, timeSpent: Long) {
        mockChapterProgress[chapterId]?.let { progress ->
            mockChapterProgress[chapterId] = progress.copy(
                isCompleted = true,
                timeSpentMinutes = timeSpent,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    // UPDATED: updateLessonProgress → updateChapterProgress
    override suspend fun updateChapterProgress(progress: ChapterProgress) {
        mockChapterProgress[progress.chapterId] = progress
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

    // UPDATED: lessonId → chapterId parameter
    override suspend fun startStudySession(userId: String, courseId: String?, chapterId: String?, sessionType: String): StudySession {
        val session = StudySession(
            id = "session_${System.currentTimeMillis()}",
            userId = userId,
            courseId = courseId,
            chapterId = chapterId,
            sessionType = SessionType.CHAPTER_STUDY,
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
                chaptersCompleted = 2,
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
            chaptersCompleted = 2,
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
            chaptersCompleted = 8,
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
            chaptersPerWeek = 2.5f,
            exercisesPerSession = 8.0f,
            timePerChapter = 1500000L,
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

    // UPDATED: getNextLessonRecommendation → getNextChapterRecommendation
    override suspend fun getNextChapterRecommendation(userId: String, courseId: String): ChapterRecommendation? {
        return ChapterRecommendation(
            chapterId = "chapter_next",
            title = "Advanced Algebra",
            reason = "Based on your progress in basic algebra",
            confidence = 0.8f,
            estimatedDuration = 45L,
            prerequisites = listOf("chapter_basic_algebra"),
            difficulty = "Intermediate"
        )
    }
} 