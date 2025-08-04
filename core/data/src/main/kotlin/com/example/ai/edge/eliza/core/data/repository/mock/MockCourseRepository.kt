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
         
         // Enhanced logic: isCompleted becomes true only when correct OR was already completed
         // This implements "permanently solved" - once solved, stays solved!
         updateExercise(exercise.copy(
             userAnswer = answerIndex,
             isCorrect = isCorrect,
             isCompleted = isCorrect || exercise.isCompleted  // KEY: Once solved, stays solved
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
     
     override suspend fun resetChapterProgress(chapterId: String) {
         // Reset all exercises for this chapter - clear user progress
         exercises.filter { it.chapterId == chapterId }.forEach { exercise ->
             updateExercise(exercise.copy(
                 userAnswer = null,
                 isCorrect = null,
                 isCompleted = false  // Reset "solved" state
             ))
         }
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
         
         // Update trial with answer
         updateTrial(trial.copy(
             userAnswer = answerIndex,
             isCorrect = isCorrect,
             isCompleted = true
         ))
         
         // Enhanced logic: If trial is correct, mark original exercise as solved too!
         if (isCorrect) {
             val originalExercise = exercises.find { it.id == trial.originalExerciseId }
             if (originalExercise != null) {
                 updateExercise(originalExercise.copy(isCompleted = true))
             }
         }
         
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
             totalChapters = 5,
             estimatedHours = 50,
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
         // Create exercises first so they can be included in chapters
         // Linear Equations Chapter Exercises
         val linearExercises = listOf(
             Exercise(
                 id = "exercise_linear_1",
                 chapterId = "chapter_linear_eq",
                 questionText = "Solve for x: 2x + 7 = 19",
                 options = listOf("x = 5", "x = 6", "x = 7", "x = 8"),
                 correctAnswerIndex = 1,
                 explanation = "Subtract 7 from both sides: 2x = 12. Then divide by 2: x = 6.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_linear_2",
                 chapterId = "chapter_linear_eq",
                 questionText = "What is the slope of the line 3x + 4y = 12?",
                 options = listOf("-3/4", "3/4", "-4/3", "4/3"),
                 correctAnswerIndex = 0,
                 explanation = "Rewrite in slope-intercept form: y = -3/4 x + 3. The slope is -3/4.",
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_linear_3",
                 chapterId = "chapter_linear_eq",
                 questionText = "Which equation represents a line with slope 2 and y-intercept -3?",
                 options = listOf("y = 2x - 3", "y = -3x + 2", "y = 2x + 3", "y = 3x - 2"),
                 correctAnswerIndex = 0,
                 explanation = "The slope-intercept form is y = mx + b, where m is slope and b is y-intercept.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_linear_4",
                 chapterId = "chapter_linear_eq",
                 questionText = "Find the x-intercept of the line 2x - 3y = 6",
                 options = listOf("x = 2", "x = 3", "x = -2", "x = -3"),
                 correctAnswerIndex = 1,
                 explanation = "Set y = 0: 2x - 3(0) = 6, so 2x = 6, therefore x = 3.",
                 difficulty = Difficulty.MEDIUM
             )
         )
         
         // Quadratic Equations Chapter Exercises
         val quadraticExercises = listOf(
             Exercise(
                 id = "exercise_quad_1",
                 chapterId = "chapter_quadratics",
                 questionText = "What are the solutions to x² - 5x + 6 = 0?",
                 options = listOf("x = 2, 3", "x = 1, 6", "x = -2, -3", "x = 2, -3"),
                 correctAnswerIndex = 0,
                 explanation = "Factor: (x - 2)(x - 3) = 0, so x = 2 or x = 3.",
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_quad_2",
                 chapterId = "chapter_quadratics",
                 questionText = "Using the quadratic formula, solve x² + 2x - 3 = 0",
                 options = listOf("x = 1, -3", "x = -1, 3", "x = 1, 3", "x = -1, -3"),
                 correctAnswerIndex = 0,
                 explanation = "x = (-2 ± √(4 + 12))/2 = (-2 ± 4)/2, so x = 1 or x = -3.",
                 difficulty = Difficulty.HARD
             ),
             Exercise(
                 id = "exercise_quad_3",
                 chapterId = "chapter_quadratics",
                 questionText = "What is the vertex of the parabola y = x² - 4x + 3?",
                 options = listOf("(2, -1)", "(2, 1)", "(-2, -1)", "(-2, 1)"),
                 correctAnswerIndex = 0,
                 explanation = "Complete the square: y = (x - 2)² - 1. Vertex is (2, -1).",
                 difficulty = Difficulty.HARD
             )
         )
         
         // Polynomials Chapter Exercises
         val polynomialExercises = listOf(
             Exercise(
                 id = "exercise_poly_1",
                 chapterId = "chapter_polynomials",
                 questionText = "What is the degree of the polynomial 3x⁴ - 2x² + 7x - 1?",
                 options = listOf("3", "4", "2", "1"),
                 correctAnswerIndex = 1,
                 explanation = "The degree is the highest power of x, which is 4.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_poly_2",
                 chapterId = "chapter_polynomials",
                 questionText = "Factor completely: x² - 9",
                 options = listOf("(x - 3)(x + 3)", "(x - 9)(x + 1)", "(x - 3)²", "Cannot be factored"),
                 correctAnswerIndex = 0,
                 explanation = "This is a difference of squares: a² - b² = (a - b)(a + b).",
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_poly_3",
                 chapterId = "chapter_polynomials",
                 questionText = "Add: (2x² + 3x - 1) + (x² - 2x + 4)",
                 options = listOf("3x² + x + 3", "3x² + 5x + 3", "x² + x + 3", "3x² + x - 3"),
                 correctAnswerIndex = 0,
                 explanation = "Combine like terms: (2x² + x²) + (3x - 2x) + (-1 + 4) = 3x² + x + 3.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_poly_4",
                 chapterId = "chapter_polynomials",
                 questionText = "What are the roots of x³ - 6x² + 11x - 6 = 0?",
                 options = listOf("x = 1, 2, 3", "x = 0, 2, 3", "x = 1, 1, 6", "x = -1, -2, -3"),
                 correctAnswerIndex = 0,
                 explanation = "Factor: (x - 1)(x - 2)(x - 3) = 0, so x = 1, 2, or 3.",
                 difficulty = Difficulty.HARD
             )
         )
         
         // Geometry - Basic Shapes Exercises
         val shapesExercises = listOf(
             Exercise(
                 id = "exercise_shapes_1",
                 chapterId = "chapter_geo_shapes",
                 questionText = "What is the area of a rectangle with length 8 and width 5?",
                 options = listOf("40", "26", "13", "20"),
                 correctAnswerIndex = 0,
                 explanation = "Area of rectangle = length × width = 8 × 5 = 40.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_shapes_2",
                 chapterId = "chapter_geo_shapes",
                 questionText = "What is the circumference of a circle with radius 3?",
                 options = listOf("6π", "9π", "3π", "12π"),
                 correctAnswerIndex = 0,
                 explanation = "Circumference = 2πr = 2π(3) = 6π.",
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_shapes_3",
                 chapterId = "chapter_geo_shapes",
                 questionText = "In a triangle, if all three sides are equal, what type of triangle is it?",
                 options = listOf("Equilateral", "Isosceles", "Scalene", "Right"),
                 correctAnswerIndex = 0,
                 explanation = "A triangle with all three sides equal is called equilateral.",
                 difficulty = Difficulty.EASY
             )
         )
         
         // Geometry - Angles Exercises
         val angleExercises = listOf(
             Exercise(
                 id = "exercise_angles_1",
                 chapterId = "chapter_geo_angles",
                 questionText = "What is the complement of a 35° angle?",
                 options = listOf("55°", "145°", "65°", "125°"),
                 correctAnswerIndex = 0,
                 explanation = "Complementary angles sum to 90°: 90° - 35° = 55°.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_angles_2",
                 chapterId = "chapter_geo_angles",
                 questionText = "In a triangle, what is the sum of all interior angles?",
                 options = listOf("180°", "360°", "90°", "270°"),
                 correctAnswerIndex = 0,
                 explanation = "The sum of interior angles in any triangle is always 180°.",
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_angles_3",
                 chapterId = "chapter_geo_angles",
                 questionText = "If two parallel lines are cut by a transversal, what can you say about corresponding angles?",
                 options = listOf("They are equal", "They are supplementary", "They are complementary", "They sum to 270°"),
                 correctAnswerIndex = 0,
                 explanation = "Corresponding angles are equal when parallel lines are cut by a transversal.",
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_angles_4",
                 chapterId = "chapter_geo_angles",
                 questionText = "What type of angle measures exactly 90°?",
                 options = listOf("Right angle", "Acute angle", "Obtuse angle", "Straight angle"),
                 correctAnswerIndex = 0,
                 explanation = "An angle that measures exactly 90° is called a right angle.",
                 difficulty = Difficulty.EASY
             )
         )
 
         val linearEquationsChapter = Chapter(
             id = "chapter_linear_eq",
             courseId = "course_algebra_1",
             chapterNumber = 1,
             title = "Foundations for Algebra",
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
             exercises = linearExercises,
             estimatedReadingTime = 15,
             isCompleted = false
         )
         
         val quadraticsChapter = Chapter(
             id = "chapter_quadratics",
             courseId = "course_algebra_1",
             chapterNumber = 2,
             title = "Quadratic Equations",
             markdownContent = """
 
             """.trimIndent(),
             exercises = quadraticExercises,
             estimatedReadingTime = 20,
             isCompleted = false
         )
         
         // MISSING 3rd chapter for algebra course - this was causing the inconsistency!
         val polynomialsChapter = Chapter(
             id = "chapter_polynomials",
             courseId = "course_algebra_1",
             chapterNumber = 3,
             title = "Polynomials",
             markdownContent = """
                 # Polynomials
                 
                 A polynomial is an expression consisting of variables and coefficients.
                 
                 ## Definition
                 A polynomial in one variable x is: **anx^n + an-1x^(n-1) + ... + a1x + a0**
                 
                 ## Types of Polynomials
                 1. Monomial (1 term): 5x²
                 2. Binomial (2 terms): 3x + 7
                 3. Trinomial (3 terms): x² + 2x + 1
                 
                 ## Operations
                 - Addition and Subtraction
                 - Multiplication
                 - Factoring
                 - Finding Roots
                 
                 ## Examples
                 1. Add: (2x² + 3x + 1) + (x² - x + 4) = 3x² + 2x + 5
                 2. Factor: x² - 5x + 6 = (x - 2)(x - 3)
             """.trimIndent(),
             exercises = polynomialExercises,
             estimatedReadingTime = 25,
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
         
         // MISSING: Create chapters for course_geometry_1 (2 chapters needed)
         val shapesChapter = Chapter(
             id = "chapter_geo_shapes",
             courseId = "course_geometry_1",
             chapterNumber = 1,
             title = "Basic Shapes and Properties",
             markdownContent = """
                 # Basic Shapes and Properties
                 
                 Geometry begins with understanding the fundamental shapes and their properties.
                 
                 ## Points, Lines, and Planes
                 - **Point**: A location in space with no size
                 - **Line**: Extends infinitely in both directions
                 - **Plane**: A flat surface extending infinitely
                 
                 ## Basic Shapes
                 1. Triangle: 3 sides, angles sum to 180°
                 2. Square: 4 equal sides, 4 right angles
                 3. Rectangle: 4 sides, opposite sides equal
                 4. Circle: All points equidistant from center
                 
                 ## Properties
                 - Perimeter: Distance around a shape
                 - Area: Space inside a shape
                 - Angle: Measure of rotation between two lines
             """.trimIndent(),
             exercises = shapesExercises,
             estimatedReadingTime = 20,
             isCompleted = false
         )
         
         val anglesChapter = Chapter(
             id = "chapter_geo_angles",
             courseId = "course_geometry_1",
             chapterNumber = 2,
             title = "Angles and Measurements",
             markdownContent = """
                 # Angles and Measurements
                 
                 Understanding angles is crucial for geometric problem solving.
                 
                 ## Types of Angles
                 1. **Acute**: Less than 90°
                 2. **Right**: Exactly 90°
                 3. **Obtuse**: Between 90° and 180°
                 4. **Straight**: Exactly 180°
                 
                 ## Angle Relationships
                 - Complementary: Two angles that sum to 90°
                 - Supplementary: Two angles that sum to 180°
                 - Vertical angles: Opposite angles when lines intersect
                 
                 ## Applications
                 - Finding missing angles in triangles
                 - Parallel lines and transversals
                 - Circle angle theorems
             """.trimIndent(),
             exercises = angleExercises,
             estimatedReadingTime = 25,
             isCompleted = false
         )
         
         // MISSING: Complete calculus course (2 more chapters needed)
         val integralsChapter = Chapter(
             id = "chapter_calc_integrals",
             courseId = "course_calculus_1",
             chapterNumber = 4,
             title = "Introduction to Integrals",
             markdownContent = """
                 # Introduction to Integrals
                 
                 Integration is the reverse process of differentiation.
                 
                 ## Indefinite Integrals
                 The antiderivative of a function f(x) is F(x) where F'(x) = f(x).
                 
                 ## Basic Integration Rules
                 1. ∫ x^n dx = x^(n+1)/(n+1) + C
                 2. ∫ sin(x) dx = -cos(x) + C
                 3. ∫ cos(x) dx = sin(x) + C
                 4. ∫ e^x dx = e^x + C
                 
                 ## Definite Integrals
                 Represent the area under a curve between two points.
                 
                 ## Applications
                 - Finding areas under curves
                 - Calculating volumes of solids
                 - Physics applications (displacement, work)
             """.trimIndent(),
             estimatedReadingTime = 35,
             isCompleted = false
         )
         
         val calculusApplicationsChapter = Chapter(
             id = "chapter_calc_real_world",
             courseId = "course_calculus_1",
             chapterNumber = 5,
             title = "Real-World Applications",
             markdownContent = """
                 # Real-World Applications of Calculus
                 
                 Calculus is everywhere in science, engineering, and daily life.
                 
                 ## Physics Applications
                 - Motion: position, velocity, acceleration
                 - Work and energy calculations
                 - Wave functions and oscillations
                 
                 ## Economics Applications
                 - Marginal cost and revenue
                 - Optimization of profit
                 - Growth models
                 
                 ## Biology Applications
                 - Population growth models
                 - Rate of chemical reactions
                 - Blood flow calculations
                 
                 ## Engineering Applications
                 - Bridge design and stress analysis
                 - Electrical circuit analysis
                 - Signal processing
             """.trimIndent(),
             estimatedReadingTime = 30,
             isCompleted = false
         )
         
         // MISSING: Create chapters for course_trigonometry_1 (4 chapters needed)
         val trigBasicsChapter = Chapter(
             id = "chapter_trig_basics",
             courseId = "course_trigonometry_1",
             chapterNumber = 1,
             title = "Trigonometric Functions",
             markdownContent = """
                 # Trigonometric Functions
                 
                 Trigonometry studies the relationships between angles and sides in triangles.
                 
                 ## The Unit Circle
                 A circle with radius 1 centered at the origin, fundamental to trigonometry.
                 
                 ## Primary Functions
                 1. **Sine (sin)**: y-coordinate on unit circle
                 2. **Cosine (cos)**: x-coordinate on unit circle  
                 3. **Tangent (tan)**: sin/cos = slope of the radius
                 
                 ## Right Triangle Ratios
                 - sin(θ) = opposite/hypotenuse
                 - cos(θ) = adjacent/hypotenuse
                 - tan(θ) = opposite/adjacent
                 
                 ## Special Angles
                 - 30°, 45°, 60° and their exact values
                 - Quadrant analysis for all angles
             """.trimIndent(),
             estimatedReadingTime = 25,
             isCompleted = false
         )
         
         val trigIdentitiesChapter = Chapter(
             id = "chapter_trig_identities",
             courseId = "course_trigonometry_1",
             chapterNumber = 2,
             title = "Trigonometric Identities",
             markdownContent = """
                 # Trigonometric Identities
                 
                 Identities are equations that are true for all valid values.
                 
                 ## Fundamental Identities
                 1. sin²(θ) + cos²(θ) = 1 (Pythagorean identity)
                 2. tan(θ) = sin(θ)/cos(θ)
                 3. sec(θ) = 1/cos(θ)
                 
                 ## Sum and Difference Formulas
                 - sin(A ± B) = sin(A)cos(B) ± cos(A)sin(B)
                 - cos(A ± B) = cos(A)cos(B) ∓ sin(A)sin(B)
                 
                 ## Double Angle Formulas
                 - sin(2θ) = 2sin(θ)cos(θ)
                 - cos(2θ) = cos²(θ) - sin²(θ)
                 
                 ## Applications
                 - Simplifying complex expressions
                 - Solving trigonometric equations
                 - Proving other identities
             """.trimIndent(),
             estimatedReadingTime = 30,
             isCompleted = false
         )
         
         val trigGraphsChapter = Chapter(
             id = "chapter_trig_graphs",
             courseId = "course_trigonometry_1",
             chapterNumber = 3,
             title = "Trigonometric Graphs",
             markdownContent = """
                 # Trigonometric Graphs
                 
                 Understanding the graphical behavior of trigonometric functions.
                 
                 ## Sine and Cosine Graphs
                 - Period: 2π (360°)
                 - Amplitude: maximum height from center
                 - Phase shift: horizontal translation
                 
                 ## Graph Transformations
                 - y = A sin(Bx + C) + D
                 - A affects amplitude
                 - B affects period (2π/B)
                 - C affects phase shift
                 - D affects vertical shift
                 
                 ## Tangent Graph
                 - Period: π (180°)
                 - Vertical asymptotes at odd multiples of π/2
                 - Range: all real numbers
                 
                 ## Applications
                 - Modeling periodic phenomena
                 - Sound waves and music
                 - Seasonal temperature variations
             """.trimIndent(),
             estimatedReadingTime = 35,
             isCompleted = false
         )
         
         val trigApplicationsChapter = Chapter(
             id = "chapter_trig_applications",
             courseId = "course_trigonometry_1",
             chapterNumber = 4,
             title = "Applications and Problem Solving",
             markdownContent = """
                 # Applications and Problem Solving
                 
                 Using trigonometry to solve real-world problems.
                 
                 ## Law of Sines
                 For any triangle: a/sin(A) = b/sin(B) = c/sin(C)
                 
                 ## Law of Cosines  
                 c² = a² + b² - 2ab cos(C)
                 
                 ## Real-World Applications
                 1. **Navigation**: Finding distances and bearings
                 2. **Architecture**: Calculating roof angles and supports
                 3. **Astronomy**: Measuring distances to stars
                 4. **Engineering**: Bridge and building design
                 
                 ## Problem-Solving Steps
                 1. Draw a diagram
                 2. Identify known and unknown values
                 3. Choose appropriate law or formula
                 4. Solve and check reasonableness
             """.trimIndent(),
             estimatedReadingTime = 40,
             isCompleted = false
         )
         
         // MISSING: Create chapters for course_statistics_1 (6 chapters needed)
         val statsDataChapter = Chapter(
             id = "chapter_stats_data",
             courseId = "course_statistics_1",
             chapterNumber = 1,
             title = "Data Collection and Organization",
             markdownContent = """
                 # Data Collection and Organization
                 
                 Statistics begins with understanding how to collect and organize data.
                 
                 ## Types of Data
                 1. **Quantitative**: Numerical data (height, weight, age)
                 2. **Qualitative**: Categorical data (color, gender, brand)
                 
                 ## Data Collection Methods
                 - Surveys and questionnaires
                 - Experiments and controlled studies
                 - Observational studies
                 - Sampling techniques
                 
                 ## Data Organization
                 - Frequency tables
                 - Grouped data and class intervals
                 - Stem-and-leaf plots
                 - Data visualization basics
                 
                 ## Population vs Sample
                 - Population: entire group being studied
                 - Sample: subset of the population
                 - Representative sampling importance
             """.trimIndent(),
             estimatedReadingTime = 25,
             isCompleted = false
         )
         
         val statsMeasuresChapter = Chapter(
             id = "chapter_stats_measures",
             courseId = "course_statistics_1",
             chapterNumber = 2,
             title = "Measures of Central Tendency",
             markdownContent = """
                 # Measures of Central Tendency
                 
                 Understanding the "center" or typical value of a dataset.
                 
                 ## Mean (Average)
                 Sum of all values divided by the number of values.
                 - Sensitive to outliers
                 - Most commonly used measure
                 
                 ## Median
                 Middle value when data is arranged in order.
                 - Less affected by outliers
                 - Better for skewed distributions
                 
                 ## Mode
                 Most frequently occurring value.
                 - Can have multiple modes
                 - Useful for categorical data
                 
                 ## When to Use Each
                 - Mean: normal distributions, no outliers
                 - Median: skewed distributions, outliers present
                 - Mode: categorical data, finding most common
                 
                 ## Calculating with Technology
                 Using calculators and software for large datasets.
             """.trimIndent(),
             estimatedReadingTime = 20,
             isCompleted = false
         )
         
         val statsVariabilityChapter = Chapter(
             id = "chapter_stats_variability",
             courseId = "course_statistics_1",
             chapterNumber = 3,
             title = "Measures of Variability",
             markdownContent = """
                 # Measures of Variability
                 
                 Understanding how spread out or variable data is.
                 
                 ## Range
                 Difference between maximum and minimum values.
                 - Simple but limited measure
                 - Affected by outliers
                 
                 ## Variance
                 Average of squared deviations from the mean.
                 - Population variance: σ²
                 - Sample variance: s²
                 
                 ## Standard Deviation
                 Square root of variance, same units as original data.
                 - Most important measure of spread
                 - σ for population, s for sample
                 
                 ## Interquartile Range (IQR)
                 Range of the middle 50% of data.
                 - Q3 - Q1
                 - Resistant to outliers
                 
                 ## Applications
                 - Comparing variability between datasets
                 - Quality control in manufacturing
                 - Risk assessment in finance
             """.trimIndent(),
             estimatedReadingTime = 30,
             isCompleted = false
         )
         
         val statsProbabilityChapter = Chapter(
             id = "chapter_stats_probability",
             courseId = "course_statistics_1",
             chapterNumber = 4,
             title = "Introduction to Probability",
             markdownContent = """
                 # Introduction to Probability
                 
                 Understanding uncertainty and chance in statistical contexts.
                 
                 ## Basic Probability
                 Probability = Number of favorable outcomes / Total number of outcomes
                 - Values between 0 and 1 (or 0% to 100%)
                 - P(certain event) = 1
                 - P(impossible event) = 0
                 
                 ## Probability Rules
                 1. Addition rule: P(A or B) = P(A) + P(B) - P(A and B)
                 2. Multiplication rule: P(A and B) = P(A) × P(B|A)
                 3. Complement rule: P(A') = 1 - P(A)
                 
                 ## Independent vs Dependent Events
                 - Independent: outcome of one doesn't affect the other
                 - Dependent: outcome of one affects the other
                 
                 ## Applications
                 - Games of chance
                 - Medical testing and diagnosis
                 - Weather forecasting
                 - Business decision making
             """.trimIndent(),
             estimatedReadingTime = 35,
             isCompleted = false
         )
         
         val statsDistributionsChapter = Chapter(
             id = "chapter_stats_distributions",
             courseId = "course_statistics_1",
             chapterNumber = 5,
             title = "Probability Distributions",
             markdownContent = """
                 # Probability Distributions
                 
                 Understanding how probability is distributed across all possible outcomes.
                 
                 ## Normal Distribution
                 - Bell-shaped, symmetric curve
                 - Mean = median = mode
                 - 68-95-99.7 rule (empirical rule)
                 - Standard normal distribution (μ=0, σ=1)
                 
                 ## Binomial Distribution
                 - Fixed number of trials
                 - Each trial has two possible outcomes
                 - Constant probability of success
                 
                 ## Other Important Distributions
                 - Uniform distribution
                 - Exponential distribution
                 - Poisson distribution
                 
                 ## Z-Scores and Standardization
                 z = (x - μ) / σ
                 - Converting to standard normal
                 - Finding probabilities and percentiles
                 
                 ## Applications
                 - Quality control
                 - Test score interpretation
                 - Manufacturing tolerances
             """.trimIndent(),
             estimatedReadingTime = 40,
             isCompleted = false
         )
         
         val statsInferenceChapter = Chapter(
             id = "chapter_stats_inference",
             courseId = "course_statistics_1",
             chapterNumber = 6,
             title = "Statistical Inference",
             markdownContent = """
                 # Statistical Inference
                 
                 Making conclusions about populations based on sample data.
                 
                 ## Confidence Intervals
                 Range of values likely to contain the true population parameter.
                 - Confidence level (90%, 95%, 99%)
                 - Margin of error
                 - Critical values
                 
                 ## Hypothesis Testing
                 Process for testing claims about population parameters.
                 1. State null and alternative hypotheses
                 2. Choose significance level (α)
                 3. Calculate test statistic
                 4. Make decision based on p-value
                 
                 ## Types of Tests
                 - One-sample t-test
                 - Two-sample t-test
                 - Chi-square tests
                 - ANOVA (one-way)
                 
                 ## Errors in Hypothesis Testing
                 - Type I error: Rejecting true null hypothesis
                 - Type II error: Failing to reject false null hypothesis
                 - Power of a test
                 
                 ## Applications
                 - Medical research
                 - Market research
                 - Quality control
                 - Scientific studies
             """.trimIndent(),
             estimatedReadingTime = 45,
             isCompleted = false
         )
         
         // MISSING: Create chapters for course_algebra_2 (7 chapters needed)
         val advPolynomialsChapter = Chapter(
             id = "chapter_alg2_polynomials",
             courseId = "course_algebra_2",
             chapterNumber = 1,
             title = "Advanced Polynomial Functions",
             markdownContent = """
                 # Advanced Polynomial Functions
                 
                 Extending polynomial knowledge to higher degrees and complex applications.
                 
                 ## Higher Degree Polynomials
                 - Cubic functions (degree 3)
                 - Quartic functions (degree 4)
                 - General polynomial behavior
                 
                 ## Polynomial Operations
                 - Advanced factoring techniques
                 - Synthetic division
                 - Polynomial long division
                 - Remainder and factor theorems
                 
                 ## Roots and Zeros
                 - Fundamental theorem of algebra
                 - Rational root theorem
                 - Complex roots and conjugate pairs
                 - Multiplicity of roots
                 
                 ## Graphing Polynomials
                 - End behavior analysis
                 - Finding x and y intercepts
                 - Local maxima and minima
                 - Sketching complete graphs
                 
                 ## Applications
                 - Volume and area optimization
                 - Physics motion problems
                 - Economic modeling
             """.trimIndent(),
             estimatedReadingTime = 35,
             isCompleted = false
         )
         
         val rationalFunctionsChapter = Chapter(
             id = "chapter_alg2_rational",
             courseId = "course_algebra_2",
             chapterNumber = 2,
             title = "Rational Functions",
             markdownContent = """
                 # Rational Functions
                 
                 Functions that are ratios of polynomial functions.
                 
                 ## Definition and Domain
                 f(x) = P(x)/Q(x) where P and Q are polynomials
                 - Domain: all real numbers except where Q(x) = 0
                 - Vertical asymptotes at zeros of denominator
                 
                 ## Asymptotes
                 1. **Vertical**: x-values where function is undefined
                 2. **Horizontal**: behavior as x approaches ±∞
                 3. **Oblique**: when degree of numerator > degree of denominator
                 
                 ## Graphing Rational Functions
                 - Find domain and asymptotes
                 - Locate x and y intercepts
                 - Test behavior near asymptotes
                 - Check end behavior
                 
                 ## Operations with Rational Functions
                 - Addition and subtraction
                 - Multiplication and division
                 - Simplification techniques
                 
                 ## Applications
                 - Rate problems
                 - Concentration problems
                 - Cost-benefit analysis
             """.trimIndent(),
             estimatedReadingTime = 40,
             isCompleted = false
         )
         
         val exponentialChapter = Chapter(
             id = "chapter_alg2_exponential",
             courseId = "course_algebra_2",
             chapterNumber = 3,
             title = "Exponential and Logarithmic Functions",
             markdownContent = """
                 # Exponential and Logarithmic Functions
                 
                 Functions involving exponents and their inverse relationships.
                 
                 ## Exponential Functions
                 f(x) = a·b^x where a > 0, b > 0, b ≠ 1
                 - Growth (b > 1) vs decay (0 < b < 1)
                 - y-intercept at (0, a)
                 - Horizontal asymptote at y = 0
                 
                 ## The Natural Base e
                 e ≈ 2.71828...
                 - f(x) = e^x (natural exponential function)
                 - Continuous compound interest
                 - Growth and decay models
                 
                 ## Logarithmic Functions
                 y = log_b(x) if and only if x = b^y
                 - Domain: x > 0
                 - Vertical asymptote at x = 0
                 - x-intercept at (1, 0)
                 
                 ## Properties of Logarithms
                 1. log_b(xy) = log_b(x) + log_b(y)
                 2. log_b(x/y) = log_b(x) - log_b(y)
                 3. log_b(x^n) = n·log_b(x)
                 
                 ## Applications
                 - Population growth
                 - Radioactive decay
                 - pH and Richter scales
                 - Investment calculations
             """.trimIndent(),
             estimatedReadingTime = 45,
             isCompleted = false
         )
         
         val systemsChapter = Chapter(
             id = "chapter_alg2_systems",
             courseId = "course_algebra_2",
             chapterNumber = 4,
             title = "Systems of Equations and Inequalities",
             markdownContent = """
                 # Systems of Equations and Inequalities
                 
                 Solving multiple equations or inequalities simultaneously.
                 
                 ## Linear Systems
                 - Two variables: graphing, substitution, elimination
                 - Three variables: elimination method
                 - Matrix representation and solutions
                 
                 ## Nonlinear Systems
                 - Linear-quadratic systems
                 - Quadratic-quadratic systems
                 - Substitution and elimination strategies
                 
                 ## Systems of Inequalities
                 - Graphing solution regions
                 - Linear programming basics
                 - Optimization with constraints
                 
                 ## Matrix Methods
                 - Gaussian elimination
                 - Matrix operations
                 - Determinants and Cramer's rule
                 - Inverse matrices
                 
                 ## Applications
                 - Business optimization
                 - Resource allocation
                 - Mixture problems
                 - Break-even analysis
             """.trimIndent(),
             estimatedReadingTime = 50,
             isCompleted = false
         )
         
         val conicsChapter = Chapter(
             id = "chapter_alg2_conics",
             courseId = "course_algebra_2",
             chapterNumber = 5,
             title = "Conic Sections",
             markdownContent = """
                 # Conic Sections
                 
                 Curves formed by intersecting a cone with a plane.
                 
                 ## Circles
                 (x - h)² + (y - k)² = r²
                 - Center at (h, k)
                 - Radius r
                 - Standard and general forms
                 
                 ## Parabolas
                 - Vertex form: y = a(x - h)² + k
                 - Focus and directrix
                 - Horizontal and vertical orientations
                 
                 ## Ellipses
                 (x - h)²/a² + (y - k)²/b² = 1
                 - Center at (h, k)
                 - Major and minor axes
                 - Foci and eccentricity
                 
                 ## Hyperbolas
                 (x - h)²/a² - (y - k)²/b² = 1
                 - Center at (h, k)
                 - Asymptotes and branches
                 - Foci and eccentricity
                 
                 ## Applications
                 - Satellite orbits
                 - Telescope mirrors
                 - Architecture and design
                 - Physics trajectories
             """.trimIndent(),
             estimatedReadingTime = 40,
             isCompleted = false
         )
         
         val sequencesChapter = Chapter(
             id = "chapter_alg2_sequences",
             courseId = "course_algebra_2",
             chapterNumber = 6,
             title = "Sequences and Series",
             markdownContent = """
                 # Sequences and Series
                 
                 Ordered lists of numbers and their sums.
                 
                 ## Sequences
                 - Definition and notation: a₁, a₂, a₃, ...
                 - Finding patterns and nth terms
                 - Recursive vs explicit formulas
                 
                 ## Arithmetic Sequences
                 - Common difference: d
                 - nth term: aₙ = a₁ + (n-1)d
                 - Arithmetic means
                 
                 ## Geometric Sequences
                 - Common ratio: r
                 - nth term: aₙ = a₁ · r^(n-1)
                 - Geometric means
                 
                 ## Series
                 - Sum of sequence terms
                 - Arithmetic series: Sₙ = n(a₁ + aₙ)/2
                 - Geometric series: Sₙ = a₁(1-r^n)/(1-r)
                 
                 ## Infinite Series
                 - Convergent vs divergent
                 - Sum of infinite geometric series
                 
                 ## Applications
                 - Investment and annuities
                 - Population growth models
                 - Physics oscillations
             """.trimIndent(),
             estimatedReadingTime = 35,
             isCompleted = false
         )
         
         val probabilityChapter = Chapter(
             id = "chapter_alg2_probability",
             courseId = "course_algebra_2",
             chapterNumber = 7,
             title = "Advanced Probability and Statistics",
             markdownContent = """
                 # Advanced Probability and Statistics
                 
                 Deeper exploration of probability concepts and statistical analysis.
                 
                 ## Counting Principles
                 - Fundamental counting principle
                 - Permutations: P(n,r) = n!/(n-r)!
                 - Combinations: C(n,r) = n!/[r!(n-r)!]
                 - Applications to probability
                 
                 ## Binomial Probability
                 - Binomial experiments
                 - Binomial probability formula
                 - Binomial theorem and expansion
                 - Expected value and variance
                 
                 ## Normal Distribution
                 - Standard normal curve
                 - Z-scores and probability
                 - Applications to real data
                 
                 ## Statistical Analysis
                 - Sampling and bias
                 - Confidence intervals
                 - Hypothesis testing basics
                 - Correlation vs causation
                 
                 ## Advanced Applications
                 - Quality control
                 - Medical testing accuracy
                 - Sports analytics
                 - Business decision making
             """.trimIndent(),
             estimatedReadingTime = 45,
             isCompleted = false
         )
         
         chapters.addAll(listOf(
             // Algebra I (existing)
             linearEquationsChapter, quadraticsChapter, polynomialsChapter,
             // Geometry
             shapesChapter, anglesChapter,
             // Calculus (existing + new)
             limitsChapter, derivativesChapter, applicationsChapter, integralsChapter, calculusApplicationsChapter,
             // Trigonometry
             trigBasicsChapter, trigIdentitiesChapter, trigGraphsChapter, trigApplicationsChapter,
             // Statistics  
             statsDataChapter, statsMeasuresChapter, statsVariabilityChapter, statsProbabilityChapter, statsDistributionsChapter, statsInferenceChapter,
             // Advanced Algebra
             advPolynomialsChapter, rationalFunctionsChapter, exponentialChapter, systemsChapter, conicsChapter, sequencesChapter, probabilityChapter
         ))
         
         // Add all exercises to the main collection (they're already included in chapters above)
         exercises.addAll(linearExercises)
         exercises.addAll(quadraticExercises)
         exercises.addAll(polynomialExercises)
         exercises.addAll(shapesExercises)
         exercises.addAll(angleExercises)
         
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