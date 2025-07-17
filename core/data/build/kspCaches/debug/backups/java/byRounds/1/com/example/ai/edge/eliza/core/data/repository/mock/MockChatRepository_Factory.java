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
public final class MockChatRepository_Factory implements Factory<MockChatRepository> {
  @Override
  public MockChatRepository get() {
    return newInstance();
  }

  public static MockChatRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockChatRepository newInstance() {
    return new MockChatRepository();
  }

  private static final class InstanceHolder {
    static final MockChatRepository_Factory INSTANCE = new MockChatRepository_Factory();
  }
}
