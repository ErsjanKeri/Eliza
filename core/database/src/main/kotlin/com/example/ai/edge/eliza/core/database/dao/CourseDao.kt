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
import com.example.ai.edge.eliza.core.database.entity.CourseEntity
import com.example.ai.edge.eliza.core.database.entity.ExerciseEntity
import com.example.ai.edge.eliza.core.database.entity.ChapterEntity
import com.example.ai.edge.eliza.core.database.entity.TrialEntity
import com.example.ai.edge.eliza.core.database.entity.VideoExplanationEntity
import com.example.ai.edge.eliza.core.database.entity.ExerciseHelpEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for course-related operations.
 * UPDATED: Now supports chapter-based operations and video explanation system.
 */
@Dao
interface CourseDao {
    
    // Course operations
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: String): Flow<CourseEntity?>
    
    @Query("SELECT * FROM courses WHERE subject = :subject ORDER BY createdAt DESC")
    fun getCoursesBySubject(subject: String): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE isDownloaded = 1 ORDER BY createdAt DESC")
    fun getDownloadedCourses(): Flow<List<CourseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    @Update
    suspend fun updateCourse(course: CourseEntity)
    
    @Delete
    suspend fun deleteCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String)
    
    // Chapter operations (RENAMED from lesson operations)
    @Query("SELECT * FROM chapters WHERE courseId = :courseId ORDER BY chapterNumber ASC")
    fun getChaptersByCourse(courseId: String): Flow<List<ChapterEntity>>
    
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterById(chapterId: String): Flow<ChapterEntity?>
    
    @Query("SELECT * FROM chapters WHERE courseId = :courseId AND chapterNumber = :chapterNumber")
    fun getChapterByNumber(courseId: String, chapterNumber: Int): Flow<ChapterEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)
    
    @Update
    suspend fun updateChapter(chapter: ChapterEntity)
    
    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
    
    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: String)
    
    // Exercise operations (UPDATED for chapter-based structure)
    @Query("SELECT * FROM exercises WHERE chapterId = :chapterId ORDER BY createdAt ASC")
    fun getExercisesByChapter(chapterId: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: String): Flow<ExerciseEntity?>
    
    @Query("SELECT * FROM exercises WHERE chapterId = :chapterId AND isCompleted = 0")
    fun getIncompleteExercises(chapterId: String): Flow<List<ExerciseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
    
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: String)
    
    // Trial operations
    @Query("SELECT * FROM trials WHERE originalExerciseId = :exerciseId ORDER BY generatedAt DESC")
    fun getTrialsByExercise(exerciseId: String): Flow<List<TrialEntity>>
    
    @Query("SELECT * FROM trials WHERE id = :trialId")
    fun getTrialById(trialId: String): Flow<TrialEntity?>
    
    @Query("SELECT * FROM trials WHERE originalExerciseId = :exerciseId AND isCompleted = 0")
    fun getIncompleteTrials(exerciseId: String): Flow<List<TrialEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrial(trial: TrialEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrials(trials: List<TrialEntity>)
    
    @Update
    suspend fun updateTrial(trial: TrialEntity)
    
    @Delete
    suspend fun deleteTrial(trial: TrialEntity)
    
    @Query("DELETE FROM trials WHERE id = :trialId")
    suspend fun deleteTrialById(trialId: String)
    
    // NEW: Video explanation operations
    @Query("SELECT * FROM video_explanations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getVideoExplanationsByUser(userId: String): Flow<List<VideoExplanationEntity>>
    
    @Query("SELECT * FROM video_explanations WHERE chapterId = :chapterId AND userId = :userId ORDER BY createdAt DESC")
    fun getVideoExplanationsByChapter(chapterId: String, userId: String): Flow<List<VideoExplanationEntity>>
    
    @Query("SELECT * FROM video_explanations WHERE exerciseId = :exerciseId AND userId = :userId ORDER BY createdAt DESC")
    fun getVideoExplanationsByExercise(exerciseId: String, userId: String): Flow<List<VideoExplanationEntity>>
    
    @Query("SELECT * FROM video_explanations WHERE id = :videoId")
    fun getVideoExplanationById(videoId: String): Flow<VideoExplanationEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoExplanation(videoExplanation: VideoExplanationEntity)
    
    @Update
    suspend fun updateVideoExplanation(videoExplanation: VideoExplanationEntity)
    
    @Delete
    suspend fun deleteVideoExplanation(videoExplanation: VideoExplanationEntity)
    
    @Query("DELETE FROM video_explanations WHERE id = :videoId")
    suspend fun deleteVideoExplanationById(videoId: String)
    
    @Query("UPDATE video_explanations SET lastAccessedAt = :timestamp WHERE id = :videoId")
    suspend fun updateVideoLastAccessed(videoId: String, timestamp: Long)
    
    // NEW: Exercise help operations
    @Query("SELECT * FROM exercise_help WHERE exerciseId = :exerciseId AND userId = :userId ORDER BY createdAt DESC")
    fun getExerciseHelpByExercise(exerciseId: String, userId: String): Flow<List<ExerciseHelpEntity>>
    
    @Query("SELECT * FROM exercise_help WHERE id = :helpId")
    fun getExerciseHelpById(helpId: String): Flow<ExerciseHelpEntity?>
    
    @Query("SELECT * FROM exercise_help WHERE userId = :userId ORDER BY createdAt DESC")
    fun getExerciseHelpByUser(userId: String): Flow<List<ExerciseHelpEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseHelp(exerciseHelp: ExerciseHelpEntity)
    
    @Update
    suspend fun updateExerciseHelp(exerciseHelp: ExerciseHelpEntity)
    
    @Delete
    suspend fun deleteExerciseHelp(exerciseHelp: ExerciseHelpEntity)
    
    @Query("DELETE FROM exercise_help WHERE id = :helpId")
    suspend fun deleteExerciseHelpById(helpId: String)
    
    @Query("UPDATE exercise_help SET wasHelpful = :wasHelpful WHERE id = :helpId")
    suspend fun updateExerciseHelpFeedback(helpId: String, wasHelpful: Boolean)
    
    // Aggregate queries (UPDATED for chapters)
    @Query("SELECT COUNT(*) FROM chapters WHERE courseId = :courseId")
    suspend fun getChapterCountByCourse(courseId: String): Int
    
    @Query("SELECT COUNT(*) FROM exercises WHERE chapterId = :chapterId")
    suspend fun getExerciseCountByChapter(chapterId: String): Int
    
    @Query("SELECT COUNT(*) FROM exercises WHERE chapterId = :chapterId AND isCompleted = 1")
    suspend fun getCompletedExerciseCount(chapterId: String): Int
    
    @Query("SELECT COUNT(*) FROM video_explanations WHERE userId = :userId")
    suspend fun getVideoExplanationCountByUser(userId: String): Int
} 