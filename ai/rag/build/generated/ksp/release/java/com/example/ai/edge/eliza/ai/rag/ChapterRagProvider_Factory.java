package com.example.ai.edge.eliza.ai.rag;

import com.example.ai.edge.eliza.core.data.repository.CourseRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ChapterRagProvider_Factory implements Factory<ChapterRagProvider> {
  private final Provider<CourseRepository> courseRepositoryProvider;

  public ChapterRagProvider_Factory(Provider<CourseRepository> courseRepositoryProvider) {
    this.courseRepositoryProvider = courseRepositoryProvider;
  }

  @Override
  public ChapterRagProvider get() {
    return newInstance(courseRepositoryProvider.get());
  }

  public static ChapterRagProvider_Factory create(
      Provider<CourseRepository> courseRepositoryProvider) {
    return new ChapterRagProvider_Factory(courseRepositoryProvider);
  }

  public static ChapterRagProvider newInstance(CourseRepository courseRepository) {
    return new ChapterRagProvider(courseRepository);
  }
}
