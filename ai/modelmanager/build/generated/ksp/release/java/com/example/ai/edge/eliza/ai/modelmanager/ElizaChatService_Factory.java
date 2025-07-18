package com.example.ai.edge.eliza.ai.modelmanager;

import com.example.ai.edge.eliza.ai.inference.ElizaInferenceHelper;
import com.example.ai.edge.eliza.ai.rag.RagProviderFactory;
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
public final class ElizaChatService_Factory implements Factory<ElizaChatService> {
  private final Provider<ElizaInferenceHelper> inferenceHelperProvider;

  private final Provider<RagProviderFactory> ragProviderFactoryProvider;

  public ElizaChatService_Factory(Provider<ElizaInferenceHelper> inferenceHelperProvider,
      Provider<RagProviderFactory> ragProviderFactoryProvider) {
    this.inferenceHelperProvider = inferenceHelperProvider;
    this.ragProviderFactoryProvider = ragProviderFactoryProvider;
  }

  @Override
  public ElizaChatService get() {
    return newInstance(inferenceHelperProvider.get(), ragProviderFactoryProvider.get());
  }

  public static ElizaChatService_Factory create(
      Provider<ElizaInferenceHelper> inferenceHelperProvider,
      Provider<RagProviderFactory> ragProviderFactoryProvider) {
    return new ElizaChatService_Factory(inferenceHelperProvider, ragProviderFactoryProvider);
  }

  public static ElizaChatService newInstance(ElizaInferenceHelper inferenceHelper,
      RagProviderFactory ragProviderFactory) {
    return new ElizaChatService(inferenceHelper, ragProviderFactory);
  }
}
