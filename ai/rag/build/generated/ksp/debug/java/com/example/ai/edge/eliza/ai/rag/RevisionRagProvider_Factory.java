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
public final class RevisionRagProvider_Factory implements Factory<RevisionRagProvider> {
  private final Provider<CourseRepository> courseRepositoryProvider;

  public RevisionRagProvider_Factory(Provider<CourseRepository> courseRepositoryProvider) {
    this.courseRepositoryProvider = courseRepositoryProvider;
  }

  @Override
  public RevisionRagProvider get() {
    return newInstance(courseRepositoryProvider.get());
  }

  public static RevisionRagProvider_Factory create(
      Provider<CourseRepository> courseRepositoryProvider) {
    return new RevisionRagProvider_Factory(courseRepositoryProvider);
  }

  public static RevisionRagProvider newInstance(CourseRepository courseRepository) {
    return new RevisionRagProvider(courseRepository);
  }
}
