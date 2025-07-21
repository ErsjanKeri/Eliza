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

package com.example.ai.edge.eliza.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ai.edge.eliza.core.database.entity.AchievementEntity
import com.example.ai.edge.eliza.core.database.entity.LearningStatsEntity
import com.example.ai.edge.eliza.core.database.entity.ChapterProgressEntity // UPDATED from LessonProgressEntity
import com.example.ai.edge.eliza.core.database.entity.StudySessionEntity
import com.example.ai.edge.eliza.core.database.entity.UserAnswerEntity
import com.example.ai.edge.eliza.core.database.entity.UserProgressEntity
import com.example.ai.edge.eliza.core.database.entity.WeeklyProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for progress-related operations.
 * UPDATED: Now supports chapter-based progress tracking instead of lesson-based.
 */
@Dao
interface ProgressDao {
    
    // User Progress operations
    @Query("SELECT * FROM user_progress WHERE courseId = :courseId")
    fun getUserProgressByCourse(courseId: String): Flow<UserProgressEntity?>
    
    @Query("SELECT * FROM user_progress ORDER BY lastStudiedAt DESC")
    fun getAllUserProgress(): Flow<List<UserProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)
    
    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)
    
    @Delete
    suspend fun deleteUserProgress(progress: UserProgressEntity)
    
    @Query("DELETE FROM user_progress WHERE courseId = :courseId")
    suspend fun deleteUserProgressByCourse(courseId: String)
    
    // Chapter Progress operations (RENAMED from lesson progress operations)
    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId AND userId = :userId")
    fun getChapterProgress(chapterId: String, userId: String): Flow<ChapterProgressEntity?>
    
    @Query("SELECT * FROM chapter_progress WHERE userId = :userId ORDER BY lastAccessAt DESC")
    fun getUserChapterProgress(userId: String): Flow<List<ChapterProgressEntity>>
    
    @Query("SELECT * FROM chapter_progress WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedChapters(userId: String): Flow<List<ChapterProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterProgress(progress: ChapterProgressEntity)
    
    @Update
    suspend fun updateChapterProgress(progress: ChapterProgressEntity)
    
    @Delete
    suspend fun deleteChapterProgress(progress: ChapterProgressEntity)
    
    // User Answer operations
    @Query("SELECT * FROM user_answers WHERE exerciseId = :exerciseId AND userId = :userId")
    fun getUserAnswersByExercise(exerciseId: String, userId: String): Flow<List<UserAnswerEntity>>
    
    @Query("SELECT * FROM user_answers WHERE userId = :userId ORDER BY answeredAt DESC")
    fun getAllUserAnswers(userId: String): Flow<List<UserAnswerEntity>>
    
    @Query("SELECT * FROM user_answers WHERE userId = :userId AND isCorrect = 1")
    fun getCorrectAnswers(userId: String): Flow<List<UserAnswerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(answer: UserAnswerEntity)
    
    @Update
    suspend fun updateUserAnswer(answer: UserAnswerEntity)
    
    @Delete
    suspend fun deleteUserAnswer(answer: UserAnswerEntity)
    
    // Study Session operations (UPDATED for chapters)
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getStudySessionsByUser(userId: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND courseId = :courseId ORDER BY startedAt DESC")
    fun getStudySessionsByCourse(userId: String, courseId: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND chapterId = :chapterId ORDER BY startedAt DESC")
    fun getStudySessionsByChapter(userId: String, chapterId: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND sessionType = :sessionType ORDER BY startedAt DESC")
    fun getStudySessionsByType(userId: String, sessionType: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND endedAt IS NULL")
    fun getActiveStudySession(userId: String): Flow<StudySessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity)
    
    @Update
    suspend fun updateStudySession(session: StudySessionEntity)
    
    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)
    
    // Achievement operations
    @Query("SELECT * FROM achievements ORDER BY createdAt DESC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL ORDER BY createdAt DESC")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)
    
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
    
    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)
    
    // Learning Stats operations
    @Query("SELECT * FROM learning_stats WHERE userId = :userId")
    fun getLearningStats(userId: String): Flow<LearningStatsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningStats(stats: LearningStatsEntity)
    
    @Update
    suspend fun updateLearningStats(stats: LearningStatsEntity)
    
    @Delete
    suspend fun deleteLearningStats(stats: LearningStatsEntity)
    
    // Weekly Progress operations
    @Query("SELECT * FROM weekly_progress WHERE userId = :userId ORDER BY weekStartDate DESC")
    fun getWeeklyProgress(userId: String): Flow<List<WeeklyProgressEntity>>
    
    @Query("SELECT * FROM weekly_progress WHERE userId = :userId AND weekStartDate >= :startDate ORDER BY weekStartDate DESC")
    fun getWeeklyProgressSince(userId: String, startDate: Long): Flow<List<WeeklyProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyProgress(progress: WeeklyProgressEntity)
    
    @Update
    suspend fun updateWeeklyProgress(progress: WeeklyProgressEntity)
    
    @Delete
    suspend fun deleteWeeklyProgress(progress: WeeklyProgressEntity)
    
    // Aggregate queries (UPDATED for chapters)
    @Query("SELECT COUNT(*) FROM user_answers WHERE userId = :userId AND isCorrect = 1")
    suspend fun getCorrectAnswerCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM user_answers WHERE userId = :userId")
    suspend fun getTotalAnswerCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM chapter_progress WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedChapterCount(userId: String): Int
    
    @Query("SELECT SUM(timeSpentMinutes) FROM chapter_progress WHERE userId = :userId")
    suspend fun getTotalStudyTime(userId: String): Long
    
    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedAchievementCount(): Int
    
    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE userId = :userId AND startedAt >= :startDate")
    suspend fun getStudyTimeSince(userId: String, startDate: Long): Long
} 