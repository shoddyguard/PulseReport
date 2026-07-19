package dev.pulsereport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(entry: MoodEntryEntity): Long

    @Query("SELECT * FROM mood_entries ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun delete(id: Long)
}
