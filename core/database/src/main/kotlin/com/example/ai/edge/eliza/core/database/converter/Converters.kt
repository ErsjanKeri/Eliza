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

package com.example.ai.edge.eliza.core.database.converter

import androidx.room.TypeConverter
import com.example.ai.edge.eliza.core.database.entity.BoundingBoxEntity
import com.example.ai.edge.eliza.core.database.entity.MathStepEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room database to handle complex data types.
 */
class Converters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromMathStepsList(value: List<MathStepEntity>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toMathStepsList(value: String): List<MathStepEntity> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromBoundingBoxList(value: List<BoundingBoxEntity>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toBoundingBoxList(value: String): List<BoundingBoxEntity> {
        return json.decodeFromString(value)
    }
} 