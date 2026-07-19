package dev.pulsereport.feature.sources.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pulsereport.feature.sources.AppLabelResolver
import dev.pulsereport.feature.sources.PackageManagerAppLabelResolver

@Module
@InstallIn(SingletonComponent::class)
abstract class SourcesModule {

    @Binds
    abstract fun bindAppLabelResolver(impl: PackageManagerAppLabelResolver): AppLabelResolver
}
