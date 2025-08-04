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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.model.VideoErrorType
import com.example.ai.edge.eliza.core.model.VideoExplanationStatusType
import com.example.ai.edge.eliza.core.network.model.VideoStatus

/** Composable function to display video generation request status with live updates and error handling. */
@Composable
fun MessageBodyVideoRequest(
  message: ChatMessageVideoRequest,
  onRetryRequest: ((String) -> Unit)? = null,
  onCancelRequest: ((String) -> Unit)? = null,
  onFallbackToTextChat: ((String) -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Header with icon and user prompt
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = Icons.Default.VideoCall,
        contentDescription = "Video Request",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
      
      Text(
        text = "Video Explanation",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.primary
      )
      
      Spacer(modifier = Modifier.weight(1f))
      
      // Elapsed time
      Text(
        text = formatElapsedTime(message.getElapsedTimeSeconds()),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    
    // User's original prompt (truncated)
    Text(
      text = "\"${message.userPrompt}\"",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    
    // Progress bar (if progress is available)
    if (message.progress != null) {
      LinearProgressIndicator(
        progress = { message.progress / 100f },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
      
      // Progress percentage
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = getStatusDisplayText(message.status),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
          text = "${message.progress}%",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      // Status without progress
      Text(
        text = getStatusDisplayText(message.status),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    
    // Current status message
    Text(
      text = message.currentMessage,
      style = MaterialTheme.typography.bodySmall,
      color = when (message.status) {
        VideoStatus.FAILED -> MaterialTheme.colorScheme.error
        VideoStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
      }
    )
    
    // Error information and retry button (if applicable)
    if (message.status == VideoStatus.FAILED && message.errorInfo != null) {
      Spacer(modifier = Modifier.height(8.dp))
      
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Error header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Error,
              contentDescription = "Error",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(16.dp)
            )
            
            Text(
              text = "Video Generation Failed",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.error
            )
          }
          
          // Error message
          Text(
            text = message.errorInfo.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          
          // Suggested action (if available)
          message.errorInfo.suggestedAction?.let { action ->
            Text(
              text = "💡 $action",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          // Retry attempt information
          if (message.retryCount > 0) {
            Text(
              text = "Attempt ${message.retryCount + 1} of ${message.errorInfo.maxRetries + 1}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          // Text fallback suggestion (if applicable)
          val shouldShowFallback = message.errorInfo.type?.let { errorType ->
            VideoFallbackHelper.shouldOfferTextFallback(errorType, message.retryCount)
          } ?: false
          
          if (shouldShowFallback && onFallbackToTextChat != null) {
            val fallbackMessage = message.errorInfo.type?.let { errorType ->
              VideoFallbackHelper.getFallbackMessage(errorType)
            } ?: "I can answer your question with text instead."
            
            Text(
              text = "$fallbackMessage",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            )
          }
          
          // Action buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (shouldShowFallback) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Text fallback button (if applicable)
            if (shouldShowFallback && onFallbackToTextChat != null) {
              Button(
                onClick = { onFallbackToTextChat(message.userPrompt) },
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Text("Get Text Answer")
              }
            }
            
            Row(
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Retry button (if retryable)
              if (message.canRetry && onRetryRequest != null) {
                OutlinedButton(
                  onClick = { onRetryRequest(message.userPrompt) },
                  modifier = Modifier.padding(end = 8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Retry")
                }
              }
              
              // Cancel button (if available)
              if (onCancelRequest != null) {
                OutlinedButton(
                  onClick = { onCancelRequest(message.videoId) }
                ) {
                  Text("Cancel")
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Get user-friendly display text for video status.
 */
private fun getStatusDisplayText(status: VideoStatus): String {
  return when (status) {
    VideoStatus.QUEUED -> "Queued for processing..."
    VideoStatus.GENERATING_SCRIPT -> "Generating script..."
    VideoStatus.RENDERING_VIDEO -> "Rendering video..."
    VideoStatus.COMPLETED -> "Video ready!"
    VideoStatus.FAILED -> "Generation failed"
  }
}

/**
 * Format elapsed time in a user-friendly way.
 */
private fun formatElapsedTime(seconds: Long): String {
  return when {
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
  }
}