package com.example.ai.edge.eliza.core.data.chat;

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
public final class RagProviderFactoryImpl_Factory implements Factory<RagProviderFactoryImpl> {
  private final Provider<ChapterRagProvider> chapterRagProvider;

  private final Provider<RevisionRagProvider> revisionRagProvider;

  private final Provider<GeneralRagProvider> generalRagProvider;

  private final Provider<ExerciseRagProvider> exerciseRagProvider;

  public RagProviderFactoryImpl_Factory(Provider<ChapterRagProvider> chapterRagProvider,
      Provider<RevisionRagProvider> revisionRagProvider,
      Provider<GeneralRagProvider> generalRagProvider,
      Provider<ExerciseRagProvider> exerciseRagProvider) {
    this.chapterRagProvider = chapterRagProvider;
    this.revisionRagProvider = revisionRagProvider;
    this.generalRagProvider = generalRagProvider;
    this.exerciseRagProvider = exerciseRagProvider;
  }

  @Override
  public RagProviderFactoryImpl get() {
    return newInstance(chapterRagProvider.get(), revisionRagProvider.get(), generalRagProvider.get(), exerciseRagProvider.get());
  }

  public static RagProviderFactoryImpl_Factory create(
      Provider<ChapterRagProvider> chapterRagProvider,
      Provider<RevisionRagProvider> revisionRagProvider,
      Provider<GeneralRagProvider> generalRagProvider,
      Provider<ExerciseRagProvider> exerciseRagProvider) {
    return new RagProviderFactoryImpl_Factory(chapterRagProvider, revisionRagProvider, generalRagProvider, exerciseRagProvider);
  }

  public static RagProviderFactoryImpl newInstance(ChapterRagProvider chapterRagProvider,
      RevisionRagProvider revisionRagProvider, GeneralRagProvider generalRagProvider,
      ExerciseRagProvider exerciseRagProvider) {
    return new RagProviderFactoryImpl(chapterRagProvider, revisionRagProvider, generalRagProvider, exerciseRagProvider);
  }
}
