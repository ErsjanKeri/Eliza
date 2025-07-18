package com.example.ai.edge.eliza.ai.modelmanager;

import android.content.Context;
import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ElizaModelManager_Factory implements Factory<ElizaModelManager> {
  private final Provider<Context> contextProvider;

  private final Provider<ModelDownloadRepository> downloadRepositoryProvider;

  private final Provider<ElizaInferenceHelper> inferenceHelperProvider;

  public ElizaModelManager_Factory(Provider<Context> contextProvider,
      Provider<ModelDownloadRepository> downloadRepositoryProvider,
      Provider<ElizaInferenceHelper> inferenceHelperProvider) {
    this.contextProvider = contextProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.inferenceHelperProvider = inferenceHelperProvider;
  }

  @Override
  public ElizaModelManager get() {
    return newInstance(contextProvider.get(), downloadRepositoryProvider.get(), inferenceHelperProvider.get());
  }

  public static ElizaModelManager_Factory create(Provider<Context> contextProvider,
      Provider<ModelDownloadRepository> downloadRepositoryProvider,
      Provider<ElizaInferenceHelper> inferenceHelperProvider) {
    return new ElizaModelManager_Factory(contextProvider, downloadRepositoryProvider, inferenceHelperProvider);
  }

  public static ElizaModelManager newInstance(Context context,
      ModelDownloadRepository downloadRepository, ElizaInferenceHelper inferenceHelper) {
    return new ElizaModelManager(context, downloadRepository, inferenceHelper);
  }
}
