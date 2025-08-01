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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.ai.modelmanager.data.Model
import com.example.ai.edge.eliza.ai.modelmanager.data.Task
import com.example.ai.edge.eliza.ai.modelmanager.manager.ElizaModelManager
import kotlinx.coroutines.launch

/**
 * Gallery's exact ModelPickerChipsPager component adapted for Eliza.
 * Shows current model as a chip and allows model selection via bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorChip(
    task: Task,
    selectedModel: Model?,
    modelManager: ElizaModelManager,
    onModelSelected: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by modelManager.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showModelPicker by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // Model selector chip
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    scope.launch {
                        showModelPicker = true
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Model status icon
            val downloadStatus = selectedModel?.let { uiState.modelDownloadStatus[it.name] }
            ModelStatusIcon(downloadStatus = downloadStatus)
            
            // Model name
            Text(
                selectedModel?.name ?: "Select Model",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .widthIn(0.dp, screenWidthDp - 250.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            
            // Dropdown arrow
            Icon(
                Icons.Rounded.ArrowDropDown,
                modifier = Modifier.size(20.dp),
                contentDescription = "Select model",
            )
        }
    }

    // Model picker bottom sheet
    if (showModelPicker) {
        ModalBottomSheet(
            onDismissRequest = { showModelPicker = false },
            sheetState = sheetState
        ) {
            ElizaModelPicker(
                task = task,
                modelManager = modelManager,
                onModelSelected = { model ->
                    showModelPicker = false
                    onModelSelected(model)
                }
            )
        }
    }
} 