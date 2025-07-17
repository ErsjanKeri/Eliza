package com.example.ai.edge.eliza.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.ai.edge.eliza.core.database.converter.Converters;
import com.example.ai.edge.eliza.core.database.entity.CourseEntity;
import com.example.ai.edge.eliza.core.database.entity.ExerciseEntity;
import com.example.ai.edge.eliza.core.database.entity.LessonEntity;
import com.example.ai.edge.eliza.core.database.entity.TrialEntity;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CourseDao_Impl implements CourseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CourseEntity> __insertionAdapterOfCourseEntity;

  private final EntityInsertionAdapter<LessonEntity> __insertionAdapterOfLessonEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<ExerciseEntity> __insertionAdapterOfExerciseEntity;

  private final EntityInsertionAdapter<TrialEntity> __insertionAdapterOfTrialEntity;

  private final EntityDeletionOrUpdateAdapter<CourseEntity> __deletionAdapterOfCourseEntity;

  private final EntityDeletionOrUpdateAdapter<LessonEntity> __deletionAdapterOfLessonEntity;

  private final EntityDeletionOrUpdateAdapter<ExerciseEntity> __deletionAdapterOfExerciseEntity;

  private final EntityDeletionOrUpdateAdapter<TrialEntity> __deletionAdapterOfTrialEntity;

  private final EntityDeletionOrUpdateAdapter<CourseEntity> __updateAdapterOfCourseEntity;

  private final EntityDeletionOrUpdateAdapter<LessonEntity> __updateAdapterOfLessonEntity;

  private final EntityDeletionOrUpdateAdapter<ExerciseEntity> __updateAdapterOfExerciseEntity;

  private final EntityDeletionOrUpdateAdapter<TrialEntity> __updateAdapterOfTrialEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCourseById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLessonById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExerciseById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTrialById;

  public CourseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCourseEntity = new EntityInsertionAdapter<CourseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `courses` (`id`,`title`,`subject`,`grade`,`description`,`totalLessons`,`estimatedHours`,`imageUrl`,`isDownloaded`,`downloadUrl`,`sizeInBytes`,`version`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CourseEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getSubject());
        statement.bindString(4, entity.getGrade());
        statement.bindString(5, entity.getDescription());
        statement.bindLong(6, entity.getTotalLessons());
        statement.bindLong(7, entity.getEstimatedHours());
        if (entity.getImageUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getImageUrl());
        }
        final int _tmp = entity.isDownloaded() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getDownloadUrl() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDownloadUrl());
        }
        statement.bindLong(11, entity.getSizeInBytes());
        statement.bindString(12, entity.getVersion());
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfLessonEntity = new EntityInsertionAdapter<LessonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `lessons` (`id`,`courseId`,`lessonNumber`,`title`,`markdownContent`,`imageReferences`,`estimatedReadingTime`,`isCompleted`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCourseId());
        statement.bindLong(3, entity.getLessonNumber());
        statement.bindString(4, entity.getTitle());
        statement.bindString(5, entity.getMarkdownContent());
        final String _tmp = __converters.fromStringList(entity.getImageReferences());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getEstimatedReadingTime());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfExerciseEntity = new EntityInsertionAdapter<ExerciseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exercises` (`id`,`lessonId`,`questionText`,`options`,`correctAnswerIndex`,`explanation`,`difficulty`,`isCompleted`,`userAnswer`,`isCorrect`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getLessonId());
        statement.bindString(3, entity.getQuestionText());
        final String _tmp = __converters.fromStringList(entity.getOptions());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getCorrectAnswerIndex());
        statement.bindString(6, entity.getExplanation());
        statement.bindString(7, entity.getDifficulty());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUserAnswer() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUserAnswer());
        }
        final Integer _tmp_2 = entity.isCorrect() == null ? null : (entity.isCorrect() ? 1 : 0);
        if (_tmp_2 == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, _tmp_2);
        }
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfTrialEntity = new EntityInsertionAdapter<TrialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `trials` (`id`,`originalExerciseId`,`questionText`,`options`,`correctAnswerIndex`,`explanation`,`difficulty`,`isCompleted`,`userAnswer`,`isCorrect`,`generatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrialEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getOriginalExerciseId());
        statement.bindString(3, entity.getQuestionText());
        final String _tmp = __converters.fromStringList(entity.getOptions());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getCorrectAnswerIndex());
        statement.bindString(6, entity.getExplanation());
        statement.bindString(7, entity.getDifficulty());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUserAnswer() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUserAnswer());
        }
        final Integer _tmp_2 = entity.isCorrect() == null ? null : (entity.isCorrect() ? 1 : 0);
        if (_tmp_2 == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, _tmp_2);
        }
        statement.bindLong(11, entity.getGeneratedAt());
      }
    };
    this.__deletionAdapterOfCourseEntity = new EntityDeletionOrUpdateAdapter<CourseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `courses` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CourseEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfLessonEntity = new EntityDeletionOrUpdateAdapter<LessonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `lessons` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfExerciseEntity = new EntityDeletionOrUpdateAdapter<ExerciseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `exercises` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfTrialEntity = new EntityDeletionOrUpdateAdapter<TrialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `trials` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrialEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfCourseEntity = new EntityDeletionOrUpdateAdapter<CourseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `courses` SET `id` = ?,`title` = ?,`subject` = ?,`grade` = ?,`description` = ?,`totalLessons` = ?,`estimatedHours` = ?,`imageUrl` = ?,`isDownloaded` = ?,`downloadUrl` = ?,`sizeInBytes` = ?,`version` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CourseEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getSubject());
        statement.bindString(4, entity.getGrade());
        statement.bindString(5, entity.getDescription());
        statement.bindLong(6, entity.getTotalLessons());
        statement.bindLong(7, entity.getEstimatedHours());
        if (entity.getImageUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getImageUrl());
        }
        final int _tmp = entity.isDownloaded() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getDownloadUrl() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDownloadUrl());
        }
        statement.bindLong(11, entity.getSizeInBytes());
        statement.bindString(12, entity.getVersion());
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
        statement.bindString(15, entity.getId());
      }
    };
    this.__updateAdapterOfLessonEntity = new EntityDeletionOrUpdateAdapter<LessonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `lessons` SET `id` = ?,`courseId` = ?,`lessonNumber` = ?,`title` = ?,`markdownContent` = ?,`imageReferences` = ?,`estimatedReadingTime` = ?,`isCompleted` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCourseId());
        statement.bindLong(3, entity.getLessonNumber());
        statement.bindString(4, entity.getTitle());
        statement.bindString(5, entity.getMarkdownContent());
        final String _tmp = __converters.fromStringList(entity.getImageReferences());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getEstimatedReadingTime());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindString(10, entity.getId());
      }
    };
    this.__updateAdapterOfExerciseEntity = new EntityDeletionOrUpdateAdapter<ExerciseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `exercises` SET `id` = ?,`lessonId` = ?,`questionText` = ?,`options` = ?,`correctAnswerIndex` = ?,`explanation` = ?,`difficulty` = ?,`isCompleted` = ?,`userAnswer` = ?,`isCorrect` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getLessonId());
        statement.bindString(3, entity.getQuestionText());
        final String _tmp = __converters.fromStringList(entity.getOptions());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getCorrectAnswerIndex());
        statement.bindString(6, entity.getExplanation());
        statement.bindString(7, entity.getDifficulty());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUserAnswer() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUserAnswer());
        }
        final Integer _tmp_2 = entity.isCorrect() == null ? null : (entity.isCorrect() ? 1 : 0);
        if (_tmp_2 == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, _tmp_2);
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindString(12, entity.getId());
      }
    };
    this.__updateAdapterOfTrialEntity = new EntityDeletionOrUpdateAdapter<TrialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trials` SET `id` = ?,`originalExerciseId` = ?,`questionText` = ?,`options` = ?,`correctAnswerIndex` = ?,`explanation` = ?,`difficulty` = ?,`isCompleted` = ?,`userAnswer` = ?,`isCorrect` = ?,`generatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrialEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getOriginalExerciseId());
        statement.bindString(3, entity.getQuestionText());
        final String _tmp = __converters.fromStringList(entity.getOptions());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getCorrectAnswerIndex());
        statement.bindString(6, entity.getExplanation());
        statement.bindString(7, entity.getDifficulty());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUserAnswer() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUserAnswer());
        }
        final Integer _tmp_2 = entity.isCorrect() == null ? null : (entity.isCorrect() ? 1 : 0);
        if (_tmp_2 == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, _tmp_2);
        }
        statement.bindLong(11, entity.getGeneratedAt());
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCourseById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM courses WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteLessonById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM lessons WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExerciseById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exercises WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteTrialById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM trials WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCourse(final CourseEntity course,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCourseEntity.insert(course);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCourses(final List<CourseEntity> courses,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCourseEntity.insert(courses);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLesson(final LessonEntity lesson,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLessonEntity.insert(lesson);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLessons(final List<LessonEntity> lessons,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLessonEntity.insert(lessons);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertExercise(final ExerciseEntity exercise,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExerciseEntity.insert(exercise);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertExercises(final List<ExerciseEntity> exercises,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExerciseEntity.insert(exercises);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertTrial(final TrialEntity trial, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrialEntity.insert(trial);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertTrials(final List<TrialEntity> trials,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrialEntity.insert(trials);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCourse(final CourseEntity course,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCourseEntity.handle(course);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLesson(final LessonEntity lesson,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLessonEntity.handle(lesson);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExercise(final ExerciseEntity exercise,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExerciseEntity.handle(exercise);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTrial(final TrialEntity trial, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTrialEntity.handle(trial);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCourse(final CourseEntity course,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCourseEntity.handle(course);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLesson(final LessonEntity lesson,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLessonEntity.handle(lesson);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExercise(final ExerciseEntity exercise,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExerciseEntity.handle(exercise);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTrial(final TrialEntity trial, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTrialEntity.handle(trial);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCourseById(final String courseId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCourseById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, courseId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCourseById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLessonById(final String lessonId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLessonById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, lessonId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLessonById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExerciseById(final String exerciseId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExerciseById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, exerciseId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExerciseById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTrialById(final String trialId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTrialById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, trialId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTrialById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CourseEntity>> getAllCourses() {
    final String _sql = "SELECT * FROM courses ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<CourseEntity>>() {
      @Override
      @NonNull
      public List<CourseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "grade");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfEstimatedHours = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedHours");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadUrl");
          final int _cursorIndexOfSizeInBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeInBytes");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<CourseEntity> _result = new ArrayList<CourseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CourseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final String _tmpGrade;
            _tmpGrade = _cursor.getString(_cursorIndexOfGrade);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpEstimatedHours;
            _tmpEstimatedHours = _cursor.getInt(_cursorIndexOfEstimatedHours);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadUrl;
            if (_cursor.isNull(_cursorIndexOfDownloadUrl)) {
              _tmpDownloadUrl = null;
            } else {
              _tmpDownloadUrl = _cursor.getString(_cursorIndexOfDownloadUrl);
            }
            final long _tmpSizeInBytes;
            _tmpSizeInBytes = _cursor.getLong(_cursorIndexOfSizeInBytes);
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new CourseEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpGrade,_tmpDescription,_tmpTotalLessons,_tmpEstimatedHours,_tmpImageUrl,_tmpIsDownloaded,_tmpDownloadUrl,_tmpSizeInBytes,_tmpVersion,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<CourseEntity> getCourseById(final String courseId) {
    final String _sql = "SELECT * FROM courses WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<CourseEntity>() {
      @Override
      @Nullable
      public CourseEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "grade");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfEstimatedHours = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedHours");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadUrl");
          final int _cursorIndexOfSizeInBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeInBytes");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final CourseEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final String _tmpGrade;
            _tmpGrade = _cursor.getString(_cursorIndexOfGrade);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpEstimatedHours;
            _tmpEstimatedHours = _cursor.getInt(_cursorIndexOfEstimatedHours);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadUrl;
            if (_cursor.isNull(_cursorIndexOfDownloadUrl)) {
              _tmpDownloadUrl = null;
            } else {
              _tmpDownloadUrl = _cursor.getString(_cursorIndexOfDownloadUrl);
            }
            final long _tmpSizeInBytes;
            _tmpSizeInBytes = _cursor.getLong(_cursorIndexOfSizeInBytes);
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new CourseEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpGrade,_tmpDescription,_tmpTotalLessons,_tmpEstimatedHours,_tmpImageUrl,_tmpIsDownloaded,_tmpDownloadUrl,_tmpSizeInBytes,_tmpVersion,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CourseEntity>> getCoursesBySubject(final String subject) {
    final String _sql = "SELECT * FROM courses WHERE subject = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, subject);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<CourseEntity>>() {
      @Override
      @NonNull
      public List<CourseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "grade");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfEstimatedHours = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedHours");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadUrl");
          final int _cursorIndexOfSizeInBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeInBytes");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<CourseEntity> _result = new ArrayList<CourseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CourseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final String _tmpGrade;
            _tmpGrade = _cursor.getString(_cursorIndexOfGrade);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpEstimatedHours;
            _tmpEstimatedHours = _cursor.getInt(_cursorIndexOfEstimatedHours);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadUrl;
            if (_cursor.isNull(_cursorIndexOfDownloadUrl)) {
              _tmpDownloadUrl = null;
            } else {
              _tmpDownloadUrl = _cursor.getString(_cursorIndexOfDownloadUrl);
            }
            final long _tmpSizeInBytes;
            _tmpSizeInBytes = _cursor.getLong(_cursorIndexOfSizeInBytes);
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new CourseEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpGrade,_tmpDescription,_tmpTotalLessons,_tmpEstimatedHours,_tmpImageUrl,_tmpIsDownloaded,_tmpDownloadUrl,_tmpSizeInBytes,_tmpVersion,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CourseEntity>> getDownloadedCourses() {
    final String _sql = "SELECT * FROM courses WHERE isDownloaded = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<CourseEntity>>() {
      @Override
      @NonNull
      public List<CourseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "grade");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfEstimatedHours = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedHours");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadUrl");
          final int _cursorIndexOfSizeInBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeInBytes");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<CourseEntity> _result = new ArrayList<CourseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CourseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final String _tmpGrade;
            _tmpGrade = _cursor.getString(_cursorIndexOfGrade);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpEstimatedHours;
            _tmpEstimatedHours = _cursor.getInt(_cursorIndexOfEstimatedHours);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadUrl;
            if (_cursor.isNull(_cursorIndexOfDownloadUrl)) {
              _tmpDownloadUrl = null;
            } else {
              _tmpDownloadUrl = _cursor.getString(_cursorIndexOfDownloadUrl);
            }
            final long _tmpSizeInBytes;
            _tmpSizeInBytes = _cursor.getLong(_cursorIndexOfSizeInBytes);
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new CourseEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpGrade,_tmpDescription,_tmpTotalLessons,_tmpEstimatedHours,_tmpImageUrl,_tmpIsDownloaded,_tmpDownloadUrl,_tmpSizeInBytes,_tmpVersion,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<LessonEntity>> getLessonsByCourse(final String courseId) {
    final String _sql = "SELECT * FROM lessons WHERE courseId = ? ORDER BY lessonNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lessons"}, new Callable<List<LessonEntity>>() {
      @Override
      @NonNull
      public List<LessonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonNumber");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfImageReferences = CursorUtil.getColumnIndexOrThrow(_cursor, "imageReferences");
          final int _cursorIndexOfEstimatedReadingTime = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedReadingTime");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<LessonEntity> _result = new ArrayList<LessonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LessonEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCourseId;
            _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            final int _tmpLessonNumber;
            _tmpLessonNumber = _cursor.getInt(_cursorIndexOfLessonNumber);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final List<String> _tmpImageReferences;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfImageReferences);
            _tmpImageReferences = __converters.toStringList(_tmp);
            final int _tmpEstimatedReadingTime;
            _tmpEstimatedReadingTime = _cursor.getInt(_cursorIndexOfEstimatedReadingTime);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new LessonEntity(_tmpId,_tmpCourseId,_tmpLessonNumber,_tmpTitle,_tmpMarkdownContent,_tmpImageReferences,_tmpEstimatedReadingTime,_tmpIsCompleted,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<LessonEntity> getLessonById(final String lessonId) {
    final String _sql = "SELECT * FROM lessons WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lessons"}, new Callable<LessonEntity>() {
      @Override
      @Nullable
      public LessonEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonNumber");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfImageReferences = CursorUtil.getColumnIndexOrThrow(_cursor, "imageReferences");
          final int _cursorIndexOfEstimatedReadingTime = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedReadingTime");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final LessonEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCourseId;
            _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            final int _tmpLessonNumber;
            _tmpLessonNumber = _cursor.getInt(_cursorIndexOfLessonNumber);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final List<String> _tmpImageReferences;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfImageReferences);
            _tmpImageReferences = __converters.toStringList(_tmp);
            final int _tmpEstimatedReadingTime;
            _tmpEstimatedReadingTime = _cursor.getInt(_cursorIndexOfEstimatedReadingTime);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new LessonEntity(_tmpId,_tmpCourseId,_tmpLessonNumber,_tmpTitle,_tmpMarkdownContent,_tmpImageReferences,_tmpEstimatedReadingTime,_tmpIsCompleted,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<LessonEntity> getLessonByNumber(final String courseId, final int lessonNumber) {
    final String _sql = "SELECT * FROM lessons WHERE courseId = ? AND lessonNumber = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, lessonNumber);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lessons"}, new Callable<LessonEntity>() {
      @Override
      @Nullable
      public LessonEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonNumber");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfImageReferences = CursorUtil.getColumnIndexOrThrow(_cursor, "imageReferences");
          final int _cursorIndexOfEstimatedReadingTime = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedReadingTime");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final LessonEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCourseId;
            _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            final int _tmpLessonNumber;
            _tmpLessonNumber = _cursor.getInt(_cursorIndexOfLessonNumber);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final List<String> _tmpImageReferences;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfImageReferences);
            _tmpImageReferences = __converters.toStringList(_tmp);
            final int _tmpEstimatedReadingTime;
            _tmpEstimatedReadingTime = _cursor.getInt(_cursorIndexOfEstimatedReadingTime);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new LessonEntity(_tmpId,_tmpCourseId,_tmpLessonNumber,_tmpTitle,_tmpMarkdownContent,_tmpImageReferences,_tmpEstimatedReadingTime,_tmpIsCompleted,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ExerciseEntity>> getExercisesByLesson(final String lessonId) {
    final String _sql = "SELECT * FROM exercises WHERE lessonId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exercises"}, new Callable<List<ExerciseEntity>>() {
      @Override
      @NonNull
      public List<ExerciseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExerciseEntity> _result = new ArrayList<ExerciseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExerciseEntity(_tmpId,_tmpLessonId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<ExerciseEntity> getExerciseById(final String exerciseId) {
    final String _sql = "SELECT * FROM exercises WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, exerciseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exercises"}, new Callable<ExerciseEntity>() {
      @Override
      @Nullable
      public ExerciseEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ExerciseEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ExerciseEntity(_tmpId,_tmpLessonId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ExerciseEntity>> getIncompleteExercises(final String lessonId) {
    final String _sql = "SELECT * FROM exercises WHERE lessonId = ? AND isCompleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exercises"}, new Callable<List<ExerciseEntity>>() {
      @Override
      @NonNull
      public List<ExerciseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExerciseEntity> _result = new ArrayList<ExerciseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExerciseEntity(_tmpId,_tmpLessonId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrialEntity>> getTrialsByExercise(final String exerciseId) {
    final String _sql = "SELECT * FROM trials WHERE originalExerciseId = ? ORDER BY generatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, exerciseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trials"}, new Callable<List<TrialEntity>>() {
      @Override
      @NonNull
      public List<TrialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "originalExerciseId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final List<TrialEntity> _result = new ArrayList<TrialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrialEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOriginalExerciseId;
            _tmpOriginalExerciseId = _cursor.getString(_cursorIndexOfOriginalExerciseId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            _item = new TrialEntity(_tmpId,_tmpOriginalExerciseId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpGeneratedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<TrialEntity> getTrialById(final String trialId) {
    final String _sql = "SELECT * FROM trials WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, trialId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trials"}, new Callable<TrialEntity>() {
      @Override
      @Nullable
      public TrialEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "originalExerciseId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final TrialEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOriginalExerciseId;
            _tmpOriginalExerciseId = _cursor.getString(_cursorIndexOfOriginalExerciseId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            _result = new TrialEntity(_tmpId,_tmpOriginalExerciseId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpGeneratedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrialEntity>> getIncompleteTrials(final String exerciseId) {
    final String _sql = "SELECT * FROM trials WHERE originalExerciseId = ? AND isCompleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, exerciseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trials"}, new Callable<List<TrialEntity>>() {
      @Override
      @NonNull
      public List<TrialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "originalExerciseId");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfOptions = CursorUtil.getColumnIndexOrThrow(_cursor, "options");
          final int _cursorIndexOfCorrectAnswerIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswerIndex");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfUserAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "userAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final List<TrialEntity> _result = new ArrayList<TrialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrialEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOriginalExerciseId;
            _tmpOriginalExerciseId = _cursor.getString(_cursorIndexOfOriginalExerciseId);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final List<String> _tmpOptions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfOptions);
            _tmpOptions = __converters.toStringList(_tmp);
            final int _tmpCorrectAnswerIndex;
            _tmpCorrectAnswerIndex = _cursor.getInt(_cursorIndexOfCorrectAnswerIndex);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final Integer _tmpUserAnswer;
            if (_cursor.isNull(_cursorIndexOfUserAnswer)) {
              _tmpUserAnswer = null;
            } else {
              _tmpUserAnswer = _cursor.getInt(_cursorIndexOfUserAnswer);
            }
            final Boolean _tmpIsCorrect;
            final Integer _tmp_2;
            if (_cursor.isNull(_cursorIndexOfIsCorrect)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsCorrect);
            }
            _tmpIsCorrect = _tmp_2 == null ? null : _tmp_2 != 0;
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            _item = new TrialEntity(_tmpId,_tmpOriginalExerciseId,_tmpQuestionText,_tmpOptions,_tmpCorrectAnswerIndex,_tmpExplanation,_tmpDifficulty,_tmpIsCompleted,_tmpUserAnswer,_tmpIsCorrect,_tmpGeneratedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLessonCountByCourse(final String courseId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM lessons WHERE courseId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getExerciseCountByLesson(final String lessonId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM exercises WHERE lessonId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCompletedExerciseCount(final String lessonId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM exercises WHERE lessonId = ? AND isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
