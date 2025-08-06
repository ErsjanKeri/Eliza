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
import com.example.ai.edge.eliza.core.model.LocalizedContent
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
    
    // Proper translation helper functions with all three languages
    private fun t(english: String, albanian: String, german: String): LocalizedContent {
        return LocalizedContent.create(
            english = english,
            albanian = albanian,
            german = german
        )
    }
    
    private fun tMarkdown(english: String, albanian: String, german: String): LocalizedContent {
        return LocalizedContent.create(
            english = english,
            albanian = albanian, 
            german = german  
        )
    }
    
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
             explanation = exercise.explanation, // Now accepts LocalizedContent directly
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
                questionText = LocalizedContent.englishOnly("Generated practice question $i based on: ${exercise.questionText.en}"),
                options = exercise.options.shuffled(),
                correctAnswerIndex = (0..exercise.options.size - 1).random(),
                explanation = LocalizedContent.englishOnly("Generated explanation for trial $i"),
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
             explanation = trial.explanation, // Now accepts LocalizedContent directly
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
            title = t("Algebra I Fundamentals", "Bazat e Algjebrës I", "Grundlagen der Algebra I"),
            subject = Subject.ALGEBRA,
            grade = "9th Grade",
            description = t(
                english = "Master the fundamentals of algebra including linear equations, polynomials, and factoring.",
                albanian = "Zotëroni bazat e algjebrës duke përfshirë ekuacionet lineare, polinomet dhe faktorizimin.",
                german = "Meistern Sie die Grundlagen der Algebra, einschließlich linearer Gleichungen, Polynome und Faktorisierung."
            ),
            chapters = emptyList(), // Will be populated separately
            totalChapters = 3,
            estimatedHours = 20,
            imageUrl = null,
            isDownloaded = true
        )
        
        val geometryCourse = Course(
            id = "course_geometry_1",
            title = t("Geometry Basics", "Bazat e Gjeometrisë", "Grundlagen der Geometrie"),
            subject = Subject.GEOMETRY,
            grade = "10th Grade",
            description = t(
                english = "Learn about shapes, angles, area, and volume in this comprehensive geometry course.",
                albanian = "Mësoni rreth formave, këndeve, sipërfaqes dhe vëllimit në këtë kurs gjithëpërfshirës të gjeometrisë.",
                german = "Lernen Sie in diesem umfassenden Geometriekurs alles über Formen, Winkel, Flächen und Volumen."
            ),
            chapters = emptyList(),
            totalChapters = 2,
            estimatedHours = 15,
            imageUrl = null,
            isDownloaded = false
        )
        
        val calculusCourse = Course(
            id = "course_calculus_1",
            title = t("Introduction to Calculus", "Hyrje në Kalkulus", "Einführung in die Infinitesimalrechnung"),
            subject = Subject.CALCULUS,
            grade = "11th Grade",
            description = t(
                english = "Explore the fundamentals of differential and integral calculus with practical applications.",
                albanian = "Eksploroni bazat e kalkulusit diferencial dhe integral me aplikime praktike.",
                german = "Entdecken Sie die Grundlagen der Differential- und Integralrechnung mit praktischen Anwendungen."
            ),
            chapters = emptyList(),
            totalChapters = 5,
            estimatedHours = 30,
            imageUrl = null,
            isDownloaded = true
        )
        
        val trigonometryCourse = Course(
            id = "course_trigonometry_1",
            title = t("Trigonometry Essentials", "Bazat e Trigonometrisë", "Grundlagen der Trigonometrie"),
            subject = Subject.TRIGONOMETRY,
            grade = "10th Grade",
            description = t(
                english = "Master sine, cosine, tangent and their applications in solving triangles and modeling periodic phenomena.",
                albanian = "Zotëroni sinusin, kosinusin, tangentin dhe aplikimet e tyre në zgjidhjen e trekëndëshave dhe modelimin e fenomeneve periodike.",
                german = "Meistern Sie Sinus, Kosinus, Tangens und ihre Anwendungen zur Lösung von Dreiecken und zur Modellierung periodischer Phänomene."
            ),
            chapters = emptyList(),
            totalChapters = 4,
            estimatedHours = 18,
            imageUrl = null,
            isDownloaded = false
        )
        
        val statisticsCourse = Course(
            id = "course_statistics_1",
            title = t("Introduction to Statistics", "Hyrje në Statistikë", "Einführung in die Statistik"),
            subject = Subject.STATISTICS,
            grade = "11th Grade",
            description = t(
                english = "Learn data analysis, probability, and statistical inference to make informed decisions from data.",
                albanian = "Mësoni analizën e të dhënave, probabilitetin dhe konkluzionet statistikore për të marrë vendime të informuara nga të dhënat.",
                german = "Lernen Sie Datenanalyse, Wahrscheinlichkeit und statistische Inferenz, um fundierte Entscheidungen auf der Grundlage von Daten zu treffen."
            ),
            chapters = emptyList(),
            totalChapters = 6,
            estimatedHours = 25,
            imageUrl = null,
            isDownloaded = false
        )
        
        val algebraAdvancedCourse = Course(
            id = "course_algebra_2",
            title = t("Advanced Algebra", "Algjebër e Avancuar", "Fortgeschrittene Algebra"),
            subject = Subject.ALGEBRA,
            grade = "11th Grade",
            description = t(
                english = "Dive deeper into polynomial functions, logarithms, and exponential equations for advanced problem solving.",
                albanian = "Thellohu më shumë në funksionet polinomiale, logaritmet dhe ekuacionet eksponenciale për zgjidhjen e problemeve të avancuara.",
                german = "Tauchen Sie tiefer in Polynomfunktionen, Logarithmen und Exponentialgleichungen für fortgeschrittene Problemlösungen ein."
            ),
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
                 questionText = t(
                     english = "Solve for x: 2x + 7 = 19",
                     albanian = "Zgjidh për x: 2x + 7 = 19", 
                     german = "Löse für x: 2x + 7 = 19"
                 ),
                 options = listOf(
                     t("x = 5", "x = 5", "x = 5"),
                     t("x = 6", "x = 6", "x = 6"), 
                     t("x = 7", "x = 7", "x = 7"),
                     t("x = 8", "x = 8", "x = 8")
                 ),
                 correctAnswerIndex = 1,
                 explanation = t(
                     english = "Subtract 7 from both sides: 2x = 12. Then divide by 2: x = 6.",
                     albanian = "Zbrit 7 nga të dyja anët: 2x = 12. Pastaj pjesëto me 2: x = 6.",
                     german = "Subtrahiere 7 von beiden Seiten: 2x = 12. Dann teile durch 2: x = 6."
                 ),
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_linear_2",
                 chapterId = "chapter_linear_eq",
                 questionText = t(
                     english = "What is the slope of the line 3x + 4y = 12?",
                     albanian = "Cili është pjerrësia e vijës 3x + 4y = 12?",
                     german = "Was ist die Steigung der Linie 3x + 4y = 12?"
                 ),
                 options = listOf(
                     t("-3/4", "-3/4", "-3/4"),
                     t("3/4", "3/4", "3/4"),
                     t("-4/3", "-4/3", "-4/3"), 
                     t("4/3", "4/3", "4/3")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Rewrite in slope-intercept form: y = -3/4 x + 3. The slope is -3/4.",
                     albanian = "Rishkruaje në formën pjerrësi-prerje: y = -3/4 x + 3. Pjerrësia është -3/4.",
                     german = "Umschreiben in Steigung-Achsenabschnitt-Form: y = -3/4 x + 3. Die Steigung ist -3/4."
                 ),
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_linear_3",
                 chapterId = "chapter_linear_eq",
                 questionText = t(
                     english = "Which equation represents a line with slope 2 and y-intercept -3?",
                     albanian = "Cila ekuacion përfaqëson një vijë me pjerrësi 2 dhe prerje y -3?",
                     german = "Welche Gleichung stellt eine Linie mit Steigung 2 und y-Achsenabschnitt -3 dar?"
                 ),
                 options = listOf(
                     t("y = 2x - 3", "y = 2x - 3", "y = 2x - 3"),
                     t("y = -3x + 2", "y = -3x + 2", "y = -3x + 2"),
                     t("y = 2x + 3", "y = 2x + 3", "y = 2x + 3"),
                     t("y = 3x - 2", "y = 3x - 2", "y = 3x - 2")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "The slope-intercept form is y = mx + b, where m is slope and b is y-intercept.",
                     albanian = "Forma pjerrësi-prerje është y = mx + b, ku m është pjerrësia dhe b është prerja y.",
                     german = "Die Steigung-Achsenabschnitt-Form ist y = mx + b, wobei m die Steigung und b der y-Achsenabschnitt ist."
                 ),
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_linear_4",
                 chapterId = "chapter_linear_eq",
                 questionText = t(
                     english = "Find the x-intercept of the line 2x - 3y = 6",
                     albanian = "Gjej prerjen x të vijës 2x - 3y = 6",
                     german = "Finde den x-Achsenabschnitt der Linie 2x - 3y = 6"
                 ),
                 options = listOf(
                     t("x = 2", "x = 2", "x = 2"),
                     t("x = 3", "x = 3", "x = 3"),
                     t("x = -2", "x = -2", "x = -2"),
                     t("x = -3", "x = -3", "x = -3")
                 ),
                 correctAnswerIndex = 1,
                 explanation = t(
                     english = "Set y = 0: 2x - 3(0) = 6, so 2x = 6, therefore x = 3.",
                     albanian = "Vendos y = 0: 2x - 3(0) = 6, kështu 2x = 6, prandaj x = 3.",
                     german = "Setze y = 0: 2x - 3(0) = 6, also 2x = 6, daher x = 3."
                 ),
                 difficulty = Difficulty.MEDIUM
             )
         )
         
         // Quadratic Equations Chapter Exercises
         val quadraticExercises = listOf(
             Exercise(
                 id = "exercise_quad_1",
                 chapterId = "chapter_quadratics",
                 questionText = t(
                     english = "What are the solutions to x² - 5x + 6 = 0?",
                     albanian = "Cilat janë zgjidhjet e x² - 5x + 6 = 0?",
                     german = "Was sind die Lösungen von x² - 5x + 6 = 0?"
                 ),
                 options = listOf(
                     t("x = 2, 3", "x = 2, 3", "x = 2, 3"),
                     t("x = 1, 6", "x = 1, 6", "x = 1, 6"),
                     t("x = -2, -3", "x = -2, -3", "x = -2, -3"),
                     t("x = 2, -3", "x = 2, -3", "x = 2, -3")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Factor: (x - 2)(x - 3) = 0, so x = 2 or x = 3.",
                     albanian = "Faktorizon: (x - 2)(x - 3) = 0, kështu x = 2 ose x = 3.",
                     german = "Faktorisieren: (x - 2)(x - 3) = 0, also x = 2 oder x = 3."
                 ),
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_quad_2",
                 chapterId = "chapter_quadratics",
                 questionText = t(
                     english = "Using the quadratic formula, solve x² + 2x - 3 = 0",
                     albanian = "Duke përdorur formulën kuadratike, zgjidh x² + 2x - 3 = 0",
                     german = "Mit der quadratischen Formel löse x² + 2x - 3 = 0"
                 ),
                 options = listOf(
                     t("x = 1, -3", "x = 1, -3", "x = 1, -3"),
                     t("x = -1, 3", "x = -1, 3", "x = -1, 3"),
                     t("x = 1, 3", "x = 1, 3", "x = 1, 3"),
                     t("x = -1, -3", "x = -1, -3", "x = -1, -3")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "x = (-2 ± √(4 + 12))/2 = (-2 ± 4)/2, so x = 1 or x = -3.",
                     albanian = "x = (-2 ± √(4 + 12))/2 = (-2 ± 4)/2, kështu x = 1 ose x = -3.",
                     german = "x = (-2 ± √(4 + 12))/2 = (-2 ± 4)/2, also x = 1 oder x = -3."
                 ),
                 difficulty = Difficulty.HARD
             ),
             Exercise(
                 id = "exercise_quad_3",
                 chapterId = "chapter_quadratics",
                 questionText = t(
                     english = "What is the vertex of the parabola y = x² - 4x + 3?",
                     albanian = "Cili është kulmi i parabolës y = x² - 4x + 3?",
                     german = "Was ist der Scheitelpunkt der Parabel y = x² - 4x + 3?"
                 ),
                 options = listOf(
                     t("(2, -1)", "(2, -1)", "(2, -1)"),
                     t("(2, 1)", "(2, 1)", "(2, 1)"),
                     t("(-2, -1)", "(-2, -1)", "(-2, -1)"),
                     t("(-2, 1)", "(-2, 1)", "(-2, 1)")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Complete the square: y = (x - 2)² - 1. Vertex is (2, -1).",
                     albanian = "Përfundo katrorin: y = (x - 2)² - 1. Kulmi është (2, -1).",
                     german = "Vervollständige das Quadrat: y = (x - 2)² - 1. Der Scheitelpunkt ist (2, -1)."
                 ),
                 difficulty = Difficulty.HARD
             )
         )
         
         // Polynomials Chapter Exercises
         val polynomialExercises = listOf(
             Exercise(
                 id = "exercise_poly_1",
                 chapterId = "chapter_polynomials",
                 questionText = t(
                     english = "What is the degree of the polynomial 3x⁴ - 2x² + 7x - 1?",
                     albanian = "Cili është shkalla e polinomit 3x⁴ - 2x² + 7x - 1?",
                     german = "Was ist der Grad des Polynoms 3x⁴ - 2x² + 7x - 1?"
                 ),
                 options = listOf(
                     t("3", "3", "3"),
                     t("4", "4", "4"),
                     t("2", "2", "2"),
                     t("1", "1", "1")
                 ),
                 correctAnswerIndex = 1,
                 explanation = t(
                     english = "The degree is the highest power of x, which is 4.",
                     albanian = "Shkalla është fuqia më e lartë e x, që është 4.",
                     german = "Der Grad ist die höchste Potenz von x, die 4 ist."
                 ),
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_poly_2",
                 chapterId = "chapter_polynomials",
                 questionText = t(
                     english = "Factor completely: x² - 9",
                     albanian = "Faktorizon plotësisht: x² - 9",
                     german = "Vollständig faktorisieren: x² - 9"
                 ),
                 options = listOf(
                     t("(x - 3)(x + 3)", "(x - 3)(x + 3)", "(x - 3)(x + 3)"),
                     t("(x - 9)(x + 1)", "(x - 9)(x + 1)", "(x - 9)(x + 1)"),
                     t("(x - 3)²", "(x - 3)²", "(x - 3)²"),
                     t("Cannot be factored", "Nuk mund të faktorizohet", "Kann nicht faktorisiert werden")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "This is a difference of squares: a² - b² = (a - b)(a + b).",
                     albanian = "Kjo është dallim katrorësh: a² - b² = (a - b)(a + b).",
                     german = "Dies ist eine Differenz von Quadraten: a² - b² = (a - b)(a + b)."
                 ),
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_poly_3",
                 chapterId = "chapter_polynomials",
                 questionText = t(
                     english = "Add: (2x² + 3x - 1) + (x² - 2x + 4)",
                     albanian = "Shto: (2x² + 3x - 1) + (x² - 2x + 4)",
                     german = "Addiere: (2x² + 3x - 1) + (x² - 2x + 4)"
                 ),
                 options = listOf(
                     t("3x² + x + 3", "3x² + x + 3", "3x² + x + 3"),
                     t("3x² + 5x + 3", "3x² + 5x + 3", "3x² + 5x + 3"),
                     t("x² + x + 3", "x² + x + 3", "x² + x + 3"),
                     t("3x² + x - 3", "3x² + x - 3", "3x² + x - 3")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Combine like terms: (2x² + x²) + (3x - 2x) + (-1 + 4) = 3x² + x + 3.",
                     albanian = "Kombinon termat e ngjashëm: (2x² + x²) + (3x - 2x) + (-1 + 4) = 3x² + x + 3.",
                     german = "Gleiche Terme zusammenfassen: (2x² + x²) + (3x - 2x) + (-1 + 4) = 3x² + x + 3."
                 ),
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_poly_4",
                 chapterId = "chapter_polynomials",
                 questionText = t(
                     english = "What are the roots of x³ - 6x² + 11x - 6 = 0?",
                     albanian = "Cilat janë rrënjët e x³ - 6x² + 11x - 6 = 0?",
                     german = "Was sind die Wurzeln von x³ - 6x² + 11x - 6 = 0?"
                 ),
                 options = listOf(
                     t("x = 1, 2, 3", "x = 1, 2, 3", "x = 1, 2, 3"),
                     t("x = 0, 2, 3", "x = 0, 2, 3", "x = 0, 2, 3"),
                     t("x = 1, 1, 6", "x = 1, 1, 6", "x = 1, 1, 6"),
                     t("x = -1, -2, -3", "x = -1, -2, -3", "x = -1, -2, -3")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Factor: (x - 1)(x - 2)(x - 3) = 0, so x = 1, 2, or 3.",
                     albanian = "Faktorizon: (x - 1)(x - 2)(x - 3) = 0, kështu x = 1, 2, ose 3.",
                     german = "Faktorisieren: (x - 1)(x - 2)(x - 3) = 0, also x = 1, 2, oder 3."
                 ),
                 difficulty = Difficulty.HARD
             )
         )
         
         // Geometry - Basic Shapes Exercises
         val shapesExercises = listOf(
             Exercise(
                 id = "exercise_shapes_1",
                 chapterId = "chapter_geo_shapes",
                 questionText = t(
                     english = "What is the area of a rectangle with length 8 and width 5?",
                     albanian = "Cila është sipërfaqja e një drejtkëndëshi me gjatësi 8 dhe gjerësi 5?",
                     german = "Was ist die Fläche eines Rechtecks mit Länge 8 und Breite 5?"
                 ),
                 options = listOf(
                     t("40", "40", "40"),
                     t("26", "26", "26"),
                     t("13", "13", "13"),
                     t("20", "20", "20")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Area of rectangle = length × width = 8 × 5 = 40.",
                     albanian = "Sipërfaqja e drejtkëndëshit = gjatësia × gjerësia = 8 × 5 = 40.",
                     german = "Fläche des Rechtecks = Länge × Breite = 8 × 5 = 40."
                 ),
                 difficulty = Difficulty.EASY
             ),
             Exercise(
                 id = "exercise_shapes_2",
                 chapterId = "chapter_geo_shapes",
                 questionText = t(
                     english = "What is the circumference of a circle with radius 3?",
                     albanian = "Cila është perimetri i një rrethi me rreze 3?",
                     german = "Was ist der Umfang eines Kreises mit Radius 3?"
                 ),
                 options = listOf(
                     t("6π", "6π", "6π"),
                     t("9π", "9π", "9π"),
                     t("3π", "3π", "3π"),
                     t("12π", "12π", "12π")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "Circumference = 2πr = 2π(3) = 6π.",
                     albanian = "Perimetri = 2πr = 2π(3) = 6π.",
                     german = "Umfang = 2πr = 2π(3) = 6π."
                 ),
                 difficulty = Difficulty.MEDIUM
             ),
             Exercise(
                 id = "exercise_shapes_3",
                 chapterId = "chapter_geo_shapes",
                 questionText = t(
                     english = "In a triangle, if all three sides are equal, what type of triangle is it?",
                     albanian = "Në një trekëndësh, nëse të tri anët janë të barabarta, çfarë lloji trekëndëshi është?",
                     german = "In einem Dreieck, wenn alle drei Seiten gleich sind, welche Art von Dreieck ist es?"
                 ),
                 options = listOf(
                     t("Equilateral", "Barabrinjës", "Gleichseitig"),
                     t("Isosceles", "Dybrinjënjëshëm", "Gleichschenklig"),
                     t("Scalene", "Tërnjësore", "Unregelmäßig"),
                     t("Right", "Kënddrejtë", "Rechtwinklig")
                 ),
                 correctAnswerIndex = 0,
                 explanation = t(
                     english = "A triangle with all three sides equal is called equilateral.",
                     albanian = "Një trekëndësh me të tri anët e barabarta quhet barabrinjës.",
                     german = "Ein Dreieck mit allen drei gleichen Seiten nennt man gleichseitig."
                 ),
                 difficulty = Difficulty.EASY
             )
         )
         
         // Geometry - Angles Exercises
         val angleExercises = listOf(
            Exercise(
                id = "exercise_angles_1",
                chapterId = "chapter_geo_angles",
                questionText = t(
                    english = "What is the complement of a 35° angle?",
                    albanian = "Cili është komplementi i një këndi 35°?",
                    german = "Was ist das Komplement eines 35°-Winkels?"
                ),
                options = listOf(
                    t("55°", "55°", "55°"),
                    t("145°", "145°", "145°"),
                    t("65°", "65°", "65°"),
                    t("125°", "125°", "125°")
                ),
                correctAnswerIndex = 0,
                explanation = t(
                    english = "Complementary angles sum to 90°: 90° - 35° = 55°.",
                    albanian = "Këndet komplementare e kanë shumën 90°: 90° - 35° = 55°.",
                    german = "Komplementärwinkel summieren sich zu 90°: 90° - 35° = 55°."
                ),
                difficulty = Difficulty.EASY
            ),
            Exercise(
                id = "exercise_angles_2",
                chapterId = "chapter_geo_angles",
                questionText = t(
                    english = "In a triangle, what is the sum of all interior angles?",
                    albanian = "Në një trekëndësh, sa është shuma e të gjithë këndeve të brendshme?",
                    german = "Was ist die Summe aller Innenwinkel in einem Dreieck?"
                ),
                options = listOf(
                    t("180°", "180°", "180°"),
                    t("360°", "360°", "360°"),
                    t("90°", "90°", "90°"),
                    t("270°", "270°", "270°")
                ),
                correctAnswerIndex = 0,
                explanation = t(
                    english = "The sum of interior angles in any triangle is always 180°.",
                    albanian = "Shuma e këndeve të brendshme në çdo trekëndësh është gjithmonë 180°.",
                    german = "Die Summe der Innenwinkel in jedem Dreieck beträgt immer 180°."
                ),
                difficulty = Difficulty.EASY
            ),
            Exercise(
                id = "exercise_angles_3",
                chapterId = "chapter_geo_angles",
                questionText = t(
                    english = "If two parallel lines are cut by a transversal, what can you say about corresponding angles?",
                    albanian = "Nëse dy vija paralele priten nga një tërthore, çfarë mund të thuash për këndet korresponduese?",
                    german = "Wenn zwei parallele Linien von einer Transversale geschnitten werden, was kann man über entsprechende Winkel sagen?"
                ),
                options = listOf(
                    t("They are equal", "Ato janë të barabarta", "Sie sind gleich"),
                    t("They are supplementary", "Ato janë suplementare", "Sie sind supplementär"),
                    t("They are complementary", "Ato janë komplementare", "Sie sind komplementär"),
                    t("They sum to 270°", "Shuma e tyre është 270°", "Ihre Summe beträgt 270°")
                ),
                correctAnswerIndex = 0,
                explanation = t(
                    english = "Corresponding angles are equal when parallel lines are cut by a transversal.",
                    albanian = "Këndet korresponduese janë të barabarta kur vijat paralele priten nga një tërthore.",
                    german = "Entsprechende Winkel sind gleich, wenn parallele Linien von einer Transversale geschnitten werden."
                ),
                difficulty = Difficulty.MEDIUM
            ),
            Exercise(
                id = "exercise_angles_4",
                chapterId = "chapter_geo_angles",
                questionText = t(
                    english = "What type of angle measures exactly 90°?",
                    albanian = "Çfarë lloj këndi mat saktësisht 90°?",
                    german = "Welche Art von Winkel misst genau 90°?"
                ),
                options = listOf(
                    t("Right angle", "Kënd i drejtë", "Rechter Winkel"),
                    t("Acute angle", "Kënd i ngushtë", "Spitzer Winkel"),
                    t("Obtuse angle", "Kënd i gjerë", "Stumpfer Winkel"),
                    t("Straight angle", "Kënd i shtrirë", "Gestreckter Winkel")
                ),
                correctAnswerIndex = 0,
                explanation = t(
                    english = "An angle that measures exactly 90° is called a right angle.",
                    albanian = "Një kënd që mat saktësisht 90° quhet kënd i drejtë.",
                    german = "Ein Winkel, der genau 90° misst, wird als rechter Winkel bezeichnet."
                ),
                difficulty = Difficulty.EASY
            )
        )
 
        val linearEquationsChapter = Chapter(
            id = "chapter_linear_eq",
            courseId = "course_algebra_1",
            chapterNumber = 1,
            title = t("Linear Equations", "Ekuacionet Lineare", "Lineare Gleichungen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Ekuacionet Lineare
                    
                    Një ekuacion linear është një ekuacion që krijon një vijë të drejtë kur paraqitet në grafik.
                    
                    ## Forma Standarde
                    Forma standarde e një ekuacioni linear është: **ax + b = c**
                    
                    ## Shembuj
                    1. Zgjidh: 2x + 5 = 15
                    - Zbrit 5 nga të dyja anët: 2x = 10
                    - Pjesëto me 2: x = 5
                    
                    2. Zgjidh: 3x - 7 = 8
                    - Shto 7 në të dyja anët: 3x = 15
                    - Pjesëto me 3: x = 5
                """.trimIndent(),
                german = """
                    # Lineare Gleichungen
                    
                    Eine lineare Gleichung ist eine Gleichung, die beim grafischen Darstellen eine gerade Linie ergibt.
                    
                    ## Standardform
                    Die Standardform einer linearen Gleichung lautet: **ax + b = c**
                    
                    ## Beispiele
                    1. Löse: 2x + 5 = 15
                    - Subtrahiere 5 von beiden Seiten: 2x = 10
                    - Dividiere durch 2: x = 5
                    
                    2. Löse: 3x - 7 = 8
                    - Addiere 7 auf beiden Seiten: 3x = 15
                    - Dividiere durch 3: x = 5
                """.trimIndent()
            ),
            exercises = linearExercises,
            estimatedReadingTime = 15,
            isCompleted = false
        )

        val quadraticsChapter = Chapter(
            id = "chapter_quadratics",
            courseId = "course_algebra_1",
            chapterNumber = 2,
            title = t("Quadratic Equations", "Ekuacionet Kuadratike", "Quadratische Gleichungen"),
            markdownContent = tMarkdown(
                english = """
                    # Quadratic Equations
                    
                    A quadratic equation is a polynomial equation of degree 2.
                    
                    ## Standard Form
                    The standard form is: **ax² + bx + c = 0**
                    
                    ## Solving Methods
                    1. Factoring
                    2. Quadratic Formula
                    3. Completing the Square
                    
                    ## Examples
                    1. Solve: x² - 5x + 6 = 0
                    - Factor: (x - 2)(x - 3) = 0
                    - Solutions: x = 2 or x = 3
                    
                    2. Using the quadratic formula: x = (-b ± √(b² - 4ac)) / 2a
                """.trimIndent(),
                albanian = """
                    # Ekuacionet Kuadratike
                    
                    Një ekuacion kuadratik është një ekuacion polinomial i shkallës së dytë.
                    
                    ## Forma Standarde
                    Forma standarde është: **ax² + bx + c = 0**
                    
                    ## Metodat e Zgjidhjes
                    1. Faktorizimi
                    2. Formula Kuadratike
                    3. Plotësimi i Katrorit
                    
                    ## Shembuj
                    1. Zgjidh: x² - 5x + 6 = 0
                    - Faktorizo: (x - 2)(x - 3) = 0
                    - Zgjidhjet: x = 2 ose x = 3
                    
                    2. Duke përdorur formulën kuadratike: x = (-b ± √(b² - 4ac)) / 2a
                """.trimIndent(),
                german = """
                    # Quadratische Gleichungen
                    
                    Eine quadratische Gleichung ist eine Polynomgleichung zweiten Grades.
                    
                    ## Standardform
                    Die Standardform lautet: **ax² + bx + c = 0**
                    
                    ## Lösungsmethoden
                    1. Faktorisierung
                    2. Quadratische Lösungsformel (Mitternachtsformel)
                    3. Quadratische Ergänzung
                    
                    ## Beispiele
                    1. Löse: x² - 5x + 6 = 0
                    - Faktorisieren: (x - 2)(x - 3) = 0
                    - Lösungen: x = 2 oder x = 3
                    
                    2. Mit der quadratischen Lösungsformel: x = (-b ± √(b² - 4ac)) / 2a
                """.trimIndent()
            ),
            exercises = quadraticExercises,
            estimatedReadingTime = 20,
            isCompleted = false
        )

        val polynomialsChapter = Chapter(
            id = "chapter_polynomials",
            courseId = "course_algebra_1",
            chapterNumber = 3,
            title = t("Polynomials", "Polinomet", "Polynome"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Polinomet
                    
                    Një polinom është një shprehje që përbëhet nga variabla dhe koeficientë.
                    
                    ## Përkufizimi
                    Një polinom me një variabël x është: **anx^n + an-1x^(n-1) + ... + a1x + a0**
                    
                    ## Llojet e Polinomeve
                    1. Monom (1 term): 5x²
                    2. Binom (2 terma): 3x + 7
                    3. Trinom (3 terma): x² + 2x + 1
                    
                    ## Veprimet
                    - Mbledhja dhe Zbritja
                    - Shumëzimi
                    - Faktorizimi
                    - Gjetja e Rrënjëve
                    
                    ## Shembuj
                    1. Shto: (2x² + 3x + 1) + (x² - x + 4) = 3x² + 2x + 5
                    2. Faktorizo: x² - 5x + 6 = (x - 2)(x - 3)
                """.trimIndent(),
                german = """
                    # Polynome
                    
                    Ein Polynom ist ein Ausdruck, der aus Variablen und Koeffizienten besteht.
                    
                    ## Definition
                    Ein Polynom in einer Variablen x ist: **anx^n + an-1x^(n-1) + ... + a1x + a0**
                    
                    ## Arten von Polynomen
                    1. Monom (1 Term): 5x²
                    2. Binom (2 Terme): 3x + 7
                    3. Trinom (3 Terme): x² + 2x + 1
                    
                    ## Operationen
                    - Addition und Subtraktion
                    - Multiplikation
                    - Faktorisierung
                    - Wurzeln finden
                    
                    ## Beispiele
                    1. Addiere: (2x² + 3x + 1) + (x² - x + 4) = 3x² + 2x + 5
                    2. Faktorisieren: x² - 5x + 6 = (x - 2)(x - 3)
                """.trimIndent()
            ),
            exercises = polynomialExercises,
            estimatedReadingTime = 25,
            isCompleted = false
        )

        val limitsChapter = Chapter(
            id = "chapter_calc_limits",
            courseId = "course_calculus_1",
            chapterNumber = 1,
            title = t("Limits and Continuity", "Limitet dhe Vazhdueshmëria", "Grenzwerte und Stetigkeit"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Limitet dhe Vazhdueshmëria
                    
                    Limitet janë themeli i kalkulusit, duke përshkruar se çfarë ndodh me një funksion kur hyrja e tij i afrohet një vlere të caktuar.
                    
                    ## Përkufizimi i Limitit
                    Limiti i f(x) kur x i afrohet a-së është L nëse f(x) i afrohet në mënyrë arbitrare L-së kur x i afrohet në mënyrë arbitrare a-së.
                    
                    ## Shënimi
                    Ne shkruajmë: **lim[x→a] f(x) = L**
                    
                    ## Shembuj
                    1. lim[x→2] (3x + 1) = 7
                    2. lim[x→0] (sin x)/x = 1
                """.trimIndent(),
                german = """
                    # Grenzwerte und Stetigkeit
                    
                    Grenzwerte sind die Grundlage der Infinitesimalrechnung und beschreiben, was mit einer Funktion geschieht, wenn sich ihre Eingabe einem bestimmten Wert nähert.
                    
                    ## Definition eines Grenzwertes
                    Der Grenzwert von f(x), wenn x sich a nähert, ist L, wenn f(x) beliebig nahe an L kommt, wenn x sich beliebig nahe an a annähert.
                    
                    ## Notation
                    Wir schreiben: **lim[x→a] f(x) = L**
                    
                    ## Beispiele
                    1. lim[x→2] (3x + 1) = 7
                    2. lim[x→0] (sin x)/x = 1
                """.trimIndent()
            ),
            estimatedReadingTime = 25,
            isCompleted = true
        )

        val derivativesChapter = Chapter(
            id = "chapter_calc_derivatives",
            courseId = "course_calculus_1",
            chapterNumber = 2,
            title = t("Introduction to Derivatives", "Hyrje në Derivate", "Einführung in Ableitungen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Hyrje në Derivate
                    
                    Derivati mat se si ndryshon një funksion kur ndryshon hyrja e tij.
                    
                    ## Përkufizimi
                    Derivati i f(x) është: **f'(x) = lim[h→0] [f(x+h) - f(x)]/h**
                    
                    ## Rregullat Themelore
                    1. Rregulli i Fuqisë: d/dx(x^n) = nx^(n-1)
                    2. Rregulli i Prodhimit: d/dx(fg) = f'g + fg'
                    3. Rregulli Zinxhir: d/dx(f(g(x))) = f'(g(x)) × g'(x)
                    
                    ## Zbatimet
                    - Gjetja e pjerrësive të vijave tangjente
                    - Problemet e optimizimit
                    - Llogaritjet e shkallës së ndryshimit
                """.trimIndent(),
                german = """
                    # Einführung in Ableitungen
                    
                    Die Ableitung misst, wie sich eine Funktion ändert, wenn sich ihre Eingabe ändert.
                    
                    ## Definition
                    Die Ableitung von f(x) ist: **f'(x) = lim[h→0] [f(x+h) - f(x)]/h**
                    
                    ## Grundlegende Regeln
                    1. Potenzregel: d/dx(x^n) = nx^(n-1)
                    2. Produktregel: d/dx(fg) = f'g + fg'
                    3. Kettenregel: d/dx(f(g(x))) = f'(g(x)) × g'(x)
                    
                    ## Anwendungen
                    - Finden von Tangentensteigungen
                    - Optimierungsprobleme
                    - Berechnungen von Änderungsraten
                """.trimIndent()
            ),
            estimatedReadingTime = 30,
            isCompleted = true
        )

        val applicationsChapter = Chapter(
            id = "chapter_calc_applications",
            courseId = "course_calculus_1",
            chapterNumber = 3,
            title = t("Applications of Derivatives", "Zbatimet e Derivateve", "Anwendungen von Ableitungen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Zbatimet e Derivateve
                    
                    Derivatet kanë shumë zbatime praktike në zgjidhjen e problemeve të botës reale.
                    
                    ## Optimizimi
                    Gjeni vlerat maksimale dhe minimale të funksioneve për të zgjidhur probleme si:
                    - Maksimizimi i fitimit
                    - Minimizimi i kostos
                    - Gjetja e dimensioneve optimale
                    
                    ## Shkallët e Lidhura
                    Zgjidhja e problemeve ku sasi të shumta ndryshojnë në lidhje me kohën.
                    
                    ## Shembull
                    Një tullumbace po fryhet. Nëse rrezja rritet me 2 cm/min, sa shpejt po ndryshon vëllimi kur r = 10 cm?
                """.trimIndent(),
                german = """
                    # Anwendungen von Ableitungen
                    
                    Ableitungen haben viele praktische Anwendungen bei der Lösung von Problemen in der realen Welt.
                    
                    ## Optimierung
                    Finden Sie Maximal- und Minimalwerte von Funktionen, um Probleme zu lösen wie:
                    - Gewinnmaximierung
                    - Kostenminimierung
                    - Finden optimaler Abmessungen
                    
                    ## Verwandte Raten
                    Lösen von Problemen, bei denen sich mehrere Größen in Bezug auf die Zeit ändern.
                    
                    ## Beispiel
                    Ein Ballon wird aufgeblasen. Wenn der Radius mit 2 cm/min zunimmt, wie schnell ändert sich das Volumen, wenn r = 10 cm ist?
                """.trimIndent()
            ),
            estimatedReadingTime = 28,
            isCompleted = true
        )

        val shapesChapter = Chapter(
            id = "chapter_geo_shapes",
            courseId = "course_geometry_1",
            chapterNumber = 1,
            title = t("Basic Shapes and Properties", "Format Bazë dhe Vetitë", "Grundformen und Eigenschaften"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Format Bazë dhe Vetitë
                    Gjeometria fillon me kuptimin e formave themelore dhe vetive të tyre.
                    ## Pikat, Vijat dhe Planet
                    - **Pika**: Një vendndodhje në hapësirë pa përmasa
                    - **Vija**: Zgjatet pafundësisht në të dy drejtimet
                    - **Plani**: Një sipërfaqe e sheshtë që zgjatet pafundësisht
                    ## Format Bazë
                    1. Trekëndëshi: 3 brinjë, shuma e këndeve 180°
                    2. Katrori: 4 brinjë të barabarta, 4 kënde të drejta
                    3. Drejtkëndëshi: 4 brinjë, brinjët e kundërta të barabarta
                    4. Rrethi: Të gjitha pikat njëlloj të larguara nga qendra
                    ## Vetitë
                    - Perimetri: Distanca rreth një forme
                    - Sipërfaqja: Hapësira brenda një forme
                    - Këndi: Masa e rrotullimit midis dy vijave
                """.trimIndent(),
                german = """
                    # Grundformen und Eigenschaften
                    Die Geometrie beginnt mit dem Verständnis der grundlegenden Formen und ihrer Eigenschaften.
                    ## Punkte, Linien und Ebenen
                    - **Punkt**: Ein Ort im Raum ohne Ausdehnung
                    - **Linie**: Erstreckt sich unendlich in beide Richtungen
                    - **Ebene**: Eine flache Oberfläche, die sich unendlich ausdehnt
                    ## Grundformen
                    1. Dreieck: 3 Seiten, Winkelsumme 180°
                    2. Quadrat: 4 gleiche Seiten, 4 rechte Winkel
                    3. Rechteck: 4 Seiten, gegenüberliegende Seiten gleich lang
                    4. Kreis: Alle Punkte sind vom Mittelpunkt gleich weit entfernt
                    ## Eigenschaften
                    - Umfang: Der Abstand um eine Form herum
                    - Fläche: Der Raum innerhalb einer Form
                    - Winkel: Das Maß der Drehung zwischen zwei Linien
                """.trimIndent()
            ),
            exercises = shapesExercises,
            estimatedReadingTime = 20,
            isCompleted = false
        )

        val anglesChapter = Chapter(
            id = "chapter_geo_angles",
            courseId = "course_geometry_1",
            chapterNumber = 2,
            title = t("Angles and Measurements", "Këndet dhe Matjet", "Winkel und Messungen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Këndet dhe Matjet
                    
                    Kuptimi i këndeve është vendimtar për zgjidhjen e problemeve gjeometrike.
                    
                    ## Llojet e Këndeve
                    1. **I ngushtë**: Më pak se 90°
                    2. **I drejtë**: Saktësisht 90°
                    3. **I gjerë**: Midis 90° dhe 180°
                    4. **I shtrirë**: Saktësisht 180°
                    
                    ## Marrëdhëniet e Këndeve
                    - Komplementarë: Dy kënde që e kanë shumën 90°
                    - Suplementarë: Dy kënde që e kanë shumën 180°
                    - Këndet e kundërta në kulm: Këndet e kundërta kur vijat priten
                    
                    ## Zbatimet
                    - Gjetja e këndeve që mungojnë në trekëndësha
                    - Vijat paralele dhe tërthoret
                    - Teoremat e këndeve në rreth
                """.trimIndent(),
                german = """
                    # Winkel und Messungen
                    
                    Das Verständnis von Winkeln ist entscheidend für die Lösung geometrischer Probleme.
                    
                    ## Arten von Winkeln
                    1. **Spitzer Winkel**: Weniger als 90°
                    2. **Rechter Winkel**: Genau 90°
                    3. **Stumpfer Winkel**: Zwischen 90° und 180°
                    4. **Gestreckter Winkel**: Genau 180°
                    
                    ## Winkelbeziehungen
                    - Komplementärwinkel: Zwei Winkel, deren Summe 90° beträgt
                    - Supplementärwinkel: Zwei Winkel, deren Summe 180° beträgt
                    - Scheitelwinkel: Gegenüberliegende Winkel, wenn sich Linien schneiden
                    
                    ## Anwendungen
                    - Finden fehlender Winkel in Dreiecken
                    - Parallele Linien und Transversalen
                    - Kreiswinkelsätze
                """.trimIndent()
            ),
            exercises = angleExercises,
            estimatedReadingTime = 25,
            isCompleted = false
        )

        val integralsChapter = Chapter(
            id = "chapter_calc_integrals",
            courseId = "course_calculus_1",
            chapterNumber = 4,
            title = t("Introduction to Integrals", "Hyrje në Integrale", "Einführung in Integrale"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Hyrje në Integrale
                    
                    Integrimi është procesi i kundërt i diferencimit.
                    
                    ## Integralët e pacaktuar
                    Antiderivati i një funksioni f(x) është F(x) ku F'(x) = f(x).
                    
                    ## Rregullat Themelore të Integrimit
                    1. ∫ x^n dx = x^(n+1)/(n+1) + C
                    2. ∫ sin(x) dx = -cos(x) + C
                    3. ∫ cos(x) dx = sin(x) + C
                    4. ∫ e^x dx = e^x + C
                    
                    ## Integralët e caktuar
                    Paraqesin sipërfaqen nën një kurbë midis dy pikave.
                    
                    ## Zbatimet
                    - Gjetja e sipërfaqeve nën kurba
                    - Llogaritja e vëllimeve të trupave të ngurtë
                    - Zbatime në fizikë (zhvendosja, puna)
                """.trimIndent(),
                german = """
                    # Einführung in Integrale
                    
                    Integration ist der umgekehrte Prozess der Differentiation.
                    
                    ## Unbestimmte Integrale
                    Die Stammfunktion einer Funktion f(x) ist F(x), wobei F'(x) = f(x).
                    
                    ## Grundlegende Integrationsregeln
                    1. ∫ x^n dx = x^(n+1)/(n+1) + C
                    2. ∫ sin(x) dx = -cos(x) + C
                    3. ∫ cos(x) dx = sin(x) + C
                    4. ∫ e^x dx = e^x + C
                    
                    ## Bestimmte Integrale
                    Stellen die Fläche unter einer Kurve zwischen zwei Punkten dar.
                    
                    ## Anwendungen
                    - Finden von Flächen unter Kurven
                    - Berechnung von Volumina von Körpern
                    - Physikalische Anwendungen (Verschiebung, Arbeit)
                """.trimIndent()
            ),
            estimatedReadingTime = 35,
            isCompleted = false
        )

        val calculusApplicationsChapter = Chapter(
            id = "chapter_calc_real_world",
            courseId = "course_calculus_1",
            chapterNumber = 5,
            title = t("Real-World Applications of Calculus", "Zbatime të Kalkulusit në Botën Reale", "Anwendungen der Infinitesimalrechnung in der realen Welt"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Zbatime të Kalkulusit në Botën Reale
                    
                    Kalkulusi është kudo në shkencë, inxhinieri dhe jetën e përditshme.
                    
                    ## Zbatime në Fizikë
                    - Lëvizja: pozicioni, shpejtësia, nxitimi
                    - Llogaritjet e punës dhe energjisë
                    - Funksionet e valëve dhe lëkundjet
                    
                    ## Zbatime në Ekonomi
                    - Kostoja marxhinale dhe të ardhurat
                    - Optimizimi i fitimit
                    - Modelet e rritjes
                    
                    ## Zbatime në Biologji
                    - Modelet e rritjes së popullsisë
                    - Shkalla e reaksioneve kimike
                    - Llogaritjet e rrjedhës së gjakut
                    
                    ## Zbatime në Inxhinieri
                    - Projektimi i urave dhe analiza e sforcimeve
                    - Analiza e qarqeve elektrike
                    - Përpunimi i sinjaleve
                """.trimIndent(),
                german = """
                    # Anwendungen der Infinitesimalrechnung in der realen Welt
                    
                    Die Infinitesimalrechnung ist überall in Wissenschaft, Ingenieurwesen und im täglichen Leben.
                    
                    ## Physikalische Anwendungen
                    - Bewegung: Position, Geschwindigkeit, Beschleunigung
                    - Berechnungen von Arbeit und Energie
                    - Wellenfunktionen und Schwingungen
                    
                    ## Wirtschaftliche Anwendungen
                    - Grenzkosten und Grenzerlös
                    - Gewinnoptimierung
                    - Wachstumsmodelle
                    
                    ## Biologische Anwendungen
                    - Bevölkerungswachstumsmodelle
                    - Geschwindigkeit chemischer Reaktionen
                    - Berechnungen des Blutflusses
                    
                    ## Ingenieurtechnische Anwendungen
                    - Brückenkonstruktion und Belastungsanalyse
                    - Analyse elektrischer Schaltungen
                    - Signalverarbeitung
                """.trimIndent()
            ),
            estimatedReadingTime = 30,
            isCompleted = false
        )

        val trigBasicsChapter = Chapter(
            id = "chapter_trig_basics",
            courseId = "course_trigonometry_1",
            chapterNumber = 1,
            title = t("Trigonometric Functions", "Funksionet Trigonometrike", "Trigonometrische Funktionen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Funksionet Trigonometrike
                    
                    Trigonometria studion marrëdhëniet midis këndeve dhe brinjëve në trekëndësha.
                    
                    ## Rrethi Njësi
                    Një rreth me rreze 1 me qendër në origjinë, themelor për trigonometrinë.
                    
                    ## Funksionet Kryesore
                    1. **Sinus (sin)**: koordinata y në rrethin njësi
                    2. **Kosinus (cos)**: koordinata x në rrethin njësi  
                    3. **Tangjent (tan)**: sin/cos = pjerrësia e rrezes
                    
                    ## Raportet në Trekëndëshin Kënddrejtë
                    - sin(θ) = kateti përballë/hipotenuza
                    - cos(θ) = kateti anëshkruar/hipotenuza
                    - tan(θ) = kateti përballë/kateti anëshkruar
                    
                    ## Këndet e Veçanta
                    - 30°, 45°, 60° dhe vlerat e tyre të sakta
                    - Analiza e kuadranteve për të gjithë këndet
                """.trimIndent(),
                german = """
                    # Trigonometrische Funktionen
                    
                    Die Trigonometrie untersucht die Beziehungen zwischen Winkeln und Seiten in Dreiecken.
                    
                    ## Der Einheitskreis
                    Ein Kreis mit Radius 1 im Ursprung, grundlegend für die Trigonometrie.
                    
                    ## Primärfunktionen
                    1. **Sinus (sin)**: y-Koordinate am Einheitskreis
                    2. **Kosinus (cos)**: x-Koordinate am Einheitskreis  
                    3. **Tangens (tan)**: sin/cos = Steigung des Radius
                    
                    ## Verhältnisse im rechtwinkligen Dreieck
                    - sin(θ) = Gegenkathete/Hypotenuse
                    - cos(θ) = Ankathete/Hypotenuse
                    - tan(θ) = Gegenkathete/Ankathete
                    
                    ## Spezielle Winkel
                    - 30°, 45°, 60° und ihre exakten Werte
                    - Quadrantenanalyse für alle Winkel
                """.trimIndent()
            ),
            estimatedReadingTime = 25,
            isCompleted = false
        )
        
        val trigIdentitiesChapter = Chapter(
            id = "chapter_trig_identities",
            courseId = "course_trigonometry_1",
            chapterNumber = 2,
            title = t("Trigonometric Identities", "Identitetet Trigonometrike", "Trigonometrische Identitäten"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Identitetet Trigonometrike
                    
                    Identitetet janë ekuacione që janë të vërteta për të gjitha vlerat e vlefshme.
                    
                    ## Identitetet Themelore
                    1. sin²(θ) + cos²(θ) = 1 (Identiteti Pitagorian)
                    2. tan(θ) = sin(θ)/cos(θ)
                    3. sec(θ) = 1/cos(θ)
                    
                    ## Formulat e Shumës dhe Diferencës
                    - sin(A ± B) = sin(A)cos(B) ± cos(A)sin(B)
                    - cos(A ± B) = cos(A)cos(B) ∓ sin(A)sin(B)
                    
                    ## Formulat e Këndit të Dyfishtë
                    - sin(2θ) = 2sin(θ)cos(θ)
                    - cos(2θ) = cos²(θ) - sin²(θ)
                    
                    ## Zbatimet
                    - Thjeshtimi i shprehjeve komplekse
                    - Zgjidhja e ekuacioneve trigonometrike
                    - Vërtetimi i identiteteve të tjera
                """.trimIndent(),
                german = """
                    # Trigonometrische Identitäten
                    
                    Identitäten sind Gleichungen, die für alle gültigen Werte wahr sind.
                    
                    ## Grundlegende Identitäten
                    1. sin²(θ) + cos²(θ) = 1 (Trigonometrischer Pythagoras)
                    2. tan(θ) = sin(θ)/cos(θ)
                    3. sec(θ) = 1/cos(θ)
                    
                    ## Summen- und Differenzformeln
                    - sin(A ± B) = sin(A)cos(B) ± cos(A)sin(B)
                    - cos(A ± B) = cos(A)cos(B) ∓ sin(A)sin(B)
                    
                    ## Doppelwinkelfunktionen
                    - sin(2θ) = 2sin(θ)cos(θ)
                    - cos(2θ) = cos²(θ) - sin²(θ)
                    
                    ## Anwendungen
                    - Vereinfachung komplexer Ausdrücke
                    - Lösen trigonometrischer Gleichungen
                    - Beweisen anderer Identitäten
                """.trimIndent()
            ),
            estimatedReadingTime = 30,
            isCompleted = false
        )
        
        val trigGraphsChapter = Chapter(
            id = "chapter_trig_graphs",
            courseId = "course_trigonometry_1",
            chapterNumber = 3,
            title = t("Trigonometric Graphs", "Grafikët Trigonometrikë", "Trigonometrische Graphen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Grafikët Trigonometrikë
                    
                    Kuptimi i sjelljes grafike të funksioneve trigonometrike.
                    
                    ## Grafikët e Sinusit dhe Kosinusit
                    - Perioda: 2π (360°)
                    - Amplituda: lartësia maksimale nga qendra
                    - Zhvendosja fazore: translacion horizontal
                    
                    ## Transformimet e Grafikëve
                    - y = A sin(Bx + C) + D
                    - A ndikon në amplitudë
                    - B ndikon në periodë (2π/B)
                    - C ndikon në zhvendosjen fazore
                    - D ndikon në zhvendosjen vertikale
                    
                    ## Grafiku i Tangjentit
                    - Perioda: π (180°)
                    - Asimptota vertikale në shumëfishat tek të π/2
                    - Bashkësia e vlerave: të gjithë numrat realë
                    
                    ## Zbatimet
                    - Modelimi i fenomeneve periodike
                    - Valët e zërit dhe muzika
                    - Ndryshimet sezonale të temperaturës
                """.trimIndent(),
                german = """
                    # Trigonometrische Graphen
                    
                    Das grafische Verhalten trigonometrischer Funktionen verstehen.
                    
                    ## Sinus- und Kosinusgraphen
                    - Periode: 2π (360°)
                    - Amplitude: maximale Höhe von der Mittellinie
                    - Phasenverschiebung: horizontale Verschiebung
                    
                    ## Graphentransformationen
                    - y = A sin(Bx + C) + D
                    - A beeinflusst die Amplitude
                    - B beeinflusst die Periode (2π/B)
                    - C beeinflusst die Phasenverschiebung
                    - D beeinflusst die vertikale Verschiebung
                    
                    ## Tangensgraph
                    - Periode: π (180°)
                    - Vertikale Asymptoten bei ungeraden Vielfachen von π/2
                    - Wertebereich: alle reellen Zahlen
                    
                    ## Anwendungen
                    - Modellierung periodischer Phänomene
                    - Schallwellen und Musik
                    - Saisonale Temperaturschwankungen
                """.trimIndent()
            ),
            estimatedReadingTime = 35,
            isCompleted = false
        )
        
        val trigApplicationsChapter = Chapter(
            id = "chapter_trig_applications",
            courseId = "course_trigonometry_1",
            chapterNumber = 4,
            title = t("Applications and Problem Solving", "Zbatime dhe Zgjidhje Problemash", "Anwendungen und Problemlösung"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Zbatime dhe Zgjidhje Problemash
                    
                    Përdorimi i trigonometrisë për të zgjidhur probleme të botës reale.
                    
                    ## Teorema e Sinuseve
                    Për çdo trekëndësh: a/sin(A) = b/sin(B) = c/sin(C)
                    
                    ## Teorema e Kosinuseve  
                    c² = a² + b² - 2ab cos(C)
                    
                    ## Zbatime në Botën Reale
                    1. **Navigim**: Gjetja e distancave dhe drejtimeve
                    2. **Arkitekturë**: Llogaritja e këndeve të çative dhe mbështetësve
                    3. **Astronomi**: Matja e distancave deri te yjet
                    4. **Inxhinieri**: Projektimi i urave dhe ndërtesave
                    
                    ## Hapat për Zgjidhjen e Problemeve
                    1. Vizato një diagram
                    2. Identifiko vlerat e njohura dhe të panjohura
                    3. Zgjidh teoremën ose formulën e duhur
                    4. Zgjidh dhe kontrollo logjikshmërinë
                """.trimIndent(),
                german = """
                    # Anwendungen und Problemlösung
                    
                    Anwendung der Trigonometrie zur Lösung realer Probleme.
                    
                    ## Sinussatz
                    Für jedes Dreieck: a/sin(A) = b/sin(B) = c/sin(C)
                    
                    ## Kosinussatz  
                    c² = a² + b² - 2ab cos(C)
                    
                    ## Anwendungen in der realen Welt
                    1. **Navigation**: Finden von Entfernungen und Peilungen
                    2. **Architektur**: Berechnung von Dachwinkeln und Stützen
                    3. **Astronomie**: Messung von Entfernungen zu Sternen
                    4. **Ingenieurwesen**: Brücken- und Gebäudedesign
                    
                    ## Schritte zur Problemlösung
                    1. Zeichnen Sie ein Diagramm
                    2. Identifizieren Sie bekannte und unbekannte Werte
                    3. Wählen Sie den passenden Satz oder die passende Formel
                    4. Lösen und auf Plausibilität prüfen
                """.trimIndent()
            ),
            estimatedReadingTime = 40,
            isCompleted = false
        )
        
        val statsDataChapter = Chapter(
            id = "chapter_stats_data",
            courseId = "course_statistics_1",
            chapterNumber = 1,
            title = t("Data Collection and Organization", "Mbledhja dhe Organizimi i të Dhënave", "Datenerfassung und -organisation"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Mbledhja dhe Organizimi i të Dhënave
                    
                    Statistika fillon me kuptimin se si të mblidhen dhe organizohen të dhënat.
                    
                    ## Llojet e të Dhënave
                    1. **Sasiore**: Të dhëna numerike (gjatësia, pesha, mosha)
                    2. **Cilësore**: Të dhëna kategorike (ngjyra, gjinia, marka)
                    
                    ## Metodat e Mbledhjes së të Dhënave
                    - Anketat dhe pyetësorët
                    - Eksperimentet dhe studimet e kontrolluara
                    - Studimet vëzhguese
                    - Teknikat e marrjes së mostrave
                    
                    ## Organizimi i të Dhënave
                    - Tabelat e frekuencave
                    - Të dhënat e grupuara dhe intervalet e klasave
                    - Diagramet kërcell-e-gjethe
                    - Bazat e vizualizimit të të dhënave
                    
                    ## Popullacioni vs Mostra
                    - Popullacioni: i gjithë grupi që studiohet
                    - Mostra: nënbashkësi e popullacionit
                    - Rëndësia e marrjes së mostrave përfaqësuese
                """.trimIndent(),
                german = """
                    # Datenerfassung und -organisation
                    
                    Statistik beginnt mit dem Verständnis, wie Daten gesammelt und organisiert werden.
                    
                    ## Arten von Daten
                    1. **Quantitativ**: Numerische Daten (Größe, Gewicht, Alter)
                    2. **Qualitativ**: Kategorische Daten (Farbe, Geschlecht, Marke)
                    
                    ## Datenerhebungsmethoden
                    - Umfragen und Fragebögen
                    - Experimente und kontrollierte Studien
                    - Beobachtungsstudien
                    - Stichprobenverfahren
                    
                    ## Datenorganisation
                    - Häufigkeitstabellen
                    - Gruppierte Daten und Klassenintervalle
                    - Stamm-Blatt-Diagramme
                    - Grundlagen der Datenvisualisierung
                    
                    ## Population vs. Stichprobe
                    - Population: die gesamte zu untersuchende Gruppe
                    - Stichprobe: Teilmenge der Population
                    - Bedeutung repräsentativer Stichproben
                """.trimIndent()
            ),
            estimatedReadingTime = 25,
            isCompleted = false
        )
        
        val statsMeasuresChapter = Chapter(
            id = "chapter_stats_measures",
            courseId = "course_statistics_1",
            chapterNumber = 2,
            title = t("Measures of Central Tendency", "Masat e Tendencës Qendrore", "Maße der zentralen Tendenz"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Masat e Tendencës Qendrore
                    
                    Kuptimi i "qendrës" ose vlerës tipike të një bashkësie të dhënash.
                    
                    ## Mesatarja Aritmetike
                    Shuma e të gjitha vlerave pjestuar me numrin e vlerave.
                    - E ndjeshme ndaj vlerave ekstreme
                    - Masa më e përdorur
                    
                    ## Mesorja
                    Vlera e mesit kur të dhënat janë të renditura.
                    - Më pak e ndikuar nga vlerat ekstreme
                    - Më e mirë për shpërndarje të anuar
                    
                    ## Moda
                    Vlera që paraqitet më shpesh.
                    - Mund të ketë disa moda
                    - E dobishme për të dhëna kategorike
                    
                    ## Kur të Përdoret Secila
                    - Mesatarja: shpërndarje normale, pa vlera ekstreme
                    - Mesorja: shpërndarje të anuar, prani e vlerave ekstreme
                    - Moda: të dhëna kategorike, gjetja e më të zakonshmes
                    
                    ## Llogaritja me Teknologji
                    Përdorimi i kalkulatorëve dhe softuerit për bashkësi të mëdha të dhënash.
                """.trimIndent(),
                german = """
                    # Maße der zentralen Tendenz
                    
                    Das Verständnis des „Zentrums“ oder des typischen Werts eines Datensatzes.
                    
                    ## Mittelwert (Durchschnitt)
                    Summe aller Werte geteilt durch die Anzahl der Werte.
                    - Empfindlich gegenüber Ausreißern
                    - Am häufigsten verwendetes Maß
                    
                    ## Median
                    Der mittlere Wert, wenn die Daten geordnet sind.
                    - Weniger von Ausreißern betroffen
                    - Besser für schiefe Verteilungen
                    
                    ## Modus
                    Der am häufigsten vorkommende Wert.
                    - Kann mehrere Modi haben
                    - Nützlich für kategoriale Daten
                    
                    ## Wann man was verwendet
                    - Mittelwert: Normalverteilungen, keine Ausreißer
                    - Median: schiefe Verteilungen, Ausreißer vorhanden
                    - Modus: kategoriale Daten, Suche nach dem häufigsten Wert
                    
                    ## Berechnung mit Technologie
                    Verwendung von Taschenrechnern und Software für große Datensätze.
                """.trimIndent()
            ),
            estimatedReadingTime = 20,
            isCompleted = false
        )
        
        val statsVariabilityChapter = Chapter(
            id = "chapter_stats_variability",
            courseId = "course_statistics_1",
            chapterNumber = 3,
            title = t("Measures of Variability", "Masat e Variabilitetit", "Streuungsmaße"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Masat e Variabilitetit
                    
                    Kuptimi se sa të shpërndara ose të ndryshueshme janë të dhënat.
                    
                    ## Amplituda
                    Diferenca midis vlerës maksimale dhe minimale.
                    - Masë e thjeshtë por e kufizuar
                    - Ndikohet nga vlerat ekstreme
                    
                    ## Varianca
                    Mesatarja e devijimeve në katror nga mesatarja.
                    - Varianca e popullacionit: σ²
                    - Varianca e mostrës: s²
                    
                    ## Devijimi Standard
                    Rrënja katrore e variancës, në të njëjtat njësi si të dhënat origjinale.
                    - Masa më e rëndësishme e shpërndarjes
                    - σ për popullacionin, s për mostrën
                    
                    ## Amplituda Ndërkuartile (IQR)
                    Amplituda e 50% të mesit të të dhënave.
                    - Q3 - Q1
                    - Rezistente ndaj vlerave ekstreme
                    
                    ## Zbatimet
                    - Krahasimi i variabilitetit midis bashkësive të të dhënave
                    - Kontrolli i cilësisë në prodhim
                    - Vlerësimi i rrezikut në financë
                """.trimIndent(),
                german = """
                    # Streuungsmaße
                    
                    Verständnis, wie gestreut oder variabel Daten sind.
                    
                    ## Spannweite
                    Differenz zwischen dem maximalen und minimalen Wert.
                    - Einfaches, aber begrenztes Maß
                    - Von Ausreißern beeinflusst
                    
                    ## Varianz
                    Durchschnitt der quadratischen Abweichungen vom Mittelwert.
                    - Populationsvarianz: σ²
                    - Stichprobenvarianz: s²
                    
                    ## Standardabweichung
                    Quadratwurzel der Varianz, in denselben Einheiten wie die Originaldaten.
                    - Wichtigstes Streuungsmaß
                    - σ für die Population, s für die Stichprobe
                    
                    ## Interquartilsabstand (IQR)
                    Spannweite der mittleren 50% der Daten.
                    - Q3 - Q1
                    - Resistent gegen Ausreißer
                    
                    ## Anwendungen
                    - Vergleich der Variabilität zwischen Datensätzen
                    - Qualitätskontrolle in der Fertigung
                    - Risikobewertung im Finanzwesen
                """.trimIndent()
            ),
            estimatedReadingTime = 30,
            isCompleted = false
        )
        
        val statsProbabilityChapter = Chapter(
            id = "chapter_stats_probability",
            courseId = "course_statistics_1",
            chapterNumber = 4,
            title = t("Introduction to Probability", "Hyrje në Probabilitet", "Einführung in die Wahrscheinlichkeit"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Hyrje në Probabilitet
                    
                    Kuptimi i pasigurisë dhe rastësisë në kontekste statistikore.
                    
                    ## Probabiliteti Bazë
                    Probabiliteti = Numri i rezultateve të favorshme / Numri total i rezultateve
                    - Vlera midis 0 dhe 1 (ose 0% deri 100%)
                    - P(ngjarje e sigurt) = 1
                    - P(ngjarje e pamundur) = 0
                    
                    ## Rregullat e Probabilitetit
                    1. Rregulli i mbledhjes: P(A ose B) = P(A) + P(B) - P(A dhe B)
                    2. Rregulli i shumëzimit: P(A dhe B) = P(A) × P(B|A)
                    3. Rregulli i komplementit: P(A') = 1 - P(A)
                    
                    ## Ngjarje të Pavarura vs të Varura
                    - Të pavarura: rezultati i njërës nuk ndikon te tjetra
                    - Të varura: rezultati i njërës ndikon te tjetra
                    
                    ## Zbatimet
                    - Lojërat e fatit
                    - Testimi mjekësor dhe diagnoza
                    - Parashikimi i motit
                    - Marrja e vendimeve në biznes
                """.trimIndent(),
                german = """
                    # Einführung in die Wahrscheinlichkeit
                    
                    Verständnis von Unsicherheit und Zufall in statistischen Kontexten.
                    
                    ## Grundlegende Wahrscheinlichkeit
                    Wahrscheinlichkeit = Anzahl der günstigen Ergebnisse / Gesamtzahl der Ergebnisse
                    - Werte zwischen 0 und 1 (oder 0 % bis 100 %)
                    - P(sicheres Ereignis) = 1
                    - P(unmögliches Ereignis) = 0
                    
                    ## Wahrscheinlichkeitsregeln
                    1. Additionsregel: P(A oder B) = P(A) + P(B) - P(A und B)
                    2. Multiplikationsregel: P(A und B) = P(A) × P(B|A)
                    3. Komplementärregel: P(A') = 1 - P(A)
                    
                    ## Unabhängige vs. Abhängige Ereignisse
                    - Unabhängig: Das Ergebnis des einen beeinflusst nicht das andere
                    - Abhängig: Das Ergebnis des einen beeinflusst das andere
                    
                    ## Anwendungen
                    - Glücksspiele
                    - Medizinische Tests und Diagnosen
                    - Wettervorhersage
                    - Geschäftsentscheidungen
                """.trimIndent()
            ),
            estimatedReadingTime = 35,
            isCompleted = false
        )
        
        val statsDistributionsChapter = Chapter(
            id = "chapter_stats_distributions",
            courseId = "course_statistics_1",
            chapterNumber = 5,
            title = t("Probability Distributions", "Shpërndarjet e Probabilitetit", "Wahrscheinlichkeitsverteilungen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Shpërndarjet e Probabilitetit
                    
                    Kuptimi se si probabiliteti shpërndahet në të gjitha rezultatet e mundshme.
                    
                    ## Shpërndarja Normale
                    - Kurbë në formë kambane, simetrike
                    - Mesatarja = Mesorja = Moda
                    - Rregulli 68-95-99.7 (rregulli empirik)
                    - Shpërndarja normale standarde (μ=0, σ=1)
                    
                    ## Shpërndarja Binomiale
                    - Numër i caktuar provash
                    - Çdo provë ka dy rezultate të mundshme
                    - Probabilitet konstant i suksesit
                    
                    ## Shpërndarje të Tjera të Rëndësishme
                    - Shpërndarja uniforme
                    - Shpërndarja eksponenciale
                    - Shpërndarja Poisson
                    
                    ## Rezultatet Z dhe Standardizimi
                    z = (x - μ) / σ
                    - Konvertimi në normalen standarde
                    - Gjetja e probabiliteteve dhe përqindjeve
                    
                    ## Zbatimet
                    - Kontrolli i cilësisë
                    - Interpretimi i rezultateve të testeve
                    - Tolerancat në prodhim
                """.trimIndent(),
                german = """
                    # Wahrscheinlichkeitsverteilungen
                    
                    Verständnis, wie die Wahrscheinlichkeit über alle möglichen Ergebnisse verteilt ist.
                    
                    ## Normalverteilung
                    - Glockenförmige, symmetrische Kurve
                    - Mittelwert = Median = Modus
                    - 68-95-99,7-Regel (empirische Regel)
                    - Standardnormalverteilung (μ=0, σ=1)
                    
                    ## Binomialverteilung
                    - Feste Anzahl von Versuchen
                    - Jeder Versuch hat zwei mögliche Ergebnisse
                    - Konstante Erfolgswahrscheinlichkeit
                    
                    ## Andere wichtige Verteilungen
                    - Gleichverteilung
                    - Exponentialverteilung
                    - Poisson-Verteilung
                    
                    ## Z-Werte und Standardisierung
                    z = (x - μ) / σ
                    - Umrechnung in Standardnormal
                    - Finden von Wahrscheinlichkeiten und Perzentilen
                    
                    ## Anwendungen
                    - Qualitätskontrolle
                    - Interpretation von Testergebnissen
                    - Fertigungstoleranzen
                """.trimIndent()
            ),
            estimatedReadingTime = 40,
            isCompleted = false
        )
        
        val statsInferenceChapter = Chapter(
            id = "chapter_stats_inference",
            courseId = "course_statistics_1",
            chapterNumber = 6,
            title = t("Statistical Inference", "Konkluzioni Statistikor", "Statistische Inferenz"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Konkluzioni Statistikor
                    
                    Nxjerrja e konkluzioneve për popullacionet bazuar në të dhënat e mostrës.
                    
                    ## Intervalet e Besimit
                    Gama e vlerave që ka gjasa të përmbajë parametrin e vërtetë të popullacionit.
                    - Niveli i besimit (90%, 95%, 99%)
                    - Marzhi i gabimit
                    - Vlerat kritike
                    
                    ## Testimi i Hipotezave
                    Procesi për testimin e pretendimeve rreth parametrave të popullacionit.
                    1. Parashtro hipotezat zero dhe alternative
                    2. Zgjidh nivelin e signifikancës (α)
                    3. Llogarit statistikën e testit
                    4. Merr vendim bazuar në vlerën p
                    
                    ## Llojet e Testeve
                    - Testi t me një mostër
                    - Testi t me dy mostra
                    - Testet Hi-katror
                    - ANOVA (një-drejtimëshe)
                    
                    ## Gabimet në Testimin e Hipotezave
                    - Gabimi i tipit I: Refuzimi i hipotezës zero të vërtetë
                    - Gabimi i tipit II: Dështimi për të refuzuar hipotezën zero të rreme
                    - Fuqia e një testi
                    
                    ## Zbatimet
                    - Kërkimi mjekësor
                    - Kërkimi i tregut
                    - Kontrolli i cilësisë
                    - Studimet shkencore
                """.trimIndent(),
                german = """
                    # Statistische Inferenz
                    
                    Schlussfolgerungen über Populationen auf der Grundlage von Stichprobendaten.
                    
                    ## Konfidenzintervalle
                    Bereich von Werten, der wahrscheinlich den wahren Populationsparameter enthält.
                    - Konfidenzniveau (90 %, 95 %, 99 %)
                    - Fehlermarge
                    - Kritische Werte
                    
                    ## Hypothesentests
                    Prozess zum Testen von Behauptungen über Populationsparameter.
                    1. Null- und Alternativhypothesen formulieren
                    2. Signifikanzniveau (α) wählen
                    3. Teststatistik berechnen
                    4. Entscheidung auf der Grundlage des p-Wertes treffen
                    
                    ## Arten von Tests
                    - Einstichproben-t-Test
                    - Zweistichproben-t-Test
                    - Chi-Quadrat-Tests
                    - ANOVA (einfaktoriell)
                    
                    ## Fehler beim Hypothesentest
                    - Fehler 1. Art: Ablehnung einer wahren Nullhypothese
                    - Fehler 2. Art: Nichtablehnung einer falschen Nullhypothese
                    - Macht eines Tests
                    
                    ## Anwendungen
                    - Medizinische Forschung
                    - Marktforschung
                    - Qualitätskontrolle
                    - Wissenschaftliche Studien
                """.trimIndent()
            ),
            estimatedReadingTime = 45,
            isCompleted = false
        )
        
        val advPolynomialsChapter = Chapter(
            id = "chapter_alg2_polynomials",
            courseId = "course_algebra_2",
            chapterNumber = 1,
            title = t("Advanced Polynomial Functions", "Funksionet Polinomiale të Avancuara", "Fortgeschrittene Polynomfunktionen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Funksionet Polinomiale të Avancuara
                    
                    Zgjerimi i njohurive mbi polinomet në shkallë më të larta dhe zbatime komplekse.
                    
                    ## Polinomet e Shkallës së Lartë
                    - Funksionet kubike (shkalla 3)
                    - Funksionet kuartike (shkalla 4)
                    - Sjellja e përgjithshme e polinomeve
                    
                    ## Veprimet me Polinome
                    - Teknika të avancuara faktorizimi
                    - Pjesëtimi sintetik
                    - Pjesëtimi i gjatë i polinomeve
                    - Teoremat e mbetjes dhe faktorit
                    
                    ## Rrënjët dhe Zerot
                    - Teorema themelore e algjebrës
                    - Teorema e rrënjës racionale
                    - Rrënjët komplekse dhe çiftet e konjuguara
                    - Shumëfishëria e rrënjëve
                    
                    ## Ndërtimi i Grafikëve të Polinomeve
                    - Analiza e sjelljes në skaje
                    - Gjetja e pikëprerjeve me boshtet x dhe y
                    - Maksimumet dhe minimumet lokale
                    - Skicimi i grafikëve të plotë
                    
                    ## Zbatimet
                    - Optimizimi i vëllimit dhe sipërfaqes
                    - Problemet e lëvizjes në fizikë
                    - Modelimi ekonomik
                """.trimIndent(),
                german = """
                    # Fortgeschrittene Polynomfunktionen
                    
                    Erweiterung des Polynomwissens auf höhere Grade und komplexe Anwendungen.
                    
                    ## Polynome höheren Grades
                    - Kubische Funktionen (Grad 3)
                    - Quartische Funktionen (Grad 4)
                    - Allgemeines Polynomverhalten
                    
                    ## Polynomoperationen
                    - Fortgeschrittene Faktorisierungstechniken
                    - Synthetische Division
                    - Polynomlange Division
                    - Rest- und Faktorsätze
                    
                    ## Wurzeln und Nullstellen
                    - Fundamentalsatz der Algebra
                    - Satz über rationale Nullstellen
                    - Komplexe Wurzeln und konjugierte Paare
                    - Vielfachheit von Wurzeln
                    
                    ## Grafische Darstellung von Polynomen
                    - Analyse des Endverhaltens
                    - Finden von x- und y-Achsenabschnitten
                    - Lokale Maxima und Minima
                    - Skizzieren vollständiger Graphen
                    
                    ## Anwendungen
                    - Volumen- und Flächenoptimierung
                    - Bewegungsprobleme in der Physik
                    - Ökonomische Modellierung
                """.trimIndent()
            ),
            estimatedReadingTime = 35,
            isCompleted = false
        )
        
        val rationalFunctionsChapter = Chapter(
            id = "chapter_alg2_rational",
            courseId = "course_algebra_2",
            chapterNumber = 2,
            title = t("Rational Functions", "Funksionet Racionale", "Rationale Funktionen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Funksionet Racionale
                    
                    Funksione që janë raporte të funksioneve polinomiale.
                    
                    ## Përkufizimi dhe Fusha e Përcaktimit
                    f(x) = P(x)/Q(x) ku P dhe Q janë polinome
                    - Fusha e përcaktimit: të gjithë numrat realë përveç aty ku Q(x) = 0
                    - Asimptota vertikale në zerot e emëruesit
                    
                    ## Asimptotat
                    1. **Vertikale**: vlerat e x ku funksioni është i papërcaktuar
                    2. **Horizontale**: sjellja kur x i afrohet ±∞
                    3. **E pjerrët**: kur shkalla e numëruesit > shkalla e emëruesit
                    
                    ## Ndërtimi i Grafikëve të Funksioneve Racionale
                    - Gjej fushën e përcaktimit dhe asimptotat
                    - Gjej pikëprerjet me boshtet x dhe y
                    - Testo sjelljen afër asimptotave
                    - Kontrollo sjelljen në skaje
                    
                    ## Veprimet me Funksione Racionale
                    - Mbledhja dhe zbritja
                    - Shumëzimi dhe pjesëtimi
                    - Teknikat e thjeshtimit
                    
                    ## Zbatimet
                    - Problemet e normës
                    - Problemet e përqendrimit
                    - Analiza kosto-përfitim
                """.trimIndent(),
                german = """
                    # Rationale Funktionen
                    
                    Funktionen, die Verhältnisse von Polynomfunktionen sind.
                    
                    ## Definition und Definitionsbereich
                    f(x) = P(x)/Q(x) wobei P und Q Polynome sind
                    - Definitionsbereich: alle reellen Zahlen außer dort, wo Q(x) = 0
                    - Vertikale Asymptoten bei den Nullstellen des Nenners
                    
                    ## Asymptoten
                    1. **Vertikal**: x-Werte, bei denen die Funktion undefiniert ist
                    2. **Horizontal**: Verhalten, wenn x sich ±∞ nähert
                    3. **Schräg**: wenn der Grad des Zählers > Grad des Nenners
                    
                    ## Grafische Darstellung rationaler Funktionen
                    - Definitionsbereich und Asymptoten finden
                    - x- und y-Achsenabschnitte lokalisieren
                    - Verhalten in der Nähe von Asymptoten testen
                    - Endverhalten prüfen
                    
                    ## Operationen mit rationalen Funktionen
                    - Addition und Subtraktion
                    - Multiplikation und Division
                    - Vereinfachungstechniken
                    
                    ## Anwendungen
                    - Ratenprobleme
                    - Konzentrationsprobleme
                    - Kosten-Nutzen-Analyse
                """.trimIndent()
            ),
            estimatedReadingTime = 40,
            isCompleted = false
        )
        
        val exponentialChapter = Chapter(
            id = "chapter_alg2_exponential",
            courseId = "course_algebra_2",
            chapterNumber = 3,
            title = t("Exponential and Logarithmic Functions", "Funksionet Eksponenciale dhe Logaritmike", "Exponential- und Logarithmusfunktionen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Funksionet Eksponenciale dhe Logaritmike
                    
                    Funksione që përfshijnë eksponentë dhe marrëdhëniet e tyre të anasjellta.
                    
                    ## Funksionet Eksponenciale
                    f(x) = a·b^x ku a > 0, b > 0, b ≠ 1
                    - Rritja (b > 1) vs rënia (0 < b < 1)
                    - Pikëprerja me boshtin y në (0, a)
                    - Asimptota horizontale në y = 0
                    
                    ## Baza Natyrore e
                    e ≈ 2.71828...
                    - f(x) = e^x (funksioni eksponencial natyror)
                    - Interesi i përbërë i vazhdueshëm
                    - Modelet e rritjes dhe rënies
                    
                    ## Funksionet Logaritmike
                    y = log_b(x) nëse dhe vetëm nëse x = b^y
                    - Fusha e përcaktimit: x > 0
                    - Asimptota vertikale në x = 0
                    - Pikëprerja me boshtin x në (1, 0)
                    
                    ## Vetitë e Logaritmeve
                    1. log_b(xy) = log_b(x) + log_b(y)
                    2. log_b(x/y) = log_b(x) - log_b(y)
                    3. log_b(x^n) = n·log_b(x)
                    
                    ## Zbatimet
                    - Rritja e popullsisë
                    - Zbërthimi radioaktiv
                    - Shkallët pH dhe Richter
                    - Llogaritjet e investimeve
                """.trimIndent(),
                german = """
                    # Exponential- und Logarithmusfunktionen
                    
                    Funktionen, die Exponenten und ihre inversen Beziehungen beinhalten.
                    
                    ## Exponentialfunktionen
                    f(x) = a·b^x wobei a > 0, b > 0, b ≠ 1
                    - Wachstum (b > 1) vs. Zerfall (0 < b < 1)
                    - y-Achsenabschnitt bei (0, a)
                    - Horizontale Asymptote bei y = 0
                    
                    ## Die natürliche Basis e
                    e ≈ 2.71828...
                    - f(x) = e^x (natürliche Exponentialfunktion)
                    - Stetige Zinseszinsrechnung
                    - Wachstums- und Zerfallsmodelle
                    
                    ## Logarithmusfunktionen
                    y = log_b(x) genau dann, wenn x = b^y
                    - Definitionsbereich: x > 0
                    - Vertikale Asymptote bei x = 0
                    - x-Achsenabschnitt bei (1, 0)
                    
                    ## Eigenschaften von Logarithmen
                    1. log_b(xy) = log_b(x) + log_b(y)
                    2. log_b(x/y) = log_b(x) - log_b(y)
                    3. log_b(x^n) = n·log_b(x)
                    
                    ## Anwendungen
                    - Bevölkerungswachstum
                    - Radioaktiver Zerfall
                    - pH- und Richterskalen
                    - Investitionsberechnungen
                """.trimIndent()
            ),
            estimatedReadingTime = 45,
            isCompleted = false
        )
        
        val systemsChapter = Chapter(
            id = "chapter_alg2_systems",
            courseId = "course_algebra_2",
            chapterNumber = 4,
            title = t("Systems of Equations and Inequalities", "Sistemet e Ekuacioneve dhe Inekuacioneve", "Gleichungs- und Ungleichungssysteme"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Sistemet e Ekuacioneve dhe Inekuacioneve
                    
                    Zgjidhja e disa ekuacioneve ose inekuacioneve njëkohësisht.
                    
                    ## Sistemet Lineare
                    - Dy variabla: grafiku, zëvendësimi, eliminimi
                    - Tre variabla: metoda e eliminimit
                    - Paraqitja me matrica dhe zgjidhjet
                    
                    ## Sistemet Jolineare
                    - Sistemet linearo-kuadratike
                    - Sistemet kuadratiko-kuadratike
                    - Strategjitë e zëvendësimit dhe eliminimit
                    
                    ## Sistemet e Inekuacioneve
                    - Ndërtimi i grafikut të zonave të zgjidhjes
                    - Bazat e programimit linear
                    - Optimizimi me kufizime
                    
                    ## Metodat me Matrica
                    - Eliminimi Gaussian
                    - Veprimet me matrica
                    - Përcaktorët dhe rregulli i Cramer-it
                    - Matricat inverse
                    
                    ## Zbatimet
                    - Optimizimi në biznes
                    - Alokimi i burimeve
                    - Problemet e përzierjes
                    - Analiza e pikës kritike
                """.trimIndent(),
                german = """
                    # Gleichungs- und Ungleichungssysteme
                    
                    Gleichzeitiges Lösen mehrerer Gleichungen oder Ungleichungen.
                    
                    ## Lineare Systeme
                    - Zwei Variablen: grafische Darstellung, Substitution, Elimination
                    - Drei Variablen: Eliminationsverfahren
                    - Matrixdarstellung und Lösungen
                    
                    ## Nichtlineare Systeme
                    - Linear-quadratische Systeme
                    - Quadratisch-quadratische Systeme
                    - Substitutions- und Eliminationsstrategien
                    
                    ## Ungleichungssysteme
                    - Grafische Darstellung von Lösungsbereichen
                    - Grundlagen der linearen Programmierung
                    - Optimierung mit Nebenbedingungen
                    
                    ## Matrixmethoden
                    - Gauß-Elimination
                    - Matrixoperationen
                    - Determinanten und Cramersche Regel
                    - Inverse Matrizen
                    
                    ## Anwendungen
                    - Unternehmensoptimierung
                    - Ressourcenallokation
                    - Mischungsprobleme
                    - Break-Even-Analyse
                """.trimIndent()
            ),
            estimatedReadingTime = 50,
            isCompleted = false
        )
        
        val conicsChapter = Chapter(
            id = "chapter_alg2_conics",
            courseId = "course_algebra_2",
            chapterNumber = 5,
            title = t("Conic Sections", "Seksionet Konike", "Kegelschnitte"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Seksionet Konike
                    
                    Kurba të formuara nga prerja e një koni me një plan.
                    
                    ## Rrathët
                    (x - h)² + (y - k)² = r²
                    - Qendra në (h, k)
                    - Rrezja r
                    - Format standarde dhe të përgjithshme
                    
                    ## Parabolat
                    - Forma me kulm: y = a(x - h)² + k
                    - Vatra dhe vija drejtuese
                    - Orientimet horizontale dhe vertikale
                    
                    ## Elipset
                    (x - h)²/a² + (y - k)²/b² = 1
                    - Qendra në (h, k)
                    - Boshtet i madh dhe i vogël
                    - Vatrat dhe jashtëqendërsia
                    
                    ## Hiperbolat
                    (x - h)²/a² - (y - k)²/b² = 1
                    - Qendra në (h, k)
                    - Asimptotat dhe degët
                    - Vatrat dhe jashtëqendërsia
                    
                    ## Zbatimet
                    - Orbitat e satelitëve
                    - Pasqyrat e teleskopëve
                    - Arkitektura dhe dizajni
                    - Trajektoret në fizikë
                """.trimIndent(),
                german = """
                    # Kegelschnitte
                    
                    Kurven, die durch den Schnitt eines Kegels mit einer Ebene entstehen.
                    
                    ## Kreise
                    (x - h)² + (y - k)² = r²
                    - Mittelpunkt bei (h, k)
                    - Radius r
                    - Standard- und allgemeine Formen
                    
                    ## Parabeln
                    - Scheitelpunktform: y = a(x - h)² + k
                    - Brennpunkt und Leitlinie
                    - Horizontale und vertikale Ausrichtungen
                    
                    ## Ellipsen
                    (x - h)²/a² + (y - k)²/b² = 1
                    - Mittelpunkt bei (h, k)
                    - Haupt- und Nebenachsen
                    - Brennpunkte und Exzentrizität
                    
                    ## Hyperbeln
                    (x - h)²/a² - (y - k)²/b² = 1
                    - Mittelpunkt bei (h, k)
                    - Asymptoten und Zweige
                    - Brennpunkte und Exzentrizität
                    
                    ## Anwendungen
                    - Satellitenbahnen
                    - Teleskopspiegel
                    - Architektur und Design
                    - Physikalische Flugbahnen
                """.trimIndent()
            ),
            estimatedReadingTime = 40,
            isCompleted = false
        )
        
        val sequencesChapter = Chapter(
            id = "chapter_alg2_sequences",
            courseId = "course_algebra_2",
            chapterNumber = 6,
            title = t("Sequences and Series", "Vargjet dhe Seritë", "Folgen und Reihen"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Vargjet dhe Seritë
                    
                    Lista të renditura numrash dhe shumat e tyre.
                    
                    ## Vargjet
                    - Përkufizimi dhe shënimi: a₁, a₂, a₃, ...
                    - Gjetja e modeleve dhe kufizave të n-ta
                    - Formula rekurrente vs eksplicite
                    
                    ## Vargjet Aritmetike
                    - Diferenca e përbashkët: d
                    - Kufiza e n-të: aₙ = a₁ + (n-1)d
                    - Mesataret aritmetike
                    
                    ## Vargjet Gjeometrike
                    - Raporti i përbashkët: r
                    - Kufiza e n-të: aₙ = a₁ · r^(n-1)
                    - Mesataret gjeometrike
                    
                    ## Seritë
                    - Shuma e kufizave të vargut
                    - Seritë aritmetike: Sₙ = n(a₁ + aₙ)/2
                    - Seritë gjeometrike: Sₙ = a₁(1-r^n)/(1-r)
                    
                    ## Seritë e pafundme
                    - Konvergjente vs divergjente
                    - Shuma e serive gjeometrike të pafundme
                    
                    ## Zbatimet
                    - Investimet dhe rentat vjetore
                    - Modelet e rritjes së popullsisë
                    - Lëkundjet në fizikë
                """.trimIndent(),
                german = """
                    # Folgen und Reihen
                    
                    Geordnete Listen von Zahlen und deren Summen.
                    
                    ## Folgen
                    - Definition und Notation: a₁, a₂, a₃, ...
                    - Muster und n-te Glieder finden
                    - Rekursive vs. explizite Formeln
                    
                    ## Arithmetische Folgen
                    - Gemeinsame Differenz: d
                    - n-tes Glied: aₙ = a₁ + (n-1)d
                    - Arithmetische Mittel
                    
                    ## Geometrische Folgen
                    - Gemeinsames Verhältnis: r
                    - n-tes Glied: aₙ = a₁ · r^(n-1)
                    - Geometrische Mittel
                    
                    ## Reihen
                    - Summe der Glieder einer Folge
                    - Arithmetische Reihe: Sₙ = n(a₁ + aₙ)/2
                    - Geometrische Reihe: Sₙ = a₁(1-r^n)/(1-r)
                    
                    ## Unendliche Reihen
                    - Konvergent vs. divergent
                    - Summe unendlicher geometrischer Reihen
                    
                    ## Anwendungen
                    - Investitionen und Annuitäten
                    - Bevölkerungswachstumsmodelle
                    - Physikalische Schwingungen
                """.trimIndent()
            ),
            estimatedReadingTime = 35,
            isCompleted = false
        )
        
        val probabilityChapter = Chapter(
            id = "chapter_alg2_probability",
            courseId = "course_algebra_2",
            chapterNumber = 7,
            title = t("Advanced Probability and Statistics", "Probabilitet i Avancuar dhe Statistikë", "Fortgeschrittene Wahrscheinlichkeit und Statistik"),
            markdownContent = tMarkdown(
                english = """
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
                albanian = """
                    # Probabilitet i Avancuar dhe Statistikë
                    
                    Eksplorim më i thelluar i koncepteve të probabilitetit dhe analizës statistikore.
                    
                    ## Parimet e Numërimit
                    - Parimi themelor i numërimit
                    - Përkëmbimet: P(n,r) = n!/(n-r)!
                    - Kombinimet: C(n,r) = n!/[r!(n-r)!]
                    - Zbatimet në probabilitet
                    
                    ## Probabiliteti Binomial
                    - Eksperimentet binomiale
                    - Formula e probabilitetit binomial
                    - Teorema e binomit dhe zgjerimi
                    - Pritja matematike dhe varianca
                    
                    ## Shpërndarja Normale
                    - Kurba normale standarde
                    - Rezultatet Z dhe probabiliteti
                    - Zbatimet në të dhëna reale
                    
                    ## Analiza Statistikore
                    - Marrja e mostrave dhe anësia
                    - Intervalet e besimit
                    - Bazat e testimit të hipotezave
                    - Korrelacioni vs shkakësia
                    
                    ## Zbatime të Avancuara
                    - Kontrolli i cilësisë
                    - Saktësia e testimit mjekësor
                    - Analitika sportive
                    - Marrja e vendimeve në biznes
                """.trimIndent(),
                german = """
                    # Fortgeschrittene Wahrscheinlichkeit und Statistik
                    
                    Tiefere Untersuchung von Wahrscheinlichkeitskonzepten und statistischer Analyse.
                    
                    ## Zählprinzipien
                    - Fundamentales Zählprinzip
                    - Permutationen: P(n,r) = n!/(n-r)!
                    - Kombinationen: C(n,r) = n!/[r!(n-r)!]
                    - Anwendungen auf die Wahrscheinlichkeit
                    
                    ## Binomialwahrscheinlichkeit
                    - Binomialexperimente
                    - Binomialwahrscheinlichkeitsformel
                    - Binomischer Lehrsatz und Entwicklung
                    - Erwartungswert und Varianz
                    
                    ## Normalverteilung
                    - Standardnormalverteilungskurve
                    - Z-Werte und Wahrscheinlichkeit
                    - Anwendungen auf reale Daten
                    
                    ## Statistische Analyse
                    - Stichproben und Verzerrung
                    - Konfidenzintervalle
                    - Grundlagen des Hypothesentestens
                    - Korrelation vs. Kausalität
                    
                    ## Fortgeschrittene Anwendungen
                    - Qualitätskontrolle
                    - Genauigkeit medizinischer Tests
                    - Sportanalytik
                    - Geschäftsentscheidungen
                """.trimIndent()
            ),
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
         
       
         
      
        // Create mock exercise help
        val mockHelp1 = ExerciseHelp(
            id = "help_linear_1",
            exerciseId = "exercise_linear_1",
            userId = "user_demo",
            incorrectAnswer = 0, // User chose x = 5
            correctAnswer = 1, // Correct answer is x = 6
            userQuestion = t(
                "Why isn't x = 5 correct?",
                "Pse nuk është x = 5 e saktë?",
                "Warum ist x = 5 nicht richtig?"
            ).en, // Assuming userQuestion is a simple string
            helpType = HelpType.LOCAL_AI,
            explanation = t(
                "You correctly subtracted 7, but when you have 2x = 12, you need to divide both sides by 2, which gives x = 6, not x = 5.",
                "Ju zbritët saktë 7, por kur keni 2x = 12, duhet të pjesëtoni të dyja anët me 2, gjë që jep x = 6, jo x = 5.",
                "Sie haben 7 korrekt subtrahiert, aber wenn Sie 2x = 12 haben, müssen Sie beide Seiten durch 2 teilen, was x = 6 ergibt, nicht x = 5."
            ).en, // Assuming explanation is a simple string
            videoExplanation = null,
            wasHelpful = true
        )
         
         exerciseHelp.add(mockHelp1)
     }
 } 