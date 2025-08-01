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

import android.content.ClipData
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class ChatInputType {
  TEXT,
  IMAGE,
}

/** Composable function for the main chat panel, displaying messages and handling user input. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPanel(
  messages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
  onImageSelected: (Bitmap) -> Unit = {},
  chatInputType: ChatInputType = ChatInputType.TEXT,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val haptic = LocalHapticFeedback.current

  var curMessage by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  // Remember the LazyListState to control scrolling
  val listState = rememberLazyListState()
  val density = LocalDensity.current

  var showMessageLongPressedSheet by remember { mutableStateOf(false) }
  val longPressedMessage: MutableState<ChatMessage?> = remember { mutableStateOf(null) }

  // Keep track of the last message and last message content.
  val lastMessage: MutableState<ChatMessage?> = remember { mutableStateOf(null) }
  val lastMessageContent: MutableState<String> = remember { mutableStateOf("") }
  if (messages.isNotEmpty()) {
    val tmpLastMessage = messages.last()
    lastMessage.value = tmpLastMessage
    if (tmpLastMessage is ChatMessageText) {
      lastMessageContent.value = tmpLastMessage.content
    }
  }

  // Scroll the content to the bottom when any of these changes.
  LaunchedEffect(
    messages.size,
    lastMessage.value,
    lastMessageContent.value,
    WindowInsets.ime.getBottom(density),
  ) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex, scrollOffset = 10000)
    }
  }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
        // If downward scroll, clear the focus from any currently focused composable.
        // This is useful for dismissing software keyboards or hiding text input fields
        // when the user starts scrolling down a list.
        if (available.y > 0) {
          focusManager.clearFocus()
        }
        // Let LazyColumn handle the scroll
        return androidx.compose.ui.geometry.Offset.Zero
      }
    }
  }

  Column(modifier = modifier.imePadding()) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.weight(1f)) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
        state = listState,
        verticalArrangement = Arrangement.Top,
      ) {
        items(messages) { message ->
          val imageHistoryCurIndex = remember { mutableIntStateOf(0) }
          var hAlign: Alignment.Horizontal = Alignment.End
          var backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
          var hardCornerAtLeftOrRight = false
          var extraPaddingStart = 48.dp
          var extraPaddingEnd = 0.dp
          
          if (message.side == ChatSide.AGENT) {
            hAlign = Alignment.Start
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer
            hardCornerAtLeftOrRight = true
            extraPaddingStart = 0.dp
            extraPaddingEnd = 48.dp
          } else if (message.side == ChatSide.SYSTEM) {
            extraPaddingStart = 24.dp
            extraPaddingEnd = 24.dp
          }
          
          if (message.type == ChatMessageType.IMAGE) {
            backgroundColor = Color.Transparent
          }
          val bubbleBorderRadius = 12.dp

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = 12.dp + extraPaddingStart,
                end = 12.dp + extraPaddingEnd,
                top = 6.dp,
                bottom = 6.dp,
              ),
            horizontalAlignment = hAlign,
          ) {
            // Sender row.
            if (message.side == ChatSide.AGENT) {
              Text(
                text = "AI Tutor",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
              )
            }

            // Message body with long press handling
            Box(
              modifier = Modifier
                .background(
                  color = backgroundColor,
                  shape = MessageBubbleShape(
                    radius = bubbleBorderRadius,
                    hardCornerAtLeftOrRight = hardCornerAtLeftOrRight
                  )
                )
// TODO: Add long press handling later
            ) {
              when (message) {
                // Loading.
                is ChatMessageLoading -> MessageBodyLoading()

                // Info.
                is ChatMessageInfo -> MessageBodyInfo(message = message)

                // Text message.
                is ChatMessageText -> MessageBodyText(message = message)

                // Image message.
                is ChatMessageImage -> {
                  androidx.compose.foundation.Image(
                    bitmap = message.imageBitMap,
                    contentDescription = "User image",
                    modifier = Modifier
                      .size(200.dp)
                      .padding(8.dp)
                  )
                }

                else -> {
                  // Fallback for other message types
                  MessageBodyText(message = ChatMessageText(content = "Unsupported message type", side = ChatSide.SYSTEM))
                }
              }
            }
          }
        }
      }

      SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(vertical = 4.dp))

      // Show info message when no messages
      if (messages.isEmpty()) {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          MessageBodyInfo(
            ChatMessageInfo(
              content = "Ask me anything about this chapter! I'm here to help you understand the material."
            ),
            smallFontSize = false,
          )
        }
      }
    }

    // Chat input
    when (chatInputType) {
      ChatInputType.TEXT -> {
        MessageInputText(
          curMessage = curMessage,
          inProgress = isLoading,
          onValueChanged = { curMessage = it },
          onSendMessage = { text ->
            onSendMessage(text)
            curMessage = ""
          },
        )
      }

      ChatInputType.IMAGE -> {
        // Image input placeholder - would go here when needed
      }
    }
  }

  // TODO: Add clipboard functionality later
  // Temporarily commented out to fix compilation issues
} 