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

package com.example.ai.edge.eliza.core.data.repository.mock

import com.example.ai.edge.eliza.core.data.repository.CourseProgress
import com.example.ai.edge.eliza.core.data.repository.CourseRepository
import com.example.ai.edge.eliza.core.data.repository.DownloadProgress
import com.example.ai.edge.eliza.core.data.repository.DownloadStatus
import com.example.ai.edge.eliza.core.data.repository.ExerciseResult
import com.example.ai.edge.eliza.core.data.repository.TrialResult
import com.example.ai.edge.eliza.core.model.Course
import com.example.ai.edge.eliza.core.model.Difficulty
import com.example.ai.edge.eliza.core.model.Exercise
import com.example.ai.edge.eliza.core.model.Lesson
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.Trial
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of CourseRepository with hardcoded dummy data.
 * This allows development to proceed while the real backend is being prepared.
 * Similar to how Gallery uses model_allowlist.json for model management.
 */
@Singleton
class MockCourseRepository @Inject constructor() : CourseRepository {
    
    // Mock data storage
    private val courses = mutableListOf<Course>()
    private val lessons = mutableListOf<Lesson>()
    private val exercises = mutableListOf<Exercise>()
    private val trials = mutableListOf<Trial>()
    
    init {
        initializeMockData()
    }
    
    private fun initializeMockData() {
        // Create sample courses
        val algebraCourse = Course(
            id = "course_algebra_101",
            title = "Algebra Fundamentals",
            subject = Subject.ALGEBRA,
            grade = "9th Grade",
            description = "Master the fundamentals of algebra with step-by-step guidance from AI tutor Eliza.",
            lessons = emptyList(), // Will be populated separately
            totalLessons = 8,
            estimatedHours = 24,
            imageUrl = "https://example.com/algebra_course.jpg",
            isDownloaded = true,
            sizeInBytes = 15_000_000L
        )
        
        val geometryCourse = Course(
            id = "course_geometry_101",
            title = "Geometry Basics",
            subject = Subject.GEOMETRY,
            grade = "9th Grade",
            description = "Learn geometric concepts, shapes, and proofs with interactive AI tutoring.",
            lessons = emptyList(),
            totalLessons = 10,
            estimatedHours = 30,
            imageUrl = "https://example.com/geometry_course.jpg",
            isDownloaded = false,
            downloadUrl = "https://api.eliza.ai/courses/geometry_101",
            sizeInBytes = 22_000_000L
        )
        
        val calculusCourse = Course(
            id = "course_calculus_101",
            title = "Calculus Introduction",
            subject = Subject.CALCULUS,
            grade = "12th Grade",
            description = "Introduction to differential and integral calculus with AI-powered explanations.",
            lessons = emptyList(),
            totalLessons = 12,
            estimatedHours = 40,
            imageUrl = "https://example.com/calculus_course.jpg",
            isDownloaded = false,
            downloadUrl = "https://api.eliza.ai/courses/calculus_101",
            sizeInBytes = 35_000_000L
        )
        
        courses.addAll(listOf(algebraCourse, geometryCourse, calculusCourse))
        
        // Create sample lessons for algebra course
        val algebraLessons = listOf(
            Lesson(
                id = "lesson_1",
                courseId = "course_algebra_101",
                lessonNumber = 1,
                title = "Introduction to Variables",
                markdownContent = """
                # Introduction to Variables
                
                ## What is a Variable?
                A variable is a symbol (usually a letter) that represents a number we don't know yet.
                
                ## Examples
                - In the expression **x + 5**, the letter **x** is a variable
                - Variables can represent any number
                - We use variables to write general rules and solve problems
                
                ## Practice
                Try identifying variables in these expressions:
                1. 3y + 7
                2. 2a - 4b
                3. x² + 5x + 6
                """.trimIndent(),
                exercises = emptyList(),
                estimatedReadingTime = 15
            ),
            Lesson(
                id = "lesson_2", 
                courseId = "course_algebra_101",
                lessonNumber = 2,
                title = "Solving Linear Equations",
                markdownContent = """
                # Solving Linear Equations
                
                ## What is a Linear Equation?
                A linear equation is an equation where the variable has a power of 1.
                
                ## Basic Steps
                1. **Isolate the variable** on one side
                2. **Keep the equation balanced** - what you do to one side, do to the other
                3. **Simplify** both sides
                
                ## Example
                Solve: 2x + 3 = 7
                
                Step 1: Subtract 3 from both sides
                2x + 3 - 3 = 7 - 3
                2x = 4
                
                Step 2: Divide both sides by 2
                x = 2
                """.trimIndent(),
                exercises = emptyList(),
                estimatedReadingTime = 20
            )
        )
        
        lessons.addAll(algebraLessons)
        
        // Create sample exercises for the first lesson
        val lessonExercises = listOf(
            Exercise(
                id = "exercise_1",
                lessonId = "lesson_1",
                questionText = "Which of the following is a variable?",
                options = listOf("5", "x", "+", "="),
                correctAnswerIndex = 1,
                explanation = "A variable is a letter that represents an unknown number. In this case, 'x' is the variable.",
                difficulty = Difficulty.EASY
            ),
            Exercise(
                id = "exercise_2",
                lessonId = "lesson_1", 
                questionText = "In the expression '3y + 7', what is the variable?",
                options = listOf("3", "y", "7", "+"),
                correctAnswerIndex = 1,
                explanation = "The variable is 'y' because it represents an unknown number that can change.",
                difficulty = Difficulty.EASY
            ),
            Exercise(
                id = "exercise_3",
                lessonId = "lesson_2",
                questionText = "Solve for x: 2x + 4 = 10",
                options = listOf("x = 2", "x = 3", "x = 4", "x = 5"),
                correctAnswerIndex = 1,
                explanation = "First subtract 4 from both sides: 2x = 6. Then divide by 2: x = 3.",
                difficulty = Difficulty.MEDIUM
            )
        )
        
        exercises.addAll(lessonExercises)
        
        // Create sample trials
        val sampleTrials = listOf(
            Trial(
                id = "trial_1",
                originalExerciseId = "exercise_1",
                questionText = "Which symbol represents a variable in algebra?",
                options = listOf("a", "2", "*", "()"),
                correctAnswerIndex = 0,
                explanation = "Letters like 'a', 'x', 'y' represent variables - unknown numbers we're trying to find.",
                difficulty = Difficulty.EASY
            ),
            Trial(
                id = "trial_2",
                originalExerciseId = "exercise_2",
                questionText = "In '5a - 2', identify the variable:",
                options = listOf("5", "a", "2", "-"),
                correctAnswerIndex = 1,
                explanation = "The variable is 'a' - it's the letter representing the unknown value.",
                difficulty = Difficulty.EASY
            )
        )
        
        trials.addAll(sampleTrials)
    }
    
    // Course operations
    override fun getAllCourses(): Flow<List<Course>> = flowOf(courses)
    
    override fun getCourseById(courseId: String): Flow<Course?> = 
        flowOf(courses.find { it.id == courseId })
    
    override fun getCoursesBySubject(subject: Subject): Flow<List<Course>> = 
        flowOf(courses.filter { it.subject == subject })
    
    override fun getDownloadedCourses(): Flow<List<Course>> = 
        flowOf(courses.filter { it.isDownloaded })
    
    override suspend fun insertCourse(course: Course) {
        courses.removeIf { it.id == course.id }
        courses.add(course)
    }
    
    override suspend fun updateCourse(course: Course) {
        val index = courses.indexOfFirst { it.id == course.id }
        if (index != -1) {
            courses[index] = course
        }
    }
    
    override suspend fun deleteCourse(courseId: String) {
        courses.removeIf { it.id == courseId }
        lessons.removeIf { it.courseId == courseId }
    }
    
    override suspend fun downloadCourse(courseId: String): Flow<DownloadProgress> = flow {
        val course = courses.find { it.id == courseId } ?: return@flow
        
        // Simulate download progress
        emit(DownloadProgress(courseId, 0.0f, DownloadStatus.PENDING))
        delay(500)
        
        emit(DownloadProgress(courseId, 0.0f, DownloadStatus.DOWNLOADING))
        
        // Simulate progress updates
        for (progress in 10..100 step 10) {
            delay(200)
            emit(DownloadProgress(
                courseId = courseId,
                progress = progress / 100f,
                status = DownloadStatus.DOWNLOADING,
                bytesDownloaded = (course.sizeInBytes * progress / 100),
                totalBytes = course.sizeInBytes
            ))
        }
        
        // Mark as downloaded
        updateCourse(course.copy(isDownloaded = true))
        emit(DownloadProgress(courseId, 1.0f, DownloadStatus.COMPLETED))
    }
    
    // Lesson operations
    override fun getLessonsByCourse(courseId: String): Flow<List<Lesson>> = 
        flowOf(lessons.filter { it.courseId == courseId }.sortedBy { it.lessonNumber })
    
    override fun getLessonById(lessonId: String): Flow<Lesson?> = 
        flowOf(lessons.find { it.id == lessonId })
    
    override fun getLessonByNumber(courseId: String, lessonNumber: Int): Flow<Lesson?> = 
        flowOf(lessons.find { it.courseId == courseId && it.lessonNumber == lessonNumber })
    
    override suspend fun insertLesson(lesson: Lesson) {
        lessons.removeIf { it.id == lesson.id }
        lessons.add(lesson)
    }
    
    override suspend fun updateLesson(lesson: Lesson) {
        val index = lessons.indexOfFirst { it.id == lesson.id }
        if (index != -1) {
            lessons[index] = lesson
        }
    }
    
    override suspend fun deleteLesson(lessonId: String) {
        lessons.removeIf { it.id == lessonId }
        exercises.removeIf { it.lessonId == lessonId }
    }
    
    override suspend fun markLessonCompleted(lessonId: String) {
        val lesson = lessons.find { it.id == lessonId }
        if (lesson != null) {
            updateLesson(lesson.copy(isCompleted = true))
        }
    }
    
    // Exercise operations
    override fun getExercisesByLesson(lessonId: String): Flow<List<Exercise>> = 
        flowOf(exercises.filter { it.lessonId == lessonId })
    
    override fun getExerciseById(exerciseId: String): Flow<Exercise?> = 
        flowOf(exercises.find { it.id == exerciseId })
    
    override fun getIncompleteExercises(lessonId: String): Flow<List<Exercise>> = 
        flowOf(exercises.filter { it.lessonId == lessonId && !it.isCompleted })
    
    override suspend fun insertExercise(exercise: Exercise) {
        exercises.removeIf { it.id == exercise.id }
        exercises.add(exercise)
    }
    
    override suspend fun updateExercise(exercise: Exercise) {
        val index = exercises.indexOfFirst { it.id == exercise.id }
        if (index != -1) {
            exercises[index] = exercise
        }
    }
    
    override suspend fun deleteExercise(exerciseId: String) {
        exercises.removeIf { it.id == exerciseId }
        trials.removeIf { it.originalExerciseId == exerciseId }
    }
    
    override suspend fun submitExerciseAnswer(exerciseId: String, answerIndex: Int): ExerciseResult {
        val exercise = exercises.find { it.id == exerciseId } ?: 
            return ExerciseResult(exerciseId, false, answerIndex, 0, "Exercise not found")
        
        val isCorrect = answerIndex == exercise.correctAnswerIndex
        
        // Update exercise
        updateExercise(exercise.copy(
            isCompleted = true,
            userAnswer = answerIndex,
            isCorrect = isCorrect
        ))
        
        return ExerciseResult(
            exerciseId = exerciseId,
            isCorrect = isCorrect,
            selectedAnswer = answerIndex,
            correctAnswer = exercise.correctAnswerIndex,
            explanation = exercise.explanation,
            timeSpent = (Math.random() * 30000).toLong() + 10000 // 10-40 seconds
        )
    }
    
    // Trial operations
    override fun getTrialsByExercise(exerciseId: String): Flow<List<Trial>> = 
        flowOf(trials.filter { it.originalExerciseId == exerciseId })
    
    override fun getTrialById(trialId: String): Flow<Trial?> = 
        flowOf(trials.find { it.id == trialId })
    
    override suspend fun generateTrialsForExercise(exerciseId: String, count: Int): List<Trial> {
        val exercise = exercises.find { it.id == exerciseId } ?: return emptyList()
        
        // Generate mock trials based on the original exercise
        val generatedTrials = mutableListOf<Trial>()
        for (i in 1..count) {
            val trial = Trial(
                id = "trial_${exerciseId}_$i",
                originalExerciseId = exerciseId,
                questionText = "Generated practice: ${exercise.questionText}",
                options = exercise.options.shuffled(),
                correctAnswerIndex = (0 until exercise.options.size).random(),
                explanation = "This is an AI-generated practice question based on the original exercise.",
                difficulty = exercise.difficulty
            )
            generatedTrials.add(trial)
            trials.add(trial)
        }
        
        return generatedTrials
    }
    
    override suspend fun insertTrial(trial: Trial) {
        trials.removeIf { it.id == trial.id }
        trials.add(trial)
    }
    
    override suspend fun updateTrial(trial: Trial) {
        val index = trials.indexOfFirst { it.id == trial.id }
        if (index != -1) {
            trials[index] = trial
        }
    }
    
    override suspend fun deleteTrial(trialId: String) {
        trials.removeIf { it.id == trialId }
    }
    
    override suspend fun submitTrialAnswer(trialId: String, answerIndex: Int): TrialResult {
        val trial = trials.find { it.id == trialId } ?: 
            return TrialResult(trialId, false, answerIndex, 0, "Trial not found")
        
        val isCorrect = answerIndex == trial.correctAnswerIndex
        
        // Update trial
        updateTrial(trial.copy(
            isCompleted = true,
            userAnswer = answerIndex,
            isCorrect = isCorrect
        ))
        
        return TrialResult(
            trialId = trialId,
            isCorrect = isCorrect,
            selectedAnswer = answerIndex,
            correctAnswer = trial.correctAnswerIndex,
            explanation = trial.explanation,
            timeSpent = (Math.random() * 45000).toLong() + 15000 // 15-60 seconds
        )
    }
    
    // Aggregate operations
    override suspend fun getCourseProgress(courseId: String): CourseProgress {
        val courseLessons = lessons.filter { it.courseId == courseId }
        val courseExercises = exercises.filter { lesson -> 
            courseLessons.any { it.id == lesson.lessonId }
        }
        
        val completedLessons = courseLessons.count { it.isCompleted }
        val completedExercises = courseExercises.count { it.isCompleted }
        val correctAnswers = courseExercises.count { it.isCorrect == true }
        
        return CourseProgress(
            courseId = courseId,
            completedLessons = completedLessons,
            totalLessons = courseLessons.size,
            completedExercises = completedExercises,
            totalExercises = courseExercises.size,
            averageScore = if (completedExercises > 0) (correctAnswers.toFloat() / completedExercises) * 100 else 0f,
            timeSpent = (Math.random() * 7200000).toLong() + 1800000, // 30 minutes to 2.5 hours
            lastStudied = System.currentTimeMillis() - (Math.random() * 86400000).toLong() // Last 24 hours
        )
    }
    
    override suspend fun refreshCourseContent(courseId: String) {
        // Simulate refreshing course content
        delay(1000)
    }
    
    override suspend fun syncCourseProgress(courseId: String) {
        // Simulate syncing progress
        delay(500)
    }
} 