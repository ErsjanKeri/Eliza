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

package com.example.ai.edge.eliza.ai.modelmanager.manager.di

import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper
import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelperImpl
import com.example.ai.edge.eliza.ai.modelmanager.download.ModelDownloadRepository
import com.example.ai.edge.eliza.ai.modelmanager.download.ModelDownloadRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AI ModelManager dependencies.
 * Binds interfaces to their implementations for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ModelManagerModule {

    /**
     * Binds the ModelDownloadRepository interface to its implementation.
     * This handles model downloading with progress tracking.
     */
    @Binds
    @Singleton
    abstract fun bindModelDownloadRepository(
        modelDownloadRepositoryImpl: ModelDownloadRepositoryImpl
    ): ModelDownloadRepository

    /**
     * Binds the ElizaInferenceHelper interface to its implementation.
     * This handles MediaPipe model initialization and inference.
     */
    @Binds
    @Singleton
    abstract fun bindElizaInferenceHelper(
        elizaInferenceHelperImpl: ElizaInferenceHelperImpl
    ): ElizaInferenceHelper

    // Note: ElizaChatService is automatically provided by @Singleton and @Inject
    // No binding needed since it's a concrete class, not an interface
} 