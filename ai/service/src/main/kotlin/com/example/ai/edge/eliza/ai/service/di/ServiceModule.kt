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

package com.example.ai.edge.eliza.ai.service.di

import com.example.ai.edge.eliza.ai.service.VideoExplanationService
import com.example.ai.edge.eliza.ai.service.VideoExplanationServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing AI service dependencies.
 * Follows the same pattern as RagModule for consistency.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    /**
     * Binds VideoExplanationService interface to its implementation.
     * This provides video explanation functionality with proper DI.
     */
    @Binds
    @Singleton
    abstract fun bindVideoExplanationService(
        videoExplanationServiceImpl: VideoExplanationServiceImpl
    ): VideoExplanationService
}