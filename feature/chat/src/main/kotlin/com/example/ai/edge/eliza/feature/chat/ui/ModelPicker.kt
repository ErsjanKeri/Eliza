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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.edge.eliza.ai.modelmanager.data.Model
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import com.example.ai.edge.eliza.core.designsystem.icon.ElizaIcons

/**
 * Gallery's exact ModelPicker component adapted for Eliza.
 * Shows all available models for a task with download status.
 */
@Composable
fun ElizaModelPicker(
    task: Task,
    modelManager: ElizaModelManager,
    onModelSelected: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by modelManager.uiState.collectAsState()

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        // Title
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = ElizaIcons.Chat,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
                contentDescription = "",
            )
            Text(
                "${task.type.label} models",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Model list
        val modelsForTask = uiState.tasks.find { it.type.id == task.type.id }?.models ?: emptyList()
        for (model in modelsForTask) {
            val selected = model.name == uiState.selectedModel?.name
            val downloadStatus = uiState.modelDownloadStatus[model.name]
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModelSelected(model) }
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.name, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ModelStatusIcon(downloadStatus = downloadStatus)
                        Text(
                            formatFileSize(model.sizeInBytes),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 10.sp),
                        )
                    }
                }
                if (selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        modifier = Modifier.size(16.dp),
                        contentDescription = "Selected"
                    )
                }
            }
        }
    }
}

/**
 * Format file size to human readable format.
 */
private fun formatFileSize(bytes: Long): String {
    val kilobyte = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        bytes >= gigabyte -> String.format("%.1f GB", bytes.toDouble() / gigabyte)
        bytes >= megabyte -> String.format("%.1f MB", bytes.toDouble() / megabyte)
        bytes >= kilobyte -> String.format("%.1f KB", bytes.toDouble() / kilobyte)
        else -> "$bytes B"
    }
} 