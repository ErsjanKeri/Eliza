package com.example.ai.edge.eliza.ai.rag;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GeneralRagProvider_Factory implements Factory<GeneralRagProvider> {
  @Override
  public GeneralRagProvider get() {
    return newInstance();
  }

  public static GeneralRagProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GeneralRagProvider newInstance() {
    return new GeneralRagProvider();
  }

  private static final class InstanceHolder {
    static final GeneralRagProvider_Factory INSTANCE = new GeneralRagProvider_Factory();
  }
}
