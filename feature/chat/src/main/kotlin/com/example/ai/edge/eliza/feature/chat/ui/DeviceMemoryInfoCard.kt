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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.ai.modelmanager.device.DeviceCapabilityChecker
import com.example.ai.edge.eliza.core.model.Model

/**
 * Device memory information card showing detailed specs and model requirements.
 * Provides advanced users with comprehensive device capability analysis.
 */
@Composable
fun DeviceMemoryInfoCard(
    deviceCapabilityChecker: DeviceCapabilityChecker,
    selectedModel: Model?,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(isExpanded) }
    
    val deviceInfo = deviceCapabilityChecker.getDeviceMemoryInfo(context)
    val compatibility = selectedModel?.let { 
        deviceCapabilityChecker.assessModelCompatibility(context, it) 
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Device Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Always visible summary
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", deviceInfo.usableMemoryGB)} GB Available",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (compatibility != null) {
                    Text(
                        text = "${String.format("%.0f", compatibility.memoryUtilization * 100)}% Usage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (compatibility.riskLevel) {
                            DeviceCapabilityChecker.RiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                            DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                            DeviceCapabilityChecker.RiskLevel.WARNING -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Memory usage bar
            if (compatibility != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { compatibility.memoryUtilization.coerceAtMost(1.0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (compatibility.riskLevel) {
                        DeviceCapabilityChecker.RiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                        DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        DeviceCapabilityChecker.RiskLevel.WARNING -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            // Expandable detailed info
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Device specifications
                    DeviceInfoSection(
                        title = "Device Specifications",
                        icon = Icons.Default.Info
                    ) {
                        DeviceInfoRow("Device", deviceInfo.deviceModel)
                        DeviceInfoRow("Android", deviceInfo.androidVersion)
                        DeviceInfoRow("Low RAM Device", if (deviceInfo.isLowMemoryDevice) "Yes" else "No")
                    }
                    
                    // Memory breakdown
                    DeviceInfoSection(
                        title = "Memory Analysis",
                        icon = Icons.Default.Build
                    ) {
                        DeviceInfoRow(
                            "Total RAM", 
                            "${String.format("%.1f", deviceInfo.totalMemoryGB)} GB"
                        )
                        DeviceInfoRow(
                            "Available RAM", 
                            "${String.format("%.1f", deviceInfo.availableMemoryGB)} GB"
                        )
                        DeviceInfoRow(
                            "Usable RAM", 
                            "${String.format("%.1f", deviceInfo.usableMemoryGB)} GB",
                            subtitle = "After 3GB system reserve"
                        )
                    }
                    
                    // Model requirements (if model selected)
                    if (selectedModel != null && compatibility != null) {
                        DeviceInfoSection(
                            title = "Model Requirements",
                            icon = Icons.Default.Settings
                        ) {
                            DeviceInfoRow(
                                "Model", 
                                selectedModel.name
                            )
                            DeviceInfoRow(
                                "Memory Required", 
                                "${String.format("%.1f", (selectedModel.estimatedPeakMemoryInBytes ?: 0L) / (1024f * 1024f * 1024f))} GB"
                            )
                            DeviceInfoRow(
                                "Memory Usage", 
                                "${String.format("%.1f", compatibility.memoryUtilization * 100)}%",
                                valueColor = when (compatibility.riskLevel) {
                                    DeviceCapabilityChecker.RiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                                    DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                                    DeviceCapabilityChecker.RiskLevel.WARNING -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            DeviceInfoRow(
                                "Risk Level", 
                                compatibility.riskLevel.toString(),
                                valueColor = when (compatibility.riskLevel) {
                                    DeviceCapabilityChecker.RiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                                    DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                                    DeviceCapabilityChecker.RiskLevel.WARNING -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            
                            // Compatibility assessment
                            if (compatibility.warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Warnings:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    compatibility.warnings.forEach { warning ->
                                        Text(
                                            text = "• $warning",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section header for device info groups.
 */
@Composable
private fun DeviceInfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

/**
 * Individual device info row.
 */
@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    subtitle: String? = null,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = valueColor
        )
    }
}