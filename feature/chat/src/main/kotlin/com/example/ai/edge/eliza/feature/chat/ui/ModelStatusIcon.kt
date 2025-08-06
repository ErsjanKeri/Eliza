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

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.model.ModelDownloadStatus
import com.example.ai.edge.eliza.core.model.ModelDownloadStatusType
import com.example.ai.edge.eliza.core.designsystem.theme.Green40
import com.example.ai.edge.eliza.core.designsystem.theme.Red40

@Composable
fun ModelStatusIcon(
    downloadStatus: ModelDownloadStatus?,
    modifier: Modifier = Modifier
) {
    when (downloadStatus?.status) {
        ModelDownloadStatusType.SUCCEEDED -> {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Downloaded",
                tint = Green40, // Success color from theme
                modifier = modifier.size(12.dp)
            )
        }
        ModelDownloadStatusType.IN_PROGRESS -> {
            CircularProgressIndicator(
                modifier = modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        ModelDownloadStatusType.UNZIPPING -> {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Unzipping",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.size(12.dp)
            )
        }
        ModelDownloadStatusType.FAILED -> {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Failed",
                tint = Red40, // Error color from theme
                modifier = modifier.size(12.dp)
            )
        }
        ModelDownloadStatusType.NOT_DOWNLOADED,
        ModelDownloadStatusType.PARTIALLY_DOWNLOADED -> {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Not downloaded",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(12.dp)
            )
        }
        null -> {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Unknown status",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(12.dp)
            )
        }
    }
} 