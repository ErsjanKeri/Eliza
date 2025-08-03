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

package com.example.ai.edge.eliza.ai.rag.di

import android.content.Context
import androidx.room.Room
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory
import com.example.ai.edge.eliza.ai.rag.RagProviderFactoryImpl
import com.example.ai.edge.eliza.ai.rag.data.ContentChunkDao
import com.example.ai.edge.eliza.ai.rag.data.VectorIndexMetadataDao
import com.example.ai.edge.eliza.ai.rag.data.VectorStorageDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing enhanced RAG dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RagModule {
    
    @Binds
    abstract fun bindRagProviderFactory(
        ragProviderFactoryImpl: RagProviderFactoryImpl
    ): RagProviderFactory
    
    companion object {
        
        @Provides
        @Singleton
        fun provideVectorStorageDatabase(
            @ApplicationContext context: Context
        ): VectorStorageDatabase {
            return Room.databaseBuilder(
                context,
                VectorStorageDatabase::class.java,
                "vector_storage_database"
            )
            .fallbackToDestructiveMigration() // For development only
            .build()
        }
        
        @Provides
        fun provideContentChunkDao(
            database: VectorStorageDatabase
        ): ContentChunkDao {
            return database.contentChunkDao()
        }
        
        @Provides
        fun provideVectorIndexMetadataDao(
            database: VectorStorageDatabase
        ): VectorIndexMetadataDao {
            return database.vectorIndexMetadataDao()
        }
    }
}