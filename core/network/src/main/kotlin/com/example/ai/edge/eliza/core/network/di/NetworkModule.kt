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

package com.example.ai.edge.eliza.core.network.di

import com.example.ai.edge.eliza.core.network.BuildConfig
import com.example.ai.edge.eliza.core.network.VideoService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing network layer dependencies.
 * Configures Retrofit with OkHttp and kotlinx serialization for ElizaServer API communication.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the base URL for ElizaServer API.
     * Configured through local.properties for flexible development environments.
     */
    private val BASE_URL = BuildConfig.ELIZA_SERVER_URL

    /**
     * Provides a configured Json instance for serialization.
     * Configured to ignore unknown keys for robust parsing.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Provides a configured OkHttpClient for network requests.
     * Configured with appropriate timeouts for video generation API.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS) // Long timeout for video downloads
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Provides a configured Retrofit instance for ElizaServer API.
     * Uses kotlinx serialization for JSON parsing.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * Provides the VideoService interface implementation.
     * This service handles all video generation API calls to ElizaServer.
     */
    @Provides
    @Singleton
    fun provideVideoService(retrofit: Retrofit): VideoService = 
        retrofit.create(VideoService::class.java)
}