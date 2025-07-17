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
import com.example.ai.edge.eliza.core.database.entity.LessonEntity
import com.example.ai.edge.eliza.core.database.entity.TrialEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for course-related operations.
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
    
    // Lesson operations
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY lessonNumber ASC")
    fun getLessonsByCourse(courseId: String): Flow<List<LessonEntity>>
    
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: String): Flow<LessonEntity?>
    
    @Query("SELECT * FROM lessons WHERE courseId = :courseId AND lessonNumber = :lessonNumber")
    fun getLessonByNumber(courseId: String, lessonNumber: Int): Flow<LessonEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)
    
    @Update
    suspend fun updateLesson(lesson: LessonEntity)
    
    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)
    
    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLessonById(lessonId: String)
    
    // Exercise operations
    @Query("SELECT * FROM exercises WHERE lessonId = :lessonId ORDER BY createdAt ASC")
    fun getExercisesByLesson(lessonId: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: String): Flow<ExerciseEntity?>
    
    @Query("SELECT * FROM exercises WHERE lessonId = :lessonId AND isCompleted = 0")
    fun getIncompleteExercises(lessonId: String): Flow<List<ExerciseEntity>>
    
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
    
    // Aggregate queries
    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonCountByCourse(courseId: String): Int
    
    @Query("SELECT COUNT(*) FROM exercises WHERE lessonId = :lessonId")
    suspend fun getExerciseCountByLesson(lessonId: String): Int
    
    @Query("SELECT COUNT(*) FROM exercises WHERE lessonId = :lessonId AND isCompleted = 1")
    suspend fun getCompletedExerciseCount(lessonId: String): Int
} 