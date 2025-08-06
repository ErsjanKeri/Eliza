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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.edge.eliza.ai.modelmanager.device.DeviceCapabilityChecker
import com.example.ai.edge.eliza.core.common.R
import com.example.ai.edge.eliza.core.designsystem.component.ElizaButton
import com.example.ai.edge.eliza.core.designsystem.component.ElizaOutlinedButton
import com.example.ai.edge.eliza.core.model.Model

/**
 * Memory warning dialog following Gallery's DownloadAndTryButton pattern.
 * Shows device memory analysis and model compatibility warnings.
 */
@Composable
fun MemoryWarningDialog(
    model: Model,
    compatibility: DeviceCapabilityChecker.ModelCompatibility,
    deviceInfo: DeviceCapabilityChecker.DeviceMemoryInfo,
    onProceedAnyway: () -> Unit,
    onSwitchToSaferModel: (() -> Unit)? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        modifier = modifier,
        shape = RoundedCornerShape(0.dp), // Square corners like rest of app
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Memory Warning",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = when (compatibility.riskLevel) {
                    DeviceCapabilityChecker.RiskLevel.DANGEROUS -> "High Memory Usage Detected"
                    DeviceCapabilityChecker.RiskLevel.CRITICAL -> "Memory Warning"
                    DeviceCapabilityChecker.RiskLevel.WARNING -> "High Memory Usage"
                    else -> "Memory Information"
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = when (compatibility.riskLevel) {
                    DeviceCapabilityChecker.RiskLevel.DANGEROUS,
                    DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main warning message
                Text(
                    text = compatibility.recommendation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Device memory information
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.device_information),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    DeviceMemoryInfoRow(
                        label = stringResource(R.string.device_label),
                        value = deviceInfo.deviceModel
                    )
                    
                    DeviceMemoryInfoRow(
                        label = stringResource(R.string.total_ram_label),
                        value = "${String.format("%.1f", deviceInfo.totalMemoryGB)} GB"
                    )
                    
                    DeviceMemoryInfoRow(
                        label = stringResource(R.string.available_ram_label),
                        value = "${String.format("%.1f", deviceInfo.usableMemoryGB)} GB"
                    )
                    
                    DeviceMemoryInfoRow(
                        label = stringResource(R.string.model_requires_label),
                        value = "${String.format("%.1f", (model.estimatedPeakMemoryInBytes ?: 0L) / (1024f * 1024f * 1024f))} GB"
                    )
                    
                    DeviceMemoryInfoRow(
                        label = stringResource(R.string.memory_usage_label),
                        value = "${String.format("%.0f", compatibility.memoryUtilization * 100)}%",
                        valueColor = when (compatibility.riskLevel) {
                            DeviceCapabilityChecker.RiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                            DeviceCapabilityChecker.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                            DeviceCapabilityChecker.RiskLevel.WARNING -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Warnings list
                if (compatibility.warnings.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.potential_issues),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        compatibility.warnings.forEach { warning ->
                            Text(
                                text = "• $warning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cancel button
                ElizaOutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
                
                // Switch to safer model (if available)
                if (onSwitchToSaferModel != null) {
                    ElizaButton(
                        onClick = onSwitchToSaferModel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.use_2b_model))
                    }
                }
                
                // Proceed anyway button
                ElizaOutlinedButton(
                    onClick = onProceedAnyway,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (compatibility.riskLevel) {
                            DeviceCapabilityChecker.RiskLevel.DANGEROUS -> stringResource(R.string.risk_it)
                            DeviceCapabilityChecker.RiskLevel.CRITICAL -> stringResource(R.string.continue_button)
                            else -> stringResource(R.string.proceed_button)
                        }
                    )
x                }
            }
        },
        dismissButton = null // Using custom button layout above
    )
}

/**
 * Helper composable for device memory information rows.
 */
@Composable
private fun DeviceMemoryInfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = valueColor
        )
    }
}

/**
 * Simplified memory warning for quick usage.
 */
@Composable
fun QuickMemoryWarningDialog(
    modelName: String,
    riskLevel: DeviceCapabilityChecker.RiskLevel,
    memoryUsagePercent: Float,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        modifier = modifier,
        shape = RoundedCornerShape(0.dp), // Square corners like rest of app
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.memory_warning_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = when (riskLevel) {
                    DeviceCapabilityChecker.RiskLevel.DANGEROUS -> stringResource(R.string.high_memory_usage_detected)
                    DeviceCapabilityChecker.RiskLevel.CRITICAL -> stringResource(R.string.memory_warning_title)
                    else -> stringResource(R.string.high_memory_usage)
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.memory_usage_warning,
                    modelName,
                    memoryUsagePercent,
                    when (riskLevel) {
                        DeviceCapabilityChecker.RiskLevel.DANGEROUS -> stringResource(R.string.memory_crash_warning)
                        DeviceCapabilityChecker.RiskLevel.CRITICAL -> stringResource(R.string.memory_stability_warning)
                        else -> stringResource(R.string.memory_close_apps_suggestion)
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElizaOutlinedButton(
                    onClick = onCancel
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
                
                ElizaButton(
                    onClick = onProceed
                ) {
                    Text(stringResource(R.string.continue_button))
                }
            }
        }
    )
}