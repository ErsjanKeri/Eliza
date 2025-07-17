package com.example.ai.edge.eliza.core.data.chat;

import com.example.ai.edge.eliza.core.data.repository.ProgressRepository;
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
public final class RevisionRagProvider_Factory implements Factory<RevisionRagProvider> {
  private final Provider<ProgressRepository> progressRepositoryProvider;

  public RevisionRagProvider_Factory(Provider<ProgressRepository> progressRepositoryProvider) {
    this.progressRepositoryProvider = progressRepositoryProvider;
  }

  @Override
  public RevisionRagProvider get() {
    return newInstance(progressRepositoryProvider.get());
  }

  public static RevisionRagProvider_Factory create(
      Provider<ProgressRepository> progressRepositoryProvider) {
    return new RevisionRagProvider_Factory(progressRepositoryProvider);
  }

  public static RevisionRagProvider newInstance(ProgressRepository progressRepository) {
    return new RevisionRagProvider(progressRepository);
  }
}
