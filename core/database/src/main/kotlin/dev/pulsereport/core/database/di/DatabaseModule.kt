package dev.pulsereport.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.pulsereport.core.database.MoodDao
import dev.pulsereport.core.database.PulseDatabase
import dev.pulsereport.core.database.RoomSourcePriorityRepository
import dev.pulsereport.core.database.SourcePriorityDao
import dev.pulsereport.core.database.SourcePriorityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePulseDatabase(@ApplicationContext context: Context): PulseDatabase =
        Room.databaseBuilder(context, PulseDatabase::class.java, "pulsereport.db")
            .build()

    @Provides
    fun provideMoodDao(database: PulseDatabase): MoodDao = database.moodDao()

    @Provides
    fun provideSourcePriorityDao(database: PulseDatabase): SourcePriorityDao = database.sourcePriorityDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SourcePriorityBindModule {

    @Binds
    abstract fun bindSourcePriorityRepository(impl: RoomSourcePriorityRepository): SourcePriorityRepository
}
