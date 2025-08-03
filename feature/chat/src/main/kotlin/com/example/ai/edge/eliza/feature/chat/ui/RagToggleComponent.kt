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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.ai.rag.service.RagInitializationService
import com.example.ai.edge.eliza.ai.rag.service.RagInitializationStatus
import com.example.ai.edge.eliza.core.model.ChatContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Simplified RAG toggle for top bar layout (left position).
 */
@Composable
fun SimpleRagToggle(
    modifier: Modifier = Modifier,
    viewModel: RagToggleViewModel = hiltViewModel()
) {
    val isRagEnabled by viewModel.isRagEnabled.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      
        
        if (isInitializing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Switch(
                checked = isRagEnabled,
                onCheckedChange = { viewModel.toggleRag(it) },
                modifier = Modifier.size(width = 32.dp, height = 20.dp)
            )
        }
        // add some space between the switch and the text
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "RAG enhanced",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Component for toggling RAG functionality in the chat interface.
 */
@Composable
fun RagToggleComponent(
    modifier: Modifier = Modifier,
    viewModel: RagToggleViewModel = hiltViewModel()
) {
    val isRagEnabled by viewModel.isRagEnabled.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val initializationStatus by viewModel.initializationStatus.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Enhanced RAG",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enable context-aware responses using Retrieval Augmented Generation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRagEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Show initialization status
                    when (initializationStatus) {
                        RagInitializationStatus.NOT_STARTED -> {
                            Text(
                                text = "Not initialized",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RagInitializationStatus.INITIALIZING -> {
                            Text(
                                text = "Initializing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        RagInitializationStatus.READY -> {
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        RagInitializationStatus.FAILED -> {
                            Text(
                                text = "Initialization error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (isInitializing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = isRagEnabled,
                        onCheckedChange = { viewModel.toggleRag(it) }
                    )
                }
            }
            
            if (isRagEnabled) {
                Text(
                    text = "✓ RAG is active - responses will include relevant context",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Settings component for RAG configuration.
 */
@Composable
fun RagSettingsComponent(
    modifier: Modifier = Modifier,
    viewModel: RagToggleViewModel = hiltViewModel()
) {
    val isRagEnabled by viewModel.isRagEnabled.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val initializationStatus by viewModel.initializationStatus.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RAG Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        RagToggleComponent()
        
        if (isRagEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RAG Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    when (initializationStatus) {
                        RagInitializationStatus.READY -> {
                            Text(
                                text = "✓ Context awareness: Active",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "✓ Vector storage: Initialized",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "✓ Text embedding: Ready",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        RagInitializationStatus.INITIALIZING -> {
                            Text(
                                text = "⏳ Initializing components...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RagInitializationStatus.FAILED -> {
                            Text(
                                text = "❌ Initialization failed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        RagInitializationStatus.NOT_STARTED -> {
                            Text(
                                text = "⚠️ Not initialized",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ViewModel for RAG toggle functionality.
 */
@HiltViewModel
class RagToggleViewModel @Inject constructor(
    private val ragProviderFactory: RagProviderFactory,
    private val ragInitializationService: RagInitializationService
) : ViewModel() {
    
    private val _isRagEnabled = MutableStateFlow(false)
    val isRagEnabled: StateFlow<Boolean> = _isRagEnabled.asStateFlow()
    
    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()
    
    private val _initializationStatus = MutableStateFlow(RagInitializationStatus.NOT_STARTED)
    val initializationStatus: StateFlow<RagInitializationStatus> = _initializationStatus.asStateFlow()
    
    init {
        // Check initial RAG status
        viewModelScope.launch {
            _initializationStatus.value = ragInitializationService.getInitializationStatus()
            _isRagEnabled.value = ragProviderFactory.isEnhancedRagEnabled()
        }
    }
    
    fun toggleRag(enabled: Boolean) {
        if (_isInitializing.value) return
        
        viewModelScope.launch {
            _isInitializing.value = true
            try {
                if (enabled) {
                    _initializationStatus.value = RagInitializationStatus.INITIALIZING
                    val success = ragInitializationService.initializeSync()
                    if (success) {
                        ragProviderFactory.setUseEnhancedRag(true)
                        _isRagEnabled.value = true
                        _initializationStatus.value = RagInitializationStatus.READY
                    } else {
                        _isRagEnabled.value = false
                        _initializationStatus.value = RagInitializationStatus.FAILED
                    }
                } else {
                    ragProviderFactory.setUseEnhancedRag(false)
                    _isRagEnabled.value = false
                    _initializationStatus.value = RagInitializationStatus.NOT_STARTED
                }
            } catch (e: Exception) {
                _isRagEnabled.value = false
                _initializationStatus.value = RagInitializationStatus.FAILED
            } finally {
                _isInitializing.value = false
            }
        }
    }
    
    /**
     * Check if RAG can provide context for a given chat context.
     */
    fun canProvideContext(chatContext: ChatContext): Boolean {
        return _isRagEnabled.value && 
               !_isInitializing.value && 
               _initializationStatus.value == RagInitializationStatus.READY
    }
}

