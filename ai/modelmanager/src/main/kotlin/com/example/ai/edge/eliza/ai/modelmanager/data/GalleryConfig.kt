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

package com.example.ai.edge.eliza.ai.modelmanager.data

/**
 * Gallery's exact config creation function
 * Copied exactly from Gallery's Config.kt
 */
fun createLlmChatConfigs(
    defaultMaxToken: Int = DEFAULT_MAX_TOKEN,
    defaultTopK: Int = DEFAULT_TOPK,
    defaultTopP: Float = DEFAULT_TOPP,
    defaultTemperature: Float = DEFAULT_TEMPERATURE,
    accelerators: List<Accelerator> = DEFAULT_ACCELERATORS,
): List<Config> {
    return listOf(
        Config(
            key = CONFIG_MAX_TOKENS,
            defaultValue = defaultMaxToken
        ),
        Config(
            key = CONFIG_TOPK,
            defaultValue = defaultTopK
        ),
        Config(
            key = CONFIG_TOPP,
            defaultValue = defaultTopP
        ),
        Config(
            key = CONFIG_TEMPERATURE,
            defaultValue = defaultTemperature
        ),
        Config(
            key = CONFIG_ACCELERATOR,
            defaultValue = accelerators.first().label
        ),
    )
} 