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

package com.example.ai.edge.eliza.core.data.di

import com.example.ai.edge.eliza.core.data.repository.ChatRepository
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import com.example.ai.edge.eliza.core.data.repository.mock.MockChatRepository
import com.example.ai.edge.eliza.core.data.repository.mock.MockCourseRepository
import com.example.ai.edge.eliza.core.data.repository.mock.MockProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data layer dependencies.
 * Currently configured to use mock implementations for development.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * Binds the CourseRepository interface to its mock implementation.
     * This provides course data management with realistic dummy data.
     */
    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        mockCourseRepository: MockCourseRepository
    ): CourseRepository

    /**
     * Binds the ChatRepository interface to its mock implementation.
     * This provides AI chat functionality with simulated responses.
     */
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        mockChatRepository: MockChatRepository
    ): ChatRepository

    /**
     * Binds the ProgressRepository interface to its mock implementation.
     * This provides progress tracking and analytics with realistic data.
     */
    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        mockProgressRepository: MockProgressRepository
    ): ProgressRepository
} 