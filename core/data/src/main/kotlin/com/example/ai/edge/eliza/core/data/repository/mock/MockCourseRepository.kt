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
import com.example.ai.edge.eliza.core.model.Chapter
import com.example.ai.edge.eliza.core.model.Subject
import com.example.ai.edge.eliza.core.model.Trial
import com.example.ai.edge.eliza.core.model.VideoExplanation
import com.example.ai.edge.eliza.core.model.ExerciseHelp
import com.example.ai.edge.eliza.core.model.VideoRequestType
import com.example.ai.edge.eliza.core.model.HelpType
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
 * UPDATED: Renamed lesson references to chapters and added video explanation support.
 * Similar to how Gallery uses model_allowlist.json for model management.
 */
@Singleton
class MockCourseRepository @Inject constructor() : CourseRepository {
    
    // Mock data storage
    private val courses = mutableListOf<Course>()
    private val chapters = mutableListOf<Chapter>() // RENAMED from lessons
    private val exercises = mutableListOf<Exercise>()
    private val trials = mutableListOf<Trial>()
    private val videoExplanations = mutableListOf<VideoExplanation>() // NEW
    private val exerciseHelp = mutableListOf<ExerciseHelp>() // NEW
    
    init {
        // Initialize mock data
        initializeMockData()
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
        chapters.removeIf { it.courseId == courseId }
    }
    
    override suspend fun downloadCourse(courseId: String): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(courseId, 0f, DownloadStatus.PENDING))
        delay(500)
        emit(DownloadProgress(courseId, 0.1f, DownloadStatus.DOWNLOADING))
        delay(1000)
        emit(DownloadProgress(courseId, 0.5f, DownloadStatus.DOWNLOADING))
        delay(1000)
        emit(DownloadProgress(courseId, 1.0f, DownloadStatus.COMPLETED))
        
        // Mark course as downloaded
        courses.find { it.id == courseId }?.let { course ->
            updateCourse(course.copy(isDownloaded = true))
        }
    }
    
    // Chapter operations (RENAMED from lesson operations)
    override fun getChaptersByCourse(courseId: String): Flow<List<Chapter>> =
        flowOf(chapters.filter { it.courseId == courseId }.sortedBy { it.chapterNumber })
    
    override fun getChapterById(chapterId: String): Flow<Chapter?> =
        flowOf(chapters.find { it.id == chapterId })
    
    override fun getChapterByNumber(courseId: String, chapterNumber: Int): Flow<Chapter?> =
        flowOf(chapters.find { it.courseId == courseId && it.chapterNumber == chapterNumber })
    
    override suspend fun insertChapter(chapter: Chapter) {
        chapters.removeIf { it.id == chapter.id }
        chapters.add(chapter)
    }
    
    override suspend fun updateChapter(chapter: Chapter) {
        val index = chapters.indexOfFirst { it.id == chapter.id }
        if (index != -1) {
            chapters[index] = chapter
        }
    }
    
    override suspend fun deleteChapter(chapterId: String) {
        chapters.removeIf { it.id == chapterId }
        exercises.removeIf { it.chapterId == chapterId }
    }
    
    override suspend fun markChapterCompleted(chapterId: String) {
        chapters.find { it.id == chapterId }?.let { chapter ->
            updateChapter(chapter.copy(isCompleted = true))
        }
    }
    
    // Exercise operations
    override fun getExercisesByChapter(chapterId: String): Flow<List<Exercise>> =
        flowOf(exercises.filter { it.chapterId == chapterId })
    
    override fun getExerciseById(exerciseId: String): Flow<Exercise?> =
        flowOf(exercises.find { it.id == exerciseId })
    
    override fun getIncompleteExercises(chapterId: String): Flow<List<Exercise>> =
        flowOf(exercises.filter { it.chapterId == chapterId && !it.isCompleted })
    
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
        val exercise = exercises.find { it.id == exerciseId }
            ?: throw IllegalArgumentException("Exercise not found")
        
        val isCorrect = answerIndex == exercise.correctAnswerIndex
        
        // Update exercise with user's answer
        updateExercise(exercise.copy(
            userAnswer = answerIndex,
            isCorrect = isCorrect,
            isCompleted = true
        ))
        
        return ExerciseResult(
            exerciseId = exerciseId,
            isCorrect = isCorrect,
            selectedAnswer = answerIndex,
            correctAnswer = exercise.correctAnswerIndex,
            explanation = exercise.explanation,
            timeSpent = 30000L, // Mock 30 seconds
            hintsUsed = 0
        )
    }
    
    // Trial operations
    override fun getTrialsByExercise(exerciseId: String): Flow<List<Trial>> =
        flowOf(trials.filter { it.originalExerciseId == exerciseId })
    
    override fun getTrialById(trialId: String): Flow<Trial?> =
        flowOf(trials.find { it.id == trialId })
    
    override suspend fun generateTrialsForExercise(exerciseId: String, count: Int): List<Trial> {
        val exercise = exercises.find { it.id == exerciseId }
            ?: throw IllegalArgumentException("Exercise not found")
        
        val generatedTrials = (1..count).map { i ->
            Trial(
                id = "trial_${exerciseId}_$i",
                originalExerciseId = exerciseId,
                questionText = "Generated practice question $i based on: ${exercise.questionText}",
                options = exercise.options.shuffled(),
                correctAnswerIndex = (0..exercise.options.size - 1).random(),
                explanation = "Generated explanation for trial $i",
                difficulty = exercise.difficulty
            )
        }
        
        trials.addAll(generatedTrials)
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
        val trial = trials.find { it.id == trialId }
            ?: throw IllegalArgumentException("Trial not found")
        
        val isCorrect = answerIndex == trial.correctAnswerIndex
        
        updateTrial(trial.copy(
            userAnswer = answerIndex,
            isCorrect = isCorrect,
            isCompleted = true
        ))
        
        return TrialResult(
            trialId = trialId,
            isCorrect = isCorrect,
            selectedAnswer = answerIndex,
            correctAnswer = trial.correctAnswerIndex,
            explanation = trial.explanation,
            timeSpent = 25000L, // Mock 25 seconds
            hintsUsed = 0
        )
    }
    
    // NEW: Video explanation operations
    override fun getVideoExplanationsByUser(userId: String): Flow<List<VideoExplanation>> =
        flowOf(videoExplanations.filter { it.userId == userId })
    
    override fun getVideoExplanationsByChapter(chapterId: String, userId: String): Flow<List<VideoExplanation>> =
        flowOf(videoExplanations.filter { it.chapterId == chapterId && it.userId == userId })
    
    override fun getVideoExplanationsByExercise(exerciseId: String, userId: String): Flow<List<VideoExplanation>> =
        flowOf(videoExplanations.filter { it.exerciseId == exerciseId && it.userId == userId })
    
    override suspend fun insertVideoExplanation(videoExplanation: VideoExplanation) {
        videoExplanations.removeIf { it.id == videoExplanation.id }
        videoExplanations.add(videoExplanation)
    }
    
    override suspend fun deleteVideoExplanation(videoId: String) {
        videoExplanations.removeIf { it.id == videoId }
    }
    
    override suspend fun updateVideoLastAccessed(videoId: String) {
        videoExplanations.find { it.id == videoId }?.let { video ->
            val updatedVideo = video.copy(lastAccessedAt = System.currentTimeMillis())
            videoExplanations.removeIf { it.id == videoId }
            videoExplanations.add(updatedVideo)
        }
    }
    
    // NEW: Exercise help operations
    override fun getExerciseHelpByExercise(exerciseId: String, userId: String): Flow<List<ExerciseHelp>> =
        flowOf(exerciseHelp.filter { it.exerciseId == exerciseId && it.userId == userId })
    
    override suspend fun insertExerciseHelp(help: ExerciseHelp) {
        exerciseHelp.removeIf { it.id == help.id }
        exerciseHelp.add(help)
    }
    
    override suspend fun updateExerciseHelpFeedback(helpId: String, wasHelpful: Boolean) {
        exerciseHelp.find { it.id == helpId }?.let { help ->
            val updatedHelp = help.copy(wasHelpful = wasHelpful)
            exerciseHelp.removeIf { it.id == helpId }
            exerciseHelp.add(updatedHelp)
        }
    }
    
    // Aggregate operations
    override suspend fun getCourseProgress(courseId: String): CourseProgress {
        val courseChapters = chapters.filter { it.courseId == courseId }
        val courseExercises = exercises.filter { chapter -> 
            courseChapters.any { it.id == chapter.chapterId }
        }
        
        return CourseProgress(
            courseId = courseId,
            completedChapters = courseChapters.count { it.isCompleted },
            totalChapters = courseChapters.size,
            completedExercises = courseExercises.count { it.isCompleted },
            totalExercises = courseExercises.size,
            averageScore = 85.5f, // Mock average
            timeSpent = 3600000L, // Mock 1 hour
            lastStudied = System.currentTimeMillis()
        )
    }
    
    override suspend fun refreshCourseContent(courseId: String) {
        // Mock refresh - simulate network delay
        delay(1000)
    }
    
    override suspend fun syncCourseProgress(courseId: String) {
        // Mock sync - simulate network delay
        delay(500)
    }
    
    // Initialize mock data
    private fun initializeMockData() {
        // Create mock courses
        val algebraCourse = Course(
            id = "course_algebra_1",
            title = "Algebra I Fundamentals",
            subject = Subject.ALGEBRA,
            grade = "9th Grade",
            description = "Master the fundamentals of algebra including linear equations, polynomials, and factoring.",
            chapters = emptyList(), // Will be populated separately
            totalChapters = 3,
            estimatedHours = 20,
            imageUrl = null,
            isDownloaded = true
        )
        
        val geometryCourse = Course(
            id = "course_geometry_1",
            title = "Geometry Basics",
            subject = Subject.GEOMETRY,
            grade = "10th Grade",
            description = "Learn about shapes, angles, area, and volume in this comprehensive geometry course.",
            chapters = emptyList(),
            totalChapters = 2,
            estimatedHours = 15,
            imageUrl = null,
            isDownloaded = false
        )
        
        // NEW: Add a third course that user has made significant progress in
        val calculusCourse = Course(
            id = "course_calculus_1",
            title = "Introduction to Calculus",
            subject = Subject.CALCULUS,
            grade = "11th Grade",
            description = "Explore the fundamentals of differential and integral calculus with practical applications.",
            chapters = emptyList(),
            totalChapters = 5,
            estimatedHours = 30,
            imageUrl = null,
            isDownloaded = true
        )
        
        // NEW: Add courses that haven't been started yet for "Start New Course" tab
        val trigonometryCourse = Course(
            id = "course_trigonometry_1",
            title = "Trigonometry Essentials",
            subject = Subject.TRIGONOMETRY,
            grade = "10th Grade", 
            description = "Master sine, cosine, tangent and their applications in solving triangles and modeling periodic phenomena.",
            chapters = emptyList(),
            totalChapters = 4,
            estimatedHours = 18,
            imageUrl = null,
            isDownloaded = false
        )
        
        val statisticsCourse = Course(
            id = "course_statistics_1",
            title = "Introduction to Statistics",
            subject = Subject.STATISTICS,
            grade = "11th Grade",
            description = "Learn data analysis, probability, and statistical inference to make informed decisions from data.",
            chapters = emptyList(),
            totalChapters = 6,
            estimatedHours = 25,
            imageUrl = null,
            isDownloaded = false
        )
        
        val algebraAdvancedCourse = Course(
            id = "course_algebra_2",
            title = "Advanced Algebra",
            subject = Subject.ALGEBRA,
            grade = "11th Grade",
            description = "Dive deeper into polynomial functions, logarithms, and exponential equations for advanced problem solving.",
            chapters = emptyList(),
            totalChapters = 7,
            estimatedHours = 28,
            imageUrl = null,
            isDownloaded = true
        )
        
        courses.addAll(listOf(algebraCourse, geometryCourse, calculusCourse, trigonometryCourse, statisticsCourse, algebraAdvancedCourse))
        
        // Create mock chapters
        val linearEquationsChapter = Chapter(
            id = "chapter_linear_eq",
            courseId = "course_algebra_1",
            chapterNumber = 1,
            title = "Linear Equations",
            markdownContent = """
                # Linear Equations
                
                A linear equation is an equation that makes a straight line when graphed.
                
                ## Standard Form
                The standard form of a linear equation is: **ax + b = c**
                
                ## Examples
                1. Solve: 2x + 5 = 15
                   - Subtract 5 from both sides: 2x = 10
                   - Divide by 2: x = 5
                
                2. Solve: 3x - 7 = 8
                   - Add 7 to both sides: 3x = 15
                   - Divide by 3: x = 5
            """.trimIndent(),
            estimatedReadingTime = 15,
            isCompleted = false
        )
        
        val quadraticsChapter = Chapter(
            id = "chapter_quadratics",
            courseId = "course_algebra_1",
            chapterNumber = 2,
            title = "Quadratic Equations",
            markdownContent = """
                # Quadratic Equations
                
                A quadratic equation is a polynomial equation of degree 2.
                
                ## Standard Form
                The standard form is: **ax² + bx + c = 0**
                
                ## Solving Methods
                1. Factoring
                2. Quadratic Formula
                3. Completing the Square
            """.trimIndent(),
            estimatedReadingTime = 20,
            isCompleted = false
        )
        
        // NEW: Add chapters for calculus course to match progress data
        val limitsChapter = Chapter(
            id = "chapter_calc_limits",
            courseId = "course_calculus_1",
            chapterNumber = 1,
            title = "Limits and Continuity",
            markdownContent = """
                # Limits and Continuity
                
                Limits are the foundation of calculus, describing what happens to a function as its input approaches a particular value.
                
                ## Definition of a Limit
                The limit of f(x) as x approaches a is L if f(x) gets arbitrarily close to L as x gets arbitrarily close to a.
                
                ## Notation
                We write: **lim[x→a] f(x) = L**
                
                ## Examples
                1. lim[x→2] (3x + 1) = 7
                2. lim[x→0] (sin x)/x = 1
            """.trimIndent(),
            estimatedReadingTime = 25,
            isCompleted = true
        )
        
        val derivativesChapter = Chapter(
            id = "chapter_calc_derivatives",
            courseId = "course_calculus_1",
            chapterNumber = 2,
            title = "Introduction to Derivatives",
            markdownContent = """
                # Introduction to Derivatives
                
                The derivative measures how a function changes as its input changes.
                
                ## Definition
                The derivative of f(x) is: **f'(x) = lim[h→0] [f(x+h) - f(x)]/h**
                
                ## Basic Rules
                1. Power Rule: d/dx(x^n) = nx^(n-1)
                2. Product Rule: d/dx(fg) = f'g + fg'
                3. Chain Rule: d/dx(f(g(x))) = f'(g(x)) × g'(x)
                
                ## Applications
                - Finding slopes of tangent lines
                - Optimization problems
                - Rate of change calculations
            """.trimIndent(),
            estimatedReadingTime = 30,
            isCompleted = true
        )
        
        val applicationsChapter = Chapter(
            id = "chapter_calc_applications",
            courseId = "course_calculus_1",
            chapterNumber = 3,
            title = "Applications of Derivatives",
            markdownContent = """
                # Applications of Derivatives
                
                Derivatives have many practical applications in solving real-world problems.
                
                ## Optimization
                Find maximum and minimum values of functions to solve problems like:
                - Maximizing profit
                - Minimizing cost
                - Finding optimal dimensions
                
                ## Related Rates
                Solving problems where multiple quantities change with respect to time.
                
                ## Example
                A balloon is being inflated. If the radius increases at 2 cm/min, how fast is the volume changing when r = 10 cm?
            """.trimIndent(),
            estimatedReadingTime = 28,
            isCompleted = true
        )
        
        chapters.addAll(listOf(linearEquationsChapter, quadraticsChapter, limitsChapter, derivativesChapter, applicationsChapter))
        
        // Create mock exercises
        val linearExercise1 = Exercise(
            id = "exercise_linear_1",
            chapterId = "chapter_linear_eq",
            questionText = "Solve for x: 2x + 7 = 19",
            options = listOf("x = 5", "x = 6", "x = 7", "x = 8"),
            correctAnswerIndex = 1, // x = 6
            explanation = "Subtract 7 from both sides: 2x = 12. Then divide by 2: x = 6.",
            difficulty = Difficulty.EASY
        )
        
        val linearExercise2 = Exercise(
            id = "exercise_linear_2",
            chapterId = "chapter_linear_eq",
            questionText = "What is the slope of the line 3x + 4y = 12?",
            options = listOf("-3/4", "3/4", "-4/3", "4/3"),
            correctAnswerIndex = 0, // -3/4
            explanation = "Rewrite in slope-intercept form: y = -3/4 x + 3. The slope is -3/4.",
            difficulty = Difficulty.MEDIUM
        )
        
        exercises.addAll(listOf(linearExercise1, linearExercise2))
        
        // Create mock video explanations
        val mockVideo1 = VideoExplanation(
            id = "video_linear_basics",
            userId = "user_demo",
            chapterId = "chapter_linear_eq",
            exerciseId = null,
            requestType = VideoRequestType.CHAPTER_EXPLANATION,
            userQuestion = "Can you explain linear equations visually?",
            contextData = linearEquationsChapter.markdownContent,
            videoUrl = "https://example.com/video1.mp4",
            localFilePath = "/storage/videos/video1.mp4",
            fileSizeBytes = 1500000L, // 1.5MB
            durationSeconds = 180 // 3 minutes
        )
        
        videoExplanations.add(mockVideo1)
        
        // Create mock exercise help
        val mockHelp1 = ExerciseHelp(
            id = "help_linear_1",
            exerciseId = "exercise_linear_1",
            userId = "user_demo",
            incorrectAnswer = 0, // User chose x = 5
            correctAnswer = 1, // Correct answer is x = 6
            userQuestion = "Why isn't x = 5 correct?",
            helpType = HelpType.LOCAL_AI,
            explanation = "You correctly subtracted 7, but when you have 2x = 12, you need to divide both sides by 2, which gives x = 6, not x = 5.",
            videoExplanation = null,
            wasHelpful = true
        )
        
        exerciseHelp.add(mockHelp1)
    }
} 