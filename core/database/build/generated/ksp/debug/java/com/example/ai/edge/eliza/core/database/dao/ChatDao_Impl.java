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
import com.example.ai.edge.eliza.core.database.entity.BoundingBoxEntity;
import com.example.ai.edge.eliza.core.database.entity.ChatMessageEntity;
import com.example.ai.edge.eliza.core.database.entity.ChatSessionEntity;
import com.example.ai.edge.eliza.core.database.entity.ImageMathProblemEntity;
import com.example.ai.edge.eliza.core.database.entity.MathStepEntity;
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
public final class ChatDao_Impl implements ChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatSessionEntity> __insertionAdapterOfChatSessionEntity;

  private final EntityInsertionAdapter<ChatMessageEntity> __insertionAdapterOfChatMessageEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<MathStepEntity> __insertionAdapterOfMathStepEntity;

  private final EntityInsertionAdapter<ImageMathProblemEntity> __insertionAdapterOfImageMathProblemEntity;

  private final EntityDeletionOrUpdateAdapter<ChatSessionEntity> __deletionAdapterOfChatSessionEntity;

  private final EntityDeletionOrUpdateAdapter<ChatMessageEntity> __deletionAdapterOfChatMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MathStepEntity> __deletionAdapterOfMathStepEntity;

  private final EntityDeletionOrUpdateAdapter<ImageMathProblemEntity> __deletionAdapterOfImageMathProblemEntity;

  private final EntityDeletionOrUpdateAdapter<ChatSessionEntity> __updateAdapterOfChatSessionEntity;

  private final EntityDeletionOrUpdateAdapter<ChatMessageEntity> __updateAdapterOfChatMessageEntity;

  private final EntityDeletionOrUpdateAdapter<ImageMathProblemEntity> __updateAdapterOfImageMathProblemEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteChatSessionById;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateChatSession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessageById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesBySession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMathStepsByMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteImageMathProblemById;

  public ChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatSessionEntity = new EntityInsertionAdapter<ChatSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chat_sessions` (`id`,`title`,`subject`,`courseId`,`lessonId`,`createdAt`,`lastMessageAt`,`isActive`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatSessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getSubject() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSubject());
        }
        if (entity.getCourseId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCourseId());
        }
        if (entity.getLessonId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLessonId());
        }
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getLastMessageAt());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__insertionAdapterOfChatMessageEntity = new EntityInsertionAdapter<ChatMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chat_messages` (`id`,`sessionId`,`content`,`isUser`,`timestamp`,`imageUri`,`mathSteps`,`messageType`,`status`,`relatedExerciseId`,`relatedTrialId`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getContent());
        final int _tmp = entity.isUser() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        final String _tmp_1 = __converters.fromMathStepsList(entity.getMathSteps());
        statement.bindString(7, _tmp_1);
        statement.bindString(8, entity.getMessageType());
        statement.bindString(9, entity.getStatus());
        if (entity.getRelatedExerciseId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRelatedExerciseId());
        }
        if (entity.getRelatedTrialId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getRelatedTrialId());
        }
      }
    };
    this.__insertionAdapterOfMathStepEntity = new EntityInsertionAdapter<MathStepEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `math_steps` (`id`,`messageId`,`stepNumber`,`description`,`equation`,`explanation`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MathStepEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMessageId());
        statement.bindLong(3, entity.getStepNumber());
        statement.bindString(4, entity.getDescription());
        if (entity.getEquation() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEquation());
        }
        if (entity.getExplanation() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getExplanation());
        }
      }
    };
    this.__insertionAdapterOfImageMathProblemEntity = new EntityInsertionAdapter<ImageMathProblemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `image_math_problems` (`id`,`imageUri`,`extractedText`,`problemType`,`confidence`,`boundingBoxes`,`processedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageMathProblemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getImageUri());
        statement.bindString(3, entity.getExtractedText());
        statement.bindString(4, entity.getProblemType());
        statement.bindDouble(5, entity.getConfidence());
        final String _tmp = __converters.fromBoundingBoxList(entity.getBoundingBoxes());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getProcessedAt());
      }
    };
    this.__deletionAdapterOfChatSessionEntity = new EntityDeletionOrUpdateAdapter<ChatSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `chat_sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatSessionEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfChatMessageEntity = new EntityDeletionOrUpdateAdapter<ChatMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `chat_messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessageEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfMathStepEntity = new EntityDeletionOrUpdateAdapter<MathStepEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `math_steps` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MathStepEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfImageMathProblemEntity = new EntityDeletionOrUpdateAdapter<ImageMathProblemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `image_math_problems` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageMathProblemEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfChatSessionEntity = new EntityDeletionOrUpdateAdapter<ChatSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chat_sessions` SET `id` = ?,`title` = ?,`subject` = ?,`courseId` = ?,`lessonId` = ?,`createdAt` = ?,`lastMessageAt` = ?,`isActive` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatSessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getSubject() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSubject());
        }
        if (entity.getCourseId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCourseId());
        }
        if (entity.getLessonId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLessonId());
        }
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getLastMessageAt());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getId());
      }
    };
    this.__updateAdapterOfChatMessageEntity = new EntityDeletionOrUpdateAdapter<ChatMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chat_messages` SET `id` = ?,`sessionId` = ?,`content` = ?,`isUser` = ?,`timestamp` = ?,`imageUri` = ?,`mathSteps` = ?,`messageType` = ?,`status` = ?,`relatedExerciseId` = ?,`relatedTrialId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getContent());
        final int _tmp = entity.isUser() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        final String _tmp_1 = __converters.fromMathStepsList(entity.getMathSteps());
        statement.bindString(7, _tmp_1);
        statement.bindString(8, entity.getMessageType());
        statement.bindString(9, entity.getStatus());
        if (entity.getRelatedExerciseId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRelatedExerciseId());
        }
        if (entity.getRelatedTrialId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getRelatedTrialId());
        }
        statement.bindString(12, entity.getId());
      }
    };
    this.__updateAdapterOfImageMathProblemEntity = new EntityDeletionOrUpdateAdapter<ImageMathProblemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `image_math_problems` SET `id` = ?,`imageUri` = ?,`extractedText` = ?,`problemType` = ?,`confidence` = ?,`boundingBoxes` = ?,`processedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageMathProblemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getImageUri());
        statement.bindString(3, entity.getExtractedText());
        statement.bindString(4, entity.getProblemType());
        statement.bindDouble(5, entity.getConfidence());
        final String _tmp = __converters.fromBoundingBoxList(entity.getBoundingBoxes());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getProcessedAt());
        statement.bindString(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteChatSessionById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_sessions WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeactivateChatSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chat_sessions SET isActive = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessageById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessagesBySession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE sessionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMathStepsByMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM math_steps WHERE messageId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteImageMathProblemById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM image_math_problems WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertChatSession(final ChatSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessage(final ChatMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<ChatMessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMathStep(final MathStepEntity step,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMathStepEntity.insert(step);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMathSteps(final List<MathStepEntity> steps,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMathStepEntity.insert(steps);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertImageMathProblem(final ImageMathProblemEntity problem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfImageMathProblemEntity.insert(problem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChatSession(final ChatSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChatSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final ChatMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChatMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMathStep(final MathStepEntity step,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMathStepEntity.handle(step);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteImageMathProblem(final ImageMathProblemEntity problem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfImageMathProblemEntity.handle(problem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateChatSession(final ChatSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChatSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final ChatMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChatMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageMathProblem(final ImageMathProblemEntity problem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfImageMathProblemEntity.handle(problem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChatSessionById(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteChatSessionById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
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
          __preparedStmtOfDeleteChatSessionById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateChatSession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateChatSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
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
          __preparedStmtOfDeactivateChatSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessageById(final String messageId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessageById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfDeleteMessageById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesBySession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesBySession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
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
          __preparedStmtOfDeleteMessagesBySession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMathStepsByMessage(final String messageId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMathStepsByMessage.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfDeleteMathStepsByMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteImageMathProblemById(final String problemId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteImageMathProblemById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, problemId);
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
          __preparedStmtOfDeleteImageMathProblemById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatSessionEntity>> getAllChatSessions() {
    final String _sql = "SELECT * FROM chat_sessions ORDER BY lastMessageAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_sessions"}, new Callable<List<ChatSessionEntity>>() {
      @Override
      @NonNull
      public List<ChatSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<ChatSessionEntity> _result = new ArrayList<ChatSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatSessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
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
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new ChatSessionEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpCourseId,_tmpLessonId,_tmpCreatedAt,_tmpLastMessageAt,_tmpIsActive);
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
  public Flow<ChatSessionEntity> getChatSessionById(final String sessionId) {
    final String _sql = "SELECT * FROM chat_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_sessions"}, new Callable<ChatSessionEntity>() {
      @Override
      @Nullable
      public ChatSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final ChatSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
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
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _result = new ChatSessionEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpCourseId,_tmpLessonId,_tmpCreatedAt,_tmpLastMessageAt,_tmpIsActive);
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
  public Flow<List<ChatSessionEntity>> getActiveChatSessions() {
    final String _sql = "SELECT * FROM chat_sessions WHERE isActive = 1 ORDER BY lastMessageAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_sessions"}, new Callable<List<ChatSessionEntity>>() {
      @Override
      @NonNull
      public List<ChatSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<ChatSessionEntity> _result = new ArrayList<ChatSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatSessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
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
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new ChatSessionEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpCourseId,_tmpLessonId,_tmpCreatedAt,_tmpLastMessageAt,_tmpIsActive);
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
  public Flow<List<ChatSessionEntity>> getChatSessionsBySubject(final String subject) {
    final String _sql = "SELECT * FROM chat_sessions WHERE subject = ? ORDER BY lastMessageAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, subject);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_sessions"}, new Callable<List<ChatSessionEntity>>() {
      @Override
      @NonNull
      public List<ChatSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<ChatSessionEntity> _result = new ArrayList<ChatSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatSessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
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
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new ChatSessionEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpCourseId,_tmpLessonId,_tmpCreatedAt,_tmpLastMessageAt,_tmpIsActive);
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
  public Flow<List<ChatSessionEntity>> getChatSessionsByCourse(final String courseId) {
    final String _sql = "SELECT * FROM chat_sessions WHERE courseId = ? ORDER BY lastMessageAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, courseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_sessions"}, new Callable<List<ChatSessionEntity>>() {
      @Override
      @NonNull
      public List<ChatSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfLessonId = CursorUtil.getColumnIndexOrThrow(_cursor, "lessonId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<ChatSessionEntity> _result = new ArrayList<ChatSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatSessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
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
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new ChatSessionEntity(_tmpId,_tmpTitle,_tmpSubject,_tmpCourseId,_tmpLessonId,_tmpCreatedAt,_tmpLastMessageAt,_tmpIsActive);
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
  public Flow<List<ChatMessageEntity>> getMessagesBySession(final String sessionId) {
    final String _sql = "SELECT * FROM chat_messages WHERE sessionId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfMathSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "mathSteps");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRelatedExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedExerciseId");
          final int _cursorIndexOfRelatedTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedTrialId");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUser);
            _tmpIsUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final List<MathStepEntity> _tmpMathSteps;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMathSteps);
            _tmpMathSteps = __converters.toMathStepsList(_tmp_1);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRelatedExerciseId;
            if (_cursor.isNull(_cursorIndexOfRelatedExerciseId)) {
              _tmpRelatedExerciseId = null;
            } else {
              _tmpRelatedExerciseId = _cursor.getString(_cursorIndexOfRelatedExerciseId);
            }
            final String _tmpRelatedTrialId;
            if (_cursor.isNull(_cursorIndexOfRelatedTrialId)) {
              _tmpRelatedTrialId = null;
            } else {
              _tmpRelatedTrialId = _cursor.getString(_cursorIndexOfRelatedTrialId);
            }
            _item = new ChatMessageEntity(_tmpId,_tmpSessionId,_tmpContent,_tmpIsUser,_tmpTimestamp,_tmpImageUri,_tmpMathSteps,_tmpMessageType,_tmpStatus,_tmpRelatedExerciseId,_tmpRelatedTrialId);
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
  public Flow<ChatMessageEntity> getMessageById(final String messageId) {
    final String _sql = "SELECT * FROM chat_messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<ChatMessageEntity>() {
      @Override
      @Nullable
      public ChatMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfMathSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "mathSteps");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRelatedExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedExerciseId");
          final int _cursorIndexOfRelatedTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedTrialId");
          final ChatMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUser);
            _tmpIsUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final List<MathStepEntity> _tmpMathSteps;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMathSteps);
            _tmpMathSteps = __converters.toMathStepsList(_tmp_1);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRelatedExerciseId;
            if (_cursor.isNull(_cursorIndexOfRelatedExerciseId)) {
              _tmpRelatedExerciseId = null;
            } else {
              _tmpRelatedExerciseId = _cursor.getString(_cursorIndexOfRelatedExerciseId);
            }
            final String _tmpRelatedTrialId;
            if (_cursor.isNull(_cursorIndexOfRelatedTrialId)) {
              _tmpRelatedTrialId = null;
            } else {
              _tmpRelatedTrialId = _cursor.getString(_cursorIndexOfRelatedTrialId);
            }
            _result = new ChatMessageEntity(_tmpId,_tmpSessionId,_tmpContent,_tmpIsUser,_tmpTimestamp,_tmpImageUri,_tmpMathSteps,_tmpMessageType,_tmpStatus,_tmpRelatedExerciseId,_tmpRelatedTrialId);
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
  public Flow<List<ChatMessageEntity>> getRecentMessages(final String sessionId, final int limit) {
    final String _sql = "SELECT * FROM chat_messages WHERE sessionId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfMathSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "mathSteps");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRelatedExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedExerciseId");
          final int _cursorIndexOfRelatedTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedTrialId");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUser);
            _tmpIsUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final List<MathStepEntity> _tmpMathSteps;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMathSteps);
            _tmpMathSteps = __converters.toMathStepsList(_tmp_1);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRelatedExerciseId;
            if (_cursor.isNull(_cursorIndexOfRelatedExerciseId)) {
              _tmpRelatedExerciseId = null;
            } else {
              _tmpRelatedExerciseId = _cursor.getString(_cursorIndexOfRelatedExerciseId);
            }
            final String _tmpRelatedTrialId;
            if (_cursor.isNull(_cursorIndexOfRelatedTrialId)) {
              _tmpRelatedTrialId = null;
            } else {
              _tmpRelatedTrialId = _cursor.getString(_cursorIndexOfRelatedTrialId);
            }
            _item = new ChatMessageEntity(_tmpId,_tmpSessionId,_tmpContent,_tmpIsUser,_tmpTimestamp,_tmpImageUri,_tmpMathSteps,_tmpMessageType,_tmpStatus,_tmpRelatedExerciseId,_tmpRelatedTrialId);
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
  public Flow<ChatMessageEntity> getLastAIMessage(final String sessionId) {
    final String _sql = "SELECT * FROM chat_messages WHERE sessionId = ? AND isUser = 0 ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<ChatMessageEntity>() {
      @Override
      @Nullable
      public ChatMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfMathSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "mathSteps");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRelatedExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedExerciseId");
          final int _cursorIndexOfRelatedTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedTrialId");
          final ChatMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUser);
            _tmpIsUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final List<MathStepEntity> _tmpMathSteps;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMathSteps);
            _tmpMathSteps = __converters.toMathStepsList(_tmp_1);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRelatedExerciseId;
            if (_cursor.isNull(_cursorIndexOfRelatedExerciseId)) {
              _tmpRelatedExerciseId = null;
            } else {
              _tmpRelatedExerciseId = _cursor.getString(_cursorIndexOfRelatedExerciseId);
            }
            final String _tmpRelatedTrialId;
            if (_cursor.isNull(_cursorIndexOfRelatedTrialId)) {
              _tmpRelatedTrialId = null;
            } else {
              _tmpRelatedTrialId = _cursor.getString(_cursorIndexOfRelatedTrialId);
            }
            _result = new ChatMessageEntity(_tmpId,_tmpSessionId,_tmpContent,_tmpIsUser,_tmpTimestamp,_tmpImageUri,_tmpMathSteps,_tmpMessageType,_tmpStatus,_tmpRelatedExerciseId,_tmpRelatedTrialId);
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
  public Flow<List<ChatMessageEntity>> getMessagesWithImages() {
    final String _sql = "SELECT * FROM chat_messages WHERE imageUri IS NOT NULL ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfMathSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "mathSteps");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRelatedExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedExerciseId");
          final int _cursorIndexOfRelatedTrialId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedTrialId");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUser);
            _tmpIsUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final List<MathStepEntity> _tmpMathSteps;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMathSteps);
            _tmpMathSteps = __converters.toMathStepsList(_tmp_1);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRelatedExerciseId;
            if (_cursor.isNull(_cursorIndexOfRelatedExerciseId)) {
              _tmpRelatedExerciseId = null;
            } else {
              _tmpRelatedExerciseId = _cursor.getString(_cursorIndexOfRelatedExerciseId);
            }
            final String _tmpRelatedTrialId;
            if (_cursor.isNull(_cursorIndexOfRelatedTrialId)) {
              _tmpRelatedTrialId = null;
            } else {
              _tmpRelatedTrialId = _cursor.getString(_cursorIndexOfRelatedTrialId);
            }
            _item = new ChatMessageEntity(_tmpId,_tmpSessionId,_tmpContent,_tmpIsUser,_tmpTimestamp,_tmpImageUri,_tmpMathSteps,_tmpMessageType,_tmpStatus,_tmpRelatedExerciseId,_tmpRelatedTrialId);
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
  public Flow<List<MathStepEntity>> getMathStepsByMessage(final String messageId) {
    final String _sql = "SELECT * FROM math_steps WHERE messageId = ? ORDER BY stepNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"math_steps"}, new Callable<List<MathStepEntity>>() {
      @Override
      @NonNull
      public List<MathStepEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfStepNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "stepNumber");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfEquation = CursorUtil.getColumnIndexOrThrow(_cursor, "equation");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final List<MathStepEntity> _result = new ArrayList<MathStepEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MathStepEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final int _tmpStepNumber;
            _tmpStepNumber = _cursor.getInt(_cursorIndexOfStepNumber);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpEquation;
            if (_cursor.isNull(_cursorIndexOfEquation)) {
              _tmpEquation = null;
            } else {
              _tmpEquation = _cursor.getString(_cursorIndexOfEquation);
            }
            final String _tmpExplanation;
            if (_cursor.isNull(_cursorIndexOfExplanation)) {
              _tmpExplanation = null;
            } else {
              _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            }
            _item = new MathStepEntity(_tmpId,_tmpMessageId,_tmpStepNumber,_tmpDescription,_tmpEquation,_tmpExplanation);
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
  public Flow<List<ImageMathProblemEntity>> getAllImageMathProblems() {
    final String _sql = "SELECT * FROM image_math_problems ORDER BY processedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_math_problems"}, new Callable<List<ImageMathProblemEntity>>() {
      @Override
      @NonNull
      public List<ImageMathProblemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfExtractedText = CursorUtil.getColumnIndexOrThrow(_cursor, "extractedText");
          final int _cursorIndexOfProblemType = CursorUtil.getColumnIndexOrThrow(_cursor, "problemType");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfBoundingBoxes = CursorUtil.getColumnIndexOrThrow(_cursor, "boundingBoxes");
          final int _cursorIndexOfProcessedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "processedAt");
          final List<ImageMathProblemEntity> _result = new ArrayList<ImageMathProblemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageMathProblemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpImageUri;
            _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            final String _tmpExtractedText;
            _tmpExtractedText = _cursor.getString(_cursorIndexOfExtractedText);
            final String _tmpProblemType;
            _tmpProblemType = _cursor.getString(_cursorIndexOfProblemType);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final List<BoundingBoxEntity> _tmpBoundingBoxes;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfBoundingBoxes);
            _tmpBoundingBoxes = __converters.toBoundingBoxList(_tmp);
            final long _tmpProcessedAt;
            _tmpProcessedAt = _cursor.getLong(_cursorIndexOfProcessedAt);
            _item = new ImageMathProblemEntity(_tmpId,_tmpImageUri,_tmpExtractedText,_tmpProblemType,_tmpConfidence,_tmpBoundingBoxes,_tmpProcessedAt);
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
  public Flow<ImageMathProblemEntity> getImageMathProblemById(final String problemId) {
    final String _sql = "SELECT * FROM image_math_problems WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, problemId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_math_problems"}, new Callable<ImageMathProblemEntity>() {
      @Override
      @Nullable
      public ImageMathProblemEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfExtractedText = CursorUtil.getColumnIndexOrThrow(_cursor, "extractedText");
          final int _cursorIndexOfProblemType = CursorUtil.getColumnIndexOrThrow(_cursor, "problemType");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfBoundingBoxes = CursorUtil.getColumnIndexOrThrow(_cursor, "boundingBoxes");
          final int _cursorIndexOfProcessedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "processedAt");
          final ImageMathProblemEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpImageUri;
            _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            final String _tmpExtractedText;
            _tmpExtractedText = _cursor.getString(_cursorIndexOfExtractedText);
            final String _tmpProblemType;
            _tmpProblemType = _cursor.getString(_cursorIndexOfProblemType);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final List<BoundingBoxEntity> _tmpBoundingBoxes;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfBoundingBoxes);
            _tmpBoundingBoxes = __converters.toBoundingBoxList(_tmp);
            final long _tmpProcessedAt;
            _tmpProcessedAt = _cursor.getLong(_cursorIndexOfProcessedAt);
            _result = new ImageMathProblemEntity(_tmpId,_tmpImageUri,_tmpExtractedText,_tmpProblemType,_tmpConfidence,_tmpBoundingBoxes,_tmpProcessedAt);
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
  public Flow<List<ImageMathProblemEntity>> getImageMathProblemsByType(final String type) {
    final String _sql = "SELECT * FROM image_math_problems WHERE problemType = ? ORDER BY processedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_math_problems"}, new Callable<List<ImageMathProblemEntity>>() {
      @Override
      @NonNull
      public List<ImageMathProblemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfExtractedText = CursorUtil.getColumnIndexOrThrow(_cursor, "extractedText");
          final int _cursorIndexOfProblemType = CursorUtil.getColumnIndexOrThrow(_cursor, "problemType");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfBoundingBoxes = CursorUtil.getColumnIndexOrThrow(_cursor, "boundingBoxes");
          final int _cursorIndexOfProcessedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "processedAt");
          final List<ImageMathProblemEntity> _result = new ArrayList<ImageMathProblemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageMathProblemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpImageUri;
            _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            final String _tmpExtractedText;
            _tmpExtractedText = _cursor.getString(_cursorIndexOfExtractedText);
            final String _tmpProblemType;
            _tmpProblemType = _cursor.getString(_cursorIndexOfProblemType);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final List<BoundingBoxEntity> _tmpBoundingBoxes;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfBoundingBoxes);
            _tmpBoundingBoxes = __converters.toBoundingBoxList(_tmp);
            final long _tmpProcessedAt;
            _tmpProcessedAt = _cursor.getLong(_cursorIndexOfProcessedAt);
            _item = new ImageMathProblemEntity(_tmpId,_tmpImageUri,_tmpExtractedText,_tmpProblemType,_tmpConfidence,_tmpBoundingBoxes,_tmpProcessedAt);
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
  public Object getMessageCountBySession(final String sessionId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
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
  public Object getAIMessageCount(final String sessionId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages WHERE sessionId = ? AND isUser = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
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
  public Object getActiveSessionCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_sessions WHERE isActive = 1";
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
  public Object getImageProblemsCountSince(final long timestamp,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM image_math_problems WHERE processedAt > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, timestamp);
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
