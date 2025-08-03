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

package com.example.ai.edge.eliza.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.ai.edge.eliza.core.database.converter.Converters
import com.example.ai.edge.eliza.core.database.dao.ChatDao
import com.example.ai.edge.eliza.core.database.dao.CourseDao
import com.example.ai.edge.eliza.core.database.dao.ProgressDao
import com.example.ai.edge.eliza.core.database.entity.AchievementEntity
import com.example.ai.edge.eliza.core.database.entity.BoundingBoxEntity
import com.example.ai.edge.eliza.core.database.entity.ChatMessageEntity
import com.example.ai.edge.eliza.core.database.entity.ChatSessionEntity
import com.example.ai.edge.eliza.core.database.entity.CourseEntity
import com.example.ai.edge.eliza.core.database.entity.ExerciseEntity
import com.example.ai.edge.eliza.core.database.entity.ImageMathProblemEntity
import com.example.ai.edge.eliza.core.database.entity.LearningStatsEntity
import com.example.ai.edge.eliza.core.database.entity.ChapterEntity
import com.example.ai.edge.eliza.core.database.entity.ChapterProgressEntity
import com.example.ai.edge.eliza.core.database.entity.MathStepEntity
import com.example.ai.edge.eliza.core.database.entity.StudySessionEntity
import com.example.ai.edge.eliza.core.database.entity.TrialEntity
import com.example.ai.edge.eliza.core.database.entity.UserAnswerEntity
import com.example.ai.edge.eliza.core.database.entity.UserProgressEntity
import com.example.ai.edge.eliza.core.database.entity.WeeklyProgressEntity
import com.example.ai.edge.eliza.core.database.entity.VideoExplanationEntity
import com.example.ai.edge.eliza.core.database.entity.ExerciseHelpEntity

/**
 * Main Room database for the Eliza AI tutoring app.
 * This database stores all local data including courses, chapters, exercises,
 * chat sessions, user progress, learning analytics, and video explanations.
 * UPDATED: Added support for chapter-based organization and video explanation system.
 */
@Database(
    entities = [
        // Course-related entities
        CourseEntity::class,
        ChapterEntity::class,
        ExerciseEntity::class,
        TrialEntity::class,
        
        // NEW: Video and help system entities
        VideoExplanationEntity::class,
        ExerciseHelpEntity::class,
        
        // Chat-related entities
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        MathStepEntity::class,
        ImageMathProblemEntity::class,
        BoundingBoxEntity::class,
        
        // Progress-related entities
        UserProgressEntity::class,
        ChapterProgressEntity::class,
        UserAnswerEntity::class,
        StudySessionEntity::class,
        AchievementEntity::class,
        LearningStatsEntity::class,
        WeeklyProgressEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4) // NEW: Add ChatType fields to ChatSessionEntity
    ]
)
@TypeConverters(Converters::class)
abstract class ElizaDatabase : RoomDatabase() {
    
    abstract fun courseDao(): CourseDao
    abstract fun chatDao(): ChatDao
    abstract fun progressDao(): ProgressDao
    
    companion object {
        private const val DATABASE_NAME = "eliza_database"
        
        @Volatile
        private var INSTANCE: ElizaDatabase? = null
        
        fun getDatabase(context: Context): ElizaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ElizaDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development only - will recreate DB with new schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 