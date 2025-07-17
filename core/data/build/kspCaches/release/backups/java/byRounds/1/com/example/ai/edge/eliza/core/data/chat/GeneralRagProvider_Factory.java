package com.example.ai.edge.eliza.core.data.chat;

import com.example.ai.edge.eliza.core.data.repository.CourseRepository;
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
public final class GeneralRagProvider_Factory implements Factory<GeneralRagProvider> {
  private final Provider<CourseRepository> courseRepositoryProvider;

  public GeneralRagProvider_Factory(Provider<CourseRepository> courseRepositoryProvider) {
    this.courseRepositoryProvider = courseRepositoryProvider;
  }

  @Override
  public GeneralRagProvider get() {
    return newInstance(courseRepositoryProvider.get());
  }

  public static GeneralRagProvider_Factory create(
      Provider<CourseRepository> courseRepositoryProvider) {
    return new GeneralRagProvider_Factory(courseRepositoryProvider);
  }

  public static GeneralRagProvider newInstance(CourseRepository courseRepository) {
    return new GeneralRagProvider(courseRepository);
  }
}
