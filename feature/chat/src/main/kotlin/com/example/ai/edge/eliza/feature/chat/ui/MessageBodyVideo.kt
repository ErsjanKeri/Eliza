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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Composable function to display a completed video explanation. */
@Composable
fun MessageBodyVideo(
  message: ChatMessageVideo,
  onPlayVideo: (ChatMessageVideo) -> Unit = {}
) {
  var isPlaying by remember { mutableStateOf(false) }
  var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var showFullscreenDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  
  // Load video thumbnail when component is first created
  LaunchedEffect(message.videoId) {
    thumbnailBitmap = generateVideoThumbnail(message.localFilePath)
  }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Video player or thumbnail with play button overlay
      if (isPlaying) {
        // Show actual video player
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        ) {
          VideoPlayerComposable(
            videoFilePath = message.localFilePath,
            autoPlay = true,
            showControls = true,
            modifier = Modifier.fillMaxWidth(),
            onFullscreenToggle = { isFullscreen ->
              if (isFullscreen) {
                showFullscreenDialog = true
              }
            }
          )
          
          // Stop button overlay
          Surface(
            modifier = Modifier
              .size(40.dp)
              .align(Alignment.TopEnd)
              .padding(8.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.clickable { isPlaying = false }
            ) {
              Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop video",
                modifier = Modifier.size(20.dp),
                tint = Color.White
              )
            }
          }
        }
      } else {
        // Show thumbnail with play button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Standard video aspect ratio
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { 
              isPlaying = true
              onPlayVideo(message) // Still call the callback for potential analytics
            },
          contentAlignment = Alignment.Center
        ) {
          // Video thumbnail or placeholder
          if (thumbnailBitmap != null) {
            Image(
              bitmap = thumbnailBitmap!!,
              contentDescription = "Video thumbnail",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Icon(
              imageVector = Icons.Default.VideoFile,
              contentDescription = "Video thumbnail placeholder",
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          // Play button overlay
          Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play video",
                modifier = Modifier.size(32.dp),
                tint = Color.White
              )
            }
          }
        }
      }
      
      // Video information
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Video title
        Text(
          text = message.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        
        // Duration and file size
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Duration: ${message.getFormattedDuration()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          
          Text(
            text = "Size: ${message.getFormattedFileSize()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          
          Spacer(modifier = Modifier.weight(1f))
          
          Text(
            text = if (isPlaying) "Playing..." else "Tap to play",
            style = MaterialTheme.typography.bodySmall,
            color = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
  
  // Fullscreen video dialog
  if (showFullscreenDialog) {
    FullscreenVideoDialog(
      videoFilePath = message.localFilePath,
      onDismiss = { showFullscreenDialog = false }
    )
  }
}