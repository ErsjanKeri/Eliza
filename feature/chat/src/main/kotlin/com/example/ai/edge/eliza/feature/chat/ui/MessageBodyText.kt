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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.core.designsystem.component.ElizaMarkdownRenderer

/** Composable function to display the text content of a ChatMessageText. */
@Composable
fun MessageBodyText(message: ChatMessageText) {
  if (message.side == ChatSide.USER) {
    Text(
      message.content,
      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
      color = Color.Black,
      modifier = Modifier.padding(12.dp),
    )
  } else if (message.side == ChatSide.AGENT) {
    // Use ElizaMarkdownRenderer for AI responses - same as chapter content
    ElizaMarkdownRenderer(
      content = message.content.trimEnd(),
      smallFontSize = false,
      useDefaultPadding = false, // Chat messages handle their own padding
      modifier = Modifier.padding(12.dp)
    )
  }
} 