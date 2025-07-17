package com.example.ai.edge.eliza.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.ai.edge.eliza.core.database.dao.ChatDao;
import com.example.ai.edge.eliza.core.database.dao.ChatDao_Impl;
import com.example.ai.edge.eliza.core.database.dao.CourseDao;
import com.example.ai.edge.eliza.core.database.dao.CourseDao_Impl;
import com.example.ai.edge.eliza.core.database.dao.ProgressDao;
import com.example.ai.edge.eliza.core.database.dao.ProgressDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ElizaDatabase_Impl extends ElizaDatabase {
  private volatile CourseDao _courseDao;

  private volatile ChatDao _chatDao;

  private volatile ProgressDao _progressDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `courses` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `subject` TEXT NOT NULL, `grade` TEXT NOT NULL, `description` TEXT NOT NULL, `totalLessons` INTEGER NOT NULL, `estimatedHours` INTEGER NOT NULL, `imageUrl` TEXT, `isDownloaded` INTEGER NOT NULL, `downloadUrl` TEXT, `sizeInBytes` INTEGER NOT NULL, `version` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lessons` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `lessonNumber` INTEGER NOT NULL, `title` TEXT NOT NULL, `markdownContent` TEXT NOT NULL, `imageReferences` TEXT NOT NULL, `estimatedReadingTime` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`courseId`) REFERENCES `courses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` TEXT NOT NULL, `lessonId` TEXT NOT NULL, `questionText` TEXT NOT NULL, `options` TEXT NOT NULL, `correctAnswerIndex` INTEGER NOT NULL, `explanation` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `userAnswer` INTEGER, `isCorrect` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`lessonId`) REFERENCES `lessons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trials` (`id` TEXT NOT NULL, `originalExerciseId` TEXT NOT NULL, `questionText` TEXT NOT NULL, `options` TEXT NOT NULL, `correctAnswerIndex` INTEGER NOT NULL, `explanation` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `userAnswer` INTEGER, `isCorrect` INTEGER, `generatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`originalExerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chat_sessions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `subject` TEXT, `courseId` TEXT, `lessonId` TEXT, `createdAt` INTEGER NOT NULL, `lastMessageAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `content` TEXT NOT NULL, `isUser` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `imageUri` TEXT, `mathSteps` TEXT NOT NULL, `messageType` TEXT NOT NULL, `status` TEXT NOT NULL, `relatedExerciseId` TEXT, `relatedTrialId` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `math_steps` (`id` TEXT NOT NULL, `messageId` TEXT NOT NULL, `stepNumber` INTEGER NOT NULL, `description` TEXT NOT NULL, `equation` TEXT, `explanation` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`messageId`) REFERENCES `chat_messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `image_math_problems` (`id` TEXT NOT NULL, `imageUri` TEXT NOT NULL, `extractedText` TEXT NOT NULL, `problemType` TEXT NOT NULL, `confidence` REAL NOT NULL, `boundingBoxes` TEXT NOT NULL, `processedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `bounding_boxes` (`id` TEXT NOT NULL, `problemId` TEXT NOT NULL, `x` REAL NOT NULL, `y` REAL NOT NULL, `width` REAL NOT NULL, `height` REAL NOT NULL, `confidence` REAL NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`problemId`) REFERENCES `image_math_problems`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_progress` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `completedLessons` INTEGER NOT NULL, `totalLessons` INTEGER NOT NULL, `completedExercises` INTEGER NOT NULL, `totalExercises` INTEGER NOT NULL, `correctAnswers` INTEGER NOT NULL, `totalAnswers` INTEGER NOT NULL, `timeSpentMinutes` INTEGER NOT NULL, `streakDays` INTEGER NOT NULL, `lastStudiedAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`courseId`) REFERENCES `courses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lesson_progress` (`id` TEXT NOT NULL, `lessonId` TEXT NOT NULL, `userId` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `completedExercises` INTEGER NOT NULL, `totalExercises` INTEGER NOT NULL, `timeSpentMinutes` INTEGER NOT NULL, `firstAccessAt` INTEGER NOT NULL, `lastAccessAt` INTEGER NOT NULL, `completedAt` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`lessonId`) REFERENCES `lessons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_answers` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `trialId` TEXT, `userId` TEXT NOT NULL, `selectedAnswer` INTEGER NOT NULL, `isCorrect` INTEGER NOT NULL, `timeSpentSeconds` INTEGER NOT NULL, `hintsUsed` INTEGER NOT NULL, `answeredAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `study_sessions` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `courseId` TEXT, `lessonId` TEXT, `sessionType` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `exercisesCompleted` INTEGER NOT NULL, `correctAnswers` INTEGER NOT NULL, `totalAnswers` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `iconUrl` TEXT, `requirementType` TEXT NOT NULL, `requirementThreshold` INTEGER NOT NULL, `requirementSubject` TEXT, `requirementDifficulty` TEXT, `rewardPoints` INTEGER NOT NULL, `unlockedAt` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `learning_stats` (`userId` TEXT NOT NULL, `totalTimeMinutes` INTEGER NOT NULL, `totalLessonsCompleted` INTEGER NOT NULL, `totalExercisesCompleted` INTEGER NOT NULL, `totalCorrectAnswers` INTEGER NOT NULL, `totalQuestions` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL, `coursesCompleted` INTEGER NOT NULL, `totalCourses` INTEGER NOT NULL, `chatSessionsCount` INTEGER NOT NULL, `imageProblemsCount` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `weekly_progress` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `weekStartDate` INTEGER NOT NULL, `weekEndDate` INTEGER NOT NULL, `minutesStudied` INTEGER NOT NULL, `lessonsCompleted` INTEGER NOT NULL, `exercisesCompleted` INTEGER NOT NULL, `daysActive` INTEGER NOT NULL, `averageAccuracy` REAL NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`userId`) REFERENCES `learning_stats`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1065caed396a89d5935e380e3794c5c6')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `courses`");
        db.execSQL("DROP TABLE IF EXISTS `lessons`");
        db.execSQL("DROP TABLE IF EXISTS `exercises`");
        db.execSQL("DROP TABLE IF EXISTS `trials`");
        db.execSQL("DROP TABLE IF EXISTS `chat_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `chat_messages`");
        db.execSQL("DROP TABLE IF EXISTS `math_steps`");
        db.execSQL("DROP TABLE IF EXISTS `image_math_problems`");
        db.execSQL("DROP TABLE IF EXISTS `bounding_boxes`");
        db.execSQL("DROP TABLE IF EXISTS `user_progress`");
        db.execSQL("DROP TABLE IF EXISTS `lesson_progress`");
        db.execSQL("DROP TABLE IF EXISTS `user_answers`");
        db.execSQL("DROP TABLE IF EXISTS `study_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `achievements`");
        db.execSQL("DROP TABLE IF EXISTS `learning_stats`");
        db.execSQL("DROP TABLE IF EXISTS `weekly_progress`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCourses = new HashMap<String, TableInfo.Column>(14);
        _columnsCourses.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("subject", new TableInfo.Column("subject", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("grade", new TableInfo.Column("grade", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("totalLessons", new TableInfo.Column("totalLessons", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("estimatedHours", new TableInfo.Column("estimatedHours", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("isDownloaded", new TableInfo.Column("isDownloaded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("downloadUrl", new TableInfo.Column("downloadUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("sizeInBytes", new TableInfo.Column("sizeInBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("version", new TableInfo.Column("version", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCourses.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCourses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCourses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCourses = new TableInfo("courses", _columnsCourses, _foreignKeysCourses, _indicesCourses);
        final TableInfo _existingCourses = TableInfo.read(db, "courses");
        if (!_infoCourses.equals(_existingCourses)) {
          return new RoomOpenHelper.ValidationResult(false, "courses(com.example.ai.edge.eliza.core.database.entity.CourseEntity).\n"
                  + " Expected:\n" + _infoCourses + "\n"
                  + " Found:\n" + _existingCourses);
        }
        final HashMap<String, TableInfo.Column> _columnsLessons = new HashMap<String, TableInfo.Column>(9);
        _columnsLessons.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("courseId", new TableInfo.Column("courseId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("lessonNumber", new TableInfo.Column("lessonNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("markdownContent", new TableInfo.Column("markdownContent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("imageReferences", new TableInfo.Column("imageReferences", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("estimatedReadingTime", new TableInfo.Column("estimatedReadingTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessons.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLessons = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysLessons.add(new TableInfo.ForeignKey("courses", "CASCADE", "NO ACTION", Arrays.asList("courseId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesLessons = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLessons = new TableInfo("lessons", _columnsLessons, _foreignKeysLessons, _indicesLessons);
        final TableInfo _existingLessons = TableInfo.read(db, "lessons");
        if (!_infoLessons.equals(_existingLessons)) {
          return new RoomOpenHelper.ValidationResult(false, "lessons(com.example.ai.edge.eliza.core.database.entity.LessonEntity).\n"
                  + " Expected:\n" + _infoLessons + "\n"
                  + " Found:\n" + _existingLessons);
        }
        final HashMap<String, TableInfo.Column> _columnsExercises = new HashMap<String, TableInfo.Column>(11);
        _columnsExercises.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("lessonId", new TableInfo.Column("lessonId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("questionText", new TableInfo.Column("questionText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("options", new TableInfo.Column("options", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("correctAnswerIndex", new TableInfo.Column("correctAnswerIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("explanation", new TableInfo.Column("explanation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("userAnswer", new TableInfo.Column("userAnswer", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("isCorrect", new TableInfo.Column("isCorrect", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercises.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExercises = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExercises.add(new TableInfo.ForeignKey("lessons", "CASCADE", "NO ACTION", Arrays.asList("lessonId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExercises = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExercises = new TableInfo("exercises", _columnsExercises, _foreignKeysExercises, _indicesExercises);
        final TableInfo _existingExercises = TableInfo.read(db, "exercises");
        if (!_infoExercises.equals(_existingExercises)) {
          return new RoomOpenHelper.ValidationResult(false, "exercises(com.example.ai.edge.eliza.core.database.entity.ExerciseEntity).\n"
                  + " Expected:\n" + _infoExercises + "\n"
                  + " Found:\n" + _existingExercises);
        }
        final HashMap<String, TableInfo.Column> _columnsTrials = new HashMap<String, TableInfo.Column>(11);
        _columnsTrials.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("originalExerciseId", new TableInfo.Column("originalExerciseId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("questionText", new TableInfo.Column("questionText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("options", new TableInfo.Column("options", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("correctAnswerIndex", new TableInfo.Column("correctAnswerIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("explanation", new TableInfo.Column("explanation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("userAnswer", new TableInfo.Column("userAnswer", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("isCorrect", new TableInfo.Column("isCorrect", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrials.put("generatedAt", new TableInfo.Column("generatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrials = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTrials.add(new TableInfo.ForeignKey("exercises", "CASCADE", "NO ACTION", Arrays.asList("originalExerciseId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTrials = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTrials = new TableInfo("trials", _columnsTrials, _foreignKeysTrials, _indicesTrials);
        final TableInfo _existingTrials = TableInfo.read(db, "trials");
        if (!_infoTrials.equals(_existingTrials)) {
          return new RoomOpenHelper.ValidationResult(false, "trials(com.example.ai.edge.eliza.core.database.entity.TrialEntity).\n"
                  + " Expected:\n" + _infoTrials + "\n"
                  + " Found:\n" + _existingTrials);
        }
        final HashMap<String, TableInfo.Column> _columnsChatSessions = new HashMap<String, TableInfo.Column>(8);
        _columnsChatSessions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("subject", new TableInfo.Column("subject", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("courseId", new TableInfo.Column("courseId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("lessonId", new TableInfo.Column("lessonId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("lastMessageAt", new TableInfo.Column("lastMessageAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatSessions.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChatSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChatSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChatSessions = new TableInfo("chat_sessions", _columnsChatSessions, _foreignKeysChatSessions, _indicesChatSessions);
        final TableInfo _existingChatSessions = TableInfo.read(db, "chat_sessions");
        if (!_infoChatSessions.equals(_existingChatSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "chat_sessions(com.example.ai.edge.eliza.core.database.entity.ChatSessionEntity).\n"
                  + " Expected:\n" + _infoChatSessions + "\n"
                  + " Found:\n" + _existingChatSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsChatMessages = new HashMap<String, TableInfo.Column>(11);
        _columnsChatMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("isUser", new TableInfo.Column("isUser", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("imageUri", new TableInfo.Column("imageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("mathSteps", new TableInfo.Column("mathSteps", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("relatedExerciseId", new TableInfo.Column("relatedExerciseId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("relatedTrialId", new TableInfo.Column("relatedTrialId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChatMessages = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysChatMessages.add(new TableInfo.ForeignKey("chat_sessions", "CASCADE", "NO ACTION", Arrays.asList("sessionId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesChatMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChatMessages = new TableInfo("chat_messages", _columnsChatMessages, _foreignKeysChatMessages, _indicesChatMessages);
        final TableInfo _existingChatMessages = TableInfo.read(db, "chat_messages");
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "chat_messages(com.example.ai.edge.eliza.core.database.entity.ChatMessageEntity).\n"
                  + " Expected:\n" + _infoChatMessages + "\n"
                  + " Found:\n" + _existingChatMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsMathSteps = new HashMap<String, TableInfo.Column>(6);
        _columnsMathSteps.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMathSteps.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMathSteps.put("stepNumber", new TableInfo.Column("stepNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMathSteps.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMathSteps.put("equation", new TableInfo.Column("equation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMathSteps.put("explanation", new TableInfo.Column("explanation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMathSteps = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMathSteps.add(new TableInfo.ForeignKey("chat_messages", "CASCADE", "NO ACTION", Arrays.asList("messageId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMathSteps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMathSteps = new TableInfo("math_steps", _columnsMathSteps, _foreignKeysMathSteps, _indicesMathSteps);
        final TableInfo _existingMathSteps = TableInfo.read(db, "math_steps");
        if (!_infoMathSteps.equals(_existingMathSteps)) {
          return new RoomOpenHelper.ValidationResult(false, "math_steps(com.example.ai.edge.eliza.core.database.entity.MathStepEntity).\n"
                  + " Expected:\n" + _infoMathSteps + "\n"
                  + " Found:\n" + _existingMathSteps);
        }
        final HashMap<String, TableInfo.Column> _columnsImageMathProblems = new HashMap<String, TableInfo.Column>(7);
        _columnsImageMathProblems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("imageUri", new TableInfo.Column("imageUri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("extractedText", new TableInfo.Column("extractedText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("problemType", new TableInfo.Column("problemType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("boundingBoxes", new TableInfo.Column("boundingBoxes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageMathProblems.put("processedAt", new TableInfo.Column("processedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImageMathProblems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImageMathProblems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoImageMathProblems = new TableInfo("image_math_problems", _columnsImageMathProblems, _foreignKeysImageMathProblems, _indicesImageMathProblems);
        final TableInfo _existingImageMathProblems = TableInfo.read(db, "image_math_problems");
        if (!_infoImageMathProblems.equals(_existingImageMathProblems)) {
          return new RoomOpenHelper.ValidationResult(false, "image_math_problems(com.example.ai.edge.eliza.core.database.entity.ImageMathProblemEntity).\n"
                  + " Expected:\n" + _infoImageMathProblems + "\n"
                  + " Found:\n" + _existingImageMathProblems);
        }
        final HashMap<String, TableInfo.Column> _columnsBoundingBoxes = new HashMap<String, TableInfo.Column>(7);
        _columnsBoundingBoxes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("problemId", new TableInfo.Column("problemId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("x", new TableInfo.Column("x", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("y", new TableInfo.Column("y", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("width", new TableInfo.Column("width", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("height", new TableInfo.Column("height", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBoundingBoxes.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBoundingBoxes = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysBoundingBoxes.add(new TableInfo.ForeignKey("image_math_problems", "CASCADE", "NO ACTION", Arrays.asList("problemId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesBoundingBoxes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBoundingBoxes = new TableInfo("bounding_boxes", _columnsBoundingBoxes, _foreignKeysBoundingBoxes, _indicesBoundingBoxes);
        final TableInfo _existingBoundingBoxes = TableInfo.read(db, "bounding_boxes");
        if (!_infoBoundingBoxes.equals(_existingBoundingBoxes)) {
          return new RoomOpenHelper.ValidationResult(false, "bounding_boxes(com.example.ai.edge.eliza.core.database.entity.BoundingBoxEntity).\n"
                  + " Expected:\n" + _infoBoundingBoxes + "\n"
                  + " Found:\n" + _existingBoundingBoxes);
        }
        final HashMap<String, TableInfo.Column> _columnsUserProgress = new HashMap<String, TableInfo.Column>(13);
        _columnsUserProgress.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("courseId", new TableInfo.Column("courseId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("completedLessons", new TableInfo.Column("completedLessons", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("totalLessons", new TableInfo.Column("totalLessons", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("completedExercises", new TableInfo.Column("completedExercises", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("totalExercises", new TableInfo.Column("totalExercises", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("correctAnswers", new TableInfo.Column("correctAnswers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("totalAnswers", new TableInfo.Column("totalAnswers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("timeSpentMinutes", new TableInfo.Column("timeSpentMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("streakDays", new TableInfo.Column("streakDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("lastStudiedAt", new TableInfo.Column("lastStudiedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProgress = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysUserProgress.add(new TableInfo.ForeignKey("courses", "CASCADE", "NO ACTION", Arrays.asList("courseId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesUserProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProgress = new TableInfo("user_progress", _columnsUserProgress, _foreignKeysUserProgress, _indicesUserProgress);
        final TableInfo _existingUserProgress = TableInfo.read(db, "user_progress");
        if (!_infoUserProgress.equals(_existingUserProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "user_progress(com.example.ai.edge.eliza.core.database.entity.UserProgressEntity).\n"
                  + " Expected:\n" + _infoUserProgress + "\n"
                  + " Found:\n" + _existingUserProgress);
        }
        final HashMap<String, TableInfo.Column> _columnsLessonProgress = new HashMap<String, TableInfo.Column>(10);
        _columnsLessonProgress.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("lessonId", new TableInfo.Column("lessonId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("completedExercises", new TableInfo.Column("completedExercises", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("totalExercises", new TableInfo.Column("totalExercises", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("timeSpentMinutes", new TableInfo.Column("timeSpentMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("firstAccessAt", new TableInfo.Column("firstAccessAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("lastAccessAt", new TableInfo.Column("lastAccessAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLessonProgress.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLessonProgress = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysLessonProgress.add(new TableInfo.ForeignKey("lessons", "CASCADE", "NO ACTION", Arrays.asList("lessonId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesLessonProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLessonProgress = new TableInfo("lesson_progress", _columnsLessonProgress, _foreignKeysLessonProgress, _indicesLessonProgress);
        final TableInfo _existingLessonProgress = TableInfo.read(db, "lesson_progress");
        if (!_infoLessonProgress.equals(_existingLessonProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "lesson_progress(com.example.ai.edge.eliza.core.database.entity.LessonProgressEntity).\n"
                  + " Expected:\n" + _infoLessonProgress + "\n"
                  + " Found:\n" + _existingLessonProgress);
        }
        final HashMap<String, TableInfo.Column> _columnsUserAnswers = new HashMap<String, TableInfo.Column>(9);
        _columnsUserAnswers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("exerciseId", new TableInfo.Column("exerciseId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("trialId", new TableInfo.Column("trialId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("selectedAnswer", new TableInfo.Column("selectedAnswer", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("isCorrect", new TableInfo.Column("isCorrect", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("timeSpentSeconds", new TableInfo.Column("timeSpentSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("hintsUsed", new TableInfo.Column("hintsUsed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAnswers.put("answeredAt", new TableInfo.Column("answeredAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserAnswers = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysUserAnswers.add(new TableInfo.ForeignKey("exercises", "CASCADE", "NO ACTION", Arrays.asList("exerciseId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesUserAnswers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserAnswers = new TableInfo("user_answers", _columnsUserAnswers, _foreignKeysUserAnswers, _indicesUserAnswers);
        final TableInfo _existingUserAnswers = TableInfo.read(db, "user_answers");
        if (!_infoUserAnswers.equals(_existingUserAnswers)) {
          return new RoomOpenHelper.ValidationResult(false, "user_answers(com.example.ai.edge.eliza.core.database.entity.UserAnswerEntity).\n"
                  + " Expected:\n" + _infoUserAnswers + "\n"
                  + " Found:\n" + _existingUserAnswers);
        }
        final HashMap<String, TableInfo.Column> _columnsStudySessions = new HashMap<String, TableInfo.Column>(11);
        _columnsStudySessions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("courseId", new TableInfo.Column("courseId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("lessonId", new TableInfo.Column("lessonId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("sessionType", new TableInfo.Column("sessionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("durationMinutes", new TableInfo.Column("durationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("exercisesCompleted", new TableInfo.Column("exercisesCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("correctAnswers", new TableInfo.Column("correctAnswers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("totalAnswers", new TableInfo.Column("totalAnswers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("startedAt", new TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudySessions.put("endedAt", new TableInfo.Column("endedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStudySessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStudySessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStudySessions = new TableInfo("study_sessions", _columnsStudySessions, _foreignKeysStudySessions, _indicesStudySessions);
        final TableInfo _existingStudySessions = TableInfo.read(db, "study_sessions");
        if (!_infoStudySessions.equals(_existingStudySessions)) {
          return new RoomOpenHelper.ValidationResult(false, "study_sessions(com.example.ai.edge.eliza.core.database.entity.StudySessionEntity).\n"
                  + " Expected:\n" + _infoStudySessions + "\n"
                  + " Found:\n" + _existingStudySessions);
        }
        final HashMap<String, TableInfo.Column> _columnsAchievements = new HashMap<String, TableInfo.Column>(11);
        _columnsAchievements.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("iconUrl", new TableInfo.Column("iconUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("requirementType", new TableInfo.Column("requirementType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("requirementThreshold", new TableInfo.Column("requirementThreshold", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("requirementSubject", new TableInfo.Column("requirementSubject", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("requirementDifficulty", new TableInfo.Column("requirementDifficulty", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("rewardPoints", new TableInfo.Column("rewardPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("unlockedAt", new TableInfo.Column("unlockedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAchievements = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAchievements = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAchievements = new TableInfo("achievements", _columnsAchievements, _foreignKeysAchievements, _indicesAchievements);
        final TableInfo _existingAchievements = TableInfo.read(db, "achievements");
        if (!_infoAchievements.equals(_existingAchievements)) {
          return new RoomOpenHelper.ValidationResult(false, "achievements(com.example.ai.edge.eliza.core.database.entity.AchievementEntity).\n"
                  + " Expected:\n" + _infoAchievements + "\n"
                  + " Found:\n" + _existingAchievements);
        }
        final HashMap<String, TableInfo.Column> _columnsLearningStats = new HashMap<String, TableInfo.Column>(13);
        _columnsLearningStats.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalTimeMinutes", new TableInfo.Column("totalTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalLessonsCompleted", new TableInfo.Column("totalLessonsCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalExercisesCompleted", new TableInfo.Column("totalExercisesCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalCorrectAnswers", new TableInfo.Column("totalCorrectAnswers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalQuestions", new TableInfo.Column("totalQuestions", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("longestStreak", new TableInfo.Column("longestStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("coursesCompleted", new TableInfo.Column("coursesCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("totalCourses", new TableInfo.Column("totalCourses", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("chatSessionsCount", new TableInfo.Column("chatSessionsCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("imageProblemsCount", new TableInfo.Column("imageProblemsCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearningStats.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLearningStats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLearningStats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLearningStats = new TableInfo("learning_stats", _columnsLearningStats, _foreignKeysLearningStats, _indicesLearningStats);
        final TableInfo _existingLearningStats = TableInfo.read(db, "learning_stats");
        if (!_infoLearningStats.equals(_existingLearningStats)) {
          return new RoomOpenHelper.ValidationResult(false, "learning_stats(com.example.ai.edge.eliza.core.database.entity.LearningStatsEntity).\n"
                  + " Expected:\n" + _infoLearningStats + "\n"
                  + " Found:\n" + _existingLearningStats);
        }
        final HashMap<String, TableInfo.Column> _columnsWeeklyProgress = new HashMap<String, TableInfo.Column>(9);
        _columnsWeeklyProgress.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("weekStartDate", new TableInfo.Column("weekStartDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("weekEndDate", new TableInfo.Column("weekEndDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("minutesStudied", new TableInfo.Column("minutesStudied", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("lessonsCompleted", new TableInfo.Column("lessonsCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("exercisesCompleted", new TableInfo.Column("exercisesCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("daysActive", new TableInfo.Column("daysActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyProgress.put("averageAccuracy", new TableInfo.Column("averageAccuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWeeklyProgress = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysWeeklyProgress.add(new TableInfo.ForeignKey("learning_stats", "CASCADE", "NO ACTION", Arrays.asList("userId"), Arrays.asList("userId")));
        final HashSet<TableInfo.Index> _indicesWeeklyProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWeeklyProgress = new TableInfo("weekly_progress", _columnsWeeklyProgress, _foreignKeysWeeklyProgress, _indicesWeeklyProgress);
        final TableInfo _existingWeeklyProgress = TableInfo.read(db, "weekly_progress");
        if (!_infoWeeklyProgress.equals(_existingWeeklyProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "weekly_progress(com.example.ai.edge.eliza.core.database.entity.WeeklyProgressEntity).\n"
                  + " Expected:\n" + _infoWeeklyProgress + "\n"
                  + " Found:\n" + _existingWeeklyProgress);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1065caed396a89d5935e380e3794c5c6", "bd21ddd24eb33428a550acd67c44272a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "courses","lessons","exercises","trials","chat_sessions","chat_messages","math_steps","image_math_problems","bounding_boxes","user_progress","lesson_progress","user_answers","study_sessions","achievements","learning_stats","weekly_progress");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `courses`");
      _db.execSQL("DELETE FROM `lessons`");
      _db.execSQL("DELETE FROM `exercises`");
      _db.execSQL("DELETE FROM `trials`");
      _db.execSQL("DELETE FROM `chat_sessions`");
      _db.execSQL("DELETE FROM `chat_messages`");
      _db.execSQL("DELETE FROM `math_steps`");
      _db.execSQL("DELETE FROM `image_math_problems`");
      _db.execSQL("DELETE FROM `bounding_boxes`");
      _db.execSQL("DELETE FROM `user_progress`");
      _db.execSQL("DELETE FROM `lesson_progress`");
      _db.execSQL("DELETE FROM `user_answers`");
      _db.execSQL("DELETE FROM `study_sessions`");
      _db.execSQL("DELETE FROM `achievements`");
      _db.execSQL("DELETE FROM `learning_stats`");
      _db.execSQL("DELETE FROM `weekly_progress`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CourseDao.class, CourseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProgressDao.class, ProgressDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CourseDao courseDao() {
    if (_courseDao != null) {
      return _courseDao;
    } else {
      synchronized(this) {
        if(_courseDao == null) {
          _courseDao = new CourseDao_Impl(this);
        }
        return _courseDao;
      }
    }
  }

  @Override
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }

  @Override
  public ProgressDao progressDao() {
    if (_progressDao != null) {
      return _progressDao;
    } else {
      synchronized(this) {
        if(_progressDao == null) {
          _progressDao = new ProgressDao_Impl(this);
        }
        return _progressDao;
      }
    }
  }
}
