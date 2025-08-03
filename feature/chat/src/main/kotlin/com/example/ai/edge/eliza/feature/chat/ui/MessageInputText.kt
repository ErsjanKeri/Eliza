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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Composable function to display a text input field for composing chat messages.
 * This implementation exactly copies Gallery's MessageInputText structure.
 */
@Composable
fun MessageInputText(
    curMessage: String,
    inProgress: Boolean,
    onValueChanged: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    onStopButtonClicked: () -> Unit = {},
    showStopButtonWhenInProgress: Boolean = false,
) {
    Column(modifier = modifier) {
        Box(contentAlignment = Alignment.CenterStart) {
            // A plus button to show a popup menu to add stuff to the chat.
            IconButton(
                enabled = !inProgress,
                onClick = { /* TODO: Add menu functionality later */ },
                modifier = Modifier.offset(x = 16.dp).alpha(0.8f),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "", modifier = Modifier.size(28.dp))
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Text field.
                TextField(
                    value = curMessage,
                    minLines = 1,
                    maxLines = 3,
                    onValueChange = onValueChanged,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).padding(start = 36.dp),
                    placeholder = { Text("Type message…") },
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send button. Only shown when text is not empty.
                if (curMessage.isNotEmpty()) {
                    IconButton(
                        enabled = !inProgress,
                        onClick = { onSendMessage(curMessage.trim()) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "",
                            modifier = Modifier.offset(x = 2.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}