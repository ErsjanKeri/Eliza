package com.example.ai.edge.eliza.ai.modelmanager.impl;

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
public final class ModelDownloadRepositoryImpl_Factory implements Factory<ModelDownloadRepositoryImpl> {
  @Override
  public ModelDownloadRepositoryImpl get() {
    return newInstance();
  }

  public static ModelDownloadRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ModelDownloadRepositoryImpl newInstance() {
    return new ModelDownloadRepositoryImpl();
  }

  private static final class InstanceHolder {
    static final ModelDownloadRepositoryImpl_Factory INSTANCE = new ModelDownloadRepositoryImpl_Factory();
  }
}
