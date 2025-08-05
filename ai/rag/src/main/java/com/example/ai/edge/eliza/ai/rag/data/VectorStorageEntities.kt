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

package com.example.ai.edge.eliza.ai.rag.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room entity for storing text chunks with their vector embeddings.
 */
@Entity(tableName = "content_chunks")
data class ContentChunkEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "course_id")
    val courseId: String,
    
    @ColumnInfo(name = "chapter_id") 
    val chapterId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "chunk_type")
    val chunkType: String, // ContentChunkType as string
    
    @ColumnInfo(name = "source")
    val source: String,
    
    @ColumnInfo(name = "start_position")
    val startPosition: Int, // Starting character position in original text
    
    @ColumnInfo(name = "end_position") 
    val endPosition: Int, // Ending character position in original text
    
    @ColumnInfo(name = "token_count")
    val tokenCount: Int, // Approximate token count
    
    @ColumnInfo(name = "embedding")
    val embedding: FloatArray, // Vector embedding of the content
    
    @ColumnInfo(name = "metadata")
    val metadata: Map<String, String>, // Additional metadata
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentChunkEntity

        if (id != other.id) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}

/**
 * DAO for content chunk operations.
 */
@Dao
interface ContentChunkDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: ContentChunkEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<ContentChunkEntity>)
    
    @Query("SELECT * FROM content_chunks WHERE chapter_id = :chapterId ORDER BY start_position ASC")
    suspend fun getChunksByChapter(chapterId: String): List<ContentChunkEntity>
    
    @Query("SELECT * FROM content_chunks WHERE course_id = :courseId ORDER BY chapter_id, start_position ASC")
    suspend fun getChunksByCourse(courseId: String): List<ContentChunkEntity>
    
    @Query("SELECT * FROM content_chunks WHERE id = :chunkId")
    suspend fun getChunkById(chunkId: String): ContentChunkEntity?
    
    @Query("DELETE FROM content_chunks WHERE chapter_id = :chapterId")
    suspend fun deleteChunksByChapter(chapterId: String)
    
    @Query("DELETE FROM content_chunks WHERE course_id = :courseId")
    suspend fun deleteChunksByCourse(courseId: String)
    
    @Query("SELECT COUNT(*) FROM content_chunks WHERE chapter_id = :chapterId")
    suspend fun getChunkCountByChapter(chapterId: String): Int
    
    @Query("SELECT * FROM content_chunks WHERE chunk_type = :chunkType AND chapter_id = :chapterId")
    suspend fun getChunksByTypeAndChapter(chunkType: String, chapterId: String): List<ContentChunkEntity>
    
    @Query("SELECT DISTINCT chapter_id FROM content_chunks WHERE course_id = :courseId")
    suspend fun getIndexedChapterIds(courseId: String): List<String>
    
    @Query("SELECT * FROM content_chunks ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentChunks(limit: Int = 100): List<ContentChunkEntity>
    
    @Query("SELECT * FROM content_chunks ORDER BY course_id, chapter_id, start_position ASC")
    suspend fun getAllChunks(): List<ContentChunkEntity>
}

/**
 * Entity for storing vector search metadata and statistics.
 */
@Entity(tableName = "vector_index_metadata")
data class VectorIndexMetadata(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "index_type")
    val indexType: String, // "course" or "chapter"
    
    @ColumnInfo(name = "target_id")
    val targetId: String, // courseId or chapterId
    
    @ColumnInfo(name = "chunk_count")
    val chunkCount: Int,
    
    @ColumnInfo(name = "embedding_dimension")
    val embeddingDimension: Int,
    
    @ColumnInfo(name = "last_indexed_at")
    val lastIndexedAt: Long,
    
    @ColumnInfo(name = "index_version")
    val indexVersion: Int = 1
)

/**
 * DAO for vector index metadata operations.
 */
@Dao
interface VectorIndexMetadataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: VectorIndexMetadata)
    
    @Query("SELECT * FROM vector_index_metadata WHERE target_id = :targetId AND index_type = :indexType")
    suspend fun getMetadata(targetId: String, indexType: String): VectorIndexMetadata?
    
    @Query("DELETE FROM vector_index_metadata WHERE target_id = :targetId AND index_type = :indexType")
    suspend fun deleteMetadata(targetId: String, indexType: String)
    
    @Query("SELECT * FROM vector_index_metadata ORDER BY last_indexed_at DESC")
    suspend fun getAllMetadata(): List<VectorIndexMetadata>
}

/**
 * Type converters for Room database.
 */
class VectorStorageConverters {
    
    @TypeConverter
    fun fromFloatArray(array: FloatArray): String {
        return Gson().toJson(array.toList())
    }
    
    @TypeConverter
    fun toFloatArray(arrayString: String): FloatArray {
        val type = object : TypeToken<List<Float>>() {}.type
        val list: List<Float> = Gson().fromJson(arrayString, type)
        return list.toFloatArray()
    }
    
    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return Gson().toJson(map)
    }
    
    @TypeConverter
    fun toStringMap(mapString: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(mapString, type) ?: emptyMap()
    }
}

/**
 * Room database for vector storage.
 */
@Database(
    entities = [ContentChunkEntity::class, VectorIndexMetadata::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(VectorStorageConverters::class)
abstract class VectorStorageDatabase : RoomDatabase() {
    abstract fun contentChunkDao(): ContentChunkDao
    abstract fun vectorIndexMetadataDao(): VectorIndexMetadataDao
}