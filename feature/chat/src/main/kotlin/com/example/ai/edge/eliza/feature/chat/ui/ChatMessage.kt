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

package com.example.ai.edge.eliza.feature.chat.ui

import android.graphics.Bitmap
import com.example.ai.edge.eliza.core.model.VideoErrorInfo
import com.example.ai.edge.eliza.core.network.model.VideoStatus
import com.example.ai.edge.eliza.feature.chat.model.CourseNavigationData
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp

private const val TAG = "ElizaChatMessage"

enum class ChatMessageType {
  INFO,
  WARNING,
  TEXT,
  IMAGE,
  IMAGE_WITH_HISTORY,
  AUDIO_CLIP,
  LOADING,
  CLASSIFICATION,
  CONFIG_VALUES_CHANGE,
  BENCHMARK_RESULT,
  BENCHMARK_LLM_RESULT,
  PROMPT_TEMPLATES,
  VIDEO_REQUEST,
  VIDEO,
  COURSE_SUGGESTION, // NEW: For AI responses with course navigation actions
}

enum class ChatSide {
  USER,
  AGENT,
  SYSTEM,
}

/** Base class for a chat message. */
open class ChatMessage(
  open val type: ChatMessageType,
  open val side: ChatSide,
  open val latencyMs: Float = -1f,
  open val accelerator: String = "",
) {
  open fun clone(): ChatMessage {
    return ChatMessage(type = type, side = side, latencyMs = latencyMs)
  }
}

/** Chat message for showing loading status. */
class ChatMessageLoading(override val accelerator: String = "") :
  ChatMessage(type = ChatMessageType.LOADING, side = ChatSide.AGENT, accelerator = accelerator)

/** Chat message for info (help). */
class ChatMessageInfo(val content: String) :
  ChatMessage(type = ChatMessageType.INFO, side = ChatSide.SYSTEM)

/** Chat message for warning. */
class ChatMessageWarning(val content: String) :
  ChatMessage(type = ChatMessageType.WARNING, side = ChatSide.SYSTEM)

/** Chat message for plain text. */
open class ChatMessageText(
  val content: String,
  override val side: ChatSide,
  // Negative numbers will hide the latency display.
  override val latencyMs: Float = 0f,
  val isMarkdown: Boolean = true,

  // Benchmark result for LLM response.
  var llmBenchmarkResult: ChatMessageBenchmarkLlmResult? = null,
  override val accelerator: String = "",
) :
  ChatMessage(
    type = ChatMessageType.TEXT,
    side = side,
    latencyMs = latencyMs,
    accelerator = accelerator,
  ) {
  override fun clone(): ChatMessageText {
    return ChatMessageText(
      content = content,
      side = side,
      latencyMs = latencyMs,
      accelerator = accelerator,
      isMarkdown = isMarkdown,
      llmBenchmarkResult = llmBenchmarkResult,
    )
  }
}

/** Chat message for course suggestions with navigation actions. */
class ChatMessageCourseSuggestion(
  content: String,
  val navigationData: CourseNavigationData,
  side: ChatSide = ChatSide.AGENT,
  latencyMs: Float = 0f,
  isMarkdown: Boolean = true,
  accelerator: String = "",
) : ChatMessageText(
  content = content,
  side = side,
  latencyMs = latencyMs,
  isMarkdown = isMarkdown,
  accelerator = accelerator
) {
  
  override val type: ChatMessageType = ChatMessageType.COURSE_SUGGESTION
  
  override fun clone(): ChatMessageCourseSuggestion {
    return ChatMessageCourseSuggestion(
      content = content,
      navigationData = navigationData,
      side = side,
      latencyMs = latencyMs,
      isMarkdown = isMarkdown,
      accelerator = accelerator,
    )
  }
}

/** Chat message for images. */
class ChatMessageImage(
  val bitmap: Bitmap,
  val imageBitMap: ImageBitmap,
  override val side: ChatSide,
  override val latencyMs: Float = 0f,
) : ChatMessage(type = ChatMessageType.IMAGE, side = side, latencyMs = latencyMs) {
  override fun clone(): ChatMessageImage {
    return ChatMessageImage(
      bitmap = bitmap,
      imageBitMap = imageBitMap,
      side = side,
      latencyMs = latencyMs,
    )
  }
}

/** Chat message for audio clip. */
class ChatMessageAudioClip(
  val audioData: ByteArray,
  val sampleRate: Int,
  override val side: ChatSide,
  override val latencyMs: Float = 0f,
) : ChatMessage(type = ChatMessageType.AUDIO_CLIP, side = side, latencyMs = latencyMs) {
  override fun clone(): ChatMessageAudioClip {
    return ChatMessageAudioClip(
      audioData = audioData,
      sampleRate = sampleRate,
      side = side,
      latencyMs = latencyMs,
    )
  }

  fun genByteArrayForWav(): ByteArray {
    val header = ByteArray(44)

    val pcmDataSize = audioData.size
    val wavFileSize = pcmDataSize + 44 // 44 bytes for the header
    val channels = 1 // Mono
    val bitsPerSample: Short = 16
    val byteRate = sampleRate * channels * bitsPerSample / 8
    Log.d(TAG, "Wav metadata: sampleRate: $sampleRate")

    // RIFF/WAVE header
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    header[4] = (wavFileSize and 0xff).toByte()
    header[5] = (wavFileSize shr 8 and 0xff).toByte()
    header[6] = (wavFileSize shr 16 and 0xff).toByte()
    header[7] = (wavFileSize shr 24 and 0xff).toByte()
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    header[16] = 16
    header[17] = 0
    header[18] = 0
    header[19] = 0 // Sub-chunk size (16 for PCM)
    header[20] = 1
    header[21] = 0 // Audio format (1 for PCM)
    header[22] = channels.toByte()
    header[23] = 0 // Number of channels
    header[24] = (sampleRate and 0xff).toByte()
    header[25] = (sampleRate shr 8 and 0xff).toByte()
    header[26] = (sampleRate shr 16 and 0xff).toByte()
    header[27] = (sampleRate shr 24 and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte()
    header[29] = (byteRate shr 8 and 0xff).toByte()
    header[30] = (byteRate shr 16 and 0xff).toByte()
    header[31] = (byteRate shr 24 and 0xff).toByte()
    header[32] = (channels * bitsPerSample / 8).toByte()
    header[33] = 0 // Block align
    header[34] = bitsPerSample.toByte()
    header[35] = (bitsPerSample.toInt() shr 8 and 0xff).toByte() // Bits per sample
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    header[40] = (pcmDataSize and 0xff).toByte()
    header[41] = (pcmDataSize shr 8 and 0xff).toByte()
    header[42] = (pcmDataSize shr 16 and 0xff).toByte()
    header[43] = (pcmDataSize shr 24 and 0xff).toByte()

    return header + audioData
  }

  fun getDurationInSeconds(): Float {
    // PCM 16-bit
    val bytesPerSample = 2
    val bytesPerFrame = bytesPerSample * 1 // mono
    val totalFrames = audioData.size.toFloat() / bytesPerFrame
    return totalFrames / sampleRate
  }
}

/** Chat message for images with history. */
class ChatMessageImageWithHistory(
  val bitmaps: List<Bitmap>,
  val imageBitMaps: List<ImageBitmap>,
  val totalIterations: Int,
  override val side: ChatSide,
  override val latencyMs: Float = 0f,
  var curIteration: Int = 0, // 0-based
) : ChatMessage(type = ChatMessageType.IMAGE_WITH_HISTORY, side = side, latencyMs = latencyMs) {
  fun isRunning(): Boolean {
    return curIteration < totalIterations - 1
  }
}

/** A stat used in benchmark result. */
data class Stat(val id: String, val label: String, val unit: String)

data class Histogram(val buckets: List<Int>, val maxCount: Int, val highlightBucketIndex: Int = -1)

/** Chat message for video generation requests with live status updates and error handling. */
class ChatMessageVideoRequest(
  val videoId: String,
  val userPrompt: String,
  val status: VideoStatus,
  val progress: Int?,
  val currentMessage: String,
  val startedAt: Long = System.currentTimeMillis(),
  val errorInfo: VideoErrorInfo? = null, // Enhanced error information
  val retryCount: Int = 0, // Number of retry attempts
  val canRetry: Boolean = false, // Whether retry is possible
  override val side: ChatSide,
  override val latencyMs: Float = 0f,
) : ChatMessage(type = ChatMessageType.VIDEO_REQUEST, side = side, latencyMs = latencyMs) {
  override fun clone(): ChatMessageVideoRequest {
    return ChatMessageVideoRequest(
      videoId = videoId,
      userPrompt = userPrompt,
      status = status,
      progress = progress,
      currentMessage = currentMessage,
      startedAt = startedAt,
      errorInfo = errorInfo,
      retryCount = retryCount,
      canRetry = canRetry,
      side = side,
      latencyMs = latencyMs,
    )
  }
  
  /**
   * Create an updated version of this message with new status and progress.
   */
  fun withStatusUpdate(
    videoId: String = this.videoId,
    status: VideoStatus = this.status,
    progress: Int? = this.progress,
    currentMessage: String = this.currentMessage,
    errorInfo: VideoErrorInfo? = this.errorInfo,
    retryCount: Int = this.retryCount,
    canRetry: Boolean = this.canRetry
  ): ChatMessageVideoRequest {
    return ChatMessageVideoRequest(
      videoId = videoId,
      userPrompt = userPrompt,
      status = status,
      progress = progress,
      currentMessage = currentMessage,
      startedAt = startedAt,
      errorInfo = errorInfo,
      retryCount = retryCount,
      canRetry = canRetry,
      side = side,
      latencyMs = latencyMs,
    )
  }
  
  /**
   * Create a failed version of this message with error information.
   */
  fun withError(errorInfo: VideoErrorInfo): ChatMessageVideoRequest {
    return withStatusUpdate(
      status = VideoStatus.FAILED,
      currentMessage = errorInfo.message,
      errorInfo = errorInfo,
      canRetry = errorInfo.isRetryable && retryCount < errorInfo.maxRetries
    )
  }
  
  /**
   * Create a retry version of this message.
   */
  fun withRetry(): ChatMessageVideoRequest {
    return withStatusUpdate(
      retryCount = retryCount + 1,
      currentMessage = "Retrying video request...",
      status = VideoStatus.QUEUED,
      progress = 0
    )
  }
  
  /**
   * Get elapsed time since video generation started.
   */
  fun getElapsedTimeSeconds(): Long {
    return (System.currentTimeMillis() - startedAt) / 1000
  }
  
  /**
   * Check if the video generation is still in progress.
   */
  fun isInProgress(): Boolean {
    return status == VideoStatus.QUEUED || 
           status == VideoStatus.GENERATING_SCRIPT || 
           status == VideoStatus.RENDERING_VIDEO
  }
}

/** Chat message for completed video explanations. */
class ChatMessageVideo(
  val videoId: String,
  val title: String,
  val localFilePath: String,
  val durationSeconds: Int,
  val fileSizeBytes: Long,
  override val side: ChatSide,
  override val latencyMs: Float = 0f,
) : ChatMessage(type = ChatMessageType.VIDEO, side = side, latencyMs = latencyMs) {
  override fun clone(): ChatMessageVideo {
    return ChatMessageVideo(
      videoId = videoId,
      title = title,
      localFilePath = localFilePath,
      durationSeconds = durationSeconds,
      fileSizeBytes = fileSizeBytes,
      side = side,
      latencyMs = latencyMs,
    )
  }
  
  /**
   * Get formatted duration string (e.g., "2:30").
   */
  fun getFormattedDuration(): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
  }
  
  /**
   * Get formatted file size string (e.g., "1.2 MB").
   */
  fun getFormattedFileSize(): String {
    return when {
      fileSizeBytes < 1024 -> "${fileSizeBytes} B"
      fileSizeBytes < 1024 * 1024 -> "${String.format("%.1f", fileSizeBytes / 1024.0)} KB"
      else -> "${String.format("%.1f", fileSizeBytes / (1024.0 * 1024.0))} MB"
    }
  }
}

/** Chat message for showing LLM benchmark result. */
class ChatMessageBenchmarkLlmResult(
  val orderedStats: List<Stat>,
  val statValues: MutableMap<String, Float>,
  val running: Boolean,
  override val latencyMs: Float = 0f,
  override val accelerator: String = "",
) :
  ChatMessage(
    type = ChatMessageType.BENCHMARK_LLM_RESULT,
    side = ChatSide.AGENT,
    latencyMs = latencyMs,
    accelerator = accelerator,
  ) 