package dev.pulsereport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch milliseconds of when the mood was recorded. */
    val recordedAt: Long,
    /** 1 (very low) to 5 (very high). */
    val rating: Int,
    val note: String?,
)
