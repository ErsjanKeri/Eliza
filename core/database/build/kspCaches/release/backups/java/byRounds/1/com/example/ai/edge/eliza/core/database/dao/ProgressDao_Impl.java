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
import com.example.ai.edge.eliza.core.database.entity.AchievementEntity;
import com.example.ai.edge.eliza.core.database.entity.LearningStatsEntity;
import com.example.ai.edge.eliza.core.database.entity.LessonProgressEntity;
import com.example.ai.edge.eliza.core.database.entity.StudySessionEntity;
import com.example.ai.edge.eliza.core.database.entity.UserAnswerEntity;
import com.example.ai.edge.eliza.core.database.entity.UserProgressEntity;
import com.example.ai.edge.eliza.core.database.entity.WeeklyProgressEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class ProgressDao_Impl implements ProgressDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserProgressEntity> __insertionAdapterOfUserProgressEntity;

  private final EntityInsertionAdapter<LessonProgressEntity> __insertionAdapterOfLessonProgressEntity;

  private final EntityInsertionAdapter<UserAnswerEntity> __insertionAdapterOfUserAnswerEntity;

  private final EntityInsertionAdapter<StudySessionEntity> __insertionAdapterOfStudySessionEntity;

  private final EntityInsertionAdapter<AchievementEntity> __insertionAdapterOfAchievementEntity;

  private final EntityInsertionAdapter<LearningStatsEntity> __insertionAdapterOfLearningStatsEntity;

  private final EntityInsertionAdapter<WeeklyProgressEntity> __insertionAdapterOfWeeklyProgressEntity;

  private final EntityDeletionOrUpdateAdapter<UserProgressEntity> __deletionAdapterOfUserProgressEntity;

  private final EntityDeletionOrUpdateAdapter<LessonProgressEntity> __deletionAdapterOfLessonProgressEntity;

  private final EntityDeletionOrUpdateAdapter<UserAnswerEntity> __deletionAdapterOfUserAnswerEntity;

  private final EntityDeletionOrUpdateAdapter<StudySessionEntity> __deletionAdapterOfStudySessionEntity;

  private final EntityDeletionOrUpdateAdapter<AchievementEntity> __deletionAdapterOfAchievementEntity;

  private final EntityDeletionOrUpdateAdapter<LearningStatsEntity> __deletionAdapterOfLearningStatsEntity;

  private final EntityDeletionOrUpdateAdapter<WeeklyProgressEntity> __deletionAdapterOfWeeklyProgressEntity;

  private final EntityDeletionOrUpdateAdapter<UserProgressEntity> __updateAdapterOfUserProgressEntity;

  private final EntityDeletionOrUpdateAdapter<LessonProgressEntity> __updateAdapterOfLessonProgressEntity;

  private final EntityDeletionOrUpdateAdapter<UserAnswerEntity> __updateAdapterOfUserAnswerEntity;

  private final EntityDeletionOrUpdateAdapter<StudySessionEntity> __updateAdapterOfStudySessionEntity;

  private final EntityDeletionOrUpdateAdapter<AchievementEntity> __updateAdapterOfAchievementEntity;

  private final EntityDeletionOrUpdateAdapter<LearningStatsEntity> __updateAdapterOfLearningStatsEntity;

  private final EntityDeletionOrUpdateAdapter<WeeklyProgressEntity> __updateAdapterOfWeeklyProgressEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUserProgressByCourse;

  public ProgressDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserProgressEntity = new EntityInsertionAdapter<UserProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_progress` (`id`,`courseId`,`completedLessons`,`totalLessons`,`completedExercises`,`totalExercises`,`correctAnswers`,`totalAnswers`,`timeSpentMinutes`,`streakDays`,`lastStudiedAt`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCourseId());
        statement.bindLong(3, entity.getCompletedLessons());
        statement.bindLong(4, entity.getTotalLessons());
        statement.bindLong(5, entity.getCompletedExercises());
        statement.bindLong(6, entity.getTotalExercises());
        statement.bindLong(7, entity.getCorrectAnswers());
        statement.bindLong(8, entity.getTotalAnswers());
        statement.bindLong(9, entity.getTimeSpentMinutes());
        statement.bindLong(10, entity.getStreakDays());
        statement.bindLong(11, entity.getLastStudiedAt());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfLessonProgressEntity = new EntityInsertionAdapter<LessonProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `lesson_progress` (`id`,`lessonId`,`userId`,`isCompleted`,`completedExercises`,`totalExercises`,`timeSpentMinutes`,`firstAccessAt`,`lastAccessAt`,`completedAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getLessonId());
        statement.bindString(3, entity.getUserId());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getCompletedExercises());
        statement.bindLong(6, entity.getTotalExercises());
        statement.bindLong(7, entity.getTimeSpentMinutes());
        statement.bindLong(8, entity.getFirstAccessAt());
        statement.bindLong(9, entity.getLastAccessAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getCompletedAt());
        }
      }
    };
    this.__insertionAdapterOfUserAnswerEntity = new EntityInsertionAdapter<UserAnswerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_answers` (`id`,`exerciseId`,`trialId`,`userId`,`selectedAnswer`,`isCorrect`,`timeSpentSeconds`,`hintsUsed`,`answeredAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserAnswerEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getExerciseId());
        if (entity.getTrialId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTrialId());
        }
        statement.bindString(4, entity.getUserId());
        statement.bindLong(5, entity.getSelectedAnswer());
        final int _tmp = entity.isCorrect() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getTimeSpentSeconds());
        statement.bindLong(8, entity.getHintsUsed());
        statement.bindLong(9, entity.getAnsweredAt());
      }
    };
    this.__insertionAdapterOfStudySessionEntity = new EntityInsertionAdapter<StudySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `study_sessions` (`id`,`userId`,`courseId`,`lessonId`,`sessionType`,`durationMinutes`,`exercisesCompleted`,`correctAnswers`,`totalAnswers`,`startedAt`,`endedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudySessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        if (entity.getCourseId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCourseId());
        }
        if (entity.getLessonId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLessonId());
        }
        statement.bindString(5, entity.getSessionType());
        statement.bindLong(6, entity.getDurationMinutes());
        statement.bindLong(7, entity.getExercisesCompleted());
        statement.bindLong(8, entity.getCorrectAnswers());
        statement.bindLong(9, entity.getTotalAnswers());
        statement.bindLong(10, entity.getStartedAt());
        if (entity.getEndedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getEndedAt());
        }
      }
    };
    this.__insertionAdapterOfAchievementEntity = new EntityInsertionAdapter<AchievementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `achievements` (`id`,`title`,`description`,`iconUrl`,`requirementType`,`requirementThreshold`,`requirementSubject`,`requirementDifficulty`,`rewardPoints`,`unlockedAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AchievementEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getDescription());
        if (entity.getIconUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconUrl());
        }
        statement.bindString(5, entity.getRequirementType());
        statement.bindLong(6, entity.getRequirementThreshold());
        if (entity.getRequirementSubject() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRequirementSubject());
        }
        if (entity.getRequirementDifficulty() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRequirementDifficulty());
        }
        statement.bindLong(9, entity.getRewardPoints());
        if (entity.getUnlockedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getUnlockedAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfLearningStatsEntity = new EntityInsertionAdapter<LearningStatsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `learning_stats` (`userId`,`totalTimeMinutes`,`totalLessonsCompleted`,`totalExercisesCompleted`,`totalCorrectAnswers`,`totalQuestions`,`currentStreak`,`longestStreak`,`coursesCompleted`,`totalCourses`,`chatSessionsCount`,`imageProblemsCount`,`lastUpdated`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearningStatsEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindLong(2, entity.getTotalTimeMinutes());
        statement.bindLong(3, entity.getTotalLessonsCompleted());
        statement.bindLong(4, entity.getTotalExercisesCompleted());
        statement.bindLong(5, entity.getTotalCorrectAnswers());
        statement.bindLong(6, entity.getTotalQuestions());
        statement.bindLong(7, entity.getCurrentStreak());
        statement.bindLong(8, entity.getLongestStreak());
        statement.bindLong(9, entity.getCoursesCompleted());
        statement.bindLong(10, entity.getTotalCourses());
        statement.bindLong(11, entity.getChatSessionsCount());
        statement.bindLong(12, entity.getImageProblemsCount());
        statement.bindLong(13, entity.getLastUpdated());
      }
    };
    this.__insertionAdapterOfWeeklyProgressEntity = new EntityInsertionAdapter<WeeklyProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `weekly_progress` (`id`,`userId`,`weekStartDate`,`weekEndDate`,`minutesStudied`,`lessonsCompleted`,`exercisesCompleted`,`daysActive`,`averageAccuracy`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WeeklyProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getWeekStartDate());
        statement.bindLong(4, entity.getWeekEndDate());
        statement.bindLong(5, entity.getMinutesStudied());
        statement.bindLong(6, entity.getLessonsCompleted());
        statement.bindLong(7, entity.getExercisesCompleted());
        statement.bindLong(8, entity.getDaysActive());
        statement.bindDouble(9, entity.getAverageAccuracy());
      }
    };
    this.__deletionAdapterOfUserProgressEntity = new EntityDeletionOrUpdateAdapter<UserProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `user_progress` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProgressEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfLessonProgressEntity = new EntityDeletionOrUpdateAdapter<LessonProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `lesson_progress` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonProgressEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfUserAnswerEntity = new EntityDeletionOrUpdateAdapter<UserAnswerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `user_answers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserAnswerEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfStudySessionEntity = new EntityDeletionOrUpdateAdapter<StudySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `study_sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudySessionEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfAchievementEntity = new EntityDeletionOrUpdateAdapter<AchievementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `achievements` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AchievementEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfLearningStatsEntity = new EntityDeletionOrUpdateAdapter<LearningStatsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `learning_stats` WHERE `userId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearningStatsEntity entity) {
        statement.bindString(1, entity.getUserId());
      }
    };
    this.__deletionAdapterOfWeeklyProgressEntity = new EntityDeletionOrUpdateAdapter<WeeklyProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `weekly_progress` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WeeklyProgressEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfUserProgressEntity = new EntityDeletionOrUpdateAdapter<UserProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `user_progress` SET `id` = ?,`courseId` = ?,`completedLessons` = ?,`totalLessons` = ?,`completedExercises` = ?,`totalExercises` = ?,`correctAnswers` = ?,`totalAnswers` = ?,`timeSpentMinutes` = ?,`streakDays` = ?,`lastStudiedAt` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCourseId());
        statement.bindLong(3, entity.getCompletedLessons());
        statement.bindLong(4, entity.getTotalLessons());
        statement.bindLong(5, entity.getCompletedExercises());
        statement.bindLong(6, entity.getTotalExercises());
        statement.bindLong(7, entity.getCorrectAnswers());
        statement.bindLong(8, entity.getTotalAnswers());
        statement.bindLong(9, entity.getTimeSpentMinutes());
        statement.bindLong(10, entity.getStreakDays());
        statement.bindLong(11, entity.getLastStudiedAt());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
        statement.bindString(14, entity.getId());
      }
    };
    this.__updateAdapterOfLessonProgressEntity = new EntityDeletionOrUpdateAdapter<LessonProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `lesson_progress` SET `id` = ?,`lessonId` = ?,`userId` = ?,`isCompleted` = ?,`completedExercises` = ?,`totalExercises` = ?,`timeSpentMinutes` = ?,`firstAccessAt` = ?,`lastAccessAt` = ?,`completedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LessonProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getLessonId());
        statement.bindString(3, entity.getUserId());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getCompletedExercises());
        statement.bindLong(6, entity.getTotalExercises());
        statement.bindLong(7, entity.getTimeSpentMinutes());
        statement.bindLong(8, entity.getFirstAccessAt());
        statement.bindLong(9, entity.getLastAccessAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getCompletedAt());
        }
        statement.bindString(11, entity.getId());
      }
    };
    this.__updateAdapterOfUserAnswerEntity = new EntityDeletionOrUpdateAdapter<UserAnswerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `user_answers` SET `id` = ?,`exerciseId` = ?,`trialId` = ?,`userId` = ?,`selectedAnswer` = ?,`isCorrect` = ?,`timeSpentSeconds` = ?,`hintsUsed` = ?,`answeredAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserAnswerEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getExerciseId());
        if (entity.getTrialId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTrialId());
        }
        statement.bindString(4, entity.getUserId());
        statement.bindLong(5, entity.getSelectedAnswer());
        final int _tmp = entity.isCorrect() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getTimeSpentSeconds());
        statement.bindLong(8, entity.getHintsUsed());
        statement.bindLong(9, entity.getAnsweredAt());
        statement.bindString(10, entity.getId());
      }
    };
    this.__updateAdapterOfStudySessionEntity = new EntityDeletionOrUpdateAdapter<StudySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `study_sessions` SET `id` = ?,`userId` = ?,`courseId` = ?,`lessonId` = ?,`sessionType` = ?,`durationMinutes` = ?,`exercisesCompleted` = ?,`correctAnswers` = ?,`totalAnswers` = ?,`startedAt` = ?,`endedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudySessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        if (entity.getCourseId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCourseId());
        }
        if (entity.getLessonId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLessonId());
        }
        statement.bindString(5, entity.getSessionType());
        statement.bindLong(6, entity.getDurationMinutes());
        statement.bindLong(7, entity.getExercisesCompleted());
        statement.bindLong(8, entity.getCorrectAnswers());
        statement.bindLong(9, entity.getTotalAnswers());
        statement.bindLong(10, entity.getStartedAt());
        if (entity.getEndedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getEndedAt());
        }
        statement.bindString(12, entity.getId());
      }
    };
    this.__updateAdapterOfAchievementEntity = new EntityDeletionOrUpdateAdapter<AchievementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `achievements` SET `id` = ?,`title` = ?,`description` = ?,`iconUrl` = ?,`requirementType` = ?,`requirementThreshold` = ?,`requirementSubject` = ?,`requirementDifficulty` = ?,`rewardPoints` = ?,`unlockedAt` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AchievementEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getDescription());
        if (entity.getIconUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconUrl());
        }
        statement.bindString(5, entity.getRequirementType());
        statement.bindLong(6, entity.getRequirementThreshold());
        if (entity.getRequirementSubject() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRequirementSubject());
        }
        if (entity.getRequirementDifficulty() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRequirementDifficulty());
        }
        statement.bindLong(9, entity.getRewardPoints());
        if (entity.getUnlockedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getUnlockedAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindString(12, entity.getId());
      }
    };
    this.__updateAdapterOfLearningStatsEntity = new EntityDeletionOrUpdateAdapter<LearningStatsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `learning_stats` SET `userId` = ?,`totalTimeMinutes` = ?,`totalLessonsCompleted` = ?,`totalExercisesCompleted` = ?,`totalCorrectAnswers` = ?,`totalQuestions` = ?,`currentStreak` = ?,`longestStreak` = ?,`coursesCompleted` = ?,`totalCourses` = ?,`chatSessionsCount` = ?,`imageProblemsCount` = ?,`lastUpdated` = ? WHERE `userId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearningStatsEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindLong(2, entity.getTotalTimeMinutes());
        statement.bindLong(3, entity.getTotalLessonsCompleted());
        statement.bindLong(4, entity.getTotalExercisesCompleted());
        statement.bindLong(5, entity.getTotalCorrectAnswers());
        statement.bindLong(6, entity.getTotalQuestions());
        statement.bindLong(7, entity.getCurrentStreak());
        statement.bindLong(8, entity.getLongestStreak());
        statement.bindLong(9, entity.getCoursesCompleted());
        statement.bindLong(10, entity.getTotalCourses());
        statement.bindLong(11, entity.getChatSessionsCount());
        statement.bindLong(12, entity.getImageProblemsCount());
        statement.bindLong(13, entity.getLastUpdated());
        statement.bindString(14, entity.getUserId());
      }
    };
    this.__updateAdapterOfWeeklyProgressEntity = new EntityDeletionOrUpdateAdapter<WeeklyProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `weekly_progress` SET `id` = ?,`userId` = ?,`weekStartDate` = ?,`weekEndDate` = ?,`minutesStudied` = ?,`lessonsCompleted` = ?,`exercisesCompleted` = ?,`daysActive` = ?,`averageAccuracy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WeeklyProgressEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getWeekStartDate());
        statement.bindLong(4, entity.getWeekEndDate());
        statement.bindLong(5, entity.getMinutesStudied());
        statement.bindLong(6, entity.getLessonsCompleted());
        statement.bindLong(7, entity.getExercisesCompleted());
        statement.bindLong(8, entity.getDaysActive());
        statement.bindDouble(9, entity.getAverageAccuracy());
        statement.bindString(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteUserProgressByCourse = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_progress WHERE courseId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertUserProgress(final UserProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserProgressEntity.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLessonProgress(final LessonProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLessonProgressEntity.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertUserAnswer(final UserAnswerEntity answer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserAnswerEntity.insert(answer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStudySession(final StudySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStudySessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAchievement(final AchievementEntity achievement,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAchievementEntity.insert(achievement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLearningStats(final LearningStatsEntity stats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLearningStatsEntity.insert(stats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertWeeklyProgress(final WeeklyProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWeeklyProgressEntity.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUserProgress(final UserProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfUserProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLessonProgress(final LessonProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLessonProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUserAnswer(final UserAnswerEntity answer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfUserAnswerEntity.handle(answer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStudySession(final StudySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfStudySessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAchievement(final AchievementEntity achievement,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAchievementEntity.handle(achievement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLearningStats(final LearningStatsEntity stats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLearningStatsEntity.handle(stats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteWeeklyProgress(final WeeklyProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWeeklyProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUserProgress(final UserProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLessonProgress(final LessonProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLessonProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUserAnswer(final UserAnswerEntity answer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserAnswerEntity.handle(answer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStudySession(final StudySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfStudySessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAchievement(final AchievementEntity achievement,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAchievementEntity.handle(achievement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLearningStats(final LearningStatsEntity stats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLearningStatsEntity.handle(stats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateWeeklyProgress(final WeeklyProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfWeeklyProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUserProgressByCourse(final String courseId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUserProgressByCourse.acquire();
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
          __preparedStmtOfDeleteUserProgressByCourse.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserProgressEntity> getUserProgressByCourse(final String courseId) {
    final String _sql = "SELECT * FROM user_progress WHERE courseId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_progress"}, new Callable<UserProgressEntity>() {
      @Override
      @Nullable
      public UserProgressEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfCompletedLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "completedLessons");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfCompletedExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "completedExercises");
          final int _cursorIndexOfTotalExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercises");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfTimeSpentMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentMinutes");
          final int _cursorIndexOfStreakDays = CursorUtil.getColumnIndexOrThrow(_cursor, "streakDays");
          final int _cursorIndexOfLastStudiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStudiedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final UserProgressEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCourseId;
            _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            final int _tmpCompletedLessons;
            _tmpCompletedLessons = _cursor.getInt(_cursorIndexOfCompletedLessons);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpCompletedExercises;
            _tmpCompletedExercises = _cursor.getInt(_cursorIndexOfCompletedExercises);
            final int _tmpTotalExercises;
            _tmpTotalExercises = _cursor.getInt(_cursorIndexOfTotalExercises);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpTimeSpentMinutes;
            _tmpTimeSpentMinutes = _cursor.getLong(_cursorIndexOfTimeSpentMinutes);
            final int _tmpStreakDays;
            _tmpStreakDays = _cursor.getInt(_cursorIndexOfStreakDays);
            final long _tmpLastStudiedAt;
            _tmpLastStudiedAt = _cursor.getLong(_cursorIndexOfLastStudiedAt);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new UserProgressEntity(_tmpId,_tmpCourseId,_tmpCompletedLessons,_tmpTotalLessons,_tmpCompletedExercises,_tmpTotalExercises,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpTimeSpentMinutes,_tmpStreakDays,_tmpLastStudiedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<UserProgressEntity>> getAllUserProgress() {
    final String _sql = "SELECT * FROM user_progress ORDER BY lastStudiedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_progress"}, new Callable<List<UserProgressEntity>>() {
      @Override
      @NonNull
      public List<UserProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfCompletedLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "completedLessons");
          final int _cursorIndexOfTotalLessons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessons");
          final int _cursorIndexOfCompletedExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "completedExercises");
          final int _cursorIndexOfTotalExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercises");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfTimeSpentMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentMinutes");
          final int _cursorIndexOfStreakDays = CursorUtil.getColumnIndexOrThrow(_cursor, "streakDays");
          final int _cursorIndexOfLastStudiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStudiedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<UserProgressEntity> _result = new ArrayList<UserProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserProgressEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCourseId;
            _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            final int _tmpCompletedLessons;
            _tmpCompletedLessons = _cursor.getInt(_cursorIndexOfCompletedLessons);
            final int _tmpTotalLessons;
            _tmpTotalLessons = _cursor.getInt(_cursorIndexOfTotalLessons);
            final int _tmpCompletedExercises;
            _tmpCompletedExercises = _cursor.getInt(_cursorIndexOfCompletedExercises);
            final int _tmpTotalExercises;
            _tmpTotalExercises = _cursor.getInt(_cursorIndexOfTotalExercises);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpTimeSpentMinutes;
            _tmpTimeSpentMinutes = _cursor.getLong(_cursorIndexOfTimeSpentMinutes);
            final int _tmpStreakDays;
            _tmpStreakDays = _cursor.getInt(_cursorIndexOfStreakDays);
            final long _tmpLastStudiedAt;
            _tmpLastStudiedAt = _cursor.getLong(_cursorIndexOfLastStudiedAt);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new UserProgressEntity(_tmpId,_tmpCourseId,_tmpCompletedLessons,_tmpTotalLessons,_tmpCompletedExercises,_tmpTotalExercises,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpTimeSpentMinutes,_tmpStreakDays,_tmpLastStudiedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<LessonProgressEntity> getLessonProgress(final String lessonId, final String userId) {
    final String _sql = "SELECT * FROM lesson_progress WHERE lessonId = ? AND userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lessonId);
    _argIndex = 2;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lesson_progress"}, new Callable<LessonProgressEntity>() {
      @Override
      @Nullable
      public LessonProgressEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "completedExercises");
          final int _cursorIndexOfTotalExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercises");
          final int _cursorIndexOfTimeSpentMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentMinutes");
          final int _cursorIndexOfFirstAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "firstAccessAt");
          final int _cursorIndexOfLastAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAccessAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final LessonProgressEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpCompletedExercises;
            _tmpCompletedExercises = _cursor.getInt(_cursorIndexOfCompletedExercises);
            final int _tmpTotalExercises;
            _tmpTotalExercises = _cursor.getInt(_cursorIndexOfTotalExercises);
            final long _tmpTimeSpentMinutes;
            _tmpTimeSpentMinutes = _cursor.getLong(_cursorIndexOfTimeSpentMinutes);
            final long _tmpFirstAccessAt;
            _tmpFirstAccessAt = _cursor.getLong(_cursorIndexOfFirstAccessAt);
            final long _tmpLastAccessAt;
            _tmpLastAccessAt = _cursor.getLong(_cursorIndexOfLastAccessAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _result = new LessonProgressEntity(_tmpId,_tmpLessonId,_tmpUserId,_tmpIsCompleted,_tmpCompletedExercises,_tmpTotalExercises,_tmpTimeSpentMinutes,_tmpFirstAccessAt,_tmpLastAccessAt,_tmpCompletedAt);
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
  public Flow<List<LessonProgressEntity>> getUserLessonProgress(final String userId) {
    final String _sql = "SELECT * FROM lesson_progress WHERE userId = ? ORDER BY lastAccessAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lesson_progress"}, new Callable<List<LessonProgressEntity>>() {
      @Override
      @NonNull
      public List<LessonProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "completedExercises");
          final int _cursorIndexOfTotalExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercises");
          final int _cursorIndexOfTimeSpentMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentMinutes");
          final int _cursorIndexOfFirstAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "firstAccessAt");
          final int _cursorIndexOfLastAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAccessAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<LessonProgressEntity> _result = new ArrayList<LessonProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LessonProgressEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpCompletedExercises;
            _tmpCompletedExercises = _cursor.getInt(_cursorIndexOfCompletedExercises);
            final int _tmpTotalExercises;
            _tmpTotalExercises = _cursor.getInt(_cursorIndexOfTotalExercises);
            final long _tmpTimeSpentMinutes;
            _tmpTimeSpentMinutes = _cursor.getLong(_cursorIndexOfTimeSpentMinutes);
            final long _tmpFirstAccessAt;
            _tmpFirstAccessAt = _cursor.getLong(_cursorIndexOfFirstAccessAt);
            final long _tmpLastAccessAt;
            _tmpLastAccessAt = _cursor.getLong(_cursorIndexOfLastAccessAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new LessonProgressEntity(_tmpId,_tmpLessonId,_tmpUserId,_tmpIsCompleted,_tmpCompletedExercises,_tmpTotalExercises,_tmpTimeSpentMinutes,_tmpFirstAccessAt,_tmpLastAccessAt,_tmpCompletedAt);
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
  public Flow<List<LessonProgressEntity>> getCompletedLessons(final String userId) {
    final String _sql = "SELECT * FROM lesson_progress WHERE userId = ? AND isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lesson_progress"}, new Callable<List<LessonProgressEntity>>() {
      @Override
      @NonNull
      public List<LessonProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "completedExercises");
          final int _cursorIndexOfTotalExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercises");
          final int _cursorIndexOfTimeSpentMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentMinutes");
          final int _cursorIndexOfFirstAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "firstAccessAt");
          final int _cursorIndexOfLastAccessAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAccessAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<LessonProgressEntity> _result = new ArrayList<LessonProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LessonProgressEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpLessonId;
            _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpCompletedExercises;
            _tmpCompletedExercises = _cursor.getInt(_cursorIndexOfCompletedExercises);
            final int _tmpTotalExercises;
            _tmpTotalExercises = _cursor.getInt(_cursorIndexOfTotalExercises);
            final long _tmpTimeSpentMinutes;
            _tmpTimeSpentMinutes = _cursor.getLong(_cursorIndexOfTimeSpentMinutes);
            final long _tmpFirstAccessAt;
            _tmpFirstAccessAt = _cursor.getLong(_cursorIndexOfFirstAccessAt);
            final long _tmpLastAccessAt;
            _tmpLastAccessAt = _cursor.getLong(_cursorIndexOfLastAccessAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new LessonProgressEntity(_tmpId,_tmpLessonId,_tmpUserId,_tmpIsCompleted,_tmpCompletedExercises,_tmpTotalExercises,_tmpTimeSpentMinutes,_tmpFirstAccessAt,_tmpLastAccessAt,_tmpCompletedAt);
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
  public Flow<List<UserAnswerEntity>> getUserAnswersByExercise(final String exerciseId,
      final String userId) {
    final String _sql = "SELECT * FROM user_answers WHERE exerciseId = ? AND userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, exerciseId);
    _argIndex = 2;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_answers"}, new Callable<List<UserAnswerEntity>>() {
      @Override
      @NonNull
      public List<UserAnswerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseId");
          final int _cursorIndexOfTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "trialId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfSelectedAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfTimeSpentSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentSeconds");
          final int _cursorIndexOfHintsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "hintsUsed");
          final int _cursorIndexOfAnsweredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "answeredAt");
          final List<UserAnswerEntity> _result = new ArrayList<UserAnswerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserAnswerEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpExerciseId;
            _tmpExerciseId = _cursor.getString(_cursorIndexOfExerciseId);
            final String _tmpTrialId;
            if (_cursor.isNull(_cursorIndexOfTrialId)) {
              _tmpTrialId = null;
            } else {
              _tmpTrialId = _cursor.getString(_cursorIndexOfTrialId);
            }
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpSelectedAnswer;
            _tmpSelectedAnswer = _cursor.getInt(_cursorIndexOfSelectedAnswer);
            final boolean _tmpIsCorrect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCorrect);
            _tmpIsCorrect = _tmp != 0;
            final long _tmpTimeSpentSeconds;
            _tmpTimeSpentSeconds = _cursor.getLong(_cursorIndexOfTimeSpentSeconds);
            final int _tmpHintsUsed;
            _tmpHintsUsed = _cursor.getInt(_cursorIndexOfHintsUsed);
            final long _tmpAnsweredAt;
            _tmpAnsweredAt = _cursor.getLong(_cursorIndexOfAnsweredAt);
            _item = new UserAnswerEntity(_tmpId,_tmpExerciseId,_tmpTrialId,_tmpUserId,_tmpSelectedAnswer,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpHintsUsed,_tmpAnsweredAt);
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
  public Flow<List<UserAnswerEntity>> getAllUserAnswers(final String userId) {
    final String _sql = "SELECT * FROM user_answers WHERE userId = ? ORDER BY answeredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_answers"}, new Callable<List<UserAnswerEntity>>() {
      @Override
      @NonNull
      public List<UserAnswerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseId");
          final int _cursorIndexOfTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "trialId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfSelectedAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfTimeSpentSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentSeconds");
          final int _cursorIndexOfHintsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "hintsUsed");
          final int _cursorIndexOfAnsweredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "answeredAt");
          final List<UserAnswerEntity> _result = new ArrayList<UserAnswerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserAnswerEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpExerciseId;
            _tmpExerciseId = _cursor.getString(_cursorIndexOfExerciseId);
            final String _tmpTrialId;
            if (_cursor.isNull(_cursorIndexOfTrialId)) {
              _tmpTrialId = null;
            } else {
              _tmpTrialId = _cursor.getString(_cursorIndexOfTrialId);
            }
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpSelectedAnswer;
            _tmpSelectedAnswer = _cursor.getInt(_cursorIndexOfSelectedAnswer);
            final boolean _tmpIsCorrect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCorrect);
            _tmpIsCorrect = _tmp != 0;
            final long _tmpTimeSpentSeconds;
            _tmpTimeSpentSeconds = _cursor.getLong(_cursorIndexOfTimeSpentSeconds);
            final int _tmpHintsUsed;
            _tmpHintsUsed = _cursor.getInt(_cursorIndexOfHintsUsed);
            final long _tmpAnsweredAt;
            _tmpAnsweredAt = _cursor.getLong(_cursorIndexOfAnsweredAt);
            _item = new UserAnswerEntity(_tmpId,_tmpExerciseId,_tmpTrialId,_tmpUserId,_tmpSelectedAnswer,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpHintsUsed,_tmpAnsweredAt);
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
  public Flow<List<UserAnswerEntity>> getCorrectAnswers(final String userId) {
    final String _sql = "SELECT * FROM user_answers WHERE userId = ? AND isCorrect = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_answers"}, new Callable<List<UserAnswerEntity>>() {
      @Override
      @NonNull
      public List<UserAnswerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseId");
          final int _cursorIndexOfTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "trialId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfSelectedAnswer = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAnswer");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final int _cursorIndexOfTimeSpentSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSpentSeconds");
          final int _cursorIndexOfHintsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "hintsUsed");
          final int _cursorIndexOfAnsweredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "answeredAt");
          final List<UserAnswerEntity> _result = new ArrayList<UserAnswerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserAnswerEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpExerciseId;
            _tmpExerciseId = _cursor.getString(_cursorIndexOfExerciseId);
            final String _tmpTrialId;
            if (_cursor.isNull(_cursorIndexOfTrialId)) {
              _tmpTrialId = null;
            } else {
              _tmpTrialId = _cursor.getString(_cursorIndexOfTrialId);
            }
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpSelectedAnswer;
            _tmpSelectedAnswer = _cursor.getInt(_cursorIndexOfSelectedAnswer);
            final boolean _tmpIsCorrect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCorrect);
            _tmpIsCorrect = _tmp != 0;
            final long _tmpTimeSpentSeconds;
            _tmpTimeSpentSeconds = _cursor.getLong(_cursorIndexOfTimeSpentSeconds);
            final int _tmpHintsUsed;
            _tmpHintsUsed = _cursor.getInt(_cursorIndexOfHintsUsed);
            final long _tmpAnsweredAt;
            _tmpAnsweredAt = _cursor.getLong(_cursorIndexOfAnsweredAt);
            _item = new UserAnswerEntity(_tmpId,_tmpExerciseId,_tmpTrialId,_tmpUserId,_tmpSelectedAnswer,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpHintsUsed,_tmpAnsweredAt);
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
  public Flow<List<StudySessionEntity>> getStudySessionsByUser(final String userId) {
    final String _sql = "SELECT * FROM study_sessions WHERE userId = ? ORDER BY startedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"study_sessions"}, new Callable<List<StudySessionEntity>>() {
      @Override
      @NonNull
      public List<StudySessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final List<StudySessionEntity> _result = new ArrayList<StudySessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudySessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpCourseId;
            if (_cursor.isNull(_cursorIndexOfCourseId)) {
              _tmpCourseId = null;
            } else {
              _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            }
            final String _tmpLessonId;
            if (_cursor.isNull(_cursorIndexOfLessonId)) {
              _tmpLessonId = null;
            } else {
              _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            }
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final long _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getLong(_cursorIndexOfDurationMinutes);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            _item = new StudySessionEntity(_tmpId,_tmpUserId,_tmpCourseId,_tmpLessonId,_tmpSessionType,_tmpDurationMinutes,_tmpExercisesCompleted,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpStartedAt,_tmpEndedAt);
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
  public Flow<List<StudySessionEntity>> getStudySessionsByCourse(final String userId,
      final String courseId) {
    final String _sql = "SELECT * FROM study_sessions WHERE userId = ? AND courseId = ? ORDER BY startedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"study_sessions"}, new Callable<List<StudySessionEntity>>() {
      @Override
      @NonNull
      public List<StudySessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final List<StudySessionEntity> _result = new ArrayList<StudySessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudySessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpCourseId;
            if (_cursor.isNull(_cursorIndexOfCourseId)) {
              _tmpCourseId = null;
            } else {
              _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            }
            final String _tmpLessonId;
            if (_cursor.isNull(_cursorIndexOfLessonId)) {
              _tmpLessonId = null;
            } else {
              _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            }
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final long _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getLong(_cursorIndexOfDurationMinutes);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            _item = new StudySessionEntity(_tmpId,_tmpUserId,_tmpCourseId,_tmpLessonId,_tmpSessionType,_tmpDurationMinutes,_tmpExercisesCompleted,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpStartedAt,_tmpEndedAt);
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
  public Flow<List<StudySessionEntity>> getStudySessionsByType(final String userId,
      final String sessionType) {
    final String _sql = "SELECT * FROM study_sessions WHERE userId = ? AND sessionType = ? ORDER BY startedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, sessionType);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"study_sessions"}, new Callable<List<StudySessionEntity>>() {
      @Override
      @NonNull
      public List<StudySessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final List<StudySessionEntity> _result = new ArrayList<StudySessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudySessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpCourseId;
            if (_cursor.isNull(_cursorIndexOfCourseId)) {
              _tmpCourseId = null;
            } else {
              _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            }
            final String _tmpLessonId;
            if (_cursor.isNull(_cursorIndexOfLessonId)) {
              _tmpLessonId = null;
            } else {
              _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            }
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final long _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getLong(_cursorIndexOfDurationMinutes);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            _item = new StudySessionEntity(_tmpId,_tmpUserId,_tmpCourseId,_tmpLessonId,_tmpSessionType,_tmpDurationMinutes,_tmpExercisesCompleted,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpStartedAt,_tmpEndedAt);
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
  public Flow<StudySessionEntity> getActiveStudySession(final String userId) {
    final String _sql = "SELECT * FROM study_sessions WHERE userId = ? AND endedAt IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"study_sessions"}, new Callable<StudySessionEntity>() {
      @Override
      @Nullable
      public StudySessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "correctAnswers");
          final int _cursorIndexOfTotalAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAnswers");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final StudySessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpCourseId;
            if (_cursor.isNull(_cursorIndexOfCourseId)) {
              _tmpCourseId = null;
            } else {
              _tmpCourseId = _cursor.getString(_cursorIndexOfCourseId);
            }
            final String _tmpLessonId;
            if (_cursor.isNull(_cursorIndexOfLessonId)) {
              _tmpLessonId = null;
            } else {
              _tmpLessonId = _cursor.getString(_cursorIndexOfLessonId);
            }
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final long _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getLong(_cursorIndexOfDurationMinutes);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpCorrectAnswers;
            _tmpCorrectAnswers = _cursor.getInt(_cursorIndexOfCorrectAnswers);
            final int _tmpTotalAnswers;
            _tmpTotalAnswers = _cursor.getInt(_cursorIndexOfTotalAnswers);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            _result = new StudySessionEntity(_tmpId,_tmpUserId,_tmpCourseId,_tmpLessonId,_tmpSessionType,_tmpDurationMinutes,_tmpExercisesCompleted,_tmpCorrectAnswers,_tmpTotalAnswers,_tmpStartedAt,_tmpEndedAt);
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
  public Flow<List<AchievementEntity>> getAllAchievements() {
    final String _sql = "SELECT * FROM achievements ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"achievements"}, new Callable<List<AchievementEntity>>() {
      @Override
      @NonNull
      public List<AchievementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfRequirementType = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementType");
          final int _cursorIndexOfRequirementThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementThreshold");
          final int _cursorIndexOfRequirementSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementSubject");
          final int _cursorIndexOfRequirementDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementDifficulty");
          final int _cursorIndexOfRewardPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardPoints");
          final int _cursorIndexOfUnlockedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AchievementEntity> _result = new ArrayList<AchievementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AchievementEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpRequirementType;
            _tmpRequirementType = _cursor.getString(_cursorIndexOfRequirementType);
            final int _tmpRequirementThreshold;
            _tmpRequirementThreshold = _cursor.getInt(_cursorIndexOfRequirementThreshold);
            final String _tmpRequirementSubject;
            if (_cursor.isNull(_cursorIndexOfRequirementSubject)) {
              _tmpRequirementSubject = null;
            } else {
              _tmpRequirementSubject = _cursor.getString(_cursorIndexOfRequirementSubject);
            }
            final String _tmpRequirementDifficulty;
            if (_cursor.isNull(_cursorIndexOfRequirementDifficulty)) {
              _tmpRequirementDifficulty = null;
            } else {
              _tmpRequirementDifficulty = _cursor.getString(_cursorIndexOfRequirementDifficulty);
            }
            final int _tmpRewardPoints;
            _tmpRewardPoints = _cursor.getInt(_cursorIndexOfRewardPoints);
            final Long _tmpUnlockedAt;
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null;
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpIconUrl,_tmpRequirementType,_tmpRequirementThreshold,_tmpRequirementSubject,_tmpRequirementDifficulty,_tmpRewardPoints,_tmpUnlockedAt,_tmpCreatedAt);
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
  public Flow<List<AchievementEntity>> getUnlockedAchievements() {
    final String _sql = "SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"achievements"}, new Callable<List<AchievementEntity>>() {
      @Override
      @NonNull
      public List<AchievementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfRequirementType = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementType");
          final int _cursorIndexOfRequirementThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementThreshold");
          final int _cursorIndexOfRequirementSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementSubject");
          final int _cursorIndexOfRequirementDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementDifficulty");
          final int _cursorIndexOfRewardPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardPoints");
          final int _cursorIndexOfUnlockedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AchievementEntity> _result = new ArrayList<AchievementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AchievementEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpRequirementType;
            _tmpRequirementType = _cursor.getString(_cursorIndexOfRequirementType);
            final int _tmpRequirementThreshold;
            _tmpRequirementThreshold = _cursor.getInt(_cursorIndexOfRequirementThreshold);
            final String _tmpRequirementSubject;
            if (_cursor.isNull(_cursorIndexOfRequirementSubject)) {
              _tmpRequirementSubject = null;
            } else {
              _tmpRequirementSubject = _cursor.getString(_cursorIndexOfRequirementSubject);
            }
            final String _tmpRequirementDifficulty;
            if (_cursor.isNull(_cursorIndexOfRequirementDifficulty)) {
              _tmpRequirementDifficulty = null;
            } else {
              _tmpRequirementDifficulty = _cursor.getString(_cursorIndexOfRequirementDifficulty);
            }
            final int _tmpRewardPoints;
            _tmpRewardPoints = _cursor.getInt(_cursorIndexOfRewardPoints);
            final Long _tmpUnlockedAt;
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null;
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpIconUrl,_tmpRequirementType,_tmpRequirementThreshold,_tmpRequirementSubject,_tmpRequirementDifficulty,_tmpRewardPoints,_tmpUnlockedAt,_tmpCreatedAt);
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
  public Flow<List<AchievementEntity>> getLockedAchievements() {
    final String _sql = "SELECT * FROM achievements WHERE unlockedAt IS NULL ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"achievements"}, new Callable<List<AchievementEntity>>() {
      @Override
      @NonNull
      public List<AchievementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfRequirementType = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementType");
          final int _cursorIndexOfRequirementThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementThreshold");
          final int _cursorIndexOfRequirementSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementSubject");
          final int _cursorIndexOfRequirementDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "requirementDifficulty");
          final int _cursorIndexOfRewardPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardPoints");
          final int _cursorIndexOfUnlockedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AchievementEntity> _result = new ArrayList<AchievementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AchievementEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpRequirementType;
            _tmpRequirementType = _cursor.getString(_cursorIndexOfRequirementType);
            final int _tmpRequirementThreshold;
            _tmpRequirementThreshold = _cursor.getInt(_cursorIndexOfRequirementThreshold);
            final String _tmpRequirementSubject;
            if (_cursor.isNull(_cursorIndexOfRequirementSubject)) {
              _tmpRequirementSubject = null;
            } else {
              _tmpRequirementSubject = _cursor.getString(_cursorIndexOfRequirementSubject);
            }
            final String _tmpRequirementDifficulty;
            if (_cursor.isNull(_cursorIndexOfRequirementDifficulty)) {
              _tmpRequirementDifficulty = null;
            } else {
              _tmpRequirementDifficulty = _cursor.getString(_cursorIndexOfRequirementDifficulty);
            }
            final int _tmpRewardPoints;
            _tmpRewardPoints = _cursor.getInt(_cursorIndexOfRewardPoints);
            final Long _tmpUnlockedAt;
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null;
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpIconUrl,_tmpRequirementType,_tmpRequirementThreshold,_tmpRequirementSubject,_tmpRequirementDifficulty,_tmpRewardPoints,_tmpUnlockedAt,_tmpCreatedAt);
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
  public Flow<LearningStatsEntity> getLearningStats(final String userId) {
    final String _sql = "SELECT * FROM learning_stats WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"learning_stats"}, new Callable<LearningStatsEntity>() {
      @Override
      @Nullable
      public LearningStatsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTotalTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTimeMinutes");
          final int _cursorIndexOfTotalLessonsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLessonsCompleted");
          final int _cursorIndexOfTotalExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "totalExercisesCompleted");
          final int _cursorIndexOfTotalCorrectAnswers = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCorrectAnswers");
          final int _cursorIndexOfTotalQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "totalQuestions");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfLongestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "longestStreak");
          final int _cursorIndexOfCoursesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "coursesCompleted");
          final int _cursorIndexOfTotalCourses = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCourses");
          final int _cursorIndexOfChatSessionsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "chatSessionsCount");
          final int _cursorIndexOfImageProblemsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "imageProblemsCount");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final LearningStatsEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpTotalTimeMinutes;
            _tmpTotalTimeMinutes = _cursor.getLong(_cursorIndexOfTotalTimeMinutes);
            final int _tmpTotalLessonsCompleted;
            _tmpTotalLessonsCompleted = _cursor.getInt(_cursorIndexOfTotalLessonsCompleted);
            final int _tmpTotalExercisesCompleted;
            _tmpTotalExercisesCompleted = _cursor.getInt(_cursorIndexOfTotalExercisesCompleted);
            final int _tmpTotalCorrectAnswers;
            _tmpTotalCorrectAnswers = _cursor.getInt(_cursorIndexOfTotalCorrectAnswers);
            final int _tmpTotalQuestions;
            _tmpTotalQuestions = _cursor.getInt(_cursorIndexOfTotalQuestions);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpLongestStreak;
            _tmpLongestStreak = _cursor.getInt(_cursorIndexOfLongestStreak);
            final int _tmpCoursesCompleted;
            _tmpCoursesCompleted = _cursor.getInt(_cursorIndexOfCoursesCompleted);
            final int _tmpTotalCourses;
            _tmpTotalCourses = _cursor.getInt(_cursorIndexOfTotalCourses);
            final int _tmpChatSessionsCount;
            _tmpChatSessionsCount = _cursor.getInt(_cursorIndexOfChatSessionsCount);
            final int _tmpImageProblemsCount;
            _tmpImageProblemsCount = _cursor.getInt(_cursorIndexOfImageProblemsCount);
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            _result = new LearningStatsEntity(_tmpUserId,_tmpTotalTimeMinutes,_tmpTotalLessonsCompleted,_tmpTotalExercisesCompleted,_tmpTotalCorrectAnswers,_tmpTotalQuestions,_tmpCurrentStreak,_tmpLongestStreak,_tmpCoursesCompleted,_tmpTotalCourses,_tmpChatSessionsCount,_tmpImageProblemsCount,_tmpLastUpdated);
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
  public Flow<List<WeeklyProgressEntity>> getWeeklyProgress(final String userId) {
    final String _sql = "SELECT * FROM weekly_progress WHERE userId = ? ORDER BY weekStartDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"weekly_progress"}, new Callable<List<WeeklyProgressEntity>>() {
      @Override
      @NonNull
      public List<WeeklyProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfWeekStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekStartDate");
          final int _cursorIndexOfWeekEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekEndDate");
          final int _cursorIndexOfMinutesStudied = CursorUtil.getColumnIndexOrThrow(_cursor, "minutesStudied");
          final int _cursorIndexOfLessonsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonsCompleted");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfDaysActive = CursorUtil.getColumnIndexOrThrow(_cursor, "daysActive");
          final int _cursorIndexOfAverageAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "averageAccuracy");
          final List<WeeklyProgressEntity> _result = new ArrayList<WeeklyProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WeeklyProgressEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpWeekStartDate;
            _tmpWeekStartDate = _cursor.getLong(_cursorIndexOfWeekStartDate);
            final long _tmpWeekEndDate;
            _tmpWeekEndDate = _cursor.getLong(_cursorIndexOfWeekEndDate);
            final long _tmpMinutesStudied;
            _tmpMinutesStudied = _cursor.getLong(_cursorIndexOfMinutesStudied);
            final int _tmpLessonsCompleted;
            _tmpLessonsCompleted = _cursor.getInt(_cursorIndexOfLessonsCompleted);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpDaysActive;
            _tmpDaysActive = _cursor.getInt(_cursorIndexOfDaysActive);
            final float _tmpAverageAccuracy;
            _tmpAverageAccuracy = _cursor.getFloat(_cursorIndexOfAverageAccuracy);
            _item = new WeeklyProgressEntity(_tmpId,_tmpUserId,_tmpWeekStartDate,_tmpWeekEndDate,_tmpMinutesStudied,_tmpLessonsCompleted,_tmpExercisesCompleted,_tmpDaysActive,_tmpAverageAccuracy);
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
  public Flow<List<WeeklyProgressEntity>> getWeeklyProgressSince(final String userId,
      final long startDate) {
    final String _sql = "SELECT * FROM weekly_progress WHERE userId = ? AND weekStartDate >= ? ORDER BY weekStartDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"weekly_progress"}, new Callable<List<WeeklyProgressEntity>>() {
      @Override
      @NonNull
      public List<WeeklyProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfWeekStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekStartDate");
          final int _cursorIndexOfWeekEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekEndDate");
          final int _cursorIndexOfMinutesStudied = CursorUtil.getColumnIndexOrThrow(_cursor, "minutesStudied");
          final int _cursorIndexOfLessonsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonsCompleted");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfDaysActive = CursorUtil.getColumnIndexOrThrow(_cursor, "daysActive");
          final int _cursorIndexOfAverageAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "averageAccuracy");
          final List<WeeklyProgressEntity> _result = new ArrayList<WeeklyProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WeeklyProgressEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpWeekStartDate;
            _tmpWeekStartDate = _cursor.getLong(_cursorIndexOfWeekStartDate);
            final long _tmpWeekEndDate;
            _tmpWeekEndDate = _cursor.getLong(_cursorIndexOfWeekEndDate);
            final long _tmpMinutesStudied;
            _tmpMinutesStudied = _cursor.getLong(_cursorIndexOfMinutesStudied);
            final int _tmpLessonsCompleted;
            _tmpLessonsCompleted = _cursor.getInt(_cursorIndexOfLessonsCompleted);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpDaysActive;
            _tmpDaysActive = _cursor.getInt(_cursorIndexOfDaysActive);
            final float _tmpAverageAccuracy;
            _tmpAverageAccuracy = _cursor.getFloat(_cursorIndexOfAverageAccuracy);
            _item = new WeeklyProgressEntity(_tmpId,_tmpUserId,_tmpWeekStartDate,_tmpWeekEndDate,_tmpMinutesStudied,_tmpLessonsCompleted,_tmpExercisesCompleted,_tmpDaysActive,_tmpAverageAccuracy);
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
  public Object getCorrectAnswerCount(final String userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM user_answers WHERE userId = ? AND isCorrect = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
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
  public Object getTotalAnswerCount(final String userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM user_answers WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
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
  public Object getCompletedLessonCount(final String userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM lesson_progress WHERE userId = ? AND isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
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
  public Object getTotalStudyTime(final String userId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT SUM(timeSpentMinutes) FROM lesson_progress WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
  public Object getUnlockedAchievementCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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
  public Object getStudyTimeSince(final String userId, final long startDate,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT SUM(durationMinutes) FROM study_sessions WHERE userId = ? AND startedAt >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
