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

package com.example.ai.edge.eliza.core.data.chat

import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository
import com.example.ai.edge.eliza.core.model.Subject
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RAG provider for chapter reading context.
 * Retrieves relevant content from the current chapter and lesson.
 */
class ChapterRagProvider @Inject constructor(
    private val courseRepository: CourseRepository
) : RagProvider {

    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return when (context) {
            is ChatContext.ChapterContext -> {
                // Search within the current chapter content
                val lesson = courseRepository.getLessonById(context.lessonId).first()
                val course = courseRepository.getCourseById(context.courseId).first()
                
                val chunks = mutableListOf<ContentChunk>()
                
                // Search in lesson content
                lesson?.let { lessonData ->
                    val contentSections = lessonData.content.split("\n\n")
                    contentSections.forEachIndexed { index, section ->
                        if (section.contains(query, ignoreCase = true)) {
                            chunks.add(ContentChunk(
                                id = "lesson-${lessonData.id}-section-$index",
                                title = lessonData.title,
                                content = section,
                                source = "lesson-${lessonData.id}",
                                relevanceScore = calculateRelevanceScore(query, section),
                                chunkType = ContentChunkType.CHAPTER_SECTION
                            ))
                        }
                    }
                }
                
                // Search in course examples
                course?.let { courseData ->
                    val examples = courseRepository.getExamplesByCourse(courseData.id).first()
                    examples.forEach { example ->
                        if (example.content.contains(query, ignoreCase = true)) {
                            chunks.add(ContentChunk(
                                id = "example-${example.id}",
                                title = example.title,
                                content = example.content,
                                source = "course-${courseData.id}",
                                relevanceScore = calculateRelevanceScore(query, example.content),
                                chunkType = ContentChunkType.EXAMPLE
                            ))
                        }
                    }
                }
                
                chunks.sortedByDescending { it.relevanceScore }.take(maxChunks)
            }
            else -> emptyList()
        }
    }

    override suspend fun enhancePrompt(prompt: String, context: ChatContext): String {
        val relevantContent = getRelevantContent(prompt, context)
        if (relevantContent.isEmpty()) return prompt
        
        return when (context) {
            is ChatContext.ChapterContext -> {
                buildString {
                    append("You are an AI tutor helping with ${context.subject} in the lesson '${context.chapterTitle}'.\n\n")
                    append("Context from current lesson:\n")
                    relevantContent.forEach { chunk ->
                        append("- ${chunk.title}: ${chunk.content}\n")
                    }
                    append("\nStudent's question: $prompt")
                }
            }
            else -> prompt
        }
    }

    override suspend fun getSystemInstructions(context: ChatContext): String {
        return when (context) {
            is ChatContext.ChapterContext -> {
                """
                You are an AI tutor specializing in ${context.subject}. The student is currently reading 
                "${context.chapterTitle}" and may have questions about the content. 
                
                Guidelines:
                - Provide clear, educational explanations
                - Reference the current lesson content when relevant
                - Use step-by-step explanations for problems
                - Encourage active learning and understanding
                - If the question is outside the current lesson, gently guide back to the topic
                """.trimIndent()
            }
            else -> "You are a helpful AI tutor."
        }
    }

    private fun calculateRelevanceScore(query: String, content: String): Float {
        val queryWords = query.lowercase().split(" ")
        val contentWords = content.lowercase().split(" ")
        
        val matches = queryWords.count { queryWord ->
            contentWords.any { contentWord ->
                contentWord.contains(queryWord) || queryWord.contains(contentWord)
            }
        }
        
        return matches.toFloat() / queryWords.size.toFloat()
    }
}

/**
 * RAG provider for revision context.
 * Generates explanations and practice problems based on wrong answers.
 */
class RevisionRagProvider @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : RagProvider {

    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return when (context) {
            is ChatContext.RevisionContext -> {
                val chunks = mutableListOf<ContentChunk>()
                
                // Get relevant exercises and explanations
                val exercises = courseRepository.getExercisesByLesson(context.lessonId).first()
                exercises.forEach { exercise ->
                    if (exercise.explanation.contains(query, ignoreCase = true)) {
                        chunks.add(ContentChunk(
                            id = "exercise-${exercise.id}",
                            title = "Exercise: ${exercise.question}",
                            content = exercise.explanation,
                            source = "exercise-${exercise.id}",
                            relevanceScore = calculateRelevanceScore(query, exercise.explanation),
                            chunkType = ContentChunkType.EXPLANATION
                        ))
                    }
                }
                
                // Get concept explanations for review topics
                context.conceptsToReview.forEach { concept ->
                    if (concept.contains(query, ignoreCase = true)) {
                        chunks.add(ContentChunk(
                            id = "concept-$concept",
                            title = "Concept: $concept",
                            content = "Review focus: $concept",
                            source = "revision-concepts",
                            relevanceScore = 0.8f,
                            chunkType = ContentChunkType.CONCEPT_OVERVIEW
                        ))
                    }
                }
                
                chunks.sortedByDescending { it.relevanceScore }.take(maxChunks)
            }
            else -> emptyList()
        }
    }

    override suspend fun enhancePrompt(prompt: String, context: ChatContext): String {
        return when (context) {
            is ChatContext.RevisionContext -> {
                buildString {
                    append("You are helping a student with revision after they got some answers wrong.\n\n")
                    append("Wrong answers in ${context.subject}:\n")
                    context.wrongAnswers.forEach { answer ->
                        append("- Question: ${answer.questionText}\n")
                        append("- Student's answer: ${answer.answer}\n")
                        append("- Correct answer: ${answer.correctAnswer}\n")
                    }
                    append("\nConcepts to review: ${context.conceptsToReview.joinToString(", ")}\n")
                    append("\nStudent's question: $prompt")
                }
            }
            else -> prompt
        }
    }

    override suspend fun getSystemInstructions(context: ChatContext): String {
        return when (context) {
            is ChatContext.RevisionContext -> {
                """
                You are an AI tutor helping with revision in ${context.subject}. The student has made 
                mistakes and needs to review specific concepts.
                
                Guidelines:
                - Focus on explaining WHY answers were wrong
                - Provide clear step-by-step corrections
                - Generate similar practice problems
                - Identify common mistake patterns
                - Build confidence through understanding
                - Adapt difficulty to ${context.difficultyLevel} level
                """.trimIndent()
            }
            else -> "You are a helpful AI tutor."
        }
    }

    private fun calculateRelevanceScore(query: String, content: String): Float {
        val queryWords = query.lowercase().split(" ")
        val contentWords = content.lowercase().split(" ")
        
        val matches = queryWords.count { queryWord ->
            contentWords.any { contentWord ->
                contentWord.contains(queryWord) || queryWord.contains(contentWord)
            }
        }
        
        return matches.toFloat() / queryWords.size.toFloat()
    }
}

/**
 * RAG provider for general context.
 * Provides open-ended tutoring support.
 */
class GeneralRagProvider @Inject constructor(
    private val courseRepository: CourseRepository
) : RagProvider {

    override suspend fun getRelevantContent(
        query: String,
        context: ChatContext,
        maxChunks: Int
    ): List<ContentChunk> {
        return when (context) {
            is ChatContext.GeneralContext -> {
                val chunks = mutableListOf<ContentChunk>()
                
                // Search across all courses if subject is specified
                context.subject?.let { subject ->
                    val courses = courseRepository.getCoursesBySubject(subject).first()
                    courses.forEach { course ->
                        val lessons = courseRepository.getLessonsByCourse(course.id).first()
                        lessons.forEach { lesson ->
                            if (lesson.content.contains(query, ignoreCase = true)) {
                                chunks.add(ContentChunk(
                                    id = "general-lesson-${lesson.id}",
                                    title = "${course.title} - ${lesson.title}",
                                    content = lesson.content.take(200) + "...",
                                    source = "course-${course.id}",
                                    relevanceScore = calculateRelevanceScore(query, lesson.content),
                                    chunkType = ContentChunkType.CHAPTER_SECTION
                                ))
                            }
                        }
                    }
                }
                
                chunks.sortedByDescending { it.relevanceScore }.take(maxChunks)
            }
            else -> emptyList()
        }
    }

    override suspend fun enhancePrompt(prompt: String, context: ChatContext): String {
        return when (context) {
            is ChatContext.GeneralContext -> {
                buildString {
                    append("You are an AI tutor providing general help")
                    context.subject?.let { append(" in ${it}") }
                    append(".\n\n")
                    
                    if (context.learningGoals.isNotEmpty()) {
                        append("Student's learning goals: ${context.learningGoals.joinToString(", ")}\n")
                    }
                    
                    context.userLevel?.let { append("Student level: $it\n") }
                    context.preferredDifficulty?.let { append("Preferred difficulty: $it\n") }
                    
                    append("\nStudent's question: $prompt")
                }
            }
            else -> prompt
        }
    }

    override suspend fun getSystemInstructions(context: ChatContext): String {
        return when (context) {
            is ChatContext.GeneralContext -> {
                """
                You are an AI tutor providing general educational support${context.subject?.let { " in $it" } ?: ""}.
                
                Guidelines:
                - Provide clear, comprehensive explanations
                - Use examples and analogies to aid understanding
                - Encourage critical thinking and problem-solving
                - Adapt to the student's level and goals
                - Be patient and supportive
                - Guide students to discover answers themselves when possible
                """.trimIndent()
            }
            else -> "You are a helpful AI tutor."
        }
    }

    private fun calculateRelevanceScore(query: String, content: String): Float {
        val queryWords = query.lowercase().split(" ")
        val contentWords = content.lowercase().split(" ")
        
        val matches = queryWords.count { queryWord ->
            contentWords.any { contentWord ->
                contentWord.contains(queryWord) || queryWord.contains(contentWord)
            }
        }
        
        return matches.toFloat() / queryWords.size.toFloat()
    }
}

/**
 * Factory for creating appropriate RagProvider instances.
 */
@Singleton
class RagProviderFactoryImpl @Inject constructor(
    private val chapterRagProvider: ChapterRagProvider,
    private val revisionRagProvider: RevisionRagProvider,
    private val generalRagProvider: GeneralRagProvider
) : RagProviderFactory {

    override fun createProvider(context: ChatContext): RagProvider {
        return when (context) {
            is ChatContext.ChapterContext -> chapterRagProvider
            is ChatContext.RevisionContext -> revisionRagProvider
            is ChatContext.GeneralContext -> generalRagProvider
            is ChatContext.ExerciseContext -> chapterRagProvider // Reuse chapter provider
        }
    }
} 