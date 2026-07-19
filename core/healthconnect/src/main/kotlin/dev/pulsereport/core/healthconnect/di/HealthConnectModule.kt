package dev.pulsereport.core.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pulsereport.core.healthconnect.HealthConnectRepository
import dev.pulsereport.core.healthconnect.HealthRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {

    @Binds
    abstract fun bindHealthRepository(impl: HealthConnectRepository): HealthRepository
}
