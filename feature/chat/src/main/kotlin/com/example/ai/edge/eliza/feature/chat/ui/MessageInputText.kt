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

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Composable function to display a text input field for composing chat messages.
 * This implementation exactly copies Gallery's MessageInputText structure.
 */
@Composable
fun MessageInputText(
    curMessage: String,
    inProgress: Boolean,
    onValueChanged: (String) -> Unit,
    onSendMessage: (List<ChatMessage>) -> Unit,
    modifier: Modifier = Modifier,
    onStopButtonClicked: () -> Unit = {},
    showStopButtonWhenInProgress: Boolean = false,
    onImageSelected: (Bitmap) -> Unit = {},
    showImagePickerInMenu: Boolean = true,
) {
    val context = LocalContext.current
    var showAddContentMenu by remember { mutableStateOf(false) }
    var pickedImages by remember { mutableStateOf<List<Bitmap>>(listOf()) }
    var tempPhotoUri by remember { mutableStateOf(value = Uri.EMPTY) }
    
    // Image picker launcher - Gallery pattern
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            handleImageSelected(
                context = context, 
                uri = uri, 
                onImageSelected = { bitmap ->
                    pickedImages = pickedImages + bitmap
                    onImageSelected(bitmap)
                }
            )
        }
    }
    
    // Camera launcher - Gallery pattern  
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isImageSaved ->
        if (isImageSaved) {
            handleImageSelected(
                context = context, 
                uri = tempPhotoUri, 
                onImageSelected = { bitmap ->
                    pickedImages = pickedImages + bitmap
                    onImageSelected(bitmap)
                },
                rotateForPortrait = true
            )
        }
    }
    
    // Camera permission launcher - Gallery pattern
    val takePicturePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (permissionGranted) {
            tempPhotoUri = createTempPictureUri(context)
            cameraLauncher.launch(tempPhotoUri)
        }
    }
    
    Column(modifier = modifier) {
        // Show picked images if any
        if (pickedImages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pickedImages) { bitmap ->
                    Box {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button for each image
                        IconButton(
                            onClick = {
                                pickedImages = pickedImages.filter { it != bitmap }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Box(contentAlignment = Alignment.CenterStart) {
            // A plus button to show a popup menu to add stuff to the chat.
            IconButton(
                enabled = !inProgress,
                onClick = { showAddContentMenu = true },
                modifier = Modifier.offset(x = 16.dp).alpha(0.8f),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "", modifier = Modifier.size(28.dp))
            }
            
            // Dropdown menu for adding content - Gallery pattern
            DropdownMenu(
                expanded = showAddContentMenu,
                onDismissRequest = { showAddContentMenu = false }
            ) {
                if (showImagePickerInMenu) {
                    // Take a picture
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.PhotoCamera, contentDescription = "")
                                Text("Take a picture")
                            }
                        },
                        enabled = !inProgress,
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                    tempPhotoUri = createTempPictureUri(context)
                                    cameraLauncher.launch(tempPhotoUri)
                                }
                                else -> {
                                    takePicturePermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            showAddContentMenu = false
                        }
                    )
                    
                    // Pick from album
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Photo, contentDescription = "")
                                Text("Pick from album")
                            }
                        },
                        enabled = !inProgress,
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            showAddContentMenu = false
                        }
                    )
                }
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

                // Stop button when in progress - Gallery's exact pattern
                if (inProgress && showStopButtonWhenInProgress) {
                    IconButton(
                        onClick = onStopButtonClicked,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                    ) {
                        Icon(
                            Icons.Rounded.Stop,
                            contentDescription = "Stop response",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else if (curMessage.isNotEmpty() || pickedImages.isNotEmpty()) {
                    // Send button. Shown when there's text or images to send.
                    IconButton(
                        enabled = !inProgress,
                        onClick = { 
                            // Create messages from picked images and text - Gallery's exact pattern
                            onSendMessage(
                                createMessagesToSend(
                                    pickedImages = pickedImages,
                                    text = curMessage.trim(),
                                )
                            )
                            // Clear picked images after sending
                            pickedImages = emptyList()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send message",
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

// Helper functions from Gallery

private fun handleImageSelected(
    context: android.content.Context,
    uri: Uri,
    onImageSelected: (Bitmap) -> Unit,
    rotateForPortrait: Boolean = false
) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (bitmap != null) {
            val processedBitmap = if (rotateForPortrait) {
                // Rotate if needed for camera photos
                rotateBitmapIfNeeded(bitmap, uri, context)
            } else {
                bitmap
            }
            onImageSelected(processedBitmap)
        }
    } catch (e: Exception) {
        android.util.Log.e("MessageInputText", "Error loading image", e)
    }
}

private fun rotateBitmapIfNeeded(
    bitmap: Bitmap,
    uri: Uri,
    context: android.content.Context
): Bitmap {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exif = androidx.exifinterface.media.ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        )
        
        val matrix = android.graphics.Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        
        inputStream.close()
        
        return if (matrix.isIdentity) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    } catch (e: Exception) {
        return bitmap
    }
}

private fun createTempPictureUri(context: android.content.Context): Uri {
    val tempFile = java.io.File(
        context.externalCacheDir,
        "temp_photo_${System.currentTimeMillis()}.jpg"
    )
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

/**
 * Helper function to create messages from picked images and text.
 * EXACT COPY of Gallery's createMessagesToSend function.
 */
private fun createMessagesToSend(
    pickedImages: List<Bitmap>,
    text: String,
): List<ChatMessage> {
    val messages: MutableList<ChatMessage> = mutableListOf()

    // Add image messages.
    var imageMessages: MutableList<ChatMessageImage> = mutableListOf()
    if (pickedImages.isNotEmpty()) {
        for (image in pickedImages) {
            imageMessages.add(
                ChatMessageImage(bitmap = image, imageBitMap = image.asImageBitmap(), side = ChatSide.USER)
            )
        }
    }
    // Cap the number of image messages (Gallery uses MAX_IMAGE_COUNT = 10).
    val MAX_IMAGE_COUNT = 10
    if (imageMessages.size > MAX_IMAGE_COUNT) {
        imageMessages = imageMessages.subList(fromIndex = 0, toIndex = MAX_IMAGE_COUNT)
    }
    messages.addAll(imageMessages)

    // Add text message if present.
    if (text.isNotEmpty()) {
        messages.add(ChatMessageText(content = text, side = ChatSide.USER))
    }

    return messages
}