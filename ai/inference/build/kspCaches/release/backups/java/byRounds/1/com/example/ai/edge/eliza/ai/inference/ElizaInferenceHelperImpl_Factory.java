package com.example.ai.edge.eliza.ai.inference;

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
public final class ElizaInferenceHelperImpl_Factory implements Factory<ElizaInferenceHelperImpl> {
  @Override
  public ElizaInferenceHelperImpl get() {
    return newInstance();
  }

  public static ElizaInferenceHelperImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ElizaInferenceHelperImpl newInstance() {
    return new ElizaInferenceHelperImpl();
  }

  private static final class InstanceHolder {
    static final ElizaInferenceHelperImpl_Factory INSTANCE = new ElizaInferenceHelperImpl_Factory();
  }
}
