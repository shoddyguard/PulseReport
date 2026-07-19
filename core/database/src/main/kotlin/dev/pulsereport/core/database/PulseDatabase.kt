package dev.pulsereport.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MoodEntryEntity::class, SourcePriorityEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun sourcePriorityDao(): SourcePriorityDao
}
