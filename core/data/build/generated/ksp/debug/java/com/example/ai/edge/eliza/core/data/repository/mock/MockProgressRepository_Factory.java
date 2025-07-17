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
public final class MockProgressRepository_Factory implements Factory<MockProgressRepository> {
  @Override
  public MockProgressRepository get() {
    return newInstance();
  }

  public static MockProgressRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockProgressRepository newInstance() {
    return new MockProgressRepository();
  }

  private static final class InstanceHolder {
    static final MockProgressRepository_Factory INSTANCE = new MockProgressRepository_Factory();
  }
}
