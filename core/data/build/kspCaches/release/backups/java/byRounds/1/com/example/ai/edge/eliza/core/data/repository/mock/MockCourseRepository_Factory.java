package com.example.ai.edge.eliza.core.data.repository.mock;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MockCourseRepository_Factory implements Factory<MockCourseRepository> {
  @Override
  public MockCourseRepository get() {
    return newInstance();
  }

  public static MockCourseRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockCourseRepository newInstance() {
    return new MockCourseRepository();
  }

  private static final class InstanceHolder {
    static final MockCourseRepository_Factory INSTANCE = new MockCourseRepository_Factory();
  }
}
