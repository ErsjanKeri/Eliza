package com.example.ai.edge.eliza.core.data.chat;

import com.example.ai.edge.eliza.core.data.repository.CourseRepository;
import com.example.ai.edge.eliza.core.data.repository.ProgressRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ExerciseRagProvider_Factory implements Factory<ExerciseRagProvider> {
  private final Provider<CourseRepository> courseRepositoryProvider;

  private final Provider<ProgressRepository> progressRepositoryProvider;

  public ExerciseRagProvider_Factory(Provider<CourseRepository> courseRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider) {
    this.courseRepositoryProvider = courseRepositoryProvider;
    this.progressRepositoryProvider = progressRepositoryProvider;
  }

  @Override
  public ExerciseRagProvider get() {
    return newInstance(courseRepositoryProvider.get(), progressRepositoryProvider.get());
  }

  public static ExerciseRagProvider_Factory create(
      Provider<CourseRepository> courseRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider) {
    return new ExerciseRagProvider_Factory(courseRepositoryProvider, progressRepositoryProvider);
  }

  public static ExerciseRagProvider newInstance(CourseRepository courseRepository,
      ProgressRepository progressRepository) {
    return new ExerciseRagProvider(courseRepository, progressRepository);
  }
}
