/*
 * Copyright 2025 The Eliza Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Eliza icons - Educational app iconography
 * Material icons optimized for learning and teaching experiences
 */
object ElizaIcons {
    
    // Navigation icons
    val ArrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    val Home = Icons.Rounded.Home
    val HomeBorder = Icons.Outlined.Home
    val MoreVert = Icons.Rounded.MoreVert
    
    // Learning & Education icons
    val School = Icons.Outlined.School
    val Book = Icons.AutoMirrored.Rounded.MenuBook
    val Progress = Icons.AutoMirrored.Outlined.TrendingUp
    val Camera = Icons.Outlined.CameraAlt
    
    // Communication icons
    val Chat = Icons.AutoMirrored.Rounded.Chat
    val ChatBorder = Icons.AutoMirrored.Outlined.Chat
    val Send = Icons.AutoMirrored.Rounded.Send
    
    // Action icons
    val Add = Icons.Rounded.Add
    val Close = Icons.Rounded.Close
    val Delete = Icons.Rounded.Delete
    val Edit = Icons.Rounded.Edit
    val Search = Icons.Rounded.Search
    val Share = Icons.Rounded.Share
    val Settings = Icons.Rounded.Settings
    val SettingsBorder = Icons.Outlined.Settings
    val Refresh = Icons.Rounded.Refresh
    val Info = Icons.Rounded.Info
    
    // Status icons
    val Check = Icons.Rounded.Check
    val CheckCircle = Icons.Rounded.CheckCircle
    val Lock = Icons.Rounded.Lock
    val Play = Icons.Rounded.PlayArrow
    val Pause = Icons.Rounded.Pause
    val Download = Icons.Rounded.Download
    
    // Bookmark icons
    val BookmarkAdd = Icons.Outlined.BookmarkAdd
    val BookmarkAdded = Icons.Outlined.BookmarkAdded
    
    // Favorite icons
    val Favorite = Icons.Rounded.Favorite
    val FavoriteBorder = Icons.Rounded.FavoriteBorder
    
    // Star icons
    val Star = Icons.Rounded.Star
    val StarBorder = Icons.Rounded.StarBorder
    
    // Visibility icons
    val Visibility = Icons.Rounded.Visibility
    val VisibilityOff = Icons.Rounded.VisibilityOff
    
    // Expansion icons
    val ExpandMore = Icons.Rounded.ExpandMore
    val ExpandLess = Icons.Rounded.ExpandLess
    
    // Profile icon
    val Person = Icons.Rounded.Person
}

/**
 * Educational context icons with semantic meaning
 */
object EducationIcons {
    val Lesson = ElizaIcons.Book
    val Course = ElizaIcons.School
    val Progress = ElizaIcons.Progress
    val AIChat = ElizaIcons.Chat
    val Camera = ElizaIcons.Camera
    val Complete = ElizaIcons.CheckCircle
    val Locked = ElizaIcons.Lock
    val Bookmark = ElizaIcons.BookmarkAdd
    val Bookmarked = ElizaIcons.BookmarkAdded
}

/**
 * UI context icons for interface elements
 */
object UIIcons {
    val Back = ElizaIcons.ArrowBack
    val Close = ElizaIcons.Close
    val Menu = ElizaIcons.MoreVert
    val Add = ElizaIcons.Add
    val Send = ElizaIcons.Send
    val Search = ElizaIcons.Search
    val Settings = ElizaIcons.Settings
    val Info = ElizaIcons.Info
    val Share = ElizaIcons.Share
} 