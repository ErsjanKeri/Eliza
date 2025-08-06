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

package com.example.ai.edge.eliza.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.example.ai.edge.eliza.SettingsSerializer
import com.example.ai.edge.eliza.core.data.repository.DataStoreRepository
import com.example.ai.edge.eliza.data.DefaultDataStoreRepository
import com.example.ai.edge.eliza.proto.ElizaSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

  // Provides the SettingsSerializer
  @Provides
  @Singleton
  fun provideSettingsSerializer(): Serializer<ElizaSettings> {
    return SettingsSerializer
  }

  // Provides DataStore<ElizaSettings>
  @Provides
  @Singleton
  fun provideSettingsDataStore(
    @ApplicationContext context: Context,
    settingsSerializer: Serializer<ElizaSettings>,
  ): DataStore<ElizaSettings> {
    return DataStoreFactory.create(
      serializer = settingsSerializer,
      produceFile = { context.dataStoreFile("eliza_settings.pb") },
    )
  }

  // Provides DataStoreRepository
  @Provides
  @Singleton
  fun provideDataStoreRepository(dataStore: DataStore<ElizaSettings>): DataStoreRepository {
    return DefaultDataStoreRepository(dataStore)
  }
}